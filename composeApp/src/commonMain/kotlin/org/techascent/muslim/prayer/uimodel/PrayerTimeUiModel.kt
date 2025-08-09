package org.techascent.muslim.prayer.uimodel

import apphub.composeapp.generated.resources.Res
import apphub.composeapp.generated.resources.text_asr
import apphub.composeapp.generated.resources.text_dhuhr
import apphub.composeapp.generated.resources.text_fajr
import apphub.composeapp.generated.resources.text_isha
import apphub.composeapp.generated.resources.text_maghrib
import apphub.composeapp.generated.resources.text_salat_ud_duha
import apphub.composeapp.generated.resources.warning_prayer_time
import kotlinx.datetime.Clock.System
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.jetbrains.compose.resources.StringResource
import org.techascent.muslim.common.toHourMinuteString
import org.techascent.muslim.common.toReadableDate
import org.techascent.muslim.getPlaceName
import org.techascent.shared.data.dto.PrayerName
import org.techascent.shared.data.dto.PrayerTimeDto
import org.techascent.shared.data.dto.PrayerTimeInterval
import kotlin.collections.map

data class PrayerTimeUiModel(
    val intervals: List<PrayerTimeIntervalModel>,
    val currentPrayer: PrayerTimeIntervalModel?,
    val hijriDate: String,
    val iftarTime: IftarTimeUiModel?,
    val warningMessage: StringResource,
    val sunrise: String,
    val sunset: String,
    val currentDateTime: String,
    val apiUrl: String = "https://aladhan.com/about",
    val placeName: String
)

data class PrayerTimeIntervalModel(
    val name: StringResource,
    val displayableStartTime: String,
    val displayableEndTime: String,
    val startTimeInstant: Instant? = null,
    val endTimeInstant: Instant? = null,
)

data class IftarTimeUiModel(
    val iftarStartTime: String?,
    val lastTimeOfSahri: String?
)

internal suspend fun PrayerTimeDto.toUiModel(): PrayerTimeUiModel {
    return PrayerTimeUiModel(
        intervals = intervals.map { it.toUiModel() },
        currentPrayer = currentPrayer?.toUiModel(),
        hijriDate = hijriDate,
        warningMessage = Res.string.warning_prayer_time,
        sunrise = sunrise.toHourMinuteString(false),
        sunset = sunset.toHourMinuteString(false),
        currentDateTime = System.now().toEpochMilliseconds().toReadableDate(),
        iftarTime = IftarTimeUiModel(
            iftarStartTime = iftarTime?.startTime?.toHourMinuteString(false),
            lastTimeOfSahri = iftarTime?.endTime?.toHourMinuteString(false),
        ),
        placeName = getPlaceName(location.latitude, location.longitude)
        //currentWaqtEnd = currentPrayer?.endTime?.toInstant(TimeZone.currentSystemDefault())
    )

}

private fun PrayerTimeInterval.toUiModel(): PrayerTimeIntervalModel {
    return PrayerTimeIntervalModel(
        name = name.toDisplayString(),
        displayableStartTime = startTime.toHourMinuteString(false),
        displayableEndTime = endTime.toHourMinuteString(false),
        startTimeInstant = startTime.toInstant(TimeZone.currentSystemDefault()),
        endTimeInstant = endTime.toInstant(TimeZone.currentSystemDefault())
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