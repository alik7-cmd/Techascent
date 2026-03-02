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
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.workDataOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import org.techascent.muslim.MainActivity
import org.techascent.muslim.PrayerNotificationService
import org.techascent.muslim.R
import org.techascent.muslim.worker.PrayerNotificationWorker
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

var mediaPlayer: MediaPlayer? = null

class AndroidPrayerNotificationService(private val context: Context) : PrayerNotificationService {
    companion object {
        private const val CHANNEL_ID = "prayer_times"
        private const val CHANNEL_NAME = "Prayer Times"
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
            context.registerReceiver(receiver, intentFilter, Context.RECEIVER_NOT_EXPORTED)
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
                // Don't set a default sound on the channel so we can play audio ourselves
                setSound(null, null)
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
        audioFile: String
    ) {
        val currentTimeMillis = System.currentTimeMillis()
        val scheduledTimeMillis = scheduledTime.toEpochMilliseconds()
        val delay = (scheduledTimeMillis - currentTimeMillis).coerceAtLeast(0)

        val inputData = workDataOf(
            "prayer_name" to prayerName,
            "title" to title,
            "message" to message,
            "audio_file" to audioFile
        )

        val builder = OneTimeWorkRequestBuilder<PrayerNotificationWorker>()
            .setInputData(inputData)
            .addTag("prayer_$prayerName")

        if (delay > 0) {
            builder.setInitialDelay(delay, TimeUnit.MILLISECONDS)
        } else if (audioFile.isNotEmpty()) {
            // Only use expedited (foreground service) when audio needs to play
            builder.setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
        }

        WorkManager.getInstance(context).enqueueUniqueWork(
            "prayer_$prayerName",
            ExistingWorkPolicy.REPLACE,
            builder.build()
        )

        Log.d("PrayerNotification", "Scheduled $prayerName in ${delay / 1000}s")
    }

    override suspend fun playAudio(audioFile: String) {
        try {
            withContext(Dispatchers.IO) {
                suspendCancellableCoroutine<Unit> { cont ->
                    mediaPlayer?.release()
                    val player = MediaPlayer()
                    player.setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )

                    // Play from local raw resource — no network required
                    val afd = context.resources.openRawResourceFd(R.raw.azan)
                    player.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    afd.close()

                    player.setOnPreparedListener(
                        MediaPlayer.OnPreparedListener { mp ->
                            Log.d("playAudio", "Audio prepared, starting playback from local resource")
                            mp.start()
                        }
                    )

                    player.setOnCompletionListener(
                        MediaPlayer.OnCompletionListener { mp ->
                            Log.d("playAudio", "Audio completed")
                            mp.release()
                            mediaPlayer = null
                            if (cont.isActive) cont.resume(Unit)
                        }
                    )

                    player.setOnErrorListener(
                        MediaPlayer.OnErrorListener { mp, what, extra ->
                            Log.e("playAudio", "MediaPlayer Error: $what, $extra")
                            mp.release()
                            mediaPlayer = null
                            if (cont.isActive) cont.resume(Unit)
                            true
                        }
                    )

                    mediaPlayer = player
                    player.prepareAsync()

                    cont.invokeOnCancellation {
                        try {
                            if (player.isPlaying) player.stop()
                            player.release()
                        } catch (_: Exception) {}
                        mediaPlayer = null
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("playAudio", "Failed: ${e.message}", e)
        }
    }


    override suspend fun cancelNotification(notificationId: String) {
        WorkManager.getInstance(context).cancelUniqueWork("prayer_$notificationId")
        // Also dismiss the notification
        NotificationManagerCompat.from(context).cancel(notificationId.hashCode())
    }

    override suspend fun cancelAllNotifications() {
        // Cancel all prayer-related work
        WorkManager.getInstance(context).cancelAllWorkByTag("prayer_FAJR")
        WorkManager.getInstance(context).cancelAllWorkByTag("prayer_SALAT_UD_DUHA")
        WorkManager.getInstance(context).cancelAllWorkByTag("prayer_DUHR")
        WorkManager.getInstance(context).cancelAllWorkByTag("prayer_ASR")
        WorkManager.getInstance(context).cancelAllWorkByTag("prayer_MAGHRIB")
        WorkManager.getInstance(context).cancelAllWorkByTag("prayer_ISHA")
        WorkManager.getInstance(context).cancelAllWorkByTag("prayer_TEST")
        for (i in 1..5) {
            WorkManager.getInstance(context).cancelAllWorkByTag("prayer_TEST_REPEAT_$i")
        }
        // Stop any currently playing audio
        mediaPlayer?.let {
            if (it.isPlaying) it.stop()
            it.release()
        }
        mediaPlayer = null
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
            // Also dismiss all prayer notifications
            context?.let {
                NotificationManagerCompat.from(it).cancelAll()
            }
        }
    }
}
