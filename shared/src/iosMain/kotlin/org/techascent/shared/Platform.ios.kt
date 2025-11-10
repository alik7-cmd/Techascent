package org.techascent.shared

import androidx.room.Room
import androidx.room.RoomDatabase
import org.techascent.shared.data.room.AppDatabase
import platform.Foundation.NSHomeDirectory
import platform.UIKit.UIDevice


class IOSPlatform : Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

actual fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val dbPath = NSHomeDirectory() + "/Documents/prayer_app.db"
    return Room.databaseBuilder<AppDatabase>(
        name = dbPath
    )
}