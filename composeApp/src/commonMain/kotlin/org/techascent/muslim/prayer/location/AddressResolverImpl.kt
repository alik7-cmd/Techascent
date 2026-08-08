package org.techascent.muslim.prayer.location

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.techascent.muslim.getPlaceName
import org.techascent.shared.data.common.AddressInfo

/**
 * DataStore-backed implementation of [AddressResolver].
 *
 * Register as a **singleton** so the in-memory [memoryCache] persists across
 * the app session and geocoder calls are minimised.
 */
class AddressResolverImpl(
    private val dataStore: DataStore<Preferences>,
) : AddressResolver {

    private val json = Json { ignoreUnknownKeys = true }
    private val KEY = stringPreferencesKey("cached_address_info")

    /** In-memory shortcut — avoids a DataStore read on every launch. */
    private var memoryCache: AddressInfo? = null

    // ── AddressResolver ─────────────────────────────────────────────────

    override suspend fun getCachedAddress(): AddressInfo? {
        memoryCache?.let { return it }
        return withContext(Dispatchers.Default) {
            try {
                dataStore.data.first()[KEY]?.let { raw ->
                    json.decodeFromString<AddressInfo>(raw).also { memoryCache = it }
                }
            } catch (_: Exception) { null }
        }
    }

    override suspend fun resolve(latitude: Double, longitude: Double): AddressResolutionResult {
        val cached = getCachedAddress()

        // ── Proximity shortcut ──────────────────────────────────────────
        // If the user hasn't moved more than ~1 km, skip the geocoder entirely.
        if (cached != null && isWithinProximity(latitude, longitude, cached.latitude, cached.longitude)) {
            return AddressResolutionResult.Success(address = cached, locationChanged = false)
        }

        // ── Geocode ─────────────────────────────────────────────────────
        val fresh = try {
            getPlaceName(latitude, longitude)
        } catch (_: Exception) {
            // Geocoder unavailable (offline) — fall back to cache if present
            return if (cached != null) AddressResolutionResult.Fallback(cached)
            else AddressResolutionResult.Unavailable
        }

        val locationChanged = cached == null || !isSameArea(fresh, cached)
        val withCoords = fresh.copy(latitude = latitude, longitude = longitude)
        memoryCache = withCoords
        persistAddress(withCoords)
        return AddressResolutionResult.Success(address = withCoords, locationChanged = locationChanged)
    }

    override suspend fun clearCache() {
        memoryCache = null
        try { dataStore.edit { it.remove(KEY) } } catch (_: Exception) { }
    }

    // ── Helpers ─────────────────────────────────────────────────────────

    /**
     * Bounding-box proximity check.
     * ±0.009° lat ≈ 1 km, ±0.012° lon ≈ 1 km at the equator.
     * City-level accuracy is sufficient for prayer-time calculation.
     */
    private fun isWithinProximity(
        lat: Double,
        lon: Double,
        cachedLat: Double?,
        cachedLon: Double?,
    ): Boolean {
        if (cachedLat == null || cachedLon == null) return false
        return kotlin.math.abs(lat - cachedLat) < 0.009 &&
               kotlin.math.abs(lon - cachedLon) < 0.012
    }

    /**
     * Compares two addresses by district → city → country priority.
     * Returns true if they represent the same prayer-time calculation area.
     */
    private fun isSameArea(fresh: AddressInfo, cached: AddressInfo): Boolean = when {
        fresh.district != null && cached.district != null ->
            fresh.district.equals(cached.district, ignoreCase = true) &&
            fresh.country.equals(cached.country, ignoreCase = true)
        fresh.city != null && cached.city != null ->
            fresh.city.equals(cached.city, ignoreCase = true) &&
            fresh.country.equals(cached.country, ignoreCase = true)
        else -> fresh.country.equals(cached.country, ignoreCase = true)
    }

    private suspend fun persistAddress(info: AddressInfo) {
        try {
            val encoded = withContext(Dispatchers.Default) { json.encodeToString(info) }
            dataStore.edit { it[KEY] = encoded }
        } catch (_: Exception) { }
    }
}

