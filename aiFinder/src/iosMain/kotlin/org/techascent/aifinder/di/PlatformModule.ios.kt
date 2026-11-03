package org.techascent.aifinder.di

import org.koin.dsl.module
import org.techascent.aifinder.data.cache.databaseBuilder
import org.techascent.aifinder.data.cache.buildAiFinderDatabase
import org.techascent.aifinder.data.location.LocationRemoteDataSource
import org.techascent.aifinder.data.location.UnavailableLocationDataSource
import org.techascent.aifinder.data.auth.GoogleAuthDataSource
import org.techascent.aifinder.data.auth.SignInRemoteDataSource
import org.techascent.aifinder.data.auth.UnavailableSignInDataSource
import org.techascent.aifinder.data.auth.UnavailableGoogleAuthDataSource
import org.techascent.aifinder.data.inventory.ItemRemoteDataSource
import org.techascent.aifinder.data.inventory.UnavailableItemDataSource
import org.techascent.aifinder.data.photo.FilePhotoLocalDataSource
import org.techascent.aifinder.data.photo.PhotoLocalDataSource
import org.techascent.aifinder.data.photo.PhotoRemoteDataSource
import org.techascent.aifinder.data.photo.UnavailablePhotoDataSource

actual val platformModule = module {
    single<GoogleAuthDataSource> { UnavailableGoogleAuthDataSource() }
    single<SignInRemoteDataSource> { UnavailableSignInDataSource() }
    single<LocationRemoteDataSource> { UnavailableLocationDataSource() }
    single<ItemRemoteDataSource> { UnavailableItemDataSource() }
    single<PhotoRemoteDataSource> { UnavailablePhotoDataSource() }
    single<PhotoLocalDataSource> { FilePhotoLocalDataSource() }
    single { databaseBuilder().buildAiFinderDatabase() }
}
