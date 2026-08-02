package org.techascent.muslim.prayer.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import apphub.composeapp.generated.resources.Res
import apphub.composeapp.generated.resources.text_prayer_announcement
import apphub.composeapp.generated.resources.text_prayer_data_announcement
import org.jetbrains.compose.resources.stringResource
import org.techascent.composa.cell.Cell
import org.techascent.composa.cell.center.CenterSlot
import org.techascent.composa.cell.left.LeftSlot
import org.techascent.composa.cell.right.RightSlot
import org.techascent.composa.common.ComposaSpacing
import org.techascent.composa.theming.ComposaTheme

@Composable
internal fun AnnouncementSection() {
    SectionCard(
        emoji = "📢",
        title = stringResource(Res.string.text_prayer_announcement),
        accentColor = ComposaTheme.color.prayer.announcementAccent,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ComposaSpacing.Medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("📌", fontSize = 18.sp)
            Spacer(Modifier.width(12.dp))
            Text(
                text = stringResource(Res.string.text_prayer_data_announcement),
                style = ComposaTheme.typography.footnote,
                color = ComposaTheme.color.textNeutralSubtle,
                textAlign = TextAlign.Start,
            )
        }
    }
}

// ─── Reusable section card shell — used by AnnouncementSection & FastingSection ──

@Composable
internal fun SectionCard(
    emoji: String,
    title: String,
    accentColor: Color,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ComposaSpacing.Medium)
            .clip(RoundedCornerShape(20.dp))
            .background(ComposaTheme.color.prayer.cardBg),
    ) {
        Cell(
            leftSlot = LeftSlot.Emoji(
                emoji = emoji,
                accentColor = accentColor.copy(alpha = 0.12f),
                size = 34.dp,
                fontSize = 18,
            ),
            centerSlot = CenterSlot.Title(title = title),
            rightSlot = RightSlot.None,
            backgroundColor = Color.Transparent,
        )
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = ComposaSpacing.Medium),
            thickness = 0.5.dp,
            color = ComposaTheme.color.strokeNeutralSubtle.copy(alpha = 0.4f),
        )
        content()
    }
}

