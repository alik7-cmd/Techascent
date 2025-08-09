package org.techascent.composa.text

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle

@Immutable
sealed interface TextSegment {
    val text: String
    val order: Int
}

@Immutable
data class NormalText(
    override val text: String,
    override val order: Int
) : TextSegment

@Immutable
data class DecoratedText(
    override val text: String,
    override val order: Int,
    val spanStyle: SpanStyle,
    val tag: String? = null,
    val isClickable: Boolean = false
) : TextSegment

@Immutable
data class StyledText(
    override val text: String,
    override val order: Int,
    val textStyle: TextStyle,
    val tag: String? = null,
    val isClickable: Boolean = false
) : TextSegment