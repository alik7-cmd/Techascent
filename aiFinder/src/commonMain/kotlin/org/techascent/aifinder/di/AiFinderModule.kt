package org.techascent.aifinder.di

import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.dsl.module
import org.techascent.aifinder.data.InventoryRepository
import org.techascent.aifinder.data.MockInventoryRepository
import org.techascent.aifinder.presentation.home.HomeViewModel
import org.techascent.aifinder.presentation.welcome.WelcomeViewModel

val aiFinderModule = module {
    single<InventoryRepository> { MockInventoryRepository() }
    viewModel { WelcomeViewModel() }
    viewModel { HomeViewModel(repository = get()) }
}
