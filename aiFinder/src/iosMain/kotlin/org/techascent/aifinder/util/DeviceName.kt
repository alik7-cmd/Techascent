package org.techascent.aifinder.util

import platform.UIKit.UIDevice

actual fun getDeviceName(): String = UIDevice.currentDevice.name
