package org.techascent.muslim.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.techascent.muslim.settings.state.SettingsUiState
import org.techascent.muslim.settings.state.getSettingsUiState
import org.techascent.shared.data.enum.School
import kotlin.text.get

class SettingsViewModel(
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    val schoolPreference: StateFlow<Int> = dataStore.data
        .map { preferences ->
            preferences[intPreferencesKey("school_preference")] ?: School.HANAFI.code
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = School.HANAFI.code
        )

    private val _uiState = MutableStateFlow(getSettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState


    fun updateSchoolPreference(isChecked: Int) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[intPreferencesKey("school_preference")] = isChecked
            }
        }
    }


}