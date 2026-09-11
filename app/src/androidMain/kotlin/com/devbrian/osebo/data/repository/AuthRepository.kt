package com.devbrian.osebo.data.repository

import com.devbrian.osebo.data.remote.dto.request.SignUpRequest
import com.devbrian.osebo.data.remote.dto.request.VerifyOtpRequest
import com.devbrian.osebo.data.remote.dto.response.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okio.IOException
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

class AuthRepository {

    private interface LocalApiService {
        @POST("api/v1/auth/signup")
        suspend fun signUp(@Body request: SignUpRequest): Response<BaseResponse<SignUpResponse>>

        @POST("api/v1/auth/signin")
        suspend fun requestOtp(@Body request: Map<String, String>): Response<BaseResponse<SigninResponse>>

        @POST("api/v1/auth/verify-2fa")
        suspend fun verify2fa(@Body request: Map<String, String>): Response<BaseResponse<PreAuthData>>

        @POST("api/v1/auth/select-account")
        suspend fun selectAccount(@Body request: Map<String, String>): Response<BaseResponse<SigninData>>

        @POST("api/v1/auth/resend-otp")
        suspend fun resendOtp(@Body request: Map<String, String>): Response<BaseResponse<Unit>>

        @POST("api/v1/auth/signin/password")
        suspend fun signInWithPassword(@Body request: Map<String, String>): Response<BaseResponse<PreAuthData>>
    }

    private val apiService: LocalApiService by lazy {
        // BASE_URL stays unversioned — "v1" is now part of each individual path (api/v1/...)
        val BASE_URL = "https://prod-api.osebo.ai/"

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
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LocalApiService::class.java)
    }

    // ---------- OTP FLOW ----------
    suspend fun requestOtp(identifier: String): Result<SigninResponse> {
        return try {
            val response = apiService.requestOtp(mapOf("username" to identifier))
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                if (data != null && data.userId.isNotEmpty()) {
                    Result.success(data)
                } else {
                    Result.failure(Exception("Invalid response: missing user ID"))
                }
            } else {
                Result.failure(Exception(extractErrorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Request failed: ${e.message}"))
        }
    }

    suspend fun verify2fa(userId: String, otp: String): Result<PreAuthData> {
        return try {
            val response = apiService.verify2fa(mapOf("userId" to userId, "otp" to otp))
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                if (data != null && data.preAuthToken.isNotEmpty() && data.accounts.isNotEmpty()) {
                    Result.success(data)
                } else {
                    Result.failure(Exception("Invalid response: missing preAuthToken or accounts"))
                }
            } else {
                val errorMsg = extractErrorMessage(response)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Verification error: ${e.message}"))
        }
    }

    suspend fun selectAccount(preAuthToken: String, accountId: String): Result<SigninData> {
        return try {
            val response = apiService.selectAccount(mapOf("preAuthToken" to preAuthToken, "accountId" to accountId))
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                if (data != null && data.accessToken.isNotEmpty()) {
                    Result.success(data)
                } else {
                    Result.failure(Exception("Invalid response: missing tokens"))
                }
            } else {
                Result.failure(Exception(extractErrorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Account selection error: ${e.message}"))
        }
    }

    suspend fun resendOtp(userId: String): Result<Unit> {
        return try {
            val response = apiService.resendOtp(mapOf("userId" to userId))
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(extractErrorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Resend failed: ${e.message}"))
        }
    }

    suspend fun loginWithPassword(email: String, password: String): Result<PreAuthData> {
        return try {
            val response = apiService.signInWithPassword(mapOf("username" to email, "password" to password))
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                if (data != null && data.preAuthToken.isNotEmpty() && data.accounts.isNotEmpty()) {
                    Result.success(data)
                } else {
                    Result.failure(Exception("Invalid response: missing preAuthToken or accounts"))
                }
            } else {
                Result.failure(Exception(extractErrorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Login error: ${e.message}"))
        }
    }

    suspend fun signUp(request: SignUpRequest): Result<SignUpResponse> {
        return try {
            val response = apiService.signUp(request)
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let { Result.success(it) }
                    ?: Result.failure(Exception("Empty response"))
            } else {
                Result.failure(Exception(extractErrorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Signup error: ${e.message}"))
        }
    }

    suspend fun verifyOtp(request: VerifyOtpRequest): Result<Unit> {
        return try {
            val response = apiService.verify2fa(mapOf("userId" to request.userId, "otp" to request.otp))
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(extractErrorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Verification error: ${e.message}"))
        }
    }

    private fun extractErrorMessage(response: Response<*>): String {
        return try {
            val errorBody = response.errorBody()?.string()
            if (!errorBody.isNullOrEmpty()) {
                val messagePattern = "\"message\"\\s*:\\s*\"([^\"]+)\"".toRegex()
                val match = messagePattern.find(errorBody)
                match?.groupValues?.get(1) ?: errorBody
            } else {
                response.message().takeIf { it.isNotEmpty() } ?: "Error ${response.code()}"
            }
        } catch (e: Exception) {
            response.message().takeIf { it.isNotEmpty() } ?: "Error ${response.code()}"
        }
    }
}