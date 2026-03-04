package org.techascent.muslim.location

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import apphub.composeapp.generated.resources.Res
import apphub.composeapp.generated.resources.ic_map_location
import dev.icerock.moko.permissions.PermissionState
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import org.jetbrains.compose.resources.stringResource
import org.techascent.composa.button.primary.ComposaButton
import org.techascent.composa.common.ComposaSpacing
import org.techascent.composa.common.DrawableData
import org.techascent.composa.icon.ComposaIcon
import org.techascent.composa.theming.ComposaTheme
import org.techascent.muslim.location.state.LocationPickerUiState
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// ═════════════════════════════════════════════════════════════════════════════════
//  LocationPickerViewV2 — Redesigned with modern visual hierarchy
// ═════════════════════════════════════════════════════════════════════════════════

@Composable
fun LocationPickerViewV2(
    onNavigatePrayerView: () -> Unit,
) {
    val factory = rememberPermissionsControllerFactory()
    val controller = remember(factory) { factory.createPermissionsController() }
    BindEffect(controller)

    val viewModel = viewModel { LocationPickerViewModel(controller) }

    ComposaTheme {
        val uiState by viewModel.uiState.collectAsState()
        LocationPickerContentV2(
            uiState = uiState,
            controller = controller,
            onProvideOrRequestLocationPermission = viewModel::provideOrRequestLocationPermission,
            onNavigatePrayerView = onNavigatePrayerView,
        )
    }
}

// ─── Root layout ────────────────────────────────────────────────────────────────

@Composable
private fun LocationPickerContentV2(
    uiState: LocationPickerUiState,
    controller: PermissionsController,
    onProvideOrRequestLocationPermission: () -> Unit,
    onNavigatePrayerView: () -> Unit,
) {
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = Modifier
            .fillMaxSize()
            .background(ComposaTheme.color.backgroundAppBackground),
    ) { innerPadding ->
        LazyColumn {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    when (uiState.state) {
                        PermissionState.NotDetermined,
                        PermissionState.NotGranted,
                        PermissionState.Denied -> PermissionRequestScreen(
                            uiState = uiState,
                            isDeniedAlways = false,
                            onAction = onProvideOrRequestLocationPermission,
                        )

                        PermissionState.Granted -> onNavigatePrayerView()

                        PermissionState.DeniedAlways -> PermissionRequestScreen(
                            uiState = uiState,
                            isDeniedAlways = true,
                            onAction = { controller.openAppSettings() },
                        )
                    }
                }
            }
        }

    }
}

// ═════════════════════════════════════════════════════════════════════════════════
//  Unified permission request screen  — works for both initial & denied-always
// ═════════════════════════════════════════════════════════════════════════════════

@Composable
private fun PermissionRequestScreen(
    uiState: LocationPickerUiState,
    isDeniedAlways: Boolean,
    onAction: () -> Unit,
) {
    val skyStart = ComposaTheme.color.prayer.skyStart
    val skyEnd = ComposaTheme.color.prayer.skyEnd
    val accent = ComposaTheme.color.prayer.timerAccent

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(ComposaSpacing.Medium),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // ── Animated location hero illustration ──────────────────────
        LocationHeroCard(skyStart = skyStart, skyEnd = skyEnd, accent = accent)

        Spacer(Modifier.height(ComposaSpacing.ExtraLarge))

        // ── Title ────────────────────────────────────────────────────
        Text(
            text = stringResource(uiState.title),
            style = ComposaTheme.typography.titleMediumEmphasized,
            color = ComposaTheme.color.textNeutral,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = ComposaSpacing.Medium),
        )

        Spacer(Modifier.height(ComposaSpacing.Small))

        // ── Description ──────────────────────────────────────────────
        Text(
            text = stringResource(uiState.message),
            style = ComposaTheme.typography.footnote,
            color = ComposaTheme.color.textNeutralSubtle,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = ComposaSpacing.Large),
        )

        Spacer(Modifier.height(ComposaSpacing.ExtraLarge))

        // ── Features list ────────────────────────────────────────────
        FeatureHighlights(accent = accent)

        Spacer(Modifier.height(ComposaSpacing.ExtraLarge))

        // ── Action button ────────────────────────────────────────────
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val scale by animateFloatAsState(
            if (isPressed) 0.96f else 1f,
            spring(dampingRatio = Spring.DampingRatioMediumBouncy),
            label = "btnScale",
        )

        val buttonText = if (isDeniedAlways) {
            stringResource(uiState.buttonOpenSettingsText)
        } else {
            stringResource(uiState.buttonText)
        }

        ComposaButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ComposaSpacing.Medium)
                .scale(scale),
            text = buttonText,
            onClick = onAction,
            iconTint = Color.Unspecified,
        )

        if (isDeniedAlways) {
            Spacer(Modifier.height(ComposaSpacing.Small))
            Text(
                text = "⚠️  Location permission was permanently denied.\nPlease enable it in your device settings.",
                style = ComposaTheme.typography.caption,
                color = ComposaTheme.color.textWarningBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = ComposaSpacing.Large),
            )
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════════
//  Hero card — animated mosque silhouette + pulsing location pin
// ═════════════════════════════════════════════════════════════════════════════════

