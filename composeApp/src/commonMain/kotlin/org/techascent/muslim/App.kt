package org.techascent.muslim

import androidx.compose.runtime.*
import org.koin.compose.KoinContext
import org.techascent.muslim.di.initializeKoin
import org.techascent.muslim.home.MainScreen

@Composable
fun App() {
    initializeKoin()
    KoinContext {
        //AppNavGraph()
        MainScreen()
    }
}
