package org.techascent.muslim.worker

import android.Manifest
import android.R as Muslim
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.techascent.muslim.MainActivity
import org.techascent.muslim.servive.mediaPlayer
import java.io.File
import java.net.URL


const val AZAN_URL =
    "https://archive.org/download/adhan.recordings.from.doha.qatar/Adhan_Doha_Qatar_01_Fajr_Adhan.ogg"

class PrayerNotificationWorker(
    val context: Context, params: WorkerParameters
) :
    CoroutineWorker(context, params) {

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override suspend fun doWork(): Result {
        return try {
            val prayerName = inputData.getString("prayer_name") ?: return Result.retry()
            val title = inputData.getString("title") ?: "Prayer Time"
            val message = inputData.getString("message") ?: "Time for prayer"
            val audioUrl = inputData.getString("audio_url") ?: AZAN_URL

            showNotificationImmediately(prayerName, title, message)

            if (audioUrl.isNotEmpty()) {
                playAudio(audioUrl = audioUrl)
            }

            // Keep worker alive for audio to play
            delay(15000)
            Result.success()
        } catch (e: Exception) {
            Log.e("PrayerNotificationWorker", "Error showing notification", e)
            Result.retry()
        }
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

    suspend fun playAudio(audioUrl: String) {
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

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showNotificationImmediately(
        prayerName: String,
        title: String,
        message: String
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

        NotificationManagerCompat.from(applicationContext).notify(notificationId, builder.build())
    }
}