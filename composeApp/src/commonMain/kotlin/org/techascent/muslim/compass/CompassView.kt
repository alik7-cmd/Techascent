package org.techascent.muslim.compass

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import apphub.composeapp.generated.resources.Res
import apphub.composeapp.generated.resources.ic_back
import apphub.composeapp.generated.resources.ic_compass_2
import apphub.composeapp.generated.resources.text_compass_suggestion
import apphub.composeapp.generated.resources.title_quibla
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.techascent.composa.appbar.TopAppBar
import org.techascent.composa.common.ComposaSpacing
import org.techascent.composa.common.DrawableData
import org.techascent.composa.icon.ComposaIcon
import org.techascent.composa.theming.ComposaTheme
import org.techascent.muslim.getScreenHeightPx
import org.techascent.muslim.getScreenWidthPx

@Composable
fun CompassView(
    onNavigateBack: () -> Unit
) {
    ComposaTheme {
        CompassScreen(
            onNavigateBack = onNavigateBack
        )
    }
}

@OptIn(KoinExperimentalAPI::class)
@Composable
private fun CompassScreen(
    viewModel: CompassViewModel = koinViewModel<CompassViewModel>(),
    onNavigateBack: () -> Unit
) {
    CompassContent(
        quiblaDirectionFlow = viewModel.qiblaDirection,
        onNavigateBack = onNavigateBack
    )

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CompassContent(
    quiblaDirectionFlow: Flow<Float>,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = Modifier
            .fillMaxSize()
            .background(color = ComposaTheme.color.backgroundAppBackground),
        topBar = {
            TopAppBar(
                title = stringResource(resource = Res.string.title_quibla),
                navigationIcon = Res.drawable.ic_back,
                onNavigationIconClicked = onNavigateBack
            )
        }

    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = innerPadding,
            verticalArrangement = spacedBy(space = ComposaSpacing.Medium)
        ) {
            quiblaDirectionCompass(quiblaDirectionFlow = quiblaDirectionFlow)
        }
    }
}

fun LazyListScope.quiblaDirectionCompass(
    quiblaDirectionFlow: Flow<Float>,
) {
    item {
        val direction by quiblaDirectionFlow.collectAsState(initial = 0f)

        val rotationDegrees by animateFloatAsState(targetValue = direction)

        val iconSize = responsiveDp(fraction = 0.5f)

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
                ComposaIcon(
                    icon = DrawableData(
                        imageRes = Res.drawable.ic_compass_2,
                        tint = ComposaTheme.color.iconAction
                    ),
                    modifier = Modifier.size(iconSize)
                        .rotate(rotationDegrees)
                )

                Text(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = ComposaSpacing.Medium),
                    text = stringResource(resource = Res.string.text_compass_suggestion),
                    style = ComposaTheme.typography.footnote,
                    color = ComposaTheme.color.textNeutral
                )
            }

        }
    }

}

@Composable
fun responsiveDp(fraction: Float = 0.2f): Dp {
    val widthPx = getScreenWidthPx()
    val heightPx = getScreenHeightPx()
    val density = LocalDensity.current

    val smallerPx = if (widthPx < heightPx) widthPx else heightPx

    return with(density) { (smallerPx * fraction).toDp() }
}