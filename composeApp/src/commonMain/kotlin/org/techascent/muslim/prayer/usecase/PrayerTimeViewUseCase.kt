package org.techascent.muslim.prayer.usecase

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import org.techascent.muslim.common.location.LocationService
import org.techascent.muslim.datastore.DataStoreKey
import org.techascent.muslim.prayer.cache.PrayerTimeCache
import org.techascent.muslim.prayer.location.AddressResolutionResult
import org.techascent.muslim.prayer.location.AddressResolver
import org.techascent.muslim.prayer.uimodel.PrayerNameEnum
import org.techascent.muslim.prayer.uimodel.PrayerTimeUiModel
import org.techascent.muslim.prayer.uimodel.getImageByPrayerEnum
import org.techascent.muslim.prayer.uimodel.toUiModel
import org.techascent.shared.data.common.getCurrentDateFormatted
import org.techascent.shared.data.common.getYesterdayDateFormatted
import org.techascent.shared.data.enum.School
import org.techascent.shared.data.repository.PrayerTimesRepository
import org.techascent.shared.network.ResultState

/**
 * Orchestrates prayer-time loading.
 *
 * Single responsibility: **combine** location, cache, and network data into a
 * stream of [ResultState]<[PrayerTimeUiModel]>, then apply Islamic business
 * rules (current-prayer detection, after-midnight Isha fix).
 *
 * Caching  → [PrayerTimeCache]
 * Geocoding → [AddressResolver]
 *
 * Constructor-injection of interfaces makes every code path unit-testable
 * without touching DataStore or the platform geocoder.
 */
