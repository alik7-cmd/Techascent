package org.techascent.muslim.settings.composable

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import apphub.composeapp.generated.resources.Res
import apphub.composeapp.generated.resources.ic_ecternal_link
import apphub.composeapp.generated.resources.icon_internal_navigation
import org.jetbrains.compose.resources.stringResource
import org.techascent.composa.card.ComposaCardFrame
import org.techascent.composa.cell.Cell
import org.techascent.composa.cell.center.CenterSlot
import org.techascent.composa.cell.left.LeftSlot
import org.techascent.composa.cell.right.RightSlot
import org.techascent.composa.common.ComposaSpacing
import org.techascent.composa.common.DrawableData
import org.techascent.composa.theming.ComposaTheme
import org.techascent.muslim.settings.state.AboutUsUiModel
import org.techascent.muslim.settings.state.AppearanceUiModel
import org.techascent.muslim.settings.state.NavigationType
import org.techascent.muslim.settings.state.SettingsItem

internal fun LazyListScope.appearanceContent(
    appearanceUiModel: AppearanceUiModel
) {
    item {
        Header(text = stringResource(resource = appearanceUiModel.title))
        ComposaCardFrame(
            modifier = Modifier.padding(horizontal = ComposaSpacing.Medium),
            borderColor = ComposaTheme.color.strokeNeutralSubtle,
            content = {
                appearanceUiModel.listOfElements.forEach { item ->
                    SettingsCell(item)
                }
            }
        )
    }
}

internal fun LazyListScope.ratingContent(
    aboutUsUiModel: AboutUsUiModel
) {
    item {
        Header(text = stringResource(resource = aboutUsUiModel.title))
        ComposaCardFrame(
            modifier = Modifier.padding(horizontal = ComposaSpacing.Medium),
            borderColor = ComposaTheme.color.strokeNeutralSubtle,
            content = {
                aboutUsUiModel.listOfElements.forEach { item ->
                    SettingsCell(item)
                }
            }
        )
    }
}

@Composable
private fun SettingsCell(
    item: SettingsItem
) {
    Cell(
        leftSlot = LeftSlot.None,
        centerSlot = item.subtitle?.let { subTitleRes ->
            CenterSlot.TitleWithLabel(
                title = stringResource(item.title),
                label = stringResource(subTitleRes)
            )
        } ?: run {
            CenterSlot.Title(
                title = stringResource(item.title)
            )
        },
        rightSlot = RightSlot.Icon(
            data = DrawableData(
                imageRes = if (item.navigationType == NavigationType.INTERNAL)
                    Res.drawable.icon_internal_navigation
                else Res.drawable.ic_ecternal_link,
                tint = ComposaTheme.color.textNeutral
            )
        )
    )

}

internal fun LazyListScope.spacer() {
    item {
        Spacer(modifier = Modifier.size(ComposaSpacing.Medium))
    }
}

@Composable
private fun Header(
    modifier: Modifier = Modifier,
    text: String
) {
    Text(
        modifier = modifier.padding(horizontal = ComposaSpacing.Medium)
            .padding(bottom = ComposaSpacing.Small),
        text = text,
        style = ComposaTheme.typography.titleEmphasized,
        color = ComposaTheme.color.textNeutral,
        fontSize = 15.sp
    )


}