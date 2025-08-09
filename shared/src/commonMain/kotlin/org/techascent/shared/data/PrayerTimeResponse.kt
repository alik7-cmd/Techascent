package org.techascent.shared.data

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class PrayerTimesResponse(
    val code: Int,
    val status: String,
    val data: PrayerData
)

@kotlinx.serialization.Serializable
data class PrayerData(
    val timings: Timings,
    val date: DateInfo,
    val meta: Meta
)

@kotlinx.serialization.Serializable
data class Timings(
    @SerialName("Fajr")
    val fajr: String,
    @SerialName("Sunrise")
    val sunrise: String,
    @SerialName("Dhuhr")
    val dhuhr: String,
    @SerialName("Asr")
    val asr: String,
    @SerialName("Sunset")
    val sunset: String,
    @SerialName("Maghrib")
    val maghrib: String,
    @SerialName("Isha")
    val isha: String,
    @SerialName("Imsak")
    val imsak: String,
    @SerialName("Midnight")
    val midnight: String,
    @SerialName("Firstthird")
    val firstThird: String,
    @SerialName("Lastthird")
    val lastThird: String
)

@kotlinx.serialization.Serializable
data class DateInfo(
    val readable: String,
    val timestamp: String,
    val hijri: HijriDate,
    val gregorian: GregorianDate
)

@kotlinx.serialization.Serializable
data class HijriDate(
    val date: String,
    val format: String,
    val day: String,
    val weekday: Weekday,
    val month: HijriMonth,
    val year: String,
    val designation: Designation,
    val holidays: List<String>,
    val adjustedHolidays: List<String>,
    val method: String
)

@kotlinx.serialization.Serializable
data class GregorianDate(
    val date: String,
    val format: String,
    val day: String,
    val weekday: Weekday,
    val month: GregorianMonth,
    val year: String,
    val designation: Designation,
    val lunarSighting: Boolean
)

@kotlinx.serialization.Serializable
data class Weekday(val en: String)

@kotlinx.serialization.Serializable
data class HijriMonth(val number: Int, val en: String, val ar: String, val days: Int)

@kotlinx.serialization.Serializable
data class GregorianMonth(val number: Int, val en: String)

@kotlinx.serialization.Serializable
data class Designation(val abbreviated: String, val expanded: String)

@kotlinx.serialization.Serializable
data class Meta(
    val latitude: Double,
    val longitude: Double,
    val timezone: String,
    val method: CalculationMethod,
    val latitudeAdjustmentMethod: String,
    val midnightMode: String,
    val school: String,
    val offset: Map<String, Int>
)

@kotlinx.serialization.Serializable
data class CalculationMethod(
    val id: Int,
    val name: String,
    val params: Map<String, Int>,
    val location: Location
)

@kotlinx.serialization.Serializable
data class Location(
    val latitude: Double,
    val longitude: Double
)
