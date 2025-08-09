package org.techascent.muslim

import android.content.Context
import android.content.Intent
import org.techascent.muslim.common.AppRatingManager
import androidx.core.net.toUri

class AndroidRateUsManager (private val context: Context) : AppRatingManager {
    override fun rateApp() {
        val packageName = context.packageName
        val uri = "market://details?id=$packageName".toUri()
        val goToMarket = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        }

        try {
            context.startActivity(goToMarket)
        } catch (e: Exception) {
            // Fallback to web if Play Store is unavailable
            val webUri = "https://play.google.com/store/apps/details?id=$packageName".toUri()
            context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
        }
    }
}