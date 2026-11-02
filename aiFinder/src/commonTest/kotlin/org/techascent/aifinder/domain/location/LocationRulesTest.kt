package org.techascent.aifinder.domain.location

import org.techascent.aifinder.data.location.LocationDto
import org.techascent.aifinder.domain.model.CreateLocationRequest
import org.techascent.aifinder.domain.model.LocationType
import org.techascent.aifinder.domain.model.StorageLocation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class LocationRulesTest {
    private val existing = listOf(
        StorageLocation("garage", "Garage", LocationType.AREA),
        StorageLocation("shelf", "Shelf", LocationType.CONTAINER, "garage"),
    )

    @Test
    fun trimsValidRequest() {
        val valid = CreateLocationRequest("  Toolbox ", LocationType.CONTAINER, "shelf").validated(existing)
        assertEquals("Toolbox", valid.name)
    }

    @Test
    fun rejectsInvalidRequests() {
        assertFailsWith<LocationError.BlankName> { CreateLocationRequest(" ", LocationType.ROOM).validated(existing) }
        assertFailsWith<LocationError.InvalidType> { CreateLocationRequest("A", LocationType.DRAWER).validated(existing) }
        assertFailsWith<LocationError.ParentNotFound> {
            CreateLocationRequest("A", LocationType.ROOM, "missing").validated(existing)
        }
        assertFailsWith<LocationError.Duplicate> {
            CreateLocationRequest("SHELF", LocationType.CONTAINER, "garage").validated(existing)
        }
        // Same name is fine under a different parent.
        CreateLocationRequest("Shelf", LocationType.CONTAINER, null).validated(existing)
    }

    @Test
    fun buildsPathAndSurvivesCycles() {
        assertEquals("Garage › Shelf", existing.pathOf("shelf"))
        val cyclic = listOf(
            StorageLocation("a", "A", LocationType.ROOM, "b"),
            StorageLocation("b", "B", LocationType.ROOM, "a"),
        )
        assertEquals("B › A", cyclic.pathOf("a"))
        assertEquals("", existing.pathOf("missing"))
    }

    @Test
    fun mapsDtoRoundTripAndSkipsMalformedNodes() {
        val dto = LocationDto("Garage", "AREA", null, 42L)
        assertEquals(dto, LocationDto.fromMap(dto.toMap()))
        assertEquals(LocationType.AREA, dto.toDomain("id").type)
        assertEquals(LocationType.CONTAINER, LocationDto("X", "UNKNOWN", null, 0).toDomain("id").type)
        assertNull(LocationDto.fromMap(mapOf("type" to "ROOM")))
    }
}
