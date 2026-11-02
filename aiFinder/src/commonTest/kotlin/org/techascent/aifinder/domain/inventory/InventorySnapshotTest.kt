package org.techascent.aifinder.domain.inventory

import org.techascent.aifinder.domain.model.InventoryItem
import org.techascent.aifinder.domain.model.LocationType
import org.techascent.aifinder.domain.model.StorageLocation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InventorySnapshotTest {
    private val garage = StorageLocation("g", "Garage", LocationType.AREA)
    private val toolbox = StorageLocation("t", "Toolbox", LocationType.CONTAINER, parentId = "g")
    private val drawer = StorageLocation("d", "Top drawer", LocationType.CONTAINER, parentId = "t")
    private val kitchen = StorageLocation("k", "Kitchen", LocationType.ROOM)

    private fun stored(id: String, name: String, locationId: String, category: String = "Tools", tags: List<String> = emptyList()) =
        StoredItem(InventoryItem(id, name, category, tags), locationId, createdAt = 0)

    private val snapshot = buildSnapshot(
        locations = listOf(garage, toolbox, drawer, kitchen),
        items = listOf(
            stored("1", "Scissors", "d"),
            stored("2", "Hammer", "t"),
            stored("3", "HDMI cable", "k", category = "Electronics", tags = listOf("hdmi")),
            stored("4", "Orphan", "missing"),
        ),
    )

    @Test
    fun joinsItemsWithPathsAndSkipsItemsWithoutLocation() {
        assertEquals(listOf("1", "2", "3"), snapshot.items.map { it.item.id })
        assertEquals("Garage › Toolbox › Top drawer", snapshot.item("1")!!.path)
    }

    @Test
    fun topLevelSpacesCountDescendantsAndTheirItems() {
        val spaces = snapshot.topLevelSpaces.associateBy { it.id }
        assertEquals(setOf("g", "k"), spaces.keys)
        assertEquals(2, spaces.getValue("g").spaceCount)
        assertEquals(2, spaces.getValue("g").itemCount)
        assertEquals(1, spaces.getValue("k").itemCount)
    }

    @Test
    fun allSpacesAreOrderedByPathWithCounts() {
        assertEquals(listOf("g", "t", "d", "k"), snapshot.allSpaces.map { it.id })
        assertEquals(1, snapshot.allSpaces.first { it.id == "t" }.spaceCount)
    }

    @Test
    fun itemsInIncludesNestedLocations() {
        assertEquals(setOf("1", "2"), snapshot.itemsIn("t").map { it.item.id }.toSet())
        assertEquals(listOf("1"), snapshot.itemsIn("d").map { it.item.id })
        assertTrue(snapshot.itemsIn("unknown").isEmpty())
    }

    @Test
    fun searchMatchesNameCategoryTagsAndPathIgnoringQuestionWords() {
        assertEquals(listOf("3"), snapshot.search("Where is my HDMI cable?").map { it.item.id })
        assertEquals(listOf("3"), snapshot.search("electronics").map { it.item.id })
        assertEquals(setOf("1", "2"), snapshot.search("toolbox").map { it.item.id }.toSet())
        assertEquals(listOf("1"), snapshot.search("drawer scis").map { it.item.id })
        assertEquals(3, snapshot.search("  ").size)
        assertTrue(snapshot.search("bicycle").isEmpty())
    }
}
