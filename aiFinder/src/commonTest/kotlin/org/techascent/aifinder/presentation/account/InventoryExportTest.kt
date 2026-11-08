package org.techascent.aifinder.presentation.account

import org.techascent.aifinder.domain.inventory.*
import org.techascent.aifinder.domain.model.*
import org.techascent.aifinder.presentation.location.placeIdFromQr
import org.techascent.aifinder.presentation.location.placeQrValue
import kotlin.test.*

class InventoryExportTest {
    @Test
    fun csvQuotesCommasLineBreaksAndQuotesAndNeutralizesSpreadsheetFormulas() {
        assertEquals("\"'=SUM(A1:A2)\"", csvCell("=SUM(A1:A2)"))
        assertEquals("\"'  +cmd\"", csvCell("  +cmd"))
        assertEquals("\"'\tformula\"", csvCell("\tformula"))
        assertEquals("\"Cable, \"\"blue\"\"\nsecond line\"", csvCell("Cable, \"blue\"\nsecond line"))
        val snapshot = buildSnapshot(listOf(StorageLocation("p", "Office", LocationType.ROOM)), listOf(
            StoredItem(InventoryItem("i", "USB cable", "Electronics", listOf("cord"), "Blue"), "p", 10, 20),
        ))
        val csv = snapshot.exportCsv()
        assertTrue(csv.contains("\"USB cable\",\"Electronics\",\"Office\",\"Blue\",\"cord\",\"10\",\"20\"\r\n"))
    }

    @Test
    fun qrPayloadOnlyParsesTheExactLocalPlaceSchemeAndSafeIds() {
        assertEquals("-abc_123", placeIdFromQr(placeQrValue("-abc_123")))
        listOf("", "https://example.com/place/id", "aifinder://place/", "aifinder://place/../id",
            "aifinder://place/id?user=someone", "aifinder://place/%2F", "aifinder://place/a/b")
            .forEach { assertNull(placeIdFromQr(it), it) }
    }
}
