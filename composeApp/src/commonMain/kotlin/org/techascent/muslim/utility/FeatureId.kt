package org.techascent.muslim.utility

import androidx.compose.ui.graphics.Color
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

/**
 * Stable identifier for every feature in the Explore (Utility) tab.
 *
 * ### Design Decisions
 * - The [key] is used as a DataStore preference key suffix (`"usage_{key}"`), so it **must never
 *   be renamed** once shipped — renaming resets all recorded usage data.
 * - [emoji] / [titleRes] / [accentColor] mirror the values defined in `UtilityUiState.kt` so the
 *   quick-access cards on the Prayer screen look visually consistent with the Explore tab.
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
        accentColor = Color(0xFF4CAF50),
    ),
    MANUAL_HALAL_CHECK(
        key = "manual_halal_check",
        emoji = "✍️",
        titleRes = Res.string.title_manual_halal_check,
        accentColor = Color(0xFF00897B),
    ),
    SCAN_HISTORY(
        key = "scan_history",
        emoji = "📋",
        titleRes = Res.string.title_scan_history,
        accentColor = Color(0xFF5E35B1),
    ),
    QURAN(
        key = "quran",
        emoji = "📖",
        titleRes = Res.string.title_quran,
        accentColor = Color(0xFF1565C0),
    ),
    QIBLA(
        key = "qibla",
        emoji = "🧭",
        titleRes = Res.string.title_quibla,
        accentColor = Color(0xFF00838F),
    ),
    TASBEEH(
        key = "tasbeeh",
        emoji = "📿",
        titleRes = Res.string.title_tasbeeh,
        accentColor = Color(0xFF7B1FA2),
    ),
    NEARBY_MOSQUE(
        key = "nearby_mosque",
        emoji = "🕌",
        titleRes = Res.string.title_nearby_mosque,
        accentColor = Color(0xFFE65100),
    ),
    PRAYER_CALENDAR(
        key = "prayer_calendar",
        emoji = "🗓️",
        titleRes = Res.string.title_prayer_calendar,
        accentColor = Color(0xFFD84315),
    ),
    ZAKAT_CALCULATOR(
        key = "zakat_calculator",
        emoji = "💰",
        titleRes = Res.string.title_zakat_calculator,
        accentColor = Color(0xFF2E7D32),
    ),
}

