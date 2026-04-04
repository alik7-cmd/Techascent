package org.techascent.muslim.receiver

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.PowerManager
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import org.techascent.muslim.MainActivity
import org.techascent.muslim.R
import org.techascent.muslim.servive.mediaPlayer
import android.R as Muslim

/**
 * BroadcastReceiver triggered by AlarmManager at exact prayer times.
 * This guarantees the notification + azan fires at the precise scheduled moment,
 * even in Doze mode, because we use AlarmManager.setAlarmClock().
 */
class PrayerAlarmReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_PRAYER_NAME = "prayer_name"
        const val EXTRA_TITLE = "title"
        const val EXTRA_MESSAGE = "message"
        const val EXTRA_AUDIO_FILE = "audio_file"
        private const val CHANNEL_ID = "prayer_times"
        private const val TAG = "PrayerAlarmReceiver"
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onReceive(context: Context, intent: Intent?) {
        val prayerName = intent?.getStringExtra(EXTRA_PRAYER_NAME) ?: return
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Prayer Time"
        val message = intent.getStringExtra(EXTRA_MESSAGE) ?: "Time for prayer"
        val audioFile = intent.getStringExtra(EXTRA_AUDIO_FILE) ?: ""

        Log.d(TAG, "Alarm fired for $prayerName at exact time")

        // Acquire a wake lock to ensure audio plays fully
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "muslim:prayer_alarm_$prayerName"
        )
        wakeLock.acquire(5 * 60 * 1000L) // 5 minutes max

        try {
            if (audioFile.isNotEmpty()) {
                showNotificationWithStopButton(context, prayerName, title, message)
                playAzan(context, prayerName, wakeLock)
            } else {
                showSimpleNotification(context, prayerName, title, message)
                wakeLock.release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in alarm receiver", e)
            if (wakeLock.isHeld) wakeLock.release()
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showNotificationWithStopButton(
        context: Context,
        prayerName: String,
        title: String,
        message: String
    ) {
        val notificationId = prayerName.hashCode()

        val tapIntent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val tapPendingIntent = PendingIntent.getActivity(
            context, notificationId, tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent("org.techascent.muslim.STOP_AUDIO").apply {
            setPackage(context.packageName)
        }
        val stopPendingIntent = PendingIntent.getBroadcast(
            context, notificationId + 1, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(Muslim.drawable.stat_sys_headset)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(tapPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVibrate(longArrayOf(0, 500, 250, 500))
            .setAutoCancel(false)
            .setOngoing(true)
            .addAction(Muslim.drawable.ic_media_pause, "Stop Azan", stopPendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showSimpleNotification(
        context: Context,
        prayerName: String,
        title: String,
        message: String
    ) {
        val notificationId = prayerName.hashCode()

        val tapIntent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val tapPendingIntent = PendingIntent.getActivity(
            context, notificationId, tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(Muslim.drawable.stat_sys_headset)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(tapPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVibrate(longArrayOf(0, 500, 250, 500))
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    private fun playAzan(context: Context, prayerName: String, wakeLock: PowerManager.WakeLock) {
        try {
            // Release any existing player
            mediaPlayer?.let {
                if (it.isPlaying) it.stop()
                it.release()
            }
            mediaPlayer = null

            val player = MediaPlayer()
            player.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )

            val afd = context.resources.openRawResourceFd(R.raw.azan)
            player.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            afd.close()

            player.setOnPreparedListener { mp ->
                Log.d(TAG, "Azan prepared for $prayerName, starting playback")
                mp.start()
            }

            player.setOnCompletionListener { mp ->
                Log.d(TAG, "Azan completed for $prayerName")
                mp.release()
                mediaPlayer = null
                // Dismiss the ongoing notification
                NotificationManagerCompat.from(context).cancel(prayerName.hashCode())
                if (wakeLock.isHeld) wakeLock.release()
            }

            player.setOnErrorListener { mp, what, extra ->
                Log.e(TAG, "MediaPlayer error for $prayerName: what=$what, extra=$extra")
                mp.release()
                mediaPlayer = null
                if (wakeLock.isHeld) wakeLock.release()
                true
            }

            mediaPlayer = player
            player.prepareAsync()

        } catch (e: Exception) {
            Log.e(TAG, "Failed to play azan for $prayerName", e)
            if (wakeLock.isHeld) wakeLock.release()
        }
    }
}

