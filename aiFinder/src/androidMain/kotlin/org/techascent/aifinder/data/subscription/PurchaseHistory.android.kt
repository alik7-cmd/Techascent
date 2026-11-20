package org.techascent.aifinder.data.subscription

import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import org.techascent.aifinder.BuildConfig
import org.techascent.aifinder.data.location.RealtimeDatabasePaths
import org.techascent.aifinder.domain.subscription.ScanPurchase
import java.text.DateFormat
import java.util.Date

internal actual fun createPurchaseHistoryRemote(): PurchaseHistoryRemote = object : PurchaseHistoryRemote {
    override suspend fun load(userId: String): List<ScanPurchase> {
        val snapshot = FirebaseDatabase.getInstance(BuildConfig.FIREBASE_DATABASE_URL)
            .getReference(RealtimeDatabasePaths.purchases(userId)).get().await()
        return snapshot.children.map { row ->
            fun text(field: String) = checkNotNull(row.child(field).getValue(String::class.java)) {
                "Purchase history contains an invalid $field."
            }
            fun number(field: String): Long {
                val value = checkNotNull(row.child(field).value as? Number) {
                    "Purchase history contains an invalid $field."
                }
                return value.toLong().also {
                    check(it >= 0 && value.toDouble() == it.toDouble()) { "Invalid purchase $field." }
                }
            }
            ScanPurchase(
                purchaseId = text("purchaseId"),
                productId = text("productId"),
                scansGranted = number("scansGranted"),
                purchasedAt = number("purchasedAt"),
                store = text("store"),
                status = text("status"),
            )
        }
    }
}

internal actual fun formatPurchaseDate(timestamp: Long): String =
    DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(timestamp))
