package org.techascent.muslim.preference

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey


typealias PrayerPrefManager =  DataStore<Preferences>

object PrefKey {

    private const val COUNT_KEY = "COUNT_KEY"
    internal val countKey = intPreferencesKey(name = COUNT_KEY)
}