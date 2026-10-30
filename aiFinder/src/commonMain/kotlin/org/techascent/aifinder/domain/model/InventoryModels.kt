package org.techascent.aifinder.domain.model

data class Household(val id: String, val name: String, val memberIds: List<String> = emptyList())
enum class LocationType { HOME, ROOM, CABINET, SHELF, DRAWER, BOX }
data class StorageLocation(
    val id: String,
    val name: String,
    val type: LocationType,
    val parentId: String? = null,
    val householdId: String = "home",
    val spaceCount: Int = 0,
    val itemCount: Int = 0,
    val boxCount: Int? = null
)
data class InventoryItem(val id: String, val name: String, val category: String, val tags: List<String> = emptyList(), val description: String = "")
data class ItemPlacement(val itemId: String, val locationId: String, val confirmedAt: String? = null)
data class InventoryPhoto(val id: String, val locationId: String, val localUri: String, val capturedAt: String? = null)
enum class DetectionStatus { SUGGESTED, CONFIRMED, REJECTED }
data class ItemDetection(val photoId: String, val item: InventoryItem, val confidence: Float? = null, val status: DetectionStatus = DetectionStatus.SUGGESTED, val bounds: List<Float>? = null)
data class ItemEvent(val itemId: String, val previousLocationId: String?, val newLocationId: String?, val occurredAt: String)
enum class SubscriptionTier { FREE, PLUS, LIFETIME }
data class Subscription(val userId: String, val tier: SubscriptionTier = SubscriptionTier.FREE, val scansUsed: Int = 0, val scanLimit: Int = 10)
data class LocatedItem(val item: InventoryItem, val location: StorageLocation, val path: String)
