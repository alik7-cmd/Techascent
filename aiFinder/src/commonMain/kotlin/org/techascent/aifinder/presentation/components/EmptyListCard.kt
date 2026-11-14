package org.techascent.aifinder.presentation.components

import org.techascent.aifinder.presentation.theme.AiFinderSpacing

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import apphub.aifinder.generated.resources.*
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.techascent.aifinder.presentation.theme.AiFinderPalette

@Composable
internal fun EmptyListCard(
    modifier: Modifier = Modifier,
    titleRes: StringResource = Res.string.home_spaces_empty_title,
    descriptionRes: StringResource = Res.string.home_spaces_empty_message,
    description: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Surface(modifier = modifier.fillMaxWidth(), color = AiFinderPalette.Surface, shape = RoundedCornerShape(28.dp)) {
        EmptyStateBody(titleRes, description ?: stringResource(descriptionRes), actionLabel, onAction)
    }
}

@Composable
private fun EmptyStateBody(
    titleRes: StringResource,
    description: String,
    actionLabel: String?,
    onAction: (() -> Unit)?,
) {
    Column(
        Modifier.fillMaxWidth().padding(horizontal = AiFinderSpacing.dp24, vertical = AiFinderSpacing.dp24),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(Res.drawable.empty_spaces_illustration),
            contentDescription = null,
            modifier = Modifier.widthIn(max = 260.dp).fillMaxWidth().aspectRatio(320f / 220f),
        )
        Spacer(Modifier.height(AiFinderSpacing.dp16))
        Text(
            stringResource(titleRes),
            color = AiFinderPalette.Ink,
            fontSize = 21.sp,
            lineHeight = 28.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(AiFinderSpacing.dp8))
        Text(
            description,
            color = AiFinderPalette.Ink.copy(alpha = 0.72f),
            fontSize = 15.sp,
            lineHeight = 22.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 320.dp),
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(AiFinderSpacing.dp16))
            TextButton(onClick = onAction) {
                Text(actionLabel, color = AiFinderPalette.Teal, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
