package org.techascent.aifinder.domain.inventory.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.techascent.aifinder.domain.inventory.InventoryError
import org.techascent.aifinder.domain.inventory.InventoryRepository
import org.techascent.aifinder.domain.inventory.InventorySnapshot
import org.techascent.aifinder.domain.inventory.StoredItem
import org.techascent.aifinder.domain.inventory.buildSnapshot
import org.techascent.aifinder.domain.location.LocationRepository
import org.techascent.aifinder.domain.model.ItemDetection
import org.techascent.aifinder.domain.photo.ItemPhotoRepository
import org.techascent.aifinder.domain.vision.CapturedPhoto

class ObserveInventoryUseCase(
    private val items: InventoryRepository,
    private val locations: LocationRepository,
) {
    operator fun invoke(): Flow<InventorySnapshot> =
        combine(locations.observeLocations(), items.observeItems(), ::buildSnapshot)
}

/** Uploads the scan photo (when there is one) before saving its items, so saved items never point at a missing photo. */
class SaveDetectionsUseCase(
    private val repository: InventoryRepository,
    private val photos: ItemPhotoRepository,
) {
    suspend operator fun invoke(
        locationId: String?,
        detections: List<ItemDetection>,
        photo: CapturedPhoto? = null,
    ): Result<List<StoredItem>> {
        if (locationId.isNullOrBlank()) return Result.failure(InventoryError.NoLocation)
        val valid = detections.filter { it.item.name.isNotBlank() }
        if (valid.isEmpty()) return Result.failure(InventoryError.NothingToSave)
        if (photo != null) photos.save(photo.id, photo.jpegBytes).onFailure { return Result.failure(it) }
        return repository.saveDetections(locationId, valid)
    }
}
