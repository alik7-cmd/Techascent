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
import org.techascent.aifinder.presentation.subscription.SubscriptionSheet
import org.techascent.aifinder.presentation.subscription.SubscriptionViewModel
import org.techascent.aifinder.presentation.subscription.SubscriptionUiState

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
    val subscription = koinViewModel<SubscriptionViewModel>()
    val subscriptionState by subscription.state.collectAsState()
    var choosingLocation by rememberSaveable { mutableStateOf(false) }
    var showingSubscription by rememberSaveable { mutableStateOf(false) }
    var pendingScan by rememberSaveable { mutableStateOf(false) }
    val openScanner: () -> Unit = {
        showingSubscription = false
        pendingScan = false
        destination = "scan"
    }
    val checkingScan = pendingScan && !showingSubscription && subscriptionState is SubscriptionUiState.Loading
    val requestScan: () -> Unit = {
        pendingScan = true
        subscription.refresh(onAllowed = openScanner)
    }
    LaunchedEffect(Unit) {
        if (pendingScan || showingSubscription) {
            subscription.refresh(onAllowed = if (pendingScan) openScanner else null)
        }
    }
    LaunchedEffect(subscriptionState, pendingScan) {
        if (pendingScan && (subscriptionState is SubscriptionUiState.Error ||
                (subscriptionState as? SubscriptionUiState.Ready)?.access?.canScan == false)) {
            showingSubscription = true
        }
    }
    // Where back goes from screens reachable from more than one place.
    var spaceBack by rememberSaveable { mutableStateOf("home") }
    var detailsBack by rememberSaveable { mutableStateOf("search") }
    var scanCheckOrigin by remember { mutableStateOf(destination) }
    LaunchedEffect(destination) {
        // Leaving the screen while the credit check runs cancels the pending scan.
        if (destination != scanCheckOrigin && pendingScan && !showingSubscription) {
            pendingScan = false
            subscription.reset()
        }
        scanCheckOrigin = destination
        when (destination) {
            "home" -> home.start()
            "scan" -> scan.loadBalance()
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
        enabled = !showingSubscription && !pendingScan && destination in listOf("scan", "review", "search", "details", "account", "spaces", "space"),
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
            onScan = requestScan,
            isCheckingScan = checkingScan,
            onAccount = { destination = "account" },
            onSpace = { id -> openSpace(id, "home") },
            onSeeAll = { destination = "spaces" },
        )

        "spaces" -> SpacesScreen(
            allSpacesState,
            onSpace = { id -> openSpace(id, "spaces") },
            onScan = requestScan,
            isCheckingScan = checkingScan,
            onBack = onBack,
            onRetry = spaces::retry
        )

        "space" -> SpaceScreen(
            spaceState,
            onItem = { id -> openDetails(id, "space") },
            onScan = requestScan,
            isCheckingScan = checkingScan,
            onBack = onBack,
            onRetry = spaces::retry
        )

        "account" -> AccountScreen(
            viewModel = account,
            onBack = onBack,
            onSubscription = {
                pendingScan = false
                showingSubscription = true
                subscription.refresh()
            },
            onSignedOut = {
                locations.reset()
                home.reset()
                search.reset()
                details.reset()
                spaces.reset()
                choosingLocation = false
                showingSubscription = false
                pendingScan = false
                subscription.reset()
                destination = "login"
            }
        )

        "scan" -> ScanScreen(
            scanState,
            onAnalyze = scan::analyze,
            onBack = onBack,
            onRetry = scan::retry,
            selectedLocation = locationState.selectedPath,
            onChooseLocation = { choosingLocation = true },
            onRetryBalance = scan::loadBalance,
            onGetScans = {
                showingSubscription = true
                subscription.refresh()
            }
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
    if (showingSubscription) {
        SubscriptionSheet(
            state = subscriptionState,
            onRetry = { subscription.refresh(onAllowed = if (pendingScan) openScanner else null) },
            onPurchaseCompleted = { purchase ->
                subscription.onPurchaseCompleted(purchase, onAllowed = when {
                    pendingScan -> openScanner
                    destination == "scan" -> ({
                        showingSubscription = false
                        scan.loadBalance()
                    })
                    else -> null
                })
            },
            onDismiss = {
                showingSubscription = false
                pendingScan = false
                subscription.reset()
            }
        )
    }
}
