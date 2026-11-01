package org.techascent.aifinder.presentation.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import org.techascent.aifinder.domain.model.InventoryItem
import org.techascent.aifinder.domain.model.ItemDetection

class ReviewViewModelTest {
    private val first = ItemDetection(
        photoId = "photo",
        item = InventoryItem("medal", "Medal", "Accessories", listOf("medal")),
        confidence = 0.94f,
        bounds = listOf(0.1f, 0.2f, 0.4f, 0.5f)
    )
    private val second = first.copy(item = InventoryItem("cable", "HDMI cable", "Electronics"))

    @Test
    fun renameTrimsNameAndPreservesDetectionMetadataAndIdentity() {
        val viewModel = ReviewViewModel()
        viewModel.show(listOf(first, second))
        viewModel.rename(first.item.id, "  Running medal  ")
        assertEquals(
            listOf(first.copy(item = first.item.copy(name = "Running medal")), second),
            viewModel.items()
        )
        viewModel.retry()
        assertEquals("Running medal", viewModel.items().first().item.name)
    }

    @Test
    fun blankNamesAreRejectedWithoutChangingResults() {
        val viewModel = ReviewViewModel()
        viewModel.show(listOf(first))
        assertFailsWith<IllegalArgumentException> { viewModel.rename(first.item.id, "  ") }
        assertEquals(listOf(first), viewModel.items())
    }

    @Test
    fun deleteRemovesOnlySelectedItemAndRetryDoesNotRestoreIt() {
        val viewModel = ReviewViewModel()
        viewModel.show(listOf(first, second))
        viewModel.delete(first.item.id)
        viewModel.retry()
        assertEquals(listOf(second), viewModel.items())
        viewModel.delete(second.item.id)
        assertTrue(viewModel.items().isEmpty())
    }

    @Test
    fun newScanReplacesPreviouslyEditedResults() {
        val viewModel = ReviewViewModel()
        viewModel.show(listOf(first))
        viewModel.rename(first.item.id, "My medal")
        viewModel.delete(first.item.id)
        viewModel.show(listOf(second))
        assertEquals(listOf(second), viewModel.items())
    }

    private fun ReviewViewModel.items() = (state as ReviewUiState.Success).data.detections
}
