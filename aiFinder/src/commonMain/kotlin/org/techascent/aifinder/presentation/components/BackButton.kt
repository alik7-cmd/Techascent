package org.techascent.aifinder.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.graphics.Color
import apphub.aifinder.generated.resources.Res
import apphub.aifinder.generated.resources.back
import apphub.aifinder.generated.resources.box_lid
import apphub.aifinder.generated.resources.ic_back
import org.jetbrains.compose.resources.stringResource
import org.techascent.composa.button.text.ComposaButtonText
import org.techascent.composa.common.DrawableData

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
        leftIcon = DrawableData(
            imageRes = Res.drawable.ic_back,
            tint = Color.Black
        ),
        onClick = onBack
    )
}
