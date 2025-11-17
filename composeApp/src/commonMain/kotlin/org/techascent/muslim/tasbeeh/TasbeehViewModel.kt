package org.techascent.muslim.tasbeeh

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.techascent.muslim.tasbeeh.state.TasbeehUiState

class TasbeehViewModel(
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    companion object {
        private val COUNTER_KEY = intPreferencesKey("tasbeeh_counter")
    }

    internal val uiState: MutableStateFlow<TasbeehUiState> =
        MutableStateFlow(TasbeehUiState())

    init {
        loadCounterFromDataStore()
    }

    private fun loadCounterFromDataStore() {
        viewModelScope.launch {
            dataStore.data
                .map { preferences -> preferences[COUNTER_KEY] ?: 0 }
                .collect { count ->
                    uiState.update { currentState ->
                        currentState.copy(count = count)
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
            currentState.copy(count = 0)
        }
    }

    private fun saveCounterToDataStore(count: Int) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[COUNTER_KEY] = count
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
