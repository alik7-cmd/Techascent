package org.techascent.shared.data.common

import kotlinx.serialization.Serializable

/**
 * Domain-level prayer name enum.
 * This is the shared module's version — used for caching, notifications, and business logic.
 * The composeApp module maps this to display strings (StringResource).
 */
@Serializable
enum class PrayerNameEnum {
    FAJR,
    SALAT_UD_DUHA,
    DUHR,
    ASR,
    MAGHRIB,
    ISHA
}

