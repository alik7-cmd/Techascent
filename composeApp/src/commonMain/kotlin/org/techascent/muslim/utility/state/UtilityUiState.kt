package org.techascent.muslim.utility.state

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import apphub.composeapp.generated.resources.Res
import apphub.composeapp.generated.resources.title_halal_scanner
import apphub.composeapp.generated.resources.title_manual_halal_check
import apphub.composeapp.generated.resources.title_nearby_mosque
import apphub.composeapp.generated.resources.title_quibla
import apphub.composeapp.generated.resources.title_quran
import apphub.composeapp.generated.resources.title_scan_history
import apphub.composeapp.generated.resources.title_tasbeeh
import apphub.composeapp.generated.resources.title_zakat_calculator
import apphub.composeapp.generated.resources.text_utility_desc_halal
import apphub.composeapp.generated.resources.text_utility_desc_manual_halal
import apphub.composeapp.generated.resources.text_utility_desc_quran
import apphub.composeapp.generated.resources.text_utility_desc_scan_history
import apphub.composeapp.generated.resources.text_utility_desc_tasbeeh
import apphub.composeapp.generated.resources.text_utility_desc_qibla
import apphub.composeapp.generated.resources.text_utility_desc_mosque
import apphub.composeapp.generated.resources.text_utility_desc_zakat
import apphub.composeapp.generated.resources.title_prayer_calendar
import apphub.composeapp.generated.resources.text_utility_desc_calendar
import apphub.composeapp.generated.resources.text_category_faith_knowledge
import apphub.composeapp.generated.resources.text_category_daily_tools
import apphub.composeapp.generated.resources.text_category_more
import org.jetbrains.compose.resources.StringResource
import org.techascent.composa.theming.color.ComposaBlue500
import org.techascent.composa.theming.color.ComposaBlue700
import org.techascent.composa.theming.color.ComposaGreen500
import org.techascent.composa.theming.color.ComposaGreen600
import org.techascent.composa.theming.color.ComposaGreen700
import org.techascent.composa.theming.color.ComposaOrange500
import org.techascent.composa.theming.color.ComposaOrange700
import org.techascent.composa.theming.color.ComposaPurple600
import org.techascent.composa.theming.color.ComposaPurple700

data class UtilityUiState(
    @Stable
    val categories: List<FeatureCategory> = featureCategories
)

data class FeatureCategory(
    val titleRes: StringResource,
    val items: List<FeatureItem>,
)

data class FeatureItem(
    val titleRes: StringResource,
    val descriptionRes: StringResource? = null,
    val tint: Color = Color.Unspecified,
    val emoji: String = "",
    val accentColor: Color = ComposaGreen600, // default uses palette constant, not hex
)

// ── Faith & Knowledge: Halal-related + Quran ────────────────────────────────────
// Accent colors mirror FeatureId.accentColor — keep them in sync with FeatureId.kt.
private val faithAndKnowledgeItems = listOf(
    FeatureItem(
        titleRes = Res.string.title_halal_scanner,
        descriptionRes = Res.string.text_utility_desc_halal,
        emoji = "🔍",
        accentColor = ComposaGreen600,
    ),
    FeatureItem(
        titleRes = Res.string.title_manual_halal_check,
        descriptionRes = Res.string.text_utility_desc_manual_halal,
        emoji = "✍️",
        accentColor = ComposaGreen500,
    ),
    FeatureItem(
        titleRes = Res.string.title_scan_history,
        descriptionRes = Res.string.text_utility_desc_scan_history,
        emoji = "📋",
        accentColor = ComposaPurple600,
    ),
    FeatureItem(
        titleRes = Res.string.title_quran,
        descriptionRes = Res.string.text_utility_desc_quran,
        emoji = "📖",
        accentColor = ComposaBlue700,
    ),
)

// ── Daily Essentials: Qibla, Tasbeeh, Nearby Mosque ─────────────────────────────
private val dailyToolsItems = listOf(
    FeatureItem(
        titleRes = Res.string.title_quibla,
        descriptionRes = Res.string.text_utility_desc_qibla,
        emoji = "🧭",
        accentColor = ComposaBlue500,
    ),
    FeatureItem(
        titleRes = Res.string.title_tasbeeh,
        descriptionRes = Res.string.text_utility_desc_tasbeeh,
        emoji = "📿",
        accentColor = ComposaPurple700,
    ),
    FeatureItem(
        titleRes = Res.string.title_nearby_mosque,
        descriptionRes = Res.string.text_utility_desc_mosque,
        emoji = "🕌",
        accentColor = ComposaOrange700,
    ),
)

// ── Plan & Explore: Calendar, Zakat ─────────────────────────────────────────────
private val planAndExploreItems = listOf(
    FeatureItem(
        titleRes = Res.string.title_prayer_calendar,
        descriptionRes = Res.string.text_utility_desc_calendar,
        emoji = "🗓️",
        accentColor = ComposaOrange500,
    ),
    FeatureItem(
        titleRes = Res.string.title_zakat_calculator,
        descriptionRes = Res.string.text_utility_desc_zakat,
        emoji = "💰",
        accentColor = ComposaGreen700,
    ),
)

private val featureCategories = listOf(
    FeatureCategory(
        titleRes = Res.string.text_category_faith_knowledge,
        items = faithAndKnowledgeItems,
    ),
    FeatureCategory(
        titleRes = Res.string.text_category_daily_tools,
        items = dailyToolsItems,
    ),
    FeatureCategory(
        titleRes = Res.string.text_category_more,
        items = planAndExploreItems,
    ),
)
