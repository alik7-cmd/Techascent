package org.techascent.muslim.common

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration

data class DateEntity(
    val day: Int,
    val month: Int,
    val year: Int
)


fun Long.toReadableDate(): String {
    val instant = Instant.fromEpochMilliseconds(this)
    val date = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date

    val day = date.dayOfMonth.toString().padStart(2, '0')
    val month = date.month.name.lowercase()
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    val year = date.year.toString()

    return "$day $month $year"
}

fun Duration.formatDuration(): String {
    val totalSeconds = this.inWholeSeconds
    val days = totalSeconds / (24 * 3600)
    val hours = (totalSeconds % (24 * 3600)) / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    val hoursStr = hours.toString().padStart(2, '0')
    val minutesStr = minutes.toString().padStart(2, '0')
    val secondsStr = seconds.toString().padStart(2, '0')

    return if (days > 0) {
        val daysStr = days.toString()
        "$daysStr:$hoursStr:$minutesStr:$secondsStr"
    } else {
        "$hoursStr:$minutesStr:$secondsStr"
    }
}

fun LocalDateTime.toHourMinuteString(is24HourFormat: Boolean = true): String {
    val hourPart = if (is24HourFormat) {
        hour.toString().padStart(2, '0')
    } else {
        // Convert 24h to 12h format
        val h = when {
            hour == 0 || hour == 12 -> 12
            else -> hour % 12
        }
        h.toString()
    }
    val minutePart = minute.toString().padStart(2, '0')

    return if (is24HourFormat) {
        "$hourPart:$minutePart"
    } else {
        val amPm = if (hour < 12) "AM" else "PM"
        "$hourPart:$minutePart $amPm"
    }
}

fun getCurrentDateFormatted(): String {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val day = now.dayOfMonth.toString().padStart(2, '0')
    val month = now.monthNumber.toString().padStart(2, '0')
    val year = now.year.toString()
    return "$day-$month-$year"
}

fun getCurrentYearAndMonth(): DateEntity {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val day = now.dayOfMonth.toString().padStart(2, '0')
    val month = now.monthNumber.toString().padStart(2, '0')
    val year = now.year.toString()
    return DateEntity(day = day.toInt(), month = month.toInt(), year = year.toInt())
}
val currentDate: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

fun LocalDate.toDayMonthYearString(): String {
    val day = this.dayOfMonth.toString().padStart(2, '0')
    val month = this.monthNumber.toString().padStart(2, '0')
    val year = this.year.toString()
    return "$day-$month-$year"
}

fun yearMonth(year: Int, month: Int): MonthLength {
    val daysInMonth = when (month) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        2 -> if (isLeapYear(year)) 29 else 28
        else -> 30
    }
    return MonthLength(daysInMonth)
}

data class MonthLength(val days: Int)

fun MonthLength.lengthOfMonth(): Int = days

fun isLeapYear(year: Int): Boolean =
    (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)