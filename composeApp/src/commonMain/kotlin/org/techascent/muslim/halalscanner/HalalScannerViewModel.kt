package org.techascent.muslim.halalscanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.techascent.muslim.halalscanner.state.HalalScannerUiState
import org.techascent.muslim.prayer.state.PrayerTimeUiState
import org.techascent.shared.data.repository.halalscanner.HalalScannerRepository
import org.techascent.shared.network.ResultState

class HalalScannerViewModel(
    val repository: HalalScannerRepository
) : ViewModel() {
    private val _uiState: MutableStateFlow<HalalScannerUiState> =
        MutableStateFlow(HalalScannerUiState())
    val uiState = _uiState.asStateFlow()


    fun fetchProductByBarcode(barcode: String) = viewModelScope.launch {
        repository.fetchProductByBarcode(
            barcode = barcode
        ).collect {
            when (it) {
                is ResultState.Error -> TODO()
                is ResultState.Loading -> TODO()
                is ResultState.Success -> TODO()
            }
        }
    }

}