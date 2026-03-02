package org.techascent.muslim.prayer.usecase

import androidx.compose.ui.text.capitalize
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import org.techascent.muslim.datastore.DataStoreKey
import org.techascent.muslim.getPrayerNotificationService
import org.techascent.muslim.prayer.uimodel.PrayerNameEnum
import org.techascent.muslim.prayer.uimodel.PrayerTimeIntervalModel
import kotlin.time.Duration.Companion.minutes

const val AZAN_AUDIO_FILE = "azan.mp3"

class PrayerNotificationUseCase(
    private val dataStore: DataStore<Preferences>
) {

    companion object {
        private val NOTIFY_PRAYERS_KEY = stringPreferencesKey(DataStoreKey.NOTIFICATION_PRAYER_LIST)
        private val ADHAN_PREFERENCE_KEY = booleanPreferencesKey(DataStoreKey.ADHAN_NOTIFICATION_PREFERENCE)
    }

    /**
     * Returns true if the user wants adhan audio with notifications, false for silent notifications.
     * Defaults to true if no preference is set.
     */
    private suspend fun isAdhanEnabled(): Boolean {
        return try {
            dataStore.data.first()[ADHAN_PREFERENCE_KEY] ?: true
        } catch (e: Exception) {
            true
        }
    }

    /**
     * Returns the local audio file name if adhan is enabled, empty string otherwise.
     * When audioFile is empty, the worker/service will show notification without playing audio.
     */
    private suspend fun getAudioForNotification(): String {
        return if (isAdhanEnabled()) AZAN_AUDIO_FILE else ""
    }

    suspend fun schedulePrayerNotifications(intervals: List<PrayerTimeIntervalModel>) {
        val currentList = getNotifyPrayersList()
        val notificationService = getPrayerNotificationService()
        val now = Clock.System.now()
        val audioFile = getAudioForNotification()

        // Cancel individual prayer notifications (not test ones, not audio)
        PrayerNameEnum.entries.forEach { prayer ->
            notificationService.cancelNotification(prayer.name)
        }

        // Schedule upcoming prayers that are in the notify list
        val upcomingPrayers = intervals.filter { interval ->
            interval.startTimeInstant != null &&
                    interval.startTimeInstant > now &&
                    currentList.contains(interval.name)
        }

        upcomingPrayers.forEach { interval ->
            interval.startTimeInstant?.let { instant ->
                notificationService.scheduleNotification(
                    prayerName = interval.name.name,
                    scheduledTime = instant,
                    title = "🔔 Prayer Time",
                    message = "Time for ${interval.name.name.lowercase().replaceFirstChar { it.uppercase() }}",
                    audioFile = audioFile
                )
            }
        }
    }

    suspend fun testNotificationNow() {
        val notificationService = getPrayerNotificationService()
        val audioFile = getAudioForNotification()
        notificationService.scheduleNotification(
            prayerName = "TEST",
            scheduledTime = Clock.System.now(),
            title = "Test Notification",
            message = "This is a test notification",
            audioFile = audioFile
        )
    }

    /**
     * Schedules 5 test azan notifications at 1-minute intervals.
     * Each gets a unique work name so they all fire independently.
     * Works even after the app is killed (WorkManager persists them).
     */
    suspend fun startRepeatingTestNotification() {
        val notificationService = getPrayerNotificationService()
        val audioFile = getAudioForNotification()
        // Cancel any previous test notifications first
        for (i in 1..5) {
            notificationService.cancelNotification("TEST_REPEAT_$i")
        }
        val now = Clock.System.now()
        for (i in 1..5) {
            val scheduledTime = now + i.minutes
            notificationService.scheduleNotification(
                prayerName = "TEST_REPEAT_$i",
                scheduledTime = scheduledTime,
                title = "🔔 Test Azan #$i",
                message = "Repeating test notification ($i of 5) — fires every 1 min",
                audioFile = audioFile
            )
        }
    }

    /**
     * Cancels all repeating test notifications.
     */
    suspend fun stopRepeatingTestNotification() {
        val notificationService = getPrayerNotificationService()
        for (i in 1..5) {
            notificationService.cancelNotification("TEST_REPEAT_$i")
        }
    }

    suspend fun addPrayerToNotify(prayerName: PrayerNameEnum) {
        val currentList = getNotifyPrayersList().toMutableList()
        if (!currentList.contains(prayerName)) {
            currentList.add(prayerName)
            saveNotifyPrayersList(currentList)
        }
    }

    suspend fun removePrayerFromNotify(prayerName: PrayerNameEnum) {
        val currentList = getNotifyPrayersList().toMutableList()
        currentList.remove(prayerName)
        saveNotifyPrayersList(currentList)
    }

    private suspend fun getNotifyPrayersList(): List<PrayerNameEnum> {
        return try {
            val jsonString = dataStore.data.first()[NOTIFY_PRAYERS_KEY] ?: "[]"
            Json.decodeFromString<List<String>>(jsonString).mapNotNull {
                try {
                    PrayerNameEnum.valueOf(it)
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private suspend fun saveNotifyPrayersList(prayers: List<PrayerNameEnum>) {
        try {
            val jsonString = Json.encodeToString(prayers.map { it.name })
            dataStore.edit { preferences ->
                preferences[NOTIFY_PRAYERS_KEY] = jsonString
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}