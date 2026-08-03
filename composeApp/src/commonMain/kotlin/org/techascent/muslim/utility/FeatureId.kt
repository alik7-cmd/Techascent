package org.techascent.muslim.utility

import apphub.composeapp.generated.resources.Res
import apphub.composeapp.generated.resources.title_halal_scanner
import apphub.composeapp.generated.resources.title_manual_halal_check
import apphub.composeapp.generated.resources.title_nearby_mosque
import apphub.composeapp.generated.resources.title_prayer_calendar
import apphub.composeapp.generated.resources.title_quibla
import apphub.composeapp.generated.resources.title_quran
import apphub.composeapp.generated.resources.title_scan_history
import apphub.composeapp.generated.resources.title_tasbeeh
import apphub.composeapp.generated.resources.title_zakat_calculator
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
import androidx.compose.ui.graphics.Color

/**
 * Stable identifier for every feature in the Explore (Utility) tab.
 *
 * ### Design Decisions
 * - The [key] is used as a DataStore preference key suffix (`"usage_{key}"`), so it **must never
 *   be renamed** once shipped — renaming resets all recorded usage data.
 * - [accentColor] references **named Composa palette constants** (Tier 1 of the design system),
 *   NOT raw hex literals. This is intentional: enum constructors run outside Compose composition,
 *   so `ComposaTheme.color` (Tier 3) is not accessible here. The palette constants are the
 *   correct, manageable alternative for non-composable contexts.
 * - Colors here and in [UtilityUiState]'s FeatureItem must stay in sync — both reference the
 *   same palette constants.
 */
enum class FeatureId(
    /** Stable DataStore/persistence identifier — do NOT rename after shipping. */
    val key: String,
    val emoji: String,
    val titleRes: StringResource,
    val accentColor: Color,
) {
    HALAL_SCANNER(
        key = "halal_scanner",
        emoji = "🔍",
        titleRes = Res.string.title_halal_scanner,
        accentColor = ComposaGreen600,
    ),
    MANUAL_HALAL_CHECK(
        key = "manual_halal_check",
        emoji = "✍️",
        titleRes = Res.string.title_manual_halal_check,
        accentColor = ComposaGreen500,
    ),
    SCAN_HISTORY(
        key = "scan_history",
        emoji = "📋",
        titleRes = Res.string.title_scan_history,
        accentColor = ComposaPurple600,
    ),
    QURAN(
        key = "quran",
        emoji = "📖",
        titleRes = Res.string.title_quran,
        accentColor = ComposaBlue700,
    ),
    QIBLA(
        key = "qibla",
        emoji = "🧭",
        titleRes = Res.string.title_quibla,
        accentColor = ComposaBlue500,
    ),
    TASBEEH(
        key = "tasbeeh",
        emoji = "📿",
        titleRes = Res.string.title_tasbeeh,
        accentColor = ComposaPurple700,
    ),
    NEARBY_MOSQUE(
        key = "nearby_mosque",
        emoji = "🕌",
        titleRes = Res.string.title_nearby_mosque,
        accentColor = ComposaOrange700,
    ),
    PRAYER_CALENDAR(
        key = "prayer_calendar",
        emoji = "🗓️",
        titleRes = Res.string.title_prayer_calendar,
        accentColor = ComposaOrange500,
    ),
    ZAKAT_CALCULATOR(
        key = "zakat_calculator",
        emoji = "💰",
        titleRes = Res.string.title_zakat_calculator,
        accentColor = ComposaGreen700,
    ),
}
