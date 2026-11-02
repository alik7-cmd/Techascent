package org.techascent.aifinder.domain.location

import org.techascent.aifinder.domain.model.CreateLocationRequest
import org.techascent.aifinder.domain.model.LocationType
import org.techascent.aifinder.domain.model.StorageLocation

sealed class LocationError(message: String) : Exception(message) {
    data object BlankName : LocationError("Enter a location name.")
    data object InvalidType : LocationError("Choose Area, Room or Container.")
    data object ParentNotFound : LocationError("Parent location not found.")
    data object Duplicate : LocationError("A location with this name already exists here.")
    data object NotSignedIn : LocationError("Sign in to sync your locations.")
    data object Unavailable : LocationError("Location sync isn't available on this device yet.")
    class Remote(message: String?) : LocationError(message ?: "Couldn't reach the database. Try again.")
}

val CreatableLocationTypes = listOf(LocationType.AREA, LocationType.ROOM, LocationType.CONTAINER)

/** Returns the trimmed request, or throws a [LocationError] describing the first rule it breaks. */
fun CreateLocationRequest.validated(existing: List<StorageLocation>): CreateLocationRequest {
    val trimmed = name.trim()
    if (trimmed.isEmpty()) throw LocationError.BlankName
    if (type !in CreatableLocationTypes) throw LocationError.InvalidType
    if (parentId != null && existing.none { it.id == parentId }) throw LocationError.ParentNotFound
    if (existing.any { it.parentId == parentId && it.name.equals(trimmed, ignoreCase = true) }) {
        throw LocationError.Duplicate
    }
    return copy(name = trimmed)
}

/** Builds "Parent › Child" from root to [id]; tolerates missing parents and cycles in remote data. */
fun List<StorageLocation>.pathOf(id: String): String {
    val byId = associateBy { it.id }
    val segments = ArrayDeque<String>()
    val visited = mutableSetOf<String>()
    var current = byId[id]
    while (current != null && visited.add(current.id)) {
        segments.addFirst(current.name)
        current = current.parentId?.let(byId::get)
    }
    return segments.joinToString(" › ")
}
