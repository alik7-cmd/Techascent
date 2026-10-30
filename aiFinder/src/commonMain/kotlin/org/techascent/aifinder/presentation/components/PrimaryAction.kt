package org.techascent.aifinder.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.techascent.composa.button.primary.ComposaButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.graphics.Color
import org.techascent.aifinder.presentation.theme.AiFinderPalette

@Composable
fun PrimaryAction(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    ComposaButton(
        text = label,
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = AiFinderPalette.Teal,
            contentColor = Color.White
        ),
        iconTint = Color.White
    )
}
