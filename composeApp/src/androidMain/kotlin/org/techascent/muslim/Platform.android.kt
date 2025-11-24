package org.techascent.muslim

import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
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
import org.techascent.muslim.prayer.uimodel.AddressInfo
import java.io.File
import java.util.Locale
import kotlin.math.*
import android.content.Intent
import android.content.IntentFilter
import androidx.core.net.toUri
import android.content.res.Resources
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.datetime.Instant
import okio.Path.Companion.toPath
import java.util.concurrent.TimeUnit
import kotlin.text.get
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlin.compareTo
import kotlin.or
import kotlin.text.compareTo

private var mediaPlayer: android.media.MediaPlayer? = null


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
        .setCancelable(false)
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

actual suspend fun getPlaceName(latitude: Double, longitude: Double): AddressInfo {
    val defaultAddress = AddressInfo(
        district = null,
        city = null,
        country = null,
        address = "Unknown Location"
    )
    val context = appContext ?: return defaultAddress
    return withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            addresses?.firstOrNull()?.let {
                AddressInfo(
                    district = it.subAdminArea,
                    city = it.locality,
                    country = it.countryName,
                    address = addresses[0].getAddressLine(0)
                )
            } ?: defaultAddress
        } catch (e: Exception) {
            defaultAddress
        }
    }
}


