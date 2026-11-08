package org.techascent.aifinder.util

/** QR generation, decoding and sharing are local operations and never use the AI service. */
expect suspend fun generateQrPng(value: String, label: String = ""): ByteArray
expect suspend fun decodeQrPhoto(jpeg: ByteArray): String?
expect suspend fun shareFile(name: String, data: ByteArray, mimeType: String)
expect suspend fun createInventoryPdf(lines: List<String>): ByteArray
