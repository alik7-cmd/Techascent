package org.techascent.aifinder.presentation.navigation

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import org.techascent.aifinder.presentation.welcome.*
import org.techascent.aifinder.presentation.home.*
import org.techascent.aifinder.presentation.scan.*
import org.techascent.aifinder.presentation.review.*
import org.techascent.aifinder.presentation.search.*
import org.techascent.aifinder.presentation.details.*

/** Lightweight mock navigation. Replace with the app's preferred navigation library later. */
@Composable
fun AiFinderNavigation() {
    var destination by rememberSaveable { mutableStateOf("welcome") }
    val scan = remember { ScanViewModel() }
    val review = remember { ReviewViewModel() }
    val search = remember { SearchViewModel() }
    val details = remember { DetailsViewModel() }
    when (destination) {
        "welcome" -> WelcomeScreen(
            onContinue = { destination = "home" }
        )

        "home" -> HomeScreen(
            onSearch = { destination = "search" },
            onScan = { destination = "scan" },
        )

        "scan" -> ScanScreen(
            scan.state,
            onAnalyze = { destination = "review" },
            onBack = { destination = "home" },
            onRetry = scan::retry
        )

        "review" -> ReviewScreen(
            review.state,
            onSave = { destination = "home" },
            onBack = { destination = "scan" },
            onRetry = review::retry
        )

        "search" -> SearchScreen(
            search.state,
            onQueryChange = search::updateQuery,
            onItemSelected = { id -> details.select(id); destination = "details" },
            onBack = { destination = "home" },
            onRetry = search::retry
        )

        "details" -> DetailsScreen(
            details.state,
            onBack = { destination = "search" },
            onRetry = details::retry
        )
    }
}
