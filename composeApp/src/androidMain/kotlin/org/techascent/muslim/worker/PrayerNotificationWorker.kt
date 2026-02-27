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
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.techascent.muslim.MainActivity
import org.techascent.muslim.servive.mediaPlayer
import java.io.File
import java.net.URL
import kotlin.coroutines.resume


const val AZAN_URL =
    "https://archive.org/download/adhan.recordings.from.doha.qatar/Adhan_Doha_Qatar_01_Fajr_Adhan.ogg"

class PrayerNotificationWorker(
    val context: Context, params: WorkerParameters
) :
    CoroutineWorker(context, params) {

    companion object {
        private const val CHANNEL_ID = "prayer_times"
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override suspend fun doWork(): Result {
        return try {
            val prayerName = inputData.getString("prayer_name") ?: return Result.retry()
            val title = inputData.getString("title") ?: "Prayer Time"
            val message = inputData.getString("message") ?: "Time for prayer"
            val audioUrl = inputData.getString("audio_url") ?: AZAN_URL

            // Promote to foreground so the worker stays alive for audio playback
            setForeground(createForegroundInfo(prayerName, title, message))

            if (audioUrl.isNotEmpty()) {
                playAudioAndWait(audioUrl)
            }

            Result.success()
        } catch (e: Exception) {
            Log.e("PrayerNotificationWorker", "Error showing notification", e)
            Result.failure()
        }
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

    private suspend fun downloadAndCacheAudio(audioUrl: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val fileName = "prayer_audio_${audioUrl.hashCode()}.ogg"
                val cacheFile = File(context.cacheDir, fileName)

                // Return cached file if it already exists
                if (cacheFile.exists() && cacheFile.length() > 0) {
                    Log.d("PrayerWorker", "Using cached audio: ${cacheFile.absolutePath}")
                    return@withContext cacheFile.absolutePath
                }

                Log.d("PrayerWorker", "Downloading audio from: $audioUrl")
                val url = URL(audioUrl)
                url.openStream().use { input ->
                    cacheFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                Log.d("PrayerWorker", "Audio cached to: ${cacheFile.absolutePath}")
                cacheFile.absolutePath
            } catch (e: Exception) {
                Log.e("PrayerWorker", "Failed to cache audio: ${e.message}", e)
                null
            }
        }
    }

    /**
     * Plays audio and suspends until playback is complete (or an error occurs).
     * This ensures the worker stays alive for the full duration of the Azan.
     */
    private suspend fun playAudioAndWait(audioUrl: String) {
        try {
            val cachedAudioPath = downloadAndCacheAudio(audioUrl)
            if (cachedAudioPath == null) {
                Log.e("PrayerWorker", "Failed to cache audio, skipping playback")
                return
            }

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
                    player.setDataSource(cachedAudioPath)

                    player.setOnPreparedListener(
                        MediaPlayer.OnPreparedListener { mp ->
                            Log.d("PrayerWorker", "Audio prepared, starting playback")
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