package org.techascent.aifinder.data.subscription

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.ServerValue
import com.google.firebase.database.Transaction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import org.techascent.aifinder.BuildConfig
import org.techascent.aifinder.data.location.RealtimeDatabasePaths
import org.techascent.aifinder.domain.subscription.ScanAccess
import org.techascent.aifinder.domain.subscription.ScanAccessRequiredException
import org.techascent.aifinder.domain.subscription.ScanCreditWallet
import org.techascent.aifinder.domain.subscription.ScanPurchase
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FirebaseScanAccessDataSource(
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance(BuildConfig.FIREBASE_DATABASE_URL),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val functions: FirebaseFunctions = FirebaseFunctions.getInstance("us-central1"),
) : ScanAccessRemoteDataSource {
    override suspend fun load(userId: String): ScanAccess {
        check(auth.currentUser?.uid == userId) { "The signed-in user changed. Please try again." }
        val response = functions.getHttpsCallable("ensureScanWallet").call().await().data as? Map<*, *>
        val count = response?.get("remainingScans")?.let(::validCount)
        checkNotNull(count) { "Invalid scan balance returned by the server." }
        check(auth.currentUser?.uid == userId) { "The signed-in user changed. Please try again." }
        return ScanAccess(userId, count)
    }

    override suspend fun consume(userId: String, scanId: String): ScanAccess {
        require(scanId.isNotBlank() && scanId.none { it in ".#$[]/" }) { "Invalid scan ID." }
        val ref = database.getReference(RealtimeDatabasePaths.scanWallet(userId))
        // Seed the transaction cache so an existing wallet isn't mistaken for a missing one.
        ref.get().await()
        return suspendCancellableCoroutine { continuation ->
            ref.runTransaction(object : Transaction.Handler {
                override fun doTransaction(data: MutableData): Transaction.Result {
                    if (data.value == null) return Transaction.success(data)
                    val remaining = data.child("remainingScans").value?.let(::validCount) ?: return Transaction.abort()
                    if (data.child("usedScans").child(scanId).value == true) {
                        data.child("activeSubscription").value = remaining > 0
                        return Transaction.success(data)
                    }
                    if (remaining == 0L) return Transaction.abort()
                    val wallet = ScanCreditWallet(remaining).consume(scanId)
                    data.child("remainingScans").value = wallet.remainingScans
                    data.child("activeSubscription").value = wallet.activeSubscription
                    data.child("usedScans").child(scanId).value = true
                    return Transaction.success(data)
                }

                override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {
                    if (!continuation.isActive) return
                    when {
                        error != null -> continuation.resumeWithException(error.toException())
                        !committed -> continuation.resumeWithException(ScanAccessRequiredException())
                        snapshot == null -> continuation.resumeWithException(IllegalStateException("Missing scan balance."))
                        snapshot.child("usedScans").child(scanId).value != true ->
                            continuation.resumeWithException(ScanAccessRequiredException())
                        else -> continuation.resumeWith(runCatching { ScanAccess(userId, remaining(snapshot)) })
                    }
                }
            }, false)
        }
    }

    override suspend fun recordPurchase(userId: String, purchase: ScanPurchase): ScanAccess {
        require(purchase.scansGranted > 0) { "Purchase grants no scans." }
        // Store transaction ids (e.g. Play's "GPA.123…") can contain characters Firebase keys forbid.
        val key = purchase.purchaseId.map { if (it in ".#$[]/") '_' else it }.joinToString("")
        val historyRef = database.getReference(RealtimeDatabasePaths.purchases(userId)).child(key)
        val existingStatus = historyRef.child("status").get().await().value
        if (existingStatus != ScanPurchase.STATUS_CREDITED) {
            historyRef.setValue(purchase.toMap(ScanPurchase.STATUS_PENDING)).await()
        }

        val walletRef = database.getReference(RealtimeDatabasePaths.scanWallet(userId))
        walletRef.get().await()
        val access = suspendCancellableCoroutine { continuation ->
            walletRef.runTransaction(object : Transaction.Handler {
                override fun doTransaction(data: MutableData): Transaction.Result {
                    val raw = data.child("remainingScans").value
                    val remaining = if (raw == null) 0L else validCount(raw) ?: return Transaction.abort()
                    val credited = data.child("creditedPurchases").child(key)
                    val newBalance = if (credited.value == true) remaining else remaining + purchase.scansGranted
                    data.child("remainingScans").value = newBalance
                    data.child("activeSubscription").value = ScanCreditWallet(newBalance).activeSubscription
                    credited.value = true
                    return Transaction.success(data)
                }

                override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {
                    if (!continuation.isActive) return
                    when {
                        error != null -> continuation.resumeWithException(error.toException())
                        !committed || snapshot == null ->
                            continuation.resumeWithException(IllegalStateException("Could not add purchased scans."))
                        else -> continuation.resumeWith(runCatching { ScanAccess(userId, remaining(snapshot)) })
                    }
                }
            }, false)
        }

        historyRef.updateChildren(
            mapOf("status" to ScanPurchase.STATUS_CREDITED, "creditedAt" to ServerValue.TIMESTAMP)
        ).await()
        return access
    }

    private fun ScanPurchase.toMap(status: String) = mapOf(
        "purchaseId" to purchaseId,
        "productId" to productId,
        "scansGranted" to scansGranted,
        "purchasedAt" to purchasedAt,
        "store" to store,
        "status" to status,
        "recordedAt" to ServerValue.TIMESTAMP,
    )

    private fun remaining(snapshot: DataSnapshot): Long {
        val value = snapshot.child("remainingScans").value ?: return 0
        return checkNotNull(validCount(value)) { "Invalid scan balance." }
    }

    private fun validCount(value: Any): Long? {
        val number = value as? Number ?: return null
        val count = number.toLong()
        return count.takeIf { it >= 0 && number.toDouble() == it.toDouble() }
    }
}
