package org.techascent.aifinder.util

import android.os.Build

actual fun getDeviceName(): String =
    if (Build.MODEL.startsWith(Build.MANUFACTURER, ignoreCase = true)) Build.MODEL
    else "${Build.MANUFACTURER} ${Build.MODEL}"
