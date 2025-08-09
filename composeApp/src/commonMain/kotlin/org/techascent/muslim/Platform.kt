package org.techascent.muslim

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.flow.Flow
import org.techascent.muslim.common.location.LocationService

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

expect suspend fun getPlaceName(latitude: Double, longitude: Double): String
