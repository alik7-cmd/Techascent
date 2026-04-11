package org.techascent.muslim.widget

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import org.techascent.muslim.ensureContext
import org.techascent.muslim.prayer.uimodel.PrayerTimeUiModel
import org.techascent.muslim.provideDataStore
import org.techascent.shared.data.common.DataStoreKey
import org.techascent.shared.data.common.PrayerNameEnum
import org.techascent.shared.data.common.getCurrentDateFormatted
import org.techascent.shared.data.common.toFormattedTimeString
import org.techascent.shared.data.enum.School

private const val TAG = "WidgetDataProvider"
private const val WIDGET_PREFS_NAME = "prayer_widget_prefs"
private const val KEY_WIDGET_JSON = "widget_data_json"

/**
 * Lightweight data class carrying only the info the widget needs.
 */
data class WidgetPrayerData(
    val currentPrayerName: String,
    val currentPrayerEmoji: String,
    val currentPrayerStart: String,
    val currentPrayerEnd: String,
    val locationLabel: String,
    val hijriDate: String,
    val currentDate: String,
    val iftarTime: String?,
    val sahriTime: String?,
    val lastUpdated: String,
    /** All prayer times for today (name, emoji, start time, isCurrent). */
    val allPrayers: List<WidgetPrayerRow>,
)

data class WidgetPrayerRow(
    val name: String,
    val emoji: String,
    val time: String,
    val isCurrent: Boolean,
)

// ═══════════════════════════════════════════════════════════════════════════
//  Write: called from WidgetUpdater when app has fresh prayer data
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Persists the current prayer data into a lightweight SharedPreferences file
 * so the widget can read it without touching DataStore at all.
 * Call this before triggering [PrayerTimeWidget.updateAll].
 */
suspend fun writeWidgetSnapshot(context: Context) {
    try {
        ensureContext(context)
        val dataStore = provideDataStore()
        val json = Json { ignoreUnknownKeys = true }

        val prefs = dataStore.data.first()

        val is24Hr = prefs[booleanPreferencesKey(DataStoreKey.IS_24_HOUR_FORMAT)] ?: false
        val schoolCode = prefs[intPreferencesKey(DataStoreKey.SCHOOL_PREFERENCE)] ?: School.HANAFI.code
        val school = School.fromCode(schoolCode)

        val currentDate = getCurrentDateFormatted()
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val year = now.year
        val month = now.monthNumber

        val addressJson = prefs[stringPreferencesKey("cached_address_info")]
        val city = if (addressJson != null) {
            try {
                val addr = json.decodeFromString<org.techascent.shared.data.common.AddressInfo>(addressJson)
                addr.district ?: "default"
            } catch (_: Exception) { "default" }
        } else "default"

        val cacheKey = "${DataStoreKey.MONTHLY_PRAYER_INITIAL}${city}_${school.name}_${year}_${month}"
        val raw = prefs[stringPreferencesKey(cacheKey)] ?: return

        // Write the raw monthly JSON + metadata to SharedPreferences
        val sp = widgetPrefs(context)
        sp.edit()
            .putString(KEY_WIDGET_JSON, raw)
            .putString("cache_date", currentDate)
            .putBoolean("is_24hr", is24Hr)
            .putString("address_json", addressJson ?: "")
            .putLong("last_updated", System.currentTimeMillis())
            .apply()

        Log.d(TAG, "Widget snapshot written for $currentDate")
    } catch (e: Exception) {
        Log.e(TAG, "Failed to write widget snapshot", e)
    }
}

// ═══════════════════════════════════════════════════════════════════════════
//  Read: called by the widget during provideGlance
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Reads cached prayer data from the widget's SharedPreferences,
 * determines the current waqt, and produces a [WidgetPrayerData].
 *
 * Returns `null` when no snapshot is available (user hasn't opened
 * the app yet or data is stale).
 */
suspend fun loadWidgetData(context: Context): WidgetPrayerData? {
    return try {
        loadWidgetDataInternal(context)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to load widget data", e)
        null
    }
}

