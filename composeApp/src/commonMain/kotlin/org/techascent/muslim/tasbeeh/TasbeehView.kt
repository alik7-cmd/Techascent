package org.techascent.muslim.tasbeeh

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import apphub.composeapp.generated.resources.Res
import org.techascent.composa.common.ComposaSpacing
import org.techascent.composa.theming.ComposaTheme
import androidx.compose.runtime.getValue
import apphub.composeapp.generated.resources.ic_back
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.techascent.composa.appbar.TopAppBar
import org.techascent.composa.appbar.TrailingAction
import org.techascent.muslim.showNativeResetDialog
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
        onCounterIncrement = viewModel::onCounterIncrement,
        onResetIncrement = viewModel::onResetIncrement
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TasbeehContent(
    uiState: TasbeehUiState,
    onNavigateBack: () -> Unit,
    onCounterIncrement: () -> Unit,
    onResetIncrement: () -> Unit
) {
    val title = stringResource(resource = uiState.dialogProperty.title)
    val message = stringResource(resource = uiState.dialogProperty.message)
    val confirmText = stringResource(resource = uiState.dialogProperty.confirmText)
    val cancelText = stringResource(resource = uiState.dialogProperty.cancelText)
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = Modifier
            .fillMaxSize()
            .background(ComposaTheme.color.backgroundAppBackground),
        topBar = {
            TopAppBar(
                title = stringResource(resource = uiState.title),
                navigationIcon = Res.drawable.ic_back,
                onNavigationIconClicked = onNavigateBack,
                action = TrailingAction.TextButton(
                    text = "Reset",
                    onClick = {
                        ResetWarningDialog(
                            title = title,
                            message = message,
                            confirmText = confirmText,
                            cancelText = cancelText,
                            onDismissRequest = {},
                            onProceedClick = {
                                onResetIncrement()
                            }
                        )
                    }
                )
            )
        },
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = innerPadding,
            verticalArrangement = spacedBy(ComposaSpacing.Medium)
        ) {
            parabolicTasbeeh(
                uiState = uiState,
                onCounterIncrement = onCounterIncrement
            )
        }
    }
}

fun ResetWarningDialog(
    title: String,
    message: String,
    confirmText: String,
    cancelText: String,
    onDismissRequest: () -> Unit,
    onProceedClick: () -> Unit
) {
    showNativeResetDialog(
        title = title,
        message = message,
        confirmText = confirmText,
        cancelText = cancelText,
        onConfirm = onProceedClick,
        onCancel = onDismissRequest
    )
}




