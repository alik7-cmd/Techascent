package org.techascent.muslim.utility

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import apphub.composeapp.generated.resources.Res
import apphub.composeapp.generated.resources.text_halal_scanner_more
import apphub.composeapp.generated.resources.title_halal_scanner
import apphub.composeapp.generated.resources.title_quibla
import apphub.composeapp.generated.resources.title_tasbeeh
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.techascent.composa.appbar.TopAppBar
import org.techascent.composa.card.ComposaCardFrame
import org.techascent.composa.common.ComposaSpacing
import org.techascent.composa.common.DrawableData
import org.techascent.composa.icon.ComposaIcon
import org.techascent.composa.theming.ComposaTheme
import org.techascent.muslim.utility.state.FeatureItem


@Composable
fun UtilityView(
    onNavigateToTasbeeh: () -> Unit,
    onNavigateToCompass: () -> Unit,
    onNavigateHalalScanner: () -> Unit,
) {
    ComposaTheme {
        UtilityScreen(
            onNavigateToCompass = onNavigateToCompass,
            onNavigateToTasbeeh = onNavigateToTasbeeh,
            onNavigateHalalScanner = onNavigateHalalScanner
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UtilityScreen(
    onNavigateToTasbeeh: () -> Unit,
    onNavigateToCompass: () -> Unit,
    onNavigateHalalScanner: () -> Unit,
    viewModel: UtilityViewModel = koinViewModel<UtilityViewModel>(),
) {
    val uiState by viewModel.uiState.collectAsState()
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = Modifier
            .fillMaxSize()
            .background(ComposaTheme.color.backgroundAppBackground),
        topBar = {
            TopAppBar(
                title = stringResource(Res.string.text_halal_scanner_more),
            )
        },
    ) { innerPadding ->
        LazyVerticalGrid(
            modifier = Modifier.fillMaxSize(),
            contentPadding = innerPadding,
            columns = GridCells.Adaptive(minSize = 200.dp)
        ) {
            items(uiState.listOfFeatures) { item ->
                CenteredCardWithImageAndTitle(
                    item = item,
                    onNavigateToCompass = onNavigateToCompass,
                    onNavigateToTasbeeh = onNavigateToTasbeeh,
                    onNavigateHalalScanner = onNavigateHalalScanner
                )
            }
        }

    }
}

@Composable
fun CenteredCardWithImageAndTitle(
    item: FeatureItem,
    modifier: Modifier = Modifier,
    onNavigateToTasbeeh: () -> Unit,
    onNavigateToCompass: () -> Unit,
    onNavigateHalalScanner: () -> Unit,
) {
    Box(
        modifier = modifier.fillMaxSize().padding(ComposaSpacing.ExtraSmall),
        contentAlignment = Alignment.Center
    ) {
        ComposaCardFrame(
            borderColor = ComposaTheme.color.strokeNeutralSubtle,
            modifier = Modifier.clickable {
                when (item.titleRes) {
                    Res.string.title_tasbeeh -> onNavigateToTasbeeh()
                    Res.string.title_quibla -> onNavigateToCompass()
                    Res.string.title_halal_scanner -> onNavigateHalalScanner()
                }
            },
            content = {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(ComposaSpacing.Medium),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ComposaIcon(
                        modifier = Modifier.size(60.dp),
                        icon = DrawableData(
                            imageRes = item.imageRes,
                            tint = ComposaTheme.color.decorativeSecondBase,
                        )
                    )

                    Spacer(modifier = Modifier.size(ComposaSpacing.Medium))
                    Text(
                        text = stringResource(item.titleRes),
                        style = ComposaTheme.typography.titleDemi
                    )
                }
            }
        )
    }
}

