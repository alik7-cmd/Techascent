package org.techascent.muslim.utility

import androidx.compose.runtime.Composable
import org.techascent.composa.theming.ComposaTheme


@Composable
fun UtilityView(
    onNavigateToTasbeeh: () -> Unit,
    onNavigateToCompass: () -> Unit,
    onNavigateHalalScanner: () -> Unit,
    onNavigateToQuran: () -> Unit,
    onNavigateManualHalalCheck: () -> Unit,
    onNavigateScanHistory: () -> Unit,
) {
    ComposaTheme {
        UtilityScreenV2(
            onNavigateToCompass = onNavigateToCompass,
            onNavigateToTasbeeh = onNavigateToTasbeeh,
            onNavigateHalalScanner = onNavigateHalalScanner,
            onNavigateToQuran = onNavigateToQuran,
            onNavigateManualHalalCheck = onNavigateManualHalalCheck,
            onNavigateScanHistory = onNavigateScanHistory,
        )
    }
}