class PrayerTimeViewUseCase(
    private val repository: PrayerTimesRepository,
    private val locationService: LocationService,
    private val dataStore: DataStore<Preferences>,
    private val prayerCache: PrayerTimeCache,
    private val addressResolver: AddressResolver,
) {
    companion object {
        private val NOTIFY_PRAYERS_KEY = stringPreferencesKey(DataStoreKey.NOTIFICATION_PRAYER_LIST)
        private val json = Json { ignoreUnknownKeys = true }
    }

    /** In-memory notify list — avoids a repeated DataStore read inside [getMonthlyPrayerTimes]. */
    private var notifyListCache: List<PrayerNameEnum>? = null

    // ── Public API ──────────────────────────────────────────────────────

    /**
     * Emits today's prayer data.
     *
     * Fast-path: emits from [prayerCache] immediately (milliseconds), then
     * checks GPS in the background and silently updates if the user moved.
     */
    fun getMonthlyPrayerTimes(): Flow<ResultState<PrayerTimeUiModel>> = channelFlow {
        // 1. Single DataStore read for school + notify prefs
        val prefs = dataStore.data.first()
        val school = School.fromCode(
            prefs[intPreferencesKey(DataStoreKey.SCHOOL_PREFERENCE)] ?: School.HANAFI.code
        )
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val currentDate = getCurrentDateFormatted()
        notifyListCache = readNotifyPrayersList(prefs)

        // 2. GPS starts in background immediately
        val locationDeferred = async { locationService.getCurrentLocation() }

        // 3. Emit from cache right away using last-known address
        val cachedAddress = addressResolver.getCachedAddress()
        var cachedDataEmitted: PrayerTimeUiModel? = null
        if (cachedAddress != null) {
            val city = cachedAddress.district ?: cachedAddress.city ?: "default"
            val todayFromCache = prayerCache.getToday(city, school, now.year, now.monthNumber, currentDate)
            if (todayFromCache != null) {
                var data = updateCurrentPrayer(todayFromCache)
                val yesterday = now.date.plus(-1, DateTimeUnit.DAY)
                data = applyAfterMidnightIshaFix(data) { date ->
                    prayerCache.getToday(city, school, yesterday.year, yesterday.monthNumber, date)
                }
                send(ResultState.Success(data))
                cachedDataEmitted = data
            }
        }

        // 4. Await GPS (already running in parallel with step 3)
        val location = locationDeferred.await()

        when {
            location == null -> {
                if (cachedDataEmitted != null) {
                    val displayCity = cachedAddress?.city ?: cachedAddress?.district ?: "unknown"
                    send(ResultState.Warning(data = cachedDataEmitted, message = displayCity))
                } else {
                    send(ResultState.Error(NO_LOCATION_ERROR))
                }
            }
            else -> {
                val addressResult = addressResolver.resolve(location.latitude, location.longitude)
                val (newAddress, locationChanged) = when (addressResult) {
                    is AddressResolutionResult.Success ->
                        addressResult.address to addressResult.locationChanged
                    is AddressResolutionResult.Fallback ->
                        addressResult.cachedAddress to false
                    is AddressResolutionResult.Unavailable -> {
                        if (cachedDataEmitted == null) send(ResultState.Error(NO_LOCATION_ERROR))
                        return@channelFlow
                    }
                }

                if (locationChanged) prayerCache.invalidate()

                val newCity = newAddress.district ?: newAddress.city ?: "default"
                if (cachedDataEmitted == null || locationChanged) {
                    fetchAndCacheYear(
                        year = now.year,
                        startMonth = now.monthNumber,
                        latitude = location.latitude,
                        longitude = location.longitude,
                        school = school,
                        city = newCity,
                        currentDate = currentDate,
                    ).collect { result ->
                        // Suppress Loading if data is already on screen — avoids flicker
                        if (result is ResultState.Loading && cachedDataEmitted != null) return@collect
                        send(result)
                    }
                }
                // GPS confirmed same location — first emission was correct, nothing more to do.
            }
        }
    }

    /** Used by CalendarViewModel. */
    suspend fun getAllCachedPrayerData(): Map<String, PrayerTimeUiModel> =
        prayerCache.getAllData()

    /** Used by PrayerNotificationUseCase. */
    suspend fun getCachedMonthlyPrayerTimes(cacheKey: String): List<PrayerTimeUiModel>? =
        prayerCache.getForKey(cacheKey)

    /**
     * Clears all caches (prayer data + address).
     * Call when the user changes their calculation school setting.
     */
    suspend fun invalidateCache() {
        prayerCache.invalidate()
        addressResolver.clearCache()
        notifyListCache = null
    }

    // ── Network fetching ────────────────────────────────────────────────

    private fun fetchAndCacheYear(
        year: Int,
        startMonth: Int,
        latitude: Double,
        longitude: Double,
        school: School,
        city: String,
        currentDate: String,
    ): Flow<ResultState<PrayerTimeUiModel>> = channelFlow {
        val monthPairs = (0 until 12).map { offset ->
            val totalMonth = startMonth + offset
            val y = year + (totalMonth - 1) / 12
            val m = ((totalMonth - 1) % 12) + 1
            Pair(y, m)
        }
        val (firstYear, firstMonth) = monthPairs.first()

        repository.getMonthlyPrayerTimes(
            year = firstYear,
            month = firstMonth,
            latitude = latitude,
            longitude = longitude,
            school = school.code,
        ).collect { resultState ->
            when (resultState) {
                is ResultState.Success -> {
                    val uiModels = resultState.data.map { it.toUiModel(school = school) }
                    prayerCache.putMonth(city, school, firstYear, firstMonth, uiModels)

                    val today = uiModels.find { it.currentDateTime == currentDate }
                    val result = if (today != null) {
                        var data = updateCurrentPrayer(today)
                        data = applyAfterMidnightIshaFix(data) { date ->
                            uiModels.find { it.currentDateTime == date }
                        }
                        ResultState.Success(data)
                    } else {
                        ResultState.Error("Prayer data not found for today")
                    }
                    send(result)
                    launch { prefetchRemainingMonths(monthPairs.drop(1), latitude, longitude, school, city) }
                }
                is ResultState.Error -> send(resultState)
                is ResultState.Loading -> send(resultState)
                else -> Unit
            }
        }
    }

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
                                school = school.code,
                            ).collect { state ->
                                if (state is ResultState.Success) {
                                    prayerCache.putMonth(
                                        city, school, y, m,
                                        state.data.map { it.toUiModel(school = school) },
                                    )
                                }
                            }
                        } catch (_: Exception) { }
                    }
                }.awaitAll()
            }
        } catch (_: Exception) { }
    }

    // ── Business logic ──────────────────────────────────────────────────

    /**
     * After midnight but before Fajr the Islamic "night" still belongs to
     * yesterday's Isha. Patches [data] so the UI shows the correct prayer.
     */
    private suspend fun applyAfterMidnightIshaFix(
        data: PrayerTimeUiModel,
        findYesterday: suspend (String) -> PrayerTimeUiModel?,
    ): PrayerTimeUiModel {
        if (data.currentPrayer != null) return data
        val nowInstant = Clock.System.now()
        val todayFajr = data.intervals
            .firstOrNull { it.name == PrayerNameEnum.FAJR }?.startTimeInstant ?: return data
        if (nowInstant >= todayFajr) return data
        val yesterdayModel = findYesterday(getYesterdayDateFormatted()) ?: return data
        val ishaFromYesterday = yesterdayModel.intervals
            .firstOrNull { it.name == PrayerNameEnum.ISHA } ?: return data
        if (ishaFromYesterday.endTimeInstant == null ||
            nowInstant >= ishaFromYesterday.endTimeInstant) return data
        return data.copy(
            currentPrayer = ishaFromYesterday,
            iftarTime = yesterdayModel.iftarTime,
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
            },
        )
    }

    private fun readNotifyPrayersList(prefs: Preferences): List<PrayerNameEnum> {
        return try {
            val raw = prefs[NOTIFY_PRAYERS_KEY] ?: "[]"
            json.decodeFromString<List<String>>(raw).mapNotNull {
                try { PrayerNameEnum.valueOf(it) } catch (_: Exception) { null }
            }
        } catch (_: Exception) { emptyList() }
    }
}

private const val NO_LOCATION_ERROR =
    "Location is unavailable and no cached data was found. " +
    "Please enable GPS and open the app once to download prayer times for offline use."
