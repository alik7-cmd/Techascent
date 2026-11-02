package org.techascent.aifinder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import org.techascent.aifinder.data.auth.CurrentActivityHolder

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CurrentActivityHolder.attach(this)
        setContent {
            AiFinderApp()
        }
    }

    override fun onResume() {
        super.onResume()
        CurrentActivityHolder.attach(this)
    }

    override fun onDestroy() {
        CurrentActivityHolder.detach(this)
        super.onDestroy()
    }
}
