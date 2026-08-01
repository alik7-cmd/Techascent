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
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import org.techascent.shared.data.common.AddressInfo
import org.techascent.shared.data.common.getCurrentDateFormatted
import org.techascent.shared.data.common.getYesterdayDateFormatted
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
        // ── 1. Single DataStore read for all prefs ──────────────────────
        val prefs = dataStore.data.first()
        val code = prefs[intPreferencesKey(DataStoreKey.SCHOOL_PREFERENCE)] ?: School.HANAFI.code
        val school = School.fromCode(code)
        val currentDate = getCurrentDateFormatted()
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val year = now.year
        val month = now.monthNumber

        notifyListCache = readNotifyPrayersList(prefs)

        // ── 2. Try to get live GPS location ─────────────────────────────
        val location = locationService.getCurrentLocation()

        // ── 3. Resolve address — live GPS or fall back to cached ─────────
        val locationUnavailable = location == null
        val addressInfo = if (location != null) {
            resolveAddress(location.latitude, location.longitude, prefs)
        } else {
            // GPS off → try in-memory cache, then DataStore cache
            addressCache
                ?: prefs[stringPreferencesKey(ADDRESS_CACHE_KEY)]?.let {
                    try {
                        json.decodeFromString<AddressInfo>(it).also { a -> addressCache = a }
                    } catch (_: Exception) { null }
                }
                ?: return flowOf(ResultState.Error("Location is unavailable and no cached data was found. Please enable GPS and open the app once to download prayer times for offline use."))
        }

        val city = addressInfo.district ?: addressInfo.city ?: "default"

        // ── 4. Try in-memory → DataStore cache for the current month ────
        val cacheKey = cacheKeyFor(city, school, year, month)
        val todayFromCache = findTodayInCache(cacheKey, currentDate, prefs)
        if (todayFromCache != null) {
            var data = updateCurrentPrayer(todayFromCache)
            // After midnight but before Fajr: Isha from yesterday is still active
            val yesterday = now.date.plus(-1, DateTimeUnit.DAY)
            val yCacheKey = cacheKeyFor(city, school, yesterday.year, yesterday.monthNumber)
            data = applyAfterMidnightIshaFix(data) { date -> findTodayInCache(yCacheKey, date, prefs) }
            return if (locationUnavailable) {
                val displayCity = addressInfo.city ?: addressInfo.district ?: "unknown"
                flowOf(ResultState.Warning(data = data, message = displayCity))
            } else {
                flowOf(ResultState.Success(data))
            }
        }

        // ── 5. Cache miss — only fetch if we have live location ──────────
        if (location == null) {
            return flowOf(ResultState.Error("Location is unavailable and no cached data was found. Please enable GPS and open the app once to download prayer times for offline use."))
        }

        return fetchAndCacheYear(
            year = year,
            startMonth = month,
            latitude = location.latitude,
            longitude = location.longitude,
            school = school,
            city = city,
            currentDate = currentDate,
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
                    val result = if (today != null) {
                        var data = updateCurrentPrayer(today)
                        // After midnight but before Fajr: Isha from yesterday is still active
                        data = applyAfterMidnightIshaFix(data) { date ->
                            uiModels.find { it.currentDateTime == date }
                        }
                        ResultState.Success(data)
                    } else {
                        ResultState.Error("Prayer data not found for today")
                    }
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
                else -> Unit
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

    // ── Address resolution (with location-change detection) ─────────────

    private suspend fun resolveAddress(
        latitude: Double,
        longitude: Double,
        prefs: Preferences
    ): AddressInfo {

        // ── 1. In-memory cache exists → geocode fresh & compare ─────────
        addressCache?.let { cached ->
            val fresh = try {
                getPlaceName(latitude, longitude)
            } catch (_: Exception) {
                return cached // Geocoder failed (offline) → serve cached silently
            }

            return if (isSameLocation(fresh, cached)) {
                cached // Same area → keep existing prayer cache
            } else {
                // User moved to a different area → invalidate prayer cache
                invalidatePrayerCacheOnly()
                val updated = fresh.copy(latitude = latitude, longitude = longitude)
                addressCache = updated
                persistAddress(updated)
                updated
            }
        }

        // ── 2. DataStore cache exists → geocode fresh & compare ─────────
        val cachedJson = prefs[stringPreferencesKey(ADDRESS_CACHE_KEY)]
        if (cachedJson != null) {
            try {
                val cached = json.decodeFromString<AddressInfo>(cachedJson)
                val fresh = try {
                    getPlaceName(latitude, longitude)
                } catch (_: Exception) {
                    addressCache = cached
                    return cached // Geocoder failed (offline) → serve cached silently
                }

                return if (isSameLocation(fresh, cached)) {
                    addressCache = cached
                    cached // Same area → keep existing prayer cache
                } else {
                    // User moved → invalidate prayer cache, update address
                    invalidatePrayerCacheOnly()
                    val updated = fresh.copy(latitude = latitude, longitude = longitude)
                    addressCache = updated
                    persistAddress(updated)
                    updated
                }
            } catch (_: Exception) { /* fall through */ }
        }

        // ── 3. No cache at all (first install) → geocode and store ──────
        val info = try {
            getPlaceName(latitude, longitude)
        } catch (_: Exception) {
            AddressInfo(district = null, city = null, country = null, address = "Unknown")
        }
        val withCoords = info.copy(latitude = latitude, longitude = longitude)
        addressCache = withCoords
        persistAddress(withCoords)
        return withCoords
    }

    /**
     * Compares two addresses by district → city → country (priority order).
     * Returns true if they represent the same prayer-time area.
     */
    private fun isSameLocation(fresh: AddressInfo, cached: AddressInfo): Boolean {
        return when {
            fresh.district != null && cached.district != null ->
                fresh.district.equals(cached.district, ignoreCase = true) &&
                fresh.country.equals(cached.country, ignoreCase = true)

            fresh.city != null && cached.city != null ->
                fresh.city.equals(cached.city, ignoreCase = true) &&
                fresh.country.equals(cached.country, ignoreCase = true)

            else ->
                fresh.country.equals(cached.country, ignoreCase = true)
        }
    }

    /**
     * Clears ONLY prayer-time month caches (memory + DataStore).
     * Does NOT clear the address cache — caller handles that separately.
     */
    private suspend fun invalidatePrayerCacheOnly() {
        memoryCache.clear()
        try {
            dataStore.edit { prefs ->
                val toRemove = prefs.asMap().keys.filter { it.name.startsWith(PREFIX) }
                toRemove.forEach { prefs.remove(it) }
            }
        } catch (_: Exception) { }
    }

    /**
     * Persists an AddressInfo to DataStore.
     */
    private suspend fun persistAddress(info: AddressInfo) {
        try {
            dataStore.edit { it[stringPreferencesKey(ADDRESS_CACHE_KEY)] = json.encodeToString(info) }
        } catch (_: Exception) { }
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

    /**
     * After midnight but before today's Fajr, the current Islamic "night" still belongs
     * to yesterday's Isha. This fix detects that window and:
     *  1. Sets [currentPrayer] to yesterday's Isha interval (endTimeInstant = today's Fajr)
     *  2. Replaces [iftarTime] with yesterday's fasting window so Suhoor shows correctly.
     *
     * [findYesterday] is a lambda that receives yesterday's date string and returns the
     * corresponding cached [PrayerTimeUiModel] if available.
     */
    private fun applyAfterMidnightIshaFix(
        data: PrayerTimeUiModel,
        findYesterday: (String) -> PrayerTimeUiModel?,
    ): PrayerTimeUiModel {
        if (data.currentPrayer != null) return data
        val nowInstant = Clock.System.now()
        val todayFajr = data.intervals
            .firstOrNull { it.name == PrayerNameEnum.FAJR }?.startTimeInstant ?: return data
        if (nowInstant >= todayFajr) return data           // Past Fajr — nothing to fix
        val yesterdayModel = findYesterday(getYesterdayDateFormatted()) ?: return data
        val ishaFromYesterday = yesterdayModel.intervals
            .firstOrNull { it.name == PrayerNameEnum.ISHA } ?: return data
        if (ishaFromYesterday.endTimeInstant == null || nowInstant >= ishaFromYesterday.endTimeInstant) return data
        return data.copy(
            currentPrayer = ishaFromYesterday,
            iftarTime = yesterdayModel.iftarTime,   // Suhoor countdown uses yesterday's window
            prayerImage = getImageByPrayerEnum(PrayerNameEnum.ISHA),
        )
    }

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
     * Clears ALL caches (memory + DataStore) including address.
     * Call this when the user changes school setting.
     */
    suspend fun invalidateCache() {
        invalidatePrayerCacheOnly()
        addressCache = null
        notifyListCache = null
        try {
            dataStore.edit { prefs ->
                prefs.remove(stringPreferencesKey(ADDRESS_CACHE_KEY))
            }
        } catch (_: Exception) { }
    }

    /**
     * Returns a flat map of "DD-MM-YYYY" → PrayerTimeUiModel for every day
     * currently held in cache (memory + DataStore). Used by the calendar feature.
     */
    suspend fun getAllCachedPrayerData(): Map<String, PrayerTimeUiModel> {
        val result = mutableMapOf<String, PrayerTimeUiModel>()

        // 1. Anything already in memory
        memoryCache.values.forEach { monthList ->
            monthList.forEach { model -> result[model.currentDateTime] = model }
        }

        // 2. Scan DataStore for any monthly keys not yet in memory
        try {
            val prefs = dataStore.data.first()
            for ((key, value) in prefs.asMap()) {
                if (key.name.startsWith(PREFIX) && value is String && !memoryCache.containsKey(key.name)) {
                    try {
                        val list = json.decodeFromString<List<PrayerTimeUiModel>>(value)
                        memoryCache[key.name] = list
                        list.forEach { model -> result[model.currentDateTime] = model }
                    } catch (_: Exception) { }
                }
            }
        } catch (_: Exception) { }

        return result
    }
}
