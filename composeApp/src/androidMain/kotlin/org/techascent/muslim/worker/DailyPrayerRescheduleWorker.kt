package org.techascent.muslim.worker

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import org.techascent.muslim.datastore.DataStoreKey
import org.techascent.muslim.ensureContext
import org.techascent.muslim.getPrayerNotificationService
import org.techascent.muslim.prayer.uimodel.PrayerNameEnum
import org.techascent.muslim.prayer.uimodel.PrayerTimeUiModel
import org.techascent.muslim.prayer.usecase.AZAN_AUDIO_FILE
import org.techascent.muslim.provideDataStore

/**
 * A daily worker that runs once per day (around midnight or early morning)
 * to reschedule prayer notifications for the new day.
 *
 * This ensures notifications continue to fire daily even if the app is never opened.
 * It reads cached monthly prayer data from DataStore, finds today's and tomorrow's
 * prayer times, and schedules WorkManager one-shot workers for each.
 */
class DailyPrayerRescheduleWorker(
    val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val WORK_NAME = "daily_prayer_reschedule"
        private const val TAG = "DailyRescheduleWorker"
    }

    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "Daily prayer reschedule worker started")

            ensureContext(context)
            val dataStore = provideDataStore()

            // 1. Read the notify prayers list from datastore
            val notifyPrayersJson = dataStore.data.first()[
                stringPreferencesKey(DataStoreKey.NOTIFICATION_PRAYER_LIST)
            ] ?: "[]"

            val notifyPrayers = try {
                Json.decodeFromString<List<String>>(notifyPrayersJson).mapNotNull {
                    try { PrayerNameEnum.valueOf(it) } catch (e: Exception) { null }
                }
            } catch (e: Exception) {
                emptyList()
            }

            if (notifyPrayers.isEmpty()) {
                Log.d(TAG, "No prayers are set for notification, skipping.")
                return Result.success()
            }

            // 2. Check if adhan audio is enabled
            val adhanEnabled = dataStore.data.first()[
                booleanPreferencesKey(DataStoreKey.ADHAN_NOTIFICATION_PREFERENCE)
            ] ?: true
            val audioFile = if (adhanEnabled) AZAN_AUDIO_FILE else ""

            // 3. Find cached prayer data for today and tomorrow
            val allPrefs = dataStore.data.first()
            val todayDate = getDateFormatted(0)
            val tomorrowDate = getDateFormatted(1)
            var todayPrayerData: PrayerTimeUiModel? = null
            var tomorrowPrayerData: PrayerTimeUiModel? = null

            for ((key, value) in allPrefs.asMap()) {
                if (key.name.startsWith(DataStoreKey.MONTHLY_PRAYER_INITIAL) && value is String) {
                    try {
                        val cachedList = Json.decodeFromString<List<PrayerTimeUiModel>>(value)
                        if (todayPrayerData == null) {
                            todayPrayerData = cachedList.find { it.currentDateTime == todayDate }
                        }
                        if (tomorrowPrayerData == null) {
                            tomorrowPrayerData = cachedList.find { it.currentDateTime == tomorrowDate }
                        }
                        if (todayPrayerData != null && tomorrowPrayerData != null) break
                    } catch (e: Exception) {
                        // Not a valid prayer cache, skip
                    }
                }
            }

            if (todayPrayerData == null && tomorrowPrayerData == null) {
                Log.d(TAG, "No cached prayer data found for today ($todayDate) or tomorrow ($tomorrowDate)")
                return Result.success()
            }

            // 4. Schedule notifications
            val notificationService = getPrayerNotificationService()
            val now = Clock.System.now()

            // Cancel all existing prayer notifications first
            PrayerNameEnum.entries.forEach { prayer ->
                notificationService.cancelNotification(prayer.name)
                notificationService.cancelNotification("NEXT_${prayer.name}")
            }

            var scheduledCount = 0

            // Schedule today's remaining prayers
            todayPrayerData?.intervals
                ?.filter { interval ->
                    interval.startTimeInstant != null &&
                            interval.startTimeInstant > now &&
                            notifyPrayers.contains(interval.name)
                }
                ?.forEach { interval ->
                    interval.startTimeInstant?.let { instant ->
                        notificationService.scheduleNotification(
                            prayerName = interval.name.name,
                            scheduledTime = instant,
                            title = "🔔 Prayer Time",
                            message = "Time for ${interval.name.name.lowercase().replaceFirstChar { it.uppercase() }}",
                            audioFile = audioFile
                        )
                        scheduledCount++
                        Log.d(TAG, "Scheduled today's ${interval.name.name}")
                    }
                }

            // Schedule tomorrow's prayers
            tomorrowPrayerData?.intervals
                ?.filter { interval ->
                    interval.startTimeInstant != null &&
                            notifyPrayers.contains(interval.name)
                }
                ?.forEach { interval ->
                    interval.startTimeInstant?.let { instant ->
                        notificationService.scheduleNotification(
                            prayerName = "NEXT_${interval.name.name}",
                            scheduledTime = instant,
                            title = "🔔 Prayer Time",
                            message = "Time for ${interval.name.name.lowercase().replaceFirstChar { it.uppercase() }}",
                            audioFile = audioFile
                        )
                        scheduledCount++
                        Log.d(TAG, "Scheduled tomorrow's ${interval.name.name}")
                    }
                }

            Log.d(TAG, "Successfully rescheduled $scheduledCount prayer notifications")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error in daily reschedule worker", e)
            Result.retry()
        }
    }

    /**
     * Returns a formatted date string "DD-MM-YYYY" for today + [daysOffset].
     */
    private fun getDateFormatted(daysOffset: Int): String {
        val now = Clock.System.now()
        val tz = TimeZone.currentSystemDefault()
        val date = now.toLocalDateTime(tz).date.plus(daysOffset, DateTimeUnit.DAY)
        val day = date.dayOfMonth.toString().padStart(2, '0')
        val month = date.monthNumber.toString().padStart(2, '0')
        val year = date.year.toString()
        return "$day-$month-$year"
    }
}

