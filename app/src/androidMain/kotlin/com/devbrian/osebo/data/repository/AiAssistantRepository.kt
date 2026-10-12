package com.devbrian.osebo.data.repository

import com.devbrian.osebo.data.ApiService
import com.devbrian.osebo.models.AiAssistantReadiness
import com.devbrian.osebo.models.AiChatMessage
import com.devbrian.osebo.models.AiConversation
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import retrofit2.Response
import java.io.IOException

class AiAssistantRepository(
    private val apiService: ApiService,
) {
    suspend fun getReadiness(): Result<AiAssistantReadiness> = runApiCall {
        val data = requireData(apiService.getAiAssistantReadiness())
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

    suspend fun getConversations(): Result<List<AiConversation>> = runApiCall {
        val data = requireData(apiService.getAiConversations())
        conversationArray(data).mapNotNull(::parseConversation)
    }

    suspend fun createConversation(title: String): Result<AiConversation> = runApiCall {
        var response = apiService.createAiConversation(mapOf("title" to title))
        if (response.code() == 400 || response.code() == 422) {
            response = apiService.createAiConversation(emptyMap())
        }
        val data = requireData(response)
        parseConversation(conversationObject(data) ?: data.asObjectOrNull())
            ?: throw AiApiException("The assistant did not return a conversation")
    }

    suspend fun getConversation(conversationId: String): Result<AiConversation> = runApiCall {
        val data = requireData(apiService.getAiConversation(conversationId))
        parseConversation(conversationObject(data) ?: data.asObjectOrNull())
            ?: throw AiApiException("The conversation could not be loaded")
    }

    suspend fun sendMessage(
        conversationId: String,
        message: String,
    ): Result<List<AiChatMessage>> = runApiCall {
        var response = apiService.sendAiMessage(conversationId, mapOf("message" to message))
        if (response.code() == 400 || response.code() == 422) {
            response = apiService.sendAiMessage(conversationId, mapOf("content" to message))
        }
        val data = requireData(response)
        parseMessagePayload(data)
    }

    suspend fun archiveConversation(conversationId: String): Result<Unit> = runApiCall {
        requireData(apiService.archiveAiConversation(conversationId), allowEmpty = true)
        Unit
    }

    private inline fun <T> runApiCall(block: () -> T): Result<T> = try {
        Result.success(block())
    } catch (error: AiApiException) {
        Result.failure(error)
    } catch (error: IOException) {
        Result.failure(AiApiException("Check your internet connection and try again", error))
    } catch (error: Exception) {
        Result.failure(AiApiException(error.message ?: "The assistant is unavailable", error))
    }

    private fun requireData(
        response: Response<JsonElement>,
        allowEmpty: Boolean = false,
    ): JsonElement {
        if (!response.isSuccessful) {
            throw AiApiException(errorMessage(response))
        }
        val body = response.body()
        if (body == null || body.isJsonNull) {
            if (allowEmpty) return JsonObject()
            throw AiApiException("The assistant returned an empty response")
        }
        return body.unwrapData()
    }

    private fun errorMessage(response: Response<JsonElement>): String {
        val fallback = when (response.code()) {
            401 -> "Your session has expired. Please sign in again."
            403 -> "Your subscription does not include Osebo AI."
            404 -> "The conversation is no longer available."
            429 -> "Osebo AI is busy. Please wait a moment and retry."
            in 500..599 -> "Osebo AI is temporarily unavailable."
            else -> "Unable to reach Osebo AI (${response.code()})"
        }
        val raw = response.errorBody()?.string().orEmpty()
        if (raw.isBlank()) return fallback
        return runCatching {
            val json = JsonParser.parseString(raw).asObjectOrNull()
            json?.string("message", "error", "detail")
        }.getOrNull().orEmpty().ifBlank { fallback }
    }

    private fun JsonElement.unwrapData(): JsonElement {
        val objectValue = asObjectOrNull() ?: return this
        return objectValue.get("data")?.takeUnless(JsonElement::isJsonNull)
            ?: objectValue.get("result")?.takeUnless(JsonElement::isJsonNull)
            ?: this
    }

    private fun conversationArray(data: JsonElement): JsonArray {
        if (data.isJsonArray) return data.asJsonArray
        val objectValue = data.asObjectOrNull() ?: return JsonArray()
        return objectValue.array("conversations", "items", "results") ?: JsonArray()
    }

    private fun conversationObject(data: JsonElement): JsonObject? {
        val objectValue = data.asObjectOrNull() ?: return null
        return objectValue.obj("conversation", "item", "result")
    }

    private fun parseConversation(element: JsonElement?): AiConversation? {
        val value = element?.asObjectOrNull() ?: return null
        val id = value.string("id", "conversationId", "conversation_id", "uuid") ?: return null
        val messages = value.array("messages", "items")
            ?.mapIndexedNotNull { index, message -> parseMessage(message, index = index) }
            .orEmpty()
        val fallbackTitle = messages.firstOrNull()?.text
            ?.take(48)
            ?.takeIf(String::isNotBlank)
            ?: "New conversation"
        return AiConversation(
            id = id,
            title = value.string("title", "name", "subject")?.takeIf(String::isNotBlank)
                ?: fallbackTitle,
            updatedAt = value.string("updatedAt", "updated_at", "createdAt", "created_at").orEmpty(),
            messages = messages,
        )
    }

    private fun parseMessagePayload(data: JsonElement): List<AiChatMessage> {
        if (data.isJsonArray) {
            return data.asJsonArray.mapIndexedNotNull { index, item -> parseMessage(item, index = index) }
        }
        if (data.isJsonPrimitive && data.asJsonPrimitive.isString) {
            return listOf(assistantMessage(data.asString))
        }
        val objectValue = data.asObjectOrNull() ?: return emptyList()
        objectValue.array("messages", "items", "results")?.let { messages ->
            return messages.mapIndexedNotNull { index, item -> parseMessage(item, index = index) }
        }

        val result = mutableListOf<AiChatMessage>()
        objectValue.get("userMessage")?.let { parseMessage(it, true)?.let(result::add) }
        objectValue.get("user_message")?.let { parseMessage(it, true)?.let(result::add) }
        objectValue.get("assistantMessage")?.let { parseMessage(it, false)?.let(result::add) }
        objectValue.get("assistant_message")?.let { parseMessage(it, false)?.let(result::add) }
        objectValue.get("reply")?.let { parseMessage(it, false)?.let(result::add) }
        objectValue.get("response")?.let { parseMessage(it, false)?.let(result::add) }
        objectValue.get("answer")?.let { parseMessage(it, false)?.let(result::add) }
        if (result.isNotEmpty()) return result

        parseMessage(objectValue)?.let(result::add)
        return result
    }

    private fun parseMessage(
        element: JsonElement,
        defaultIsFromUser: Boolean? = null,
        index: Int = 0,
    ): AiChatMessage? {
        if (element.isJsonPrimitive && element.asJsonPrimitive.isString) {
            val content = element.asString.trim()
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
            ?.takeIf(String::isNotBlank)
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

    private fun JsonElement.asObjectOrNull(): JsonObject? =
        takeIf { it.isJsonObject }?.asJsonObject

    private fun JsonObject.string(vararg keys: String): String? = keys.firstNotNullOfOrNull { key ->
        get(key)?.takeIf { it.isJsonPrimitive }?.asJsonPrimitive?.takeIf { it.isString }?.asString
    }

    private fun JsonObject.boolean(vararg keys: String): Boolean? = keys.firstNotNullOfOrNull { key ->
        get(key)?.takeIf { it.isJsonPrimitive }?.asJsonPrimitive?.let { primitive ->
            runCatching { primitive.asBoolean }.getOrNull()
        }
    }

    private fun JsonObject.array(vararg keys: String): JsonArray? = keys.firstNotNullOfOrNull { key ->
        get(key)?.takeIf { it.isJsonArray }?.asJsonArray
    }

    private fun JsonObject.obj(vararg keys: String): JsonObject? = keys.firstNotNullOfOrNull { key ->
        get(key)?.takeIf { it.isJsonObject }?.asJsonObject
    }
}

private class AiApiException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)
