package org.techascent.aifinder.di

import org.koin.dsl.module
import org.techascent.aifinder.data.cache.AppContextHolder
import org.techascent.aifinder.data.cache.databaseBuilder
import org.techascent.aifinder.data.cache.buildAiFinderDatabase
import org.techascent.aifinder.data.location.LocationRemoteDataSource
import org.techascent.aifinder.data.location.FirebaseLocationDataSource
import org.techascent.aifinder.data.auth.FirebaseGoogleAuthDataSource
import org.techascent.aifinder.data.auth.GoogleAuthDataSource
import org.techascent.aifinder.data.inventory.FirebaseItemDataSource
import org.techascent.aifinder.data.inventory.ItemRemoteDataSource
import org.techascent.aifinder.data.photo.FilePhotoLocalDataSource
import org.techascent.aifinder.data.photo.FirebasePhotoDataSource
import org.techascent.aifinder.data.photo.PhotoLocalDataSource
import org.techascent.aifinder.data.photo.PhotoRemoteDataSource
import java.io.File

actual val platformModule = module {
    single<GoogleAuthDataSource> { FirebaseGoogleAuthDataSource() }
    single<LocationRemoteDataSource> { FirebaseLocationDataSource() }
    single<ItemRemoteDataSource> { FirebaseItemDataSource() }
    single<PhotoRemoteDataSource> { FirebasePhotoDataSource() }
    single<PhotoLocalDataSource> { FilePhotoLocalDataSource(File(AppContextHolder.context.filesDir, "item_photos")) }
    single { databaseBuilder(AppContextHolder.context).buildAiFinderDatabase() }
}
