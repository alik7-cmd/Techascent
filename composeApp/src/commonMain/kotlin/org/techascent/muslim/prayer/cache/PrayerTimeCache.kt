package org.techascent.muslim.prayer.cache

import org.techascent.muslim.prayer.uimodel.PrayerTimeUiModel
import org.techascent.shared.data.enum.School

/**
 * Local cache for monthly prayer-time data.
 *
 * Owns two storage layers:
 *  - **In-memory map** — zero-latency after first load.
 *  - **DataStore** — survives process restarts.
 *
 * This is the single source of truth for persisted prayer data inside the
 * composeApp module. No other class should read/write prayer-data keys in
 * DataStore directly.
 *
 * Testable: inject a fake/mock [PrayerTimeCache] in unit tests to decouple
 * use-cases from DataStore and JSON serialization.
 */
interface PrayerTimeCache {

    /** Builds the DataStore key for a given city / school / month. */
    fun keyFor(city: String, school: School, year: Int, month: Int): String

    /**
     * Returns the model for [date] from cache (memory → DataStore), or null on
     * a full cache miss.
     */
    suspend fun getToday(
        city: String,
        school: School,
        year: Int,
        month: Int,
        date: String,
    ): PrayerTimeUiModel?

    /**
     * Persists [data] for the given city / school / month to both memory and
     * DataStore. Existing data for the same key is overwritten.
     */
    suspend fun putMonth(
        city: String,
        school: School,
        year: Int,
        month: Int,
        data: List<PrayerTimeUiModel>,
    )

    /**
     * Returns the raw list for an arbitrary [cacheKey] (e.g. as constructed by
     * [keyFor]). Used by notification scheduling to look up tomorrow's prayers.
     */
    suspend fun getForKey(cacheKey: String): List<PrayerTimeUiModel>?

    /**
     * Returns every cached day as a **date-string → model** map.
     * Used by the calendar feature.
     */
    suspend fun getAllData(): Map<String, PrayerTimeUiModel>

    /** Clears all cached months from memory and DataStore. */
    suspend fun invalidate()
}

