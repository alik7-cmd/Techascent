package org.techascent.aifinder.domain.photo

/** JPEG photos of scanned spaces, keyed by the photo id stored on each item. */
interface ItemPhotoRepository {
    /** Uploads the photo to cloud storage, then keeps a local copy. */
    suspend fun save(photoId: String, jpegBytes: ByteArray): Result<Unit>

    /** Returns the photo from the device cache, downloading it once if it isn't cached; null if unavailable. */
    suspend fun load(photoId: String): ByteArray?
}

sealed class PhotoError(message: String) : Exception(message) {
    data object NotSignedIn : PhotoError("Sign in to back up your photos.")
    data object Unavailable : PhotoError("Photo backup isn't available on this device yet.")
    class Remote(message: String?) : PhotoError(message ?: "Couldn't upload the photo. Try again.")
}
