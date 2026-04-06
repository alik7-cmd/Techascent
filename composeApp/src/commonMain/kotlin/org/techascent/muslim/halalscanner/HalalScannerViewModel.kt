package org.techascent.muslim.halalscanner

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.techascent.muslim.datastore.DataStoreKey
import org.techascent.muslim.halalscanner.state.HalalScannerUiState
import org.techascent.muslim.halalscanner.state.ScanHistoryItem
import org.techascent.muslim.halalscanner.state.ScanSource
import org.techascent.muslim.halalscanner.state.toHistoryItem
import org.techascent.muslim.halalscanner.state.toUiState
import org.techascent.shared.data.repository.halalscanner.HalalScannerRepository
import org.techascent.shared.network.ResultState

class HalalScannerViewModel(
    val repository: HalalScannerRepository,
    private val dataStore: DataStore<Preferences>,
) : ViewModel() {
    private val _uiState: MutableStateFlow<HalalScannerUiState> =
        MutableStateFlow(HalalScannerUiState.Init)
    val uiState get() = _uiState.asStateFlow()

    private val _historyState: MutableStateFlow<List<ScanHistoryItem>> =
        MutableStateFlow(emptyList())
    val historyState get() = _historyState.asStateFlow()

    private val historyKey = stringPreferencesKey(DataStoreKey.SCAN_HISTORY)

    init {
        loadHistory()
    }

    fun fetchProductByBarcode(barcode: String) = viewModelScope.launch {
        repository.fetchProductByBarcode(
            barcode = barcode
        ).collect { state ->
            when (state) {
                is ResultState.Error -> _uiState.value =
                    HalalScannerUiState.Error(state.message ?: "")

                is ResultState.Loading -> _uiState.value = HalalScannerUiState.Loading

                is ResultState.Success -> {
                    val productUiState = state.data.toUiState()
                    _uiState.value = HalalScannerUiState.Success(productUiState)
                    saveToHistory(productUiState.toHistoryItem(barcode = barcode, source = ScanSource.SCANNER))
                }
            }
        }
    }

    fun fetchProductByBarcodeManual(barcode: String) = viewModelScope.launch {
        repository.fetchProductByBarcode(
            barcode = barcode
        ).collect { state ->
            when (state) {
                is ResultState.Error -> _uiState.value =
                    HalalScannerUiState.Error(state.message ?: "")

                is ResultState.Loading -> _uiState.value = HalalScannerUiState.Loading

                is ResultState.Success -> {
                    val productUiState = state.data.toUiState()
                    _uiState.value = HalalScannerUiState.Success(productUiState)
                    saveToHistory(productUiState.toHistoryItem(barcode = barcode, source = ScanSource.MANUAL_BARCODE))
                }
            }
        }
    }

    fun checkIngredients(ingredientsText: String) = viewModelScope.launch {
        _uiState.value = HalalScannerUiState.Loading

        val productDto = repository.checkIngredients(ingredientsText)
        val productUiState = productDto.toUiState()

        _uiState.value = HalalScannerUiState.Success(productUiState)
        saveToHistory(productUiState.toHistoryItem(source = ScanSource.MANUAL_INGREDIENTS))
    }

    fun resetState() {
        _uiState.value = HalalScannerUiState.Init
    }

    private fun loadHistory() = viewModelScope.launch {
        try {
            val prefs = dataStore.data.first()
            val json = prefs[historyKey] ?: return@launch
            val items = Json.decodeFromString<List<ScanHistoryItem>>(json)
            _historyState.value = items.sortedByDescending { it.timestamp }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun saveToHistory(item: ScanHistoryItem) {
        try {
            val currentList = _historyState.value.toMutableList()
            currentList.add(0, item)
            // Keep max 100 items
            val trimmedList = currentList.take(100)
            _historyState.value = trimmedList

            dataStore.edit { prefs ->
                prefs[historyKey] = Json.encodeToString(trimmedList)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun clearHistory() = viewModelScope.launch {
        _historyState.value = emptyList()
        dataStore.edit { prefs ->
            prefs.remove(historyKey)
        }
    }
}