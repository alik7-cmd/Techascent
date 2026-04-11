package org.techascent.muslim.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/**
 * Receiver that the system talks to. Delegates to [PrayerTimeWidget].
 * Also schedules the periodic 1-minute updates via [PrayerWidgetWorker].
 */
class PrayerWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = PrayerTimeWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        // Start periodic worker when first widget is placed
        PrayerWidgetWorker.enqueue(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        // Cancel periodic worker when last widget is removed
        PrayerWidgetWorker.cancel(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        // Reschedule worker on update in case it was killed
        if (intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            PrayerWidgetWorker.enqueue(context)
        }
    }
}

