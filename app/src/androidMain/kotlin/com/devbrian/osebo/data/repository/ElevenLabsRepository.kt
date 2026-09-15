package com.devbrian.osebo.data.repository

import com.devbrian.osebo.BuildConfig
import com.devbrian.osebo.data.remote.api.ElevenLabsApiService
import com.devbrian.osebo.utils.Resource
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class ElevenLabsRepository(
    private val elevenLabsApiService: ElevenLabsApiService
) {
    suspend fun transcribe(audioFile: File): Resource<String> {
        if (BuildConfig.ELEVENLABS_API_KEY.isEmpty()) {
            return Resource.Error("Voice input isn't set up yet — add an ElevenLabs API key to enable it.")
        }

        return try {
            val filePart = MultipartBody.Part.createFormData(
                "file",
                audioFile.name,
                audioFile.asRequestBody("audio/mp4".toMediaTypeOrNull())
            )
            val modelIdPart = "scribe_v1".toRequestBody("text/plain".toMediaTypeOrNull())

            val response = elevenLabsApiService.transcribe(filePart, modelIdPart)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                Resource.Success(body.text)
            } else {
                Resource.Error("Couldn't transcribe audio (${response.code()})")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Couldn't transcribe audio")
        }
    }
}
