package org.techascent.muslim.halalscanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.techascent.muslim.halalscanner.state.HalalScannerUiState
import org.techascent.shared.data.repository.halalscanner.HalalScannerRepository
import org.techascent.shared.network.ResultState

class HalalScannerViewModel(
    val repository: HalalScannerRepository,
) : ViewModel() {
    private val _uiState: MutableStateFlow<HalalScannerUiState> =
        MutableStateFlow(HalalScannerUiState())
    val uiState = _uiState.asStateFlow()


    fun fetchProductByBarcode(barcode: String) = viewModelScope.launch {
        repository.fetchProductByBarcode(
            barcode = barcode
        ).collect {state ->
            when (state) {
                is ResultState.Error -> _uiState.update {
                    it.copy(
                        loading = false,
                        resultText = state.message
                    )
                }

                is ResultState.Loading -> _uiState.update {
                    it.copy(
                        loading = true,
                    )
                }

                is ResultState.Success -> {
                    _uiState.update {
                        it.copy(
                            loading = false,
                            halalResult = state.data.halalResult
                        )
                    }
                }
            }
        }
    }

    fun updateScannerVisibility(shouldShowScanner: Boolean)= viewModelScope.launch {
        _uiState.update {
            it.copy(
                shouldShowScanner = shouldShowScanner
            )
        }

    }

}