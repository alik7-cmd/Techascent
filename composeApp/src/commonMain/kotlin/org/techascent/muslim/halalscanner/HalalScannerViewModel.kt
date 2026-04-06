package org.techascent.muslim.halalscanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.techascent.muslim.halalscanner.state.HalalScannerUiState
import org.techascent.muslim.halalscanner.state.toHistoryItem
import org.techascent.muslim.halalscanner.state.toUiState
import org.techascent.shared.data.model.ScanHistoryItem
import org.techascent.shared.data.model.ScanSource
import org.techascent.shared.data.repository.halalscanner.HalalScannerRepository
import org.techascent.shared.network.ResultState

class HalalScannerViewModel(
    val repository: HalalScannerRepository,
) : ViewModel() {
    private val _uiState: MutableStateFlow<HalalScannerUiState> =
        MutableStateFlow(HalalScannerUiState.Init)
    val uiState get() = _uiState.asStateFlow()

    private val _historyState: MutableStateFlow<List<ScanHistoryItem>> =
        MutableStateFlow(emptyList())
    val historyState get() = _historyState.asStateFlow()

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
        _historyState.value = repository.getHistory()
    }

    private suspend fun saveToHistory(item: ScanHistoryItem) {
        _historyState.value = repository.saveToHistory(item)
    }

    fun clearHistory() = viewModelScope.launch {
        repository.clearHistory()
        _historyState.value = emptyList()
    }
}