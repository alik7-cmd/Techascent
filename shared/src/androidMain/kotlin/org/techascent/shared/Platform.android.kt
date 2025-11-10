package org.techascent.shared

import android.os.Build
import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.techascent.shared.data.room.AppDatabase
import kotlin.getValue

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

object DatabaseProvider : KoinComponent {
    private val context: Context by inject()

    fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "prayer_app.db"
        )
    }
}

actual fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    return DatabaseProvider.getDatabaseBuilder()
}