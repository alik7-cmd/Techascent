package org.techascent.aifinder.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import org.techascent.aifinder.data.*

class HomeViewModel(private val repository: InventoryRepository = MockInventoryRepository()) :
    ViewModel() {

    private val _uiState: MutableStateFlow<HomeUiState> =
        MutableStateFlow(HomeUiState.Loading)
    val uiState = _uiState.onStart {
        retry()
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        HomeUiState.Loading
    )

    fun retry() {
        _uiState.update {
            HomeUiState.Success(repository.spaces().let(::HomeContent))
        }
    }
}
