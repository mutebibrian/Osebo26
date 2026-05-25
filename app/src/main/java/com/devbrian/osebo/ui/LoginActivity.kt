package com.devbrian.osebo.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.devbrian.osebo.R
import com.devbrian.osebo.data.PreferenceManager
import com.devbrian.osebo.data.remote.dto.response.AccountInfo
import com.devbrian.osebo.data.remote.dto.response.PreAuthData
import com.devbrian.osebo.data.remote.dto.response.SigninData
import com.devbrian.osebo.data.repository.AuthRepository
import com.devbrian.osebo.utils.NetworkUtils
import kotlinx.coroutines.*

class LoginActivity : AppCompatActivity() {

    private lateinit var rgLoginMethod: RadioGroup
    private lateinit var rbPhone: RadioButton
    private lateinit var rbEmail: RadioButton
    private lateinit var emailLayout: LinearLayout
    private lateinit var phoneLayout: LinearLayout
    private lateinit var etEmail: EditText
    private lateinit var etPhone: EditText
    private lateinit var etPassword: EditText
    private lateinit var etOtpCode: EditText
    private lateinit var btnSignIn: Button
    private lateinit var tvSignUp: TextView
    private lateinit var tvForgotPassword: TextView
    private lateinit var tvResendOtp: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var otpLayout: LinearLayout
    private lateinit var tvError: TextView

    private val authRepository = AuthRepository()
    private lateinit var preferenceManager: PreferenceManager

    private var currentPhoneNumber: String = ""
    private var currentUserId: String? = null
    private var currentPreAuthToken: String? = null
    private var currentAccounts: List<AccountInfo>? = null
    private var isOtpMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        preferenceManager = PreferenceManager.getInstance(this)

