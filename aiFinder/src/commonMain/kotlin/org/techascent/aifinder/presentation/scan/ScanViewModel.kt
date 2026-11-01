package org.techascent.aifinder.presentation.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.techascent.aifinder.domain.model.ItemDetection
import org.techascent.aifinder.domain.vision.PhotoAnalyzer
import org.techascent.aifinder.domain.vision.RecognizedObject
import org.techascent.aifinder.domain.vision.toItemDetections
import org.techascent.aifinder.util.logDebug
import kotlin.math.roundToInt

class ScanViewModel(
    private val analyzer: PhotoAnalyzer
) : ViewModel() {
    private val _state = MutableStateFlow<ScanUiState>(ScanUiState.Success(ScanContent()))
    val state: StateFlow<ScanUiState> = _state.asStateFlow()
    private val _detections = MutableStateFlow<List<ItemDetection>?>(null)
    val detections: StateFlow<List<ItemDetection>?> = _detections.asStateFlow()
    private var analysisJob: Job? = null

    fun analyze(camera: CameraCaptureController) {
        val current = (_state.value as? ScanUiState.Success)?.data ?: return
        if (current.isAnalyzing) return
        updateContent { it.copy(isAnalyzing = true, errorMessage = null) }
        analysisJob = viewModelScope.launch {
            try {
                val photo = camera.capture()
                val recognized = analyzer.analyze(photo)
                coroutineContext.ensureActive()
                logRaw(photo.id, recognized)
                val detections = recognized.toItemDetections(photo.id)
                logTransformed(detections)
                if (detections.isEmpty()) {
                    updateContent {
                        it.copy(isAnalyzing = false, errorMessage = "No items recognized. Try moving closer or adding more light.")
                    }
                } else {
                    updateContent { it.copy(isAnalyzing = false) }
                    _detections.value = detections
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                updateContent {
                    it.copy(isAnalyzing = false, errorMessage = e.message ?: "Couldn't analyze the photo. Please try again.")
                }
            }
        }
    }

    fun cancelAnalysis() {
        analysisJob?.cancel()
        analysisJob = null
        updateContent { it.copy(isAnalyzing = false) }
        consumeDetections()
    }

    fun consumeDetections() {
        _detections.value = null
    }

    fun retry() {
        _state.value = ScanUiState.Success(ScanContent())
    }

    private fun logRaw(photoId: String, results: List<RecognizedObject>) {
        logDebug(TAG, "Raw detection for $photoId: ${results.size} result(s)")
        results.forEachIndexed { i, r ->
            val pct = (r.confidence * 100).roundToInt()
            logDebug(TAG, "  raw[$i] label=\"${r.label}\" confidence=$pct% bounds=${r.bounds ?: "whole image"}")
        }
    }

    private fun logTransformed(detections: List<ItemDetection>) {
        logDebug(TAG, "Transformed into ${detections.size} ItemDetection(s)")
        detections.forEachIndexed { i, d ->
            logDebug(TAG, "  item[$i] $d")
        }
    }

    private fun updateContent(transform: (ScanContent) -> ScanContent) {
        _state.update { state -> if (state is ScanUiState.Success) state.copy(data = transform(state.data)) else state }
    }
}

private const val TAG = "AiFinderScan"
