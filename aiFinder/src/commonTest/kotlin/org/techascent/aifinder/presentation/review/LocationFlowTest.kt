package org.techascent.aifinder.presentation.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.techascent.aifinder.data.MockInventoryRepository
import org.techascent.aifinder.domain.model.CreateLocationRequest
import org.techascent.aifinder.domain.model.InventoryItem
import org.techascent.aifinder.domain.model.ItemDetection
import org.techascent.aifinder.domain.model.LocationType
import org.techascent.aifinder.presentation.location.LocationViewModel

class LocationFlowTest {
    @Test
    fun createsNestedLocationsAndSelectsNewLocation() {
        val repository = MockInventoryRepository()
        val model = LocationViewModel(repository)
        assertTrue(model.create(CreateLocationRequest(" Garage ", LocationType.AREA, "home")))
        val garageId = model.selectedId!!
        assertEquals("Home › Garage", model.selectedPath)
        assertTrue(model.create(CreateLocationRequest("Shelf", LocationType.CONTAINER, garageId)))
        assertEquals("Home › Garage › Shelf", model.selectedPath)
        assertEquals(garageId, model.locations.last().parentId)
    }

    @Test
    fun validatesNameParentAndDuplicateSiblingWithoutChangingSelection() {
        val repository = MockInventoryRepository()
        val model = LocationViewModel(repository)
        val initialId = model.selectedId
        assertFalse(model.create(CreateLocationRequest(" ", LocationType.ROOM)))
        assertEquals(initialId, model.selectedId)
        assertFalse(model.create(CreateLocationRequest("living ROOM", LocationType.ROOM, "home")))
        assertFalse(model.create(CreateLocationRequest("Desk", LocationType.CONTAINER, "missing")))
        assertTrue(model.create(CreateLocationRequest("Living room", LocationType.ROOM, null)))
    }

    @Test
    fun savesOnlyReviewedItemsWithEditedNameAndCorrectPlacement() {
        val repository = MockInventoryRepository()
        val location = repository.createLocation(CreateLocationRequest("Toolbox", LocationType.CONTAINER, "storage"))
        val review = ReviewViewModel(repository)
        review.show(listOf(
            ItemDetection("photo", InventoryItem("one", "Scissors", "Tools")),
            ItemDetection("photo", InventoryItem("two", "Medal", "Accessories"))
        ))
        review.rename("one", "Kitchen scissors")
        review.delete("two")
        review.save(location.id)
        val saved = repository.search("Kitchen scissors").single()
        assertEquals("Kitchen scissors", saved.item.name)
        assertEquals(location.id, saved.location.id)
        assertEquals("Home › Storage room › Toolbox", saved.path)
        assertFalse(repository.items().any { it.item.id == "two" })
        review.save(location.id)
        assertEquals(1, repository.items().count { it.item.id == "one" })
        assertEquals(1, repository.spaces().first { it.id == "storage" }.itemCount)
    }

    @Test
    fun rejectsSavingToUnknownLocationOrSavingEmptyResults() {
        val repository = MockInventoryRepository()
        assertFailsWith<IllegalStateException> { repository.saveDetections("missing", emptyList()) }
        assertFailsWith<IllegalArgumentException> { repository.saveDetections("drawer", emptyList()) }
    }
}
