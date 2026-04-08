package org.techascent.muslim.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import apphub.composeapp.generated.resources.Res
import apphub.composeapp.generated.resources.text_am
import apphub.composeapp.generated.resources.text_pm
import org.jetbrains.compose.resources.stringResource
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
 * Replaces English "AM" / "PM" markers with their localized equivalents
 * (e.g. Bengali পূর্বাহ্ন / অপরাহ্ন). Call **before** [localizeDigits] so
 * that digit replacement doesn't interfere with the AM/PM text.
 */
@Composable
fun String.localizeAmPm(): String {
    val localizedAm = stringResource(Res.string.text_am)
    val localizedPm = stringResource(Res.string.text_pm)
    return this.replace("AM", localizedAm).replace("PM", localizedPm)
}

/**
 * Convenience: localizes both AM/PM and digits in one call.
 * Use this on any time string to get fully localized output.
 */
@Composable
fun String.localizeTime(): String = localizeAmPm().localizeDigits()

/**
 * Convenience: wraps children with [LocalAppLang] provided.
 */
@Composable
fun ProvideAppLang(lang: AppLang, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalAppLang provides lang) {
        content()
    }
}

