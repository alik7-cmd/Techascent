package org.techascent.muslim.settings.event

sealed interface SettingsEvent {
    data class OpenExternalLink(val url: String) : SettingsEvent
}