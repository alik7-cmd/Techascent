package org.techascent.shared.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [PrayerTimesEntity::class], version = 1)
@TypeConverters(PrayerResponseConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun prayerTimesDao(): PrayerTimesDao
}
