package org.techascent.composa.button.primary

import org.techascent.composa.common.DrawableData

sealed class ButtonState(open val iconData: DrawableData?) {
    data object Idle : ButtonState(null)
    data object Loading : ButtonState(null)
    data class Success(override val iconData: DrawableData) : ButtonState(iconData)
    data class Failed(override val iconData: DrawableData) : ButtonState(iconData)
}
