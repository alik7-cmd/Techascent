package org.techascent.muslim.settings.state

import apphub.composeapp.generated.resources.Res
import apphub.composeapp.generated.resources.text_information
import apphub.composeapp.generated.resources.text_see_api_sub_title
import apphub.composeapp.generated.resources.text_see_api_title
import org.jetbrains.compose.resources.StringResource
import org.techascent.shared.data.enum.School

data class SettingsUiState(
    val school: School = School.HANAFI,
    val haptic: Boolean = true,
    val links: LinksUiModel
)

data class LinksUiModel(
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

internal fun getSettingsUiState()= SettingsUiState(
    links = LinksUiModel(
        title = Res.string.text_information,
        listOfElements = listOf(
            SettingsItem(
                navigationType = NavigationType.EXTERNAL,
                title = Res.string.text_see_api_title,
                subtitle = Res.string.text_see_api_sub_title,
            ),
        )
    )
)
