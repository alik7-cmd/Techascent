package org.techascent.aifinder.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.animation.Crossfade
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import apphub.aifinder.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.techascent.aifinder.presentation.components.SectionTitle
import org.techascent.aifinder.presentation.theme.AiFinderPalette

@Composable
internal fun ScanPrompt(onScan: () -> Unit, isChecking: Boolean = false, compact: Boolean = false) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .padding(top = 6.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(AiFinderPalette.Mint)
            .padding(if (compact) 20.dp else 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (compact) {
            Text(
                stringResource(Res.string.home_scan_compact_title),
                color = AiFinderPalette.Ink,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
        } else SectionTitle(stringResource(Res.string.home_scan_title))
        Text(
            stringResource(if (compact) Res.string.home_scan_compact_description else Res.string.home_scan_description),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 16.sp,
            lineHeight = 24.sp
        )
        val checkingLabel = stringResource(Res.string.subscription_checking)
        Button(
            onClick = onScan,
            enabled = !isChecking,
            modifier = (if (compact) Modifier.fillMaxWidth() else Modifier.widthIn(min = 220.dp)).heightIn(min = 50.dp)
                .semantics { if (isChecking) stateDescription = checkingLabel },
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AiFinderPalette.Teal,
                contentColor = AiFinderPalette.OnTeal,
                disabledContainerColor = AiFinderPalette.Teal,
                disabledContentColor = AiFinderPalette.OnTeal
            )
        ) {
            // Keep the label so the button doesn't change width; only the leading icon swaps.
            Box(Modifier.size(20.dp), contentAlignment = Alignment.Center) {
                Crossfade(isChecking, label = "scanIcon") { checking ->
                    if (checking) {
                        CircularProgressIndicator(
                            Modifier.size(18.dp),
                            color = AiFinderPalette.OnTeal,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Outlined.CameraAlt, contentDescription = null, modifier = Modifier.size(20.dp))
                    }
                }
            }
            Spacer(Modifier.width(8.dp))
            Text(
                stringResource(Res.string.home_scan_action),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
