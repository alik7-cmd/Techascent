package org.techascent.composa.cell.right

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import org.techascent.composa.checkbox.ComposaCheckbox
import org.techascent.composa.common.ComposaSpacing
import org.techascent.composa.common.DrawableData
import org.techascent.composa.icon.ComposaIcon
import org.techascent.composa.switch.ComposaSwitch
import org.techascent.composa.theming.ComposaTheme

sealed interface RightSlot {
    val weight: Float?
        get() = null

    data object None : RightSlot

    data class Switch(
        val checked: Boolean,
        val onCheckedChange: ((Boolean) -> Unit)?,
        val enabled: Boolean = true,
        val iconRes: DrawableData? = null,
        val showIconWhenChecked: Boolean = true
    ) : RightSlot

    data class Icon(val data: DrawableData) : RightSlot

    data class Checkbox(
        val checked: Boolean,
        val labelText: String? = null,
        val onCheckedChange: ((Boolean) -> Unit),
    ) : RightSlot

    data class TitleWithLabel(val title: String, val label: String) : RightSlot

    data class TextEmphasized(
        val text: String
    ) : RightSlot
}

@Composable
internal fun RowScope.RightSlot(rightSlot: RightSlot, modifier: Modifier = Modifier) {
    when (rightSlot) {
        is RightSlot.Switch -> Switch(
            rightSlot = rightSlot,
            modifier = modifier,
        )

        is RightSlot.Icon -> ComposaIcon(modifier = modifier, icon = rightSlot.data)
        is RightSlot.None -> {}
        is RightSlot.Checkbox -> Checkbox(
            modifier = modifier,
            rightSlot = rightSlot
        )

        is RightSlot.TextEmphasized -> CellEmphasizedText(
            rightSlot = rightSlot,
            modifier = modifier
        )

        is RightSlot.TitleWithLabel -> CellTitleWithLabel(
            rightSlot = rightSlot,
            modifier = modifier
        )
    }
}

@Composable
internal fun RowScope.Switch(
    rightSlot: RightSlot.Switch,
    modifier: Modifier = Modifier
) {
    ComposaSwitch(
        modifier = modifier,
        checked = rightSlot.checked,
        enabled = rightSlot.enabled,
        onCheckedChange = rightSlot.onCheckedChange,
        showIconWhenChecked = rightSlot.showIconWhenChecked,
        iconRes = rightSlot.iconRes
    )
}

@Composable
private fun RowScope.Checkbox(
    rightSlot: RightSlot.Checkbox,
    modifier: Modifier = Modifier
) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ComposaCheckbox(
            modifier = modifier,
            checked = rightSlot.checked,
            onCheckedChange = rightSlot.onCheckedChange
        )
        rightSlot.labelText?.let {
            Text(
                text = it,
                style = ComposaTheme.typography.caption,
                color = ComposaTheme.color.textNeutral
            )
        }

    }
}

@Composable
internal fun CellTitleWithLabel(
    rightSlot: RightSlot.TitleWithLabel,
    modifier: Modifier = Modifier,
) {
    Text(
        modifier = modifier,
        text = rightSlot.title,
        style = ComposaTheme.typography.title,
        color = ComposaTheme.color.textNeutral
    )
    Text(
        modifier = modifier
            .padding(top = ComposaSpacing.ExtraSmall),
        text = rightSlot.label,
        style = ComposaTheme.typography.footnote,
        color = ComposaTheme.color.textNeutralAlternative
    )
}

@Composable
internal fun CellEmphasizedText(
    rightSlot: RightSlot.TextEmphasized,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(ComposaTheme.shape.small)
            .background(ComposaTheme.color.backgroundInfoBold),
    ) {
        Text(
            modifier = Modifier.padding(
                vertical = ComposaSpacing.ExtraSmall,
                horizontal = ComposaSpacing.Small
            ),
            text = rightSlot.text,
            style = ComposaTheme.typography.subheadEmphasized,
            color = ComposaTheme.color.textNeutralSolid
        )

    }
}
