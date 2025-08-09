package org.techascent.muslim.prayer.event

sealed interface PrayerTimeEvent{
    data class OpenExternalLink(val url: String): PrayerTimeEvent

}