@file:OptIn(kotlin.time.ExperimentalTime::class)

package org.techascent.muslim.prayer.uimodel

import apphub.composeapp.generated.resources.Res
import apphub.composeapp.generated.resources.img_asr
import apphub.composeapp.generated.resources.img_dhuhr
import apphub.composeapp.generated.resources.img_fajr
import apphub.composeapp.generated.resources.img_isha
import apphub.composeapp.generated.resources.img_maghrib
import apphub.composeapp.generated.resources.text_asr
import apphub.composeapp.generated.resources.text_dhuhr
import apphub.composeapp.generated.resources.text_fajr
import apphub.composeapp.generated.resources.text_isha
import apphub.composeapp.generated.resources.text_maghrib
import apphub.composeapp.generated.resources.text_salat_ud_duha
import kotlinx.datetime.Clock.System
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.techascent.muslim.common.toHourMinuteString
import org.techascent.muslim.common.toReadableDate
import org.techascent.muslim.getPlaceName
import org.techascent.shared.data.dto.PrayerName
import org.techascent.shared.data.dto.PrayerTimeDto
import org.techascent.shared.data.dto.PrayerTimeInterval
import org.techascent.shared.data.enum.School
import kotlin.collections.map


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
)

@Serializable
data class IftarTimeUiModel(
    val iftarStartTime: String?,
    val lastTimeOfSahri: String?
)

@Serializable
data class AddressInfo(
    val district: String?,
    val city: String?,
    val country: String?,
    val address: String
)

@Serializable
enum class PrayerNameEnum {
    FAJR,
    SALAT_UD_DUHA,
    DUHR,
    ASR,
    MAGHRIB,
    ISHA
}

internal suspend fun PrayerTimeDto.toUiModel(
    school: School
): PrayerTimeUiModel {
    return PrayerTimeUiModel(
        intervals = intervals.map { it.toUiModel() },
        currentPrayer = currentPrayer?.toUiModel(),
        hijriDate = hijriDate,
        sunrise = sunrise.toHourMinuteString(false),
        sunset = sunset.toHourMinuteString(false),
        currentDateTime = System.now().toEpochMilliseconds().toReadableDate(),
        iftarTime = IftarTimeUiModel(
            iftarStartTime = iftarTime?.startTime?.toHourMinuteString(false),
            lastTimeOfSahri = iftarTime?.endTime?.toHourMinuteString(false),
        ),
        addressInfo = getPlaceName(location.latitude, location.longitude),
        prayerImage = getImageByPrayer(currentPrayer?.name),
        school = school
        //currentWaqtEnd = currentPrayer?.endTime?.toInstant(TimeZone.currentSystemDefault())
    )

}

private fun PrayerTimeInterval.toUiModel(): PrayerTimeIntervalModel {
    return PrayerTimeIntervalModel(
        name = name.toPrayerNameEnum(),
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

fun getImageByPrayer(name: PrayerName?) = when (name) {
    PrayerName.FAJR, PrayerName.SALAT_UD_DUHA -> "${BASE}img_fajr.webp"
    PrayerName.DUHR -> "${BASE}img_dhuhr.webp"
    PrayerName.ASR -> "${BASE}img_asr.webp"
    PrayerName.MAGHRIB -> "${BASE}img_maghrib.webp"
    PrayerName.ISHA -> "${BASE}img_isha.webp"
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

fun PrayerNameEnum.toDisplayImageRes(): DrawableResource {
    return when (this) {
        PrayerNameEnum.FAJR -> Res.drawable.img_fajr
        PrayerNameEnum.DUHR -> Res.drawable.img_dhuhr
        PrayerNameEnum.ASR -> Res.drawable.img_asr
        PrayerNameEnum.MAGHRIB -> Res.drawable.img_maghrib
        PrayerNameEnum.ISHA -> Res.drawable.img_isha
        else -> Res.drawable.img_asr
    }
}