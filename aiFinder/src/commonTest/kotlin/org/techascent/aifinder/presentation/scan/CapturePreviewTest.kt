package org.techascent.aifinder.presentation.scan

import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.techascent.aifinder.domain.subscription.*
import org.techascent.aifinder.domain.vision.*
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class CapturePreviewTest {
    private class Access : ScanAccessRepository {
        var checks = 0
        var spends = 0
        override suspend fun refresh(): ScanAccess { checks++; return ScanAccess("u", 3) }
        override suspend fun consume(userId: String, scanId: String): ScanAccess { spends++; return ScanAccess("u", 2) }
        override suspend fun recordPurchase(purchase: ScanPurchase): ScanAccess = error("Unused")
    }
    @BeforeTest fun setup() = Dispatchers.setMain(UnconfinedTestDispatcher())
    @AfterTest fun cleanup() = Dispatchers.resetMain()

    @Test
    fun previewAndRetakesAreLocalAndOnlyExplicitAnalysisUsesOneCallAndOneCredit() {
        var modelCalls = 0
        val access = Access()
        val analyzer = object : PhotoAnalyzer {
            override suspend fun analyze(photo: CapturedPhoto): List<RecognizedObject> {
                modelCalls++
                return listOf(RecognizedObject("Keys", 0.9f, listOf(0.1f, 0.2f, 0.4f, 0.5f)))
            }
        }
        val camera = CameraCaptureController().apply {
            captureHandler = { CapturedPhoto("photo", byteArrayOf(1)) }
        }
        val vm = ScanViewModel(MeteredPhotoAnalyzer(analyzer, access), access)
        vm.capture(camera)
        assertNotNull((vm.state.value as ScanUiState.Success).data.previewPhoto)
        assertEquals(0, access.checks)
        assertEquals(0, modelCalls)
        vm.retake()
        assertNull((vm.state.value as ScanUiState.Success).data.previewPhoto)
        vm.capture(camera)
        vm.analyze(camera)
        assertEquals(1, modelCalls)
        assertEquals(1, access.spends)
        assertNotNull(vm.detections.value?.detections?.single()?.bounds)
        vm.reset()
    }

    @Test
    fun analysisUsesThePreviewPhotoWithoutRecapturingAndBackClearsIt() {
        val access = Access()
        var captures = 0
        val preview = CapturedPhoto("preview", byteArrayOf(1))
        var analyzed: CapturedPhoto? = null
        val analyzer = object : PhotoAnalyzer {
            override suspend fun analyze(photo: CapturedPhoto): List<RecognizedObject> {
                analyzed = photo
                awaitCancellation()
            }
        }
        val camera = CameraCaptureController().apply {
            captureHandler = { captures++; preview }
        }
        val vm = ScanViewModel(analyzer, access)
        vm.capture(camera)
        vm.analyze(camera)
        assertSame(preview, analyzed)
        assertEquals(1, captures)
        assertSame(preview, (vm.state.value as ScanUiState.Success).data.previewPhoto)
        assertTrue((vm.state.value as ScanUiState.Success).data.isAnalyzing)
        vm.cancelAnalysis()
        val content = (vm.state.value as ScanUiState.Success).data
        assertFalse(content.isAnalyzing)
        assertFalse(content.isCapturing)
        assertNull(content.previewPhoto)
        assertNull(vm.detections.value)
    }
}
