package org.techascent.aifinder.data.firebase

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import org.techascent.aifinder.data.inventory.*
import org.techascent.aifinder.data.location.*
import org.techascent.aifinder.data.photo.PhotoRemoteDataSource
import org.techascent.aifinder.data.photo.StoragePaths
import org.techascent.aifinder.data.subscription.ScanAccessRemoteDataSource
import org.techascent.aifinder.domain.subscription.*
import kotlin.io.encoding.Base64
import kotlin.uuid.Uuid

class IosLocationDataSource : LocationRemoteDataSource {
    override suspend fun fetchAll(userId: String) = readNodes(RealtimeDatabasePaths.locations(userId)).map { (id, row) ->
        id to checkNotNull(LocationDto.fromMap(row)) { "Invalid place record." }
    }
    @OptIn(kotlin.uuid.ExperimentalUuidApi::class)
    override suspend fun create(userId: String, location: LocationDto): String {
        val id = Uuid.random().toString()
        firebaseWrite("${RealtimeDatabasePaths.locations(userId)}/$id", location.toMap())
        return id
    }
}

class IosItemDataSource : ItemRemoteDataSource {
    override suspend fun clearPhotos(userId: String, itemIds: List<String>) {
        firebaseWrite(RealtimeDatabasePaths.items(userId),
            itemIds.flatMap { listOf("$it/photoId" to null, "$it/bounds" to null) }.toMap(), update = true)
    }
    override suspend fun writeAll(userId: String, items: List<Pair<String, ItemDto>>) {
        firebaseWrite(RealtimeDatabasePaths.items(userId), items.associate { it.first to it.second.toMap() }, update = true)
    }
    override suspend fun fetchAll(userId: String) = readNodes(RealtimeDatabasePaths.items(userId)).map { (id, row) ->
        id to checkNotNull(ItemDto.fromMap(row)) { "Invalid item record." }
    }
    @OptIn(kotlin.uuid.ExperimentalUuidApi::class)
    override suspend fun createAll(userId: String, items: List<ItemDto>): List<Pair<String, ItemDto>> {
        val created = items.map { Uuid.random().toString() to it }
        firebaseWrite(RealtimeDatabasePaths.items(userId), created.associate { it.first to it.second.toMap() }, update = true)
        return created
    }
    override suspend fun update(userId: String, itemId: String, item: ItemDto) {
        firebaseWrite("${RealtimeDatabasePaths.items(userId)}/$itemId", item.toMap())
    }
    override suspend fun delete(userId: String, itemId: String) {
        firebaseWrite(RealtimeDatabasePaths.items(userId), mapOf(itemId to null), update = true)
    }
}

class IosPhotoDataSource : PhotoRemoteDataSource {
    override suspend fun delete(userId: String, photoId: String) {
        firebaseCall<Unit> { bridge, done -> bridge.deletePhoto(path(userId, photoId)) { done(Unit, it) } }
    }
    override suspend fun upload(userId: String, photoId: String, jpegBytes: ByteArray) {
        firebaseCall<Unit> { bridge, done ->
            bridge.upload(path(userId, photoId), Base64.encode(jpegBytes)) { done(Unit, it) }
        }
    }
    override suspend fun download(userId: String, photoId: String): ByteArray? {
        val data = firebaseCall<String> { bridge, done -> bridge.download(path(userId, photoId), done) }
        return data.takeIf(String::isNotEmpty)?.let(Base64::decode)
    }
    private fun path(userId: String, photoId: String) = StoragePaths.photo(userId, photoId)
}

class IosScanAccessDataSource : ScanAccessRemoteDataSource {
    override suspend fun load(userId: String) = wallet(userId, "load")
    override suspend fun consume(userId: String, scanId: String) = wallet(userId, "consume", mapOf("scanId" to scanId))
    override suspend fun recordPurchase(userId: String, purchase: ScanPurchase) = wallet(userId, "purchase", mapOf(
        "purchaseId" to purchase.purchaseId, "productId" to purchase.productId,
        "scansGranted" to purchase.scansGranted, "purchasedAt" to purchase.purchasedAt,
        "store" to purchase.store,
    ))
    private suspend fun wallet(userId: String, operation: String, data: Map<String, Any?> = emptyMap()): ScanAccess {
        val text = firebaseCall<String> { bridge, done ->
            bridge.wallet(userId, operation, data.toJson().toString(), done)
        }
        val count = Json.parseToJsonElement(text).jsonObject.getValue("remainingScans").jsonPrimitive.long
        check(count >= 0) { "Invalid scan balance." }
        return ScanAccess(userId, count)
    }
}
