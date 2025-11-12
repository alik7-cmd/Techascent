package org.techascent.composa.text

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import org.techascent.composa.theming.ComposaTheme

@Composable
fun BulletText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = LocalTextStyle.current,
    bulletColor: Color = ComposaTheme.color.textNeutral
) {
    Row(modifier = modifier, horizontalArrangement = spacedBy(4.dp)) {

        val tempFontSize = resolveFontSize(fontSize, style)
        val tempLineHeight = resolveLineHeight(lineHeight, style)

        val bulletPadding = tempLineHeight / 3
        val bulletSize = tempFontSize / 2

        Bullet(
            modifier = Modifier.paddingFromBaseline(bulletPadding),
            bulletColor = bulletColor,
            bulletSize = bulletSize
        )
        Text(
            text = text,
            color = color,
            fontSize = fontSize,
            fontStyle = fontStyle,
            fontWeight = fontWeight,
            fontFamily = fontFamily,
            letterSpacing = letterSpacing,
            textDecoration = textDecoration,
            textAlign = textAlign,
            lineHeight = lineHeight,
            overflow = overflow,
            softWrap = softWrap,
            maxLines = maxLines,
            minLines = minLines,
            onTextLayout = onTextLayout,
            style = style
        )
    }
}

@Composable
private fun Bullet(
    modifier: Modifier = Modifier,
    bulletColor: Color = Color.Black,
    bulletSize: TextUnit = LocalTextStyle.current.fontSize
) {
    val bulletSizeInDp = LocalDensity.current.run {
        bulletSize.toDp()
    }
    Canvas(modifier = modifier.size(bulletSizeInDp)) {
        drawCircle(color = bulletColor)
    }

}

@Composable
private fun resolveFontSize(
    fontSize: TextUnit,
    style: TextStyle
): TextUnit = when {
    fontSize != TextUnit.Unspecified -> fontSize
    style.fontSize != TextUnit.Unspecified -> style.fontSize
    else -> LocalTextStyle.current.fontSize
}

@Composable
private fun resolveLineHeight(
    lineHeight: TextUnit,
    style: TextStyle
): TextUnit = when {
    lineHeight != TextUnit.Unspecified -> lineHeight
    style.lineHeight != TextUnit.Unspecified -> style.lineHeight
    else -> LocalTextStyle.current.lineHeight
}