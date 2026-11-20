package org.techascent.aifinder.data.cache

import androidx.room.Entity
import androidx.room.Index
import androidx.room.ColumnInfo

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
    @ColumnInfo(defaultValue = "''") val description: String = "",
    @ColumnInfo(defaultValue = "0") val confirmedAt: Long = 0,
    val bounds: String? = null,
)
