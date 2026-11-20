package org.techascent.aifinder.data.inventory

import org.techascent.aifinder.domain.inventory.StoredItem
import org.techascent.aifinder.domain.model.InventoryItem
import org.techascent.aifinder.domain.model.ItemDetection

/** Shape of a node under `aifinder/users/{uid}/items/{itemId}` in Realtime Database. */
data class ItemDto(
    val name: String,
    val category: String,
    val tags: List<String>,
    val locationId: String,
    val photoId: String?,
    val confidence: Float?,
    val createdAt: Long,
    val description: String = "",
    val confirmedAt: Long = createdAt,
    val bounds: List<Float>? = null,
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "name" to name,
        "category" to category,
        "tags" to tags,
        "locationId" to locationId,
        "photoId" to photoId,
        "confidence" to confidence?.toDouble(),
        "createdAt" to createdAt,
        "description" to description,
        "confirmedAt" to confirmedAt,
        "bounds" to bounds?.map { it.toDouble() },
    )

    fun toDomain(id: String) = StoredItem(
        item = InventoryItem(id = id, name = name, category = category, tags = tags, photoId = photoId, description = description),
        locationId = locationId,
        createdAt = createdAt,
        confirmedAt = confirmedAt,
        bounds = bounds,
    )

    companion object {
        fun from(detection: ItemDetection, locationId: String, createdAt: Long) = ItemDto(
            name = detection.item.name.trim(),
            category = detection.item.category,
            tags = detection.item.tags,
            locationId = locationId,
            photoId = detection.photoId.takeIf { it.isNotBlank() },
            confidence = detection.confidence,
            createdAt = createdAt,
            description = detection.item.description,
            bounds = detection.bounds,
        )

        /** Returns null for malformed nodes so one bad record can't break the whole list. */
        fun fromMap(value: Map<*, *>): ItemDto? {
            val name = (value["name"] as? String)?.takeIf { it.isNotBlank() } ?: return null
            val locationId = (value["locationId"] as? String)?.takeIf { it.isNotBlank() } ?: return null
            return ItemDto(
                name = name,
                category = value["category"] as? String ?: "General",
                tags = (value["tags"] as? List<*>).orEmpty().filterIsInstance<String>(),
                locationId = locationId,
                photoId = value["photoId"] as? String,
                confidence = (value["confidence"] as? Number)?.toFloat(),
                createdAt = (value["createdAt"] as? Number)?.toLong() ?: 0L,
                description = value["description"] as? String ?: "",
                confirmedAt = (value["confirmedAt"] as? Number)?.toLong()
                    ?: (value["createdAt"] as? Number)?.toLong() ?: 0L,
                bounds = (value["bounds"] as? List<*>)?.mapNotNull { (it as? Number)?.toFloat() },
            )
        }
    }
}
