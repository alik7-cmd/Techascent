package org.techascent.aifinder.util

import platform.Foundation.NSLog

actual fun logDebug(tag: String, message: String) {
    NSLog("[%@] %@", tag, message)
}
