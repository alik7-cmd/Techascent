package org.techascent.muslim.quran

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import apphub.composeapp.generated.resources.Res
import apphub.composeapp.generated.resources.ic_back
import apphub.composeapp.generated.resources.text_ayahs
import apphub.composeapp.generated.resources.title_quran
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.techascent.composa.appbar.TopAppBar
import org.techascent.composa.common.ComposaSpacing
import org.techascent.composa.shimmer.shimmerEffect
import org.techascent.composa.theming.ComposaTheme
import org.techascent.muslim.prayer.composable.ErrorScreen
import org.techascent.muslim.quran.state.SurahListUiState
import org.techascent.shared.data.SurahInfo

@Composable
internal fun SurahListView(
    onNavigateBack: () -> Unit,
    onNavigateToSurahDetail: (Int) -> Unit,
) {
    ComposaTheme {
        SurahListScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToSurahDetail = onNavigateToSurahDetail,
        )
    }
}

@OptIn(KoinExperimentalAPI::class, ExperimentalMaterial3Api::class)
@Composable
private fun SurahListScreen(
    viewModel: QuranViewModel = koinViewModel<QuranViewModel>(),
    onNavigateBack: () -> Unit,
    onNavigateToSurahDetail: (Int) -> Unit,
) {
    val uiState by viewModel.surahListState.collectAsState()

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = Modifier
            .fillMaxSize()
            .background(ComposaTheme.color.backgroundAppBackground),
        topBar = {
            TopAppBar(
                title = stringResource(Res.string.title_quran),
                navigationIcon = Res.drawable.ic_back,
                onNavigationIconClicked = onNavigateBack,
            )
        },
    ) { innerPadding ->
        when (val state = uiState) {
            is SurahListUiState.Loading -> {
                SurahListSkeleton(innerPadding = innerPadding)
            }

            is SurahListUiState.Error -> {
                ErrorScreen(
                    description = state.message,
                    onRetry = { viewModel.loadSurahList() },
                    modifier = Modifier.padding(innerPadding),
                )
            }

            is SurahListUiState.Success -> {
                val listState = rememberLazyListState()

                // Scroll to last surah
                LaunchedEffect(state.lastSurahNumber) {
                    if (state.lastSurahNumber > 0) {
                        val index = state.surahs.indexOfFirst { it.number == state.lastSurahNumber }
                        if (index >= 0) {
                            listState.animateScrollToItem(index)
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    state = listState,
                    contentPadding = PaddingValues(vertical = ComposaSpacing.Small),
                ) {
                    items(
                        items = state.surahs,
                        key = { it.number }
                    ) { surah ->
                        SurahItem(
                            surah = surah,
                            isLastRead = surah.number == state.lastSurahNumber,
                            onClick = { onNavigateToSurahDetail(surah.number) }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = ComposaSpacing.Medium),
                            color = ComposaTheme.color.strokeNeutralSubtle
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SurahItem(
    surah: SurahInfo,
    isLastRead: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(
                if (isLastRead) ComposaTheme.color.strokeNeutralSubtle.copy(alpha = 0.3f)
                else ComposaTheme.color.backgroundAppBackground
            )
            .padding(
                horizontal = ComposaSpacing.Medium,
                vertical = ComposaSpacing.Small + ComposaSpacing.ExtraSmall
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Surah number badge
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(ComposaTheme.color.strokeNeutralSubtle),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = surah.number.toString(),
                style = ComposaTheme.typography.footnote,
                color = ComposaTheme.color.textNeutral,
            )
        }

        Spacer(modifier = Modifier.width(ComposaSpacing.Medium))

        // Surah name and translation
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = surah.englishName,
                style = ComposaTheme.typography.bodyEmphasized,
                color = ComposaTheme.color.textNeutral,
            )
            Text(
                text = "${surah.englishNameTranslation} • ${surah.numberOfAyahs} ${stringResource(Res.string.text_ayahs)}",
                style = ComposaTheme.typography.footnote,
                color = ComposaTheme.color.textNeutral.copy(alpha = 0.6f),
            )
        }

        // Arabic name
        Text(
            text = surah.name,
            style = ComposaTheme.typography.titleEmphasized,
            color = ComposaTheme.color.textNeutral,
        )
    }
}

// ─── Surah List Skeleton ────────────────────────────────────────────────────────

@Composable
private fun SurahListSkeleton(innerPadding: PaddingValues) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(innerPadding),
        contentPadding = PaddingValues(vertical = ComposaSpacing.Small),
    ) {
        items(12) { idx ->
            SurahItemSkeleton()
            if (idx < 11) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = ComposaSpacing.Medium),
                    color = ComposaTheme.color.strokeNeutralSubtle.copy(alpha = 0.4f),
                )
            }
        }
    }
}

@Composable
private fun SurahItemSkeleton() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = ComposaSpacing.Medium,
                vertical = ComposaSpacing.Small + ComposaSpacing.ExtraSmall,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Number badge
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .shimmerEffect(true)
        )

        Spacer(modifier = Modifier.width(ComposaSpacing.Medium))

        // Name + translation
        Column(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .size(width = 120.dp, height = 16.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .shimmerEffect(true)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .size(width = 160.dp, height = 12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .shimmerEffect(true)
            )
        }

        // Arabic name
        Box(
            modifier = Modifier
                .size(width = 60.dp, height = 22.dp)
                .clip(RoundedCornerShape(6.dp))
                .shimmerEffect(true)
        )
    }
}
