package org.techascent.shared

import android.os.Build

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

/*
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
}*/
