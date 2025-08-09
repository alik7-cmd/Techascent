package org.techascent.muslim.compass

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
import org.techascent.muslim.getQiblaDirection

class CompassViewModel: ViewModel() {
    val qiblaDirection: Flow<Float> = getQiblaDirection(59.9139, 10.7522)
}