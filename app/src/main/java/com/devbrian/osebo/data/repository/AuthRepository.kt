package com.devbrian.osebo.data.repository

import com.devbrian.osebo.data.remote.dto.request.SignUpRequest
import com.devbrian.osebo.data.remote.dto.response.SignUpResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okio.IOException
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

class AuthRepository {


    private interface LocalApiService {
        // FIXED: Added /api/ prefix to all endpoints
        @POST("api/auth/signup")
        suspend fun signUp(@Body request: SignUpRequest): Response<SignUpResponse>

        @POST("api/auth/signin")
        suspend fun login(@Body request: Map<String, String>): Response<SignUpResponse>

        @POST("api/auth/verify-otp")
        suspend fun verifyOtp(@Body request: Map<String, String>): Response<SignUpResponse>

        @POST("api/auth/resend-otp")
        suspend fun resendOtp(@Body request: Map<String, String>): Response<SignUpResponse>


        @POST("api/auth/generate-otp")
        suspend fun generateOtp(@Body request: Map<String, String>): Response<SignUpResponse>
    }


    private val apiService: LocalApiService by lazy {
        // FIXED: Added trailing slash to base URL
        val BASE_URL = "https://dev-api.osebo.ai/"

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Accept", "application/json")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("X-App-Platform", "Android")
                    .build()
                chain.proceed(request)
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)  // Now ends with /
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LocalApiService::class.java)
    }

    // Rest of your code remains exactly the same...
    suspend fun signUp(signUpRequest: SignUpRequest): Result<SignUpResponse> {
        return try {
            val response: Response<SignUpResponse> = apiService.signUp(signUpRequest)

            if (response.isSuccessful) {
                response.body()?.let { body ->
                    Result.success(body)
                } ?: Result.failure(Exception("Empty response from server"))
            } else {
                val errorMessage = parseErrorMessage(response)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Network error: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(Exception("Sign up failed: ${e.message}"))
        }
    }

    suspend fun verifyOtp(userId: String, otp: String): Result<SignUpResponse> {
        return try {
            val request = mapOf(
                "userId" to userId,
                "otp" to otp
            )

            println("DEBUG: Calling verify-otp with userId: $userId, otp: $otp")

            val response: Response<SignUpResponse> = apiService.verifyOtp(request)

            println("DEBUG: Verify OTP response code: ${response.code()}")
            println("DEBUG: Verify OTP response body: ${response.body()}")
            println("DEBUG: Verify OTP error body: ${response.errorBody()?.string()}")

            if (response.isSuccessful) {
                response.body()?.let { body ->
                    println("DEBUG: OTP verification successful: $body")
                    Result.success(body)
                } ?: Result.failure(Exception("Empty response from server"))
            } else {
                val errorMessage = parseErrorMessage(response)
                println("DEBUG: OTP verification failed: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: IOException) {
            println("DEBUG: OTP verification network error: ${e.message}")
            Result.failure(Exception("Network error: ${e.message}"))
        } catch (e: Exception) {
            println("DEBUG: OTP verification exception: ${e.message}")
            Result.failure(Exception("OTP verification failed: ${e.message}"))
        }
    }

    suspend fun resendOtp(userId: String): Result<SignUpResponse> {
        return try {
            val request = mapOf("userId" to userId)

            println("DEBUG: Calling resend-otp with userId: $userId")

            val response: Response<SignUpResponse> = apiService.resendOtp(request)

            println("DEBUG: Resend OTP response code: ${response.code()}")

            if (response.isSuccessful) {
                response.body()?.let { body ->
                    println("DEBUG: OTP resent successfully")
                    Result.success(body)
                } ?: Result.failure(Exception("Empty response from server"))
            } else {
                val errorMessage = parseErrorMessage(response)
                println("DEBUG: Resend OTP failed: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: IOException) {
            println("DEBUG: Resend OTP network error: ${e.message}")
            Result.failure(Exception("Network error: ${e.message}"))
        } catch (e: Exception) {
            println("DEBUG: Resend OTP exception: ${e.message}")
            Result.failure(Exception("Failed to resend OTP: ${e.message}"))
        }
    }


    private fun parseErrorMessage(response: Response<SignUpResponse>): String {
        return try {
            val errorBody = response.errorBody()?.string()
            if (!errorBody.isNullOrEmpty()) {
                println("DEBUG: Error body raw: $errorBody")


                if (errorBody.contains("\"message\"")) {
                    val messagePattern = "\"message\"\\s*:\\s*\"([^\"]+)\"".toRegex()
                    val match = messagePattern.find(errorBody)
                    val extractedMessage = match?.groupValues?.get(1) ?: errorBody
                    println("DEBUG: Extracted message: $extractedMessage")
                    extractedMessage
                } else if (errorBody.contains("message")) {

                    val altPattern = "message\\s*[=:]\\s*\"?([^\",}]+)\"?".toRegex()
                    val altMatch = altPattern.find(errorBody)
                    altMatch?.groupValues?.get(1)?.trim() ?: errorBody
                } else {
                    errorBody
                }
            } else {
                response.message().takeIf { it.isNotEmpty() } ?: "Error ${response.code()}"
            }
        } catch (e: Exception) {
            println("DEBUG: Error parsing error message: ${e.message}")
            response.message().takeIf { it.isNotEmpty() } ?: "Error ${response.code()}"
        }
    }


    suspend fun login(email: String, password: String): Result<SignUpResponse> {
        return try {
            val loginRequest = mapOf(
                "email" to email,
                "password" to password
            )

            val response: Response<SignUpResponse> = apiService.login(loginRequest)

            if (response.isSuccessful) {
                response.body()?.let { body ->
                    Result.success(body)
                } ?: Result.failure(Exception("Empty response from server"))
            } else {
                val errorMessage = parseErrorMessage(response)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Network error: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(Exception("Login failed: ${e.message}"))
        }
    }
}