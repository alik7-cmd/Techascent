package org.techascent.aifinder.data.location

import org.techascent.aifinder.domain.model.CreateLocationRequest
import org.techascent.aifinder.domain.model.LocationType
import org.techascent.aifinder.domain.model.StorageLocation

/** Shape of a node under `aifinder/users/{uid}/locations/{locationId}` in Realtime Database. */
data class LocationDto(
    val name: String,
    val type: String,
    val parentId: String?,
    val createdAt: Long,
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "name" to name,
        "type" to type,
        "parentId" to parentId,
        "createdAt" to createdAt,
    )

    fun toDomain(id: String) = StorageLocation(
        id = id,
        name = name,
        type = LocationType.entries.firstOrNull { it.name == type } ?: LocationType.CONTAINER,
        parentId = parentId,
    )

    companion object {
        fun from(request: CreateLocationRequest, createdAt: Long) = LocationDto(
            name = request.name,
            type = request.type.name,
            parentId = request.parentId,
            createdAt = createdAt,
        )

        /** Returns null for malformed nodes so one bad record can't break the whole list. */
        fun fromMap(value: Map<*, *>): LocationDto? {
            val name = (value["name"] as? String)?.takeIf { it.isNotBlank() } ?: return null
            return LocationDto(
                name = name,
                type = value["type"] as? String ?: LocationType.CONTAINER.name,
                parentId = value["parentId"] as? String,
                createdAt = (value["createdAt"] as? Number)?.toLong() ?: 0L,
            )
        }
    }
}
