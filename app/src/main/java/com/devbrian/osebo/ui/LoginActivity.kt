package com.devbrian.osebo.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.devbrian.osebo.R
import com.devbrian.osebo.data.PreferenceManager
import com.devbrian.osebo.data.remote.dto.response.PreAuthData
import com.devbrian.osebo.data.remote.dto.response.SigninData
import com.devbrian.osebo.data.repository.AuthRepository
import com.devbrian.osebo.utils.NetworkUtils
import com.hbb20.CountryCodePicker
import kotlinx.coroutines.*

class LoginActivity : AppCompatActivity() {

    // UI
    private lateinit var rgLoginMethod: RadioGroup
    private lateinit var rbPhone: RadioButton
    private lateinit var rbEmail: RadioButton

    private lateinit var emailLayout: LinearLayout
    private lateinit var phoneLayout: LinearLayout
    private lateinit var otpLayout: LinearLayout

    private lateinit var etEmail: EditText
    private lateinit var etPhone: EditText
    private lateinit var etPassword: EditText
    private lateinit var etOtpCode: EditText

    private lateinit var btnSignIn: Button
    private lateinit var tvSignUp: TextView
    private lateinit var tvForgotPassword: TextView
    private lateinit var tvResendOtp: TextView
    private lateinit var tvError: TextView
    private lateinit var progressBar: ProgressBar

    private lateinit var ccp: CountryCodePicker

    // Logic
    private val authRepository = AuthRepository()
    private lateinit var preferenceManager: PreferenceManager

    private var currentPhoneNumber = ""
    private var currentUserId: String? = null
    private var isOtpMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        preferenceManager = PreferenceManager.getInstance(this)

        initViews()
        setupListeners()

