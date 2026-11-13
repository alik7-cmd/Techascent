package org.techascent.aifinder.di

import org.koin.dsl.module
import org.techascent.aifinder.data.cache.databaseBuilder
import org.techascent.aifinder.data.cache.buildAiFinderDatabase
import org.techascent.aifinder.data.location.LocationRemoteDataSource
import org.techascent.aifinder.data.location.UnavailableLocationDataSource
import org.techascent.aifinder.data.auth.GoogleAuthDataSource
import org.techascent.aifinder.data.auth.SignInRemoteDataSource
import org.techascent.aifinder.data.auth.IosAuthBridgeRegistry
import org.techascent.aifinder.data.auth.IosGoogleAuthDataSource
import org.techascent.aifinder.data.auth.IosSignInDataSource
import org.techascent.aifinder.data.inventory.ItemRemoteDataSource
import org.techascent.aifinder.data.inventory.UnavailableItemDataSource
import org.techascent.aifinder.data.photo.FilePhotoLocalDataSource
import org.techascent.aifinder.data.photo.PhotoLocalDataSource
import org.techascent.aifinder.data.photo.PhotoRemoteDataSource
import org.techascent.aifinder.data.photo.UnavailablePhotoDataSource
import org.techascent.aifinder.data.subscription.ScanAccessRemoteDataSource
import org.techascent.aifinder.data.subscription.UnavailableScanAccessDataSource
import org.techascent.aifinder.data.firebase.*

actual val platformModule = module {
    single<org.techascent.aifinder.domain.auth.usecase.ProfileDeletionRemote> {
        org.techascent.aifinder.data.auth.IosProfileDeletion()
    }
    single<ScanAccessRemoteDataSource> { IosScanAccessDataSource() }
    single<GoogleAuthDataSource> { IosGoogleAuthDataSource(IosAuthBridgeRegistry.requireBridge()) }
    single<SignInRemoteDataSource> { IosSignInDataSource(IosAuthBridgeRegistry.requireBridge()) }
    single<LocationRemoteDataSource> { IosLocationDataSource() }
    single<ItemRemoteDataSource> { IosItemDataSource() }
    single<PhotoRemoteDataSource> { IosPhotoDataSource() }
    single<PhotoLocalDataSource> { FilePhotoLocalDataSource() }
    single { databaseBuilder().buildAiFinderDatabase() }
}
