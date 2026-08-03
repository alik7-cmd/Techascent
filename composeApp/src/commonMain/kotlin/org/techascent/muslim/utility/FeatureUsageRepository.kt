package org.techascent.muslim.utility

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Persists and queries how many times a user has tapped each Explore-tab feature.
 *
 * ### Storage format
 * Each [FeatureId] maps to a single `intPreferencesKey("usage_{key}")` in DataStore.
 * A value of `0` (or absent key) means the feature has never been opened.
 *
 * ### Usage
 * - Call [recordUsage] every time the user navigates to a feature from the Explore tab.
 * - Observe [getTopFeatures] from the Prayer screen ViewModel to drive the Quick Access section.
 *
 * This class is a singleton injected via Koin so the same DataStore instance is shared with
 * the rest of the app.
 */
class FeatureUsageRepository(private val dataStore: DataStore<Preferences>) {

    /**
     * Atomically increments the tap-count for [featureId] by 1.
     * Safe to call from any coroutine context.
     */
    suspend fun recordUsage(featureId: FeatureId) {
        dataStore.edit { prefs ->
            val key = prefKey(featureId)
            prefs[key] = (prefs[key] ?: 0) + 1
        }
    }

    /**
     * Returns a [Flow] that emits the top [limit] most-used features, sorted by usage count
     * (highest first). Features with zero usages are excluded so the list is empty on a fresh
     * install — the Prayer screen will hide the section automatically.
     *
     * @param limit Maximum number of features to return. Defaults to 3.
     */
    fun getTopFeatures(limit: Int = TOP_FEATURE_LIMIT): Flow<List<FeatureId>> =
        dataStore.data.map { prefs ->
            FeatureId.entries
                .map { feature -> feature to (prefs[prefKey(feature)] ?: 0) }
                .filter { (_, count) -> count > 0 }
                .sortedByDescending { (_, count) -> count }
                .take(limit)
                .map { (feature, _) -> feature }
        }

    // ── Helpers ─────────────────────────────────────────────────────────────────

    private fun prefKey(featureId: FeatureId) =
        intPreferencesKey("usage_${featureId.key}")

    companion object {
        const val TOP_FEATURE_LIMIT = 3
    }
}

