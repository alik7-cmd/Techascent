package org.techascent.aifinder.presentation.scan

import org.techascent.aifinder.presentation.theme.AiFinderSpacing

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import apphub.aifinder.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.techascent.aifinder.presentation.components.*
import org.techascent.aifinder.presentation.theme.AiFinderPalette

@Composable
fun ScanScreen(
    onOpenCamera: () -> Unit,
    onBack: () -> Unit,
    selectedLocation: String,
    onChooseLocation: () -> Unit,
) {
    val needsPlace = selectedLocation.isBlank()
    ScreenLayout(topBar = { ScreenTopBar(stringResource(Res.string.home_scan_action), onBack) }) {
        item {
            Text(stringResource(Res.string.scan_setup_intro), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Surface(
                onClick = onChooseLocation,
                shape = RoundedCornerShape(24.dp),
                color = if (needsPlace) AiFinderPalette.Mint else AiFinderPalette.Surface,
                border = BorderStroke(if (needsPlace) 2.dp else 1.dp,
                    if (needsPlace) AiFinderPalette.Teal else AiFinderPalette.Border),
            ) {
                Column(Modifier.fillMaxWidth().padding(AiFinderSpacing.dp20), verticalArrangement = Arrangement.spacedBy(AiFinderSpacing.dp12)) {
                    Icon(Icons.Outlined.Place, null, tint = AiFinderPalette.Teal)
                    Text(stringResource(if (needsPlace) Res.string.scan_place_first else Res.string.location_save_into),
                        color = AiFinderPalette.Ink, fontWeight = FontWeight.Bold)
                    Text(if (needsPlace) stringResource(Res.string.scan_place_reason) else selectedLocation,
                        color = if (needsPlace) AiFinderPalette.Ink else AiFinderPalette.Teal)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AiFinderSpacing.dp8)) {
                        Text(stringResource(if (needsPlace) Res.string.location_choose_title else Res.string.scan_change_place),
                            modifier = Modifier.weight(1f),
                            color = AiFinderPalette.Teal, fontWeight = FontWeight.SemiBold)
                        Icon(Icons.Outlined.ChevronRight, null, tint = AiFinderPalette.Teal, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(AiFinderSpacing.dp16)) {
                InfoCard(
                    stringResource(Res.string.scan_camera_step),
                    stringResource(Res.string.scan_capture_guidance),
                    leadingContent = { Icon(Icons.Outlined.CameraAlt, null, tint = AiFinderPalette.Teal) },
                )
                PrimaryAction(
                    stringResource(if (needsPlace) Res.string.scan_choose_continue else Res.string.scan_open_camera),
                    onClick = if (needsPlace) onChooseLocation else onOpenCamera,
                )
                Text(stringResource(Res.string.scan_credit_notice), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
