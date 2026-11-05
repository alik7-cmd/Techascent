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
import org.techascent.aifinder.domain.vision.CapturedPhoto
import org.techascent.aifinder.domain.vision.PhotoAnalyzer
import org.techascent.aifinder.domain.vision.RecognizedObject
import org.techascent.aifinder.domain.vision.toItemDetections
import org.techascent.aifinder.util.logDebug
import org.techascent.aifinder.domain.subscription.ScanAccessRepository
import org.techascent.aifinder.domain.subscription.ScanAccessRequiredException
import kotlin.math.roundToInt

/** A photo and the items found in it, handed to Review. */
class ScanResult(val photo: CapturedPhoto, val detections: List<ItemDetection>)

class ScanViewModel(
    private val analyzer: PhotoAnalyzer,
    private val access: ScanAccessRepository,
) : ViewModel() {
    private val _state = MutableStateFlow<ScanUiState>(ScanUiState.Success(ScanContent()))
    val state: StateFlow<ScanUiState> = _state.asStateFlow()
    private val _detections = MutableStateFlow<ScanResult?>(null)
    val detections: StateFlow<ScanResult?> = _detections.asStateFlow()
    private var analysisJob: Job? = null
    private var balanceJob: Job? = null
    private var pendingPhoto: CapturedPhoto? = null

    fun loadBalance() {
        balanceJob?.cancel()
        updateContent { it.copy(access = null, isLoadingBalance = true, balanceError = false) }
        balanceJob = viewModelScope.launch {
            try {
                val balance = access.cachedOrRefresh()
                updateContent { it.copy(access = balance, isLoadingBalance = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logDebug(TAG, "Loading scan balance failed: ${e.message}")
                updateContent { it.copy(isLoadingBalance = false, balanceError = true) }
            }
        }
    }

    fun analyze(camera: CameraCaptureController) {
        val current = (_state.value as? ScanUiState.Success)?.data ?: return
        if (current.isAnalyzing || current.requiresPurchase) return
        pendingPhoto = null
        analyzePhoto { camera.capture() }
    }

    fun resumeAfterPurchase() {
        val photo = pendingPhoto ?: return
        if ((_state.value as? ScanUiState.Success)?.data?.isAnalyzing == true) return
        analyzePhoto { photo }
    }

    fun dismissPurchase() {
        pendingPhoto = null
        updateContent { it.copy(requiresPurchase = false) }
    }

    private fun analyzePhoto(capture: suspend () -> CapturedPhoto) {
        updateContent { it.copy(isAnalyzing = true, errorMessage = null, requiresPurchase = false) }
        analysisJob = viewModelScope.launch {
            try {
                val photo = capture()
                pendingPhoto = photo
                val recognized = analyzer.analyze(photo)
                coroutineContext.ensureActive()
                pendingPhoto = null
                logRaw(photo.id, recognized)
                val detections = recognized.toItemDetections(photo.id)
                logTransformed(detections)
                if (detections.isEmpty()) {
                    updateContent {
                        it.copy(isAnalyzing = false, errorMessage = "No items recognized. Try moving closer or adding more light.")
                    }
                } else {
                    updateContent { it.copy(isAnalyzing = false) }
                    _detections.value = ScanResult(photo, detections)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: ScanAccessRequiredException) {
                updateContent { it.copy(isAnalyzing = false, requiresPurchase = true) }
            } catch (e: Exception) {
                pendingPhoto = null
                updateContent {
                    it.copy(isAnalyzing = false, errorMessage = e.message ?: "Couldn't analyze the photo. Please try again.")
                }
            } finally {
                if (coroutineContext[Job]?.isActive == true) loadBalance()
            }
        }
    }

    fun cancelAnalysis() {
        analysisJob?.cancel()
        analysisJob = null
        pendingPhoto = null
        updateContent { it.copy(isAnalyzing = false, requiresPurchase = false) }
        consumeDetections()
    }

    fun consumeDetections() {
        _detections.value = null
    }

    fun retry() {
        _state.value = ScanUiState.Success(ScanContent())
        loadBalance()
    }

    fun reset() {
        cancelAnalysis()
        balanceJob?.cancel()
        balanceJob = null
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
