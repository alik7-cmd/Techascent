package org.techascent.shared.data.enum

enum class PrayerCalculationMethod(val code: Int) {
    JAFARI(0),
    KARACHI(1),
    ISNA(2),
    MWL(3),
    MAKKAH(4),
    EGYPT(5),
    TEHRAN(7),
    GULF(8),
    KUWAIT(9),
    QATAR(10),
    SINGAPORE(11),
    FRANCE(12),
    TURKEY(13),
    RUSSIA(14),
    MOONSIGHTING(15),
    DUBAI(16),
    MALAYSIA(17),
    TUNISIA(18),
    ALGERIA(19),
    INDONESIA(20),
    MOROCCO(21),
    PORTUGAL(22),
    JORDAN(23),
    CUSTOM(99);
}
internal fun PrayerCalculationMethod.toCode(): Int = this.code
