package org.techascent.shared.data.repository.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.techascent.shared.data.common.DataStoreKey
import org.techascent.shared.data.enum.School

class SettingsRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {

    companion object {
        private val SCHOOL_KEY = intPreferencesKey(DataStoreKey.SCHOOL_PREFERENCE)
        private val HAPTIC_KEY = booleanPreferencesKey(DataStoreKey.HAPTIC_FEEDBACK)
        private val ADHAN_KEY = booleanPreferencesKey(DataStoreKey.ADHAN_NOTIFICATION_PREFERENCE)
    }

    override fun observeSchoolPreference(): Flow<Int> =
        dataStore.data.map { it[SCHOOL_KEY] ?: School.HANAFI.code }

    override fun observeHapticPreference(): Flow<Boolean> =
        dataStore.data.map { it[HAPTIC_KEY] ?: true }

    override fun observeAdhanPreference(): Flow<Boolean> =
        dataStore.data.map { it[ADHAN_KEY] ?: true }

    override suspend fun updateSchoolPreference(code: Int) {
        dataStore.edit { it[SCHOOL_KEY] = code }
    }

    override suspend fun updateHapticPreference(enabled: Boolean) {
        dataStore.edit { it[HAPTIC_KEY] = enabled }
    }

    override suspend fun updateAdhanPreference(enabled: Boolean) {
        dataStore.edit { it[ADHAN_KEY] = enabled }
    }
}

