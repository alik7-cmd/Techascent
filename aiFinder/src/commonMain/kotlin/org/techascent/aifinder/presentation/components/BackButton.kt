package org.techascent.aifinder.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.graphics.Color
import apphub.aifinder.generated.resources.Res
import apphub.aifinder.generated.resources.back
import org.jetbrains.compose.resources.stringResource
import org.techascent.composa.button.text.ComposaButtonText

@Composable
fun BackButton(onBack: () -> Unit, contentColor: Color? = Color.Black) {
    if (contentColor != null) {
        TextButton(
            onClick = onBack,
            colors = ButtonDefaults.textButtonColors(contentColor = contentColor)
        ) {
            Text(stringResource(Res.string.back))
        }
        return
    }
    ComposaButtonText(
        text = stringResource(Res.string.back),
        onClick = onBack
    )
}
