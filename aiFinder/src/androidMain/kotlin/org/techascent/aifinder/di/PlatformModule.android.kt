package org.techascent.aifinder.di

import org.koin.dsl.module
import org.techascent.aifinder.data.cache.AppContextHolder
import org.techascent.aifinder.data.cache.databaseBuilder
import org.techascent.aifinder.data.cache.buildAiFinderDatabase
import org.techascent.aifinder.data.location.LocationRemoteDataSource
import org.techascent.aifinder.data.location.FirebaseLocationDataSource
import org.techascent.aifinder.data.auth.FirebaseGoogleAuthDataSource
import org.techascent.aifinder.data.auth.GoogleAuthDataSource

actual val platformModule = module {
    single<GoogleAuthDataSource> { FirebaseGoogleAuthDataSource() }
    single<LocationRemoteDataSource> { FirebaseLocationDataSource() }
    single { databaseBuilder(AppContextHolder.context).buildAiFinderDatabase() }
}
