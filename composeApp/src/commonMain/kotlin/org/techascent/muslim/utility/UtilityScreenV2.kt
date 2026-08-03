package org.techascent.muslim.utility

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import apphub.composeapp.generated.resources.Res
import apphub.composeapp.generated.resources.button_open_settings
import apphub.composeapp.generated.resources.text_cancel
import apphub.composeapp.generated.resources.text_permission_description
import apphub.composeapp.generated.resources.text_permission_title
import apphub.composeapp.generated.resources.text_utility_greeting
import apphub.composeapp.generated.resources.text_utility_subtitle
import apphub.composeapp.generated.resources.title_halal_scanner
import apphub.composeapp.generated.resources.title_manual_halal_check
import apphub.composeapp.generated.resources.title_nearby_mosque
import apphub.composeapp.generated.resources.title_prayer_calendar
import apphub.composeapp.generated.resources.title_quibla
import apphub.composeapp.generated.resources.title_quran
import apphub.composeapp.generated.resources.title_scan_history
import apphub.composeapp.generated.resources.title_tasbeeh
import apphub.composeapp.generated.resources.title_zakat_calculator
import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.techascent.composa.common.ComposaSpacing
import org.techascent.composa.featurecard.FeatureCardCompact
import org.techascent.composa.theming.ComposaTheme
import org.techascent.muslim.openNearbyMosques
import org.techascent.muslim.showNativeResetDialog as showPermissionRationalDialog
import org.techascent.muslim.utility.state.FeatureItem

// ─── Public entry point ─────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class, KoinExperimentalAPI::class)
@Composable
internal fun UtilityScreenV2(
    onNavigateToTasbeeh: () -> Unit,
    onNavigateToCompass: () -> Unit,
    onNavigateHalalScanner: () -> Unit,
    onNavigateToQuran: () -> Unit,
    onNavigateManualHalalCheck: () -> Unit,
    onNavigateScanHistory: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    viewModel: UtilityViewModel = koinViewModel<UtilityViewModel>(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = Modifier
            .fillMaxSize()
            .background(ComposaTheme.color.backgroundAppBackground),
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + ComposaSpacing.Small,
                bottom = innerPadding.calculateBottomPadding() + ComposaSpacing.Large,
                start = ComposaSpacing.Medium,
                end = ComposaSpacing.Medium,
            ),
            verticalArrangement = Arrangement.spacedBy(ComposaSpacing.Small),
        ) {
            // ── Categorised feature cards ───────────────────────────────
            uiState.categories.forEach { category ->
                // Section title
                item(key = "category_${category.titleRes.hashCode()}") {
                    Text(
                        text = stringResource(category.titleRes),
                        style = ComposaTheme.typography.titleMediumEmphasized,
                        color = ComposaTheme.color.textNeutral,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = ComposaSpacing.Medium, bottom = ComposaSpacing.Small),
                    )
                }

                // Items in pairs (compact grid)
                val rows = category.items.chunked(2)
                items(rows) { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(ComposaSpacing.Small),
                    ) {
                        rowItems.forEach { item ->
                            Box(modifier = Modifier.weight(1f)) {
                                val onClick = rememberFeatureClickHandler(
                                    item = item,
                                    onNavigateToCompass = onNavigateToCompass,
                                    onNavigateToTasbeeh = onNavigateToTasbeeh,
                                    onNavigateHalalScanner = onNavigateHalalScanner,
                                    onNavigateToQuran = onNavigateToQuran,
                                    onNavigateManualHalalCheck = onNavigateManualHalalCheck,
                                    onNavigateScanHistory = onNavigateScanHistory,
                                    onNavigateToCalendar = onNavigateToCalendar,
                                    onRecordUsage = viewModel::recordUsage,
                                )
                                FeatureCardCompact(
                                    emoji = item.emoji,
                                    title = stringResource(item.titleRes),
                                    description = item.descriptionRes?.let { stringResource(it) },
                                    accentColor = item.accentColor,
                                    onClick = onClick,
                                )
                            }
                        }
                        // Fill remaining space if odd count
                        if (rowItems.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.size(ComposaSpacing.ExtraExtraExtraLarge)) }
        }
    }
}

