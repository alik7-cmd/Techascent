package org.techascent.aifinder.di

import org.koin.dsl.module
import org.techascent.aifinder.data.cache.databaseBuilder
import org.techascent.aifinder.data.cache.buildAiFinderDatabase
import org.techascent.aifinder.data.location.LocationRemoteDataSource
import org.techascent.aifinder.data.location.UnavailableLocationDataSource
import org.techascent.aifinder.data.auth.GoogleAuthDataSource
import org.techascent.aifinder.data.auth.UnavailableGoogleAuthDataSource

actual val platformModule = module {
    single<GoogleAuthDataSource> { UnavailableGoogleAuthDataSource() }
    single<LocationRemoteDataSource> { UnavailableLocationDataSource() }
    single { databaseBuilder().buildAiFinderDatabase() }
}
