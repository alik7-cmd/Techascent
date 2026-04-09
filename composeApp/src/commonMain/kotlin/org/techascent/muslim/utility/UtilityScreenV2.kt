package org.techascent.muslim.utility

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
            // ── Header ──────────────────────────────────────────────────
            item {
                GreetingHeader()
            }

            // ── Feature cards ───────────────────────────────────────────
            // Wide (hero) cards — first two items
            val wideItems = uiState.listOfFeatures.filter { it.isWide }
            items(wideItems) { item ->
                FeatureCardWide(
                    item = item,
                    onNavigateToCompass = onNavigateToCompass,
                    onNavigateToTasbeeh = onNavigateToTasbeeh,
                    onNavigateHalalScanner = onNavigateHalalScanner,
                    onNavigateToQuran = onNavigateToQuran,
                    onNavigateManualHalalCheck = onNavigateManualHalalCheck,
                    onNavigateScanHistory = onNavigateScanHistory,
                    onNavigateToCalendar = onNavigateToCalendar,
                )
            }

            // Compact grid — remaining items in pairs
            val compactItems = uiState.listOfFeatures.filter { !it.isWide }
            val rows = compactItems.chunked(2)
            items(rows) { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(ComposaSpacing.Small),
                ) {
                    rowItems.forEach { item ->
                        Box(modifier = Modifier.weight(1f)) {
                            FeatureCardCompact(
                                item = item,
                                onNavigateToCompass = onNavigateToCompass,
                                onNavigateToTasbeeh = onNavigateToTasbeeh,
                                onNavigateHalalScanner = onNavigateHalalScanner,
                                onNavigateToQuran = onNavigateToQuran,
                                onNavigateManualHalalCheck = onNavigateManualHalalCheck,
                                onNavigateScanHistory = onNavigateScanHistory,
                                onNavigateToCalendar = onNavigateToCalendar,
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

// ─── Wide Feature Card (Hero-style) ─────────────────────────────────────────────

@Composable
private fun FeatureCardWide(
    item: FeatureItem,
    onNavigateToTasbeeh: () -> Unit,
    onNavigateToCompass: () -> Unit,
    onNavigateHalalScanner: () -> Unit,
    onNavigateToQuran: () -> Unit,
    onNavigateManualHalalCheck: () -> Unit,
    onNavigateScanHistory: () -> Unit,
    onNavigateToCalendar: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "wideScale",
    )

    val onClick = rememberFeatureClickHandler(
        item = item,
        onNavigateToCompass = onNavigateToCompass,
        onNavigateToTasbeeh = onNavigateToTasbeeh,
        onNavigateHalalScanner = onNavigateHalalScanner,
        onNavigateToQuran = onNavigateToQuran,
        onNavigateManualHalalCheck = onNavigateManualHalalCheck,
        onNavigateScanHistory = onNavigateScanHistory,
        onNavigateToCalendar = onNavigateToCalendar,
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .background(item.accentColor.copy(alpha = 0.08f))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(ComposaSpacing.Medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Emoji circle
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(item.accentColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = item.emoji, fontSize = 28.sp)
        }

        Spacer(modifier = Modifier.width(ComposaSpacing.Medium))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(item.titleRes),
                style = ComposaTheme.typography.titleDemi,
                color = ComposaTheme.color.textNeutral,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            item.descriptionRes?.let { descRes ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(descRes),
                    style = ComposaTheme.typography.footnote,
                    color = ComposaTheme.color.textNeutralSubtle,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Spacer(modifier = Modifier.width(ComposaSpacing.Small))

        // Arrow indicator
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(item.accentColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "→",
                fontSize = 18.sp,
                color = item.accentColor,
            )
        }
    }
}

// ─── Compact Feature Card (Grid-style) ──────────────────────────────────────────

@Composable
private fun FeatureCardCompact(
    item: FeatureItem,
    onNavigateToTasbeeh: () -> Unit,
    onNavigateToCompass: () -> Unit,
    onNavigateHalalScanner: () -> Unit,
    onNavigateToQuran: () -> Unit,
    onNavigateManualHalalCheck: () -> Unit,
    onNavigateScanHistory: () -> Unit,
    onNavigateToCalendar: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "compactScale",
    )

    val onClick = rememberFeatureClickHandler(
        item = item,
        onNavigateToCompass = onNavigateToCompass,
        onNavigateToTasbeeh = onNavigateToTasbeeh,
        onNavigateHalalScanner = onNavigateHalalScanner,
        onNavigateToQuran = onNavigateToQuran,
        onNavigateManualHalalCheck = onNavigateManualHalalCheck,
        onNavigateScanHistory = onNavigateScanHistory,
        onNavigateToCalendar = onNavigateToCalendar,
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .background(item.accentColor.copy(alpha = 0.06f))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(ComposaSpacing.Medium),
        horizontalAlignment = Alignment.Start,
    ) {
        // Emoji circle
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(item.accentColor.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = item.emoji, fontSize = 24.sp)
        }

        Spacer(modifier = Modifier.height(ComposaSpacing.Small))

        Text(
            text = stringResource(item.titleRes),
            style = ComposaTheme.typography.subheadEmphasized,
            color = ComposaTheme.color.textNeutral,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        item.descriptionRes?.let { descRes ->
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = stringResource(descRes),
                style = ComposaTheme.typography.caption,
                color = ComposaTheme.color.textNeutralSubtle,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
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

