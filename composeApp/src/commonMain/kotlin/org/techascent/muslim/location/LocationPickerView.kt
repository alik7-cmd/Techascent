package org.techascent.muslim.location

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import apphub.composeapp.generated.resources.Res
import apphub.composeapp.generated.resources.ic_map_location
import dev.icerock.moko.permissions.PermissionState
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import org.jetbrains.compose.resources.stringResource
import org.koin.core.annotation.KoinExperimentalAPI
import org.techascent.composa.button.primary.ComposaButton
import org.techascent.composa.common.ComposaSpacing
import org.techascent.composa.common.DrawableData
import org.techascent.composa.icon.ComposaIcon
import org.techascent.composa.theming.ComposaTheme
import org.techascent.muslim.location.state.LocationPickerUiState

@Composable
fun LocationPickerView(
    onNavigatePrayerView: () -> Unit
) {
    val factory = rememberPermissionsControllerFactory()
    val controller = remember(factory) {
        factory.createPermissionsController()
    }
    BindEffect(controller)

    val viewModel = viewModel {
        LocationPickerViewModel(controller)
    }

    ComposaTheme {
        LocationPickerScreen(
            viewModel = viewModel,
            controller = controller,
            onProvideOrRequestLocationPermission = viewModel::provideOrRequestLocationPermission,
            onNavigatePrayerView = onNavigatePrayerView,
        )
    }
}

@OptIn(KoinExperimentalAPI::class)
@Composable
fun LocationPickerScreen(
    viewModel: LocationPickerViewModel,
    controller: PermissionsController,
    onProvideOrRequestLocationPermission: () -> Unit,
    onNavigatePrayerView: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    LocationPickerContent(
        uiState = uiState,
        controller = controller,
        onProvideOrRequestLocationPermission = onProvideOrRequestLocationPermission,
        onNavigatePrayerView = onNavigatePrayerView
    )

}

@Composable
private fun LocationPickerContent(
    uiState: LocationPickerUiState,
    controller: PermissionsController,
    onProvideOrRequestLocationPermission: () -> Unit,
    onNavigatePrayerView: () -> Unit
) {
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = Modifier
            .fillMaxSize()
            .background(color = ComposaTheme.color.backgroundAppBackground),
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = innerPadding,
            verticalArrangement = spacedBy(ComposaSpacing.Medium)
        ) {
            content(
                uiState = uiState,
                controller = controller,
                onProvideOrRequestLocationPermission = onProvideOrRequestLocationPermission,
                onNavigatePrayerView = onNavigatePrayerView
            )
        }
    }
}

private fun LazyListScope.content(
    uiState: LocationPickerUiState,
    controller: PermissionsController,
    onProvideOrRequestLocationPermission: () -> Unit,
    onNavigatePrayerView: () -> Unit
) {
    item {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillParentMaxHeight(), // or use .fillParentMaxHeight
            contentAlignment = Alignment.Center
        ) {
            when (uiState.state) {
                PermissionState.NotDetermined,
                PermissionState.NotGranted,
                PermissionState.Denied -> LocationNotDeterminedContent(
                    uiState = uiState,
                    onProvideOrRequestLocationPermission = onProvideOrRequestLocationPermission
                )

                PermissionState.Granted -> {
                    onNavigatePrayerView()
                }

                PermissionState.DeniedAlways -> LocationDeniedAlwaysContent(
                    uiState = uiState,
                    controller = controller
                )
            }
        }
    }
}

@Composable
private fun LocationNotDeterminedContent(
    uiState: LocationPickerUiState,
    onProvideOrRequestLocationPermission: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = spacedBy(ComposaSpacing.Medium)
    ) {
        ComposaIcon(
            modifier = Modifier.size(200.dp),
            icon = DrawableData(
                imageRes = Res.drawable.ic_map_location,
                tint = Color.Unspecified,
            )
        )
        Spacer(modifier = Modifier.size(ComposaSpacing.ExtraSmall))
        Text(
            modifier = Modifier.padding(horizontal = ComposaSpacing.Medium),
            text = stringResource(resource = uiState.title),
            style = ComposaTheme.typography.titleEmphasized
        )

        Text(
            modifier = Modifier.padding(horizontal = ComposaSpacing.Medium),
            text = stringResource(resource = uiState.message),
            style = ComposaTheme.typography.footnote
        )
        Spacer(modifier = Modifier.size(ComposaSpacing.Small))
        ComposaButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ComposaSpacing.Medium),
            text = stringResource(resource = uiState.buttonText),
            onClick = onProvideOrRequestLocationPermission,
            iconTint = Color.Unspecified
        )
    }
}

@Composable
private fun LocationDeniedAlwaysContent(
    uiState: LocationPickerUiState,
    controller: PermissionsController
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = spacedBy(ComposaSpacing.Medium)
    ) {
        ComposaIcon(
            modifier = Modifier.size(200.dp),
            icon = DrawableData(
                imageRes = Res.drawable.ic_map_location,
                tint = Color.Unspecified,
            )
        )
        Spacer(modifier = Modifier.size(ComposaSpacing.ExtraSmall))
        Text(
            modifier = Modifier.padding(horizontal = ComposaSpacing.Medium),
            text = stringResource(resource = uiState.title),
            style = ComposaTheme.typography.titleEmphasized
        )

        Text(
            modifier = Modifier.padding(horizontal = ComposaSpacing.Medium),
            text = stringResource(resource = uiState.message),
            style = ComposaTheme.typography.footnote
        )
        Spacer(modifier = Modifier.size(ComposaSpacing.Small))
        ComposaButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ComposaSpacing.Medium),
            text = stringResource(resource = uiState.buttonOpenSettingsText),
            onClick = { controller.openAppSettings() },
            iconTint = Color.Unspecified
        )
    }
}
