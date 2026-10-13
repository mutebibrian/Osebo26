package com.devbrian.osebo.data.repository

import com.devbrian.osebo.data.remote.ApiResult
import com.devbrian.osebo.data.remote.KtorOseboApiService
import com.devbrian.osebo.models.AiAssistantReadiness
import com.devbrian.osebo.models.AiChatMessage
import com.devbrian.osebo.models.AiConversation
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Mirrors androidMain's AiAssistantRepository as closely as kotlinx.serialization
 * allows. That Gson-based implementation parses every response defensively
 * against several possible key names and retries POSTs with a different body
 * shape on 400/422 — a strong signal the backend's AI endpoints aren't fully
 * stable/documented, so this ports the same lenient parsing rather than
 * assuming a fixed schema the way every other repository in this codebase
 * can (those all hit well-behaved, consistently-shaped endpoints).
 */
class SharedAiAssistantRepository(private val api: KtorOseboApiService) {
    suspend fun getReadiness(): Result<AiAssistantReadiness> = runCatching {
        val data = requireData(api.getAiAssistantReadiness())
        val readiness = data.asObjectOrNull()
        val status = readiness?.string("status", "state")?.lowercase()
        val isReady = readiness?.boolean("ready", "isReady", "available", "isAvailable")
            ?: when (status) {
                "ready", "healthy", "ok", "available" -> true
                "unavailable", "offline", "disabled", "not_ready" -> false
                else -> true
            }
        AiAssistantReadiness(
            isReady = isReady,
            message = readiness?.string("message", "reason", "detail").orEmpty(),
        )
    }

    suspend fun getConversations(): Result<List<AiConversation>> = runCatching {
        val data = requireData(api.getAiConversations())
        conversationArray(data).mapNotNull(::parseConversation)
    }

    suspend fun createConversation(title: String): Result<AiConversation> = runCatching {
        var result = api.createAiConversation(buildJsonObject { put("title", title) })
        if (result is ApiResult.Error && (result.httpCode == 400 || result.httpCode == 422)) {
            result = api.createAiConversation(buildJsonObject {})
        }
        val data = requireData(result)
        parseConversation(conversationObject(data) ?: data.asObjectOrNull())
            ?: error("The assistant did not return a conversation")
    }

    suspend fun getConversation(conversationId: String): Result<AiConversation> = runCatching {
        val data = requireData(api.getAiConversation(conversationId))
        parseConversation(conversationObject(data) ?: data.asObjectOrNull())
            ?: error("The conversation could not be loaded")
    }

    suspend fun sendMessage(conversationId: String, message: String): Result<List<AiChatMessage>> = runCatching {
        var result = api.sendAiMessage(conversationId, buildJsonObject { put("message", message) })
        if (result is ApiResult.Error && (result.httpCode == 400 || result.httpCode == 422)) {
            result = api.sendAiMessage(conversationId, buildJsonObject { put("content", message) })
        }
        parseMessagePayload(requireData(result))
    }

    suspend fun archiveConversation(conversationId: String): Result<Unit> = runCatching {
        requireData(api.archiveAiConversation(conversationId), allowEmpty = true)
        Unit
    }

    private fun requireData(result: ApiResult<JsonElement>, allowEmpty: Boolean = false): JsonElement = when (result) {
        is ApiResult.Success -> {
            val body = result.data
            if (body is JsonNull) {
                if (allowEmpty) JsonObject(emptyMap()) else error("The assistant returned an empty response")
            } else {
                body.unwrapData()
            }
        }
        is ApiResult.Error -> error(errorMessage(result))
        is ApiResult.NetworkError -> error(result.message)
    }

    private fun errorMessage(result: ApiResult.Error): String {
        val fallback = when (result.httpCode) {
            401 -> "Your session has expired. Please sign in again."
            403 -> "Your subscription does not include Osebo AI."
            404 -> "The conversation is no longer available."
            429 -> "Osebo AI is busy. Please wait a moment and retry."
            in 500..599 -> "Osebo AI is temporarily unavailable."
            else -> "Unable to reach Osebo AI (${result.httpCode})"
        }
        val raw = result.message
        if (raw.isBlank()) return fallback
        return runCatching {
            Json.parseToJsonElement(raw).asObjectOrNull()?.string("message", "error", "detail")
        }.getOrNull().orEmpty().ifBlank { fallback }
    }
}

private fun JsonElement.unwrapData(): JsonElement {
    val objectValue = asObjectOrNull() ?: return this
    return objectValue["data"]?.takeUnless { it is JsonNull }
        ?: objectValue["result"]?.takeUnless { it is JsonNull }
        ?: this
}

