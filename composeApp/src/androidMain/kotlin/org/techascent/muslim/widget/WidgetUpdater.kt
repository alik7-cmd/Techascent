package org.techascent.muslim.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Helper object for triggering widget updates from anywhere in the Android app.
 * Call [refreshWidgets] after prayer data is successfully loaded / cached.
 */
object WidgetUpdater {

    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    /**
     * Writes a fresh widget snapshot to SharedPreferences, then triggers
     * all PrayerTimeWidget instances to re-render.
     * Safe to call from any coroutine context.
     */
    suspend fun refreshWidgets() {
        val ctx = appContext ?: return
        try {
            // Write latest prayer data into widget-specific SharedPreferences
            writeWidgetSnapshot(ctx)
        } catch (_: Exception) {
            // Non-critical: widget will show stale or empty data
        }
        withContext(Dispatchers.Main) {
            try {
                PrayerTimeWidget().updateAll(ctx)
            } catch (_: Exception) {
                // Widget might not be placed — ignore
            }
        }
    }
}

