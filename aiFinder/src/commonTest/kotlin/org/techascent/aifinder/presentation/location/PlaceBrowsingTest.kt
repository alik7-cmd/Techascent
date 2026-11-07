package org.techascent.aifinder.presentation.location

import org.techascent.aifinder.domain.model.LocationType
import org.techascent.aifinder.domain.model.StorageLocation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PlaceBrowsingTest {
    private val state = LocationUiState(locations = listOf(
        StorageLocation("home", "Home", LocationType.HOME),
        StorageLocation("bedroom", "Bedroom", LocationType.ROOM, "home"),
        StorageLocation("wardrobe", "Wardrobe", LocationType.CONTAINER, "bedroom"),
        StorageLocation("drawer", "Top drawer", LocationType.DRAWER, "wardrobe"),
        StorageLocation("garage", "Garage", LocationType.AREA),
        StorageLocation("orphan", "Separate box", LocationType.BOX, "missing"),
    ))

    @Test
    fun rootIncludesStandalonePlacesAndChildrenOfLegacyHome() {
        assertEquals(listOf("bedroom", "garage", "orphan"), state.placesInside(null).map { it.id })
    }

    @Test
    fun browsingShowsOnlyDirectChildrenAndPreservesLegacyTypes() {
        assertEquals(listOf("wardrobe"), state.placesInside("bedroom").map { it.id })
        assertEquals(listOf("drawer"), state.placesInside("wardrobe").map { it.id })
        assertTrue(state.placesInside("drawer").isEmpty())
    }

    @Test
    fun breadcrumbsExcludeHomeAndIncludeEveryVisibleAncestor() {
        assertEquals(listOf("bedroom", "wardrobe", "drawer"), state.ancestors("drawer").map { it.id })
        assertTrue(state.ancestors(null).isEmpty())
        assertTrue(state.ancestors("missing").isEmpty())
    }

    @Test
    fun malformedCycleDoesNotLoop() {
        val cyclic = LocationUiState(locations = listOf(
            StorageLocation("a", "A", LocationType.CONTAINER, "b"),
            StorageLocation("b", "B", LocationType.CONTAINER, "a"),
        ))
        assertEquals(2, cyclic.ancestors("a").size)
    }
}
