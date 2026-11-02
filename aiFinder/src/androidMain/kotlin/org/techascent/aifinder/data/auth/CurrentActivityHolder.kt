package org.techascent.aifinder.data.auth

import android.app.Activity
import java.lang.ref.WeakReference

/** Credential Manager needs an Activity; held weakly so rotation never leaks the old one. */
object CurrentActivityHolder {
    private var ref: WeakReference<Activity>? = null

    val activity: Activity? get() = ref?.get()

    fun attach(activity: Activity) {
        ref = WeakReference(activity)
    }

    fun detach(activity: Activity) {
        if (ref?.get() === activity) ref = null
    }
}
