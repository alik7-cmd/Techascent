package org.techascent.muslim.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import org.techascent.muslim.AppLang

/**
 * CompositionLocal providing the current [AppLang] to the whole tree.
 * Default is [AppLang.English].
 */
val LocalAppLang = compositionLocalOf { AppLang.English }

/**
 * Bengali digit characters mapped by their ordinal position (0–9).
 */
private val BENGALI_DIGITS = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')

/**
 * Replaces ASCII digits (0-9) with localized equivalents based on [lang].
 * Currently supports Bengali; all other languages return the original string.
 */
fun String.localizeDigits(lang: AppLang): String = when (lang) {
    AppLang.Bengali -> buildString(length) {
        for (ch in this@localizeDigits) {
            append(if (ch in '0'..'9') BENGALI_DIGITS[ch - '0'] else ch)
        }
    }
    else -> this
}

/**
 * Composable-friendly version that reads the current locale from [LocalAppLang].
 */
@Composable
fun String.localizeDigits(): String = localizeDigits(LocalAppLang.current)

/**
 * Convenience: wraps children with [LocalAppLang] provided.
 */
@Composable
fun ProvideAppLang(lang: AppLang, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalAppLang provides lang) {
        content()
    }
}

