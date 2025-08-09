package org.techascent.composa.cell.center

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import org.techascent.composa.common.ComposaSpacing
import org.techascent.composa.theming.ComposaTheme

sealed interface CenterSlot {
    data class Title(val title: String) :
        CenterSlot

    data class TitleWithLabel(val title: String, val label: String) :
        CenterSlot

    data class LabelWithTitle(val label: String, val title: String) :
        CenterSlot
}

@Composable
internal fun RowScope.CenterSlot(
    centerSlot: CenterSlot,
    modifier: Modifier
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .align(CenterVertically)
            .padding(end = ComposaSpacing.Medium),
    ) {
        when (centerSlot) {
            is CenterSlot.Title -> CellTitle(
                modifier = modifier,
                centerSlot = centerSlot,

                )

            is CenterSlot.TitleWithLabel -> CellTitleWithLabel(
                modifier = modifier,
                centerSlot = centerSlot,
            )

            is CenterSlot.LabelWithTitle -> CellLabelWithTitle(
                modifier = modifier,
                centerSlot = centerSlot
            )
        }
    }


}

@Composable
internal fun CellTitle(centerSlot: CenterSlot.Title, modifier: Modifier = Modifier) {
    Text(
        modifier = modifier,
        text = centerSlot.title,
        style = ComposaTheme.typography.title,
        color = ComposaTheme.color.textNeutral
    )
}

@Composable
internal fun CellTitleWithLabel(
    centerSlot: CenterSlot.TitleWithLabel,
    modifier: Modifier = Modifier,
) {
    Text(
        modifier = modifier,
        text = centerSlot.title,
        style = ComposaTheme.typography.title,
        color = ComposaTheme.color.textNeutral
    )
    Text(
        modifier = modifier
            .padding(top = ComposaSpacing.ExtraSmall),
        text = centerSlot.label,
        style = ComposaTheme.typography.footnote,
        color = ComposaTheme.color.textNeutralAlternative
    )
}

@Composable
internal fun CellLabelWithTitle(
    centerSlot: CenterSlot.LabelWithTitle,
    modifier: Modifier = Modifier,
) {
    Text(
        modifier = modifier,
        text = centerSlot.label,
        style = ComposaTheme.typography.footnote,
        color = ComposaTheme.color.textNeutralAlternative
    )

    Text(
        modifier = modifier.padding(top = ComposaSpacing.ExtraSmall),
        text = centerSlot.title,
        style = ComposaTheme.typography.title,
        color = ComposaTheme.color.textNeutral
    )

}
