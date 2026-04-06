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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import apphub.composeapp.generated.resources.Res
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
                title = "A Personal Intention",
                body = "I built Muslim – Namaz & Halal Scanner quietly, in the spaces between my " +
                        "responsibilities—outside of my regular professional work and family life. " +
                        "This app is not the result of a company roadmap or a commercial strategy, " +
                        "but of a personal intention: to create something beneficial, even if only " +
                        "one person ever truly uses it. If this app helps even a single individual " +
                        "stay connected to their faith, that alone would be enough for me. I see it " +
                        "as a small contribution to the Muslim Ummah, and I hope it carries value " +
                        "beyond this life.",
            )

            Spacer(modifier = Modifier.height(ComposaSpacing.Medium))

            AboutSectionCard(
                emoji = "🔒",
                title = "Ad-Free & Privacy First",
                body = "From the beginning, I made a conscious decision to keep the app completely " +
                        "ad-free. There are no distractions, no hidden agendas, and no compromises " +
                        "when it comes to user experience. Equally important is privacy—this app " +
                        "does not collect, store, or process your personal data. Everything remains " +
                        "on your device, under your control. In a time where data is often treated " +
                        "as a commodity, I wanted to build something that respects trust and simplicity.",
            )

            Spacer(modifier = Modifier.height(ComposaSpacing.Medium))

            AboutSectionCard(
                emoji = "🕌",
                title = "Prayer Times & Adhan",
                body = "At its core, the app is designed to support daily Islamic practice in a " +
                        "seamless and thoughtful way. It provides accurate prayer times for all five " +
                        "daily Salah—Fajr, Dhuhr, Asr, Maghrib, and Isha—based on your location, " +
                        "with the option to adjust settings manually. The Adhan serves as a gentle " +
                        "reminder, helping you stay punctual in your prayers, wherever you are.",
            )

            Spacer(modifier = Modifier.height(ComposaSpacing.Medium))

            AboutSectionCard(
                emoji = "🧭",
                title = "Qibla Compass",
                body = "The Qibla compass allows you to instantly find the direction of the Kaaba, " +
                        "whether you're at home or traveling in an unfamiliar place.",
            )

            Spacer(modifier = Modifier.height(ComposaSpacing.Medium))

            AboutSectionCard(
                emoji = "🌙",
                title = "Ramadan Support",
                body = "During Ramadan, the app becomes even more helpful, offering a clear timetable " +
                        "for Suhoor and Iftar so you can plan your fasts with confidence.",
            )

            Spacer(modifier = Modifier.height(ComposaSpacing.Medium))

            AboutSectionCard(
                emoji = "📖",
                title = "Quran Reader",
                body = "One of the features I felt was especially important to include is the ability " +
                        "to read the Quran directly within the app. It's designed with a clean, " +
                        "distraction-free interface so you can reflect and engage with the words of " +
                        "Allah anytime during your day.",
            )

            Spacer(modifier = Modifier.height(ComposaSpacing.Medium))

            AboutSectionCard(
                emoji = "🔍",
                title = "Halal Scanner",
                body = "Beyond prayer and reflection, the app also supports mindful living through " +
                        "its Halal Scanner. By scanning product barcodes, you can quickly check " +
                        "whether an item is halal, haram, or doubtful. The app uses publicly " +
                        "available data from OpenFoodFacts to help you make more informed choices " +
                        "about what you consume.",
            )

            Spacer(modifier = Modifier.height(ComposaSpacing.Medium))

            AboutSectionCard(
                emoji = "📿",
                title = "Practical Tools",
                body = "There are also small, practical tools built in—like a prayer countdown timer " +
                        "to keep track of time between Salah, and a digital tasbeeh counter for " +
                        "dhikr. Each feature is simple by design, meant to serve a clear purpose " +
                        "without overwhelming the user.",
            )

            Spacer(modifier = Modifier.height(ComposaSpacing.Medium))

            AboutSectionCard(
                emoji = "💚",
                title = "Always Growing",
                body = "This app is still growing. I continue to improve it gradually, refining " +
                        "features, fixing issues, and adding what feels genuinely useful. It may " +
                        "never be perfect, but it is built with sincerity and care.",
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
                    text = buildAnnotatedString {
                        append("More than anything, ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic)) {
                            append("Muslim – Namaz & Halal Scanner")
                        }
                        append(
                            " is intended to be a quiet companion in your daily life—a tool that " +
                                    "helps you stay consistent in prayer, conscious in your choices, " +
                                    "and connected to your faith wherever you are."
                        )
                    },
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

