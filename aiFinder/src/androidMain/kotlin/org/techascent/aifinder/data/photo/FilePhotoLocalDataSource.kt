package org.techascent.aifinder.data.photo

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class FilePhotoLocalDataSource(private val root: File) : PhotoLocalDataSource {
    override suspend fun deleteForUser(userId: String): Unit = withContext(Dispatchers.IO) {
        val directory = File(root, StoragePaths.safeName(userId))
        check(!directory.exists() || directory.deleteRecursively()) { "Couldn't clear cached photos." }
    }

    override suspend fun read(userId: String, photoId: String): ByteArray? = withContext(Dispatchers.IO) {
        file(userId, photoId).takeIf { it.isFile }?.readBytes()
    }

    override suspend fun write(userId: String, photoId: String, jpegBytes: ByteArray) = withContext(Dispatchers.IO) {
        val target = file(userId, photoId)
        target.parentFile?.mkdirs()
        // Write then rename so a crash never leaves a half-written photo behind.
        val temp = File(target.parentFile, "${target.name}.tmp")
        temp.writeBytes(jpegBytes)
        if (!temp.renameTo(target)) {
            target.delete()
            check(temp.renameTo(target)) { "Couldn't store photo." }
        }
    }

    private fun file(userId: String, photoId: String) =
        File(File(root, StoragePaths.safeName(userId)), "${StoragePaths.safeName(photoId)}.jpg")
}
