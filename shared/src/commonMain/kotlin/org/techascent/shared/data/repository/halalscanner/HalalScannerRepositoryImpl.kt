package org.techascent.shared.data.repository.halalscanner

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import org.techascent.shared.data.Product
import org.techascent.shared.data.common.DataStoreKey
import org.techascent.shared.data.datasource.halalscanner.HalalScannerDataSource
import org.techascent.shared.data.dto.ProductDto
import org.techascent.shared.data.mapper.HalalChecker
import org.techascent.shared.data.mapper.toDto
import org.techascent.shared.data.model.ScanHistoryItem
import org.techascent.shared.network.ResultState

class HalalScannerRepositoryImpl(
    private val dataSource: HalalScannerDataSource,
    private val dataStore: DataStore<Preferences>,
) : HalalScannerRepository {

    private val historyKey = stringPreferencesKey(DataStoreKey.SCAN_HISTORY)

    override fun fetchProductByBarcode(barcode: String): Flow<ResultState<ProductDto>> {
        return dataSource.fetchProductByBarcode(
            barcode = barcode,
            onMapData = { response ->
                response.toDto()
            }
        )
    }

    override fun checkIngredients(ingredientsText: String): ProductDto {
        val product = Product(
            productName = null,
            brands = null,
            labels = null,
            labelsTags = null,
            ingredients_text = ingredientsText,
            image_url = null,
            certificationTag = null,
        )
        val halalResult = HalalChecker.assessHalalStatus(product)
        return ProductDto(
            brands = null,
            labels = null,
            labelsTags = null,
            ingredientsText = ingredientsText,
            imageUrl = null,
            halalResult = halalResult,
        )
    }

    override suspend fun getHistory(): List<ScanHistoryItem> {
        return try {
            val prefs = dataStore.data.first()
            val json = prefs[historyKey] ?: return emptyList()
            Json.decodeFromString<List<ScanHistoryItem>>(json)
                .sortedByDescending { it.timestamp }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun saveToHistory(item: ScanHistoryItem): List<ScanHistoryItem> {
        return try {
            val currentList = getHistory().toMutableList()
            currentList.add(0, item)
            // Keep max 100 items
            val trimmedList = currentList.take(100)
            dataStore.edit { prefs ->
                prefs[historyKey] = Json.encodeToString(trimmedList)
            }
            trimmedList
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun clearHistory() {
        dataStore.edit { prefs ->
            prefs.remove(historyKey)
        }
    }
}