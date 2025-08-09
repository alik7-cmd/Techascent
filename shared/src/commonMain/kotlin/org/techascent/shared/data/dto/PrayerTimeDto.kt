package org.techascent.shared.data.dto

import kotlinx.datetime.LocalDateTime

data class PrayerTimeDto(
    val intervals: List<PrayerTimeInterval>,
    val currentPrayer: PrayerTimeInterval?,
    val iftarTime: IftarTimeDto?,
    val hijriDate: String,
    val sunrise: LocalDateTime,
    val sunset: LocalDateTime,
    val location: LocationDto
)

data class PrayerTimeInterval(
    val name: PrayerName,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime
)

data class IftarTimeDto(
    val startTime: LocalDateTime,
    val endTime: LocalDateTime
)

data class LocationDto(
    val latitude: Double,
    val longitude: Double
)

enum class PrayerName {
    FAJR, SALAT_UD_DUHA, DUHR, ASR, MAGHRIB, ISHA
}
