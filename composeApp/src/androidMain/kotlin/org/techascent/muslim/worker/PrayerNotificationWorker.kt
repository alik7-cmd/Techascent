package org.techascent.muslim.worker

import android.Manifest
import android.R as Muslim
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.techascent.muslim.MainActivity
import org.techascent.muslim.R
import org.techascent.muslim.prayer.usecase.AZAN_AUDIO_FILE
import org.techascent.muslim.servive.mediaPlayer
import kotlin.coroutines.resume


class PrayerNotificationWorker(
    val context: Context, params: WorkerParameters
) :
    CoroutineWorker(context, params) {

    companion object {
        private const val CHANNEL_ID = "prayer_times"
    }

    /**
     * Required for setExpedited() — provides the ForegroundInfo when the system
     * needs to promote this worker to a foreground service.
     */
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override suspend fun getForegroundInfo(): ForegroundInfo {
        return createForegroundInfo(
            prayerName = inputData.getString("prayer_name") ?: "PRAYER",
            title = inputData.getString("title") ?: "Prayer Time",
            message = inputData.getString("message") ?: "Time for prayer"
        )
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override suspend fun doWork(): Result {
        return try {
            val prayerName = inputData.getString("prayer_name") ?: return Result.retry()
            val title = inputData.getString("title") ?: "Prayer Time"
            val message = inputData.getString("message") ?: "Time for prayer"
            val audioUrl = inputData.getString("audio_url") ?: AZAN_AUDIO_FILE

            // Try to promote to foreground; on Android 12+ this can fail from background
            try {
                setForeground(createForegroundInfo(prayerName, title, message))
            } catch (e: Exception) {
                Log.w("PrayerNotificationWorker", "Cannot start foreground: ${e.message}")
                // Fallback: show a regular notification instead
                showFallbackNotification(prayerName, title, message)
            }

            if (audioUrl.isNotEmpty()) {
                playAudioAndWait(audioUrl)
            }

            Result.success()
        } catch (e: Exception) {
            Log.e("PrayerNotificationWorker", "Error showing notification", e)
            Result.failure()
        }
    }

    /**
     * Fallback notification when foreground service cannot be started (Android 12+ background restriction).
     */
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showFallbackNotification(prayerName: String, title: String, message: String) {
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
            notificationId + 1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(Muslim.drawable.stat_sys_headset)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(longArrayOf(0, 500, 250, 500))
            .addAction(
                Muslim.drawable.ic_media_pause,
                "Stop Azan",
                stopPendingIntent
            )
            .build()

        NotificationManagerCompat.from(applicationContext).notify(notificationId, notification)
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun createForegroundInfo(
        prayerName: String,
        title: String,
        message: String
    ): ForegroundInfo {
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
            notificationId + 1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(Muslim.drawable.stat_sys_headset)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(longArrayOf(0, 500, 250, 500))
            .addAction(
                Muslim.drawable.ic_media_pause,
                "Stop Azan",
                stopPendingIntent
            )
            .build()

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(
                notificationId,
                notification,
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            ForegroundInfo(notificationId, notification)
        }
    }

    /**
     * Plays audio from local R.raw.azan resource and suspends until playback is complete.
     * This ensures the worker stays alive for the full duration of the Azan.
     * No network required — audio is bundled with the app.
     */
    private suspend fun playAudioAndWait(audioFile: String) {
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

                    // Play from local raw resource
                    val afd = context.resources.openRawResourceFd(R.raw.azan)
                    player.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    afd.close()

                    player.setOnPreparedListener(
                        MediaPlayer.OnPreparedListener { mp ->
                            Log.d("PrayerWorker", "Audio prepared, starting playback from local resource")
                            mp.start()
                        }
                    )

                    player.setOnCompletionListener(
                        MediaPlayer.OnCompletionListener { mp ->
                            Log.d("PrayerWorker", "Audio completed")
                            mp.release()
                            mediaPlayer = null
                            if (cont.isActive) cont.resume(Unit)
                        }
                    )

                    player.setOnErrorListener(
                        MediaPlayer.OnErrorListener { mp, what, extra ->
                            Log.e("PrayerWorker", "MediaPlayer Error: $what, $extra")
                            mp.release()
                            mediaPlayer = null
                            if (cont.isActive) cont.resume(Unit)
                            true
                        }
                    )

                    mediaPlayer = player
                    player.prepareAsync()

                    cont.invokeOnCancellation {
                        Log.d("PrayerWorker", "Coroutine cancelled, stopping audio")
                        try {
                            if (player.isPlaying) player.stop()
                            player.release()
                        } catch (_: Exception) {}
                        mediaPlayer = null
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("PrayerWorker", "Failed: ${e.message}", e)
        }
    }
}