actual fun openNearbyMosques() {
    val context = appContext ?: return
    val mapUri = "geo:0,0?q=mosque".toUri()

    val googleMapsIntent = Intent(Intent.ACTION_VIEW, mapUri).apply {
        setPackage("com.google.android.apps.maps")
    }

    if (googleMapsIntent.resolveActivity(context.packageManager) != null) {
        context.startActivity(googleMapsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } else {
        val genericMapIntent = Intent(Intent.ACTION_VIEW, mapUri)
        if (genericMapIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(genericMapIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }
}

actual fun getScreenWidthPx(): Int = Resources.getSystem().displayMetrics.widthPixels
actual fun getScreenHeightPx(): Int = Resources.getSystem().displayMetrics.heightPixels

actual fun createDataStore(producePath: () -> String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(
        produceFile = { producePath().toPath() }
    )

actual fun getPrayerNotificationService(): PrayerNotificationService {
    return AndroidPrayerNotificationService(appContext!!)
}

class AndroidPrayerNotificationService(private val context: Context) : PrayerNotificationService {
    companion object {
        private const val CHANNEL_ID = "prayer_times"
        private const val CHANNEL_NAME = "Prayer Times"
        private const val NOTIFICATION_ID_BASE = 1000
        private const val STOP_ACTION = "org.techascent.muslim.STOP_AUDIO"
    }

    init {
        createNotificationChannel()
        registerStopReceiver()
    }

    private fun registerStopReceiver() {
        val receiver = StopAudioReceiver()
        val intentFilter = IntentFilter(STOP_ACTION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, intentFilter, Context.RECEIVER_EXPORTED)
        } else {
            context.registerReceiver(receiver, intentFilter)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for prayer times"
                enableVibration(true)
                setShowBadge(true)
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    override suspend fun scheduleNotification(
        prayerName: String,
        scheduledTime: Instant,
        title: String,
        message: String,
        audioUrl: String
    ) {
        val currentTimeMillis = System.currentTimeMillis()
        val scheduledTimeMillis = scheduledTime.toEpochMilliseconds()
        val delay = scheduledTimeMillis - currentTimeMillis

        if (delay <= 0) {
            showNotificationImmediately(prayerName, title, message, showStopButton = true)
            return
        }

        val inputData = workDataOf(
            "prayer_name" to prayerName,
            "title" to title,
            "message" to message,
            "audio_url" to audioUrl
        )

        val notificationRequest = OneTimeWorkRequestBuilder<PrayerNotificationWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .addTag("prayer_$prayerName")
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "prayer_$prayerName",
            ExistingWorkPolicy.REPLACE,
            notificationRequest
        )
    }

    private fun showNotificationImmediately(
        prayerName: String,
        title: String,
        message: String,
        showStopButton: Boolean = false
    ) {
        val notificationId = prayerName.hashCode()
        val intent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(STOP_ACTION).apply {
            setPackage(context.packageName)
        }
        val stopPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(longArrayOf(0, 500, 250, 500))
            .setSound(android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION))

        if (showStopButton) {
            builder.addAction(
                android.R.drawable.ic_media_pause,
                "Stop",
                stopPendingIntent
            )
        }

        NotificationManagerCompat.from(context).notify(notificationId, builder.build())
    }

    private suspend fun downloadAndCacheAudio(audioUrl: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val fileName = "prayer_audio_${audioUrl.hashCode()}.mp3"
                val cacheFile = File(appContext?.cacheDir, fileName)

                // Return cached file if it already exists
                if (cacheFile.exists()) {
                    Log.d("downloadAndCacheAudio", "Using cached audio: ${cacheFile.absolutePath}")
                    return@withContext cacheFile.absolutePath
                }

                Log.d("downloadAndCacheAudio", "Downloading audio from: $audioUrl")
                val url = java.net.URL(audioUrl)
                url.openStream().use { input ->
                    cacheFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                Log.d("downloadAndCacheAudio", "Audio cached to: ${cacheFile.absolutePath}")
                cacheFile.absolutePath
            } catch (e: Exception) {
                Log.e("downloadAndCacheAudio", "Failed to cache audio: ${e.message}", e)
                null
            }
        }
    }

    override suspend fun playAudio(audioUrl: String) {
        try {
            withContext(Dispatchers.IO) {
                Log.d("playAudio", "Starting audio playback")

                val cachedAudioPath = downloadAndCacheAudio(audioUrl) ?: run {
                    Log.e("playAudio", "Failed to cache audio")
                    return@withContext
                }

                mediaPlayer?.release()
                mediaPlayer = android.media.MediaPlayer().apply {
                    setAudioStreamType(AudioManager.STREAM_NOTIFICATION)
                    setDataSource(cachedAudioPath)
                    setOnPreparedListener { mp ->
                        Log.d("playAudio", "Audio prepared, starting playback")
                        mp.start()
                    }
                    setOnCompletionListener { mp ->
                        Log.d("playAudio", "Audio completed")
                        mp.release()
                        mediaPlayer = null
                    }
                    setOnErrorListener { mp, what, extra ->
                        Log.e("playAudio", "MediaPlayer Error: $what, $extra")
                        mp.release()
                        mediaPlayer = null
                        false
                    }
                    prepareAsync()
                }
            }
        } catch (e: Exception) {
            Log.e("playAudio", "Failed: ${e.message}", e)
        }
    }


    override suspend fun cancelNotification(notificationId: String) {
        WorkManager.getInstance(context).cancelUniqueWork("prayer_$notificationId")
    }

    override suspend fun cancelAllNotifications() {
        WorkManager.getInstance(context).cancelAllWork()
    }
}


class PrayerNotificationWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val prayerName = inputData.getString("prayer_name") ?: return Result.retry()
            val title = inputData.getString("title") ?: "Prayer Time"
            val message = inputData.getString("message") ?: "Time for prayer"
            val audioUrl = inputData.getString("audio_url") ?: ""

            showNotificationImmediately(prayerName, title, message, showStopButton = true)

            if (audioUrl.isNotEmpty()) {
                getPrayerNotificationService().playAudio(audioUrl)
            }

            // Keep worker alive for audio to play
            kotlinx.coroutines.delay(15000)
            Result.success()
        } catch (e: Exception) {
            Log.e("PrayerNotificationWorker", "Error showing notification", e)
            Result.retry()
        }
    }

    private fun showNotificationImmediately(
        prayerName: String,
        title: String,
        message: String,
        showStopButton: Boolean = true
    ) {
        val notificationId = prayerName.hashCode()
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent("org.techascent.muslim.STOP_AUDIO").apply {
            setPackage(applicationContext.packageName)
        }
        val stopPendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            notificationId,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(applicationContext, "prayer_times")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(longArrayOf(0, 500, 250, 500))
            .setSound(android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION))

        if (showStopButton) {
            builder.addAction(
                android.R.drawable.ic_media_pause,
                "Stop",
                stopPendingIntent
            )
        }

        NotificationManagerCompat.from(applicationContext).notify(notificationId, builder.build())
    }
}


class StopAudioReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "org.techascent.muslim.STOP_AUDIO") {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                    Log.d("StopAudioReceiver", "Audio stopped")
                }
                it.release()
                mediaPlayer = null
            }
        }
    }
}








