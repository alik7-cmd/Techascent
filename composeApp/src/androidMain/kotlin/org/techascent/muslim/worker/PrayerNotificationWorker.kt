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
        val audioFile = inputData.getString("audio_file") ?: ""
        return createForegroundInfo(
            prayerName = inputData.getString("prayer_name") ?: "PRAYER",
            title = inputData.getString("title") ?: "Prayer Time",
            message = inputData.getString("message") ?: "Time for prayer",
            shouldPlayAudio = audioFile.isNotEmpty()
        )
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override suspend fun doWork(): Result {
        return try {
            val prayerName = inputData.getString("prayer_name") ?: return Result.retry()
            val title = inputData.getString("title") ?: "Prayer Time"
            val message = inputData.getString("message") ?: "Time for prayer"
            val audioFile = inputData.getString("audio_file") ?: ""
            val shouldPlayAudio = audioFile.isNotEmpty()

            if (shouldPlayAudio) {
                // Adhan enabled: use foreground service to keep worker alive during audio playback
                try {
                    setForeground(createForegroundInfo(prayerName, title, message, true))
                } catch (e: Exception) {
                    Log.w("PrayerNotificationWorker", "Cannot start foreground: ${e.message}")
                    // Fallback: show notification with stop button, audio may get cut short
                    showNotification(prayerName, title, message, true)
                }
                playAudioAndWait(audioFile)
            } else {
                // Adhan disabled: just show a simple notification, no foreground service needed
                showNotification(prayerName, title, message, false)
            }

            Result.success()
        } catch (e: Exception) {
            Log.e("PrayerNotificationWorker", "Error showing notification", e)
            Result.failure()
        }
    }

    /**
     * Shows a regular (non-foreground) notification.
     * When shouldPlayAudio is true, includes "Stop Azan" button.
     * When false, shows a simple auto-dismissing notification.
     */
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showNotification(prayerName: String, title: String, message: String, shouldPlayAudio: Boolean) {
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

        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(Muslim.drawable.stat_sys_headset)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(longArrayOf(0, 500, 250, 500))

        if (shouldPlayAudio) {
            val stopIntent = Intent("org.techascent.muslim.STOP_AUDIO").apply {
                setPackage(applicationContext.packageName)
            }
            val stopPendingIntent = PendingIntent.getBroadcast(
                applicationContext,
                notificationId + 1,
                stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(
                Muslim.drawable.ic_media_pause,
                "Stop Azan",
                stopPendingIntent
            )
        }

        NotificationManagerCompat.from(applicationContext).notify(notificationId, builder.build())
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun createForegroundInfo(
        prayerName: String,
        title: String,
        message: String,
        shouldPlayAudio: Boolean
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

        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(Muslim.drawable.stat_sys_headset)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(longArrayOf(0, 500, 250, 500))

        if (shouldPlayAudio) {
            // Ongoing notification with Stop button when audio is playing
            builder.setAutoCancel(false)
            builder.setOngoing(true)

            val stopIntent = Intent("org.techascent.muslim.STOP_AUDIO").apply {
                setPackage(applicationContext.packageName)
            }
            val stopPendingIntent = PendingIntent.getBroadcast(
                applicationContext,
                notificationId + 1,
                stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(
                Muslim.drawable.ic_media_pause,
                "Stop Azan",
                stopPendingIntent
            )
        } else {
            // Auto-dismiss notification when no audio
            builder.setAutoCancel(true)
        }

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(
                notificationId,
                builder.build(),
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            ForegroundInfo(notificationId, builder.build())
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