package org.techascent.aifinder.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.decodeToImageBitmap
import org.koin.compose.koinInject
import org.techascent.aifinder.domain.photo.ItemPhotoRepository
import org.techascent.aifinder.presentation.theme.AiFinderPalette

// Items from one scan share a photo; keep a few decoded so lists don't decode the same JPEG repeatedly.
private object DecodedPhotos {
    private const val LIMIT = 12
    private val entries = LinkedHashMap<String, ImageBitmap>()
    private var generation: String? = null

    fun get(id: String, currentGeneration: String): ImageBitmap? {
        if (generation != currentGeneration) {
            entries.clear()
            generation = currentGeneration
        }
        return entries[id]
    }

    fun put(id: String, image: ImageBitmap) {
        entries.remove(id)
        entries[id] = image
        while (entries.size > LIMIT) entries.remove(entries.keys.first())
    }
}

/** Shows an item's scan photo from the local cache (downloading it once if needed), or [placeholder]. */
@Composable
fun ItemPhoto(
    photoId: String?,
    contentDescription: String?,
    shape: Shape,
    modifier: Modifier = Modifier,
    bounds: List<Float>? = null,
    jpegBytes: ByteArray? = null,
    contentScale: ContentScale = if (bounds != null) ContentScale.Fit else ContentScale.Crop,
    matchImageAspectRatio: Boolean = false,
    placeholder: @Composable () -> Unit = {},
) {
    val photos = koinInject<ItemPhotoRepository>()
    val auth = koinInject<org.techascent.aifinder.domain.auth.AuthRepository>()
    val revision by photos.changes.collectAsState(0L)
    val generation = "${auth.currentUser?.id}/$revision"
    val cacheKey = "$generation/$photoId"
    val image by produceState(DecodedPhotos.get(cacheKey, generation), cacheKey) {
        if (value != null || photoId == null) return@produceState
        value = withContext(Dispatchers.Default) {
            (jpegBytes ?: photos.load(photoId))?.let { bytes -> runCatching { bytes.decodeToImageBitmap() }.getOrNull() }
        }?.also { DecodedPhotos.put(cacheKey, it) }
    }
    val photoModifier = if (matchImageAspectRatio) {
        modifier.aspectRatio(image?.let { it.width.toFloat() / it.height } ?: (4f / 3f))
    } else modifier
    Box(photoModifier.clip(shape).background(AiFinderPalette.Mint), contentAlignment = Alignment.Center) {
        val bitmap = image
        if (bitmap != null) {
            Image(bitmap, contentDescription, Modifier.fillMaxSize(),
                contentScale = contentScale)
            if (bounds != null && bounds.size == 4 && bounds.all { it.isFinite() && it in 0f..1f } &&
                bounds[2] > bounds[0] && bounds[3] > bounds[1]) {
                Canvas(Modifier.fillMaxSize()) {
                    val scale = contentScale.computeScaleFactor(
                        Size(bitmap.width.toFloat(), bitmap.height.toFloat()), size)
                    val width = bitmap.width * scale.scaleX
                    val height = bitmap.height * scale.scaleY
                    val dx = (size.width - width) / 2
                    val dy = (size.height - height) / 2
                    val topLeft = Offset(dx + bounds[0] * width, dy + bounds[1] * height)
                    val area = Size((bounds[2] - bounds[0]) * width, (bounds[3] - bounds[1]) * height)
                    drawRect(AiFinderPalette.Teal.copy(alpha = 0.12f), topLeft, area)
                    drawRect(androidx.compose.ui.graphics.Color.White, topLeft, area, style = Stroke(5.dp.toPx()))
                    drawRect(AiFinderPalette.Teal, topLeft, area, style = Stroke(2.dp.toPx()))
                    val center = Offset(topLeft.x + area.width / 2, topLeft.y + area.height / 2)
                    drawCircle(androidx.compose.ui.graphics.Color.White, 7.dp.toPx(), center)
                    drawCircle(AiFinderPalette.Teal, 4.dp.toPx(), center)
                }
            }
        } else {
            placeholder()
        }
    }
}
