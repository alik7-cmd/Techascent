package org.techascent.muslim.servive

import android.Manifest
import android.R as Muslim
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import org.techascent.muslim.MainActivity
import org.techascent.muslim.PrayerNotificationService
import org.techascent.muslim.worker.PrayerNotificationWorker
import java.io.File
import java.net.URL
import java.util.concurrent.TimeUnit

var mediaPlayer: MediaPlayer? = null

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
            ContextCompat.registerReceiver(
                context,
                receiver,
                intentFilter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
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

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
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
            showNotificationImmediately(prayerName, title, message)
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

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showNotificationImmediately(
        prayerName: String,
        title: String,
        message: String
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
            .setSmallIcon(Muslim.drawable.stat_sys_headset)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(longArrayOf(0, 500, 250, 500))
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))

        builder.addAction(
            Muslim.drawable.ic_media_pause,
            "Stop",
            stopPendingIntent
        )

        NotificationManagerCompat.from(context).notify(notificationId, builder.build())
    }

    private suspend fun downloadAndCacheAudio(audioUrl: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val fileName = "prayer_audio_${audioUrl.hashCode()}.mp3"
                val cacheFile = File(context.cacheDir, fileName)

                // Return cached file if it already exists
                if (cacheFile.exists()) {
                    Log.d("downloadAndCacheAudio", "Using cached audio: ${cacheFile.absolutePath}")
                    return@withContext cacheFile.absolutePath
                }

                Log.d("downloadAndCacheAudio", "Downloading audio from: $audioUrl")
                val url = URL(audioUrl)
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
                mediaPlayer = MediaPlayer().apply {
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
