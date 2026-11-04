package org.techascent.aifinder.data.photo

interface PhotoRemoteDataSource {
    suspend fun upload(userId: String, photoId: String, jpegBytes: ByteArray)

    /** Returns null when the photo doesn't exist remotely. */
    suspend fun download(userId: String, photoId: String): ByteArray?
}

/** Persistent on-device photo files, separated per user. */
interface PhotoLocalDataSource {
    suspend fun read(userId: String, photoId: String): ByteArray?
    suspend fun write(userId: String, photoId: String, jpegBytes: ByteArray)
    suspend fun deleteForUser(userId: String)
}

object StoragePaths {
    fun photo(userId: String, photoId: String) = "aifinder/users/$userId/photos/${safeName(photoId)}.jpg"

    /** Photo ids come from the app, but never let one escape its folder. */
    fun safeName(photoId: String) = photoId.replace(Regex("[^A-Za-z0-9._-]"), "_")
}
