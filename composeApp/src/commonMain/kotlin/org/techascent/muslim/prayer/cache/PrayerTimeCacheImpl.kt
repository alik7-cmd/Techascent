package org.techascent.muslim.prayer.cache

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.techascent.muslim.datastore.DataStoreKey
import org.techascent.muslim.prayer.uimodel.PrayerTimeUiModel
import org.techascent.shared.data.enum.School

/**
 * DataStore-backed implementation of [PrayerTimeCache].
 *
 * Register as a **singleton** in your DI graph so the in-memory map persists
 * for the lifetime of the process and is shared across all callers.
 *
 * JSON serialization/deserialization is dispatched to [Dispatchers.Default] to
 * avoid blocking the calling coroutine's dispatcher.
 */
class PrayerTimeCacheImpl(
    private val dataStore: DataStore<Preferences>,
) : PrayerTimeCache {

    // ── In-memory layer ─────────────────────────────────────────────────
    private val memoryCache = mutableMapOf<String, List<PrayerTimeUiModel>>()

    private val json = Json { ignoreUnknownKeys = true }
    private val PREFIX = DataStoreKey.MONTHLY_PRAYER_INITIAL

    // ── PrayerTimeCache ─────────────────────────────────────────────────

    override fun keyFor(city: String, school: School, year: Int, month: Int): String =
        "${PREFIX}${city}_${school.name}_${year}_${month}"

    override suspend fun getToday(
        city: String,
        school: School,
        year: Int,
        month: Int,
        date: String,
    ): PrayerTimeUiModel? {
        val key = keyFor(city, school, year, month)

        // Fast path: in-memory
        memoryCache[key]?.find { it.currentDateTime == date }?.let { return it }

        // Slow path: DataStore → decode on Default dispatcher
        val raw = dataStore.data.first()[stringPreferencesKey(key)] ?: return null
        return withContext(Dispatchers.Default) {
            try {
                val list = json.decodeFromString<List<PrayerTimeUiModel>>(raw)
                memoryCache[key] = list
                list.find { it.currentDateTime == date }
            } catch (_: Exception) { null }
        }
    }

    override suspend fun putMonth(
        city: String,
        school: School,
        year: Int,
        month: Int,
        data: List<PrayerTimeUiModel>,
    ) {
        val key = keyFor(city, school, year, month)
        try {
            memoryCache[key] = data
            val encoded = withContext(Dispatchers.Default) { json.encodeToString(data) }
            dataStore.edit { it[stringPreferencesKey(key)] = encoded }
        } catch (_: Exception) { }
    }

    override suspend fun getForKey(cacheKey: String): List<PrayerTimeUiModel>? {
        memoryCache[cacheKey]?.let { return it }
        return withContext(Dispatchers.Default) {
            try {
                val raw = dataStore.data.first()[stringPreferencesKey(cacheKey)]
                raw?.let {
                    val list = json.decodeFromString<List<PrayerTimeUiModel>>(it)
                    memoryCache[cacheKey] = list
                    list
                }
            } catch (_: Exception) { null }
        }
    }

    override suspend fun getAllData(): Map<String, PrayerTimeUiModel> {
        val result = mutableMapOf<String, PrayerTimeUiModel>()

        // 1. Anything already in memory
        memoryCache.values.forEach { list -> list.forEach { result[it.currentDateTime] = it } }

        // 2. Scan DataStore for keys not yet loaded into memory
        withContext(Dispatchers.Default) {
            try {
                val prefs = dataStore.data.first()
                for ((key, value) in prefs.asMap()) {
                    if (key.name.startsWith(PREFIX) && value is String && !memoryCache.containsKey(key.name)) {
                        try {
                            val list = json.decodeFromString<List<PrayerTimeUiModel>>(value)
                            memoryCache[key.name] = list
                            list.forEach { result[it.currentDateTime] = it }
                        } catch (_: Exception) { }
                    }
                }
            } catch (_: Exception) { }
        }

        return result
    }

    override suspend fun invalidate() {
        memoryCache.clear()
        try {
            dataStore.edit { prefs ->
                val toRemove = prefs.asMap().keys.filter { it.name.startsWith(PREFIX) }
                toRemove.forEach { prefs.remove(it) }
            }
        } catch (_: Exception) { }
    }
}

