package org.techascent.muslim.utility

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import apphub.composeapp.generated.resources.Res
import apphub.composeapp.generated.resources.button_open_settings
import apphub.composeapp.generated.resources.text_cancel
import apphub.composeapp.generated.resources.text_halal_scanner_more
import apphub.composeapp.generated.resources.text_permission_description
import apphub.composeapp.generated.resources.text_permission_title
import apphub.composeapp.generated.resources.title_halal_scanner
import apphub.composeapp.generated.resources.title_nearby_mosque
import apphub.composeapp.generated.resources.title_quibla
import apphub.composeapp.generated.resources.title_tasbeeh
import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.techascent.composa.appbar.TopAppBar
import org.techascent.composa.card.ComposaCardFrame
import org.techascent.composa.common.ComposaSpacing
import org.techascent.composa.common.DrawableData
import org.techascent.composa.icon.ComposaIcon
import org.techascent.composa.theming.ComposaTheme
import org.techascent.muslim.openNearbyMosques
import org.techascent.muslim.utility.state.FeatureItem


@Composable
fun UtilityView(
    onNavigateToTasbeeh: () -> Unit,
    onNavigateToCompass: () -> Unit,
    onNavigateHalalScanner: () -> Unit,
) {
    ComposaTheme {
        UtilityScreen(
            onNavigateToCompass = onNavigateToCompass,
            onNavigateToTasbeeh = onNavigateToTasbeeh,
            onNavigateHalalScanner = onNavigateHalalScanner
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, KoinExperimentalAPI::class)
@Composable
private fun UtilityScreen(
    onNavigateToTasbeeh: () -> Unit,
    onNavigateToCompass: () -> Unit,
    onNavigateHalalScanner: () -> Unit,
    viewModel: UtilityViewModel = koinViewModel<UtilityViewModel>(),
) {
    val uiState by viewModel.uiState.collectAsState()
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = Modifier
            .fillMaxSize()
            .background(ComposaTheme.color.backgroundAppBackground),
        topBar = {
            TopAppBar(
                title = stringResource(Res.string.text_halal_scanner_more),
            )
        },
    ) { innerPadding ->

        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
        ) {
            val minCellSize = (maxWidth / 2).coerceAtLeast(150.dp)
            LazyVerticalGrid(
                modifier = Modifier.fillMaxSize(),
                contentPadding = innerPadding,
                columns = GridCells.Adaptive(minSize = minCellSize)
            ) {
                items(uiState.listOfFeatures) { item ->
                    CenteredCardWithImageAndTitle(
                        item = item,
                        onNavigateToCompass = onNavigateToCompass,
                        onNavigateToTasbeeh = onNavigateToTasbeeh,
                        onNavigateHalalScanner = onNavigateHalalScanner
                    )
                }
            }
        }
    }
}

@Composable
fun CenteredCardWithImageAndTitle(
    item: FeatureItem,
    modifier: Modifier = Modifier,
    onNavigateToTasbeeh: () -> Unit,
    onNavigateToCompass: () -> Unit,
    onNavigateHalalScanner: () -> Unit,
) {
    Box(
        modifier = modifier.fillMaxSize().padding(ComposaSpacing.ExtraSmall),
        contentAlignment = Alignment.Center
    ) {
        val coroutineScope = rememberCoroutineScope()
        var showSettingsDialog by remember { mutableStateOf(false) }
        val factory = rememberPermissionsControllerFactory()
        val controller = remember(factory) {
            factory.createPermissionsController()
        }
        BindEffect(controller)
        ComposaCardFrame(
            borderColor = ComposaTheme.color.strokeNeutralSubtle,
            modifier = Modifier.clickable {
                when (item.titleRes) {
                    Res.string.title_tasbeeh -> onNavigateToTasbeeh()
                    Res.string.title_quibla -> onNavigateToCompass()
                    Res.string.title_halal_scanner -> {
                        coroutineScope.launch {
                            try {
                                controller.providePermission(Permission.CAMERA)
                                onNavigateHalalScanner()
                            } catch (e: DeniedException) {
                                e.printStackTrace()
                                showSettingsDialog = true
                            } catch (e: DeniedAlwaysException) {
                                e.printStackTrace()
                                showSettingsDialog = true
                            }
                        }
                    }

                    Res.string.title_nearby_mosque -> openNearbyMosques()
                }
            },
            content = {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(ComposaSpacing.Medium),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ComposaIcon(
                        modifier = Modifier.size(60.dp),
                        icon = DrawableData(
                            imageRes = item.imageRes,
                            tint = item.tint,
                        )
                    )

                    Spacer(modifier = Modifier.size(ComposaSpacing.Medium))
                    Text(
                        text = stringResource(item.titleRes),
                        style = ComposaTheme.typography.bodyEmphasized
                    )
                }
            }
        )
        if (showSettingsDialog) {
            AlertDialog(
                onDismissRequest = { showSettingsDialog = false },
                title = { Text(stringResource(Res.string.text_permission_title)) },
                text = { Text(stringResource(Res.string.text_permission_description)) },
                confirmButton = {
                    Button(
                        onClick = {
                            showSettingsDialog = false
                            // 10. Open the app settings so the user can grant the permission.
                            controller.openAppSettings()
                        }
                    ) {
                        Text(stringResource(Res.string.button_open_settings))
                    }
                },
                dismissButton = {
                    Button(onClick = { showSettingsDialog = false }) {
                        Text(stringResource(Res.string.text_cancel))
                    }
                }
            )
        }
    }
}

