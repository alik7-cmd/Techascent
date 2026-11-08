package org.techascent.aifinder.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.AddLocationAlt
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import apphub.aifinder.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.techascent.aifinder.domain.model.LocatedItem
import org.techascent.aifinder.presentation.components.ItemPhoto
import org.techascent.aifinder.presentation.components.SectionTitle
import org.techascent.aifinder.presentation.components.ShimmerBlock
import org.techascent.aifinder.presentation.theme.AiFinderPalette

@Composable
internal fun HomeInventoryOverview(data: HomeContent) {
    Surface(color = AiFinderPalette.Mint, shape = RoundedCornerShape(24.dp)) {
        Column(
            Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                stringResource(Res.string.home_overview), color = AiFinderPalette.Ink,
                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(28.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column {
                    Text(
                        data.itemCount.toString(),
                        color = AiFinderPalette.Teal,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        stringResource(Res.string.home_saved_items),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column {
                    Text(
                        data.placeCount.toString(),
                        color = AiFinderPalette.Teal,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        stringResource(Res.string.home_saved_places),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
internal fun HomeOverviewSkeleton() {
    ShimmerBlock(Modifier.fillMaxWidth().height(140.dp))
}

@Composable
internal fun HomeQuickActions(
    onScan: () -> Unit,
    checking: Boolean,
    onAddPlace: () -> Unit,
    onPlaces: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            stringResource(Res.string.home_quick_actions), color = AiFinderPalette.Ink,
            style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AssistChip(
                onClick = onScan, enabled = !checking, shape = RoundedCornerShape(50),
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = AiFinderPalette.Teal, labelColor = AiFinderPalette.Surface,
                    leadingIconContentColor = AiFinderPalette.Surface,
                ),
                label = { Text(stringResource(if (checking) Res.string.subscription_checking else Res.string.home_scan_action)) },
                leadingIcon = { Icon(Icons.Outlined.CameraAlt, null, Modifier.size(18.dp)) },
            )
            AssistChip(
                onClick = onAddPlace, shape = RoundedCornerShape(50),
                label = { Text(stringResource(Res.string.home_add_location)) },
                leadingIcon = { Icon(Icons.Outlined.AddLocationAlt, null, Modifier.size(18.dp)) })
            AssistChip(
                onClick = onPlaces, shape = RoundedCornerShape(50),
                label = { Text(stringResource(Res.string.home_all_places)) },
                leadingIcon = { Icon(Icons.Outlined.Place, null, Modifier.size(18.dp)) })
        }
    }
}

@Composable
internal fun HomeRecentHeading() {
    Column(Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        SectionTitle(stringResource(Res.string.home_recent_items))
        Text(
            stringResource(Res.string.home_recent_hint),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
internal fun HomeRecentItem(entry: LocatedItem, onClick: () -> Unit) {
    Surface(onClick = onClick, color = AiFinderPalette.Surface, shape = RoundedCornerShape(20.dp)) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ItemPhoto(
                entry.item.photoId, entry.item.name, RoundedCornerShape(12.dp),
                modifier = Modifier.size(56.dp), bounds = entry.bounds
            ) {
                Icon(Icons.Outlined.Inventory2, null, tint = AiFinderPalette.Teal)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    entry.item.name, color = AiFinderPalette.Ink,
                    style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold
                )
                Text(
                    entry.path, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Icon(
                Icons.Outlined.ChevronRight,
                null,
                tint = AiFinderPalette.Teal,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
