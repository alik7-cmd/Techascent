package org.techascent.aifinder.data.cache

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

fun databaseBuilder(context: Context): RoomDatabase.Builder<AiFinderDatabase> {
    val appContext = context.applicationContext
    return Room.databaseBuilder<AiFinderDatabase>(
        context = appContext,
        name = appContext.getDatabasePath(DATABASE_NAME).absolutePath,
    )
}
