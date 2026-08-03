package org.techascent.muslim.prayer.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.techascent.composa.cell.Cell
import org.techascent.composa.cell.center.CenterSlot
import org.techascent.composa.cell.left.LeftSlot
import org.techascent.composa.cell.right.RightSlot
import org.techascent.composa.common.ComposaSpacing
import org.techascent.composa.featurecard.FeatureCardCompact
import org.techascent.composa.theming.ComposaTheme
import org.techascent.muslim.utility.FeatureId
import apphub.composeapp.generated.resources.Res
import apphub.composeapp.generated.resources.text_frequently_used

/**
 * Prayer-screen section that surfaces the user's **top 3 most-used Explore features**
 * as compact shortcut cards directly below the prayer times list.
 *
 * The card is styled identically to [PrayerTimesSection] and [FastingSection]:
 * `padding(horizontal) → clip(RoundedCornerShape(20.dp)) → background(cardBg)` with a
 * [Cell] header and [HorizontalDivider] separating the header from the cards grid.
 *
 * Returns immediately (renders nothing) when [features] is empty — no UI on fresh install.
 */
@Composable
internal fun QuickAccessSection(
    features: List<FeatureId>,
    onNavigate: (FeatureId) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (features.isEmpty()) return

    val sahriAccent = ComposaTheme.color.prayer.quickAccessAccent
    val sectionAccent = ComposaTheme.color.prayer.quickAccessAccent

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = ComposaSpacing.Medium)
            .clip(RoundedCornerShape(20.dp))
            .background(ComposaTheme.color.prayer.cardBg),
    ) {
        // ── Header ──────────────────────────────────────────────────────────────
        Cell(
            leftSlot = LeftSlot.Emoji(
                emoji = "⚡",
                accentColor = sectionAccent.copy(alpha = 0.12f),
                size = 34.dp,
                fontSize = 18,
            ),
            centerSlot = CenterSlot.Title(title = stringResource(Res.string.text_frequently_used)),
            rightSlot = RightSlot.None,
            backgroundColor = Color.Transparent,
        )

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = ComposaSpacing.Medium),
            thickness = 0.5.dp,
            color = ComposaTheme.color.strokeNeutralSubtle.copy(alpha = 0.4f),
        )

        // ── Feature Cards ────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ComposaSpacing.Medium),
            horizontalArrangement = Arrangement.spacedBy(ComposaSpacing.Small),
        ) {
            features.forEach { feature ->
                Box(modifier = Modifier.weight(1f)) {
                    FeatureCardCompact(
                        emoji = feature.emoji,
                        title = stringResource(feature.titleRes),
                        accentColor = feature.accentColor,
                        onClick = { onNavigate(feature) },
                    )
                }
            }

            // Fill remaining slots so cards stay equal-width even if < 3 features
            repeat(MAX_FEATURES - features.size) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

private const val MAX_FEATURES = 3
