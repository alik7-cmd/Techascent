package org.techascent.aifinder.data.vision

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class GenerateContentResponse(val candidates: List<Candidate> = emptyList())

@Serializable
internal data class Candidate(val content: Content? = null, val finishReason: String? = null)

@Serializable
internal data class Content(val parts: List<Part> = emptyList())

@Serializable
internal data class Part(val text: String? = null)

@Serializable
internal data class DetectedItemsResponse(val items: List<DetectedItem> = emptyList())

@Serializable
internal data class DetectedItem(
    val name: String = "",
    val category: String? = null,
    val confidence: Float = 0.8f,
    @SerialName("box_2d") val box2d: List<Float>? = null
)
