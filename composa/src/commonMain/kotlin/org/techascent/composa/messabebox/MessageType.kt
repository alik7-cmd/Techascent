package org.techascent.composa.messabebox


sealed interface MessageType {
    data object Info : MessageType
    data object Warning : MessageType
    data object Error : MessageType
}