// ─── Greeting Header ────────────────────────────────────────────────────────────

@Composable
private fun GreetingHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = ComposaSpacing.Small),
    ) {
        Text(
            text = stringResource(Res.string.text_utility_greeting),
            style = ComposaTheme.typography.titleMediumEmphasized,
            color = ComposaTheme.color.textNeutral,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(Res.string.text_utility_subtitle),
            style = ComposaTheme.typography.subhead,
            color = ComposaTheme.color.textNeutralSubtle,
        )
    }
}


// ─── Shared click handler ───────────────────────────────────────────────────────

@Composable
private fun rememberFeatureClickHandler(
    item: FeatureItem,
    onNavigateToCompass: () -> Unit,
    onNavigateToTasbeeh: () -> Unit,
    onNavigateHalalScanner: () -> Unit,
    onNavigateToQuran: () -> Unit,
    onNavigateManualHalalCheck: () -> Unit,
    onNavigateScanHistory: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onRecordUsage: (FeatureId) -> Unit,
): () -> Unit {
    val coroutineScope = rememberCoroutineScope()
    val factory = rememberPermissionsControllerFactory()
    val controller = remember(factory) { factory.createPermissionsController() }
    val title = stringResource(Res.string.text_permission_title)
    val message = stringResource(Res.string.text_permission_description)
    val confirmText = stringResource(Res.string.button_open_settings)
    val cancelText = stringResource(Res.string.text_cancel)
    val uriHandler = LocalUriHandler.current
    BindEffect(controller)

    return remember(item.titleRes) {
        {
            // Map StringResource → FeatureId for usage tracking (null = unmapped / external)
            val featureId: FeatureId? = when (item.titleRes) {
                Res.string.title_halal_scanner -> FeatureId.HALAL_SCANNER
                Res.string.title_manual_halal_check -> FeatureId.MANUAL_HALAL_CHECK
                Res.string.title_scan_history -> FeatureId.SCAN_HISTORY
                Res.string.title_quran -> FeatureId.QURAN
                Res.string.title_quibla -> FeatureId.QIBLA
                Res.string.title_tasbeeh -> FeatureId.TASBEEH
                Res.string.title_nearby_mosque -> FeatureId.NEARBY_MOSQUE
                Res.string.title_prayer_calendar -> FeatureId.PRAYER_CALENDAR
                Res.string.title_zakat_calculator -> FeatureId.ZAKAT_CALCULATOR
                else -> null
            }
            featureId?.let { onRecordUsage(it) }

            when (item.titleRes) {
                Res.string.title_quran -> onNavigateToQuran()
                Res.string.title_tasbeeh -> onNavigateToTasbeeh()
                Res.string.title_quibla -> onNavigateToCompass()
                Res.string.title_manual_halal_check -> onNavigateManualHalalCheck()
                Res.string.title_scan_history -> onNavigateScanHistory()
                Res.string.title_prayer_calendar -> onNavigateToCalendar()
                Res.string.title_halal_scanner -> {
                    coroutineScope.launch {
                        try {
                            controller.providePermission(Permission.CAMERA)
                            onNavigateHalalScanner()
                        } catch (e: DeniedException) {
                            showPermissionRationalDialog(
                                title = title,
                                message = message,
                                confirmText = confirmText,
                                cancelText = cancelText,
                                onConfirm = { controller.openAppSettings() },
                            )
                        } catch (e: DeniedAlwaysException) {
                            showPermissionRationalDialog(
                                title = title,
                                message = message,
                                confirmText = confirmText,
                                cancelText = cancelText,
                                onConfirm = { controller.openAppSettings() },
                            )
                        }
                    }
                }

                Res.string.title_nearby_mosque -> openNearbyMosques()
                Res.string.title_zakat_calculator -> uriHandler.openUri("https://idrf.ca/zakat-calculator/")
            }
        }
    }
}
