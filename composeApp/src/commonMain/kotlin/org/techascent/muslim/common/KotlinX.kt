@file:Suppress("MatchingDeclarationName")

package org.techascent.muslim.common

import apphub.composeapp.generated.resources.Res
import apphub.composeapp.generated.resources.text_hanafi
import apphub.composeapp.generated.resources.text_shafi
import org.jetbrains.compose.resources.StringResource
import org.techascent.shared.data.enum.School

/**
 * Re-exports from shared so existing composeApp imports keep working.
 * New code should import directly from org.techascent.shared.data.common.
 */
typealias DateEntity = org.techascent.shared.data.common.DateEntity
typealias MonthLength = org.techascent.shared.data.common.MonthLength

// Re-export shared extension functions for backward-compatibility:
// These are top-level functions in shared, so they resolve automatically
// via the shared dependency. No need to re-declare them.
// Consumers importing from org.techascent.muslim.common will need to
// update to org.techascent.shared.data.common.* for:
//   - toHourMinuteString
//   - getCurrentDateFormatted
//   - getCurrentYearAndMonth
//   - toReadableDate
//   - formatDuration
//   - toDayMonthYearString
//   - yearMonth
//   - isLeapYear
//   - currentDate
//   - MonthLength.lengthOfMonth

/**
 * UI-only extension: map School to display StringResource.
 * This stays in composeApp because it depends on Compose Resources.
 */
fun School.toTextRes(): StringResource = when (this) {
    School.SHAFI -> Res.string.text_shafi
    School.HANAFI -> Res.string.text_hanafi
}

/**
 * UI-only extension: map School to boolean visibility toggle.
 */
fun School.toVisibility() = when (this) {
    School.SHAFI -> false
    School.HANAFI -> true
}