package org.techascent.muslim.halalscanner

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import apphub.composeapp.generated.resources.Res
import apphub.composeapp.generated.resources.ic_back
import apphub.composeapp.generated.resources.text_cancel
import apphub.composeapp.generated.resources.text_check_another
import apphub.composeapp.generated.resources.text_check_result
import apphub.composeapp.generated.resources.text_manual_check_barcode_hint
import apphub.composeapp.generated.resources.text_manual_check_barcode_title
import apphub.composeapp.generated.resources.text_manual_check_button
import apphub.composeapp.generated.resources.text_manual_check_ingredients_hint
import apphub.composeapp.generated.resources.text_manual_check_ingredients_title
import apphub.composeapp.generated.resources.text_manual_check_or
import apphub.composeapp.generated.resources.text_manual_check_subtitle
import apphub.composeapp.generated.resources.text_product_not_found
import apphub.composeapp.generated.resources.text_scan_again
import apphub.composeapp.generated.resources.title_manual_halal_check
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.techascent.composa.appbar.TopAppBar
import org.techascent.composa.appbar.TrailingAction
import org.techascent.composa.button.primary.ComposaButton
import org.techascent.composa.common.ComposaSpacing
import org.techascent.composa.theming.ComposaTheme
import org.techascent.muslim.halalscanner.composable.InformationContentV2
import org.techascent.muslim.halalscanner.composable.LoadingContent
import org.techascent.muslim.halalscanner.state.HalalScannerUiState
import org.techascent.muslim.prayer.composable.ErrorCard

private enum class ManualCheckPhase {
    INPUT,
    RESULT,
}

@OptIn(KoinExperimentalAPI::class)
@Composable
internal fun ManualHalalCheckView(
    onNavigateBack: () -> Unit,
) {
    val viewModel = koinViewModel<HalalScannerViewModel>()
    ComposaTheme {
        ManualHalalCheckScreen(
            viewModel = viewModel,
            onNavigateBack = onNavigateBack,
        )
    }
}

@OptIn(KoinExperimentalAPI::class, ExperimentalMaterial3Api::class)
@Composable
private fun ManualHalalCheckScreen(
    viewModel: HalalScannerViewModel,
    onNavigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    var phase by remember { mutableStateOf(ManualCheckPhase.INPUT) }

    AnimatedContent(
        targetState = phase,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "manualCheckPhase",
    ) { currentPhase ->
        when (currentPhase) {
            ManualCheckPhase.INPUT -> {
                ManualInputScreen(
                    onCheckBarcode = { barcode ->
                        phase = ManualCheckPhase.RESULT
                        viewModel.fetchProductByBarcodeManual(barcode)
                    },
                    onCheckIngredients = { ingredients ->
                        phase = ManualCheckPhase.RESULT
                        viewModel.checkIngredients(ingredients)
                    },
                    onNavigateBack = onNavigateBack,
                )
            }

            ManualCheckPhase.RESULT -> {
                ManualResultScreen(
                    uiState = uiState,
                    onNavigateBack = onNavigateBack,
                    onCheckAnother = {
                        viewModel.resetState()
                        phase = ManualCheckPhase.INPUT
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ManualInputScreen(
    onCheckBarcode: (String) -> Unit,
    onCheckIngredients: (String) -> Unit,
    onNavigateBack: () -> Unit,
) {
    var barcodeText by remember { mutableStateOf("") }
    var ingredientsText by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = Modifier
            .fillMaxSize()
            .background(ComposaTheme.color.backgroundAppBackground),
        topBar = {
            TopAppBar(
                title = stringResource(Res.string.title_manual_halal_check),
                navigationIcon = Res.drawable.ic_back,
                onNavigationIconClicked = onNavigateBack,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(ComposaSpacing.Medium),
            verticalArrangement = Arrangement.spacedBy(ComposaSpacing.Medium),
        ) {
            // Subtitle
            Text(
                text = stringResource(Res.string.text_manual_check_subtitle),
                style = ComposaTheme.typography.subhead,
                color = ComposaTheme.color.textNeutralSubtle,
            )

            Spacer(modifier = Modifier.height(ComposaSpacing.Small))

            // ── Barcode Section ──────────────────────────────────────────
            SectionCard(
                emoji = "📦",
                title = stringResource(Res.string.text_manual_check_barcode_title),
            ) {
                OutlinedTextField(
                    value = barcodeText,
                    onValueChange = { barcodeText = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = stringResource(Res.string.text_manual_check_barcode_hint),
                            style = ComposaTheme.typography.body,
                            color = ComposaTheme.color.textNeutralSubtle,
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                            if (barcodeText.isNotBlank()) {
                                onCheckBarcode(barcodeText.trim())
                            }
                        }
                    ),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ComposaTheme.color.textNeutral,
                        unfocusedBorderColor = ComposaTheme.color.strokeNeutralSubtle,
                        focusedTextColor = ComposaTheme.color.textNeutral,
                        unfocusedTextColor = ComposaTheme.color.textNeutral,
                        cursorColor = ComposaTheme.color.textNeutral,
                    ),
                    textStyle = ComposaTheme.typography.body,
                )

                Spacer(modifier = Modifier.height(ComposaSpacing.Small))

                ComposaButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(Res.string.text_manual_check_button),
                    onClick = {
                        keyboardController?.hide()
                        if (barcodeText.isNotBlank()) {
                            onCheckBarcode(barcodeText.trim())
                        }
                    },
                    isEnabled = barcodeText.isNotBlank(),
                    iconTint = Color.Unspecified,
                )
            }

            // ── OR Divider ──────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(ComposaSpacing.Medium),
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = ComposaTheme.color.strokeNeutralSubtle,
                )
                Text(
                    text = stringResource(Res.string.text_manual_check_or),
                    style = ComposaTheme.typography.bodyEmphasized,
                    color = ComposaTheme.color.textNeutralSubtle,
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = ComposaTheme.color.strokeNeutralSubtle,
                )
            }

            // ── Ingredients Section ──────────────────────────────────────
            SectionCard(
                emoji = "🧪",
                title = stringResource(Res.string.text_manual_check_ingredients_title),
            ) {
                OutlinedTextField(
                    value = ingredientsText,
                    onValueChange = { ingredientsText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    placeholder = {
                        Text(
                            text = stringResource(Res.string.text_manual_check_ingredients_hint),
                            style = ComposaTheme.typography.body,
                            color = ComposaTheme.color.textNeutralSubtle,
                        )
                    },
                    maxLines = 6,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                            if (ingredientsText.isNotBlank()) {
                                onCheckIngredients(ingredientsText.trim())
                            }
                        }
                    ),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ComposaTheme.color.textNeutral,
                        unfocusedBorderColor = ComposaTheme.color.strokeNeutralSubtle,
                        focusedTextColor = ComposaTheme.color.textNeutral,
                        unfocusedTextColor = ComposaTheme.color.textNeutral,
                        cursorColor = ComposaTheme.color.textNeutral,
                    ),
                    textStyle = ComposaTheme.typography.body,
                )

                Spacer(modifier = Modifier.height(ComposaSpacing.Small))

                ComposaButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(Res.string.text_manual_check_button),
                    onClick = {
                        keyboardController?.hide()
                        if (ingredientsText.isNotBlank()) {
                            onCheckIngredients(ingredientsText.trim())
                        }
                    },
                    isEnabled = ingredientsText.isNotBlank(),
                    iconTint = Color.Unspecified,
                )
            }
        }
    }
}

