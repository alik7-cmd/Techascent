package org.techascent.muslim.tasbeeh

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import apphub.composeapp.generated.resources.Res
import apphub.composeapp.generated.resources.ic_back
import apphub.composeapp.generated.resources.message_tasbeeh
import apphub.composeapp.generated.resources.text_reset
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.techascent.composa.appbar.TopAppBar
import org.techascent.composa.appbar.TrailingAction
import org.techascent.composa.common.ComposaSpacing
import org.techascent.composa.theming.ComposaTheme
import org.techascent.muslim.performHapticFeedback
import org.techascent.muslim.showNativeResetDialog
import org.techascent.muslim.common.localizeDigits
import org.techascent.muslim.tasbeeh.state.TasbeehUiState

@Composable
internal fun TasbeehViewV2(
    onNavigateBack: () -> Unit
) {
    ComposaTheme {
        TasbeehScreenV2(onNavigateBack = onNavigateBack)
    }
}

@OptIn(KoinExperimentalAPI::class)
@Composable
private fun TasbeehScreenV2(
    viewModel: TasbeehViewModel = koinViewModel<TasbeehViewModel>(),
    onNavigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    TasbeehContentV2(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onTap = {
            if (uiState.haptic) performHapticFeedback()
            if (uiState.count + 1 >= uiState.goal) {
                viewModel.onSetComplete()
            } else {
                viewModel.onCounterIncrement()
            }
        },
        onReset = viewModel::onResetIncrement,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TasbeehContentV2(
    uiState: TasbeehUiState,
    onNavigateBack: () -> Unit,
    onTap: () -> Unit,
    onReset: () -> Unit,
) {
    val title = stringResource(resource = uiState.dialogProperty.title)
    val message = stringResource(resource = uiState.dialogProperty.message)
    val confirmText = stringResource(resource = uiState.dialogProperty.confirmText)
    val cancelText = stringResource(resource = uiState.dialogProperty.cancelText)

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = Modifier
            .fillMaxSize()
            .background(ComposaTheme.color.backgroundAppBackground),
        topBar = {
            TopAppBar(
                title = stringResource(resource = uiState.title),
                navigationIcon = Res.drawable.ic_back,
                onNavigationIconClicked = onNavigateBack,
                action = TrailingAction.TextButton(
                    text = stringResource(Res.string.text_reset),
                    onClick = {
                        ResetWarningDialog(
                            title = title,
                            message = message,
                            confirmText = confirmText,
                            cancelText = cancelText,
                            onDismissRequest = {},
                            onProceedClick = onReset,
                        )
                    }
                )
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = ComposaSpacing.Medium),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.weight(0.08f))

            // ---- Stats Row ----
            StatsRow(uiState = uiState)

            Spacer(modifier = Modifier.weight(0.1f))

            // ---- Main Tap Circle with Progress ----
            TapCircle(
                count = uiState.count,
                goal = uiState.goal,
                onTap = onTap,
            )

            Spacer(modifier = Modifier.weight(0.1f))

            // ---- Dhikr Suggestion Area ----
            DhikrSuggestion()

            Spacer(modifier = Modifier.weight(0.06f))

            // ---- Info Message ----
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = ComposaSpacing.Small),
                text = stringResource(resource = uiState.infoMessage),
                style = ComposaTheme.typography.footnote,
                color = ComposaTheme.color.textNeutralSubtle,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(ComposaSpacing.Large))
        }
    }
}

// ─── Stats Row ──────────────────────────────────────────────────────────────────

@Composable
private fun StatsRow(uiState: TasbeehUiState) {
    val totalCount = (uiState.sets * uiState.goal) + uiState.count
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ComposaSpacing.Small),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        StatCard(label = "Sets", value = uiState.sets.toString().localizeDigits())
        StatCard(label = "Goal", value = uiState.goal.toString().localizeDigits())
        StatCard(label = "Total", value = totalCount.toString().localizeDigits())
    }
}

