package org.techascent.aifinder.data.location

import androidx.room.Room
import androidx.sqlite.driver.AndroidSQLiteDriver
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.techascent.aifinder.data.cache.AiFinderDatabase
import org.techascent.aifinder.data.cache.buildAiFinderDatabase
import org.techascent.aifinder.data.auth.RoomWelcomeRepository
import org.techascent.aifinder.data.inventory.ItemDto
import org.techascent.aifinder.data.inventory.RoomItemLocalDataSource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class RoomLocationLocalDataSourceTest {
    private lateinit var database: AiFinderDatabase
    private lateinit var cache: RoomLocationLocalDataSource

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder<AiFinderDatabase>(RuntimeEnvironment.getApplication())
            .buildAiFinderDatabase(AndroidSQLiteDriver())
        cache = RoomLocationLocalDataSource(database.locationDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun storesPerUserOrderedByCreation() = runBlocking<Unit> {
        assertTrue(cache.isEmpty("a"))
        cache.replaceAll("a", listOf(
            "2" to LocationDto("Shelf", "CONTAINER", "1", 20),
            "1" to LocationDto("Garage", "AREA", null, 10),
        ))
        cache.upsert("b", "x", LocationDto("Other user", "ROOM", null, 5))

        assertFalse(cache.isEmpty("a"))
        assertEquals(listOf("1", "2"), cache.observe("a").first().map { it.first })
        assertEquals(LocationDto("Shelf", "CONTAINER", "1", 20), cache.observe("a").first().last().second)
        assertEquals(listOf("x"), cache.observe("b").first().map { it.first })
    }

    @Test
    fun upsertReplacesAndReplaceAllClearsOnlyThatUser() = runBlocking<Unit> {
        cache.upsert("a", "1", LocationDto("Garage", "AREA", null, 10))
        cache.upsert("a", "1", LocationDto("Garage 2", "AREA", null, 10))
        cache.upsert("b", "9", LocationDto("Kept", "ROOM", null, 1))
        assertEquals(listOf("Garage 2"), cache.observe("a").first().map { it.second.name })

        cache.replaceAll("a", emptyList())
        assertTrue(cache.isEmpty("a"))
        assertFalse(cache.isEmpty("b"))
    }

    @Test
    fun welcomeFlagDefaultsToFalseAndCanBePersisted() = runBlocking<Unit> {
        val welcome = RoomWelcomeRepository(database.welcomePreferenceDao())
        assertFalse(welcome.hasSeenWelcome())
        welcome.markWelcomeSeen()
        assertTrue(RoomWelcomeRepository(database.welcomePreferenceDao()).hasSeenWelcome())
    }

    @Test
    fun emitsChangesFromRoom() = runBlocking<Unit> {
        val emissions = Channel<List<Pair<String, LocationDto>>>(Channel.UNLIMITED)
        val observation = launch { cache.observe("a").collect { emissions.send(it) } }
        try {
            withTimeout(10_000) {
                assertTrue(emissions.receive().isEmpty())
                cache.upsert("a", "1", LocationDto("Garage", "AREA", null, 10))
                assertEquals("Garage", emissions.receive().single().second.name)
                cache.upsert("a", "1", LocationDto("Updated garage", "AREA", null, 10))
                assertEquals("Updated garage", emissions.receive().single().second.name)
            }
        } finally {
            observation.cancel()
            emissions.close()
        }
    }

    @Test
    fun adoptsExistingCacheAndPreservesItAcrossReopens() = runBlocking<Unit> {
        val context = RuntimeEnvironment.getApplication()
        val name = "legacy-cache-test.db"
        context.deleteDatabase(name)
        val file = context.getDatabasePath(name)
        check(file.parentFile!!.mkdirs() || file.parentFile!!.isDirectory)
        val connection = AndroidSQLiteDriver().open(file.absolutePath)
        try {
            listOf(
                """CREATE TABLE CachedLocation (
                    userId TEXT NOT NULL, id TEXT NOT NULL, name TEXT NOT NULL,
                    type TEXT NOT NULL, parentId TEXT, createdAt INTEGER NOT NULL,
                    PRIMARY KEY (userId, id)
                )""",
                "CREATE INDEX CachedLocation_user_created ON CachedLocation(userId, createdAt)",
                "INSERT INTO CachedLocation VALUES ('a', '1', 'Garage', 'AREA', NULL, 10)",
                "PRAGMA user_version = 1",
            ).forEach { sql -> connection.prepare(sql).use { it.step() } }
        } finally {
            connection.close()
        }

        fun openDatabase() = Room.databaseBuilder<AiFinderDatabase>(context, file.absolutePath)
            .buildAiFinderDatabase(AndroidSQLiteDriver())

        try {
            val migrated = openDatabase()
            try {
                val local = RoomLocationLocalDataSource(migrated.locationDao())
                assertEquals("Garage", local.observe("a").first().single().second.name)
                local.upsert("a", "2", LocationDto("Shelf", "CONTAINER", "1", 20))
                val welcome = RoomWelcomeRepository(migrated.welcomePreferenceDao())
                assertFalse(welcome.hasSeenWelcome())
                welcome.markWelcomeSeen()
            } finally {
                migrated.close()
            }
            val reopened = openDatabase()
            try {
                assertTrue(RoomWelcomeRepository(reopened.welcomePreferenceDao()).hasSeenWelcome())
                assertTrue(RoomItemLocalDataSource(reopened.itemDao()).isEmpty("a"))
                assertEquals(
                    listOf("Garage", "Shelf"),
                    RoomLocationLocalDataSource(reopened.locationDao()).observe("a").first().map { it.second.name },
                )
            } finally {
                reopened.close()
            }
        } finally {
            context.deleteDatabase(name)
        }
    }

    @Test
    fun itemsArePerUserNewestFirstAndKeepTags() = runBlocking<Unit> {
        val items = RoomItemLocalDataSource(database.itemDao())
        assertTrue(items.isEmpty("a"))
        val old = ItemDto("Hammer", "Tools", listOf("hammer", "steel, heavy"), "loc", "p1", 0.8f, 10)
        val new = ItemDto("Cable", "Electronics", emptyList(), "loc", null, null, 20)
        items.replaceAll("a", listOf("1" to old))
        items.upsertAll("a", listOf("2" to new))
        items.upsertAll("b", listOf("x" to new))

        assertEquals(listOf("2" to new, "1" to old), items.observe("a").first())
        items.replaceAll("a", emptyList())
        assertTrue(items.isEmpty("a"))
        assertFalse(items.isEmpty("b"))
    }
}
