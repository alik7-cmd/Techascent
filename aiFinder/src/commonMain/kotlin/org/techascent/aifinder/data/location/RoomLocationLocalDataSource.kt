package org.techascent.aifinder.data.location

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.techascent.aifinder.data.cache.CachedLocation
import org.techascent.aifinder.data.cache.LocationDao

class RoomLocationLocalDataSource(
    private val dao: LocationDao,
) : LocationLocalDataSource {
    override fun observe(userId: String): Flow<List<Pair<String, LocationDto>>> =
        dao.observe(userId).map { rows -> rows.map { it.toNode() } }

    override suspend fun isEmpty(userId: String): Boolean = dao.count(userId) == 0L

    override suspend fun replaceAll(userId: String, nodes: List<Pair<String, LocationDto>>) {
        dao.replaceAll(userId, nodes.map { (id, dto) -> dto.toEntity(userId, id) })
    }

    override suspend fun upsert(userId: String, id: String, location: LocationDto) {
        dao.upsert(location.toEntity(userId, id))
    }

    private fun LocationDto.toEntity(userId: String, id: String) =
        CachedLocation(userId, id, name, type, parentId, createdAt)

    private fun CachedLocation.toNode() = id to LocationDto(name, type, parentId, createdAt)
}
