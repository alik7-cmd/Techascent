package org.techascent.aifinder

import androidx.compose.runtime.Composable
import org.koin.compose.KoinIsolatedContext
import org.koin.dsl.koinApplication
import org.techascent.aifinder.di.aiFinderModule
import org.techascent.aifinder.di.platformModule
import org.techascent.composa.theming.ComposaTheme
import org.techascent.aifinder.presentation.navigation.AiFinderNavigation

// Keep dependencies alive across activity recreation without sharing another app's Koin context.
private val aiFinderKoin by lazy {
    koinApplication { modules(aiFinderModule, platformModule) }
}

@Composable
fun AiFinderApp() {
    KoinIsolatedContext(context = aiFinderKoin) {
        ComposaTheme { AiFinderNavigation() }
    }
}
