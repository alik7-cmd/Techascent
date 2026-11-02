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
import org.techascent.aifinder.presentation.login.*
import org.techascent.aifinder.presentation.account.*
import org.techascent.aifinder.presentation.spaces.*
import org.techascent.aifinder.domain.auth.WelcomeDestination

@OptIn(KoinExperimentalAPI::class)
@Composable
fun AiFinderNavigation() {
    var destination by rememberSaveable { mutableStateOf("welcome") }
    val scan = koinViewModel<ScanViewModel>()
    val scanState by scan.state.collectAsState()
    val review = koinViewModel<ReviewViewModel>()
    val search = koinViewModel<SearchViewModel>()
    val searchState by search.state.collectAsState()
    val details = koinViewModel<DetailsViewModel>()
    val detailsState by details.state.collectAsState()
    val spaces = koinViewModel<SpacesViewModel>()
    val allSpacesState by spaces.allSpaces.collectAsState()
    val spaceState by spaces.space.collectAsState()
    val locations = koinViewModel<LocationViewModel>()
    val locationState by locations.state.collectAsState()
    val home = koinViewModel<HomeViewModel>()
    val login = koinViewModel<LoginViewModel>()
    val account = koinViewModel<AccountViewModel>()
    var choosingLocation by rememberSaveable { mutableStateOf(false) }
    // Where back goes from screens reachable from more than one place.
    var spaceBack by rememberSaveable { mutableStateOf("home") }
    var detailsBack by rememberSaveable { mutableStateOf("search") }
    LaunchedEffect(destination) {
        when (destination) {
            "home" -> home.start()
            "search" -> search.start()
            "details" -> details.start()
            "spaces", "space" -> spaces.start()
        }
    }
    val openSpace: (String, String) -> Unit = { id, from ->
        spaces.select(id)
        spaceBack = from
        destination = "space"
    }
    val openDetails: (String, String) -> Unit = { id, from ->
        details.select(id)
        detailsBack = from
        destination = "details"
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
            review.show(it.detections, it.photo)
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
            "details" -> destination = detailsBack
            "account" -> destination = "home"
            "spaces" -> destination = "home"
            "space" -> destination = spaceBack
        }
    }
    SystemBackHandler(
        enabled = destination in listOf("scan", "review", "search", "details", "account", "spaces", "space"),
        onBack = onBack
    )
    when (destination) {
        "welcome" -> WelcomeScreen(
            onContinue = { destination = "login" },
            onNavigate = { target ->
                destination = when (target) {
                    WelcomeDestination.Home -> "home"
                    WelcomeDestination.Login -> "login"
                    WelcomeDestination.Welcome -> "welcome"
                }
            }
        )

        "login" -> LoginScreen(
            viewModel = login,
            onSignedIn = { destination = "home" }
        )

        "home" -> HomeScreen(
            viewModel = home,
            onSearch = {
                search.clearQuery()
                destination = "search"
            },
            onScan = { destination = "scan" },
            onAccount = { destination = "account" },
            onSpace = { id -> openSpace(id, "home") },
            onSeeAll = { destination = "spaces" },
        )

        "spaces" -> SpacesScreen(
            allSpacesState,
            onSpace = { id -> openSpace(id, "spaces") },
            onScan = { destination = "scan" },
            onBack = onBack,
            onRetry = spaces::retry
        )

        "space" -> SpaceScreen(
            spaceState,
            onItem = { id -> openDetails(id, "space") },
            onScan = { destination = "scan" },
            onBack = onBack,
            onRetry = spaces::retry
        )

        "account" -> AccountScreen(
            viewModel = account,
            onBack = onBack,
            onSignedOut = {
                locations.reset()
                home.reset()
                search.reset()
                details.reset()
                spaces.reset()
                choosingLocation = false
                destination = "login"
            }
        )

        "scan" -> ScanScreen(
            scanState,
            onAnalyze = scan::analyze,
            onBack = onBack,
            onRetry = scan::retry,
            selectedLocation = locationState.selectedPath,
            onChooseLocation = { choosingLocation = true }
        )

        "review" -> ReviewScreen(
            review.state,
            onSave = {
                review.save(locationState.selectedLocation?.id) { destination = "home" }
            },
            onRename = review::rename,
            onDelete = review::delete,
            onBack = onBack,
            onRetry = review::retry,
            selectedLocation = locationState.selectedPath,
            onChooseLocation = { choosingLocation = true }
        )

        "search" -> SearchScreen(
            searchState,
            onQueryChange = search::updateQuery,
            onItemSelected = { id -> openDetails(id, "search") },
            onBack = onBack,
            onRetry = search::retry
        )

        "details" -> DetailsScreen(
            detailsState,
            onBack = onBack,
            onRetry = details::retry
        )
    }
}
