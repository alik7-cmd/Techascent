package org.techascent.aifinder

import androidx.compose.runtime.Composable
import org.koin.compose.KoinApplication
import org.techascent.aifinder.di.aiFinderModule
import org.techascent.composa.theming.ComposaTheme
import org.techascent.aifinder.presentation.navigation.AiFinderNavigation

@Composable
fun AiFinderApp() {
    KoinApplication(application = { modules(aiFinderModule) }) {
        ComposaTheme { AiFinderNavigation() }
    }
}