private fun conversationArray(data: JsonElement): JsonArray {
    if (data is JsonArray) return data
    val objectValue = data.asObjectOrNull() ?: return JsonArray(emptyList())
    return objectValue.array("conversations", "items", "results") ?: JsonArray(emptyList())
}

private fun conversationObject(data: JsonElement): JsonObject? =
    data.asObjectOrNull()?.obj("conversation", "item", "result")

private fun parseConversation(element: JsonElement?): AiConversation? {
    val value = element?.asObjectOrNull() ?: return null
    val id = value.string("id", "conversationId", "conversation_id", "uuid") ?: return null
    val messages = value.array("messages", "items")
        ?.mapIndexedNotNull { index, message -> parseMessage(message, index = index) }
        .orEmpty()
    val fallbackTitle = messages.firstOrNull()?.text
        ?.take(48)
        ?.takeIf { it.isNotBlank() }
        ?: "New conversation"
    return AiConversation(
        id = id,
        title = value.string("title", "name", "subject")?.takeIf { it.isNotBlank() } ?: fallbackTitle,
        updatedAt = value.string("updatedAt", "updated_at", "createdAt", "created_at").orEmpty(),
        messages = messages,
    )
}

private fun parseMessagePayload(data: JsonElement): List<AiChatMessage> {
    if (data is JsonArray) {
        return data.mapIndexedNotNull { index, item -> parseMessage(item, index = index) }
    }
    if (data is JsonPrimitive && data.isString) {
        return listOf(assistantMessage(data.content))
    }
    val objectValue = data.asObjectOrNull() ?: return emptyList()
    objectValue.array("messages", "items", "results")?.let { messages ->
        return messages.mapIndexedNotNull { index, item -> parseMessage(item, index = index) }
    }

    val result = mutableListOf<AiChatMessage>()
    objectValue["userMessage"]?.let { parseMessage(it, true)?.let(result::add) }
    objectValue["user_message"]?.let { parseMessage(it, true)?.let(result::add) }
    objectValue["assistantMessage"]?.let { parseMessage(it, false)?.let(result::add) }
    objectValue["assistant_message"]?.let { parseMessage(it, false)?.let(result::add) }
    objectValue["reply"]?.let { parseMessage(it, false)?.let(result::add) }
    objectValue["response"]?.let { parseMessage(it, false)?.let(result::add) }
    objectValue["answer"]?.let { parseMessage(it, false)?.let(result::add) }
    if (result.isNotEmpty()) return result

    parseMessage(objectValue)?.let(result::add)
    return result
}

private fun parseMessage(
    element: JsonElement,
    defaultIsFromUser: Boolean? = null,
    index: Int = 0,
): AiChatMessage? {
    if (element is JsonPrimitive && element.isString) {
        val content = element.content.trim()
        if (content.isBlank()) return null
        return AiChatMessage(
            id = "message-$index-${content.hashCode()}",
            isFromUser = defaultIsFromUser ?: false,
            text = content,
        )
    }
    val value = element.asObjectOrNull() ?: return null
    val content = value.string("content", "message", "text", "response", "answer")
        ?.trim()
        ?.takeIf { it.isNotBlank() }
        ?: return null
    val roleValue = value.string("role", "sender", "author", "type")?.lowercase()
    val isFromUser = defaultIsFromUser ?: (
        roleValue == "user" || roleValue == "human" || roleValue == "customer"
    )
    return AiChatMessage(
        id = value.string("id", "messageId", "message_id", "uuid")
            ?: "message-$index-${content.hashCode()}",
        isFromUser = isFromUser,
        text = content,
    )
}

private fun assistantMessage(content: String) = AiChatMessage(
    id = "assistant-${content.hashCode()}",
    isFromUser = false,
    text = content,
)

private fun JsonElement.asObjectOrNull(): JsonObject? = this as? JsonObject

private fun JsonObject.string(vararg keys: String): String? = keys.firstNotNullOfOrNull { key ->
    (this[key] as? JsonPrimitive)?.takeIf { it.isString }?.content
}

private fun JsonObject.boolean(vararg keys: String): Boolean? = keys.firstNotNullOfOrNull { key ->
    (this[key] as? JsonPrimitive)?.booleanOrNull
}

private fun JsonObject.array(vararg keys: String): JsonArray? = keys.firstNotNullOfOrNull { key ->
    this[key] as? JsonArray
}

private fun JsonObject.obj(vararg keys: String): JsonObject? = keys.firstNotNullOfOrNull { key ->
    this[key] as? JsonObject
}
