package org.techascent.aifinder.config

enum class HomeLayout { POLISHED, DASHBOARD }

object AppConfig {
    /** Development only: false restores Vertex AI. Rebuild and restart after changing. */
    const val USE_FAKE_ANALYSIS = true
    val HOME_LAYOUT = HomeLayout.POLISHED
}
