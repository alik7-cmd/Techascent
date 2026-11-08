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
import org.techascent.aifinder.domain.inventory.usecase.ObserveInventoryUseCase
import org.techascent.aifinder.domain.location.LocationRepository
import org.techascent.aifinder.domain.model.CreateLocationRequest
import org.techascent.aifinder.domain.model.StorageLocation
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
        val existing = MutableStateFlow<List<StoredItem>>(emptyList())
        var replacements: Map<String, StoredItem> = emptyMap()
        override suspend fun refresh(): Result<Unit> = failWith?.let { Result.failure(it) } ?: Result.success(Unit)
        override fun observeItems(): Flow<List<StoredItem>> = existing
        override suspend fun saveReviewed(locationId: String, detections: List<ItemDetection>, replacements: Map<String, StoredItem>): Result<List<StoredItem>> {
            this.replacements = replacements
            return saveDetections(locationId, detections)
        }
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

    private fun observe(inventory: InventoryRepository) = ObserveInventoryUseCase(inventory, object : LocationRepository {
        override fun observeLocations(): Flow<List<StorageLocation>> = MutableStateFlow(emptyList())
        override suspend fun createLocation(request: CreateLocationRequest): Result<StorageLocation> = error("Unused")
    })

    @Test
    fun rescanDefaultsToKeepingExistingWithoutUploadingOrWriting() {
        val inventory = FakeInventory()
        val original = StoredItem(detections.first().item.copy(id = "old"), "place", 10)
        inventory.existing.value = listOf(original)
        val photos = FakePhotos()
        val review = ReviewViewModel(SaveDetectionsUseCase(inventory, photos), observe(inventory))
        review.show(detections.take(1), CapturedPhoto("photo", byteArrayOf(1)))
        review.setLocation("place")
        var callbacks = 0
        review.save("place") { callbacks++ }
        assertEquals(1, callbacks)
        assertTrue(inventory.saved.isEmpty())
        assertTrue(photos.saved.isEmpty())
        assertEquals(listOf(original), inventory.existing.value)
    }

    @Test
    fun rescanUpdateMustBeExplicitAndSeparateAddDoesNotReplace() {
        val inventory = FakeInventory()
        val old = StoredItem(detections.first().item.copy(id = "old"), "place", 10)
        inventory.existing.value = listOf(old)
        val review = ReviewViewModel(SaveDetectionsUseCase(inventory, FakePhotos()), observe(inventory))
        review.show(detections.take(1))
        review.setLocation("place")
        review.choose("one", RescanChoice.UPDATE)
        review.save("place") {}
        assertEquals(mapOf("one" to old), inventory.replacements)
        review.show(detections.take(1))
        review.setLocation("place")
        review.choose("one", RescanChoice.ADD)
        review.save("place") {}
        assertTrue(inventory.replacements.isEmpty())
        assertEquals(2, inventory.saved.size)
    }

    @Test
    fun ambiguousRescanNeverOverwritesAnArbitrarySameNameItem() {
        val inventory = FakeInventory()
        inventory.existing.value = listOf("old-a", "old-b").map {
            StoredItem(detections.first().item.copy(id = it), "place", 10)
        }

        val review = ReviewViewModel(SaveDetectionsUseCase(inventory, FakePhotos()), observe(inventory))
        review.show(detections.take(1))
        review.setLocation("place")
        assertEquals(setOf("one"), review.content().ambiguousMatches)
        review.choose("one", RescanChoice.UPDATE)
        assertTrue(review.content().choices.isEmpty())
        review.save("place") {}
        assertTrue(inventory.saved.isEmpty())
        assertEquals(2, inventory.existing.value.size)
    }

    @Test
    fun cannotSaveWhenFreshDuplicateCheckFailsAndRetryKeepsResults() {
        val inventory = FakeInventory(InventoryError.Remote("offline"))
        val review = ReviewViewModel(SaveDetectionsUseCase(inventory, FakePhotos()), observe(inventory))
        review.show(detections.take(1))
        review.setLocation("place")
        review.save("place") { error("Must not save before checking existing items") }
        assertTrue(review.content().saveError != null)
        assertTrue(inventory.saved.isEmpty())
        assertEquals(1, review.content().detections.size)
        inventory.failWith = null
        review.retry()
        review.save("place") {}
        assertEquals(1, inventory.saved.size)
    }

    private fun ReviewViewModel.content() = (state as ReviewUiState.Success).data
}
