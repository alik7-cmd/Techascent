package org.techascent.aifinder.data.subscription

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import org.techascent.aifinder.BuildConfig
import org.techascent.aifinder.data.location.RealtimeDatabasePaths
import org.techascent.aifinder.domain.subscription.ScanAccess
import org.techascent.aifinder.domain.subscription.ScanAccessRequiredException
import org.techascent.aifinder.domain.subscription.ScanCreditWallet
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FirebaseScanAccessDataSource(
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance(BuildConfig.FIREBASE_DATABASE_URL),
) : ScanAccessRemoteDataSource {
    override suspend fun load(userId: String): ScanAccess {
        val ref = database.getReference(RealtimeDatabasePaths.scanWallet(userId))
        ref.get().await()
        return suspendCancellableCoroutine { continuation ->
            ref.runTransaction(object : Transaction.Handler {
                override fun doTransaction(data: MutableData): Transaction.Result {
                    val raw = data.child("remainingScans").value
                    val count = if (raw == null) 0L else validCount(raw) ?: return Transaction.abort()
                    data.child("remainingScans").value = count
                    data.child("activeSubscription").value = ScanCreditWallet(count).activeSubscription
                    return Transaction.success(data)
                }

                override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {
                    if (!continuation.isActive) return
                    when {
                        error != null -> continuation.resumeWithException(error.toException())
                        !committed || snapshot == null ->
                            continuation.resumeWithException(IllegalStateException("Could not initialize scan wallet: invalid balance."))
                        else -> continuation.resumeWith(runCatching { ScanAccess(userId, remaining(snapshot)) })
                    }
                }
            }, false)
        }
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
