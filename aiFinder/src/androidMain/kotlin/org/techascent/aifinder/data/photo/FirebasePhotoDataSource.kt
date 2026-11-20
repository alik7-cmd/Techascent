package org.techascent.aifinder.data.photo

import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.tasks.await
import org.techascent.aifinder.BuildConfig

class FirebasePhotoDataSource(
    private val storage: FirebaseStorage = FirebaseStorage.getInstance(BuildConfig.FIREBASE_STORAGE_BUCKET),
) : PhotoRemoteDataSource {
    override suspend fun delete(userId: String, photoId: String) {
        try { storage.reference.child(StoragePaths.photo(userId, photoId)).delete().await() }
        catch (e: StorageException) { if (e.errorCode != StorageException.ERROR_OBJECT_NOT_FOUND) throw e }
    }

    override suspend fun upload(userId: String, photoId: String, jpegBytes: ByteArray) {
        val metadata = StorageMetadata.Builder().setContentType("image/jpeg").build()
        storage.reference.child(StoragePaths.photo(userId, photoId)).putBytes(jpegBytes, metadata).await()
    }

    override suspend fun download(userId: String, photoId: String): ByteArray? = try {
        storage.reference.child(StoragePaths.photo(userId, photoId)).getBytes(MAX_PHOTO_BYTES).await()
    } catch (e: StorageException) {
        if (e.errorCode == StorageException.ERROR_OBJECT_NOT_FOUND) null else throw e
    }

    private companion object {
        const val MAX_PHOTO_BYTES = 10L * 1024 * 1024
    }
}
