package org.techascent.aifinder.presentation.navigation

import io.ktor.http.encodeURLPathPart

sealed class AiFinderScreen(val route: String) {
    data object Welcome : AiFinderScreen("welcome")
    data object Login : AiFinderScreen("login")
    data object Home : AiFinderScreen("home")
    data object Spaces : AiFinderScreen("spaces")
    data object Space : AiFinderScreen("space/{spaceId}") {
        const val SPACE_ID = "spaceId"
        fun createRoute(id: String) = "space/${id.encodeURLPathPart()}"
    }
    data object Account : AiFinderScreen("account")
    data object PrivacyData : AiFinderScreen("privacyData")
    data object PurchaseHistory : AiFinderScreen("purchaseHistory")
    data object Scan : AiFinderScreen("scan")
    data object Capture : AiFinderScreen("scan/capture")
    data object Review : AiFinderScreen("review")
    data object Search : AiFinderScreen("search")
    data object Details : AiFinderScreen("details/{itemId}") {
        const val ITEM_ID = "itemId"
        fun createRoute(id: String) = "details/${id.encodeURLPathPart()}"
    }
}
