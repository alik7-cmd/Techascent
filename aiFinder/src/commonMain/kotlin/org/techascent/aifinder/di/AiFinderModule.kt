package org.techascent.aifinder.di

import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.dsl.module
import org.techascent.aifinder.data.InventoryRepository
import org.techascent.aifinder.data.MockInventoryRepository
import org.techascent.aifinder.data.vision.MockPhotoAnalyzer
import org.techascent.aifinder.domain.vision.PhotoAnalyzer
import org.techascent.aifinder.presentation.details.DetailsViewModel
import org.techascent.aifinder.presentation.home.HomeViewModel
import org.techascent.aifinder.presentation.review.ReviewViewModel
import org.techascent.aifinder.presentation.scan.ScanViewModel
import org.techascent.aifinder.presentation.search.SearchViewModel
import org.techascent.aifinder.presentation.welcome.WelcomeViewModel
import org.techascent.aifinder.presentation.location.LocationViewModel
import org.techascent.aifinder.data.auth.AuthRepositoryImpl
import org.techascent.aifinder.domain.auth.AuthRepository
import org.techascent.aifinder.domain.auth.usecase.GetCurrentUserUseCase
import org.techascent.aifinder.domain.auth.usecase.SignInUseCase
import org.techascent.aifinder.domain.auth.usecase.SignOutUseCase
import org.techascent.aifinder.presentation.login.LoginViewModel
import org.techascent.aifinder.data.location.LocationRepositoryImpl
import org.techascent.aifinder.domain.location.LocationRepository
import org.techascent.aifinder.domain.location.usecase.CreateLocationUseCase
import org.techascent.aifinder.domain.location.usecase.ObserveLocationsUseCase

val aiFinderModule = module {
    single<InventoryRepository> { MockInventoryRepository() }
    // Keep VertexGeminiPhotoAnalyzer available, but don't create a network client in mock mode.
    single<PhotoAnalyzer> { MockPhotoAnalyzer() }
    single<AuthRepository> { AuthRepositoryImpl(google = get()) }
    factory { SignInUseCase(get()) }
    factory { GetCurrentUserUseCase(get()) }
    factory { SignOutUseCase(get()) }
    single<LocationRepository> { LocationRepositoryImpl(remote = get(), auth = get()) }
    factory { ObserveLocationsUseCase(get()) }
    factory { CreateLocationUseCase(get()) }
    viewModel { WelcomeViewModel() }
    viewModel { LoginViewModel(signIn = get(), getCurrentUser = get()) }
    viewModel { HomeViewModel(repository = get()) }
    viewModel { ScanViewModel(analyzer = get()) }
    viewModel { ReviewViewModel(repository = get()) }
    viewModel { LocationViewModel(observeLocations = get(), createLocation = get()) }
    viewModel { SearchViewModel(repository = get()) }
    viewModel { DetailsViewModel(repository = get()) }
}
