package com.devbrian.osebo.ui

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.devbrian.osebo.R
import com.devbrian.osebo.data.remote.dto.request.SignUpRequest
import com.devbrian.osebo.data.repository.AuthRepository
import com.devbrian.osebo.utils.NetworkUtils
import com.hbb20.CountryCodePicker
import kotlinx.coroutines.*

class SignUpActivity : AppCompatActivity() {

    private lateinit var spinnerTitle: Spinner
    private lateinit var etFirstName: EditText
    private lateinit var etLastName: EditText
    private lateinit var etPhone: EditText
    private lateinit var etEmail: EditText
    private lateinit var etOtpCode: EditText
    private lateinit var cbTerms: CheckBox
    private lateinit var btnSendOtp: Button
    private lateinit var btnVerifyAndSignup: Button
    private lateinit var tvLogin: TextView
    private lateinit var tvResendOtp: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var otpLayout: LinearLayout
    private lateinit var tvPhoneHint: TextView

    private lateinit var ccp: CountryCodePicker

    private val authRepository = AuthRepository()
    private var currentPhoneNumber: String = ""
    private var currentUserId: String? = null
    private var isOtpMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        initViews()
        setupListeners()
    }

    private fun initViews() {
        spinnerTitle = findViewById(R.id.spinnerTitle)
        etFirstName = findViewById(R.id.etFirstName)
        etLastName = findViewById(R.id.etLastName)
        etPhone = findViewById(R.id.etPhone)
        etEmail = findViewById(R.id.etEmail)
        etOtpCode = findViewById(R.id.etOtpCode)
        cbTerms = findViewById(R.id.cbTerms)
        btnSendOtp = findViewById(R.id.btnSendOtp)
        btnVerifyAndSignup = findViewById(R.id.btnVerifyAndSignup)
        tvLogin = findViewById(R.id.tvLogin)
        tvResendOtp = findViewById(R.id.tvResendOtp)
        progressBar = findViewById(R.id.progressBar)
        otpLayout = findViewById(R.id.otpLayout)
        tvPhoneHint = findViewById(R.id.tvPhoneHint)

        ccp = findViewById(R.id.ccp)

        tvPhoneHint.text = "Enter phone number"
    }

    private fun setupListeners() {
        btnSendOtp.setOnClickListener { requestOtp() }
        btnVerifyAndSignup.setOnClickListener { verifyAndSignup() }

        tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        tvResendOtp.setOnClickListener { requestOtp() }
    }

    private fun requestOtp() {
        val phoneRaw = etPhone.text.toString().trim()

        if (phoneRaw.isEmpty()) {
            etPhone.error = "Phone number required"
            return
        }

        val phoneClean = phoneRaw.replace(" ", "")
        val fullPhone = ccp.selectedCountryCodeWithPlus + phoneClean

        if (phoneClean.length < 6) {
            etPhone.error = "Invalid phone number"
            return
        }

        tvPhoneHint.visibility = View.GONE

        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "No internet", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true)

        currentPhoneNumber = fullPhone

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = authRepository.requestOtp(fullPhone)

                withContext(Dispatchers.Main) {
                    showLoading(false)

                    if (result.isSuccess) {
                        val response = result.getOrNull()!!
                        currentUserId = response.userId
                        isOtpMode = true

                        otpLayout.visibility = View.VISIBLE
                        btnSendOtp.visibility = View.GONE
                        btnVerifyAndSignup.visibility = View.VISIBLE

                        Toast.makeText(
                            this@SignUpActivity,
                            "OTP sent to $fullPhone",
                            Toast.LENGTH_SHORT
                        ).show()

                    } else {
                        val error = result.exceptionOrNull()?.message ?: "Failed to send OTP"
                        Toast.makeText(this@SignUpActivity, error, Toast.LENGTH_LONG).show()
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    Toast.makeText(this@SignUpActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun verifyAndSignup() {
        val otp = etOtpCode.text.toString().trim()

        if (otp.isEmpty() || otp.length != 6) {
            etOtpCode.error = "Enter 6-digit code"
            return
        }

        if (currentUserId == null) {
            Toast.makeText(this, "Session expired. Try again.", Toast.LENGTH_SHORT).show()
            resetOtpMode()
            return
        }

        val title = spinnerTitle.selectedItem.toString()
        val firstName = etFirstName.text.toString().trim()
        val lastName = etLastName.text.toString().trim()
        val email = etEmail.text.toString().trim()

        if (firstName.isEmpty() || lastName.isEmpty()) {
            Toast.makeText(this, "First and last name required", Toast.LENGTH_SHORT).show()
            return
        }

        if (!cbTerms.isChecked) {
            Toast.makeText(this, "Accept Terms and Conditions", Toast.LENGTH_SHORT).show()
            return
        }

        if (email.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Invalid email"
            return
        }

        showLoading(true)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val verifyResult = authRepository.verify2fa(currentUserId!!, otp)

                if (verifyResult.isFailure) {
                    withContext(Dispatchers.Main) {
                        showLoading(false)
                        Toast.makeText(this@SignUpActivity, "Invalid OTP", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                val signupRequest = SignUpRequest(
                    firstName = firstName,
                    lastName = lastName,
                    email = email,
                    phone = currentPhoneNumber,
                    title = title,
                    otp = otp
                )

                val signupResult = authRepository.signUp(signupRequest)

                withContext(Dispatchers.Main) {
                    showLoading(false)

                    if (signupResult.isSuccess) {
                        Toast.makeText(
                            this@SignUpActivity,
                            "Account created successfully!",
                            Toast.LENGTH_LONG
                        ).show()

                        startActivity(Intent(this@SignUpActivity, LoginActivity::class.java))
                        finish()

                    } else {
                        val msg = signupResult.exceptionOrNull()?.message ?: "Signup failed"
                        Toast.makeText(this@SignUpActivity, msg, Toast.LENGTH_LONG).show()
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    Toast.makeText(this@SignUpActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun resetOtpMode() {
        isOtpMode = false
        currentUserId = null

        otpLayout.visibility = View.GONE
        btnSendOtp.visibility = View.VISIBLE
        btnVerifyAndSignup.visibility = View.GONE

        etOtpCode.text.clear()
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE

        btnSendOtp.isEnabled = !show
        btnVerifyAndSignup.isEnabled = !show

        etPhone.isEnabled = !show
        etFirstName.isEnabled = !show
        etLastName.isEnabled = !show
        etEmail.isEnabled = !show
        etOtpCode.isEnabled = !show
        spinnerTitle.isEnabled = !show
        cbTerms.isEnabled = !show
    }
}