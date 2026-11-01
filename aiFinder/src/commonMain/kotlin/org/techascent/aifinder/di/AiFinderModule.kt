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

val aiFinderModule = module {
    single<InventoryRepository> { MockInventoryRepository() }
    // Keep VertexGeminiPhotoAnalyzer available, but don't create a network client in mock mode.
    single<PhotoAnalyzer> { MockPhotoAnalyzer() }
    viewModel { WelcomeViewModel() }
    viewModel { HomeViewModel(repository = get()) }
    viewModel { ScanViewModel(analyzer = get()) }
    viewModel { ReviewViewModel(repository = get()) }
    viewModel { LocationViewModel(repository = get()) }
    viewModel { SearchViewModel(repository = get()) }
    viewModel { DetailsViewModel(repository = get()) }
}
