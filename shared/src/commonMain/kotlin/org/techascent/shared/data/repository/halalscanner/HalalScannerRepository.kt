package org.techascent.shared.data.repository.halalscanner

import kotlinx.coroutines.flow.Flow
import org.techascent.shared.data.dto.ProductDto
import org.techascent.shared.data.model.ScanHistoryItem
import org.techascent.shared.network.ResultState

interface HalalScannerRepository {
    fun fetchProductByBarcode(barcode: String): Flow<ResultState<ProductDto>>

    /**
     * Assess halal status of raw ingredients text.
     * Returns a ProductDto with the halal result.
     */
    fun checkIngredients(ingredientsText: String): ProductDto

    /** Load persisted scan history, sorted newest-first. */
    suspend fun getHistory(): List<ScanHistoryItem>

    /** Persist a new scan history item (keeps max 100). */
    suspend fun saveToHistory(item: ScanHistoryItem): List<ScanHistoryItem>

    /** Clear all scan history. */
    suspend fun clearHistory()
}