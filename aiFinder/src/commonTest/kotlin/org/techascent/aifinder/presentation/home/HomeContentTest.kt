package org.techascent.aifinder.presentation.home

import org.techascent.aifinder.domain.inventory.InventorySnapshot
import org.techascent.aifinder.domain.model.InventoryItem
import org.techascent.aifinder.domain.model.LocatedItem
import org.techascent.aifinder.domain.model.LocationType
import org.techascent.aifinder.domain.model.StorageLocation
import kotlin.test.*

class HomeContentTest {
    @Test
    fun emptyInventoryHasNoInventedTotalsOrRecentItems() {
        val content = InventorySnapshot().toHomeContent()
        assertEquals(0, content.placeCount)
        assertEquals(0, content.itemCount)
        assertTrue(content.spaces.isEmpty())
        assertTrue(content.recentItems.isEmpty())
    }

    @Test
    fun totalsIncludeEntireInventoryAndRecentsUseCreationNotConfirmation() {
        val places = (1..5).map { StorageLocation("p$it", "Place $it", LocationType.DRAWER,
            parentId = if (it == 1) null else "p1") }
        val items = (1..5).map {
            LocatedItem(InventoryItem("i$it", "Item $it", "General", photoId = "photo$it"),
                places.last(), "Place 1 › Place 5", createdAt = it.toLong(),
                confirmedAt = (100 - it).toLong())
        }
        val content = InventorySnapshot(places, items).toHomeContent()
        assertEquals(5, content.placeCount)
        assertEquals(5, content.itemCount)
        assertEquals(3, content.spaces.size)
        assertEquals(listOf("i5", "i4", "i3"), content.recentItems.map { it.item.id })
        assertEquals("Place 1", content.spaces[1].parentPath)
    }

    @Test
    fun placeCoverUsesLatestPhotoFromThatPlace() {
        val place = StorageLocation("p", "Room", LocationType.ROOM)
        val items = listOf(
            LocatedItem(InventoryItem("old", "Old", "", photoId = "old-photo"), place, "Room", 1),
            LocatedItem(InventoryItem("new", "New", "", photoId = "new-photo"), place, "Room", 2),
            LocatedItem(InventoryItem("manual", "Manual", ""), place, "Room", 3),
        )
        assertEquals("new-photo", InventorySnapshot(listOf(place), items)
            .toHomeContent().spaces.single().coverPhotoId)
    }
}
