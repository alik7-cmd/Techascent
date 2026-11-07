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
    var preparingPurchase by remember { mutableStateOf(false) }
    var purchasePreparationError by remember { mutableStateOf(false) }
    var purchaseOrigin by remember { mutableStateOf<String?>(null) }
    var addingLocation by rememberSaveable { mutableStateOf(false) }
    var creatingLocationFromHome by rememberSaveable { mutableStateOf(false) }
    val openLocationCreation: () -> Unit = {
        preparingPurchase = false
        showingSubscription = false
        addingLocation = false
        creatingLocationFromHome = true
        choosingLocation = true
        scan.loadBalance()
    }
    val scanContent = ((scanState as? ScanUiState.Success)?.data ?: ScanContent()).copy(
        isPreparingPurchase = preparingPurchase,
        purchasePreparationError = purchasePreparationError,
    )
    val displayedScanState = (scanState as? ScanUiState.Success)?.copy(data = scanContent) ?: scanState
    val preparePurchase: () -> Unit = {
        if (!preparingPurchase) {
            addingLocation = false
            subscription.reset()
            purchaseOrigin = backStackEntry?.id
            purchasePreparationError = false
            preparingPurchase = true
            subscription.refresh()
        }
    }
    val addLocation: () -> Unit = {
        if (!preparingPurchase) {
            subscription.reset()
            addingLocation = true
            purchaseOrigin = backStackEntry?.id
            purchasePreparationError = false
            preparingPurchase = true
            subscription.refresh(onAllowed = openLocationCreation)
        }
    }

    LaunchedEffect(subscriptionState, preparingPurchase) {
        if (preparingPurchase) {
            when (val state = subscriptionState) {
                is SubscriptionUiState.Ready -> {
                    preparingPurchase = false
                    if (state.purchasesEnabled) {
                        showingSubscription = true
                    } else {
                        purchasePreparationError = true
                    }
                }
                SubscriptionUiState.Error -> {
                    preparingPurchase = false
                    purchasePreparationError = true
                }
                else -> Unit
            }
        }
    }

    fun replaceRoot(screen: AiFinderScreen) {
        navController.navigate(screen.route) {
            popUpTo(navController.graph.id) { inclusive = true }
            launchSingleTop = true
        }
    }

    val openScanner: () -> Unit = {
        showingSubscription = false
        navController.navigate(AiFinderScreen.Scan.route) { launchSingleTop = true }
    }
    val resumeAnalysis: () -> Unit = {
        showingSubscription = false
        scan.loadBalance()
        scan.resumeAfterPurchase()
    }
    LaunchedEffect(Unit) {
        if (showingSubscription) {
            subscription.refresh(onAllowed = when {
                addingLocation -> openLocationCreation
                scanContent.requiresPurchase -> resumeAnalysis
                else -> null
            })
        }
    }
    LaunchedEffect(scanContent.requiresPurchase) {
        if (scanContent.requiresPurchase && destination == AiFinderScreen.Scan.route) {
            showingSubscription = true
            subscription.refresh(onAllowed = resumeAnalysis)
        }
    }
    LaunchedEffect(backStackEntry?.id) {
        if (purchaseOrigin != backStackEntry?.id) {
            if (preparingPurchase) subscription.reset()
            preparingPurchase = false
            purchasePreparationError = false
            purchaseOrigin = null
            addingLocation = false
            creatingLocationFromHome = false
        }
        when (destination) {
            AiFinderScreen.Home.route -> {
                home.start()
                scan.loadBalance()
            }
            AiFinderScreen.Scan.route -> scan.loadBalance()
            AiFinderScreen.Search.route -> search.start()
            AiFinderScreen.Details.route -> details.start()
            AiFinderScreen.Spaces.route, AiFinderScreen.Space.route -> spaces.start()
        }
        if (destination != AiFinderScreen.Scan.route && scanContent.requiresPurchase) {
            scan.dismissPurchase()
            showingSubscription = false
            subscription.reset()
        }
    }
    val onBack: () -> Unit = {
        if (destination == AiFinderScreen.Scan.route) scan.cancelAnalysis()
        subscription.reset()
        navController.popBackStack()
    }
    if (choosingLocation) {
        LocationSheet(
            locations,
            itemCount = if (destination == AiFinderScreen.Review.route) {
                (review.state as? ReviewUiState.Success)?.data?.detections?.size
            } else null,
            onDismiss = { choosingLocation = false; creatingLocationFromHome = false },
            startCreating = creatingLocationFromHome,
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
                onScan = openScanner,
                scanBalance = scanContent,
                onRetryBalance = scan::loadBalance,
                onGetScans = preparePurchase,
                onAddLocation = addLocation,
                isCheckingLocationAccess = preparingPurchase && addingLocation,
                locationAccessError = purchasePreparationError && addingLocation,
                onAccount = { navController.navigate(AiFinderScreen.Account.route) },
                onSpace = { navController.navigate(AiFinderScreen.Space.createRoute(it)) },
                onSeeAll = { navController.navigate(AiFinderScreen.Spaces.route) },
            )
        }
        composable(AiFinderScreen.Spaces.route) {
            SpacesScreen(
                allSpacesState,
                onSpace = { navController.navigate(AiFinderScreen.Space.createRoute(it)) },
                onScan = openScanner,
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
                onScan = openScanner,
                onBack = onBack,
                onRetry = spaces::retry,
            )
        }
        composable(AiFinderScreen.Account.route) {
            AccountScreen(
                viewModel = account,
                onBack = onBack,
                onSubscription = { navController.navigate(AiFinderScreen.PurchaseHistory.route) { launchSingleTop = true } },
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
                    subscription.reset()
                    replaceRoot(AiFinderScreen.Login)
                },
            )
        }
        composable(AiFinderScreen.PurchaseHistory.route) {
            PurchaseHistoryScreen(viewModel = koinViewModel<PurchaseHistoryViewModel>(), onBack = onBack)
        }
        composable(AiFinderScreen.Scan.route) {
            ScanScreen(
                displayedScanState,
                onAnalyze = scan::analyze,
                onBack = onBack,
                onRetry = scan::retry,
                selectedLocation = locationState.selectedPath,
                onChooseLocation = { choosingLocation = true },
                onRetryBalance = scan::loadBalance,
                onGetScans = preparePurchase,
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
            onRetry = {
                subscription.refresh(onAllowed = when {
                    addingLocation -> openLocationCreation
                    scanContent.requiresPurchase -> resumeAnalysis
                    else -> null
                })
            },
            onPurchaseCompleted = { purchase ->
                subscription.onPurchaseCompleted(purchase, onAllowed = when {
                    addingLocation -> openLocationCreation
                    scanContent.requiresPurchase -> resumeAnalysis
                    else -> ({
                        showingSubscription = false
                        addingLocation = false
                        scan.loadBalance()
                    })
                })
            },
            onDismiss = {
                showingSubscription = false
                scan.dismissPurchase()
                subscription.reset()
            },
        )
    }
}
