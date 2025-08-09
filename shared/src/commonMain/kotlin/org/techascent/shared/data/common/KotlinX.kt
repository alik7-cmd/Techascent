package org.techascent.shared.data.common
import kotlinx.datetime.LocalDateTime

internal fun LocalDateTime.truncateToMinute(): LocalDateTime {
    return LocalDateTime(
        year = year,
        month = month,
        dayOfMonth = dayOfMonth,
        hour = hour,
        minute = minute,
        second = 0,
        nanosecond = 0
    )
}