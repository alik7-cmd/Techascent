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

@Database(entities = [CachedLocation::class, WelcomePreference::class, CachedItem::class, CachedScanWallet::class], version = 6)
@ConstructedBy(AiFinderDatabaseConstructor::class)
abstract class AiFinderDatabase : RoomDatabase() {
    abstract fun locationDao(): LocationDao
    abstract fun welcomePreferenceDao(): WelcomePreferenceDao
    abstract fun itemDao(): ItemDao
    abstract fun scanWalletDao(): ScanWalletDao
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

private val migration3To4 = object : Migration(3, 4) {
    override fun migrate(connection: SQLiteConnection) {
        listOf(
            "CREATE TABLE IF NOT EXISTS `CachedItem` (`userId` TEXT NOT NULL, `id` TEXT NOT NULL, " +
                "`name` TEXT NOT NULL, `category` TEXT NOT NULL, `tags` TEXT NOT NULL, `locationId` TEXT NOT NULL, " +
                "`photoId` TEXT, `confidence` REAL, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`userId`, `id`))",
            "CREATE INDEX IF NOT EXISTS `CachedItem_user_created` ON `CachedItem` (`userId`, `createdAt`)",
        ).forEach { sql -> connection.prepare(sql).use { it.step() } }
    }
}

private val migration4To5 = object : Migration(4, 5) {
    override fun migrate(connection: SQLiteConnection) {
        connection.prepare(
            "CREATE TABLE IF NOT EXISTS `CachedScanWallet` (`userId` TEXT NOT NULL, " +
                "`remainingScans` INTEGER NOT NULL, PRIMARY KEY(`userId`))"
        ).use { it.step() }
    }
}

private val migration5To6 = object : Migration(5, 6) {
    override fun migrate(connection: SQLiteConnection) {
        listOf(
            "ALTER TABLE CachedItem ADD COLUMN description TEXT NOT NULL DEFAULT ''",
            "ALTER TABLE CachedItem ADD COLUMN confirmedAt INTEGER NOT NULL DEFAULT 0",
            "ALTER TABLE CachedItem ADD COLUMN bounds TEXT",
            "UPDATE CachedItem SET confirmedAt = createdAt",
        ).forEach { sql -> connection.prepare(sql).use { it.step() } }
    }
}

fun RoomDatabase.Builder<AiFinderDatabase>.buildAiFinderDatabase(
    driver: SQLiteDriver = BundledSQLiteDriver(),
): AiFinderDatabase =
    setDriver(driver)
        .setQueryCoroutineContext(Dispatchers.IO)
        .addMigrations(migration1To2, migration2To3, migration3To4, migration4To5, migration5To6)
        .build()
