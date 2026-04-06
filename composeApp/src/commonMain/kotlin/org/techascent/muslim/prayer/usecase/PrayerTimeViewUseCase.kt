package org.techascent.muslim.prayer.usecase

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import org.techascent.shared.data.common.AddressInfo
import org.techascent.shared.data.common.getCurrentDateFormatted
import org.techascent.muslim.common.location.LocationService
import org.techascent.muslim.datastore.DataStoreKey
import org.techascent.muslim.getPlaceName
import org.techascent.muslim.prayer.uimodel.PrayerNameEnum
import org.techascent.muslim.prayer.uimodel.PrayerTimeUiModel
import org.techascent.muslim.prayer.uimodel.getImageByPrayerEnum
import org.techascent.muslim.prayer.uimodel.toUiModel
import org.techascent.shared.data.enum.School
import org.techascent.shared.data.repository.PrayerTimesRepository
import org.techascent.shared.network.ResultState

class PrayerTimeViewUseCase(
    private val repository: PrayerTimesRepository,
    private val locationService: LocationService,
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private const val PREFIX = DataStoreKey.MONTHLY_PRAYER_INITIAL
        private const val ADDRESS_CACHE_KEY = "cached_address_info"
        private val NOTIFY_PRAYERS_KEY = stringPreferencesKey(DataStoreKey.NOTIFICATION_PRAYER_LIST)

        /**
         * Cache key for a single month: "monthly_prayer_times_<city>_<school>_<year>_<month>"
         */
        private fun cacheKeyFor(city: String, school: School, year: Int, month: Int): String =
            "${PREFIX}${city}_${school.name}_${year}_${month}"
    }

    // ── In-memory caches to avoid repeated disk / JSON work ─────────────
    /**
     * In-memory map: cacheKey → deserialized list of PrayerTimeUiModel.
     * Populated on first read from DataStore and updated after remote fetch.
     */
    private val memoryCache = mutableMapOf<String, List<PrayerTimeUiModel>>()

    /** Cached notification prayer list so we don't re-read DataStore every call. */
    private var notifyListCache: List<PrayerNameEnum>? = null

    /** Cached geocoded address so we don't hit Geocoder on every call. */
    private var addressCache: AddressInfo? = null

    // ── JSON instance ───────────────────────────────────────────────────
    private val json = Json { ignoreUnknownKeys = true }

    // ── Public API ──────────────────────────────────────────────────────

    /**
     * Returns today's prayer data.
     * 1. Reads preferences ONCE for school + notify list.
     * 2. Checks in-memory cache → DataStore cache → remote (12-month prefetch).
     */
    suspend fun getMonthlyPrayerTimes(): Flow<ResultState<PrayerTimeUiModel>> {
        val location = locationService.getCurrentLocation()
            ?: return flowOf(ResultState.Error("Location not found"))

        // ── 1. Single DataStore read for all prefs ──────────────────────
        val prefs = dataStore.data.first()
        val code = prefs[intPreferencesKey(DataStoreKey.SCHOOL_PREFERENCE)] ?: School.HANAFI.code
        val school = School.fromCode(code)
        val currentDate = getCurrentDateFormatted()
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val year = now.year
        val month = now.monthNumber

        // Cache the notify list for later updateCurrentPrayer calls
        notifyListCache = readNotifyPrayersList(prefs)

        // ── 2. Resolve address (with in-memory cache) ───────────────────
        val addressInfo = resolveAddress(location.latitude, location.longitude, prefs)
        val city = addressInfo.district ?: "default"

        // ── 3. Try in-memory → DataStore cache for the current month ────
        val cacheKey = cacheKeyFor(city, school, year, month)
        val todayFromCache = findTodayInCache(cacheKey, currentDate, prefs)
        if (todayFromCache != null) {
            return flowOf(ResultState.Success(updateCurrentPrayer(todayFromCache)))
        }

        // ── 4. Cache miss → fetch 12 months in parallel & cache ─────────
        return fetchAndCacheYear(
            year = year,
            startMonth = month,
            latitude = location.latitude,
            longitude = location.longitude,
            school = school,
            city = city,
            currentDate = currentDate
        )
    }

    // ── Private helpers ─────────────────────────────────────────────────

    /**
     * Look up today's prayer data, first from the in-memory map, then
     * from DataStore (populating the memory map on hit).
     */
    private fun findTodayInCache(
        cacheKey: String,
        currentDate: String,
        prefs: Preferences
    ): PrayerTimeUiModel? {
        // Fast path: in-memory
        memoryCache[cacheKey]?.let { list ->
            list.find { it.currentDateTime == currentDate }?.let { return it }
        }

        // Slow path: DataStore → memory
        val raw = prefs[stringPreferencesKey(cacheKey)] ?: return null
        return try {
            val list = json.decodeFromString<List<PrayerTimeUiModel>>(raw)
            memoryCache[cacheKey] = list
            list.find { it.currentDateTime == currentDate }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Fetch 12 consecutive months starting from [startMonth].
     * Emits today's result immediately from the first month, then
     * prefetches remaining 11 months in the background without blocking.
     */
    private fun fetchAndCacheYear(
        year: Int,
        startMonth: Int,
        latitude: Double,
        longitude: Double,
        school: School,
        city: String,
        currentDate: String,
    ): Flow<ResultState<PrayerTimeUiModel>> = channelFlow {
        // Compute the 12 (year, month) pairs starting from the current month
        val monthPairs = (0 until 12).map { offset ->
            val totalMonth = startMonth + offset
            val y = year + (totalMonth - 1) / 12
            val m = ((totalMonth - 1) % 12) + 1
            Pair(y, m)
        }

        // The first month is the one the user needs RIGHT NOW
        val (firstYear, firstMonth) = monthPairs.first()

        // Collect the first month's flow and emit results to the caller
        repository.getMonthlyPrayerTimes(
            year = firstYear,
            month = firstMonth,
            latitude = latitude,
            longitude = longitude,
            school = school.code
        ).collect { resultState ->
            when (resultState) {
                is ResultState.Success -> {
                    val uiModels = resultState.data.map { it.toUiModel(school = school) }
                    val key = cacheKeyFor(city, school, firstYear, firstMonth)
                    cacheMonth(key, uiModels)

                    val today = uiModels.find { it.currentDateTime == currentDate }
                    val result = today?.let { ResultState.Success(updateCurrentPrayer(it)) }
                        ?: ResultState.Error("Prayer data not found for today")
                    send(result)

                    // Fire off remaining 11 months in background (non-blocking)
                    launch {
                        prefetchRemainingMonths(
                            monthPairs.drop(1), latitude, longitude, school, city
                        )
                    }
                }
                is ResultState.Error -> send(resultState)
                is ResultState.Loading -> send(resultState)
            }
        }
    }

    /**
     * Fetches the remaining months in parallel (fire-and-forget style but
     * within the calling coroutine scope so it's cancellable).
     */
    private suspend fun prefetchRemainingMonths(
        months: List<Pair<Int, Int>>,
        latitude: Double,
        longitude: Double,
        school: School,
        city: String,
    ) {
        try {
            coroutineScope {
                months.map { (y, m) ->
                    async {
                        try {
                            repository.getMonthlyPrayerTimes(
                                year = y, month = m,
                                latitude = latitude, longitude = longitude,
                                school = school.code
                            ).collect { resultState ->
                                if (resultState is ResultState.Success) {
                                    val uiModels = resultState.data.map { it.toUiModel(school = school) }
                                    val key = cacheKeyFor(city, school, y, m)
                                    cacheMonth(key, uiModels)
                                }
                            }
                        } catch (e: Exception) {
                            // Non-critical: log and continue
                            e.printStackTrace()
                        }
                    }
                }.awaitAll()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ── Caching ─────────────────────────────────────────────────────────

    /**
     * Persist a single month's data to DataStore AND update in-memory cache.
     * Does NOT remove other months — we keep up to 12 months in DataStore.
     */
    private suspend fun cacheMonth(cacheKey: String, uiModels: List<PrayerTimeUiModel>) {
        try {
            memoryCache[cacheKey] = uiModels
            val jsonString = json.encodeToString(uiModels)
            dataStore.edit { preferences ->
                preferences[stringPreferencesKey(cacheKey)] = jsonString
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Public: read monthly cache for a key (used by notification use-case).
     */
    suspend fun getCachedMonthlyPrayerTimes(cacheKey: String): List<PrayerTimeUiModel>? {
        memoryCache[cacheKey]?.let { return it }
        return try {
            val raw = dataStore.data.first()[stringPreferencesKey(cacheKey)]
            raw?.let {
                val list = json.decodeFromString<List<PrayerTimeUiModel>>(it)
                memoryCache[cacheKey] = list
                list
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // ── Address resolution (with cache) ─────────────────────────────────

    private suspend fun resolveAddress(
        latitude: Double,
        longitude: Double,
        prefs: Preferences
    ): AddressInfo {
        // Fast: in-memory
        addressCache?.let { return it }

        // Medium: DataStore
        val cachedJson = prefs[stringPreferencesKey(ADDRESS_CACHE_KEY)]
        if (cachedJson != null) {
            try {
                val cached = json.decodeFromString<AddressInfo>(cachedJson)
                addressCache = cached
                return cached
            } catch (_: Exception) { /* fall through */ }
        }

        // Slow: Geocoder
        val info = try {
            getPlaceName(latitude, longitude)
        } catch (_: Exception) {
            AddressInfo(district = null, city = null, country = null, address = "Unknown")
        }
        addressCache = info

        // Persist for next cold start
        try {
            dataStore.edit { it[stringPreferencesKey(ADDRESS_CACHE_KEY)] = json.encodeToString(info) }
        } catch (_: Exception) { }

        return info
    }

    // ── Notify prayer list ──────────────────────────────────────────────

    private fun readNotifyPrayersList(prefs: Preferences): List<PrayerNameEnum> {
        return try {
            val raw = prefs[NOTIFY_PRAYERS_KEY] ?: "[]"
            json.decodeFromString<List<String>>(raw).mapNotNull {
                try { PrayerNameEnum.valueOf(it) } catch (_: Exception) { null }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    // ── Update current prayer + notify flags ────────────────────────────

    private fun updateCurrentPrayer(uiModel: PrayerTimeUiModel): PrayerTimeUiModel {
        val now = Clock.System.now()
        val currentPrayer = uiModel.intervals.find { interval ->
            interval.startTimeInstant != null &&
                    interval.endTimeInstant != null &&
                    now >= interval.startTimeInstant &&
                    now < interval.endTimeInstant
        }

        val currentList = notifyListCache ?: emptyList()

        return uiModel.copy(
            currentPrayer = currentPrayer,
            prayerImage = getImageByPrayerEnum(currentPrayer?.name),
            intervals = uiModel.intervals.map {
                it.copy(shouldNotify = currentList.contains(it.name))
            }
        )
    }

    /**
     * Clears all prayer-time caches (memory + DataStore).
     * Call this when the user changes school or location.
     */
    suspend fun invalidateCache() {
        memoryCache.clear()
        addressCache = null
        notifyListCache = null
        try {
            dataStore.edit { prefs ->
                val toRemove = prefs.asMap().keys.filter { it.name.startsWith(PREFIX) }
                toRemove.forEach { prefs.remove(it) }
                prefs.remove(stringPreferencesKey(ADDRESS_CACHE_KEY))
            }
        } catch (_: Exception) { }
    }
}
