package org.techascent.aifinder.util

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.content.FileProvider
import androidx.core.graphics.createBitmap
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.techascent.aifinder.data.auth.CurrentActivityHolder
import java.io.ByteArrayOutputStream
import java.io.File
import org.techascent.aifinder.data.cache.AppContextHolder

actual suspend fun clearSharedFiles(): Unit = withContext(Dispatchers.IO) {
    val directory = File(AppContextHolder.context.cacheDir, "exports")
    check(!directory.exists() || directory.deleteRecursively()) { "Couldn't clear temporary exports." }
}

actual suspend fun generateQrPng(value: String, label: String): ByteArray = withContext(Dispatchers.Default) {
    val matrix = QRCodeWriter().encode(value, BarcodeFormat.QR_CODE, 768, 768,
        mapOf(EncodeHintType.MARGIN to 4, EncodeHintType.CHARACTER_SET to "UTF-8"))
    val pixels = IntArray(matrix.width * matrix.height) { index ->
        if (matrix[index % matrix.width, index / matrix.width]) 0xFF193437.toInt() else 0xFFFFFFFF.toInt()
    }
    val bitmap = createBitmap(matrix.width, matrix.height + if (label.isBlank()) 0 else 176)
    try {
        val canvas = android.graphics.Canvas(bitmap)
        canvas.drawColor(android.graphics.Color.WHITE)
        canvas.drawBitmap(pixels, 0, matrix.width, 0, 0, matrix.width, matrix.height, false, null)
        if (label.isNotBlank()) {
            val paint = android.text.TextPaint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                textSize = 28f
                color = 0xFF193437.toInt()
            }
            val text = android.text.StaticLayout.Builder.obtain(label, 0, label.length, paint, matrix.width - 64)
                .setAlignment(android.text.Layout.Alignment.ALIGN_CENTER)
                .setMaxLines(3).setEllipsize(android.text.TextUtils.TruncateAt.END).build()
            canvas.translate(32f, matrix.height.toFloat() + 16)
            text.draw(canvas)
        }
        ByteArrayOutputStream().use { stream ->
            check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)) { "Couldn't generate QR label." }
            stream.toByteArray()
        }
    } finally { bitmap.recycle() }
}

actual suspend fun decodeQrPhoto(jpeg: ByteArray): String? = withContext(Dispatchers.Default) {
    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(jpeg, 0, jpeg.size, options)
    options.inSampleSize = maxOf(1, maxOf(options.outWidth, options.outHeight) / 1600)
    options.inJustDecodeBounds = false
    val bitmap = checkNotNull(BitmapFactory.decodeByteArray(jpeg, 0, jpeg.size, options)) { "Couldn't read the photo." }
    try {
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        val source = RGBLuminanceSource(bitmap.width, bitmap.height, pixels)
        val reader = MultiFormatReader().apply {
            setHints(mapOf(DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE), DecodeHintType.TRY_HARDER to true))
        }
        try { reader.decodeWithState(BinaryBitmap(HybridBinarizer(source))).text }
        catch (_: NotFoundException) { null }
    } finally { bitmap.recycle() }
}

actual suspend fun shareFile(name: String, data: ByteArray, mimeType: String): Unit = withContext(Dispatchers.Main) {
    val activity = checkNotNull(CurrentActivityHolder.activity) { "No active screen to share from." }
    require(name.matches(Regex("[A-Za-z0-9._-]+"))) { "Invalid export filename." }
    val directory = File(activity.cacheDir, "exports").apply { mkdirs() }
    val file = File(directory, name)
    withContext(Dispatchers.IO) { file.writeBytes(data) }
    val uri = FileProvider.getUriForFile(activity, "${activity.packageName}.files", file)
    activity.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
        type = mimeType
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }, null))
}

actual suspend fun createInventoryPdf(lines: List<String>): ByteArray = withContext(Dispatchers.Default) {
    val document = android.graphics.pdf.PdfDocument()
    try {
        val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF193437.toInt()
            textSize = 12f
        }
        val wrapped = lines.flatMap { line ->
            buildList {
                var remaining = line.replace("\n", " ")
                if (remaining.isEmpty()) add("")
                while (remaining.isNotEmpty()) {
                    val count = paint.breakText(remaining, true, 523f, null).coerceAtLeast(1)
                    add(remaining.take(count))
                    remaining = remaining.drop(count)
                }
            }
        }
        wrapped.chunked(44).forEachIndexed { index, pageLines ->
            val page = document.startPage(android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, index + 1).create())
            pageLines.forEachIndexed { line, text -> page.canvas.drawText(text, 36f, 48f + line * 17f, paint) }
            document.finishPage(page)
        }
        ByteArrayOutputStream().use { document.writeTo(it); it.toByteArray() }
    } finally { document.close() }
}
