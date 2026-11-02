package org.techascent.aifinder.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
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

    fun get(id: String): ImageBitmap? = entries[id]

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
    placeholder: @Composable () -> Unit = {},
) {
    val photos = koinInject<ItemPhotoRepository>()
    val image by produceState(photoId?.let(DecodedPhotos::get), photoId) {
        if (value != null || photoId == null) return@produceState
        value = withContext(Dispatchers.Default) {
            photos.load(photoId)?.let { bytes -> runCatching { bytes.decodeToImageBitmap() }.getOrNull() }
        }?.also { DecodedPhotos.put(photoId, it) }
    }
    Box(modifier.clip(shape).background(AiFinderPalette.Mint), contentAlignment = Alignment.Center) {
        val bitmap = image
        if (bitmap != null) {
            Image(bitmap, contentDescription, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        } else {
            placeholder()
        }
    }
}