@Composable
private fun SectionCard(
    emoji: String,
    title: String,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(ComposaTheme.color.strokeNeutralSubtle.copy(alpha = 0.08f))
            .padding(ComposaSpacing.Medium),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ComposaSpacing.Small),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(ComposaTheme.color.strokeNeutralSubtle.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = emoji, fontSize = 20.sp)
            }
            Text(
                text = title,
                style = ComposaTheme.typography.bodyEmphasized,
                color = ComposaTheme.color.textNeutral,
            )
        }

        Spacer(modifier = Modifier.height(ComposaSpacing.Medium))

        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ManualResultScreen(
    uiState: HalalScannerUiState,
    onNavigateBack: () -> Unit,
    onCheckAnother: () -> Unit,
) {
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = Modifier
            .fillMaxSize()
            .background(ComposaTheme.color.backgroundAppBackground),
        topBar = {
            TopAppBar(
                title = stringResource(Res.string.text_check_result),
                navigationIcon = Res.drawable.ic_back,
                onNavigationIconClicked = onNavigateBack,
                action = when (uiState) {
                    is HalalScannerUiState.Error,
                    is HalalScannerUiState.Success -> TrailingAction.TextButton(
                        text = stringResource(Res.string.text_check_another),
                        onClick = onCheckAnother,
                    )
                    else -> null
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(ComposaTheme.color.backgroundAppBackground),
            contentAlignment = when (uiState) {
                is HalalScannerUiState.Success -> Alignment.TopCenter
                else -> Alignment.Center
            },
        ) {
            when (uiState) {
                is HalalScannerUiState.Loading -> {
                    LoadingContent(modifier = Modifier.fillMaxWidth())
                }

                is HalalScannerUiState.Error -> {
                    ErrorCard(
                        description = stringResource(Res.string.text_product_not_found),
                        buttonText = stringResource(Res.string.text_cancel),
                        onRetry = onCheckAnother,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                is HalalScannerUiState.Success -> {
                    InformationContentV2(
                        productUiState = uiState.data,
                        onNavigateBack = onNavigateBack,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                is HalalScannerUiState.Init -> Unit
            }
        }
    }
}

