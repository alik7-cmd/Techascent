package org.techascent.muslim.home

import apphub.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.DrawableResource
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import apphub.composeapp.generated.resources.ic_calendar
import apphub.composeapp.generated.resources.ic_prayer
import apphub.composeapp.generated.resources.title_calendar
import apphub.composeapp.generated.resources.title_home
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.techascent.composa.common.DrawableData
import org.techascent.composa.icon.ComposaIcon
import org.techascent.composa.theming.ComposaTheme
import org.techascent.muslim.navigation.Screen

data class NavigationItem(
    val unSelectedIcon: DrawableResource,
    val selectedIcon: DrawableResource,
    val title: StringResource,
    val route: String
)

val navigationItemsLists = listOf(
    NavigationItem(
        unSelectedIcon = Res.drawable.ic_prayer,
        selectedIcon = Res.drawable.ic_prayer,
        title = Res.string.title_home,
        route = Screen.PrayerView.route,
    ),
    NavigationItem(
        unSelectedIcon = Res.drawable.ic_calendar,
        selectedIcon = Res.drawable.ic_calendar,
        title = Res.string.title_calendar,
        route = Screen.CalendarView.route,
    ),
    /*NavigationItem(
        unSelectedIcon = Res.drawable.ic_settings_2,
        selectedIcon = Res.drawable.ic_settings_2,
        title = Res.string.title_settings,
        route = Screen.SettingsView.route,
    ),*/
)

@Composable
fun BottomNavigationBar(
    items: List<NavigationItem>,
    currentRoute: String?,
    onItemClick: (NavigationItem) -> Unit
) {
    NavigationBar(
        modifier = Modifier.fillMaxWidth(),
    ) {
        items.forEach { navigationItem ->
            NavigationBarItem(
                selected = currentRoute == navigationItem.route,
                onClick = { onItemClick(navigationItem) },
                icon = {
                    ComposaIcon(
                        modifier = Modifier.size(24.dp),
                        icon = DrawableData(
                            imageRes = if (navigationItem.route == currentRoute) navigationItem.selectedIcon else navigationItem.unSelectedIcon,
                            tint = ComposaTheme.color.textNeutral,
                        )
                    )
                },
                label = {
                    Text(
                        text = stringResource(navigationItem.title),
                        style = if (navigationItem.route == currentRoute) MaterialTheme.typography.labelLarge
                        else MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
            )
        }
    }
}

@Composable
fun NavigationSideBar(
    items: List<NavigationItem>,
    currentRoute: String?,
    onItemClick: (NavigationItem) -> Unit
) {
    NavigationRail(
        modifier = Modifier.fillMaxHeight(),
        containerColor = ComposaTheme.color.backgroundAppBackground,
    ) {
        items.forEach { navigationItem ->
            NavigationRailItem(
                selected = navigationItem.route == currentRoute,
                onClick = { onItemClick(navigationItem) },
                icon = {
                    ComposaIcon(
                        modifier = Modifier.size(24.dp),
                        icon = DrawableData(
                            imageRes = if (navigationItem.route == currentRoute) navigationItem.selectedIcon else navigationItem.unSelectedIcon,
                            tint = ComposaTheme.color.textNeutral,
                        )
                    )
                },
                modifier = Modifier.padding(vertical = 12.dp),
                label = {
                    Text(
                        text = stringResource(navigationItem.title),
                        style = if (navigationItem.route == currentRoute) MaterialTheme.typography.labelLarge
                        else MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
            )
        }
    }
}