package org.techascent.aifinder.data.firebase

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** Native SDK bridge; paths are scoped and authorized again by the Swift host. */
interface IosFirebaseBridge {
    fun deleteProfile(userId: String, completion: (String?) -> Unit)
    fun read(path: String, completion: (String?, String?) -> Unit)
    fun write(path: String, json: String, completion: (String?) -> Unit)
    fun update(path: String, json: String, completion: (String?) -> Unit)
    fun wallet(userId: String, operation: String, json: String, completion: (String?, String?) -> Unit)
    fun upload(path: String, base64: String, completion: (String?) -> Unit)
    fun download(path: String, completion: (String?, String?) -> Unit)
    fun deletePhoto(path: String, completion: (String?) -> Unit)
}

object IosFirebaseBridgeRegistry {
    private var bridge: IosFirebaseBridge? = null
    fun install(bridge: IosFirebaseBridge) { this.bridge = bridge }
    internal fun requireBridge(): IosFirebaseBridge = checkNotNull(bridge) { "Install the native Firebase bridge." }
}

internal suspend fun <T> firebaseCall(call: (IosFirebaseBridge, (T?, String?) -> Unit) -> Unit): T =
    withContext(Dispatchers.Main.immediate) {
        suspendCancellableCoroutine { continuation ->
            call(IosFirebaseBridgeRegistry.requireBridge()) { result, error ->
                if (continuation.isActive) {
                    if (error != null) continuation.resumeWithException(IllegalStateException(error))
                    else if (result != null) continuation.resume(result)
                    else continuation.resumeWithException(IllegalStateException("Firebase returned no response."))
                }
            }
        }
    }

internal suspend fun firebaseWrite(path: String, value: Map<String, Any?>, update: Boolean = false) {
    firebaseCall<Unit> { bridge, done ->
        val completion: (String?) -> Unit = { done(Unit, it) }
        if (update) bridge.update(path, value.toJson().toString(), completion)
        else bridge.write(path, value.toJson().toString(), completion)
    }
}

internal fun Any?.toJson(): JsonElement = when (this) {
    null -> JsonNull
    is String -> JsonPrimitive(this)
    is Number -> JsonPrimitive(this)
    is Boolean -> JsonPrimitive(this)
    is List<*> -> JsonArray(map { it.toJson() })
    is Map<*, *> -> JsonObject(entries.associate { it.key.toString() to it.value.toJson() })
    else -> error("Unsupported Firebase value.")
}

internal fun JsonElement.toValue(): Any? = when (this) {
    JsonNull -> null
    is JsonObject -> mapValues { it.value.toValue() }
    is JsonArray -> map { it.toValue() }
    is JsonPrimitive -> if (isString) content else booleanOrNull ?: longOrNull ?: doubleOrNull
}

internal suspend fun readNodes(path: String): List<Pair<String, Map<*, *>>> {
    val text = firebaseCall<String> { bridge, done -> bridge.read(path, done) }
    val parsed = Json.parseToJsonElement(text)
    if (parsed == JsonNull) return emptyList()
    return parsed.jsonObject.map { (id, value) ->
        id to checkNotNull(value.toValue() as? Map<*, *>) { "Invalid Firebase record." }
    }
}
