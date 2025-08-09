package org.techascent.muslim.prayer.composable

import apphub.composeapp.generated.resources.Res
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import apphub.composeapp.generated.resources.message_error
import apphub.composeapp.generated.resources.title_error
import org.jetbrains.compose.resources.stringResource
import org.techascent.composa.card.ComposaCardFrame
import org.techascent.composa.common.ComposaSpacing
import org.techascent.composa.theming.ComposaTheme
import org.techascent.muslim.common.ErrorPoster
import org.techascent.muslim.prayer.tags.PrayerTags

fun LazyListScope.errorContent(
    onRetry: () -> Unit = {}
) {
    item {

        ComposaCardFrame(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ComposaSpacing.Medium).testTag(PrayerTags.PRAYER_TIME_LOADING_ERROR),
            borderColor = ComposaTheme.color.strokeNeutralSubtle,
            content = {
                ErrorPoster(
                    title = stringResource(resource = Res.string.title_error),
                    description = stringResource(resource = Res.string.message_error),
                    onRetry = onRetry
                )
            }
        )
    }
}