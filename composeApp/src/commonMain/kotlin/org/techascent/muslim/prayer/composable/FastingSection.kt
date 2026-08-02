package org.techascent.muslim.prayer.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import apphub.composeapp.generated.resources.Res
import apphub.composeapp.generated.resources.text_iftar
import apphub.composeapp.generated.resources.text_prayer_fasting
import apphub.composeapp.generated.resources.text_suhur
import org.jetbrains.compose.resources.stringResource
import org.techascent.composa.cell.Cell
import org.techascent.composa.cell.center.CenterSlot
import org.techascent.composa.cell.left.LeftSlot
import org.techascent.composa.cell.right.RightSlot
import org.techascent.composa.common.ComposaSpacing
import org.techascent.composa.theming.ComposaTheme
import org.techascent.muslim.common.localizeTime

@Composable
internal fun FastingSection(sahri: String?, iftar: String?) {
    // TODO: add fastingCardAccent to ComposaTheme.color.prayer for full theme support
    val sahriAccent = ComposaTheme.color.prayer.fastingAccent
    val iftarAccent = Color(0xFFE65100) // deep orange — move to theme token

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ComposaSpacing.Medium)
            .clip(RoundedCornerShape(20.dp))
            .background(sahriAccent.copy(alpha = 0.06f)),
    ) {
        Cell(
            leftSlot = LeftSlot.Emoji(
                emoji = "🌙",
                accentColor = sahriAccent.copy(alpha = 0.12f),
                size = 34.dp,
                fontSize = 18,
            ),
            centerSlot = CenterSlot.Title(title = stringResource(Res.string.text_prayer_fasting)),
            rightSlot = RightSlot.None,
            backgroundColor = Color.Transparent,
        )

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = ComposaSpacing.Medium),
            thickness = 0.5.dp,
            color = sahriAccent.copy(alpha = 0.10f),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ComposaSpacing.Medium),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            sahri?.let {
                FastingTimeChip(
                    emoji = "🍽️",
                    label = stringResource(Res.string.text_suhur),
                    value = it,
                    accentColor = sahriAccent,
                )
            }
            iftar?.let {
                FastingTimeChip(
                    emoji = "🌅",
                    label = stringResource(Res.string.text_iftar),
                    value = it,
                    accentColor = iftarAccent,
                )
            }
        }
    }
}

@Composable
private fun FastingTimeChip(
    emoji: String,
    label: String,
    value: String,
    accentColor: Color,
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(accentColor.copy(alpha = 0.08f))
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(emoji, fontSize = 22.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            style = ComposaTheme.typography.caption,
            color = ComposaTheme.color.textNeutralSubtle,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = value.localizeTime(),
            style = ComposaTheme.typography.titleDemi,
            color = ComposaTheme.color.textNeutral,
        )
    }
}

