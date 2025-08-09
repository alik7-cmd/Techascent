package org.techascent.muslim.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import org.techascent.muslim.calendar.CalendarView
import org.techascent.muslim.compass.CompassView
import org.techascent.muslim.location.LocationPickerView
import org.techascent.muslim.prayer.PrayerView
import org.techascent.muslim.settings.SettingsView
import org.techascent.muslim.tasbeeh.TasbeehView

private const val LOCATION_PICKER_VIEW = "LOCATION_PICKER_VIEW"
private const val HOME_GRAPH = "HOME_GRAPH"
private const val PRAYER_VIEW = "PRAYER_VIEW"
private const val CALENDAR_VIEW = "CALENDAR_VIEW"
private const val SETTINGS_VIEW = "SETTINGS_VIEW"
private const val TASBEEH_VIEW = "TASBEEH_VIEW"
private const val COMPASS_VIEW = "COMPASS_VIEW"

sealed class Screen(val route: String) {
    data object LocationPickerView : Screen(route = LOCATION_PICKER_VIEW)
    data object HomeGraph : Screen(route = HOME_GRAPH)
    data object PrayerView : Screen(route = PRAYER_VIEW)
    data object CalendarView : Screen(route = CALENDAR_VIEW)
    data object SettingsView : Screen(route = SETTINGS_VIEW)
    data object TasbeehView : Screen(route = TASBEEH_VIEW)
    data object CompassView : Screen(route = COMPASS_VIEW)
}

@Composable
fun RootNavGraph(
    rootNavController: NavHostController,
    innerPadding: PaddingValues
) {
    NavHost(
        navController = rootNavController,
        startDestination = Screen.LocationPickerView.route
    ) {
        composable(Screen.LocationPickerView.route) {
            LocationPickerView(
                onNavigatePrayerView = {
                    rootNavController.navigate(Screen.HomeGraph.route) {
                        popUpTo(Screen.LocationPickerView.route) { inclusive = true }
                    }
                }
            )
        }
        mainNavGraph(rootNavController = rootNavController, innerPadding = innerPadding)
        composable(
            route = Screen.TasbeehView.route,
        ) {
            TasbeehView(
                onNavigateBack = { rootNavController.popBackStack() }
            )
        }

        composable(Screen.CompassView.route) {
            CompassView(
                onNavigateBack = { rootNavController.popBackStack() }
            )
        }
    }
}

fun NavGraphBuilder.mainNavGraph(
    rootNavController: NavHostController,
    innerPadding: PaddingValues
) {
    navigation(
        startDestination = Screen.PrayerView.route,
        route = Screen.HomeGraph.route
    ) {
        composable(route = Screen.PrayerView.route) {
            PrayerView(
                onNavigateToTasbeeh = { rootNavController.navigate(Screen.TasbeehView.route) },
                onNavigateToCompass = { rootNavController.navigate(Screen.CompassView.route) },
                innerPadding = innerPadding
            )
        }
        composable(route = Screen.CalendarView.route) {
            CalendarView()
        }
        /*composable(route = Screen.SettingsView.route) {
            SettingsView(
                innerPadding = innerPadding
            )
        }*/
    }
}