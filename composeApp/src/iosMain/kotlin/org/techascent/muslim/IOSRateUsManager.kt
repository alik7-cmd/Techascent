package org.techascent.muslim

import org.techascent.muslim.common.AppRatingManager
import platform.Foundation.*
import platform.UIKit.UIApplication

class IOSRateUsManager : AppRatingManager{
    override fun rateApp() {
        val appStoreUrl = "itms-apps://itunes.apple.com/app/idYOUR_APP_ID"
        val nsUrl = NSURL.URLWithString(appStoreUrl)
        nsUrl?.let {
            val app = UIApplication.sharedApplication
            if (app.canOpenURL(it)) {
                app.openURL(it)
            }
        }
    }
}