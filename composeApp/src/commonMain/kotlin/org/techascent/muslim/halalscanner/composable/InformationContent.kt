package org.techascent.muslim.halalscanner.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import apphub.composeapp.generated.resources.Res
import apphub.composeapp.generated.resources.ic_halal
import apphub.composeapp.generated.resources.text_cancel
import apphub.composeapp.generated.resources.text_scan_again
import org.jetbrains.compose.resources.stringResource
import org.techascent.composa.asyncimage.ComposeAsyncImage
import org.techascent.composa.button.primary.ComposaButton
import org.techascent.composa.common.ComposaSpacing
import org.techascent.composa.common.DrawableData
import org.techascent.composa.icon.ComposaIcon
import org.techascent.composa.messabebox.MessageBox
import org.techascent.composa.messabebox.MessageType
import org.techascent.composa.text.BulletText
import org.techascent.composa.text.DecoratedText
import org.techascent.composa.text.SpannableText
import org.techascent.composa.text.StyledText
import org.techascent.composa.theming.ComposaTheme
import org.techascent.muslim.halalscanner.state.HalalUiState
import org.techascent.muslim.halalscanner.state.ProductUiState
import org.techascent.shared.data.mapper.HalalStatus

@Composable
internal fun InformationContent(
    productUiState: ProductUiState,
    onNavigateBack: () -> Unit,
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(ComposaTheme.color.backgroundAppBackground),
        verticalArrangement = spacedBy(ComposaSpacing.Medium),
        content = {
            productUiState.imageUrl?.let {
                ComposeAsyncImage(
                    modifier = Modifier
                        .padding(horizontal = ComposaSpacing.Medium)
                        .align(Alignment.CenterHorizontally),
                    model = it,
                    contentDescription = productUiState.labels
                )
            }

            Row(
                modifier = Modifier.padding(horizontal = ComposaSpacing.Medium),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = spacedBy(ComposaSpacing.Small)
            ) {
                ComposaIcon(
                    icon = DrawableData(
                        tint = getColorByStatus(productUiState.halalUiState.status),
                        imageRes = Res.drawable.ic_halal
                    )
                )
                Text(
                    text = stringResource(productUiState.halalUiState.halalStatusRes),
                    color = getColorByStatus(productUiState.halalUiState.status),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp
                )
            }

            InfoBox(
                message = stringResource(productUiState.halalUiState.reasonRes),
                messageType = getMessageTypeByStatus(productUiState.halalUiState.status)
            )

            Text(
                modifier = Modifier.padding(horizontal = ComposaSpacing.Medium),
                text = "Ingredients",
                style = ComposaTheme.typography.titleEmphasized,
            )

            productUiState.ingredientsText?.forEach {
                BulletText(
                    text = it,
                    modifier = Modifier.padding(horizontal = ComposaSpacing.Medium)
                )
            }

            Row(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                horizontalArrangement = spacedBy(ComposaSpacing.Medium)

            ) {
                ComposaButton(
                    text = stringResource(Res.string.text_cancel),
                    onClick = onNavigateBack,
                    iconTint = Color.Unspecified,
                )
                ComposaButton(
                    text = stringResource(Res.string.text_scan_again),
                    onClick = {},
                    iconTint = Color.Unspecified,
                )
            }
        }
    )
}

@Composable
private fun StatusText(
    halalUiState: HalalUiState,
    modifier: Modifier
) {
    val color = getColorByStatus(status = halalUiState.status)
    val segments = listOf(
        StyledText(
            "Product Status: ", order = 0,
            textStyle = ComposaTheme.typography.body
        ),
        DecoratedText(
            text = stringResource(halalUiState.halalStatusRes),
            order = 1,
            spanStyle = SpanStyle(
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        ),
    )
    SpannableText(segments = segments, fontSize = 16.sp, modifier = modifier)
}

@Composable
private fun InfoBox(
    message: String,
    messageType: MessageType
) {
    MessageBox(
        modifier = Modifier.padding(horizontal = ComposaSpacing.Medium),
        messageType = messageType,
        message = message
    )
}

@Composable
private fun getColorByStatus(
    status: HalalStatus
): Color {
    return when (status) {
        HalalStatus.HALAL_CERTIFIED, HalalStatus.HALAL_POSSIBLE -> ComposaTheme.color.textAction
        HalalStatus.NOT_HALAL, HalalStatus.UNKNOWN -> ComposaTheme.color.strokeError
        HalalStatus.HALAL_DOUBTFUL -> ComposaTheme.color.strokeWarning
    }
}

private fun getMessageTypeByStatus(
    status: HalalStatus
): MessageType {
    return when (status) {
        HalalStatus.HALAL_CERTIFIED, HalalStatus.HALAL_POSSIBLE -> MessageType.Info
        HalalStatus.NOT_HALAL, HalalStatus.UNKNOWN -> MessageType.Error
        HalalStatus.HALAL_DOUBTFUL -> MessageType.Warning
    }

}