@Composable
private fun LocationHeroCard(
    skyStart: Color,
    skyEnd: Color,
    accent: Color,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "heroAnim")

    // Pulsing glow for location pin
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulseScale",
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulseAlpha",
    )

    // Floating stars twinkle
    val starAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "stars",
    )

    val stars = remember {
        List(12) {
            Triple(
                (it * 41 + 17) % 100 / 100f,
                (it * 59 + 11) % 100 / 100f,
                1f + (it % 3) * 0.5f,
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ComposaSpacing.Large)
            .height(200.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.verticalGradient(listOf(skyStart, skyEnd))),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val horizonY = h * 0.78f

            // ─ Stars ─────────────────────────────────────────────────
            stars.forEach { (xr, yr, r) ->
                val sy = yr * horizonY * 0.7f
                val sx = xr * w
                drawCircle(
                    Color.White.copy(alpha = starAlpha * (0.5f + r / 3f)),
                    radius = r,
                    center = Offset(sx, sy),
                )
            }

            // ─ Horizon ───────────────────────────────────────────────
            drawLine(
                Color.White.copy(alpha = 0.25f),
                start = Offset(0f, horizonY),
                end = Offset(w, horizonY),
                strokeWidth = 1.5f,
            )

            // ─ Simple mosque silhouette ──────────────────────────────
            val mosqueColor = Color.White.copy(alpha = 0.12f)
            val cx = w / 2
            val mBaseY = horizonY

            // Main dome
            val domePath = Path().apply {
                val domeW = w * 0.22f
                val domeH = h * 0.22f
                moveTo(cx - domeW, mBaseY)
                cubicTo(
                    cx - domeW, mBaseY - domeH * 1.6f,
                    cx + domeW, mBaseY - domeH * 1.6f,
                    cx + domeW, mBaseY,
                )
                close()
            }
            drawPath(domePath, mosqueColor)

            // Minaret left
            val minaretW = w * 0.025f
            val minaretH = h * 0.35f
            val mLeftX = cx - w * 0.2f
            drawLine(mosqueColor, Offset(mLeftX, mBaseY), Offset(mLeftX, mBaseY - minaretH), 4f)
            drawCircle(mosqueColor, 5f, Offset(mLeftX, mBaseY - minaretH))

            // Minaret right
            val mRightX = cx + w * 0.2f
            drawLine(mosqueColor, Offset(mRightX, mBaseY), Offset(mRightX, mBaseY - minaretH), 4f)
            drawCircle(mosqueColor, 5f, Offset(mRightX, mBaseY - minaretH))

            // ─ Location pin ──────────────────────────────────────────
            val pinX = cx
            val pinY = h * 0.35f

            // Pulse rings
            drawCircle(
                accent.copy(alpha = pulseAlpha),
                radius = 38f * pulseScale,
                center = Offset(pinX, pinY),
            )
            drawCircle(
                accent.copy(alpha = pulseAlpha * 0.6f),
                radius = 52f * pulseScale,
                center = Offset(pinX, pinY),
            )

            // Pin body
            val pinPath = Path().apply {
                moveTo(pinX, pinY + 22f) // tip
                cubicTo(
                    pinX - 16f, pinY + 4f,
                    pinX - 16f, pinY - 16f,
                    pinX, pinY - 20f,
                )
                cubicTo(
                    pinX + 16f, pinY - 16f,
                    pinX + 16f, pinY + 4f,
                    pinX, pinY + 22f,
                )
                close()
            }
            drawPath(pinPath, accent)

            // Inner dot
            drawCircle(Color.White, 5f, Offset(pinX, pinY - 4f))
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════════
//  Feature highlights — why we need location
// ═════════════════════════════════════════════════════════════════════════════════

@Composable
private fun FeatureHighlights(accent: Color) {
    val cardBg = ComposaTheme.color.prayer.cardBg

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ComposaSpacing.Medium)
            .clip(RoundedCornerShape(20.dp))
            .background(cardBg),
    ) {
        FeatureRow(
            emoji = "🕌",
            title = "Accurate Prayer Times",
            subtitle = "Based on your precise coordinates",
            accent = accent,
        )

        ThinDivider()

        FeatureRow(
            emoji = "🧭",
            title = "Qibla Direction",
            subtitle = "Find the direction to Makkah",
            accent = Color(0xFF00897B),
        )

        ThinDivider()

        FeatureRow(
            emoji = "🌅",
            title = "Sunrise & Sunset",
            subtitle = "Exact times for your location",
            accent = Color(0xFFFF8F00),
        )

        ThinDivider()

        FeatureRow(
            emoji = "🍽️",
            title = "Iftar & Suhur",
            subtitle = "Fasting times during Ramadan",
            accent = Color(0xFF7B1FA2),
        )
    }
}

@Composable
private fun FeatureRow(
    emoji: String,
    title: String,
    subtitle: String,
    accent: Color,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ComposaSpacing.Medium, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(emoji, fontSize = 20.sp)
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = ComposaTheme.typography.subheadEmphasized,
                color = ComposaTheme.color.textNeutral,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = ComposaTheme.typography.caption,
                color = ComposaTheme.color.textNeutralSubtle,
            )
        }

        Spacer(Modifier.width(8.dp))

        Text("✓", fontSize = 16.sp, color = accent)
    }
}

// ─── Thin Divider ───────────────────────────────────────────────────────────────

@Composable
private fun ThinDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = ComposaSpacing.Medium),
        thickness = 0.5.dp,
        color = ComposaTheme.color.strokeNeutralSubtle.copy(alpha = 0.4f),
    )
}

