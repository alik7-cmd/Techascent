package org.techascent.aifinder.presentation.review

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.techascent.aifinder.domain.inventory.InventoryError
import org.techascent.aifinder.domain.inventory.InventoryRepository
import org.techascent.aifinder.domain.inventory.StoredItem
import org.techascent.aifinder.domain.inventory.usecase.SaveDetectionsUseCase
import org.techascent.aifinder.domain.model.InventoryItem
import org.techascent.aifinder.domain.model.ItemDetection
import org.techascent.aifinder.domain.photo.ItemPhotoRepository
import org.techascent.aifinder.domain.photo.PhotoError
import org.techascent.aifinder.domain.vision.CapturedPhoto
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LocationFlowTest {
    private class FakeInventory(var failWith: Exception? = null) : InventoryRepository {
        val saved = mutableListOf<Pair<String, List<ItemDetection>>>()
        override fun observeItems(): Flow<List<StoredItem>> = MutableStateFlow(emptyList())
        override suspend fun saveDetections(locationId: String, detections: List<ItemDetection>): Result<List<StoredItem>> {
            failWith?.let { return Result.failure(it) }
            saved += locationId to detections
            return Result.success(detections.map { StoredItem(it.item, locationId, 0) })
        }
    }

    private class FakePhotos(var failWith: Exception? = null) : ItemPhotoRepository {
        val saved = mutableListOf<String>()
        override suspend fun save(photoId: String, jpegBytes: ByteArray): Result<Unit> {
            failWith?.let { return Result.failure(it) }
            saved += photoId
            return Result.success(Unit)
        }
        override suspend fun load(photoId: String): ByteArray? = null
    }

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private val detections = listOf(
        ItemDetection("photo", InventoryItem("one", "Scissors", "Tools")),
        ItemDetection("photo", InventoryItem("two", "Medal", "Accessories"))
    )

    @Test
    fun savesOnlyReviewedItemsWithEditedNameIntoChosenLocation() {
        val inventory = FakeInventory()
        val photos = FakePhotos()
        val review = ReviewViewModel(SaveDetectionsUseCase(inventory, photos))
        var savedCallbacks = 0
        review.show(detections, CapturedPhoto("photo", byteArrayOf(1)))
        review.rename("one", "Kitchen scissors")
        review.delete("two")
        review.save("-Nxyz") { savedCallbacks++ }

        val (locationId, items) = inventory.saved.single()
        assertEquals("-Nxyz", locationId)
        assertEquals(listOf("Kitchen scissors"), items.map { it.item.name })
        assertEquals(1, savedCallbacks)
        assertEquals(listOf("photo"), photos.saved)
        assertTrue(review.content().detections.isEmpty())
    }

    @Test
    fun failedSaveKeepsItemsAndShowsError() {
        val inventory = FakeInventory(failWith = InventoryError.Remote("offline"))
        val photos = FakePhotos()
        val review = ReviewViewModel(SaveDetectionsUseCase(inventory, photos))
        review.show(detections)
        review.save("-Nxyz") { error("should not navigate") }
        assertEquals("offline", review.content().saveError)
        assertFalse(review.content().isSaving)
        assertEquals(2, review.content().detections.size)
    }

    @Test
    fun rejectsSavingWithoutLocationOrItems() {
        val inventory = FakeInventory()
        val photos = FakePhotos()
        val review = ReviewViewModel(SaveDetectionsUseCase(inventory, photos))
        review.show(detections)
        review.save(null) { error("should not navigate") }
        assertEquals(InventoryError.NoLocation.message, review.content().saveError)

        review.show(emptyList())
        review.save("-Nxyz") { error("should not navigate") }
        assertEquals(InventoryError.NothingToSave.message, review.content().saveError)
        assertTrue(inventory.saved.isEmpty())
    }

    @Test
    fun failedPhotoUploadDoesNotSaveItems() {
        val inventory = FakeInventory()
        val photos = FakePhotos(failWith = PhotoError.Remote("storage denied"))
        val review = ReviewViewModel(SaveDetectionsUseCase(inventory, photos))
        review.show(detections, CapturedPhoto("photo", byteArrayOf(1)))
        review.save("-Nxyz") { error("should not navigate") }
        assertEquals("storage denied", review.content().saveError)
        assertTrue(inventory.saved.isEmpty())
    }

    private fun ReviewViewModel.content() = (state as ReviewUiState.Success).data
}
