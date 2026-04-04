package org.techascent.muslim.servive

import android.Manifest
import android.app.AlarmManager
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
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import org.techascent.muslim.MainActivity
import org.techascent.muslim.PrayerNotificationService
import org.techascent.muslim.R
import org.techascent.muslim.receiver.PrayerAlarmReceiver
import kotlin.coroutines.resume

var mediaPlayer: MediaPlayer? = null

class AndroidPrayerNotificationService(private val context: Context) : PrayerNotificationService {
    companion object {
        private const val CHANNEL_ID = "prayer_times"
        private const val CHANNEL_NAME = "Prayer Times"
        private const val STOP_ACTION = "org.techascent.muslim.STOP_AUDIO"
        private const val TAG = "PrayerNotification"
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

    /**
     * Schedules an EXACT alarm using AlarmManager.setAlarmClock().
     * This is exempt from Doze mode and fires at the precise millisecond.
     * When the alarm fires, PrayerAlarmReceiver handles notification + audio.
     */
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override suspend fun scheduleNotification(
        prayerName: String,
        scheduledTime: Instant,
        title: String,
        message: String,
        audioFile: String
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val scheduledTimeMillis = scheduledTime.toEpochMilliseconds()
        val currentTimeMillis = System.currentTimeMillis()

        // If the scheduled time has passed, fire immediately via the receiver
        if (scheduledTimeMillis <= currentTimeMillis) {
            Log.d(TAG, "$prayerName time already passed, firing immediately")
            val intent = buildAlarmIntent(prayerName, title, message, audioFile)
            context.sendBroadcast(intent)
            return
        }

        val pendingIntent = buildAlarmPendingIntent(prayerName, title, message, audioFile)

        // Check if we can schedule exact alarms (API 31+ runtime check)
        val canExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true // Pre-API 31 doesn't need the permission
        }

        if (canExact) {
            // setAlarmClock() is the MOST reliable exact alarm:
            // - Exempt from Doze mode
            // - Shows alarm icon in status bar
            // - Wakes the device from deep sleep
            val showIntent = PendingIntent.getActivity(
                context,
                prayerName.hashCode() + 1000,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(scheduledTimeMillis, showIntent),
                pendingIntent
            )
            Log.d(TAG, "Exact alarm (setAlarmClock) set for $prayerName")
        } else {
            // Fallback: slightly less exact but doesn't require permission
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                scheduledTimeMillis,
                pendingIntent
            )
            Log.w(TAG, "Exact alarm permission not granted, using setAndAllowWhileIdle for $prayerName")
        }

        val delaySec = (scheduledTimeMillis - currentTimeMillis) / 1000
        Log.d(TAG, "Alarm set for $prayerName in ${delaySec}s (at ${scheduledTimeMillis}ms)")
    }

    private fun buildAlarmIntent(
        prayerName: String,
        title: String,
        message: String,
        audioFile: String
    ): Intent {
        return Intent(context, PrayerAlarmReceiver::class.java).apply {
            action = "org.techascent.muslim.PRAYER_ALARM_$prayerName"
            putExtra(PrayerAlarmReceiver.EXTRA_PRAYER_NAME, prayerName)
            putExtra(PrayerAlarmReceiver.EXTRA_TITLE, title)
            putExtra(PrayerAlarmReceiver.EXTRA_MESSAGE, message)
            putExtra(PrayerAlarmReceiver.EXTRA_AUDIO_FILE, audioFile)
        }
    }

    private fun buildAlarmPendingIntent(
        prayerName: String,
        title: String,
        message: String,
        audioFile: String
    ): PendingIntent {
        val intent = buildAlarmIntent(prayerName, title, message, audioFile)
        return PendingIntent.getBroadcast(
            context,
            prayerName.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
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

    /**
     * Cancels the exact alarm for a specific prayer.
     */
    override suspend fun cancelNotification(notificationId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Cancel the AlarmManager alarm
        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            action = "org.techascent.muslim.PRAYER_ALARM_$notificationId"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()

        // Also dismiss any visible notification
        NotificationManagerCompat.from(context).cancel(notificationId.hashCode())

        Log.d(TAG, "Cancelled alarm for $notificationId")
    }

    override suspend fun cancelAllNotifications() {
        val prayers = listOf("FAJR", "SALAT_UD_DUHA", "DUHR", "ASR", "MAGHRIB", "ISHA")
        val prefixes = listOf("", "NEXT_")
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Cancel all prayer alarms (today + next day)
        for (prefix in prefixes) {
            for (prayer in prayers) {
                val id = "$prefix$prayer"
                val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
                    action = "org.techascent.muslim.PRAYER_ALARM_$id"
                }
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    id.hashCode(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }

        // Cancel test alarms
        val testId = "TEST"
        val testIntent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            action = "org.techascent.muslim.PRAYER_ALARM_$testId"
        }
        PendingIntent.getBroadcast(
            context, testId.hashCode(), testIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        ).let {
            alarmManager.cancel(it)
            it.cancel()
        }

        for (i in 1..5) {
            val repeatId = "TEST_REPEAT_$i"
            val repeatIntent = Intent(context, PrayerAlarmReceiver::class.java).apply {
                action = "org.techascent.muslim.PRAYER_ALARM_$repeatId"
            }
            PendingIntent.getBroadcast(
                context, repeatId.hashCode(), repeatIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            ).let {
                alarmManager.cancel(it)
                it.cancel()
            }
        }

        // Stop any currently playing audio
        mediaPlayer?.let {
            if (it.isPlaying) it.stop()
            it.release()
        }
        mediaPlayer = null

        // Dismiss all visible notifications
        NotificationManagerCompat.from(context).cancelAll()

        Log.d(TAG, "All prayer alarms cancelled")
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
