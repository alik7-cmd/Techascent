package org.techascent.aifinder.presentation.scan

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import apphub.aifinder.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.techascent.aifinder.presentation.theme.AiFinderPalette

@Composable
internal fun CreditCardComparison() {
    var stateIndex by rememberSaveable { mutableStateOf(0) }
    val states = listOf(
        Res.string.credit_preview_available,
        Res.string.credit_preview_empty,
        Res.string.credit_preview_loading,
    )
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            stringResource(Res.string.credit_preview_title),
            color = AiFinderPalette.Ink,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            stringResource(Res.string.credit_preview_notice),
            color = AiFinderPalette.Muted,
            fontSize = 14.sp,
            lineHeight = 20.sp,
        )
        OutlinedButton(onClick = { stateIndex = (stateIndex + 1) % states.size }) {
            Text(stringResource(Res.string.credit_preview_state, stringResource(states[stateIndex])))
        }
        CreditCardDesign.entries.forEach { design ->
            Text(
                stringResource(when (design) {
                    CreditCardDesign.DeepTeal -> Res.string.credit_preview_teal
                    CreditCardDesign.SoftMint -> Res.string.credit_preview_mint
                    CreditCardDesign.Compact -> Res.string.credit_preview_compact
                }),
                color = AiFinderPalette.Ink,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )
            CreditCardDesignPreview(design, stateIndex)
        }
    }
}

private enum class CreditCardDesign { DeepTeal, SoftMint, Compact }

@Composable
private fun CreditCardDesignPreview(design: CreditCardDesign, stateIndex: Int) {
    val dark = design == CreditCardDesign.DeepTeal
    val compact = design == CreditCardDesign.Compact
    val foreground = if (dark) Color.White else AiFinderPalette.Ink
    val secondary = if (dark) Color.White.copy(alpha = 0.8f) else AiFinderPalette.Muted
    val accent = if (dark) AiFinderPalette.IllustrationMint else AiFinderPalette.Teal
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = if (compact) AiFinderPalette.Surface else AiFinderPalette.Mint,
    ) {
        Box(
            Modifier.fillMaxWidth().then(
                if (dark) Modifier.background(
                    Brush.linearGradient(listOf(Color(0xFF174A48), AiFinderPalette.Teal))
                ) else Modifier
            )
        ) {
            if (dark) Canvas(Modifier.matchParentSize()) {
                drawCircle(Color.White.copy(alpha = 0.04f), size.width * 0.5f, Offset(size.width, 0f))
                drawCircle(Color.White.copy(alpha = 0.05f), size.width * 0.3f, Offset(size.width, 0f))
            }
            Column(
                Modifier.fillMaxWidth().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(if (compact) 16.dp else 24.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(Icons.Outlined.AutoAwesome, null, tint = accent, modifier = Modifier.size(20.dp))
                    Text(
                        stringResource(Res.string.scan_balance_title),
                        color = if (dark) secondary else AiFinderPalette.Teal,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.4.sp,
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Column(
                        Modifier.weight(1f).then(
                            if (design == CreditCardDesign.SoftMint)
                                Modifier.background(Color.White, RoundedCornerShape(20.dp)).padding(16.dp)
                            else Modifier
                        ),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        if (stateIndex == 2) {
                            CircularProgressIndicator(Modifier.size(32.dp), color = accent, strokeWidth = 2.dp)
                        } else {
                            Text(
                                text = if (stateIndex == 1) "0" else "12",
                                color = foreground,
                                fontSize = if (compact) 28.sp else 44.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Text(
                            stringResource(
                                if (stateIndex == 2) Res.string.subscription_checking
                                else Res.string.credit_preview_scans_available
                            ),
                            color = secondary,
                            fontSize = 13.sp,
                        )
                    }
                    if (compact) PreviewPurchaseAction(stateIndex, accent)
                }
                if (!compact) {
                    Text(
                        stringResource(
                            if (stateIndex == 1) Res.string.credit_preview_add_credits
                            else Res.string.credit_preview_scan_cost
                        ),
                        color = secondary,
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        PreviewPurchaseAction(stateIndex, accent)
                    }
                }
            }
        }
    }
}

@Composable
private fun PreviewPurchaseAction(stateIndex: Int, color: Color) {
    if (stateIndex == 2) {
        Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(Modifier.size(20.dp), color = color, strokeWidth = 2.dp)
        }
    } else {
        TextButton(
            onClick = {},
            colors = ButtonDefaults.textButtonColors(contentColor = color),
        ) {
            Text(stringResource(Res.string.scan_get_scans), fontWeight = FontWeight.SemiBold)
        }
    }
}
