package org.techascent.shared.di

import org.koin.dsl.module
import org.techascent.shared.data.api.PrayerApi
import org.techascent.shared.data.api.PrayerApiImpl
import org.techascent.shared.data.cache.CacheService
import org.techascent.shared.data.cache.DefaultCacheService
import org.techascent.shared.data.datasource.PrayerTimeDataSource
import org.techascent.shared.data.datasource.PrayerTimeDataSourceImpl
import org.techascent.shared.data.repository.PrayerTimesRepository
import org.techascent.shared.data.repository.PrayerTimesRepositoryImpl
import org.techascent.shared.network.provideHttpClient

val prayerModule = module {
    single { provideHttpClient() }
    single<PrayerApi> { PrayerApiImpl(get()) }
    single<CacheService<String, Any>> { DefaultCacheService(maxSize = 100) }
    single<PrayerTimesRepository> { PrayerTimesRepositoryImpl(get()) }
    single<PrayerTimeDataSource> { PrayerTimeDataSourceImpl(api = get(), cacheService = get()) }

}