package org.techascent.muslim.prayer.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import apphub.composeapp.generated.resources.Res
import apphub.composeapp.generated.resources.message_error
import apphub.composeapp.generated.resources.text_try_again
import apphub.composeapp.generated.resources.title_error
import org.jetbrains.compose.resources.stringResource
import org.techascent.composa.common.ComposaSpacing
import org.techascent.composa.theming.ComposaTheme
import org.techascent.muslim.prayer.tags.PrayerTags

// ═══════════════════════════════════════════════════════════════════════
//  LazyList variant
// ═══════════════════════════════════════════════════════════════════════

fun LazyListScope.errorContent(
    title: String? = null,
    description: String? = null,
    buttonText: String? = null,
    onRetry: (() -> Unit)? = null,
) {
    item {
        ErrorCard(
            title = title,
            description = description,
            buttonText = buttonText,
            onRetry = onRetry,
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  Full-screen variant (for Scaffold / Box contexts)
// ═══════════════════════════════════════════════════════════════════════

@Composable
fun ErrorScreen(
    title: String? = null,
    description: String? = null,
    buttonText: String? = null,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ComposaTheme.color.backgroundAppBackground),
        contentAlignment = Alignment.Center,
    ) {
        ErrorCard(
            title = title,
            description = description,
            buttonText = buttonText,
            onRetry = onRetry,
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  Shared error card
// ═══════════════════════════════════════════════════════════════════════

@Composable
internal fun ErrorCard(
    title: String? = null,
    description: String? = null,
    buttonText: String? = null,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val accent = ComposaTheme.color.backgroundErrorBold
    val subtleBg = ComposaTheme.color.backgroundErrorSubtle
    val cardBg = ComposaTheme.color.prayer.cardBg

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(ComposaSpacing.Medium)
            .clip(RoundedCornerShape(20.dp))
            .background(cardBg)
            .padding(ComposaSpacing.Large)
            .testTag(PrayerTags.PRAYER_TIME_LOADING_ERROR),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Emoji in a soft circle
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(subtleBg),
            contentAlignment = Alignment.Center,
        ) {
            Text("😔", fontSize = 32.sp)
        }

        Spacer(Modifier.height(ComposaSpacing.Medium))

        // Title
        Text(
            text = title ?: stringResource(Res.string.title_error),
            style = ComposaTheme.typography.titleEmphasized,
            color = ComposaTheme.color.textNeutral,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(ComposaSpacing.Small))

        // Description
        Text(
            text = description ?: stringResource(Res.string.message_error),
            style = ComposaTheme.typography.footnote,
            color = ComposaTheme.color.textNeutralSubtle,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = ComposaSpacing.Medium),
        )

        // Retry button
        onRetry?.let {
            Spacer(Modifier.height(ComposaSpacing.Large))

            FilledTonalButton(
                onClick = it,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = accent,
                    contentColor = ComposaTheme.color.textActionInverse,
                ),
            ) {
                Text(
                    text = buttonText ?: stringResource(Res.string.text_try_again),
                    style = ComposaTheme.typography.footnoteEmphasized,
                )
            }
        }
    }
}