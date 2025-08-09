package org.techascent.muslim.city

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.techascent.muslim.city.state.CityPickerUiState
import org.techascent.muslim.common.parseCountriesFromLines
import org.techascent.muslim.readCsvFile

class CityPickerViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<CityPickerUiState>(value = CityPickerUiState.Loading)
    val uiState: StateFlow<CityPickerUiState> = _uiState

    fun loadCsv(filename: String) {
        viewModelScope.launch {
            val lines = readCsvFile(filename)
            val parsed = parseCountriesFromLines(lines)
            _uiState.value = CityPickerUiState.Success(data = parsed)
        }
    }
}