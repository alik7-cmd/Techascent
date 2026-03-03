package org.techascent.muslim.home

import apphub.composeapp.generated.resources.Res
import apphub.composeapp.generated.resources.ic_tab_explore
import apphub.composeapp.generated.resources.ic_tab_explore_filled
import apphub.composeapp.generated.resources.ic_tab_prayer
import apphub.composeapp.generated.resources.ic_tab_prayer_filled
import apphub.composeapp.generated.resources.ic_tab_settings
import apphub.composeapp.generated.resources.ic_tab_settings_filled
import apphub.composeapp.generated.resources.tab_explore
import apphub.composeapp.generated.resources.tab_prayer
import apphub.composeapp.generated.resources.tab_settings
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.techascent.composa.common.ComposaSpacing
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
        unSelectedIcon = Res.drawable.ic_tab_prayer,
        selectedIcon = Res.drawable.ic_tab_prayer_filled,
        title = Res.string.tab_prayer,
        route = Screen.PrayerView.route,
    ),
    NavigationItem(
        unSelectedIcon = Res.drawable.ic_tab_explore,
        selectedIcon = Res.drawable.ic_tab_explore_filled,
        title = Res.string.tab_explore,
        route = Screen.UtilityView.route,
    ),
    NavigationItem(
        unSelectedIcon = Res.drawable.ic_tab_settings,
        selectedIcon = Res.drawable.ic_tab_settings_filled,
        title = Res.string.tab_settings,
        route = Screen.SettingsView.route,
    ),
)

@Composable
fun BottomNavigationBar(
    items: List<NavigationItem>,
    currentRoute: String?,
    onItemClick: (NavigationItem) -> Unit
) {
    val selectedColor = ComposaTheme.color.backgroundAction
    val unselectedColor = ComposaTheme.color.textNeutralSubtle
    val indicatorColor = ComposaTheme.color.backgroundActionSubtle

    NavigationBar(
        modifier = Modifier.fillMaxWidth(),
        containerColor = ComposaTheme.color.backgroundAppBackground,
        tonalElevation = 0.dp,
    ) {
        items.forEach { navigationItem ->
            val isSelected = currentRoute == navigationItem.route
            NavigationBarItem(
                selected = isSelected,
                onClick = { onItemClick(navigationItem) },
                icon = {
                    ComposaIcon(
                        modifier = Modifier.size(22.dp),
                        icon = DrawableData(
                            imageRes = if (isSelected) navigationItem.selectedIcon else navigationItem.unSelectedIcon,
                            tint = if (isSelected) selectedColor else unselectedColor,
                        )
                    )
                },
                label = {
                    Text(
                        text = stringResource(navigationItem.title),
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) selectedColor else unselectedColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = selectedColor,
                    unselectedIconColor = unselectedColor,
                    selectedTextColor = selectedColor,
                    unselectedTextColor = unselectedColor,
                    indicatorColor = indicatorColor,
                ),
                alwaysShowLabel = true,
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
    val selectedColor = ComposaTheme.color.backgroundAction
    val unselectedColor = ComposaTheme.color.textNeutralSubtle
    val indicatorColor = ComposaTheme.color.backgroundActionSubtle

    NavigationRail(
        modifier = Modifier.fillMaxHeight(),
        containerColor = ComposaTheme.color.backgroundAppBackground,
    ) {
        items.forEach { navigationItem ->
            val isSelected = navigationItem.route == currentRoute
            NavigationRailItem(
                selected = isSelected,
                onClick = { onItemClick(navigationItem) },
                icon = {
                    ComposaIcon(
                        modifier = Modifier.size(22.dp),
                        icon = DrawableData(
                            imageRes = if (isSelected) navigationItem.selectedIcon else navigationItem.unSelectedIcon,
                            tint = if (isSelected) selectedColor else unselectedColor,
                        )
                    )
                },
                modifier = Modifier.padding(vertical = ComposaSpacing.Medium),
                label = {
                    Text(
                        text = stringResource(navigationItem.title),
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) selectedColor else unselectedColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = selectedColor,
                    unselectedIconColor = unselectedColor,
                    selectedTextColor = selectedColor,
                    unselectedTextColor = unselectedColor,
                    indicatorColor = indicatorColor,
                ),
                alwaysShowLabel = true,
            )
        }
    }
}