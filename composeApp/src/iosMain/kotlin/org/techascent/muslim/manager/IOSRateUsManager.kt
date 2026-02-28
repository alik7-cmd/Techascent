package org.techascent.muslim.manager

import org.techascent.muslim.common.AppRatingManager
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

class IOSRateUsManager : AppRatingManager {
    override fun rateApp() {
        val appStoreUrl = "itms-apps://itunes.apple.com/app/idYOUR_APP_ID"
        val nsUrl = NSURL.Companion.URLWithString(appStoreUrl)
        nsUrl?.let {
            val app = UIApplication.Companion.sharedApplication
            if (app.canOpenURL(it)) {
                app.openURL(it)
            }
        }
    }
}