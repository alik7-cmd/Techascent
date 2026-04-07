package org.techascent.muslim.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import apphub.composeapp.generated.resources.Res
import apphub.composeapp.generated.resources.about_closing_note
import apphub.composeapp.generated.resources.about_section_growing_body
import apphub.composeapp.generated.resources.about_section_growing_title
import apphub.composeapp.generated.resources.about_section_halal_body
import apphub.composeapp.generated.resources.about_section_halal_title
import apphub.composeapp.generated.resources.about_section_intention_body
import apphub.composeapp.generated.resources.about_section_intention_title
import apphub.composeapp.generated.resources.about_section_prayer_body
import apphub.composeapp.generated.resources.about_section_prayer_title
import apphub.composeapp.generated.resources.about_section_privacy_body
import apphub.composeapp.generated.resources.about_section_privacy_title
import apphub.composeapp.generated.resources.about_section_qibla_body
import apphub.composeapp.generated.resources.about_section_qibla_title
import apphub.composeapp.generated.resources.about_section_quran_body
import apphub.composeapp.generated.resources.about_section_quran_title
import apphub.composeapp.generated.resources.about_section_ramadan_body
import apphub.composeapp.generated.resources.about_section_ramadan_title
import apphub.composeapp.generated.resources.about_section_tools_body
import apphub.composeapp.generated.resources.about_section_tools_title
import apphub.composeapp.generated.resources.ic_back
import apphub.composeapp.generated.resources.title_about_us
import org.jetbrains.compose.resources.stringResource
import org.techascent.composa.appbar.TopAppBar
import org.techascent.composa.common.ComposaSpacing
import org.techascent.composa.theming.ComposaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit,
) {
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = Modifier
            .fillMaxSize()
            .background(ComposaTheme.color.backgroundAppBackground),
        topBar = {
            TopAppBar(
                title = stringResource(Res.string.title_about_us),
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
                .padding(horizontal = ComposaSpacing.Medium)
                .padding(bottom = ComposaSpacing.ExtraLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // ── Content sections ─────────────────────────────────────────
            AboutSectionCard(
                emoji = "🤲",
                title = stringResource(Res.string.about_section_intention_title),
                body = stringResource(Res.string.about_section_intention_body),
            )

            Spacer(modifier = Modifier.height(ComposaSpacing.Medium))

            AboutSectionCard(
                emoji = "🔒",
                title = stringResource(Res.string.about_section_privacy_title),
                body = stringResource(Res.string.about_section_privacy_body),
            )

            Spacer(modifier = Modifier.height(ComposaSpacing.Medium))

            AboutSectionCard(
                emoji = "🕌",
                title = stringResource(Res.string.about_section_prayer_title),
                body = stringResource(Res.string.about_section_prayer_body),
            )

            Spacer(modifier = Modifier.height(ComposaSpacing.Medium))

            AboutSectionCard(
                emoji = "🧭",
                title = stringResource(Res.string.about_section_qibla_title),
                body = stringResource(Res.string.about_section_qibla_body),
            )

            Spacer(modifier = Modifier.height(ComposaSpacing.Medium))

            AboutSectionCard(
                emoji = "🌙",
                title = stringResource(Res.string.about_section_ramadan_title),
                body = stringResource(Res.string.about_section_ramadan_body),
            )

            Spacer(modifier = Modifier.height(ComposaSpacing.Medium))

            AboutSectionCard(
                emoji = "📖",
                title = stringResource(Res.string.about_section_quran_title),
                body = stringResource(Res.string.about_section_quran_body),
            )

            Spacer(modifier = Modifier.height(ComposaSpacing.Medium))

            AboutSectionCard(
                emoji = "🔍",
                title = stringResource(Res.string.about_section_halal_title),
                body = stringResource(Res.string.about_section_halal_body),
            )

            Spacer(modifier = Modifier.height(ComposaSpacing.Medium))

            AboutSectionCard(
                emoji = "📿",
                title = stringResource(Res.string.about_section_tools_title),
                body = stringResource(Res.string.about_section_tools_body),
            )

            Spacer(modifier = Modifier.height(ComposaSpacing.Medium))

            AboutSectionCard(
                emoji = "💚",
                title = stringResource(Res.string.about_section_growing_title),
                body = stringResource(Res.string.about_section_growing_body),
            )

            Spacer(modifier = Modifier.height(ComposaSpacing.Medium))

            // ── Closing note ─────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF1565C0).copy(alpha = 0.08f))
                    .padding(ComposaSpacing.Medium),
            ) {
                Text(
                    text = stringResource(Res.string.about_closing_note),
                    style = ComposaTheme.typography.subhead,
                    color = ComposaTheme.color.textNeutral,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(modifier = Modifier.height(ComposaSpacing.Large))

            Text(
                text = "بِسْمِ ٱللَّٰهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ",
                style = ComposaTheme.typography.titleMediumEmphasized,
                color = ComposaTheme.color.textNeutralSubtle,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(ComposaSpacing.Medium))
        }
    }
}

// ─── Section Card ────────────────────────────────────────────────────────────────

@Composable
private fun AboutSectionCard(
    emoji: String,
    title: String,
    body: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(ComposaTheme.color.strokeNeutralSubtle.copy(alpha = 0.10f))
            .padding(ComposaSpacing.Medium),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Header row
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(ComposaTheme.color.backgroundAction.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = emoji, fontSize = 18.sp)
        }

        Text(
            text = title,
            style = ComposaTheme.typography.subheadEmphasized,
            color = ComposaTheme.color.textNeutral,
        )

        Text(
            text = body,
            style = ComposaTheme.typography.subhead,
            color = ComposaTheme.color.textNeutralSubtle,
            lineHeight = 22.sp,
        )
    }
}

