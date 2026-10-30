package org.techascent.aifinder.presentation.review

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import apphub.aifinder.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.techascent.aifinder.presentation.components.*
import org.techascent.aifinder.presentation.theme.AiFinderPalette

@Composable
fun ReviewScreen(
    state: ReviewUiState,
    onSave: () -> Unit,
    onBack: () -> Unit,
    onRetry: () -> Unit
) {
    ScreenLayout(
        topBar = {
            Surface(color = AiFinderPalette.Background) {
                Row(
                    modifier = Modifier.fillMaxWidth().statusBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 4.dp)
                ) {
                    BackButton(onBack)
                }
            }
        },
        bottomBar = {
            if (state is ReviewUiState.Success) {
                Box(
                    Modifier.fillMaxWidth().navigationBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 24.dp)
                ) {
                    PrimaryAction(
                        stringResource(Res.string.review_save_items, state.data.detections.size),
                        onSave
                    )
                }
            }
        }
    ) {
        item(key = "header") { SectionTitle(stringResource(Res.string.review_title)) }
        when (state) {
            is ReviewUiState.Loading -> item { LoadingContent() }
            is ReviewUiState.Error -> item { ErrorContent(state.message, onRetry) }
            is ReviewUiState.Success -> {
                item(key = "summary") {
                    InfoCard(
                        title = stringResource(Res.string.review_items_found, state.data.detections.size),
                        subtitle = stringResource(Res.string.review_check_items),
                        containerColor = AiFinderPalette.Mint,
                        titleColor = AiFinderPalette.Teal,
                        titleFontSize = 18.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
                items(state.data.detections, key = { it.item.id }) { detection ->
                    DetectionCard(detection)
                }
            }
        }
    }
}
