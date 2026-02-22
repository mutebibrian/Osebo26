package com.devbrian.osebo.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devbrian.osebo.data.ApiService
import com.devbrian.osebo.data.PreferenceManager
import com.devbrian.osebo.data.remote.dto.request.LoginRequest
import com.devbrian.osebo.data.remote.dto.response.AuthResponse
import com.devbrian.osebo.data.remote.dto.response.AuthData
import com.devbrian.osebo.data.remote.dto.response.UserData
import com.devbrian.osebo.data.remote.dto.response.RoleData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val apiService: ApiService,
    private val preferences: PreferenceManager
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    fun login(username: String, password: String) {
        viewModelScope.launch {
            println("🔐 LOGIN: Starting login with username: $username")
            _loginState.value = LoginState.Loading

            try {
                val request = LoginRequest(
                    username = username.trim(),
                    password = password
                )

                println("📤 LOGIN: Sending request to /api/auth/signin")
                val response = apiService.signIn(request)

                println("📥 LOGIN: Response code: ${response.code()}")

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    println("📦 LOGIN: Response body: $apiResponse")

                    if (apiResponse?.success == true) {
                        apiResponse.data?.let { authData ->
                            println("✅ LOGIN: AuthData received")
                            println("🔑 LOGIN: Token: ${authData.accessToken.take(20)}...")
                            println("👤 LOGIN: User ID: ${authData.user.id}")
                            println("👤 LOGIN: User Name: ${authData.user.firstName} ${authData.user.lastName}")
                            println("👤 LOGIN: User Email: ${authData.user.email}")
                            println("👤 LOGIN: User Role: ${authData.user.role?.name}")
                            println("👤 LOGIN: User Verified: ${authData.user.isVerified}")
                            println("👤 LOGIN: User Active: ${authData.user.isActive}")

                            // Save user data to PreferenceManager
                            saveUserData(authData)

                            // After successful login, we might need to fetch user's shops
                            // For now, we'll assume user might need to create/select a shop

                            // Create domain user for UI (without shopId)
                            val domainUser = DomainUser(
                                id = authData.user.id,
                                email = authData.user.email,
                                name = "${authData.user.firstName} ${authData.user.lastName}".trim(),
                                phone = authData.user.phone ?: "",
                                role = authData.user.role?.name ?: "owner"
                            )

                            _loginState.value = LoginState.Success(
                                DomainAuthData(
                                    token = authData.accessToken,
                                    user = domainUser
                                )
                            )
                            println("🎉 LOGIN: Success state set")
                        } ?: run {
                            println("❌ LOGIN: No data in response")
                            _loginState.value = LoginState.Error("No data received from server")
                        }
                    } else {
                        println("❌ LOGIN: API success is false")
                        _loginState.value = LoginState.Error(
                            apiResponse?.message ?: "Login failed"
                        )
                    }
                } else {
                    println("❌ LOGIN: HTTP error: ${response.code()}")
                    val errorMessage = when (response.code()) {
                        400 -> "Invalid username or password"
                        401 -> "Invalid credentials"
                        403 -> "Account is locked or disabled"
                        404 -> "User not found"
                        422 -> "Invalid input data"
                        500 -> "Server error. Please try again later"
                        else -> "Login failed: ${response.message()}"
                    }
                    _loginState.value = LoginState.Error(errorMessage)
                }
            } catch (e: HttpException) {
                println("❌ LOGIN: HttpException: ${e.message}")
                _loginState.value = LoginState.Error("Network error. Please try again.")
            } catch (e: IOException) {
                println("❌ LOGIN: IOException: ${e.message}")
                _loginState.value = LoginState.Error(
                    "Network error. Please check your internet connection."
                )
            } catch (e: Exception) {
                println("❌ LOGIN: Exception: ${e.message}")
                e.printStackTrace()
                _loginState.value = LoginState.Error(
                    e.message ?: "An unexpected error occurred"
                )
            }
        }
    }

    private fun saveUserData(authData: AuthData) {
        println("💾 LOGIN: Saving user data to PreferenceManager")

        val user = authData.user
        val fullName = "${user.firstName} ${user.lastName}".trim()

        // Save authentication token
        preferences.saveAuthToken(authData.accessToken)
        println("💾 LOGIN: Token saved: ${authData.accessToken.take(20)}...")

        // Save user information
        preferences.saveUserId(user.id)
        preferences.saveUserEmail(user.email)
        preferences.saveUserName(fullName)

        // Mark user as logged in
        preferences.setUserLoggedIn(true)

        // Save phone if available
        user.phone?.let {
            preferences.saveUserPhone(it)
            println("💾 LOGIN: Phone saved: $it")
        }

        // Save user role if available
        user.role?.name?.let {
            preferences.saveUserRole(it)
            println("💾 LOGIN: Role saved: $it")
        }

        // Save user verification status
        preferences.saveUserVerified(user.isVerified)
        println("💾 LOGIN: User verified: ${user.isVerified}")

        // Save last login timestamp
        preferences.setLastLoginTimestamp(System.currentTimeMillis())

        println("💾 LOGIN: User data saved successfully")
    }

    fun validateUsername(username: String): Boolean {
        return username.isNotEmpty()
    }

    fun validatePassword(password: String): Boolean {
        return password.isNotEmpty() && password.length >= 6
    }

    fun resetState() {
        _loginState.value = LoginState.Idle
    }

    fun isUserLoggedIn(): Boolean {
        return preferences.isLoggedIn()
    }

    fun isUserVerified(): Boolean {
        return preferences.getUserVerified()
    }

    fun clearSavedCredentials() {
        preferences.saveEmail("")
        preferences.savePhone("")
        preferences.setRememberMeEnabled(false)
    }

    // Login State
    sealed class LoginState {
        object Idle : LoginState()
        object Loading : LoginState()
        data class Success(val authData: DomainAuthData) : LoginState()
        data class Error(val message: String) : LoginState()
    }

    // Domain Models for UI (without shopId)
    data class DomainAuthData(
        val token: String,
        val user: DomainUser
    )

    data class DomainUser(
        val id: String,
        val email: String,
        val name: String,
        val phone: String,
        val role: String
    )
}