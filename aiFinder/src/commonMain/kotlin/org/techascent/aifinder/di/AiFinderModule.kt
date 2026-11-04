package org.techascent.aifinder.di

import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.dsl.module
import kotlinx.coroutines.Dispatchers
import org.techascent.aifinder.config.VertexAiConfig
import org.techascent.aifinder.data.inventory.InventoryRepositoryImpl
import org.techascent.aifinder.data.inventory.ItemLocalDataSource
import org.techascent.aifinder.data.inventory.RoomItemLocalDataSource
import org.techascent.aifinder.data.vision.VertexGeminiPhotoAnalyzer
import org.techascent.aifinder.data.vision.createAiHttpClient
import org.techascent.aifinder.domain.inventory.InventoryRepository
import org.techascent.aifinder.domain.inventory.usecase.ObserveInventoryUseCase
import org.techascent.aifinder.domain.inventory.usecase.SaveDetectionsUseCase
import org.techascent.aifinder.presentation.spaces.SpacesViewModel
import org.techascent.aifinder.data.photo.ItemPhotoRepositoryImpl
import org.techascent.aifinder.domain.photo.ItemPhotoRepository
import org.techascent.aifinder.domain.vision.PhotoAnalyzer
import org.techascent.aifinder.presentation.details.DetailsViewModel
import org.techascent.aifinder.presentation.home.HomeViewModel
import org.techascent.aifinder.presentation.review.ReviewViewModel
import org.techascent.aifinder.presentation.scan.ScanViewModel
import org.techascent.aifinder.presentation.search.SearchViewModel
import org.techascent.aifinder.presentation.welcome.WelcomeViewModel
import org.techascent.aifinder.presentation.location.LocationViewModel
import org.techascent.aifinder.data.auth.AuthRepositoryImpl
import org.techascent.aifinder.data.auth.RoomWelcomeRepository
import org.techascent.aifinder.domain.auth.WelcomeRepository
import org.techascent.aifinder.domain.auth.ResolveWelcomeDestinationUseCase
import org.techascent.aifinder.domain.auth.AuthRepository
import org.techascent.aifinder.domain.auth.usecase.GetCurrentUserUseCase
import org.techascent.aifinder.domain.auth.usecase.SignInUseCase
import org.techascent.aifinder.domain.auth.usecase.SignOutUseCase
import org.techascent.aifinder.presentation.login.LoginViewModel
import org.techascent.aifinder.presentation.account.AccountViewModel
import org.techascent.aifinder.data.location.LocationRepositoryImpl
import org.techascent.aifinder.data.location.LocationLocalDataSource
import org.techascent.aifinder.data.location.RoomLocationLocalDataSource
import org.techascent.aifinder.data.cache.AiFinderDatabase
import org.techascent.aifinder.domain.location.LocationRepository
import org.techascent.aifinder.domain.location.usecase.CreateLocationUseCase
import org.techascent.aifinder.domain.location.usecase.ObserveLocationsUseCase
import org.techascent.aifinder.domain.subscription.ScanAccessRepository
import org.techascent.aifinder.domain.subscription.MeteredPhotoAnalyzer
import org.techascent.aifinder.data.subscription.ScanAccessRepositoryImpl
import org.techascent.aifinder.presentation.subscription.SubscriptionViewModel
import org.techascent.aifinder.domain.subscription.PurchaseGateway
import org.techascent.aifinder.data.subscription.RevenueCatPurchaseGateway
import org.techascent.aifinder.data.subscription.revenueCatApiKey

val aiFinderModule = module {
    single<ScanAccessRepository> {
        ScanAccessRepositoryImpl(auth = get(), remote = get(), cache = get<AiFinderDatabase>().scanWalletDao())
    }
    single<PurchaseGateway> {
        RevenueCatPurchaseGateway(
            apiKey = revenueCatApiKey(),
            currentUserId = { get<AuthRepository>().currentUser?.id },
        )
    }
    viewModel { SubscriptionViewModel(repository = get(), purchases = get()) }
    single<PhotoAnalyzer> {
        MeteredPhotoAnalyzer(
            analyzer = VertexGeminiPhotoAnalyzer(
                client = createAiHttpClient(),
                apiKey = VertexAiConfig.API_KEY,
                model = VertexAiConfig.MODEL,
                dispatcher = Dispatchers.Default,
            ),
            access = get()
        )
    }
    single<AuthRepository> { AuthRepositoryImpl(google = get(), signInRemote = get()) }
    factory { SignInUseCase(get()) }
    factory { GetCurrentUserUseCase(get()) }
    factory {
        val database = get<AiFinderDatabase>()
        val photos = get<ItemPhotoRepositoryImpl>()
        SignOutUseCase(get()) { userId ->
            photos.clearCache(userId)
            database.itemDao().deleteForUser(userId)
            database.locationDao().deleteForUser(userId)
            database.scanWalletDao().deleteForUser(userId)
        }
    }
    single<LocationLocalDataSource> { RoomLocationLocalDataSource(get<AiFinderDatabase>().locationDao()) }
    single<LocationRepository> { LocationRepositoryImpl(remote = get(), local = get(), auth = get()) }
    factory { ObserveLocationsUseCase(get()) }
    factory { CreateLocationUseCase(get()) }
    single<ItemLocalDataSource> { RoomItemLocalDataSource(get<AiFinderDatabase>().itemDao()) }
    single<InventoryRepository> { InventoryRepositoryImpl(remote = get(), local = get(), auth = get()) }
    factory { ObserveInventoryUseCase(items = get(), locations = get()) }
    single { ItemPhotoRepositoryImpl(remote = get(), local = get(), auth = get()) }
    single<ItemPhotoRepository> { get<ItemPhotoRepositoryImpl>() }
    factory { SaveDetectionsUseCase(repository = get(), photos = get()) }
    single<WelcomeRepository> { RoomWelcomeRepository(get<AiFinderDatabase>().welcomePreferenceDao()) }
    factory { ResolveWelcomeDestinationUseCase(auth = get(), welcome = get()) }
    viewModel { WelcomeViewModel(resolveDestination = get()) }
    viewModel { LoginViewModel(signIn = get(), getCurrentUser = get(), scanAccess = get(), purchases = get()) }
    viewModel { AccountViewModel(getCurrentUser = get(), signOut = get(), purchases = get()) }
    viewModel { HomeViewModel(observeInventory = get()) }
    viewModel { ScanViewModel(analyzer = get(), access = get()) }
    viewModel { ReviewViewModel(saveDetections = get()) }
    viewModel { LocationViewModel(observeLocations = get(), createLocation = get()) }
    viewModel { SearchViewModel(observeInventory = get()) }
    viewModel { DetailsViewModel(observeInventory = get()) }
    viewModel { SpacesViewModel(observeInventory = get()) }
}
