package org.techascent.aifinder.data

import org.techascent.aifinder.domain.model.*

/** Replace this implementation with persistence/network sources; the screens need no changes. */
interface InventoryRepository {
    fun spaces(): List<StorageLocation>
    fun items(): List<LocatedItem>
    fun search(query: String): List<LocatedItem>
    fun detections(): List<ItemDetection>
}

class MockInventoryRepository : InventoryRepository {
    private val locations = listOf(
        StorageLocation("living", "Living room", LocationType.ROOM, spaceCount = 4, itemCount = 23),
        StorageLocation("bed", "Bedroom", LocationType.ROOM, spaceCount = 3, itemCount = 15),
        StorageLocation("storage", "Storage room", LocationType.ROOM, itemCount = 48, boxCount = 6),
        StorageLocation("drawer", "Drawer 2", LocationType.DRAWER, "living")
    )
    private val inventory = listOf(
        LocatedItem(InventoryItem("hdmi", "Spare HDMI cable", "Electronics", listOf("cable", "video", "adapter")), locations.last(), "Living room › TV cabinet › Drawer 2"),
        LocatedItem(InventoryItem("usb", "USB-C adapter", "Electronics"), locations.last(), "Living room › TV cabinet › Drawer 2"),
        LocatedItem(InventoryItem("remote", "TV remote", "Electronics"), locations.last(), "Living room › TV cabinet › Drawer 2"),
        LocatedItem(InventoryItem("charge", "Charging cable", "Electronics"), locations.last(), "Living room › TV cabinet › Drawer 2"),
        LocatedItem(InventoryItem("power", "Power adapter", "Electronics"), locations.last(), "Living room › TV cabinet › Drawer 2")
    )
    override fun spaces() = locations.take(3)
    override fun items() = inventory
    override fun search(query: String): List<LocatedItem> = if (query.isBlank()) inventory else inventory.filter { entry ->
        val terms = query.lowercase().split(" ").filter { it.length > 2 && it !in listOf("where", "what", "did", "put", "my", "the", "is", "find") }
        terms.any { entry.item.name.lowercase().contains(it) || entry.item.tags.any { tag -> tag.contains(it) } }
    }
    override fun detections() = inventory.mapIndexed { index, record -> ItemDetection("mock-photo", record.item, (0.94f - index * 0.03f)) }
}
