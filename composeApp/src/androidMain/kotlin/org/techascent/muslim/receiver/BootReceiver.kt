package org.techascent.muslim.receiver
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import org.techascent.muslim.servive.DailyPrayerScheduler
/**
 * Receives BOOT_COMPLETED broadcast to reschedule prayer notifications
 * after the device restarts. Also handles TIME_SET and TIMEZONE_CHANGED.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED -> {
                Log.d("BootReceiver", "Received ${intent.action}, rescheduling daily prayer worker")
                DailyPrayerScheduler.scheduleDailyWorker(context)
            }
        }
    }
}