        rbPhone.isChecked = true
        showPhoneLayout()
    }

    private fun initViews() {
        rgLoginMethod = findViewById(R.id.rgLoginMethod)
        rbPhone = findViewById(R.id.rbPhone)
        rbEmail = findViewById(R.id.rbEmail)

        emailLayout = findViewById(R.id.emailLayout)
        phoneLayout = findViewById(R.id.phoneLayout)
        otpLayout = findViewById(R.id.otpLayout)

        etEmail = findViewById(R.id.etEmail)
        etPhone = findViewById(R.id.etPhone)
        etPassword = findViewById(R.id.etPassword)
        etOtpCode = findViewById(R.id.etOtpCode)

        btnSignIn = findViewById(R.id.btnSignIn)
        tvSignUp = findViewById(R.id.tvSignUp)
        tvForgotPassword = findViewById(R.id.tvForgotPassword)
        tvResendOtp = findViewById(R.id.tvResendOtp)
        tvError = findViewById(R.id.tvError)
        progressBar = findViewById(R.id.progressBar)

        ccp = findViewById(R.id.ccp)
    }

    private fun setupListeners() {
        rgLoginMethod.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rbPhone -> showPhoneLayout()
                R.id.rbEmail -> showEmailLayout()
            }
        }

        btnSignIn.setOnClickListener { attemptLogin() }

        tvSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        tvForgotPassword.setOnClickListener {
            Toast.makeText(this, "Reset coming soon", Toast.LENGTH_SHORT).show()
        }

        tvResendOtp.setOnClickListener { resendOtp() }
    }

    // ---------------- UI MODES ----------------

    private fun showPhoneLayout() {
        phoneLayout.visibility = View.VISIBLE
        emailLayout.visibility = View.GONE
        otpLayout.visibility = View.GONE
        isOtpMode = false
        btnSignIn.text = "Sign In"
        clearErrors()
    }

    private fun showEmailLayout() {
        phoneLayout.visibility = View.GONE
        emailLayout.visibility = View.VISIBLE
        otpLayout.visibility = View.GONE
        isOtpMode = false
        btnSignIn.text = "Sign In"
        clearErrors()
    }

    // ---------------- LOGIN ENTRY ----------------

    private fun attemptLogin() {
        if (rbPhone.isChecked) attemptPhoneLogin()
        else attemptEmailLogin()
    }

    // ---------------- PHONE OTP LOGIN ----------------

    private fun attemptPhoneLogin() {
        val phoneRaw = etPhone.text.toString().trim()

        if (phoneRaw.isBlank()) {
            etPhone.error = "Phone required"
            return
        }

        currentPhoneNumber = ccp.selectedCountryCodeWithPlus + phoneRaw

        if (isOtpMode) verifyOtp()
        else requestOtp()
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
                    btnSignIn.text = "Verify OTP"

                    Toast.makeText(
                        this@LoginActivity,
                        "OTP sent to $currentPhoneNumber",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    showError(result.exceptionOrNull()?.message ?: "OTP failed")
                }
            }
        }
    }

    private fun verifyOtp() {
        val otp = etOtpCode.text.toString().trim()

        if (otp.length != 6) {
            etOtpCode.error = "Enter 6-digit OTP"
            return
        }

        if (currentUserId == null) {
            showError("Session expired")
            resetOtp()
            return
        }

        showLoading(true)

        CoroutineScope(Dispatchers.IO).launch {
            val result = authRepository.verify2fa(currentUserId!!, otp)

            withContext(Dispatchers.Main) {
                showLoading(false)

                if (result.isSuccess) {
                    val preAuth = result.getOrNull()!!
                    handleAccounts(preAuth)
                } else {
                    showError(result.exceptionOrNull()?.message ?: "Invalid OTP")
                }
            }
        }
    }

    // ---------------- EMAIL LOGIN ----------------

    private fun attemptEmailLogin() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (email.isBlank()) {
            etEmail.error = "Email required"
            return
        }

        if (password.isBlank()) {
            etPassword.error = "Password required"
            return
        }

        showLoading(true)

        CoroutineScope(Dispatchers.IO).launch {
            val result = authRepository.loginWithPassword(email, password)

            withContext(Dispatchers.Main) {
                showLoading(false)

                if (result.isSuccess) {
                    val data = result.getOrNull()!!
                    handleAccounts(data)
                } else {
                    showError("Login failed")
                }
            }
        }
    }

    // ---------------- ACCOUNT HANDLING ----------------

    private fun handleAccounts(data: PreAuthData) {
        if (data.accounts.size == 1) {
            selectAccount(data.preAuthToken, data.accounts[0].accountId)
            val intent = Intent(this, SelectAccountActivity::class.java)
            intent.putExtra(SelectAccountActivity.EXTRA_PRE_AUTH_DATA, data)
            startActivity(intent)
        }
    }

    private fun selectAccount(token: String, accountId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = authRepository.selectAccount(token, accountId)

            withContext(Dispatchers.Main) {
                if (result.isSuccess) {
                    saveAuth(result.getOrNull()!!)
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                } else {
                    showError("Account selection failed")
                }
            }
        }
    }

    // ---------------- UTIL ----------------

    private fun saveAuth(data: SigninData) {
        preferenceManager.saveAuthToken(data.accessToken)
        preferenceManager.saveRefreshToken(data.refreshToken)
        preferenceManager.saveUserId(data.user.id)
        preferenceManager.setUserLoggedIn(true)
    }

    private fun resendOtp() {
        if (currentUserId == null) return requestOtp()

        CoroutineScope(Dispatchers.IO).launch {
            authRepository.resendOtp(currentUserId!!)
        }
    }

    private fun resetOtp() {
        isOtpMode = false
        currentUserId = null
        otpLayout.visibility = View.GONE
        btnSignIn.text = "Sign In"
        etOtpCode.text.clear()
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        btnSignIn.isEnabled = !show
    }

    private fun showError(msg: String) {
        tvError.text = msg
        tvError.visibility = View.VISIBLE
    }

    private fun clearErrors() {
        tvError.visibility = View.GONE
    }
}