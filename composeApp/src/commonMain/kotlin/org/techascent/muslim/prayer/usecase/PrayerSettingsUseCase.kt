package org.techascent.muslim.prayer.usecase

import org.techascent.muslim.prayer.cache.PrayerTimeCache
import org.techascent.muslim.prayer.location.AddressResolver

/**
 * Handles prayer-related side-effects triggered by user settings changes.
 *
 * Currently responsible for cache invalidation when the user changes their
 * calculation school — this wipes both the prayer-data cache and the stored
 * address so the next GPS fix triggers a fresh network fetch from the correct
 * school.
 *
 * Depends only on [PrayerTimeCache] and [AddressResolver] — no network,
 * no DataStore access — making it trivial to unit-test.
 */
class PrayerSettingsUseCase(
    private val prayerCache: PrayerTimeCache,
    private val addressResolver: AddressResolver,
) {
    /**
     * Clears all cached prayer months and the stored address.
     * Call this whenever a setting that affects prayer calculations changes
     * (e.g. school / calculation method).
     */
    suspend fun invalidateCache() {
        prayerCache.invalidate()
        addressResolver.clearCache()
    }
}