private fun loadWidgetDataInternal(context: Context): WidgetPrayerData? {
    val json = Json { ignoreUnknownKeys = true }
    val sp = widgetPrefs(context)

    val raw = sp.getString(KEY_WIDGET_JSON, null) ?: return null
    val is24Hr = sp.getBoolean("is_24hr", false)
    val lastUpdatedMillis = sp.getLong("last_updated", 0L)
    val currentDate = getCurrentDateFormatted()

    val monthData: List<PrayerTimeUiModel> = try {
        json.decodeFromString(raw)
    } catch (_: Exception) {
        return null
    }

    val todayModel = monthData.find { it.currentDateTime == currentDate } ?: return null

    // Determine current prayer using instants
    val nowInstant = Clock.System.now()
    val currentInterval = todayModel.intervals.find { interval ->
        interval.startTimeInstant != null &&
                interval.endTimeInstant != null &&
                nowInstant >= interval.startTimeInstant &&
                nowInstant < interval.endTimeInstant
    }

    val displayName = currentInterval?.name?.toWidgetDisplayName() ?: "—"
    val displayEmoji = currentInterval?.emoji ?: "🕌"
    val startTime = currentInterval?.startTimeInstant?.toFormattedTimeString(is24Hr) ?: ""
    val endTime = currentInterval?.endTimeInstant?.toFormattedTimeString(is24Hr) ?: ""

    val locationLabel = buildString {
        val addr = todayModel.addressInfo
        if (addr.district != null) {
            append(addr.district)
            if (addr.country != null) append(", ${addr.country}")
        } else {
            append(addr.address)
        }
    }

    // Iftar & Sahri
    val iftarDisplay = todayModel.iftarTime?.iftarInstant?.toFormattedTimeString(is24Hr)
        ?: todayModel.iftarTime?.iftarStartTime
    val sahriDisplay = todayModel.iftarTime?.sahriInstant?.toFormattedTimeString(is24Hr)
        ?: todayModel.iftarTime?.lastTimeOfSahri

    // Current date formatted for display (e.g. "11 Apr 2026")
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val displayDate = buildString {
        append(now.dayOfMonth.toString().padStart(2, '0'))
        append(" ")
        append(now.month.name.take(3).lowercase()
            .replaceFirstChar { it.uppercaseChar() })
        append(" ")
        append(now.year)
    }

    // Last updated time
    val lastUpdatedStr = if (lastUpdatedMillis > 0) {
        val updatedInstant = kotlinx.datetime.Instant.fromEpochMilliseconds(lastUpdatedMillis)
        updatedInstant.toFormattedTimeString(is24Hr)
    } else ""

    // Build rows for all prayers (exclude Duha)
    val allPrayers = todayModel.intervals
        .filter { it.name != PrayerNameEnum.SALAT_UD_DUHA }
        .map { interval ->
            WidgetPrayerRow(
                name = interval.name.toWidgetDisplayName(),
                emoji = interval.emoji,
                time = interval.startTimeInstant?.toFormattedTimeString(is24Hr)
                    ?: interval.displayableStartTime,
                isCurrent = currentInterval != null &&
                        interval.startTimeInstant == currentInterval.startTimeInstant,
            )
        }

    return WidgetPrayerData(
        currentPrayerName = displayName,
        currentPrayerEmoji = displayEmoji,
        currentPrayerStart = startTime,
        currentPrayerEnd = endTime,
        locationLabel = locationLabel,
        hijriDate = todayModel.hijriDate,
        currentDate = displayDate,
        iftarTime = iftarDisplay,
        sahriTime = sahriDisplay,
        lastUpdated = lastUpdatedStr,
        allPrayers = allPrayers,
    )
}

// ═══════════════════════════════════════════════════════════════════════════
//  Helpers
// ═══════════════════════════════════════════════════════════════════════════

private fun widgetPrefs(context: Context): SharedPreferences =
    context.applicationContext.getSharedPreferences(WIDGET_PREFS_NAME, Context.MODE_PRIVATE)

private fun PrayerNameEnum.toWidgetDisplayName(): String = when (this) {
    PrayerNameEnum.FAJR -> "Fajr"
    PrayerNameEnum.SALAT_UD_DUHA -> "Duha"
    PrayerNameEnum.DUHR -> "Dhuhr"
    PrayerNameEnum.ASR -> "Asr"
    PrayerNameEnum.MAGHRIB -> "Maghrib"
    PrayerNameEnum.ISHA -> "Isha"
}
