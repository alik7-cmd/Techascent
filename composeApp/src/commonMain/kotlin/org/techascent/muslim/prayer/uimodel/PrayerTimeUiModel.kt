@file:OptIn(kotlin.time.ExperimentalTime::class)

package org.techascent.muslim.prayer.uimodel

import apphub.composeapp.generated.resources.Res
import apphub.composeapp.generated.resources.text_asr
import apphub.composeapp.generated.resources.text_dhuhr
import apphub.composeapp.generated.resources.text_fajr
import apphub.composeapp.generated.resources.text_isha
import apphub.composeapp.generated.resources.text_maghrib
import apphub.composeapp.generated.resources.text_salat_ud_duha
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource
import org.techascent.muslim.getPlaceName
import org.techascent.shared.data.common.toFormattedTimeString
import org.techascent.shared.data.common.toHourMinuteString
import org.techascent.shared.data.dto.PrayerName
import org.techascent.shared.data.dto.PrayerTimeDto
import org.techascent.shared.data.dto.PrayerTimeInterval
import org.techascent.shared.data.enum.School

/**
 * Re-export shared domain types so existing composeApp imports still work.
 * New code should import directly from org.techascent.shared.data.common.
 */
typealias AddressInfo = org.techascent.shared.data.common.AddressInfo
typealias PrayerNameEnum = org.techascent.shared.data.common.PrayerNameEnum

private const val BASE =
    "https://raw.githubusercontent.com/fbehsaan/Images/main/"

@Serializable
data class PrayerTimeUiModel(
    val intervals: List<PrayerTimeIntervalModel>,
    val currentPrayer: PrayerTimeIntervalModel?,
    val hijriDate: String,
    val iftarTime: IftarTimeUiModel?,
    val sunrise: String,
    val sunset: String,
    val sunriseInstant: Instant? = null,
    val sunsetInstant: Instant? = null,
    val currentDateTime: String,
    val apiUrl: String = "https://aladhan.com/about",
    val addressInfo: AddressInfo,
    val prayerImage: String,
    val school: School = School.HANAFI
)

@Serializable
data class PrayerTimeIntervalModel(
    val name: PrayerNameEnum,
    val displayableStartTime: String,
    val displayableEndTime: String,
    val startTimeInstant: Instant? = null,
    val endTimeInstant: Instant? = null,
    val shouldNotify: Boolean = false
)

@Serializable
data class IftarTimeUiModel(
    val iftarStartTime: String?,
    val lastTimeOfSahri: String?,
    val iftarInstant: Instant? = null,
    val sahriInstant: Instant? = null,
)

internal suspend fun PrayerTimeDto.toUiModel(
    school: School,
): PrayerTimeUiModel {
    return PrayerTimeUiModel(
        intervals = intervals.map { it.toUiModel() },
        currentPrayer = currentPrayer?.toUiModel(),
        hijriDate = hijriDate,
        sunrise = sunrise.toHourMinuteString(is24HourFormat = true),
        sunset = sunset.toHourMinuteString(is24HourFormat = true),
        sunriseInstant = sunrise.toInstant(TimeZone.currentSystemDefault()),
        sunsetInstant = sunset.toInstant(TimeZone.currentSystemDefault()),
        currentDateTime = currentDateTime,
        iftarTime = IftarTimeUiModel(
            iftarStartTime = iftarTime?.startTime?.toHourMinuteString(is24HourFormat = true),
            lastTimeOfSahri = iftarTime?.endTime?.toHourMinuteString(is24HourFormat = true),
            iftarInstant = iftarTime?.startTime?.toInstant(TimeZone.currentSystemDefault()),
            sahriInstant = iftarTime?.endTime?.toInstant(TimeZone.currentSystemDefault()),
        ),
        addressInfo = getPlaceName(location.latitude, location.longitude),
        prayerImage = getImageByPrayer(currentPrayer?.name),
        school = school
    )

}

private fun PrayerTimeInterval.toUiModel(): PrayerTimeIntervalModel {
    return PrayerTimeIntervalModel(
        name = name.toPrayerNameEnum(),
        displayableStartTime = startTime.toHourMinuteString(is24HourFormat = true),
        displayableEndTime = endTime.toHourMinuteString(is24HourFormat = true),
        startTimeInstant = startTime.toInstant(TimeZone.currentSystemDefault()),
        endTimeInstant = endTime.toInstant(TimeZone.currentSystemDefault())
    )
}

