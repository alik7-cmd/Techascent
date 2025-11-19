package org.techascent.shared.data.mapper

import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.techascent.shared.data.Data
import org.techascent.shared.data.OpenFoodFactsResponse
import org.techascent.shared.data.PrayerData
import org.techascent.shared.data.PrayerTimesMonthlyResponse
import org.techascent.shared.data.Product
import org.techascent.shared.data.common.truncateToMinute
import org.techascent.shared.data.dto.IftarTimeDto
import org.techascent.shared.data.dto.LocationDto
import org.techascent.shared.data.dto.PrayerName
import org.techascent.shared.data.dto.PrayerTimeDto
import org.techascent.shared.data.dto.PrayerTimeInterval
import org.techascent.shared.data.dto.ProductDto

fun PrayerData.toDto(): PrayerTimeDto {
    val timings = this.timings
    val timezone = TimeZone.of(this.meta.timezone)

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
    val hijriDate = this.date.hijri.day
        .plus(" ")
        .plus(this.date.hijri.month.en)
        .plus(" ").plus(this.date.hijri.year)

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
            latitude = this.meta.latitude,
            longitude = this.meta.longitude
        )
    )
}

fun Data.toDto(): PrayerTimeDto {
    val timings = this.timings
    val timezone = TimeZone.of(this.meta.timezone)

    // Current date and time in given timezone
    val nowDateTime = Clock.System.now().toLocalDateTime(timezone).truncateToMinute()
    val today = nowDateTime.date

    // Parse prayer times (LocalTime)
    val fajrTime = LocalTime.parse(timings.fajr.substringBefore(" ("))
    val sunriseTime = LocalTime.parse(timings.sunrise.substringBefore(" ("))
    val sunsetTime = LocalTime.parse(timings.sunset.substringBefore(" ("))
    val dhuhrTime = LocalTime.parse(timings.dhuhr.substringBefore(" ("))
    val asrTime = LocalTime.parse(timings.asr.substringBefore(" ("))
    val maghribTime = LocalTime.parse(timings.maghrib.substringBefore(" ("))
    val ishaTime = LocalTime.parse(timings.isha.substringBefore(" ("))
    val imsakTime = LocalTime.parse(timings.imsak.substringBefore(" ("))

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
    val hijriDate = this.date.hijri.day
        .plus(" ")
        .plus(this.date.hijri.month.en)
        .plus(" ").plus(this.date.hijri.year)

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
            latitude = this.meta.latitude,
            longitude = this.meta.longitude
        )
    )
}

fun PrayerTimesMonthlyResponse.toDto(): List<PrayerTimeDto>{
    return this.data.map {
        it.toDto()
    }
}

fun OpenFoodFactsResponse.toDto(): ProductDto {
    return ProductDto(
        brands = this.product?.brands,
        labels = this.product?.labels,
        labelsTags = this.product?.labelsTags,
        ingredientsText = this.product?.ingredients_text,
        imageUrl = this.product?.image_url,
        halalResult = HalalChecker.assessHalalStatus(this.product!!)
    )
}

fun isProductHalal(product: Product): Boolean {
    // labelsTags often contains "en:halal" or similar
    val tags = product.labelsTags?.map { it.lowercase() } ?: emptyList()
    if (tags.any { it.contains("halal") }) return true

    // labels textual
    val labels = product.labels?.lowercase()
    if (labels != null && labels.contains("halal")) return true

    // ingredients / product name fallback
    val ing = product.ingredients_text?.lowercase()
    if (ing != null && ing.contains("halal")) return true

    val name = product.productName?.lowercase()
    if (name != null && name.contains("halal")) return true

    return false
}

