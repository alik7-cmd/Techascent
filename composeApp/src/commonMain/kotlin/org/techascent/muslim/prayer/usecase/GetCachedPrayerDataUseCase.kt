package org.techascent.muslim.prayer.usecase

import org.techascent.muslim.prayer.cache.PrayerTimeCache
import org.techascent.muslim.prayer.uimodel.PrayerTimeUiModel

/**
 * Returns every prayer-time model currently held in the local cache.
 *
 * Used by the calendar feature to display prayer times for any cached date
 * without triggering a network call.
 *
 * Follows the single-method use-case convention: call via [invoke] so
 * call-sites read as `getCachedPrayerData()` rather than
 * `getCachedPrayerData.getAllCachedPrayerData()`.
 */
class GetCachedPrayerDataUseCase(
    private val prayerCache: PrayerTimeCache,
) {
    /**
     * Returns a **date-string → model** map for every day currently in cache.
     * Date strings are formatted as "DD-MM-YYYY".
     */
    suspend operator fun invoke(): Map<String, PrayerTimeUiModel> = prayerCache.getAllData()
}

