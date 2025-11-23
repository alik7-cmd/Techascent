package org.techascent.muslim.tasbeeh

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.techascent.muslim.datastore.DataStoreKey
import org.techascent.muslim.tasbeeh.state.TasbeehUiState
import kotlin.text.get

class TasbeehViewModel(
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    companion object {
        private val COUNTER_KEY = intPreferencesKey(DataStoreKey.TASBBEH_COUNTER)
        private val SET_KEY = intPreferencesKey(DataStoreKey.SET_COUNTER)
        private val HAPTIC_KEY = booleanPreferencesKey(DataStoreKey.HAPTIC_FEEDBACK)

    }

    internal val uiState: MutableStateFlow<TasbeehUiState> =
        MutableStateFlow(TasbeehUiState())

    init {
        loadCounterFromDataStore()
    }

    private fun loadCounterFromDataStore() {
        viewModelScope.launch {
            kotlinx.coroutines.flow.combine(
                dataStore.data.map { preferences -> preferences[COUNTER_KEY] ?: 0 },
                dataStore.data.map { preferences -> preferences[SET_KEY] ?: 0 },
                dataStore.data.map { preferences -> preferences[HAPTIC_KEY] ?: true }
            ) { count, sets, haptic ->
                Triple(count, sets, haptic)
            }.collect { (count, sets, haptic) ->
                uiState.update { currentState ->
                    currentState.copy(count = count, sets = sets, haptic = haptic)
                }
            }
        }
    }


    fun onCounterIncrement() {
        uiState.update { currentState ->
            val newCount = currentState.count + 1
            saveCounterToDataStore(newCount)
            currentState.copy(count = newCount)
        }
    }

    fun onResetIncrement() {
        uiState.update { currentState ->
            saveCounterToDataStore(0)
            saveSetToDataStore(0)
            currentState.copy(count = 0, sets = 0)
        }
    }

    private fun saveCounterToDataStore(count: Int) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[COUNTER_KEY] = count
            }
        }
    }

    private fun saveSetToDataStore(count: Int) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[SET_KEY] = count
            }
        }
    }

    fun onSetComplete() {
        viewModelScope.launch {
            uiState.update { currentState ->
                saveSetToDataStore(currentState.sets.plus(1))
                saveCounterToDataStore(0)
                currentState.copy(
                    sets = currentState.sets.plus(1),
                    count = 0
                )
            }
        }
    }

    fun onUpdateDialogVisibility() {
        uiState.update { currentState ->
            currentState.copy(shouldShowResetDialog = !currentState.shouldShowResetDialog)
        }
    }

    fun onProceedClick() {
        saveCounterToDataStore(0)
        uiState.update { currentState ->
            currentState.copy(
                shouldShowResetDialog = false,
                count = 0
            )
        }
    }
}
