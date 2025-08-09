package org.techascent.shared.data.mapper

import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.techascent.shared.data.PrayerTimesResponse
import org.techascent.shared.data.common.truncateToMinute
import org.techascent.shared.data.dto.IftarTimeDto
import org.techascent.shared.data.dto.LocationDto
import org.techascent.shared.data.dto.PrayerName
import org.techascent.shared.data.dto.PrayerTimeDto
import org.techascent.shared.data.dto.PrayerTimeInterval

fun PrayerTimesResponse.toDto(): PrayerTimeDto {
    val timings = this.data.timings
    val timezone = TimeZone.of(this.data.meta.timezone)

    // Current date and time in given timezone
    val nowDateTime = Clock.System.now().toLocalDateTime(timezone).truncateToMinute()
    val today = nowDateTime.date

    // Parse prayer times (LocalTime)
    val fajrTime = LocalTime.parse(timings.fajr)
    val sunriseTime = LocalTime.parse(timings.sunrise)
    val sunsetTime = LocalTime.parse(timings.sunset)
    val dhuhrTime = LocalTime.parse(timings.dhuhr)
    val asrTime = LocalTime.parse(timings.asr)
    val maghribTime = LocalTime.parse(timings.maghrib)
    val ishaTime = LocalTime.parse(timings.isha)
    val imsakTime = LocalTime.parse(timings.imsak)

    // Construct LocalDateTime for today
    val fajrStart = LocalDateTime(today, fajrTime).truncateToMinute()
    val sunriseStart = LocalDateTime(today, sunriseTime).truncateToMinute()
    val sunsetStart = LocalDateTime(today, sunsetTime).truncateToMinute()
    val dhuhrStart = LocalDateTime(today, dhuhrTime).truncateToMinute()
    val asrStart = LocalDateTime(today, asrTime).truncateToMinute()
    val maghribStart = LocalDateTime(today, maghribTime).truncateToMinute()
    val ishaStart = LocalDateTime(today, ishaTime).truncateToMinute()
    val imsak = LocalDateTime(today, imsakTime).truncateToMinute()

    // Next day's fajr (Isha interval ends next day fajr)
    val nextDay = fajrStart.date.plus(1, DateTimeUnit.DAY) // or fajrStart.date.nextDay()

    // Create next day fajr LocalDateTime
    val nextDayFajrStart = LocalDateTime(nextDay, fajrStart.time)

    // Create intervals
    val intervals = listOf(
        PrayerTimeInterval(PrayerName.FAJR, fajrStart, sunriseStart),
        PrayerTimeInterval(PrayerName.SALAT_UD_DUHA, sunriseStart, dhuhrStart),
        PrayerTimeInterval(PrayerName.DUHR, dhuhrStart, asrStart),
        PrayerTimeInterval(PrayerName.ASR, asrStart, maghribStart),
        PrayerTimeInterval(PrayerName.MAGHRIB, maghribStart, ishaStart),
        PrayerTimeInterval(PrayerName.ISHA, ishaStart, nextDayFajrStart)
    )

    // Find current prayer based on now
    val currentPrayer = intervals.find {
        nowDateTime >= it.startTime && nowDateTime < it.endTime
    }

    // Hijri date string from API response
    val hijriDate = this.data.date.hijri.date

    return PrayerTimeDto(
        intervals = intervals,
        currentPrayer = currentPrayer,
        hijriDate = hijriDate,
        sunrise = sunriseStart,
        sunset = sunsetStart,
        iftarTime = IftarTimeDto(
            startTime = sunsetStart,
            endTime = imsak
        ),
        location = LocationDto(
            latitude = this.data.meta.latitude,
            longitude = this.data.meta.longitude
        )
    )
}

