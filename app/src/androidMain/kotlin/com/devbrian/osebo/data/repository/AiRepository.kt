package com.devbrian.osebo.data.repository

import com.devbrian.osebo.data.PreferenceManager
import com.devbrian.osebo.models.AiChatMessage
import com.devbrian.osebo.utils.Resource
import kotlinx.coroutines.delay

// TODO: no backend AI endpoint exists yet. Once one is confirmed, add its Retrofit method to
// ApiService (e.g. POST api/v1/shops/{shopId}/ai/chat, sending the shop id, the message, and
// probably the running conversation) and replace the placeholder below with the real call.
class AiRepository(
    private val preferenceManager: PreferenceManager
) {
    suspend fun sendMessage(message: String): Resource<AiChatMessage> {
        val shopId = preferenceManager.getCurrentShopId()
        if (shopId.isEmpty()) {
            return Resource.Error("No shop selected")
        }

        delay(700)

        return Resource.Success(
            AiChatMessage(
                text = "I'm not connected to the AI service yet. Once the backend endpoint is ready, I'll be able to answer real questions about this shop.",
                isFromUser = false
            )
        )
    }
}
