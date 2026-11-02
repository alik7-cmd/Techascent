package org.techascent.aifinder.presentation.welcome

import apphub.aifinder.generated.resources.Res
import apphub.aifinder.generated.resources.get_started
import apphub.aifinder.generated.resources.ic_full_box
import apphub.aifinder.generated.resources.welcome_description
import apphub.aifinder.generated.resources.welcome_title
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.techascent.aifinder.domain.auth.WelcomeDestination


sealed interface WelcomeUiState {
    data object Loading : WelcomeUiState
    data class Success(val data: WelcomeContent) : WelcomeUiState
    data class Error(val message: String) : WelcomeUiState
    data class Navigate(val destination: WelcomeDestination) : WelcomeUiState
}

data class WelcomeContent(
    val boxRes: DrawableResource = Res.drawable.ic_full_box,
    val welcomeTitleRes: StringResource = Res.string.welcome_title,
    val descriptionRes: StringResource = Res.string.welcome_description,
    val buttonTextRes: StringResource = Res.string.get_started
)
