package org.techascent.aifinder.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import apphub.aifinder.generated.resources.Res
import apphub.aifinder.generated.resources.welcome_illustration_description
import org.jetbrains.compose.resources.stringResource
import org.techascent.aifinder.presentation.theme.AiFinderPalette

/** Vector scene with no baked-in background; the caller owns the surrounding surface. */
@Composable
fun WelcomeIllustration(
    modifier: Modifier = Modifier,
    surface: Color = AiFinderPalette.Surface,
    accent: Color = AiFinderPalette.Teal,
    outline: Color = AiFinderPalette.Ink,
    inset: Color = AiFinderPalette.Mint,
) {
    val description = stringResource(Res.string.welcome_illustration_description)
    Canvas(modifier.semantics { contentDescription = description }) {
        val scale = minOf(size.width / 770f, size.height / 520f)
        withTransform({
            translate((size.width - 770f * scale) / 2f, (size.height - 520f * scale) / 2f)
            scale(scale, scale, pivot = Offset.Zero)
        }) {
            fun polygon(fill: Color, first: Offset, second: Offset, third: Offset, fourth: Offset) {
                val path = Path().apply {
                    moveTo(first.x, first.y)
                    lineTo(second.x, second.y)
                    lineTo(third.x, third.y)
                    lineTo(fourth.x, fourth.y)
                    close()
                }
                drawPath(path, fill)
                drawPath(path, outline, style = Stroke(3f, cap = StrokeCap.Round))
            }
            fun line(start: Offset, end: Offset, color: Color = outline, width: Float = 3f) {
                drawLine(color, start, end, width, StrokeCap.Round)
            }

            drawOval(accent.copy(alpha = 0.1f), Offset(60f, 340f), Size(450f, 105f))
            drawOval(accent.copy(alpha = 0.1f), Offset(445f, 265f), Size(275f, 80f))

            // Storage box behind the open drawer.
            polygon(surface, Offset(476f, 170f), Offset(611f, 104f), Offset(698f, 150f), Offset(565f, 218f))
            polygon(inset, Offset(565f, 218f), Offset(698f, 150f), Offset(698f, 293f), Offset(565f, 363f))
            polygon(surface, Offset(476f, 170f), Offset(565f, 218f), Offset(565f, 363f), Offset(476f, 308f))
            polygon(surface, Offset(453f, 160f), Offset(600f, 85f), Offset(717f, 148f), Offset(565f, 226f))
            polygon(surface, Offset(453f, 160f), Offset(565f, 226f), Offset(565f, 251f), Offset(453f, 185f))
            polygon(inset, Offset(565f, 226f), Offset(717f, 148f), Offset(717f, 174f), Offset(565f, 251f))
            line(Offset(612f, 275f), Offset(651f, 254f), outline, 12f)

            // Drawer tray, contents, then the front panel.
            polygon(surface, Offset(75f, 246f), Offset(288f, 120f), Offset(493f, 237f), Offset(271f, 372f))
            polygon(inset, Offset(95f, 247f), Offset(288f, 139f), Offset(470f, 239f), Offset(271f, 352f))
            line(Offset(288f, 139f), Offset(288f, 196f))
            line(Offset(288f, 196f), Offset(142f, 280f), outline.copy(alpha = 0.3f))
            line(Offset(288f, 196f), Offset(422f, 271f), outline.copy(alpha = 0.3f))

            // Passport.
            withTransform({ rotate(-29f, Offset(202f, 268f)) }) {
                drawRoundRect(accent, Offset(157f, 216f), Size(89f, 108f), CornerRadius(8f))
                drawRoundRect(outline, Offset(157f, 216f), Size(89f, 108f), CornerRadius(8f), style = Stroke(3f))
                drawCircle(surface, 22f, Offset(201f, 259f), style = Stroke(2.5f))
                drawOval(surface, Offset(192f, 237f), Size(18f, 44f), style = Stroke(2f))
                line(Offset(180f, 259f), Offset(222f, 259f), surface, 2f)
                line(Offset(178f, 296f), Offset(224f, 296f), surface, 3f)
                line(Offset(187f, 305f), Offset(215f, 305f), surface, 2f)
            }

            // Coiled cable and plug.
            drawOval(outline, Offset(234f, 187f), Size(94f, 58f), style = Stroke(9f))
            drawOval(surface.copy(alpha = 0.5f), Offset(234f, 187f), Size(94f, 58f), style = Stroke(2f))
            line(Offset(313f, 235f), Offset(330f, 254f), outline, 7f)
            drawRoundRect(accent, Offset(319f, 249f), Size(27f, 20f), CornerRadius(4f))
            drawRoundRect(outline, Offset(319f, 249f), Size(27f, 20f), CornerRadius(4f), style = Stroke(3f))
            line(Offset(346f, 255f), Offset(355f, 255f), outline, 5f)

            // Keys and batteries.
            drawCircle(outline, 16f, Offset(281f, 297f), style = Stroke(4f))
            line(Offset(276f, 310f), Offset(250f, 336f), outline, 7f)
            line(Offset(258f, 328f), Offset(264f, 334f), outline, 5f)
            drawCircle(accent, 12f, Offset(296f, 311f))
            line(Offset(300f, 322f), Offset(309f, 342f), accent, 7f)
            listOf(Offset(379f, 241f), Offset(404f, 255f)).forEach { top ->
                drawRoundRect(surface, top, Size(18f, 49f), CornerRadius(5f))
                drawRoundRect(outline, top, Size(18f, 49f), CornerRadius(5f), style = Stroke(3f))
                drawRoundRect(accent, top, Size(18f, 17f), CornerRadius(5f))
                line(top + Offset(9f, 3f), top + Offset(9f, 11f), surface, 2f)
                line(top + Offset(5f, 7f), top + Offset(13f, 7f), surface, 2f)
            }
            polygon(surface, Offset(75f, 246f), Offset(271f, 361f), Offset(271f, 455f), Offset(75f, 337f))
            polygon(surface, Offset(271f, 361f), Offset(493f, 228f), Offset(493f, 321f), Offset(271f, 455f))
            polygon(inset, Offset(75f, 246f), Offset(88f, 239f), Offset(283f, 353f), Offset(271f, 361f))
            polygon(surface, Offset(271f, 361f), Offset(283f, 353f), Offset(493f, 228f), Offset(506f, 236f))
            line(Offset(340f, 375f), Offset(340f, 386f), outline, 10f)
            line(Offset(340f, 386f), Offset(395f, 353f), outline, 10f)
            line(Offset(395f, 353f), Offset(395f, 342f), outline, 10f)
            repeat(3) { index ->
                val y = 273f + index * 23f
                line(Offset(88f, y), Offset(251f, y + 97f), outline.copy(alpha = 0.12f), 2f)
            }

            // Transparent lens keeps the stored objects visible.
            line(Offset(491f, 273f), Offset(589f, 382f), outline, 31f)
            line(Offset(525f, 310f), Offset(596f, 390f), accent, 46f)
            drawCircle(outline, 108f, Offset(425f, 201f), style = Stroke(30f))
            drawCircle(accent, 108f, Offset(425f, 201f), style = Stroke(24f))
            drawArc(
                surface.copy(alpha = 0.6f), 207f, 60f, false,
                Offset(338f, 114f), Size(174f, 174f),
                style = Stroke(5f, cap = StrokeCap.Round),
            )
        }
    }
}