        initViews()
        setupListeners()
        // Initially select phone (default)
        rbPhone.isChecked = true
        showPhoneLayout()
    }

    private fun initViews() {
        rgLoginMethod = findViewById(R.id.rgLoginMethod)
        rbPhone = findViewById(R.id.rbPhone)
        rbEmail = findViewById(R.id.rbEmail)
        emailLayout = findViewById(R.id.emailLayout)
        phoneLayout = findViewById(R.id.phoneLayout)
        etEmail = findViewById(R.id.etEmail)
        etPhone = findViewById(R.id.etPhone)
        etPassword = findViewById(R.id.etPassword)
        etOtpCode = findViewById(R.id.etOtpCode)
        btnSignIn = findViewById(R.id.btnSignIn)
        tvSignUp = findViewById(R.id.tvSignUp)
        tvForgotPassword = findViewById(R.id.tvForgotPassword)
        tvResendOtp = findViewById(R.id.tvResendOtp)
        progressBar = findViewById(R.id.progressBar)
        otpLayout = findViewById(R.id.otpLayout)
        tvError = findViewById(R.id.tvError)
    }

    private fun setupListeners() {
        rgLoginMethod.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbPhone -> showPhoneLayout()
                R.id.rbEmail -> showEmailLayout()
            }
        }
        btnSignIn.setOnClickListener { attemptLogin() }
        tvSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
            finish()
        }
        tvForgotPassword.setOnClickListener {
            Toast.makeText(this, "Password reset coming soon", Toast.LENGTH_SHORT).show()
        }
        tvResendOtp.setOnClickListener { resendOtp() }
    }

    private fun showPhoneLayout() {
        emailLayout.visibility = View.GONE
        phoneLayout.visibility = View.VISIBLE
        otpLayout.visibility = View.GONE
        etPassword.hint = "Password (optional)"
        btnSignIn.text = "Sign In"
        isOtpMode = false
        clearErrors()
    }

    private fun showEmailLayout() {
        emailLayout.visibility = View.VISIBLE
        phoneLayout.visibility = View.GONE
        otpLayout.visibility = View.GONE
        etPassword.hint = "Password"
        btnSignIn.text = "Sign In"
        isOtpMode = false
        clearErrors()
    }

    private fun attemptLogin() {
        if (rbPhone.isChecked) {
            attemptPhoneLogin()
        } else {
            attemptEmailLogin()
        }
    }

    // ---------- PHONE LOGIN (OTP) ----------
    private fun attemptPhoneLogin() {
        val phoneRaw = etPhone.text.toString().trim()
        if (phoneRaw.isEmpty()) {
            etPhone.error = "Phone number required"
            return
        }
        val phone = formatUgandanPhone(phoneRaw)
        if (!isValidUgandanPhone(phone)) {
            etPhone.error = "Invalid Ugandan phone number"
            return
        }
        currentPhoneNumber = phone

        val password = etPassword.text.toString().trim()
        if (password.isNotEmpty()) {
            // Phone+password not supported – just ignore
            Toast.makeText(this, "Phone login uses OTP only. Password ignored.", Toast.LENGTH_SHORT).show()
        }

        if (isOtpMode) {
            verifyOtp()
        } else {
            requestOtp()
        }
    }

    private fun requestOtp() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            showError("No internet")
            return
        }
        showLoading(true)
        CoroutineScope(Dispatchers.IO).launch {
            val result = authRepository.requestOtp(currentPhoneNumber)
            withContext(Dispatchers.Main) {
                showLoading(false)
                if (result.isSuccess) {
                    currentUserId = result.getOrNull()?.userId
                    isOtpMode = true
                    otpLayout.visibility = View.VISIBLE
                    btnSignIn.text = "Verify & Sign In"
                    Toast.makeText(this@LoginActivity, "OTP sent to $currentPhoneNumber", Toast.LENGTH_SHORT).show()
                } else {
                    showError(result.exceptionOrNull()?.message ?: "Failed to request OTP")
                }
            }
        }
    }

    private fun verifyOtp() {
        val otp = etOtpCode.text.toString().trim()
        if (otp.isEmpty() || otp.length != 6) {
            etOtpCode.error = "Enter 6-digit code"
            return
        }
        if (currentUserId == null) {
            showError("Session expired. Please try again.")
            resetOtpMode()
            return
        }
        if (!NetworkUtils.isNetworkAvailable(this)) {
            showError("No internet")
            return
        }
        showLoading(true)
        CoroutineScope(Dispatchers.IO).launch {
            val result = authRepository.verify2fa(currentUserId!!, otp)
            withContext(Dispatchers.Main) {
                showLoading(false)
                if (result.isSuccess) {
                    val preAuthData = result.getOrNull()!!
                    currentPreAuthToken = preAuthData.preAuthToken
                    currentAccounts = preAuthData.accounts
                    handleAccounts(preAuthData)
                } else {
                    val errorMsg = result.exceptionOrNull()?.message ?: "Verification failed"
                    if (errorMsg.contains("already verified", ignoreCase = true)) {
                        showAlreadyVerifiedDialog()
                    } else {
                        showError(errorMsg)
                    }
                }
            }
        }
    }

    private fun handleAccounts(preAuthData: PreAuthData) {
        if (preAuthData.accounts.isEmpty()) {
            showError("No accounts found for this user")
            return
        }
        if (preAuthData.accounts.size == 1) {
            // Single account: select automatically
            selectAccount(preAuthData.preAuthToken, preAuthData.accounts[0].accountId)
        } else {
            // Multiple accounts: show picker
            showAccountPicker(preAuthData.preAuthToken, preAuthData.accounts)
        }
    }

    private fun selectAccount(preAuthToken: String, accountId: String) {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            showError("No internet")
            return
        }
        showLoading(true)
        CoroutineScope(Dispatchers.IO).launch {
            val result = authRepository.selectAccount(preAuthToken, accountId)
            withContext(Dispatchers.Main) {
                showLoading(false)
                if (result.isSuccess) {
                    val signinData = result.getOrNull()!!
                    // ✅ IMPORTANT: Save tokens and user data before navigating
                    saveAuthData(signinData)
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                } else {
                    showError(result.exceptionOrNull()?.message ?: "Account selection failed")
                }
            }
        }
    }

    private fun showAccountPicker(preAuthToken: String, accounts: List<AccountInfo>) {
        val accountNames = accounts.map { "${it.ownerFirstName} ${it.ownerLastName}" }
        AlertDialog.Builder(this)
            .setTitle("Select Account")
            .setItems(accountNames.toTypedArray()) { _, which ->
                val selectedAccount = accounts[which]
                selectAccount(preAuthToken, selectedAccount.accountId)
            }
            .setCancelable(false)
            .setNegativeButton("Cancel") { _, _ ->
                resetOtpMode()
            }
            .show()
    }

    private fun resendOtp() {
        if (currentUserId == null) {
            requestOtp()
            return
        }
        if (!NetworkUtils.isNetworkAvailable(this)) {
            showError("No internet")
            return
        }
        showLoading(true)
        CoroutineScope(Dispatchers.IO).launch {
            val result = authRepository.resendOtp(currentUserId!!)
            withContext(Dispatchers.Main) {
                showLoading(false)
                if (result.isSuccess) {
                    Toast.makeText(this@LoginActivity, "OTP resent", Toast.LENGTH_SHORT).show()
                } else {
                    showError(result.exceptionOrNull()?.message ?: "Resend failed")
                }
            }
        }
    }

    private fun resetOtpMode() {
        isOtpMode = false
        currentUserId = null
        otpLayout.visibility = View.GONE
        btnSignIn.text = "Sign In"
        etOtpCode.text.clear()
    }

    // ---------- EMAIL LOGIN (password) ----------
    private fun attemptEmailLogin() {
        val email = etEmail.text.toString().trim()
        if (email.isEmpty()) {
            etEmail.error = "Email required"
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Invalid email address"
            return
        }
        val password = etPassword.text.toString().trim()
        if (password.isEmpty()) {
            etPassword.error = "Password required"
            return
        }
        if (!NetworkUtils.isNetworkAvailable(this)) {
            showError("No internet")
            return
        }
        showLoading(true)
        CoroutineScope(Dispatchers.IO).launch {
            val result = authRepository.loginWithPassword(email, password)
            withContext(Dispatchers.Main) {
                showLoading(false)
                if (result.isSuccess) {
                    val preAuthData = result.getOrNull()!!
                    currentPreAuthToken = preAuthData.preAuthToken
                    currentAccounts = preAuthData.accounts
                    handleAccounts(preAuthData)
                } else {
                    showError(result.exceptionOrNull()?.message ?: "Login failed")
                }
            }
        }
    }

    // ---------- SAVE AUTH DATA (CRITICAL) ----------
    private fun saveAuthData(signinData: SigninData) {
        with(preferenceManager) {
            saveAuthToken(signinData.accessToken)
            saveRefreshToken(signinData.refreshToken)
            saveUserId(signinData.user.id)
            saveUserEmail(signinData.user.email ?: "")
            saveUserName("${signinData.user.firstName ?: ""} ${signinData.user.lastName ?: ""}".trim())
            saveUserPhone(signinData.user.phone ?: "")
            saveUserRole(signinData.user.role ?: "")
            setUserLoggedIn(true)
            setLastLoginTimestamp(System.currentTimeMillis())
        }
        // Debug log to verify
        println("✅ Auth tokens saved - Token: ${preferenceManager.getAuthToken().take(20)}...")
        println("✅ User ID: ${preferenceManager.getUserId()}")
    }

    // ---------- HELPERS ----------
    private fun showAlreadyVerifiedDialog() {
        AlertDialog.Builder(this)
            .setTitle("Account Already Verified")
            .setMessage("Your account is already verified. Please login with your password.")
            .setPositiveButton("Login with Password") { _, _ ->
                rbEmail.isChecked = true
                showEmailLayout()
                resetOtpMode()
                etEmail.requestFocus()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        btnSignIn.isEnabled = !show
        etEmail.isEnabled = !show
        etPhone.isEnabled = !show
        etPassword.isEnabled = !show
        etOtpCode.isEnabled = !show
        tvResendOtp.isEnabled = !show
        rgLoginMethod.isEnabled = !show
    }

    private fun showError(msg: String) {
        tvError.text = msg
        tvError.visibility = View.VISIBLE
        tvError.postDelayed({ tvError.visibility = View.GONE }, 5000)
    }

    private fun clearErrors() {
        tvError.visibility = View.GONE
        etEmail.error = null
        etPhone.error = null
        etPassword.error = null
        etOtpCode.error = null
    }

    private fun isValidUgandanPhone(phone: String): Boolean {
        val clean = phone.replace(Regex("[\\s-()]"), "")
        return clean.startsWith("+2567") && clean.length == 13
    }

    private fun formatUgandanPhone(phone: String): String {
        var clean = phone.trim().replace(Regex("[\\s-()]"), "")
        return when {
            clean.startsWith("07") && clean.length == 10 -> "+256${clean.substring(1)}"
            clean.startsWith("7") && clean.length == 9 -> "+256$clean"
            clean.startsWith("256") && clean.length == 12 -> "+$clean"
            clean.startsWith("+2567") && clean.length == 13 -> clean
            else -> clean
        }
    }
}