package org.techascent.aifinder.util

import kotlinx.coroutines.runBlocking
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.*

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class DeviceFilesTest {
    @Test
    fun printedQrWithHumanReadablePlaceNameDecodesLocally() = runBlocking {
        val payload = "aifinder://place/-Nv_12345"
        val png = generateQrPng(payload, "Bedroom > Wardrobe > Top drawer")
        assertEquals(payload, decodeQrPhoto(png))
    }

}
