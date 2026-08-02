package org.techascent.muslim

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import org.techascent.muslim.servive.DailyPrayerScheduler
import org.techascent.muslim.widget.WidgetUpdater

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        initHaptics(this)
        super.onCreate(savedInstanceState)

        // Ensure daily prayer notification rescheduler is running
        DailyPrayerScheduler.scheduleDailyWorker(this)

        // Initialize widget updater so widgets refresh when data loads
        WidgetUpdater.init(this)

        setContent {
            App()
        }
    }
}