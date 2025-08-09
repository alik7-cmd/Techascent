package org.techascent.muslim.location

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionState
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.RequestCanceledException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.techascent.muslim.location.state.LocationPickerUiState

class LocationPickerViewModel(
    private val controller: PermissionsController
) : ViewModel() {

    private val _uiState = MutableStateFlow(LocationPickerUiState())
    val uiState: StateFlow<LocationPickerUiState> = _uiState

    init {
        viewModelScope.launch {
            updateState(state = controller.getPermissionState(Permission.COARSE_LOCATION))
        }
    }

    fun provideOrRequestLocationPermission() {
        viewModelScope.launch {
            try {
                controller.providePermission(Permission.LOCATION)
                updateState(state = PermissionState.Granted)
            } catch (_: DeniedAlwaysException) {
                updateState(state = PermissionState.DeniedAlways)
            } catch (_: DeniedException) {
                updateState(state = PermissionState.Denied)
            } catch (e: RequestCanceledException) {
                e.printStackTrace()
            }
        }
    }

    private fun updateState(state: PermissionState) {
        _uiState.update {
            it.copy(
                state = state
            )
        }
    }

}