package org.techascent.aifinder.data.photo

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.dataWithContentsOfFile
import platform.Foundation.writeToFile
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class FilePhotoLocalDataSource : PhotoLocalDataSource {
    private val fileManager get() = NSFileManager.defaultManager

    override suspend fun deleteForUser(userId: String): Unit = withContext(Dispatchers.IO) {
        val path = directory(userId)
        check(!fileManager.fileExistsAtPath(path) || fileManager.removeItemAtPath(path, error = null)) {
            "Couldn't clear cached photos."
        }
    }

    override suspend fun read(userId: String, photoId: String): ByteArray? = withContext(Dispatchers.IO) {
        val data = NSData.dataWithContentsOfFile(path(userId, photoId)) ?: return@withContext null
        val size = data.length.toInt()
        if (size == 0) return@withContext null
        ByteArray(size).also { bytes -> bytes.usePinned { memcpy(it.addressOf(0), data.bytes, data.length) } }
    }

    override suspend fun write(userId: String, photoId: String, jpegBytes: ByteArray) = withContext(Dispatchers.IO) {
        val directory = directory(userId)
        fileManager.createDirectoryAtPath(directory, withIntermediateDirectories = true, attributes = null, error = null)
        val data = jpegBytes.usePinned { NSData.create(bytes = it.addressOf(0), length = jpegBytes.size.toULong()) }
        check(data.writeToFile(path(userId, photoId), atomically = true)) { "Couldn't store photo." }
    }

    private fun directory(userId: String): String {
        val base = fileManager.URLForDirectory(
            directory = NSApplicationSupportDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = true,
            error = null,
        )?.path ?: error("Unable to locate the photo directory.")
        return "$base/item_photos/${StoragePaths.safeName(userId)}"
    }

    private fun path(userId: String, photoId: String) = "${directory(userId)}/${StoragePaths.safeName(photoId)}.jpg"
}
