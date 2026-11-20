package org.techascent.aifinder.presentation.navigation

import androidx.compose.runtime.Composable

@Composable
expect fun SystemBackHandler(enabled: Boolean, onBack: () -> Unit)
