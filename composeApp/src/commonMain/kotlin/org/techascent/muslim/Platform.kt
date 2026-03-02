package org.techascent.muslim

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import org.techascent.muslim.common.location.LocationService
import org.techascent.muslim.prayer.uimodel.AddressInfo

expect fun playBeep()

expect fun performHapticFeedback()

expect fun showNativeResetDialog(
    title: String,
    message: String,
    confirmText: String,
    cancelText: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit = {}
)

expect fun getQiblaDirection(currentLat: Double, currentLng: Double): Flow<Float>

expect fun provideDataStore(): DataStore<Preferences>

expect suspend fun readCsvFile(filename: String): List<String>

expect fun getPlatformLocationService(): LocationService

expect suspend fun getPlaceName(latitude: Double, longitude: Double): AddressInfo

expect fun openNearbyMosques()

expect fun getScreenWidthPx(): Int
expect fun getScreenHeightPx(): Int

expect fun createDataStore(producePath: () -> String): DataStore<Preferences>

expect fun getPrayerNotificationService(): PrayerNotificationService

interface PrayerNotificationService {
    suspend fun scheduleNotification(
        prayerName: String,
        scheduledTime: Instant,
        title: String = "Prayer Time",
        message: String,
        audioFile: String = ""
    )

    suspend fun playAudio(audioFile: String)

    suspend fun cancelNotification(notificationId: String)

    suspend fun cancelAllNotifications()
}
