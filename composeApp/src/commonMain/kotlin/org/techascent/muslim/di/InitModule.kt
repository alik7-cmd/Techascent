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
import org.techascent.muslim.halalscanner.HalalScannerViewModel
import org.techascent.muslim.method.MethodViewModel
import org.techascent.muslim.prayer.PrayerTimeViewModel
import org.techascent.muslim.prayer.cache.PrayerTimeCache
import org.techascent.muslim.prayer.cache.PrayerTimeCacheImpl
import org.techascent.muslim.prayer.location.AddressResolver
import org.techascent.muslim.prayer.location.AddressResolverImpl
import org.techascent.muslim.prayer.usecase.PrayerNotificationUseCase
import org.techascent.muslim.prayer.usecase.PrayerTimeViewUseCase
import org.techascent.muslim.provideDataStore
import org.techascent.muslim.quran.QuranViewModel
import org.techascent.muslim.settings.SettingsViewModel
import org.techascent.muslim.tasbeeh.TasbeehViewModel
import org.techascent.muslim.utility.FeatureUsageRepository
import org.techascent.muslim.utility.UtilityViewModel
import org.techascent.shared.di.prayerModule

val appModule = module {
    single<DataStore<Preferences>> { provideDataStore() }
    single<LocationService> { getPlatformLocationService() }
    single { FeatureUsageRepository(dataStore = get()) }

    // ── Prayer data layer ────────────────────────────────────────────────
    single<PrayerTimeCache> { PrayerTimeCacheImpl(dataStore = get()) }
    single<AddressResolver> { AddressResolverImpl(dataStore = get()) }

    // ── Prayer use-cases ─────────────────────────────────────────────────
    single {
        PrayerTimeViewUseCase(
            repository = get(),
            dataStore = get(),
            locationService = get(),
            prayerCache = get(),
            addressResolver = get(),
        )
    }
    single { PrayerNotificationUseCase(dataStore = get(), prayerCache = get()) }

    // ── ViewModels ────────────────────────────────────────────────────────
    viewModel { PrayerTimeViewModel(prayerTimeUseCase = get(), prayerNotificationUseCase = get(), dataStore = get(), featureUsageRepository = get()) }
    viewModel { TasbeehViewModel(dataStore = get()) }
    viewModel { MethodViewModel() }
    viewModel { SettingsViewModel(dataStore = get(), prayerTimeUseCase = get()) }
    /*viewModel { LocationPickerViewModel(controller = get ()) }*/
    viewModel { CompassViewModel() }
    viewModel { CityPickerViewModel() }
    viewModel { HalalScannerViewModel(repository = get()) }
    viewModel { UtilityViewModel(usageRepository = get()) }
    viewModel { QuranViewModel(repository = get()) }
    viewModel { CalendarViewModel(prayerTimeUseCase = get(), dataStore = get()) }
}

private var koinStarted = false

fun initializeKoin() {
    if (koinStarted) return
    koinStarted = true
    startKoin {
        modules(prayerModule)
        modules(appModule)
    }
}