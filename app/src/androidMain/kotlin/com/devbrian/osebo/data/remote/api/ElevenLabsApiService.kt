package com.devbrian.osebo.data.remote.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

data class ElevenLabsSttResponse(
    val text: String
)

interface ElevenLabsApiService {

    @Multipart
    @POST("v1/speech-to-text")
    suspend fun transcribe(
        @Part file: MultipartBody.Part,
        @Part("model_id") modelId: RequestBody
    ): Response<ElevenLabsSttResponse>
}
