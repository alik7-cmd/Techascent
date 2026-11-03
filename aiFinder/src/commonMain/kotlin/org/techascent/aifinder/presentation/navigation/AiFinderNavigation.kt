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
import androidx.compose.ui.window.Dialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import apphub.aifinder.generated.resources.Res
import apphub.aifinder.generated.resources.subscription_checking
import org.jetbrains.compose.resources.stringResource
import org.techascent.aifinder.presentation.theme.AiFinderPalette

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
    val requestScan: () -> Unit = {
        pendingScan = true
        subscription.refresh(onAllowed = {
            showingSubscription = false
            pendingScan = false
            destination = "scan"
        })
    }
    LaunchedEffect(Unit) {
        if (pendingScan || showingSubscription) {
            subscription.refresh(onAllowed = if (pendingScan) ({
                showingSubscription = false
                pendingScan = false
                destination = "scan"
            }) else null)
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
            onAccount = { destination = "account" },
            onSpace = { id -> openSpace(id, "home") },
            onSeeAll = { destination = "spaces" },
        )

        "spaces" -> SpacesScreen(
            allSpacesState,
            onSpace = { id -> openSpace(id, "spaces") },
            onScan = requestScan,
            onBack = onBack,
            onRetry = spaces::retry
        )

        "space" -> SpaceScreen(
            spaceState,
            onItem = { id -> openDetails(id, "space") },
            onScan = requestScan,
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
    if (pendingScan && !showingSubscription && subscriptionState is SubscriptionUiState.Loading) {
        Dialog(onDismissRequest = {
            pendingScan = false
            subscription.reset()
        }) {
            Surface(shape = RoundedCornerShape(20.dp), color = AiFinderPalette.Surface) {
                Column(
                    Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(Modifier.size(28.dp), color = AiFinderPalette.Teal)
                    Text(stringResource(Res.string.subscription_checking), color = AiFinderPalette.Ink)
                }
            }
        }
    }
    if (showingSubscription) {
        SubscriptionSheet(
            state = subscriptionState,
            onRetry = {
                subscription.refresh(onAllowed = if (pendingScan) ({
                    showingSubscription = false
                    pendingScan = false
                    destination = "scan"
                }) else null)
            },
            onDismiss = {
                showingSubscription = false
                pendingScan = false
                subscription.reset()
            }
        )
    }
}