@Composable
private fun StatCard(label: String, value: String) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(ComposaTheme.color.strokeNeutralSubtle.copy(alpha = 0.25f))
            .padding(horizontal = 28.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            style = ComposaTheme.typography.titleEmphasized,
            color = ComposaTheme.color.textNeutral,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = ComposaTheme.typography.caption,
            color = ComposaTheme.color.textNeutralSubtle,
        )
    }
}

// ─── Tap Circle ─────────────────────────────────────────────────────────────────

@Composable
private fun TapCircle(
    count: Int,
    goal: Int,
    onTap: () -> Unit,
) {
    val progress = if (goal > 0) (count % goal).toFloat() / goal else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "progress",
    )

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val circleSize by animateDpAsState(
        targetValue = if (isPressed) 228.dp else 240.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "circleSize",
    )

    // Track/arc colors
    val trackColor = ComposaTheme.color.strokeNeutralSubtle.copy(alpha = 0.35f)
    val progressColor = ComposaTheme.color.textAction
    val innerBg = ComposaTheme.color.backgroundAppBackground

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(260.dp),
    ) {
        // Outer progress ring
        Box(
            modifier = Modifier
                .size(circleSize)
                .drawBehind {
                    val strokeWidth = 14.dp.toPx()
                    val radius = (size.minDimension - strokeWidth) / 2f
                    val topLeft = Offset(
                        (size.width - radius * 2) / 2f,
                        (size.height - radius * 2) / 2f,
                    )
                    val arcSize = Size(radius * 2, radius * 2)

                    // Background track
                    drawArc(
                        color = trackColor,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    )

                    // Progress arc
                    drawArc(
                        color = progressColor,
                        startAngle = -90f,
                        sweepAngle = animatedProgress * 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    )
                }
        )

        // Inner tappable circle
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(200.dp)
                .shadow(
                    elevation = if (isPressed) 2.dp else 8.dp,
                    shape = CircleShape,
                    ambientColor = progressColor.copy(alpha = 0.15f),
                    spotColor = progressColor.copy(alpha = 0.15f),
                )
                .clip(CircleShape)
                .background(innerBg)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onTap,
                ),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = (count % goal).toString().localizeDigits(),
                    style = ComposaTheme.typography.titleLargeDemi.copy(
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = ComposaTheme.color.textNeutral,
                )
                Text(
                    text = "of $goal".localizeDigits(),
                    style = ComposaTheme.typography.footnote,
                    color = ComposaTheme.color.textNeutralSubtle,
                )
            }
        }
    }
}

// ─── Dhikr Suggestions ──────────────────────────────────────────────────────────

@Composable
private fun DhikrSuggestion() {
    val dhikrList = listOf(
        "سُبْحَانَ اللّٰهِ" to "SubhanAllah (Glory be to Allah)",
        "اَلْحَمْدُ لِلّٰهِ" to "Alhamdulillah (Praise be to Allah)",
        "اللّٰهُ أَكْبَرُ" to "Allahu Akbar (Allah is Greatest)",
        "لَا إِلٰهَ إِلَّا اللّٰهُ" to "La ilaha illallah (None worthy of worship but Allah)",
        "أَسْتَغْفِرُ اللّٰهَ" to "Astaghfirullah (I seek forgiveness from Allah)",
    )
    val randomDhikr = remember { dhikrList.random() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ComposaTheme.color.strokeNeutralSubtle.copy(alpha = 0.15f))
            .padding(horizontal = ComposaSpacing.Medium, vertical = ComposaSpacing.Medium),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = randomDhikr.first,
            style = ComposaTheme.typography.titleEmphasized.copy(fontSize = 26.sp),
            color = ComposaTheme.color.textNeutral,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = randomDhikr.second,
            style = ComposaTheme.typography.footnote,
            color = ComposaTheme.color.textNeutralSubtle,
            textAlign = TextAlign.Center,
        )
    }
}

fun ResetWarningDialog(
    title: String,
    message: String,
    confirmText: String,
    cancelText: String,
    onDismissRequest: () -> Unit,
    onProceedClick: () -> Unit
) {
    showNativeResetDialog(
        title = title,
        message = message,
        confirmText = confirmText,
        cancelText = cancelText,
        onConfirm = onProceedClick,
        onCancel = onDismissRequest
    )
}

