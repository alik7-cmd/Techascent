package org.techascent.shared
import androidx.room.RoomDatabase
import org.techascent.shared.data.room.AppDatabase

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
expect fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase>


