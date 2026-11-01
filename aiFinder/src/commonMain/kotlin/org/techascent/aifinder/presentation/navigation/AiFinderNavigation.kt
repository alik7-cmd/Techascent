package org.techascent.aifinder.presentation.navigation

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.techascent.aifinder.presentation.welcome.*
import org.techascent.aifinder.presentation.home.*
import org.techascent.aifinder.presentation.scan.*
import org.techascent.aifinder.presentation.review.*
import org.techascent.aifinder.presentation.search.*
import org.techascent.aifinder.presentation.details.*
import org.techascent.aifinder.presentation.location.*

@OptIn(KoinExperimentalAPI::class)
@Composable
fun AiFinderNavigation() {
    var destination by rememberSaveable { mutableStateOf("welcome") }
    val scan = koinViewModel<ScanViewModel>()
    val scanState by scan.state.collectAsState()
    val review = koinViewModel<ReviewViewModel>()
    val search = koinViewModel<SearchViewModel>()
    val details = koinViewModel<DetailsViewModel>()
    val locations = koinViewModel<LocationViewModel>()
    val home = koinViewModel<HomeViewModel>()
    var choosingLocation by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(destination) {
        if (destination == "home") home.retry()
    }
    if (choosingLocation) {
        LocationSheet(
            locations,
            itemCount = if (destination == "review") {
                (review.state as? ReviewUiState.Success)?.data?.detections?.size
            } else null,
            onDismiss = { choosingLocation = false }
        )
    }
    val detections by scan.detections.collectAsState()
    LaunchedEffect(detections) {
        detections?.let {
            review.show(it)
            destination = "review"
            scan.consumeDetections()
        }
    }
    val onBack: () -> Unit = {
        when (destination) {
            "scan" -> {
                scan.cancelAnalysis()
                destination = "home"
            }
            "review" -> destination = "scan"
            "search" -> destination = "home"
            "details" -> destination = "search"
        }
    }
    SystemBackHandler(
        enabled = destination != "welcome" && destination != "home",
        onBack = onBack
    )
    when (destination) {
        "welcome" -> WelcomeScreen(
            onContinue = { destination = "home" }
        )

        "home" -> HomeScreen(
            viewModel = home,
            onSearch = { destination = "search" },
            onScan = { destination = "scan" },
        )

        "scan" -> ScanScreen(
            scanState,
            onAnalyze = scan::analyze,
            onBack = onBack,
            onRetry = scan::retry,
            selectedLocation = locations.selectedPath,
            onChooseLocation = { choosingLocation = true }
        )

        "review" -> ReviewScreen(
            review.state,
            onSave = {
                review.save(requireNotNull(locations.selectedId) { "Choose a location before saving." })
                search.retry()
                destination = "home"
            },
            onRename = review::rename,
            onDelete = review::delete,
            onBack = onBack,
            onRetry = review::retry,
            selectedLocation = locations.selectedPath,
            onChooseLocation = { choosingLocation = true }
        )

        "search" -> SearchScreen(
            search.state,
            onQueryChange = search::updateQuery,
            onItemSelected = { id -> details.select(id); destination = "details" },
            onBack = onBack,
            onRetry = search::retry
        )

        "details" -> DetailsScreen(
            details.state,
            onBack = onBack,
            onRetry = details::retry
        )
    }
}
