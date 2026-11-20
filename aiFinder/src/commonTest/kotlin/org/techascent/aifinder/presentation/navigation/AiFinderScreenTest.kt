package org.techascent.aifinder.presentation.navigation

import kotlin.test.Test
import kotlin.test.assertEquals

class AiFinderScreenTest {
    @Test
    fun spaceRouteIncludesItsId() {
        assertEquals("space/space-123", AiFinderScreen.Space.createRoute("space-123"))
        assertEquals("space/{spaceId}", AiFinderScreen.Space.route)
        assertEquals("spaceId", AiFinderScreen.Space.SPACE_ID)
    }

    @Test
    fun detailsRouteIncludesItsId() {
        assertEquals("details/item-123", AiFinderScreen.Details.createRoute("item-123"))
        assertEquals("details/{itemId}", AiFinderScreen.Details.route)
        assertEquals("itemId", AiFinderScreen.Details.ITEM_ID)
    }

    @Test
    fun routeIdsAreEncodedAsSinglePathSegments() {
        assertEquals("space/room%2Fbox%20%231%3F", AiFinderScreen.Space.createRoute("room/box #1?"))
        assertEquals("details/item%2F50%25", AiFinderScreen.Details.createRoute("item/50%"))
    }
}
