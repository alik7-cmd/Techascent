package org.techascent.aifinder.presentation.home

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.techascent.aifinder.domain.inventory.InventoryRepository
import org.techascent.aifinder.domain.inventory.StoredItem
import org.techascent.aifinder.domain.inventory.usecase.ObserveInventoryUseCase
import org.techascent.aifinder.domain.location.LocationRepository
import org.techascent.aifinder.domain.model.CreateLocationRequest
import org.techascent.aifinder.domain.model.ItemDetection
import org.techascent.aifinder.domain.model.LocationType
import org.techascent.aifinder.domain.model.StorageLocation
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    @BeforeTest
    fun setUp() = Dispatchers.setMain(StandardTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun nestedSpaceAppearsWithParentPathWhenLocationsEmitWithoutRestartingHome() = runTest {
        val garage = StorageLocation("garage", "Garage", LocationType.AREA)
        val toolbox = StorageLocation("box", "Toolbox", LocationType.CONTAINER, parentId = garage.id)
        val locations = MutableStateFlow(listOf(garage))
        val locationRepository = object : LocationRepository {
            override fun observeLocations() = locations
            override suspend fun createLocation(request: CreateLocationRequest): Result<StorageLocation> =
                error("Unused")
        }
        val items = object : InventoryRepository {
            override fun observeItems() = flowOf(emptyList<StoredItem>())
            override suspend fun saveDetections(
                locationId: String,
                detections: List<ItemDetection>,
            ): Result<List<StoredItem>> = error("Unused")
        }
        val home = HomeViewModel(ObserveInventoryUseCase(items, locationRepository))
        home.start()
        runCurrent()
        assertEquals(listOf("garage"), home.spaces().map { it.location.id })
        assertEquals(null, home.spaces().single().parentPath)

        locations.value = listOf(garage, toolbox)
        runCurrent()
        assertEquals(listOf("garage", "box"), home.spaces().map { it.location.id })
        assertEquals("Garage", home.spaces().last().parentPath)
        assertEquals(1, home.spaces().first().location.spaceCount)
        home.reset()
    }

    private fun HomeViewModel.spaces() = (uiState.value as HomeUiState.Success).data.spaces
}
