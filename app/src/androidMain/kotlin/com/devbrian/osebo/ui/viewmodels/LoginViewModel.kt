package com.devbrian.osebo.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devbrian.osebo.data.ApiService
import com.devbrian.osebo.data.PreferenceManager
import com.devbrian.osebo.data.remote.dto.request.LoginRequest
import com.devbrian.osebo.data.remote.dto.request.ResendOtpRequest
import com.devbrian.osebo.data.remote.dto.request.SelectAccountRequest
import com.devbrian.osebo.data.remote.dto.request.VerifyTwoFactorRequest
import com.devbrian.osebo.data.remote.dto.response.AccountInfo
import com.devbrian.osebo.data.remote.dto.response.AuthData
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

    private val _verificationState = MutableStateFlow<VerificationState>(VerificationState.Idle)
    val verificationState: StateFlow<VerificationState> = _verificationState.asStateFlow()

    private var currentPreAuthToken: String = ""
    private var availableAccounts: List<AccountInfo> = emptyList()

    fun login(username: String, password: String) {
        viewModelScope.launch {
            println("🔐 LOGIN: Starting login with username: $username")
            _loginState.value = LoginState.Loading

            try {
                val request = LoginRequest(
                    username = username.trim(),
                    password = password
                )

                val response = apiService.signIn(request)
                println("📥 LOGIN: Response code: ${response.code()}")

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    println("📥 LOGIN: Response body: $apiResponse")

                    if (apiResponse?.success == true) {
                        val data = apiResponse.data
                        println("📥 LOGIN: Data: $data")

                        if (data?.accessToken != null && data.accessToken.isNotEmpty()) {
                            println("✅ LOGIN: Direct login success with access token")

                            val domainAuthData = DomainAuthData(
                                token = data.accessToken,
                                user = DomainUser(
                                    id = data.user?.id ?: "",
                                    email = data.user?.email,
                                    name = "${data.user?.firstName ?: ""} ${data.user?.lastName ?: ""}".trim(),
                                    phone = data.user?.phone ?: "",
                                    role = data.user?.role ?: "owner",
                                    isEmailVerified = data.user?.isVerified ?: true
                                )
                            )

                            saveTokensAndUserData(data)
                            _loginState.value = LoginState.Success(domainAuthData)

                        }
                        else if (data?.userId != null && data.userId.isNotEmpty()) {
                            println("⚠️ LOGIN: 2FA verification required for userId: ${data.userId}")

                            preferences.saveTempCredentials(username, password)

                            val partialAuthData = DomainAuthData(
                                token = "",
                                user = DomainUser(
                                    id = data.userId,
                                    email = null,
                                    name = "",
                                    phone = "",
                                    role = "employee",
                                    isEmailVerified = false
                                )
                            )

                            _loginState.value = LoginState.VerificationRequired(
                                authData = partialAuthData,
                                userId = data.userId
                            )
                        }
                        else {
                            println("❌ LOGIN: No token and no userId in response")
                            println("❌ LOGIN: Response data keys: ${data?.let { it::class.java.declaredFields.map { field -> field.name } }}")
                            _loginState.value = LoginState.Error("Invalid response from server")
                        }
                    } else {
                        val errorMsg = apiResponse?.message ?: "Login failed"
                        println("❌ LOGIN: API returned error: $errorMsg")
                        _loginState.value = LoginState.Error(errorMsg)
                    }
                } else {
                    handleHttpError(response.code())
                }
            } catch (e: Exception) {
                println("❌ LOGIN: Exception: ${e.message}")
                e.printStackTrace()
                _loginState.value = LoginState.Error(e.message ?: "An error occurred")
            }
        }
    }

    private fun saveTokensAndUserData(authData: AuthData) {
        val user = authData.user ?: return

        preferences.saveAuthToken(authData.accessToken ?: "")
        preferences.saveRefreshToken(authData.refreshToken ?: "")
        preferences.saveUserId(user.id ?: "")
        preferences.saveUserEmail(user.email ?: "")
        preferences.saveUserName("${user.firstName ?: ""} ${user.lastName ?: ""}".trim())
        preferences.saveUserPhone(user.phone ?: "")
        preferences.saveUserRole(user.role ?: "owner")  // ✅ role is String?
        preferences.saveUserVerified(user.isVerified ?: true)
        preferences.setUserLoggedIn(true)
        preferences.setLastLoginTimestamp(System.currentTimeMillis())

        println("💾 Tokens and user data saved successfully")
        println("   User: ${user.firstName} ${user.lastName}")
        println("   Role: ${user.role}")
    }

    suspend fun verifyTwoFactor(userId: String, otp: String): Boolean {
        return try {
            println("🔐 VERIFY: ===== STARTING OTP VERIFICATION =====")
            println("🔐 VERIFY: userId: $userId")
            println("🔐 VERIFY: otp: $otp")

            _verificationState.value = VerificationState.Loading

            val request = VerifyTwoFactorRequest(userId = userId, otp = otp)
            println("🔐 VERIFY: Request body: ${request.toString()}")

            val response = apiService.verifyTwoFactor(request)
            println("🔐 VERIFY: Response code: ${response.code()}")

            response.headers().names().forEach { headerName ->
                println("🔐 VERIFY: Header - $headerName: ${response.headers().get(headerName)}")
            }

            if (response.isSuccessful) {
                val apiResponse = response.body()
                println("🔐 VERIFY: Response body: $apiResponse")

                if (apiResponse?.success == true) {
                    val data = apiResponse.data
                    println("🔐 VERIFY: Response data: $data")

                    if (data?.preAuthToken != null && data.accounts != null) {
                        println("✅ VERIFY: Got preAuthToken and ${data.accounts.size} accounts")
                        println("✅ VERIFY: preAuthToken length: ${data.preAuthToken.length}")
                        println("✅ VERIFY: preAuthToken preview: ${data.preAuthToken.take(20)}...")

                        data.accounts.forEachIndexed { index, account ->
                            println("✅ VERIFY: Account $index - ID: ${account.accountId}, Name: ${account.ownerFirstName} ${account.ownerLastName}, isOwner: ${account.isOwner}")
                        }

                        currentPreAuthToken = data.preAuthToken
                        availableAccounts = data.accounts

                        if (data.accounts.size == 1) {
                            val account = data.accounts.first()
                            println("✅ VERIFY: Single account detected, auto-selecting: ${account.accountId}")
                            return selectAccount(account.accountId)
                        } else {
                            println("✅ VERIFY: Multiple accounts detected, showing account picker")
                            _verificationState.value = VerificationState.AccountsReceived(data.accounts)
                            true
                        }
                    } else {
                        println("❌ VERIFY: Invalid response - missing preAuthToken or accounts")
                        println("❌ VERIFY: preAuthToken present: ${data?.preAuthToken != null}")
                        println("❌ VERIFY: accounts present: ${data?.accounts != null}")
                        _verificationState.value = VerificationState.Error("Invalid response from server")
                        false
                    }
                } else {
                    val errorMsg = apiResponse?.message ?: "Invalid OTP code"
                    println("❌ VERIFY: API returned error: $errorMsg")
                    _verificationState.value = VerificationState.Error(errorMsg)
                    false
                }
            } else {
                val errorBody = response.errorBody()?.string()
                println("❌ VERIFY: HTTP error ${response.code()}: $errorBody")
                handleVerificationError(response)
                false
            }
        } catch (e: HttpException) {
            println("❌ VERIFY: HttpException: ${e.message}")
            _verificationState.value = VerificationState.Error("Network error. Please try again.")
            false
        } catch (e: IOException) {
            println("❌ VERIFY: IOException: ${e.message}")
            _verificationState.value = VerificationState.Error("Network error. Check your connection.")
            false
        } catch (e: Exception) {
            println("❌ VERIFY: Exception: ${e.message}")
            e.printStackTrace()
            _verificationState.value = VerificationState.Error(e.message ?: "Verification failed")
            false
        }
    }

    suspend fun selectAccount(accountId: String): Boolean {
        return try {
            println("🔐 SELECT ACCOUNT: Selecting account: $accountId")

            val request = SelectAccountRequest(
                preAuthToken = currentPreAuthToken,
                accountId = accountId
            )

            val response = apiService.selectAccount(request)

            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true) {
                    val authData = apiResponse.data

                    if (authData?.accessToken != null && authData.refreshToken != null) {
                        println("✅ SELECT ACCOUNT: Tokens received successfully")

                        saveTokensAndUserData(authData)

                        _verificationState.value = VerificationState.Success("Login successful")
                        _loginState.value = LoginState.Success(createDomainAuthData(authData))

                        currentPreAuthToken = ""
                        availableAccounts = emptyList()
                        preferences.clearTempCredentials()

                        true
                    } else {
                        _verificationState.value = VerificationState.Error("No tokens received from server")
                        false
                    }
                } else {
                    val errorMsg = apiResponse?.message ?: "Account selection failed"
                    _verificationState.value = VerificationState.Error(errorMsg)
                    false
                }
            } else {
                val errorMsg = when (response.code()) {
                    401 -> "Pre-auth token expired. Please login again."
                    400 -> "Pre-auth token already used. Please restart login."
                    403 -> "You don't have access to this account."
                    else -> "Account selection failed. Please try again."
                }
                _verificationState.value = VerificationState.Error(errorMsg)
                false
            }
        } catch (e: HttpException) {
            println("❌ SELECT ACCOUNT: HttpException: ${e.message}")
            _verificationState.value = VerificationState.Error("Network error. Please try again.")
            false
        } catch (e: IOException) {
            println("❌ SELECT ACCOUNT: IOException: ${e.message}")
            _verificationState.value = VerificationState.Error("Network error. Check your connection.")
            false
        } catch (e: Exception) {
            println("❌ SELECT ACCOUNT: Exception: ${e.message}")
            e.printStackTrace()
            _verificationState.value = VerificationState.Error(e.message ?: "Failed to select account")
            false
        }
    }

    suspend fun resendOtp(userId: String): Boolean {
        return try {
            println("🔐 RESEND: Requesting new OTP for userId: $userId")
            _verificationState.value = VerificationState.Loading

            val request = ResendOtpRequest(userId = userId)
            val response = apiService.resendOtp(request)
            println("📥 RESEND: Response code: ${response.code()}")

            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true) {
                    println("✅ RESEND: New OTP sent")
                    _verificationState.value = VerificationState.Success("Verification code sent to your email")
                    true
                } else {
                    val errorMsg = apiResponse?.message ?: "Failed to resend code"
                    println("❌ RESEND: Failed - $errorMsg")
                    _verificationState.value = VerificationState.Error(errorMsg)
                    false
                }
            } else {
                val errorMsg = when (response.code()) {
                    404 -> "User not found"
                    429 -> "Too many attempts. Please wait before requesting another code"
                    else -> "Failed to resend verification code"
                }
                println("❌ RESEND: HTTP error ${response.code()}: $errorMsg")
                _verificationState.value = VerificationState.Error(errorMsg)
                false
            }
        } catch (e: HttpException) {
            println("❌ RESEND: HttpException: ${e.message}")
            _verificationState.value = VerificationState.Error("Network error. Please try again.")
            false
        } catch (e: IOException) {
            println("❌ RESEND: IOException: ${e.message}")
            _verificationState.value = VerificationState.Error("Network error. Check your connection.")
            false
        } catch (e: Exception) {
            println("❌ RESEND: Exception: ${e.message}")
            e.printStackTrace()
            _verificationState.value = VerificationState.Error(e.message ?: "Failed to resend code")
            false
        }
    }

    suspend fun refreshToken(): Boolean {
        return try {
            val refreshToken = preferences.getRefreshToken()
            if (refreshToken.isEmpty()) {
                println("❌ REFRESH: No refresh token available")
                return false
            }

            println("🔄 REFRESH: Attempting to refresh token")
            val response = apiService.refreshToken(refreshToken)

            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true && apiResponse.data != null) {
                    val authData = apiResponse.data

                    authData.accessToken?.let { preferences.saveAuthToken(it) }
                    authData.refreshToken?.let { preferences.saveRefreshToken(it) }

                    println("✅ REFRESH: Tokens refreshed successfully")
                    return true
                }
            }

            if (response.code() == 401) {
                clearSession()
            }
            false
        } catch (e: Exception) {
            println("❌ REFRESH: Exception: ${e.message}")
            false
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                val refreshToken = preferences.getRefreshToken()
                if (refreshToken.isNotEmpty()) {
                    val response = apiService.signOut(refreshToken)
                    if (response.isSuccessful) {
                        println("✅ LOGOUT: Successfully signed out")
                    }
                }
            } catch (e: Exception) {
                println("⚠️ LOGOUT: Error: ${e.message}")
            } finally {
                clearSession()
                _loginState.value = LoginState.Idle
                _verificationState.value = VerificationState.Idle
            }
        }
    }

    private fun clearSession() {
        preferences.clearAuthToken()
        preferences.clearRefreshToken()
        preferences.setUserLoggedIn(false)
        preferences.clearUserData()
        preferences.clearTempCredentials()
        currentPreAuthToken = ""
        availableAccounts = emptyList()
        println("🧹 Session cleared")
    }

    private fun createDomainAuthData(authData: AuthData): DomainAuthData {
        val user = authData.user ?: return DomainAuthData("", DomainUser("", null, "", "", "", false))

        return DomainAuthData(
            token = authData.accessToken ?: "",
            user = DomainUser(
                id = user.id ?: "",
                email = user.email,
                name = "${user.firstName ?: ""} ${user.lastName ?: ""}".trim(),
                phone = user.phone ?: "",
                role = user.role ?: "employee",   // ✅ role is String?
                isEmailVerified = true
            )
        )
    }

    fun isUserLoggedIn(): Boolean {
        return preferences.isLoggedIn() && preferences.getAuthToken().isNotEmpty()
    }

    fun isUserVerified(): Boolean {
        return preferences.getUserVerified()
    }

    fun clearSavedCredentials() {
        preferences.saveEmail("")
        preferences.savePhone("")
        preferences.setRememberMeEnabled(false)
        preferences.clearTempCredentials()
    }

    fun resetState() {
        _loginState.value = LoginState.Idle
        _verificationState.value = VerificationState.Idle
        currentPreAuthToken = ""
        availableAccounts = emptyList()
    }

    private fun handleHttpError(code: Int, errorBody: String? = null): String {
        println("❌ LOGIN: HTTP error: $code")
        println("❌ LOGIN: Error body: $errorBody")

        return when (code) {
            400 -> {
                if (errorBody?.contains("password") == true) "Invalid password"
                else if (errorBody?.contains("email") == true || errorBody?.contains("phone") == true) "Invalid email or phone number"
                else "Invalid username or password"
            }
            401 -> "Invalid credentials. Please check your email/phone and password."
            403 -> "Account is locked or disabled. Please contact support."
            404 -> "Account not found. Please sign up first."
            422 -> {
                when {
                    errorBody?.contains("password") == true -> "Password must be at least 6 characters"
                    errorBody?.contains("email") == true -> "Invalid email format"
                    errorBody?.contains("phone") == true -> "Invalid phone number format"
                    else -> "Invalid input data. Please check your information."
                }
            }
            429 -> "Too many attempts. Please try again in a few minutes."
            500 -> "Server error. Please try again later."
            502, 503, 504 -> "Server is temporarily unavailable. Please try again."
            else -> "Login failed. Please try again."
        }
    }

    private fun handleVerificationError(response: retrofit2.Response<*>) {
        val errorBody = response.errorBody()?.string()
        println("❌ VERIFY: HTTP error ${response.code()}: $errorBody")

        val errorMsg = when (response.code()) {
            400 -> "Invalid OTP code. Please check and try again."
            401 -> "OTP code has expired. Please request a new code."
            404 -> "User not found"
            429 -> "Too many attempts. Please wait before trying again."
            else -> "Verification failed. Please try again."
        }
        _verificationState.value = VerificationState.Error(errorMsg)
    }

    sealed class LoginState {
        object Idle : LoginState()
        object Loading : LoginState()
        data class Success(val authData: DomainAuthData) : LoginState()
        data class VerificationRequired(
            val authData: DomainAuthData,
            val userId: String
        ) : LoginState()
        data class Error(val message: String) : LoginState()
    }

    sealed class VerificationState {
        object Idle : VerificationState()
        object Loading : VerificationState()
        data class Success(val message: String) : VerificationState()
        data class Error(val message: String) : VerificationState()
        data class AccountsReceived(val accounts: List<AccountInfo>) : VerificationState()
    }

    data class DomainAuthData(
        val token: String,
        val user: DomainUser
    )

    data class DomainUser(
        val id: String,
        val email: String?,
        val name: String,
        val phone: String,
        val role: String,
        val isEmailVerified: Boolean = false
    )
}