package org.techascent.aifinder.data.cache

import android.annotation.SuppressLint
import android.content.Context

/** Application context for platform singletons; set before the Koin graph is first used. */
@SuppressLint("StaticFieldLeak")
object AppContextHolder {
    private var appContext: Context? = null

    val context: Context get() = checkNotNull(appContext) { "AppContextHolder.init was not called." }

    fun init(context: Context) {
        appContext = context.applicationContext
    }
}
