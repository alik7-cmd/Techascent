package org.techascent.shared.di

import org.koin.dsl.module
import org.techascent.shared.data.api.PrayerApi
import org.techascent.shared.data.api.PrayerApiImpl
import org.techascent.shared.data.api.halalscanner.HalalScannerApi
import org.techascent.shared.data.api.halalscanner.HalalScannerApiImpl
import org.techascent.shared.data.api.quran.QuranApi
import org.techascent.shared.data.api.quran.QuranApiImpl
import org.techascent.shared.data.cache.CacheService
import org.techascent.shared.data.cache.DefaultCacheService
import org.techascent.shared.data.datasource.PrayerTimeDataSource
import org.techascent.shared.data.datasource.PrayerTimeDataSourceImpl
import org.techascent.shared.data.datasource.halalscanner.HalalScannerDataSource
import org.techascent.shared.data.datasource.halalscanner.HalalScannerDataSourceImpl
import org.techascent.shared.data.datasource.quran.QuranDataSource
import org.techascent.shared.data.datasource.quran.QuranDataSourceImpl
import org.techascent.shared.data.repository.PrayerTimesRepository
import org.techascent.shared.data.repository.PrayerTimesRepositoryImpl
import org.techascent.shared.data.repository.halalscanner.HalalScannerRepository
import org.techascent.shared.data.repository.halalscanner.HalalScannerRepositoryImpl
import org.techascent.shared.data.repository.quran.QuranRepository
import org.techascent.shared.data.repository.quran.QuranRepositoryImpl
import org.techascent.shared.network.provideHttpClient

val prayerModule = module {
    single { provideHttpClient() }
    single<PrayerApi> { PrayerApiImpl(get()) }
    single<CacheService<String, Any>> { DefaultCacheService(maxSize = 100) }
    single<PrayerTimesRepository> { PrayerTimesRepositoryImpl(get()) }
    single<PrayerTimeDataSource> { PrayerTimeDataSourceImpl(api = get()) }

    single<HalalScannerApi> { HalalScannerApiImpl(get()) }
    single<HalalScannerRepository> { HalalScannerRepositoryImpl(dataSource = get(), dataStore = get()) }
    single<HalalScannerDataSource> { HalalScannerDataSourceImpl(api = get()) }

    single<QuranApi> { QuranApiImpl(get()) }
    single<QuranDataSource> { QuranDataSourceImpl(api = get()) }
    single<QuranRepository> { QuranRepositoryImpl(dataSource = get(), dataStore = get()) }
}