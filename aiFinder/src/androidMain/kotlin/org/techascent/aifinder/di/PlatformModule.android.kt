package org.techascent.aifinder.di

import org.koin.dsl.module
import org.techascent.aifinder.data.cache.AppContextHolder
import org.techascent.aifinder.data.cache.databaseBuilder
import org.techascent.aifinder.data.cache.buildAiFinderDatabase
import org.techascent.aifinder.data.location.LocationRemoteDataSource
import org.techascent.aifinder.data.location.FirebaseLocationDataSource
import org.techascent.aifinder.data.auth.FirebaseGoogleAuthDataSource
import org.techascent.aifinder.data.auth.GoogleAuthDataSource
import org.techascent.aifinder.data.auth.SignInRemoteDataSource
import org.techascent.aifinder.data.auth.FirebaseSignInDataSource
import org.techascent.aifinder.data.inventory.FirebaseItemDataSource
import org.techascent.aifinder.data.inventory.ItemRemoteDataSource
import org.techascent.aifinder.data.photo.FilePhotoLocalDataSource
import org.techascent.aifinder.data.photo.FirebasePhotoDataSource
import org.techascent.aifinder.data.photo.PhotoLocalDataSource
import org.techascent.aifinder.data.photo.PhotoRemoteDataSource
import java.io.File
import org.techascent.aifinder.data.subscription.ScanAccessRemoteDataSource
import org.techascent.aifinder.data.subscription.FirebaseScanAccessDataSource

actual val platformModule = module {
    single<org.techascent.aifinder.domain.auth.usecase.ProfileDeletionRemote> {
        org.techascent.aifinder.data.auth.FirebaseProfileDeletion()
    }
    single<ScanAccessRemoteDataSource> { FirebaseScanAccessDataSource() }
    single<GoogleAuthDataSource> { FirebaseGoogleAuthDataSource() }
    single<SignInRemoteDataSource> { FirebaseSignInDataSource() }
    single<LocationRemoteDataSource> { FirebaseLocationDataSource() }
    single<ItemRemoteDataSource> { FirebaseItemDataSource() }
    single<PhotoRemoteDataSource> { FirebasePhotoDataSource() }
    single<PhotoLocalDataSource> { FilePhotoLocalDataSource(File(AppContextHolder.context.filesDir, "item_photos")) }
    single { databaseBuilder(AppContextHolder.context).buildAiFinderDatabase() }
}
