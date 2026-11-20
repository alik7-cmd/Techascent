package org.techascent.aifinder.data.photo

import org.techascent.aifinder.domain.photo.PhotoError

// iOS needs GoogleService-Info.plist and the Firebase iOS SDK before Storage can be used.
class UnavailablePhotoDataSource : PhotoRemoteDataSource {
    override suspend fun upload(userId: String, photoId: String, jpegBytes: ByteArray) = throw PhotoError.Unavailable

    override suspend fun download(userId: String, photoId: String): ByteArray? = null
}
