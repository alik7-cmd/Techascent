package org.techascent.aifinder.data.cache

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.SQLiteDriver
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

internal const val DATABASE_NAME = "aifinder.db"

@Database(entities = [CachedLocation::class, WelcomePreference::class], version = 3)
@ConstructedBy(AiFinderDatabaseConstructor::class)
abstract class AiFinderDatabase : RoomDatabase() {
    abstract fun locationDao(): LocationDao
    abstract fun welcomePreferenceDao(): WelcomePreferenceDao
}

@Suppress("KotlinNoActualForExpect")
expect object AiFinderDatabaseConstructor : RoomDatabaseConstructor<AiFinderDatabase> {
    override fun initialize(): AiFinderDatabase
}

private val migration1To2 = object : Migration(1, 2) {
    override fun migrate(connection: SQLiteConnection) {
        // The previous SQLite cache has the same table and index; Room validates and adopts it.
    }
}

private val migration2To3 = object : Migration(2, 3) {
    override fun migrate(connection: SQLiteConnection) {
        connection.prepare(
            "CREATE TABLE IF NOT EXISTS WelcomePreference (id INTEGER NOT NULL PRIMARY KEY, seen INTEGER NOT NULL)"
        ).use { it.step() }
    }
}

fun RoomDatabase.Builder<AiFinderDatabase>.buildAiFinderDatabase(
    driver: SQLiteDriver = BundledSQLiteDriver(),
): AiFinderDatabase =
    setDriver(driver)
        .setQueryCoroutineContext(Dispatchers.IO)
        .addMigrations(migration1To2, migration2To3)
        .build()
