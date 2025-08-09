package org.techascent.muslim.settings.state

import apphub.composeapp.generated.resources.Res
import apphub.composeapp.generated.resources.ic_ecternal_link
import apphub.composeapp.generated.resources.icon_internal_navigation
import apphub.composeapp.generated.resources.sub_title_rate_us
import apphub.composeapp.generated.resources.sub_title_widgets
import apphub.composeapp.generated.resources.title_about_us
import apphub.composeapp.generated.resources.title_appearance
import apphub.composeapp.generated.resources.title_language
import apphub.composeapp.generated.resources.title_quibla
import apphub.composeapp.generated.resources.title_rate_us
import apphub.composeapp.generated.resources.title_widgets
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

data class SettingsUiState(
    val appearanceUiModel: AppearanceUiModel,
    val aboutUsUiModel: AboutUsUiModel
)

data class AppearanceUiModel(
    val title: StringResource,
    val listOfElements: List<SettingsItem>
)

data class AboutUsUiModel(
    val title: StringResource,
    val listOfElements: List<SettingsItem>
)

data class SettingsItem(
    val navigationType: NavigationType,
    val title: StringResource,
    val subtitle: StringResource? = null,
    /*val icon: DrawableResource,*/
)

enum class NavigationType {
    INTERNAL, EXTERNAL
}

internal fun getSettingsUiState() = SettingsUiState(
    appearanceUiModel = AppearanceUiModel(
        title = Res.string.title_appearance,
        listOfElements = listOf(
            SettingsItem(
                navigationType = NavigationType.INTERNAL,
                title = Res.string.title_quibla,
                /*icon = Res.drawable.icon_internal_navigation*/
            ),

            SettingsItem(
                navigationType = NavigationType.INTERNAL,
                title = Res.string.title_widgets,
               /* icon = Res.drawable.icon_internal_navigation*/
            ),

            SettingsItem(
                navigationType = NavigationType.EXTERNAL,
                title = Res.string.title_language,
                subtitle = Res.string.sub_title_widgets,
               /* icon = Res.drawable.ic_ecternal_link*/
            ),
        )
    ),
    aboutUsUiModel = AboutUsUiModel(
        title = Res.string.title_appearance,
        listOfElements = listOf(
            SettingsItem(
                navigationType = NavigationType.INTERNAL,
                title = Res.string.title_about_us,
                /*icon = Res.drawable.icon_internal_navigation*/
            ),

            SettingsItem(
                navigationType = NavigationType.EXTERNAL,
                title = Res.string.title_rate_us,
                subtitle = Res.string.sub_title_rate_us,
                /*icon = Res.drawable.icon_internal_navigation*/
            ),
        )
    )
)
