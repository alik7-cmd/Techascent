package org.techascent.aifinder.data

import org.techascent.aifinder.domain.model.*

/** Replace this implementation with persistence/network sources; the screens need no changes. */
interface InventoryRepository {
    fun spaces(): List<StorageLocation>
    fun items(): List<LocatedItem>
    fun search(query: String): List<LocatedItem>
    fun detections(): List<ItemDetection>
    /** [location] comes from [org.techascent.aifinder.domain.location.LocationRepository]. */
    fun saveDetections(location: StorageLocation, path: String, detections: List<ItemDetection>)
}

// Home spaces and items are still sample data; locations for saving come from Realtime Database.
class MockInventoryRepository : InventoryRepository {
    private val locations = mutableListOf(
        StorageLocation("home", "Home", LocationType.HOME),
        StorageLocation("living", "Living room", LocationType.ROOM, "home"),
        StorageLocation("bed", "Bedroom", LocationType.ROOM, "home"),
        StorageLocation("storage", "Storage room", LocationType.ROOM, "home"),
        StorageLocation("cabinet", "TV cabinet", LocationType.CABINET, "living"),
        StorageLocation("drawer", "Drawer 2", LocationType.DRAWER, "cabinet")
    )
    private val inventory = mutableListOf(
        LocatedItem(InventoryItem("hdmi", "Spare HDMI cable", "Electronics", listOf("cable", "video", "adapter")), locations.last(), "Living room › TV cabinet › Drawer 2"),
        LocatedItem(InventoryItem("usb", "USB-C adapter", "Electronics"), locations.last(), "Living room › TV cabinet › Drawer 2"),
        LocatedItem(InventoryItem("remote", "TV remote", "Electronics"), locations.last(), "Living room › TV cabinet › Drawer 2"),
        LocatedItem(InventoryItem("charge", "Charging cable", "Electronics"), locations.last(), "Living room › TV cabinet › Drawer 2"),
        LocatedItem(InventoryItem("power", "Power adapter", "Electronics"), locations.last(), "Living room › TV cabinet › Drawer 2")
    )
    override fun spaces() = locations.filter { it.parentId == "home" || (it.parentId == null && it.type != LocationType.HOME) }
        .map { location ->
            val descendants = locations.filter { candidate ->
                var parent = candidate.parentId
                while (parent != null && parent != location.id) {
                    parent = locations.firstOrNull { it.id == parent }?.parentId
                }
                parent == location.id
            }
            val ids = descendants.map { it.id }.toSet() + location.id
            location.copy(spaceCount = descendants.size, itemCount = inventory.count { it.location.id in ids })
        }
    override fun items() = inventory
    override fun search(query: String): List<LocatedItem> = if (query.isBlank()) inventory else inventory.filter { entry ->
        val terms = query.lowercase().split(" ").filter { it.length > 2 && it !in listOf("where", "what", "did", "put", "my", "the", "is", "find") }
        terms.any { entry.item.name.lowercase().contains(it) || entry.item.tags.any { tag -> tag.contains(it) } }
    }
    override fun detections() = inventory.mapIndexed { index, record -> ItemDetection("mock-photo", record.item, (0.94f - index * 0.03f)) }

    override fun saveDetections(location: StorageLocation, path: String, detections: List<ItemDetection>) {
        require(detections.isNotEmpty()) { "There are no items to save." }
        detections.forEach { detection ->
            inventory.removeAll { it.item.id == detection.item.id }
            inventory.add(LocatedItem(detection.item, location, path))
        }
    }
}
