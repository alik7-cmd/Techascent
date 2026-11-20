package org.techascent.aifinder.data.cache

import androidx.room.Entity
import androidx.room.Index

@Entity(
    primaryKeys = ["userId", "id"],
    indices = [Index(value = ["userId", "createdAt"], name = "CachedLocation_user_created")],
)
data class CachedLocation(
    val userId: String,
    val id: String,
    val name: String,
    val type: String,
    val parentId: String?,
    val createdAt: Long,
)
