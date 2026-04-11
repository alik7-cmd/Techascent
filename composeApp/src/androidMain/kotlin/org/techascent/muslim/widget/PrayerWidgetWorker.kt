package org.techascent.muslim.widget

import android.content.Context
import android.util.Log
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

/**
 * Periodic WorkManager worker that refreshes every widget instance.
 *
 * WorkManager's minimum period is 15 minutes, but we request 15 min
 * and the widget will also be updated by the system's
 * `updatePeriodMillis` (30 min). For countdown precision the widget
 * re-computes remaining time every time it renders — the worker just
 * triggers the re-render.
 */
class PrayerWidgetWorker(
    private val context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "PrayerWidgetWorker"
        private const val UNIQUE_WORK_NAME = "prayer_widget_periodic"

        fun enqueue(context: Context) {
            val request = PeriodicWorkRequestBuilder<PrayerWidgetWorker>(
                15, TimeUnit.MINUTES,
            ).addTag(TAG).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
            Log.d(TAG, "Widget periodic worker enqueued")
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME)
            Log.d(TAG, "Widget periodic worker cancelled")
        }
    }

    override suspend fun doWork(): Result {
        return try {
            // Write latest prayer data to widget SharedPreferences
            writeWidgetSnapshot(context)
            // Then trigger all widgets to re-render (reads from SharedPreferences)
            PrayerTimeWidget().updateAll(context)
            Log.d(TAG, "Widget updated successfully")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Widget update failed", e)
            Result.retry()
        }
    }
}

