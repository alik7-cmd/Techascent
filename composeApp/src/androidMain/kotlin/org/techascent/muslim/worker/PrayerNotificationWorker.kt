package org.techascent.muslim.worker

import android.R
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.delay
import org.techascent.muslim.MainActivity
import org.techascent.muslim.getPrayerNotificationService

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
            delay(15000)
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
            .setSmallIcon(R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(longArrayOf(0, 500, 250, 500))
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))

        if (showStopButton) {
            builder.addAction(
                R.drawable.ic_media_pause,
                "Stop",
                stopPendingIntent
            )
        }

        NotificationManagerCompat.from(applicationContext).notify(notificationId, builder.build())
    }
}