package org.techascent.shared.data.repository.settings

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    /** Observe the school preference (e.g. Hanafi / Shafi code). */
    fun observeSchoolPreference(): Flow<Int>

    /** Observe the haptic-feedback toggle. */
    fun observeHapticPreference(): Flow<Boolean>

    /** Observe the adhan-notification toggle. */
    fun observeAdhanPreference(): Flow<Boolean>

    /** Update the school preference. */
    suspend fun updateSchoolPreference(code: Int)

    /** Update the haptic-feedback toggle. */
    suspend fun updateHapticPreference(enabled: Boolean)

    /** Update the adhan-notification toggle. */
    suspend fun updateAdhanPreference(enabled: Boolean)
}

