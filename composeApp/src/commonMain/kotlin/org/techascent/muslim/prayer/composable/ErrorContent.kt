package org.techascent.muslim.prayer.composable

import apphub.composeapp.generated.resources.Res
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import apphub.composeapp.generated.resources.message_error
import apphub.composeapp.generated.resources.text_try_again
import apphub.composeapp.generated.resources.title_error
import org.jetbrains.compose.resources.stringResource
import org.techascent.composa.card.ComposaCardFrame
import org.techascent.composa.common.ComposaSpacing
import org.techascent.composa.theming.ComposaTheme
import org.techascent.muslim.common.ErrorPoster
import org.techascent.muslim.prayer.tags.PrayerTags

fun LazyListScope.errorContent(
    title: String? = null,
    description: String? = null,
    buttonText: String? = null,
    onRetry: (() -> Unit)? = null
) {
    item {
        ErrorCard(
            title = title,
            description = description,
            buttonText = buttonText,
            onRetry = onRetry
        )

    }
}

@Composable
internal fun ErrorCard(
    title: String? = null,
    description: String? = null,
    buttonText: String? = null,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    ComposaCardFrame(
        modifier = modifier
            .fillMaxWidth()
            .padding(ComposaSpacing.Medium).testTag(PrayerTags.PRAYER_TIME_LOADING_ERROR),
        borderColor = ComposaTheme.color.strokeNeutralSubtle,
        content = {
            ErrorPoster(
                title = title ?: stringResource(resource = Res.string.title_error),
                description = description ?: stringResource(resource = Res.string.message_error),
                buttonText = buttonText ?: stringResource(resource = Res.string.text_try_again),
                onRetry = onRetry
            )
        }
    )
}