// ── Format for display based on user's 24hr preference ──────────────────────────

/**
 * Re-formats all time display strings from the stored [Instant] fields
 * based on the user's preferred time format.
 * This allows caching time data in a format-agnostic way (always 24hr)
 * and only formatting for display when rendering UI.
 */
fun PrayerTimeUiModel.formatForDisplay(is24HourFormat: Boolean): PrayerTimeUiModel {
    return copy(
        intervals = intervals.map { it.formatForDisplay(is24HourFormat) },
        currentPrayer = currentPrayer?.formatForDisplay(is24HourFormat),
        sunrise = sunriseInstant?.toFormattedTimeString(is24HourFormat) ?: sunrise,
        sunset = sunsetInstant?.toFormattedTimeString(is24HourFormat) ?: sunset,
        iftarTime = iftarTime?.formatForDisplay(is24HourFormat),
    )
}

fun PrayerTimeIntervalModel.formatForDisplay(is24HourFormat: Boolean): PrayerTimeIntervalModel {
    return copy(
        displayableStartTime = startTimeInstant?.toFormattedTimeString(is24HourFormat)
            ?: displayableStartTime,
        displayableEndTime = endTimeInstant?.toFormattedTimeString(is24HourFormat)
            ?: displayableEndTime,
    )
}

fun IftarTimeUiModel.formatForDisplay(is24HourFormat: Boolean): IftarTimeUiModel {
    return copy(
        iftarStartTime = iftarInstant?.toFormattedTimeString(is24HourFormat) ?: iftarStartTime,
        lastTimeOfSahri = sahriInstant?.toFormattedTimeString(is24HourFormat) ?: lastTimeOfSahri,
    )
}

fun PrayerName.toDisplayString(): StringResource {
    return when (this) {
        PrayerName.FAJR -> Res.string.text_fajr
        PrayerName.SALAT_UD_DUHA -> Res.string.text_salat_ud_duha
        PrayerName.DUHR -> Res.string.text_dhuhr
        PrayerName.ASR -> Res.string.text_asr
        PrayerName.MAGHRIB -> Res.string.text_maghrib
        PrayerName.ISHA -> Res.string.text_isha
    }
}

fun getImageByPrayer(name: PrayerName?) = when (name) {
    PrayerName.FAJR, PrayerName.SALAT_UD_DUHA -> "${BASE}img_fajr.webp"
    PrayerName.DUHR -> "${BASE}img_dhuhr.webp"
    PrayerName.ASR -> "${BASE}img_asr.webp"
    PrayerName.MAGHRIB -> "${BASE}img_maghrib.webp"
    PrayerName.ISHA -> "${BASE}img_isha.webp"
    null -> "${BASE}img_fajr.webp"
}

fun getImageByPrayerEnum(name: PrayerNameEnum?) = when (name) {
    PrayerNameEnum.FAJR, PrayerNameEnum.SALAT_UD_DUHA -> "${BASE}img_fajr.webp"
    PrayerNameEnum.DUHR -> "${BASE}img_dhuhr.webp"
    PrayerNameEnum.ASR -> "${BASE}img_asr.webp"
    PrayerNameEnum.MAGHRIB -> "${BASE}img_maghrib.webp"
    PrayerNameEnum.ISHA -> "${BASE}img_isha.webp"
    null -> "${BASE}img_fajr.webp"
}

fun PrayerName.toPrayerNameEnum(): PrayerNameEnum {
    return when (this) {
        PrayerName.FAJR -> PrayerNameEnum.FAJR
        PrayerName.SALAT_UD_DUHA -> PrayerNameEnum.SALAT_UD_DUHA
        PrayerName.DUHR -> PrayerNameEnum.DUHR
        PrayerName.ASR -> PrayerNameEnum.ASR
        PrayerName.MAGHRIB -> PrayerNameEnum.MAGHRIB
        PrayerName.ISHA -> PrayerNameEnum.ISHA
    }
}

fun PrayerNameEnum.toDisplayString(): StringResource {
    return when (this) {
        PrayerNameEnum.FAJR -> Res.string.text_fajr
        PrayerNameEnum.SALAT_UD_DUHA -> Res.string.text_salat_ud_duha
        PrayerNameEnum.DUHR -> Res.string.text_dhuhr
        PrayerNameEnum.ASR -> Res.string.text_asr
        PrayerNameEnum.MAGHRIB -> Res.string.text_maghrib
        PrayerNameEnum.ISHA -> Res.string.text_isha
    }
}