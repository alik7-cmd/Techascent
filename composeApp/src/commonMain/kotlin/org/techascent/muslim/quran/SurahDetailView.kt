package org.techascent.muslim.quran

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import apphub.composeapp.generated.resources.Res
import apphub.composeapp.generated.resources.ic_back
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.techascent.composa.appbar.TopAppBar
import org.techascent.composa.common.ComposaSpacing
import org.techascent.composa.shimmer.shimmerEffect
import org.techascent.composa.theming.ComposaTheme
import org.techascent.muslim.prayer.composable.ErrorScreen
import org.techascent.muslim.quran.state.AyahUiModel

@Composable
internal fun SurahDetailView(
    surahNumber: Int,
    onNavigateBack: () -> Unit,
) {
    ComposaTheme {
        SurahDetailScreen(
            surahNumber = surahNumber,
            onNavigateBack = onNavigateBack,
        )
    }
}

@OptIn(KoinExperimentalAPI::class, ExperimentalMaterial3Api::class)
@Composable
private fun SurahDetailScreen(
    surahNumber: Int,
    viewModel: QuranViewModel = koinViewModel<QuranViewModel>(),
    onNavigateBack: () -> Unit,
) {
    val uiState by viewModel.surahDetailState.collectAsState()

    LaunchedEffect(surahNumber) {
        viewModel.loadSurahDetail(surahNumber)
    }

    // Stop audio and save position on back press
    DisposableEffect(surahNumber) {
        onDispose {
            viewModel.stopAudioAndSavePosition(surahNumber)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = Modifier
            .fillMaxSize()
            .background(ComposaTheme.color.backgroundAppBackground),
        topBar = {
            TopAppBar(
                title = if (uiState.surahEnglishName.isNotEmpty())
                    "${uiState.surahEnglishName} - ${uiState.surahName}"
                else "Loading...",
                navigationIcon = Res.drawable.ic_back,
                onNavigationIconClicked = onNavigateBack,
            )
        },
    ) { innerPadding ->

        if (uiState.isLoading) {
            SurahDetailSkeleton(innerPadding = innerPadding)
            return@Scaffold
        }

        if (uiState.error != null) {
            ErrorScreen(
                description = uiState.error,
                onRetry = { viewModel.loadSurahDetail(surahNumber) },
                modifier = Modifier.padding(innerPadding),
            )
            return@Scaffold
        }

        val listState = rememberLazyListState()

        // Scroll to saved ayah position
        LaunchedEffect(uiState.lastAyahIndex) {
            if (uiState.lastAyahIndex > 0 && uiState.ayahs.isNotEmpty()) {
                listState.animateScrollToItem(
                    uiState.lastAyahIndex.coerceAtMost(uiState.ayahs.size - 1)
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            state = listState,
            contentPadding = PaddingValues(
                horizontal = ComposaSpacing.Medium,
                vertical = ComposaSpacing.Small
            ),
            verticalArrangement = Arrangement.spacedBy(ComposaSpacing.Small),
        ) {
            // Surah header
            item {
                SurahHeader(
                    surahName = uiState.surahName,
                    surahTranslation = uiState.surahTranslation,
                )
            }

            // Ayahs
            itemsIndexed(
                items = uiState.ayahs,
                key = { _, ayah -> ayah.numberInSurah }
            ) { _, ayah ->
                AyahCard(
                    ayah = ayah,
                    onPlayPause = { viewModel.onPlayPauseAyah(ayah) }
                )
            }
        }
    }
}

@Composable
private fun SurahHeader(
    surahName: String,
    surahTranslation: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1B5E20).copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ComposaSpacing.Medium),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = surahName,
                style = ComposaTheme.typography.titleEmphasized.copy(fontSize = 28.sp),
                color = ComposaTheme.color.textNeutral,
            )
            Spacer(modifier = Modifier.height(ComposaSpacing.ExtraSmall))
            Text(
                text = surahTranslation,
                style = ComposaTheme.typography.footnote,
                color = ComposaTheme.color.textNeutral.copy(alpha = 0.7f),
            )
            Spacer(modifier = Modifier.height(ComposaSpacing.Small))
            Text(
                text = "بِسْمِ ٱللَّهِ ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ",
                style = ComposaTheme.typography.titleEmphasized.copy(fontSize = 22.sp),
                color = ComposaTheme.color.textNeutral,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun AyahCard(
    ayah: AyahUiModel,
    onPlayPause: () -> Unit,
) {
    val bgColor by animateColorAsState(
        targetValue = if (ayah.isPlaying) Color(0xFF1B5E20).copy(alpha = 0.08f)
        else ComposaTheme.color.backgroundAppBackground
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ComposaSpacing.Medium)
        ) {
            // Header row: ayah number + play button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Ayah number badge
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(ComposaTheme.color.strokeNeutralSubtle),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = ayah.numberInSurah.toString(),
                        style = ComposaTheme.typography.footnote,
                        color = ComposaTheme.color.textNeutral,
                    )
                }

                // Play/Pause button
                if (ayah.audioUrl != null) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (ayah.isPlaying) Color(0xFF1B5E20).copy(alpha = 0.15f)
                                else ComposaTheme.color.strokeNeutralSubtle.copy(alpha = 0.5f)
                            )
                            .clickable(onClick = onPlayPause),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (ayah.isPlaying) "⏸" else "▶",
                            color = if (ayah.isPlaying) Color(0xFF1B5E20) else ComposaTheme.color.textNeutral,
                            style = ComposaTheme.typography.bodyEmphasized,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(ComposaSpacing.Small))

            // Arabic text
            Text(
                text = ayah.arabicText,
                modifier = Modifier.fillMaxWidth(),
                style = ComposaTheme.typography.titleEmphasized.copy(
                    fontSize = 24.sp,
                    lineHeight = 42.sp,
                    textDirection = TextDirection.Rtl
                ),
                color = ComposaTheme.color.textNeutral,
                textAlign = TextAlign.End,
            )

            // Translation (tafseer)
            if (ayah.translationText.isNotEmpty()) {
                Spacer(modifier = Modifier.height(ComposaSpacing.Small))
                Text(
                    text = ayah.translationText,
                    modifier = Modifier.fillMaxWidth(),
                    style = ComposaTheme.typography.footnote.copy(
                        lineHeight = 22.sp,
                    ),
                    color = ComposaTheme.color.textNeutral.copy(alpha = 0.7f),
                )
            }
        }
    }
}

