package org.techascent.muslim.location.state

import apphub.composeapp.generated.resources.Res
import apphub.composeapp.generated.resources.button_open_settings
import apphub.composeapp.generated.resources.button_text_location_picker
import apphub.composeapp.generated.resources.message_location_picker
import apphub.composeapp.generated.resources.title_location_picker
import dev.icerock.moko.permissions.PermissionState
import org.jetbrains.compose.resources.StringResource

data class LocationPickerUiState(
    val title: StringResource = Res.string.title_location_picker,
    val message: StringResource = Res.string.message_location_picker,
    val buttonText: StringResource = Res.string.button_text_location_picker,
    val buttonOpenSettingsText: StringResource = Res.string.button_open_settings,
    val state: PermissionState = PermissionState.NotDetermined
)
