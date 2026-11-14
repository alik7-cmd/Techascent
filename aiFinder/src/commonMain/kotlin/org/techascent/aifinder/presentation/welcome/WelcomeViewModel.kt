package org.techascent.aifinder.presentation.welcome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import apphub.aifinder.generated.resources.Res
import apphub.aifinder.generated.resources.welcome_session_error
import org.jetbrains.compose.resources.getString
import org.techascent.aifinder.domain.auth.ResolveWelcomeDestinationUseCase
import org.techascent.aifinder.domain.auth.WelcomeDestination

class WelcomeViewModel(
    private val resolveDestination: ResolveWelcomeDestinationUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow<WelcomeUiState>(WelcomeUiState.Loading)
    val state = _state.asStateFlow()

    private var checkJob: Job? = null

    init { retry() }

    fun retry() {
        if (checkJob?.isActive == true) return
        _state.value = WelcomeUiState.Loading
        checkJob = viewModelScope.launch {
            try {
                _state.value = when (val destination = resolveDestination()) {
                    WelcomeDestination.Welcome -> WelcomeUiState.Success(WelcomeContent())
                    else -> WelcomeUiState.Navigate(destination)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.value = WelcomeUiState.Error(e.message ?: getString(Res.string.welcome_session_error))
            }
        }
    }
}
