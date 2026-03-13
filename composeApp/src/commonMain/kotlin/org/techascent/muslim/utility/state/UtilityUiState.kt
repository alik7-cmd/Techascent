package org.techascent.muslim.utility.state

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import apphub.composeapp.generated.resources.Res
import apphub.composeapp.generated.resources.ic_halal
import apphub.composeapp.generated.resources.ic_mosque
import apphub.composeapp.generated.resources.ic_quibla
import apphub.composeapp.generated.resources.ic_quran
import apphub.composeapp.generated.resources.ic_tasbeeh
import apphub.composeapp.generated.resources.ic_zakat
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
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

data class UtilityUiState(
    @Stable
    val listOfFeatures: List<FeatureItem> = featureList
)

data class FeatureItem(
    val titleRes: StringResource,
    val descriptionRes: StringResource? = null,
    val imageRes: DrawableResource,
    val tint: Color = Color.Unspecified,
    val emoji: String = "",
    val accentColor: Color = Color(0xFF4CAF50),
    val isWide: Boolean = false,
)

private val featureList = listOf(
    FeatureItem(
        titleRes = Res.string.title_halal_scanner,
        descriptionRes = Res.string.text_utility_desc_halal,
        imageRes = Res.drawable.ic_halal,
        tint = Color.Green,
        emoji = "🔍",
        accentColor = Color(0xFF4CAF50),
        isWide = true,
    ),
    FeatureItem(
        titleRes = Res.string.title_quran,
        descriptionRes = Res.string.text_utility_desc_quran,
        imageRes = Res.drawable.ic_quran,
        emoji = "📖",
        accentColor = Color(0xFF1565C0),
        isWide = true,
    ),
    FeatureItem(
        titleRes = Res.string.title_manual_halal_check,
        descriptionRes = Res.string.text_utility_desc_manual_halal,
        imageRes = Res.drawable.ic_halal,
        emoji = "✍️",
        accentColor = Color(0xFF00897B),
    ),
    FeatureItem(
        titleRes = Res.string.title_scan_history,
        descriptionRes = Res.string.text_utility_desc_scan_history,
        imageRes = Res.drawable.ic_halal,
        emoji = "📋",
        accentColor = Color(0xFF5E35B1),
    ),
    FeatureItem(
        titleRes = Res.string.title_tasbeeh,
        descriptionRes = Res.string.text_utility_desc_tasbeeh,
        imageRes = Res.drawable.ic_tasbeeh,
        emoji = "📿",
        accentColor = Color(0xFF7B1FA2),
    ),
    FeatureItem(
        titleRes = Res.string.title_quibla,
        descriptionRes = Res.string.text_utility_desc_qibla,
        imageRes = Res.drawable.ic_quibla,
        emoji = "🧭",
        accentColor = Color(0xFF00838F),
    ),
    FeatureItem(
        titleRes = Res.string.title_nearby_mosque,
        descriptionRes = Res.string.text_utility_desc_mosque,
        imageRes = Res.drawable.ic_mosque,
        emoji = "🕌",
        accentColor = Color(0xFFE65100),
    ),
    FeatureItem(
        titleRes = Res.string.title_zakat_calculator,
        descriptionRes = Res.string.text_utility_desc_zakat,
        imageRes = Res.drawable.ic_zakat,
        emoji = "💰",
        accentColor = Color(0xFF2E7D32),
    ),
)
