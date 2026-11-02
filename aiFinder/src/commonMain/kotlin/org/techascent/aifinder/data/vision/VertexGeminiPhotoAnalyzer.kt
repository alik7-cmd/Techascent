package org.techascent.aifinder.data.vision

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.add
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import org.techascent.aifinder.domain.vision.CapturedPhoto
import org.techascent.aifinder.domain.vision.ItemDetectionPrompt
import org.techascent.aifinder.domain.vision.PhotoAnalyzer
import org.techascent.aifinder.domain.vision.RecognizedObject
import org.techascent.aifinder.util.logDebug

/**
 * Analyzes photos with Gemini on Vertex AI (express mode, API key auth) through the REST API,
 * so Android and iOS share the same implementation.
 */
class VertexGeminiPhotoAnalyzer(
    private val client: HttpClient,
    private val apiKey: String,
    private val model: String,
    private val dispatcher: CoroutineDispatcher
) : PhotoAnalyzer {

    @OptIn(ExperimentalEncodingApi::class)
    override suspend fun analyze(photo: CapturedPhoto): List<RecognizedObject> {
        if (apiKey.isBlank()) {
            throw IllegalStateException("Vertex AI isn't configured. Add VERTEX_API_KEY to local.properties and rebuild.")
        }
        val request = withContext(dispatcher) { buildRequest(Base64.encode(photo.jpegBytes)) }
        logDebug(TAG, "Sending ${photo.id} (${photo.jpegBytes.size / 1024} KB) to Vertex AI $model")

        val response = client.post("$BASE_URL/publishers/google/models/$model:generateContent") {
            parameter("key", apiKey)
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        if (!response.status.isSuccess()) {
            val body = response.bodyAsText()
            logDebug(TAG, "Vertex AI error ${response.status.value}: $body")
            throw IllegalStateException(errorMessage(response.status.value, body))
        }

        val result = response.body<GenerateContentResponse>()
        val candidate = result.candidates.firstOrNull()
        val text = candidate?.content?.parts.orEmpty().filter { it.thought != true }.mapNotNull { it.text }.joinToString("")
        logDebug(TAG, "Vertex AI raw response (finish=${candidate?.finishReason}): $text")
        if (text.isBlank()) {
            throw IllegalStateException("The AI couldn't analyze this photo. Please try another one.")
        }
        return withContext(dispatcher) { parseItems(text) }
    }

    private fun buildRequest(imageBase64: String): JsonObject = buildJsonObject {
        putJsonObject("systemInstruction") {
            putJsonArray("parts") { add(buildJsonObject { put("text", ItemDetectionPrompt.system) }) }
        }
        putJsonArray("contents") {
            add(buildJsonObject {
                put("role", "user")
                putJsonArray("parts") {
                    add(buildJsonObject {
                        putJsonObject("inlineData") {
                            put("mimeType", "image/jpeg")
                            put("data", imageBase64)
                        }
                    })
                    add(buildJsonObject { put("text", ItemDetectionPrompt.text) })
                }
            })
        }
        putJsonObject("generationConfig") {
            put("temperature", 0.1)
            put("maxOutputTokens", 4096)
            put("responseMimeType", "application/json")
            put("responseSchema", responseSchema)
        }
    }

    private fun parseItems(text: String): List<RecognizedObject> =
        json.decodeFromString<DetectedItemsResponse>(text).items.mapNotNull { item ->
            val name = item.name.trim()
            if (name.isEmpty()) return@mapNotNull null
            RecognizedObject(
                label = name,
                confidence = item.confidence.coerceIn(0f, 1f),
                bounds = item.box2d?.toBounds(),
                category = item.category?.trim()?.takeIf { it in ItemDetectionPrompt.categories }
            )
        }

    // Gemini returns [ymin, xmin, ymax, xmax] in 0..1000; the app uses [left, top, right, bottom] in 0..1.
    private fun List<Float>.toBounds(): List<Float>? {
        if (size != 4) return null
        val (yMin, xMin, yMax, xMax) = map { (it / 1000f).coerceIn(0f, 1f) }
        return if (xMax > xMin && yMax > yMin) listOf(xMin, yMin, xMax, yMax) else null
    }

    private fun errorMessage(status: Int, body: String): String {
        val apiMessage = runCatching {
            json.parseToJsonElement(body).jsonObject["error"]?.jsonObject?.get("message")?.jsonPrimitive?.content
        }.getOrNull()
        return when (status) {
            400 -> apiMessage ?: "Vertex AI rejected the request."
            401, 403 -> "Vertex AI denied access. Check the API key and that the Vertex AI API is enabled."
            404 -> "Model \"$model\" isn't available on Vertex AI."
            429 -> "Vertex AI quota reached. Please try again in a moment."
            else -> "Vertex AI is unavailable right now ($status). Please try again."
        }
    }

    private companion object {
        const val TAG = "AiFinderScan"
        const val BASE_URL = "https://aiplatform.googleapis.com/v1"
        val json = Json { ignoreUnknownKeys = true; isLenient = true }

        val responseSchema = buildJsonObject {
            put("type", "OBJECT")
            putJsonObject("properties") {
                putJsonObject("items") {
                    put("type", "ARRAY")
                    putJsonObject("items") {
                        put("type", "OBJECT")
                        putJsonObject("properties") {
                            putJsonObject("name") { put("type", "STRING") }
                            putJsonObject("category") {
                                put("type", "STRING")
                                putJsonArray("enum") { ItemDetectionPrompt.categories.forEach { add(it) } }
                            }
                            putJsonObject("confidence") { put("type", "NUMBER") }
                            putJsonObject("box_2d") {
                                put("type", "ARRAY")
                                putJsonObject("items") { put("type", "INTEGER") }
                            }
                        }
                        put("required", buildJsonArray {
                            add("name")
                            add("category")
                            add("confidence")
                        })
                    }
                }
            }
            put("required", buildJsonArray { add("items") })
        }
    }
}
