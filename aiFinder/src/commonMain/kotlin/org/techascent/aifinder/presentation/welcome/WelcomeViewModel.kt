package org.techascent.aifinder.presentation.welcome

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class WelcomeViewModel : ViewModel() {
    private val _state = MutableStateFlow<WelcomeUiState>(WelcomeUiState.Success(WelcomeContent()))
    val state = _state.asStateFlow()

    fun retry() { _state.value = WelcomeUiState.Success(WelcomeContent()) }
}
