package org.techascent.shared.data

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class PrayerTimeMonthlyResponse(
    @SerialName("code")
    val code: Int?,
    @SerialName("data")
    val `data`: List<Data>?,
    @SerialName("status")
    val status: String?
)

@kotlinx.serialization.Serializable
data class Data(
    @SerialName("date")
    val date: Date?,
    @SerialName("meta")
    val meta: Meta?,
    @SerialName("timings")
    val timings: Timings?
)

@kotlinx.serialization.Serializable
data class Date(
    @SerialName("gregorian")
    val gregorian: Gregorian?,
    @SerialName("hijri")
    val hijri: Hijri?,
    @SerialName("readable")
    val readable: String?,
    @SerialName("timestamp")
    val timestamp: String?
)

@kotlinx.serialization.Serializable
data class Meta(
    @SerialName("latitude")
    val latitude: Double?,
    @SerialName("latitudeAdjustmentMethod")
    val latitudeAdjustmentMethod: String?,
    @SerialName("longitude")
    val longitude: Double?,
    @SerialName("method")
    val method: Method?,
    @SerialName("midnightMode")
    val midnightMode: String?,
    @SerialName("offset")
    val offset: Offset?,
    @SerialName("school")
    val school: String?,
    @SerialName("timezone")
    val timezone: String?
)

@kotlinx.serialization.Serializable
data class Gregorian(
    @SerialName("date")
    val date: String?,
    @SerialName("day")
    val day: String?,
    @SerialName("designation")
    val designation: Designation?,
    @SerialName("format")
    val format: String?,
    @SerialName("lunarSighting")
    val lunarSighting: Boolean?,
    @SerialName("month")
    val month: Month?,
    @SerialName("weekday")
    val weekday: Weekday?,
    @SerialName("year")
    val year: String?
)

@kotlinx.serialization.Serializable
data class Hijri(
    @SerialName("adjustedHolidays")
    val adjustedHolidays: List<String?>?,
    @SerialName("date")
    val date: String?,
    @SerialName("day")
    val day: String?,
    @SerialName("designation")
    val designation: Designation?,
    @SerialName("format")
    val format: String?,
    @SerialName("holidays")
    val holidays: List<String>?,
    @SerialName("method")
    val method: String?,
    @SerialName("month")
    val month: MonthX?,
    @SerialName("weekday")
    val weekday: WeekdayX?,
    @SerialName("year")
    val year: String?
)

@kotlinx.serialization.Serializable
data class Month(
    @SerialName("en")
    val en: String?,
    @SerialName("number")
    val number: Int?
)

@kotlinx.serialization.Serializable
data class MonthX(
    @SerialName("ar")
    val ar: String?,
    @SerialName("days")
    val days: Int?,
    @SerialName("en")
    val en: String?,
    @SerialName("number")
    val number: Int?
)

@kotlinx.serialization.Serializable
data class WeekdayX(
    @SerialName("ar")
    val ar: String?,
    @SerialName("en")
    val en: String?
)

@kotlinx.serialization.Serializable
data class Method(
    @SerialName("id")
    val id: Int?,
    @SerialName("location")
    val location: Location?,
    @SerialName("name")
    val name: String?,
    @SerialName("params")
    val params: Params?
)

@kotlinx.serialization.Serializable
data class Offset(
    @SerialName("Asr")
    val asr: Int?,
    @SerialName("Dhuhr")
    val dhuhr: Int?,
    @SerialName("Fajr")
    val fajr: Int?,
    @SerialName("Imsak")
    val imsak: Int?,
    @SerialName("Isha")
    val isha: Int?,
    @SerialName("Maghrib")
    val maghrib: Int?,
    @SerialName("Midnight")
    val midnight: Int?,
    @SerialName("Sunrise")
    val sunrise: Int?,
    @SerialName("Sunset")
    val sunset: Int?
)

@kotlinx.serialization.Serializable
data class Location(
    @SerialName("latitude")
    val latitude: Double?,
    @SerialName("longitude")
    val longitude: Double?
)

@kotlinx.serialization.Serializable
data class Params(
    @SerialName("Fajr")
    val fajr: Int?,
    @SerialName("Isha")
    val isha: Int?
)