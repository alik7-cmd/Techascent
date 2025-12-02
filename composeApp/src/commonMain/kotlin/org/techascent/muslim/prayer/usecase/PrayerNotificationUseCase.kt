package org.techascent.muslim.prayer.usecase

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import org.techascent.muslim.datastore.DataStoreKey
import org.techascent.muslim.getPrayerNotificationService
import org.techascent.muslim.prayer.uimodel.PrayerNameEnum
import org.techascent.muslim.prayer.uimodel.PrayerTimeIntervalModel

const val AZAN_URL =
    "https://archive.org/download/adhan.recordings.from.doha.qatar/Adhan_Doha_Qatar_01_Fajr_Adhan.ogg"

class PrayerNotificationUseCase(
    private val dataStore: DataStore<Preferences>
) {

    companion object {
        private val NOTIFY_PRAYERS_KEY = stringPreferencesKey(DataStoreKey.NOTIFICATION_PRAYER_LIST)
    }

    suspend fun schedulePrayerNotifications(intervals: List<PrayerTimeIntervalModel>) {
        val currentList = getNotifyPrayersList()
        val notificationService = getPrayerNotificationService()
        val now = Clock.System.now()

        // Find the next prayer that hasn't started yet and is in the notify list
        val nextPrayer = intervals.firstOrNull { interval ->
            interval.startTimeInstant != null &&
                    interval.startTimeInstant > now &&
                    currentList.contains(interval.name)
        }

        nextPrayer?.let { interval ->
            interval.startTimeInstant?.let { instant ->
                notificationService.scheduleNotification(
                    prayerName = interval.name.name,
                    scheduledTime = instant,
                    title = "Prayer Time",
                    message = "Time for ${interval.name.name}",
                    audioUrl = AZAN_URL
                )
            }
        }
    }

    suspend fun testNotificationNow() {
        val notificationService = getPrayerNotificationService()
        notificationService.scheduleNotification(
            prayerName = "TEST",
            scheduledTime = Clock.System.now(),
            title = "Test Notification",
            message = "This is a test notification",
            audioUrl = AZAN_URL
        )
        //notificationService.playAudio(AZAN_URL)
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