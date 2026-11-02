package org.techascent.aifinder.presentation.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import org.techascent.aifinder.data.MockInventoryRepository
import org.techascent.aifinder.domain.model.InventoryItem
import org.techascent.aifinder.domain.model.ItemDetection
import org.techascent.aifinder.domain.model.LocationType
import org.techascent.aifinder.domain.model.StorageLocation

class LocationFlowTest {
    private val toolbox = StorageLocation("-Nxyz", "Toolbox", LocationType.CONTAINER, "-Nabc")

    @Test
    fun savesOnlyReviewedItemsWithEditedNameAndCorrectPlacement() {
        val repository = MockInventoryRepository()
        val review = ReviewViewModel(repository)
        review.show(listOf(
            ItemDetection("photo", InventoryItem("one", "Scissors", "Tools")),
            ItemDetection("photo", InventoryItem("two", "Medal", "Accessories"))
        ))
        review.rename("one", "Kitchen scissors")
        review.delete("two")
        review.save(toolbox, "Garage › Toolbox")
        val saved = repository.search("Kitchen scissors").single()
        assertEquals("Kitchen scissors", saved.item.name)
        assertEquals(toolbox.id, saved.location.id)
        assertEquals("Garage › Toolbox", saved.path)
        assertFalse(repository.items().any { it.item.id == "two" })
        review.save(toolbox, "Garage › Toolbox")
        assertEquals(1, repository.items().count { it.item.id == "one" })
    }

    @Test
    fun rejectsSavingEmptyResults() {
        assertFailsWith<IllegalArgumentException> {
            MockInventoryRepository().saveDetections(toolbox, "Toolbox", emptyList())
        }
    }
}
