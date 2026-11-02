package org.techascent.aifinder.data.cache

import androidx.room.Entity
import androidx.room.Index

@Entity(
    primaryKeys = ["userId", "id"],
    indices = [Index(value = ["userId", "createdAt"], name = "CachedItem_user_created")],
)
data class CachedItem(
    val userId: String,
    val id: String,
    val name: String,
    val category: String,
    val tags: String,
    val locationId: String,
    val photoId: String?,
    val confidence: Float?,
    val createdAt: Long,
)
