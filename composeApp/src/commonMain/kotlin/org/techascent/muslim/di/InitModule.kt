package org.techascent.muslim.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.techascent.muslim.calendar.CalendarViewModel
import org.techascent.muslim.city.CityPickerViewModel
import org.techascent.muslim.common.location.LocationService
import org.techascent.muslim.compass.CompassViewModel
import org.techascent.muslim.getPlatformLocationService
import org.techascent.muslim.method.MethodViewModel
import org.techascent.muslim.prayer.PrayerTimeViewModel
import org.techascent.muslim.provideDataStore
import org.techascent.muslim.settings.SettingsViewModel
import org.techascent.muslim.tasbeeh.TasbeehViewModel
import org.techascent.shared.di.prayerModule

val appModule = module {
    single<DataStore<Preferences>> { provideDataStore() }
    single<LocationService> { getPlatformLocationService() }
    viewModel { PrayerTimeViewModel(repository = get(), locationService = get()) }
    viewModel { CalendarViewModel(repository = get(), locationService = get()) }
    viewModel { TasbeehViewModel() }
    viewModel { MethodViewModel() }
    viewModel { SettingsViewModel() }
    /*viewModel { LocationPickerViewModel(controller = get ()) }*/
    viewModel { CompassViewModel() }
    viewModel { CityPickerViewModel() }
}

fun initializeKoin() {
    startKoin {
        modules(prayerModule)
        modules(appModule)
    }
}