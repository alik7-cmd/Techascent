package org.techascent.composa.text

import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit

const val LINK_ANNOTATION_TAG = "LINK_ANNOTATION_TAG"

/**
 * A composable that displays a combination of normal and decorated text segments,
 * with support for clickable segments.
 *
 * The order of the text segments is determined by their `order` property.
 *
 * @param segments A list of [TextSegment] objects that define the content and styling.
 * @param modifier [Modifier] to be applied to the Text composable.
 * @param baseStyle The base [TextStyle] to be applied.
 * @param onTextSegmentClick A callback that is invoked when a clickable text segment
 *      (one with a `tag` and `isClickable = true`) is clicked. The callback receives the `tag`
 *      of the clicked segment.
 * @param color The default color.
 * @param fontSize The default font size.
 *
 * ### Example
 * ```
 * val segments = listOf(
 *         NormalText("Welcome to our ", order = 0),
 *         DecoratedText(
 *             text = "Special Offer",
 *             order = 1,
 *             spanStyle = SpanStyle(
 *                 color = Color(0xFFE91E63),
 *                 fontWeight = FontWeight.Bold,
 *                 fontSize = 20.sp
 *             )
 *         ),
 *         NormalText("! Don't miss out.", order = 2)
 *     )
 *     SpannableText(segments = segments, fontSize = 16.sp)
 * ```
 */
@Composable
@Suppress("ComplexMethod")
fun SpannableText(
    segments: List<TextSegment>,
    modifier: Modifier = Modifier,
    baseStyle: TextStyle = LocalTextStyle.current,
    onTextSegmentClick: ((tag: String) -> Unit)? = null,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    softWrap: Boolean = true,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    maxLines: Int = Int.MAX_VALUE
) {
    val sortedSegments = segments.sortedBy { it.order }

    val annotatedString = buildAnnotatedString {
        sortedSegments.forEach { segment ->
            when (segment) {
                is NormalText -> {
                    append(segment.text)
                }

                is DecoratedText -> {
                    if (segment.isClickable && segment.tag != null && onTextSegmentClick != null) {
                        // Use pushStringAnnotation for general annotations
                        pushStringAnnotation(tag = LINK_ANNOTATION_TAG, annotation = segment.tag)
                        withStyle(style = segment.spanStyle) {
                            append(segment.text)
                        }
                        pop() // Pop the string annotation
                    } else {
                        withStyle(style = segment.spanStyle) {
                            append(segment.text)
                        }
                    }
                }

                is StyledText -> {
                    if (segment.isClickable && segment.tag != null && onTextSegmentClick != null) {
                        pushStringAnnotation(tag = LINK_ANNOTATION_TAG, annotation = segment.tag)
                        withStyle(style = segment.textStyle.toSpanStyle()) {
                            append(segment.text)
                        }
                        pop() // Pop the string annotation
                    } else {
                        withStyle(style = segment.textStyle.toSpanStyle()) {
                            append(segment.text)
                        }
                    }
                }
            }
        }
    }

    onTextSegmentClick?.let {
        ClickableText(
            text = annotatedString,
            modifier = modifier,
            style = baseStyle.merge(
                TextStyle(
                    color = if (color != Color.Unspecified) color else baseStyle.color,
                    fontSize = if (fontSize != TextUnit.Unspecified) fontSize
                    else baseStyle.fontSize,
                    fontStyle = fontStyle ?: baseStyle.fontStyle,
                    fontWeight = fontWeight ?: baseStyle.fontWeight,
                    letterSpacing = if (letterSpacing != TextUnit.Unspecified) letterSpacing
                    else baseStyle.letterSpacing,
                    textDecoration = textDecoration ?: baseStyle.textDecoration,
                    textAlign = textAlign ?: baseStyle.textAlign,
                    lineHeight = if (lineHeight != TextUnit.Unspecified) lineHeight
                    else baseStyle.lineHeight,
                )
            ),
            softWrap = softWrap,
            onTextLayout = onTextLayout,
            maxLines = maxLines,
            onClick = { offset ->
                annotatedString.getStringAnnotations(
                    tag = LINK_ANNOTATION_TAG,
                    start = offset,
                    end = offset
                )
                    .firstOrNull()?.let { annotation ->
                        onTextSegmentClick(annotation.item)
                    }
            }
        )

    } ?: Text(
        text = annotatedString,
        modifier = modifier,
        style = baseStyle,
        color = color,
        fontSize = fontSize,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        textAlign = textAlign,
        lineHeight = lineHeight,
        softWrap = softWrap,
        onTextLayout = onTextLayout,
        maxLines = maxLines
    )
}

/*
@Composable
@Preview
private fun MixedStyleTextPreview() {
    SpannableText(
        segments = listOf(
            NormalText("Welcome to our ", order = 0),
            DecoratedText(
                text = "Special Offer",
                order = 1,
                spanStyle = SpanStyle(
                    color = Color(0xFFE91E63),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    textDecoration = TextDecoration.Underline
                ),
                tag = "special_offer_click",
                isClickable = true
            ),
            NormalText("! Don't miss out.", order = 2)
        ),
        fontSize = 16.sp,
        onTextSegmentClick = { tag ->
            Log.d("MixedStyleTextPreview", "Clicked on segment with tag: $tag")
        }
    )
}

@Preview(showBackground = true, name = "Complex Styling Preview (Clickable)")
@Composable
private fun ComplexSpannableTextPreview() {
    Column {
        SpannableText(
            segments = listOf(
                DecoratedText(
                    text = "BIG SALE: ",
                    order = 0,
                    spanStyle = SpanStyle(
                        color = Color.Red,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp
                    )
                    // Not clickable for this example part
                ),
                NormalText("Up to ", order = 1),
                DecoratedText(
                    text = "70% OFF",
                    order = 2,
                    spanStyle = SpanStyle(
                        background = Color.Yellow,
                        color = Color.Black,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 22.sp,
                        textDecoration = TextDecoration.Underline // Indicate clickability
                    ),
                    tag = "discount_70_percent",
                    isClickable = true
                ),

                NormalText(" on selected items. ", order = 3),
                StyledText(
                    text = "Limited time only!",
                    order = 4,
                    textStyle = TextStyle(
                        fontStyle = FontStyle.Italic,
                        color = Color.Gray,
                        letterSpacing = 0.5.sp
                    )
                )

            )
        )
    }

}

@Preview(showBackground = true, name = "Simple Offer Preview")
@Composable
private fun SpannableTextPreview() {
    val segments = listOf(
        NormalText("Welcome to our ", order = 0),
        DecoratedText(
            text = "Special Offer",
            order = 1,
            spanStyle = SpanStyle(
                color = Color(0xFFE91E63),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        ),
        NormalText("! Don't miss out.", order = 2)
    )
    SpannableText(segments = segments, fontSize = 16.sp)
}*/
