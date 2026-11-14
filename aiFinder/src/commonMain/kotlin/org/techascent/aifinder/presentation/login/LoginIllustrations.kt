package org.techascent.aifinder.presentation.login

import org.techascent.aifinder.presentation.theme.AiFinderSpacing

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Cable
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import apphub.aifinder.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.techascent.aifinder.presentation.theme.AiFinderPalette

/** Slide 2: a photo of a drawer where AI tags each item it recognises. */
@Composable
internal fun ScanIllustration(modifier: Modifier = Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Box(
            Modifier.fillMaxWidth(0.52f).fillMaxHeight(0.72f)
                .background(AiFinderPalette.Surface, RoundedCornerShape(22.dp))
                .border(2.dp, AiFinderPalette.Teal, RoundedCornerShape(22.dp)),
            contentAlignment = Alignment.Center
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(AiFinderSpacing.dp10)) {
                ItemTile(Icons.Outlined.Key)
                ItemTile(Icons.Outlined.Cable)
                ItemTile(Icons.Outlined.Headphones)
            }
            Box(
                Modifier.align(Alignment.BottomCenter).offset(y = AiFinderSpacing.dp20).size(40.dp)
                    .background(AiFinderPalette.Teal, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.CameraAlt, null, tint = AiFinderPalette.Surface, modifier = Modifier.size(20.dp))
            }
        }
        TagChip(
            stringResource(Res.string.login_illustration_tag_keys),
            Modifier.align(Alignment.TopStart).padding(start = AiFinderSpacing.dp12, top = AiFinderSpacing.dp18)
        )
        TagChip(
            stringResource(Res.string.login_illustration_tag_cable),
            Modifier.align(Alignment.CenterEnd).padding(end = AiFinderSpacing.dp8)
        )
        TagChip(
            stringResource(Res.string.login_illustration_tag_headphones),
            Modifier.align(Alignment.BottomStart).padding(start = AiFinderSpacing.dp16, bottom = AiFinderSpacing.dp22)
        )
    }
}

/** Slide 3: a search for an item, answered with the exact place it lives. */
@Composable
internal fun FindIllustration(modifier: Modifier = Modifier) {
    Column(
        modifier.padding(horizontal = AiFinderSpacing.dp20),
        verticalArrangement = Arrangement.spacedBy(AiFinderSpacing.dp12, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            Modifier.fillMaxWidth()
                .background(AiFinderPalette.Surface, RoundedCornerShape(50))
                .border(1.dp, AiFinderPalette.Border, RoundedCornerShape(50))
                .padding(horizontal = AiFinderSpacing.dp16, vertical = AiFinderSpacing.dp10),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.Search, null, tint = AiFinderPalette.Teal, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(AiFinderSpacing.dp10))
            Text(
                stringResource(Res.string.login_illustration_search_query),
                color = AiFinderPalette.Ink,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = AiFinderPalette.Surface,
            border = BorderStroke(1.dp, AiFinderPalette.Border),
            shadowElevation = 2.dp
        ) {
            Row(Modifier.padding(AiFinderSpacing.dp12), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(44.dp).background(AiFinderPalette.Mint, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Inventory2, null, tint = AiFinderPalette.Teal, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(AiFinderSpacing.dp12))
                Column(Modifier.weight(1f)) {
                    Text(
                        stringResource(Res.string.login_illustration_search_result),
                        color = AiFinderPalette.Ink,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(AiFinderSpacing.dp4))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.MyLocation, null, tint = AiFinderPalette.Muted, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(AiFinderSpacing.dp4))
                        Text(
                            stringResource(Res.string.login_illustration_search_path),
                            color = AiFinderPalette.Muted,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Icon(Icons.Rounded.CheckCircle, null, tint = AiFinderPalette.Teal, modifier = Modifier.size(22.dp))
            }
        }
    }
}

@Composable
internal fun PlaceIllustration(modifier: Modifier = Modifier) {
    Row(
        modifier.padding(horizontal = AiFinderSpacing.dp24),
        horizontalArrangement = Arrangement.spacedBy(AiFinderSpacing.dp16),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            Modifier.background(AiFinderPalette.Surface, RoundedCornerShape(18.dp))
                .padding(horizontal = AiFinderSpacing.dp12, vertical = AiFinderSpacing.dp10),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Outlined.QrCode2, null, tint = AiFinderPalette.Teal, modifier = Modifier.size(52.dp))
            Text(
                stringResource(Res.string.login_illustration_place_label),
                color = AiFinderPalette.Muted,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Column(
            Modifier.weight(1f).background(AiFinderPalette.Surface, RoundedCornerShape(18.dp))
                .padding(AiFinderSpacing.dp14)
        ) {
            Icon(Icons.Outlined.Inventory2, null, tint = AiFinderPalette.Teal, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(AiFinderSpacing.dp8))
            Text(
                stringResource(Res.string.login_illustration_place_path),
                color = AiFinderPalette.Ink,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ItemTile(icon: ImageVector) {
    Box(
        Modifier.size(38.dp).background(AiFinderPalette.Mint, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = AiFinderPalette.Teal, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun TagChip(label: String, modifier: Modifier = Modifier) {
    Row(
        modifier.background(AiFinderPalette.Surface, RoundedCornerShape(50))
            .border(1.dp, AiFinderPalette.Border, RoundedCornerShape(50))
            .padding(horizontal = AiFinderSpacing.dp10, vertical = AiFinderSpacing.dp6),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Outlined.AutoAwesome, null, tint = AiFinderPalette.Teal, modifier = Modifier.size(12.dp))
        Spacer(Modifier.width(AiFinderSpacing.dp4))
        Text(label, color = AiFinderPalette.Ink, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}