// ─── Surah Detail Skeleton ──────────────────────────────────────────────────────

@Composable
private fun SurahDetailSkeleton(innerPadding: PaddingValues) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(innerPadding),
        contentPadding = PaddingValues(
            horizontal = ComposaSpacing.Medium,
            vertical = ComposaSpacing.Small,
        ),
        verticalArrangement = Arrangement.spacedBy(ComposaSpacing.Small),
    ) {
        // Header skeleton
        item { SurahHeaderSkeleton() }
        // Ayah skeletons
        items(6) { AyahCardSkeleton() }
    }
}

@Composable
private fun SurahHeaderSkeleton() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = ComposaTheme.color.strokeNeutralSubtle.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(ComposaSpacing.Medium),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Arabic surah name
            Box(
                modifier = Modifier
                    .size(width = 140.dp, height = 28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .shimmerEffect(true)
            )
            Spacer(Modifier.height(ComposaSpacing.ExtraSmall))
            // Translation
            Box(
                modifier = Modifier
                    .size(width = 100.dp, height = 14.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .shimmerEffect(true)
            )
            Spacer(Modifier.height(ComposaSpacing.Small))
            // Bismillah
            Box(
                modifier = Modifier
                    .size(width = 220.dp, height = 22.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .shimmerEffect(true)
            )
        }
    }
}

@Composable
private fun AyahCardSkeleton() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = ComposaTheme.color.backgroundAppBackground
        ),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(ComposaSpacing.Medium),
        ) {
            // Header: badge + play button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .shimmerEffect(true)
                )
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .shimmerEffect(true)
                )
            }
            Spacer(Modifier.height(ComposaSpacing.Small))
            // Arabic text lines
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .shimmerEffect(true)
            )
            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .shimmerEffect(true)
                    .align(Alignment.End)
            )
            Spacer(Modifier.height(ComposaSpacing.Small))
            // Translation lines
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .shimmerEffect(true)
            )
            Spacer(Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .shimmerEffect(true)
            )
            Spacer(Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .shimmerEffect(true)
            )
        }
    }
}
