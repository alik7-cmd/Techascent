package org.techascent.shared.data.repository.tasbeeh

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import org.techascent.shared.data.common.DataStoreKey

class TasbeehRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
) : TasbeehRepository {

    companion object {
        private val COUNTER_KEY = intPreferencesKey(DataStoreKey.TASBBEH_COUNTER)
        private val SET_KEY = intPreferencesKey(DataStoreKey.SET_COUNTER)
        private val HAPTIC_KEY = booleanPreferencesKey(DataStoreKey.HAPTIC_FEEDBACK)
    }

    override fun observeTasbeehData(): Flow<TasbeehData> {
        return combine(
            dataStore.data.map { it[COUNTER_KEY] ?: 0 },
            dataStore.data.map { it[SET_KEY] ?: 0 },
            dataStore.data.map { it[HAPTIC_KEY] ?: true },
        ) { count, sets, haptic ->
            TasbeehData(count = count, sets = sets, haptic = haptic)
        }
    }

    override suspend fun saveCounter(count: Int) {
        dataStore.edit { it[COUNTER_KEY] = count }
    }

    override suspend fun saveSet(count: Int) {
        dataStore.edit { it[SET_KEY] = count }
    }

    override suspend fun saveTasbeehData(count: Int, sets: Int) {
        dataStore.edit {
            it[COUNTER_KEY] = count
            it[SET_KEY] = sets
        }
    }

    override suspend fun resetAll() {
        dataStore.edit {
            it[COUNTER_KEY] = 0
            it[SET_KEY] = 0
        }
    }
}

