package org.techascent.aifinder.presentation.account

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import apphub.aifinder.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.techascent.aifinder.presentation.components.*
import org.techascent.aifinder.presentation.theme.AiFinderPalette

@Composable
fun FeedbackScreen(onBack: () -> Unit, viewModel: FeedbackViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsState()
    ScreenLayout(topBar = { ScreenTopBar(stringResource(Res.string.feedback_title), onBack) }) {
        item {
            InfoCard(
                stringResource(if (state.sent) Res.string.feedback_thanks else Res.string.feedback_intro),
                stringResource(if (state.sent) Res.string.feedback_sent_hint else Res.string.feedback_intro_hint),
                subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant,
                leadingContent = {
                    Icon(if (state.sent) Icons.Outlined.CheckCircle else Icons.Outlined.ChatBubbleOutline,
                        null, tint = AiFinderPalette.Teal)
                },
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            )
        }
        if (state.sent) {
            item { PrimaryAction(stringResource(Res.string.feedback_done), onBack) }
        } else {
            item {
                OutlinedTextField(
                    value = state.text,
                    onValueChange = viewModel::edit,
                    label = { Text(stringResource(Res.string.feedback_label)) },
                    placeholder = { Text(stringResource(Res.string.feedback_placeholder)) },
                    enabled = !state.busy,
                    minLines = 6,
                    maxLines = 12,
                    isError = state.text.length > FEEDBACK_MAX_LENGTH,
                    supportingText = {
                        Text(stringResource(Res.string.feedback_length, state.text.length, FEEDBACK_MAX_LENGTH))
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                Text(stringResource(Res.string.feedback_private),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall)
            }
            if (state.failed) item {
                Text(stringResource(Res.string.feedback_error), color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
            }
            item {
                PrimaryAction(
                    stringResource(if (state.failed) Res.string.error_card_retry else Res.string.feedback_send),
                    viewModel::send, enabled = state.canSend, isLoading = state.busy,
                    modifier = Modifier.imePadding(),
                )
            }
        }
    }
}
