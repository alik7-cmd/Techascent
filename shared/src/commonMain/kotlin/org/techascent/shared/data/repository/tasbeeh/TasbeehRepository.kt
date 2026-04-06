package org.techascent.shared.data.repository.tasbeeh

import kotlinx.coroutines.flow.Flow

data class TasbeehData(
    val count: Int = 0,
    val sets: Int = 0,
    val haptic: Boolean = true,
)

interface TasbeehRepository {
    /** Observe counter, sets, and haptic preference as a combined flow. */
    fun observeTasbeehData(): Flow<TasbeehData>

    /** Persist the current counter value. */
    suspend fun saveCounter(count: Int)

    /** Persist the current set value. */
    suspend fun saveSet(count: Int)

    /** Persist counter and set at once. */
    suspend fun saveTasbeehData(count: Int, sets: Int)

    /** Reset counter and sets to zero. */
    suspend fun resetAll()
}
