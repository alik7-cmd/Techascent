package org.techascent.muslim

import android.app.AlertDialog
import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.hardware.*
import android.location.Geocoder
import androidx.datastore.core.DataStore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.techascent.muslim.preference.DATA_STORE_FILE_NAME
import org.techascent.muslim.preference.createDataStore
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.techascent.muslim.common.location.LocationService
import java.io.File
import java.util.Locale
import kotlin.math.*


actual fun playBeep() {
    val toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
    toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
}

private var appContext: Context? = null

fun initHaptics(context: Context) {
    appContext = context
}

actual fun provideDataStore(): DataStore<Preferences> {
    return createDataStore {
        appContext!!.filesDir.resolve(DATA_STORE_FILE_NAME).absolutePath
    }
}


actual fun performHapticFeedback() {
    val context = appContext ?: return
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(50)
    }
}

actual fun showNativeResetDialog(
    title: String,
    message: String,
    confirmText: String,
    cancelText: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    val context = appContext ?: return
    AlertDialog.Builder(context)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(confirmText) { _, _ -> onConfirm() }
        .setNegativeButton(cancelText) { _, _ -> onCancel() }
        .show()
}

actual fun getQiblaDirection(currentLat: Double, currentLng: Double): Flow<Float> = callbackFlow {
    val sensorManager = appContext?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    val gravity = FloatArray(3)
    val geomagnetic = FloatArray(3)

    val listener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            when (event.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> {
                    System.arraycopy(event.values, 0, gravity, 0, event.values.size)
                }

                Sensor.TYPE_MAGNETIC_FIELD -> {
                    System.arraycopy(event.values, 0, geomagnetic, 0, event.values.size)
                }
            }

            val R = FloatArray(9)
            val I = FloatArray(9)

            if (SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)) {
                val orientation = FloatArray(3)
                SensorManager.getOrientation(R, orientation)
                val azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()

                val bearingToQibla =
                    calculateBearing(currentLat, currentLng, 21.4225, 39.8262) // Kaaba
                val direction = (bearingToQibla - azimuth + 360) % 360
                trySend(direction)
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI)
    sensorManager.registerListener(listener, magnetometer, SensorManager.SENSOR_DELAY_UI)

    awaitClose {
        sensorManager.unregisterListener(listener)
    }
}

// Helper to compute bearing between two lat/lng points
private fun calculateBearing(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
    val φ1 = Math.toRadians(lat1)
    val φ2 = Math.toRadians(lat2)
    val Δλ = Math.toRadians(lon2 - lon1)

    val y = sin(Δλ) * cos(φ2)
    val x = cos(φ1) * sin(φ2) - sin(φ1) * cos(φ2) * cos(Δλ)

    return ((Math.toDegrees(atan2(y, x)) + 360) % 360).toFloat()
}

actual suspend fun readCsvFile(filename: String): List<String> {
    val file = File(appContext?.filesDir, filename)
    return file.readLines()
}

actual fun getPlatformLocationService(): LocationService {
    return AndroidLocationService(appContext!!)
}

actual suspend fun getPlaceName(latitude: Double, longitude: Double): String {
    val context = appContext ?: return "Unknown location"
    return withContext(Dispatchers.IO) {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses = geocoder.getFromLocation(latitude, longitude, 1)
        if (addresses != null && addresses.isNotEmpty()) {
            addresses[0].getAddressLine(0) ?: "Unknown location"
        } else {
            "Unknown location"
        }
    }
}
