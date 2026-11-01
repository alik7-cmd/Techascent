package org.techascent.aifinder.presentation.navigation

import androidx.compose.runtime.Composable

@Composable
actual fun SystemBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // iOS has no system back button; screens use the shared toolbar action.
}
