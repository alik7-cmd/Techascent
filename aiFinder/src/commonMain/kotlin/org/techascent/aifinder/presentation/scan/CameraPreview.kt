package org.techascent.aifinder.presentation.scan

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun CameraPreview(modifier: Modifier = Modifier, onUnavailable: () -> Unit)
