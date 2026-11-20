package org.techascent.aifinder.data.inventory

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.techascent.aifinder.data.cache.CachedItem
import org.techascent.aifinder.data.cache.ItemDao

class RoomItemLocalDataSource(
    private val dao: ItemDao,
) : ItemLocalDataSource {
    override fun observe(userId: String): Flow<List<Pair<String, ItemDto>>> =
        dao.observe(userId).map { rows -> rows.map { it.toNode() } }

    override suspend fun isEmpty(userId: String): Boolean = dao.count(userId) == 0L

    override suspend fun replaceAll(userId: String, items: List<Pair<String, ItemDto>>) {
        dao.replaceAll(userId, items.map { (id, dto) -> dto.toEntity(userId, id) })
    }

    override suspend fun upsertAll(userId: String, items: List<Pair<String, ItemDto>>) {
        dao.upsertAll(items.map { (id, dto) -> dto.toEntity(userId, id) })
    }

    override suspend fun delete(userId: String, itemId: String) = dao.delete(userId, itemId)

    private fun ItemDto.toEntity(userId: String, id: String) = CachedItem(
        userId = userId,
        id = id,
        name = name,
        category = category,
        tags = tags.joinToString(TAG_SEPARATOR),
        locationId = locationId,
        photoId = photoId,
        confidence = confidence,
        createdAt = createdAt,
        description = description,
        confirmedAt = confirmedAt,
        bounds = bounds?.joinToString(","),
    )

    private fun CachedItem.toNode() = id to ItemDto(
        name = name,
        category = category,
        tags = tags.split(TAG_SEPARATOR).filter { it.isNotEmpty() },
        locationId = locationId,
        photoId = photoId,
        confidence = confidence,
        createdAt = createdAt,
        description = description,
        confirmedAt = confirmedAt,
        bounds = bounds?.split(",")?.mapNotNull { it.toFloatOrNull() },
    )

    private companion object {
        // Unit separator: never typed by users, so tags can contain commas or spaces.
        const val TAG_SEPARATOR = "\u001F"
    }
}
