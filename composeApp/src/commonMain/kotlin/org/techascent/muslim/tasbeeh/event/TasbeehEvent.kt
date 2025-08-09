package org.techascent.muslim.tasbeeh.event

sealed interface TasbeehEvent {
    data object NavigateBack : TasbeehEvent
}