package org.techascent.muslim.servive

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import org.techascent.muslim.worker.DailyPrayerRescheduleWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Helper object to schedule a daily PeriodicWorkRequest that
 * reschedules prayer notifications each day.
 *
 * - Runs every 24 hours (with flex window of 1 hour).
 * - First run is delayed until ~midnight (00:30 local time).
 * - Survives app kills because WorkManager persists work.
 * - On device reboot, BootReceiver calls this again.
 */
object DailyPrayerScheduler {

    private const val TAG = "DailyPrayerScheduler"

    fun scheduleDailyWorker(context: Context) {
        // Calculate delay until next occurrence of 00:30
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 30)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            // If 00:30 today has already passed, target tomorrow
            if (before(now)) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }
        val initialDelayMs = target.timeInMillis - now.timeInMillis

        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(false)
            .build()

        val dailyRequest = PeriodicWorkRequestBuilder<DailyPrayerRescheduleWorker>(
            repeatInterval = 24,
            repeatIntervalTimeUnit = TimeUnit.HOURS,
            flexTimeInterval = 1,
            flexTimeIntervalUnit = TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .setInitialDelay(initialDelayMs, TimeUnit.MILLISECONDS)
            .addTag("daily_prayer_reschedule")
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            DailyPrayerRescheduleWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP, // Don't replace if already scheduled
            dailyRequest
        )

        Log.d(TAG, "Daily prayer reschedule worker enqueued. Initial delay: ${initialDelayMs / 1000}s (until ~00:30)")
    }
}

