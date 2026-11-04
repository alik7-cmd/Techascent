package org.techascent.aifinder.presentation.navigation

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.savedstate.read
import org.koin.compose.viewmodel.koinViewModel
import org.techascent.aifinder.domain.auth.WelcomeDestination
import org.techascent.aifinder.presentation.account.*
import org.techascent.aifinder.presentation.details.*
import org.techascent.aifinder.presentation.home.*
import org.techascent.aifinder.presentation.location.*
import org.techascent.aifinder.presentation.login.*
import org.techascent.aifinder.presentation.review.*
import org.techascent.aifinder.presentation.scan.*
import org.techascent.aifinder.presentation.search.*
import org.techascent.aifinder.presentation.spaces.*
import org.techascent.aifinder.presentation.subscription.*
import org.techascent.aifinder.presentation.welcome.*

@Composable
fun AiFinderNavigation() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val destination = backStackEntry?.destination?.route
    // Shared across destinations so the scan-to-review handoff and account cleanup use the same instances.
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

    fun replaceRoot(screen: AiFinderScreen) {
        navController.navigate(screen.route) {
            popUpTo(navController.graph.id) { inclusive = true }
            launchSingleTop = true
        }
    }

    val openScanner: () -> Unit = {
        showingSubscription = false
        pendingScan = false
        navController.navigate(AiFinderScreen.Scan.route) { launchSingleTop = true }
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
    var scanCheckOrigin by remember { mutableStateOf(backStackEntry?.id) }
    LaunchedEffect(backStackEntry?.id) {
        if (backStackEntry?.id != scanCheckOrigin && pendingScan && !showingSubscription) {
            pendingScan = false
            subscription.reset()
        }
        scanCheckOrigin = backStackEntry?.id
        when (destination) {
            AiFinderScreen.Home.route -> home.start()
            AiFinderScreen.Scan.route -> scan.loadBalance()
            AiFinderScreen.Search.route -> search.start()
            AiFinderScreen.Details.route -> details.start()
            AiFinderScreen.Spaces.route, AiFinderScreen.Space.route -> spaces.start()
        }
    }
    val onBack: () -> Unit = {
        if (destination == AiFinderScreen.Scan.route) scan.cancelAnalysis()
        if (pendingScan) {
            pendingScan = false
            subscription.reset()
        }
        navController.popBackStack()
    }
    if (choosingLocation) {
        LocationSheet(
            locations,
            itemCount = if (destination == AiFinderScreen.Review.route) {
                (review.state as? ReviewUiState.Success)?.data?.detections?.size
            } else null,
            onDismiss = { choosingLocation = false },
        )
    }
    val detections by scan.detections.collectAsState()
    LaunchedEffect(detections) {
        detections?.let {
            review.show(it.detections, it.photo)
            navController.navigate(AiFinderScreen.Review.route) { launchSingleTop = true }
            scan.consumeDetections()
        }
    }

    NavHost(navController = navController, startDestination = AiFinderScreen.Welcome.route) {
        composable(AiFinderScreen.Welcome.route) {
            WelcomeScreen(
                onContinue = { replaceRoot(AiFinderScreen.Login) },
                onNavigate = { target ->
                    when (target) {
                        WelcomeDestination.Home -> replaceRoot(AiFinderScreen.Home)
                        WelcomeDestination.Login -> replaceRoot(AiFinderScreen.Login)
                        WelcomeDestination.Welcome -> Unit
                    }
                },
            )
        }
        composable(AiFinderScreen.Login.route) {
            LoginScreen(viewModel = login, onSignedIn = { replaceRoot(AiFinderScreen.Home) })
        }
        composable(AiFinderScreen.Home.route) {
            HomeScreen(
                viewModel = home,
                onSearch = {
                    search.clearQuery()
                    navController.navigate(AiFinderScreen.Search.route)
                },
                onScan = requestScan,
                isCheckingScan = checkingScan,
                onAccount = { navController.navigate(AiFinderScreen.Account.route) },
                onSpace = { navController.navigate(AiFinderScreen.Space.createRoute(it)) },
                onSeeAll = { navController.navigate(AiFinderScreen.Spaces.route) },
            )
        }
        composable(AiFinderScreen.Spaces.route) {
            SpacesScreen(
                allSpacesState,
                onSpace = { navController.navigate(AiFinderScreen.Space.createRoute(it)) },
                onScan = requestScan,
                isCheckingScan = checkingScan,
                onBack = onBack,
                onRetry = spaces::retry,
            )
        }
        composable(
            AiFinderScreen.Space.route,
            arguments = listOf(navArgument(AiFinderScreen.Space.SPACE_ID) { type = NavType.StringType }),
        ) { entry ->
            val id = checkNotNull(entry.arguments?.read { getString(AiFinderScreen.Space.SPACE_ID) })
            LaunchedEffect(id) { spaces.select(id) }
            SpaceScreen(
                spaceState,
                onItem = { navController.navigate(AiFinderScreen.Details.createRoute(it)) },
                onScan = requestScan,
                isCheckingScan = checkingScan,
                onBack = onBack,
                onRetry = spaces::retry,
            )
        }
        composable(AiFinderScreen.Account.route) {
            AccountScreen(
                viewModel = account,
                onBack = onBack,
                onSubscription = {
                    pendingScan = false
                    showingSubscription = true
                    subscription.refresh()
                },
                onSignedOut = {
                    scan.reset()
                    review.show(emptyList())
                    locations.reset()
                    home.reset()
                    search.reset()
                    details.reset()
                    spaces.reset()
                    choosingLocation = false
                    showingSubscription = false
                    pendingScan = false
                    subscription.reset()
                    replaceRoot(AiFinderScreen.Login)
                },
            )
        }
        composable(AiFinderScreen.Scan.route) {
            ScanScreen(
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
                },
            )
        }
        composable(AiFinderScreen.Review.route) {
            ReviewScreen(
                review.state,
                onSave = {
                    review.save(locationState.selectedLocation?.id) {
                        navController.popBackStack(AiFinderScreen.Home.route, inclusive = false)
                    }
                },
                onRename = review::rename,
                onDelete = review::delete,
                onBack = onBack,
                onRetry = review::retry,
                selectedLocation = locationState.selectedPath,
                onChooseLocation = { choosingLocation = true },
            )
        }
        composable(AiFinderScreen.Search.route) {
            SearchScreen(
                searchState,
                onQueryChange = search::updateQuery,
                onItemSelected = { navController.navigate(AiFinderScreen.Details.createRoute(it)) },
                onBack = onBack,
                onRetry = search::retry,
            )
        }
        composable(
            AiFinderScreen.Details.route,
            arguments = listOf(navArgument(AiFinderScreen.Details.ITEM_ID) { type = NavType.StringType }),
        ) { entry ->
            val id = checkNotNull(entry.arguments?.read { getString(AiFinderScreen.Details.ITEM_ID) })
            LaunchedEffect(id) { details.select(id) }
            DetailsScreen(detailsState, onBack = onBack, onRetry = details::retry)
        }
    }
    // Registered after NavHost so hardware Back also cancels an active scan before popping.
    SystemBackHandler(
        enabled = !showingSubscription && !choosingLocation && navController.previousBackStackEntry != null,
        onBack = onBack,
    )
    if (showingSubscription) {
        SubscriptionSheet(
            state = subscriptionState,
            onRetry = { subscription.refresh(onAllowed = if (pendingScan) openScanner else null) },
            onPurchaseCompleted = { purchase ->
                subscription.onPurchaseCompleted(purchase, onAllowed = when {
                    pendingScan -> openScanner
                    destination == AiFinderScreen.Scan.route -> ({
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
            },
        )
    }
}
