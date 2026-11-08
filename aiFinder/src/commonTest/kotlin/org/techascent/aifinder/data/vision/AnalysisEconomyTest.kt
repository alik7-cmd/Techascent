package org.techascent.aifinder.data.vision

import io.ktor.client.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.*
import kotlin.test.*

class AnalysisEconomyTest {
    @Test
    fun singlePhotoRequestCapsOutputAndObjectsAndDisablesFlashThinking() {
        val client = HttpClient()
        try {
            val analyzer = VertexGeminiPhotoAnalyzer(client, "", "gemini-2.5-flash", Dispatchers.Default)
            val request = analyzer.buildRequest("base64-image")
            val config = request.getValue("generationConfig").jsonObject
            assertEquals(2048, config.getValue("maxOutputTokens").jsonPrimitive.int)
            assertEquals(0, config.getValue("thinkingConfig").jsonObject.getValue("thinkingBudget").jsonPrimitive.int)
            val items = config.getValue("responseSchema").jsonObject.getValue("properties")
                .jsonObject.getValue("items").jsonObject
            assertEquals(15, items.getValue("maxItems").jsonPrimitive.int)
            val box = items.getValue("items").jsonObject.getValue("properties").jsonObject.getValue("box_2d").jsonObject
            assertEquals(4, box.getValue("maxItems").jsonPrimitive.int)
            val parts = request.getValue("contents").jsonArray.single().jsonObject.getValue("parts").jsonArray
            assertEquals(1, parts.count { "inlineData" in it.jsonObject })
        } finally { client.close() }
    }
}
