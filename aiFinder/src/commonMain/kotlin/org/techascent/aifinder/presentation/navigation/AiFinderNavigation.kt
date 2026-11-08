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
import org.techascent.aifinder.presentation.inventory.InventoryActionsViewModel
import org.techascent.aifinder.presentation.inventory.ItemEditorSheet
import org.techascent.aifinder.domain.model.LocatedItem
import org.techascent.aifinder.presentation.inventory.InventorySyncViewModel

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
    val inventoryActions = koinViewModel<InventoryActionsViewModel>()
    val inventorySync = koinViewModel<InventorySyncViewModel>()
    val syncState by inventorySync.state.collectAsState()
    val inventoryActionState by inventoryActions.state.collectAsState()
    var editingItem by remember { mutableStateOf(false) }
    var editorEntry by remember { mutableStateOf<LocatedItem?>(null) }
    var editorLocationId by remember { mutableStateOf<String?>(null) }
    var movingItem by remember { mutableStateOf(false) }
    var showingQr by remember { mutableStateOf(false) }
    var qrPlaceId by remember { mutableStateOf<String?>(null) }
    var pendingScanPlaceId by remember { mutableStateOf<String?>(null) }
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
    var creatingLocationFromHome by rememberSaveable { mutableStateOf(false) }
    val openLocationCreation: () -> Unit = {
        preparingPurchase = false
        showingSubscription = false
        subscription.reset()
        creatingLocationFromHome = true
        choosingLocation = true
    }
    val scanContent = ((scanState as? ScanUiState.Success)?.data ?: ScanContent()).copy(
        isPreparingPurchase = preparingPurchase,
        purchasePreparationError = purchasePreparationError,
    )
    val displayedScanState = (scanState as? ScanUiState.Success)?.copy(data = scanContent) ?: scanState
    val preparePurchase: () -> Unit = {
        if (!preparingPurchase) {
            subscription.reset()
            purchaseOrigin = backStackEntry?.id
            purchasePreparationError = false
            preparingPurchase = true
            subscription.refresh()
        }
    }
    val addLocation: () -> Unit = {
        openLocationCreation()
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
            subscription.refresh(onAllowed = if (scanContent.requiresPurchase) resumeAnalysis else null)
        }
    }
    LaunchedEffect(scanContent.requiresPurchase) {
        if (scanContent.requiresPurchase && destination == AiFinderScreen.Capture.route) {
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
            creatingLocationFromHome = false
        }
        when (destination) {
            AiFinderScreen.Home.route -> {
                home.start()
                scan.loadBalance()
                inventorySync.refresh()
            }
            AiFinderScreen.Scan.route -> { scan.loadBalance(); locations.start() }
            AiFinderScreen.Search.route -> search.start()
            AiFinderScreen.Details.route -> details.start()
            AiFinderScreen.Spaces.route, AiFinderScreen.Space.route -> spaces.start()
        }
        if (destination != AiFinderScreen.Capture.route && scanContent.requiresPurchase) {
            scan.dismissPurchase()
            showingSubscription = false
            subscription.reset()
        }
    }
    val onBack: () -> Unit = {
        if (destination == AiFinderScreen.Scan.route || destination == AiFinderScreen.Capture.route) {
            scan.cancelAnalysis()
            pendingScanPlaceId = null
        }
        subscription.reset()
        navController.popBackStack()
    }
    LaunchedEffect(pendingScanPlaceId, locationState.locations) {
        pendingScanPlaceId?.takeIf { id -> locationState.locations.any { it.id == id } }?.let {
            locations.select(it)
            pendingScanPlaceId = null
        }
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
    if (editingItem) {
        LaunchedEffect(Unit) { locations.start() }
        ItemEditorSheet(
            entry = editorEntry,
            initialLocationId = editorLocationId,
            locations = locationState,
            action = inventoryActionState,
            moveOnly = movingItem,
            onSave = { item, place ->
                inventoryActions.save(editorEntry, item, place) { editingItem = false; editorEntry = null }
            },
            onDismiss = { editingItem = false; editorEntry = null },
        )
    }
    if (showingQr) {
        LaunchedEffect(Unit) { locations.start() }
        PlaceQrSheet(
            placeId = qrPlaceId,
            placePath = qrPlaceId?.let(locationState::path).orEmpty(),
            knownPlaceIds = locationState.locations.map { it.id }.toSet(),
            onOpenPlace = {
                showingQr = false
                navController.navigate(AiFinderScreen.Space.createRoute(it)) { launchSingleTop = true }
            },
            onDismiss = { showingQr = false },
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
                syncState = syncState,
                onSync = inventorySync::refresh,
                onDismissSyncError = inventorySync::dismissError,
                onAddLocation = addLocation,
                onAccount = { navController.navigate(AiFinderScreen.Account.route) },
                onSpace = { navController.navigate(AiFinderScreen.Space.createRoute(it)) },
                onSeeAll = { navController.navigate(AiFinderScreen.Spaces.route) },
                onScanLabel = { qrPlaceId = null; showingQr = true },
                onItem = { navController.navigate(AiFinderScreen.Details.createRoute(it)) },
            )
        }
        composable(AiFinderScreen.Spaces.route) {
            SpacesScreen(
                allSpacesState,
                onSpace = { navController.navigate(AiFinderScreen.Space.createRoute(it)) },
                onScan = openScanner,
                onBack = onBack,
                onRetry = spaces::retry,
                onAddPlace = addLocation,
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
                onScan = { pendingScanPlaceId = id; openScanner() },
                onBack = onBack,
                onRetry = spaces::retry,
                onAddItem = {
                    editorEntry = null
                    editorLocationId = id
                    movingItem = false
                    inventoryActions.clearError()
                    editingItem = true
                },
                onQrLabel = { qrPlaceId = id; showingQr = true },
            )
        }
        composable(AiFinderScreen.Account.route) {
            AccountScreen(
                viewModel = account,
                onBack = onBack,
                onSubscription = { navController.navigate(AiFinderScreen.PurchaseHistory.route) { launchSingleTop = true } },
                onPrivacy = { navController.navigate(AiFinderScreen.PrivacyData.route) { launchSingleTop = true } },
                onExport = { navController.navigate(AiFinderScreen.ExportInventory.route) { launchSingleTop = true } },
                onFeedback = { navController.navigate(AiFinderScreen.Feedback.route) { launchSingleTop = true } },
                onSignedOut = {
                    scan.reset()
                    inventorySync.reset()
                    inventoryActions.reset()
                    review.reset()
                    locations.reset()
                    home.reset()
                    search.reset()
                    details.reset()
                    spaces.reset()
                    choosingLocation = false
                    editingItem = false
                    showingQr = false
                    pendingScanPlaceId = null
                    editorEntry = null
                    showingSubscription = false
                    subscription.reset()
                    replaceRoot(AiFinderScreen.Login)
                },
            )
        }
        composable(AiFinderScreen.PurchaseHistory.route) {
            PurchaseHistoryScreen(viewModel = koinViewModel<PurchaseHistoryViewModel>(), onBack = onBack)
        }
        composable(AiFinderScreen.PrivacyData.route) { PrivacyDataScreen(onBack) }
        composable(AiFinderScreen.ExportInventory.route) { ExportInventoryScreen(onBack) }
        composable(AiFinderScreen.Feedback.route) { FeedbackScreen(onBack) }
        composable(AiFinderScreen.Scan.route) {
            ScanScreen(
                onOpenCamera = {
                    if (locationState.selectedLocation != null) {
                        navController.navigate(AiFinderScreen.Capture.route) { launchSingleTop = true }
                    }
                },
                onBack = onBack,
                selectedLocation = locationState.selectedPath,
                onChooseLocation = { choosingLocation = true },
            )
        }
        composable(AiFinderScreen.Capture.route) {
            ScanCaptureScreen(
                displayedScanState,
                onAnalyze = scan::analyze,
                onCapture = scan::capture,
                onRetake = scan::retake,
                onBack = onBack,
                selectedLocation = locationState.selectedPath,
                onChooseLocation = { choosingLocation = true },
            )
        }
        composable(AiFinderScreen.Review.route) {
            LaunchedEffect(locationState.selectedId) { review.setLocation(locationState.selectedId) }
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
                onRescanChoice = review::choose,
            )
        }
        composable(AiFinderScreen.Search.route) {
            SearchScreen(
                searchState,
                onQueryChange = search::updateQuery,
                onItemSelected = { navController.navigate(AiFinderScreen.Details.createRoute(it)) },
                onBack = onBack,
                onRetry = search::retry,
                onConfirm = inventoryActions::confirm,
                onMove = {
                    editorEntry = it; movingItem = true; inventoryActions.clearError(); editingItem = true
                },
                busy = inventoryActionState.busy,
                actionError = inventoryActionState.error,
            )
        }
        composable(
            AiFinderScreen.Details.route,
            arguments = listOf(navArgument(AiFinderScreen.Details.ITEM_ID) { type = NavType.StringType }),
        ) { entry ->
            val id = checkNotNull(entry.arguments?.read { getString(AiFinderScreen.Details.ITEM_ID) })
            LaunchedEffect(id) { details.select(id) }
            val selected = (detailsState as? DetailsUiState.Success)?.data?.entry
            DetailsScreen(
                detailsState, onBack = onBack, onRetry = details::retry,
                onEdit = {
                    editorEntry = selected; movingItem = false; inventoryActions.clearError(); editingItem = true
                },
                onMove = {
                    editorEntry = selected; movingItem = true; inventoryActions.clearError(); editingItem = true
                },
                onDelete = { selected?.let { inventoryActions.delete(it, onBack) } },
                onConfirm = { selected?.let(inventoryActions::confirm) },
                onDeletePhoto = { selected?.let(inventoryActions::deletePhoto) },
                busy = inventoryActionState.busy,
                actionError = inventoryActionState.error,
            )
        }
    }
    // Registered after NavHost so hardware Back also cancels an active scan before popping.
    SystemBackHandler(
        enabled = !showingSubscription && !choosingLocation && !editingItem && !showingQr && navController.previousBackStackEntry != null,
        onBack = onBack,
    )
    if (showingSubscription) {
        SubscriptionSheet(
            state = subscriptionState,
            onRetry = {
                subscription.refresh(onAllowed = if (scanContent.requiresPurchase) resumeAnalysis else null)
            },
            onPurchaseCompleted = { purchase ->
                subscription.onPurchaseCompleted(purchase, onAllowed = when {
                    scanContent.requiresPurchase -> resumeAnalysis
                    else -> ({
                        showingSubscription = false
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
