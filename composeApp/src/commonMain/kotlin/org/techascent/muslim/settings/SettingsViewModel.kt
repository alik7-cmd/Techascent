package org.techascent.muslim.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.techascent.muslim.datastore.DataStoreKey
import org.techascent.muslim.settings.event.SettingsEvent
import org.techascent.muslim.settings.state.SettingsUiState
import org.techascent.muslim.settings.state.getSettingsUiState
import org.techascent.shared.data.enum.School

class SettingsViewModel(
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    val schoolPreference: StateFlow<Int> = dataStore.data
        .map { preferences ->
            preferences[intPreferencesKey(DataStoreKey.SCHOOL_PREFERENCE)] ?: School.HANAFI.code
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = School.HANAFI.code
        )

    val hapticPreference: StateFlow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[booleanPreferencesKey(DataStoreKey.HAPTIC_FEEDBACK)] ?: true
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = true
        )

    private val _uiState = MutableStateFlow(getSettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState

    private val _event: Channel<SettingsEvent> = Channel()
    val event: Flow<SettingsEvent> = _event.receiveAsFlow()


    fun updateSchoolPreference(isChecked: Int) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[intPreferencesKey(DataStoreKey.SCHOOL_PREFERENCE)] = isChecked
            }
        }
    }

    fun onUpdateHaptic(isChecked: Boolean) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[booleanPreferencesKey(DataStoreKey.HAPTIC_FEEDBACK)] = isChecked
            }
        }
    }

    fun onUpdateAdhanNotification(isChecked: Boolean){
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[booleanPreferencesKey(DataStoreKey.ADHAN_NOTIFICATION_PREFERENCE)] = isChecked
            }
        }
    }

    fun onHandleEvent(event: SettingsEvent) = viewModelScope.launch {
        when (event) {
            is SettingsEvent.OpenExternalLink -> _event.send(element = event)
        }
    }


}