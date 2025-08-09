package org.techascent.muslim.tasbeeh

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import apphub.composeapp.generated.resources.Res
import org.techascent.composa.common.ComposaSpacing
import org.techascent.composa.common.DrawableData
import org.techascent.composa.icon.ComposaIcon
import org.techascent.composa.theming.ComposaTheme
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import apphub.composeapp.generated.resources.ic_back
import apphub.composeapp.generated.resources.ic_finger
import apphub.composeapp.generated.resources.ic_reset
import apphub.composeapp.generated.resources.text_reset_counter
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.techascent.composa.appbar.TopAppBar
import org.techascent.composa.button.primary.ComposaButton
import org.techascent.composa.button.text.ComposaButtonText
import org.techascent.muslim.performHapticFeedback
import org.techascent.muslim.showNativeResetDialog
import org.techascent.muslim.tasbeeh.state.DialogProperty
import org.techascent.muslim.tasbeeh.state.TasbeehUiState

@Composable
internal fun TasbeehView(
    onNavigateBack: () -> Unit
) {
    ComposaTheme {
        TasbeehScreen(
            onNavigateBack = onNavigateBack,
        )
    }
}

@OptIn(KoinExperimentalAPI::class)
@Composable
private fun TasbeehScreen(
    viewModel: TasbeehViewModel = koinViewModel<TasbeehViewModel>(),
    onNavigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    TasbeehContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onToggleDialogVisibility = viewModel::onUpdateDialogVisibility,
        onProceedClick = viewModel::onProceedClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TasbeehContent(
    uiState: TasbeehUiState,
    onNavigateBack: () -> Unit,
    onToggleDialogVisibility: () -> Unit = {},
    onProceedClick: () -> Unit
) {
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = Modifier
            .fillMaxSize()
            .background(ComposaTheme.color.backgroundAppBackground),
        topBar = {
            TopAppBar(
                title = stringResource(resource = uiState.title),
                navigationIcon = Res.drawable.ic_back,
                onNavigationIconClicked = onNavigateBack
            )
        },
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = innerPadding,
            verticalArrangement = spacedBy(ComposaSpacing.Medium)
        ) {
            content(
                uiState = uiState,
                onToggleDialogVisibility = onToggleDialogVisibility,
                onProceedClick = onProceedClick
            )
        }
    }
}

private fun LazyListScope.content(
    uiState: TasbeehUiState,
    onToggleDialogVisibility: () -> Unit = {},
    onProceedClick: () -> Unit
) {
    item {
        var count by rememberSaveable { mutableStateOf(uiState.count) }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillParentMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = spacedBy(ComposaSpacing.Medium)
            ) {
                // Counter text
                Text(
                    text = count.toString(),
                    style = ComposaTheme.typography.titleLarge
                )

                // Main button
                IconButton(
                    modifier = Modifier.size(100.dp),
                    onClick = {
                        ++count
                        performHapticFeedback()
                    },
                ) {
                    ComposaIcon(
                        icon = DrawableData(
                            imageRes = Res.drawable.ic_finger,
                            tint = Color.Unspecified,
                        )
                    )
                }
                if (uiState.shouldShowResetDialog) {
                    ResetWarningDialog(
                        dialogProperty = uiState.dialogProperty,
                        onDismissRequest = onToggleDialogVisibility,
                        onProceedClick = {
                            onProceedClick
                            count = 0
                        }
                    )
                }

                ComposaButton(
                    text = stringResource(resource = Res.string.text_reset_counter),
                    onClick = onToggleDialogVisibility,
                    iconTint = Color.Unspecified,
                    isSmall = true
                )

                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = ComposaSpacing.Medium),
                    text = stringResource(resource = uiState.infoMessage),
                    style = ComposaTheme.typography.footnote,
                    color = ComposaTheme.color.textNeutral
                )
            }

        }
    }

}

@Composable
fun ResetWarningDialog(
    dialogProperty: DialogProperty,
    onDismissRequest: () -> Unit,
    onProceedClick: () -> Unit
) {
    showNativeResetDialog(
        title = stringResource(resource = dialogProperty.title),
        message = stringResource(resource = dialogProperty.message),
        confirmText = stringResource(resource = dialogProperty.confirmText),
        cancelText = stringResource(resource = dialogProperty.cancelText),
        onConfirm = onProceedClick,
        onCancel = onDismissRequest
    )
}




