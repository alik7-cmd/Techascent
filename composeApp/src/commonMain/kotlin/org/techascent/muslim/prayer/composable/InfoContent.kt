package org.techascent.muslim.prayer.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import apphub.composeapp.generated.resources.Res
import apphub.composeapp.generated.resources.img_asr
import apphub.composeapp.generated.resources.img_dhuhr
import apphub.composeapp.generated.resources.img_isha
import apphub.composeapp.generated.resources.img_maghrib
import apphub.composeapp.generated.resources.text_asr
import apphub.composeapp.generated.resources.text_dhuhr
import apphub.composeapp.generated.resources.text_fajr
import apphub.composeapp.generated.resources.text_isha
import apphub.composeapp.generated.resources.text_maghrib
import apphub.composeapp.generated.resources.text_sunrise
import apphub.composeapp.generated.resources.text_sunset
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.techascent.composa.card.ComposaCardFrame
import org.techascent.composa.common.ComposaSpacing
import org.techascent.composa.theming.ComposaTheme
import org.techascent.muslim.common.IconWithText
import org.techascent.muslim.common.getImageAspectRatioForWindowSize
import org.techascent.muslim.prayer.tags.PrayerTags
import org.techascent.muslim.prayer.uimodel.PrayerTimeUiModel

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
internal fun LazyListScope.currentSalatContent(
    uiModel: PrayerTimeUiModel
) {
    val currentPrayer = uiModel.currentPrayer
    item {
        val windowSizeClass = calculateWindowSizeClass()
        val widthSizeClass = windowSizeClass.widthSizeClass
        ComposaCardFrame(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ComposaSpacing.Medium)
                .testTag(PrayerTags.PRAYER_TIME_CURRENT_SALAT_CONTENT),
            content = {
                Box {
                    Image(
                        painter = painterResource(
                            getSalatImage(currentPrayer?.name)
                        ),
                        contentDescription = null,
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(
                                getImageAspectRatioForWindowSize(
                                    widthSizeClass = widthSizeClass
                                )
                            )
                    )

                    Row(modifier = Modifier.align(Alignment.BottomCenter)) {
                        Column(
                            modifier = Modifier
                                .padding(ComposaSpacing.Medium)
                                .weight(1f),
                            verticalArrangement = spacedBy(ComposaSpacing.ExtraSmall)
                        ) {

                            currentPrayer?.name?.let {
                                Text(
                                    text = stringResource(resource = it),
                                    style = ComposaTheme.typography.titleLarge,
                                    color = ComposaTheme.color.textNeutralOnDark //ComposaTheme.color.strokeNeutralSubtle
                                )
                            }

                            IconWithText(
                                text = uiModel.placeName,
                                drawableData = null,
                                textStyle = ComposaTheme.typography.footnote,
                                textColor = ComposaTheme.color.textNeutralOnDark
                            )
                            Text(
                                text = uiModel.currentDateTime,
                                style = ComposaTheme.typography.footnote,
                                color = ComposaTheme.color.textNeutralOnDark
                            )
                        }

                        Column(
                            modifier = Modifier
                                .padding(top = ComposaSpacing.Medium, end = ComposaSpacing.Medium)
                                .align(Alignment.CenterVertically),
                            verticalArrangement = spacedBy(ComposaSpacing.ExtraSmall)
                        ) {
                            IconWithText(
                                text = "${stringResource(resource = Res.string.text_sunrise)}: ${uiModel.sunrise}",
                                drawableData = null,
                                textStyle = ComposaTheme.typography.footnote,
                                textColor = ComposaTheme.color.textNeutralOnDark
                            )

                            IconWithText(
                                text = "${stringResource(resource = Res.string.text_sunset)}: ${uiModel.sunset}",
                                drawableData = null,
                                textStyle = ComposaTheme.typography.footnote,
                                textColor = ComposaTheme.color.textNeutralOnDark
                            )
                        }
                    }

                }
            }
        )
    }
}

private fun getSalatImage(salatName: StringResource?): DrawableResource {
    return when (salatName) {
        Res.string.text_fajr -> Res.drawable.img_asr
        Res.string.text_dhuhr -> Res.drawable.img_dhuhr
        Res.string.text_asr -> Res.drawable.img_asr
        Res.string.text_maghrib -> Res.drawable.img_maghrib
        Res.string.text_isha -> Res.drawable.img_isha
        else -> Res.drawable.img_asr
    }
}