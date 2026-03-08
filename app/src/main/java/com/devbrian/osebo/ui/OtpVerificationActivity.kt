package com.devbrian.osebo.ui

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.devbrian.osebo.R
import com.devbrian.osebo.data.repository.AuthRepository
import com.devbrian.osebo.utils.NetworkUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OtpVerificationActivity : AppCompatActivity() {

    private lateinit var etOtp1: EditText
    private lateinit var etOtp2: EditText
    private lateinit var etOtp3: EditText
    private lateinit var etOtp4: EditText
    private lateinit var etOtp5: EditText
    private lateinit var etOtp6: EditText
    private lateinit var btnVerifyOtp: Button
    private lateinit var tvResendOtp: TextView
    private lateinit var tvTimer: TextView
    private lateinit var progressBar: ProgressBar

    private val authRepository = AuthRepository()
    private var email: String = ""
    private var phone: String = ""
    private var userId: String = ""
    private var firstName: String = ""
    private var lastName: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp_verification)


        email = intent.getStringExtra("EMAIL") ?: ""
        phone = intent.getStringExtra("PHONE") ?: ""
        userId = intent.getStringExtra("USER_ID") ?: ""
        firstName = intent.getStringExtra("FIRST_NAME") ?: ""
        lastName = intent.getStringExtra("LAST_NAME") ?: ""

        initViews()
        setupListeners()
        startOtpTimer()
    }

    private fun initViews() {
        etOtp1 = findViewById(R.id.etOtp1)
        etOtp2 = findViewById(R.id.etOtp2)
        etOtp3 = findViewById(R.id.etOtp3)
        etOtp4 = findViewById(R.id.etOtp4)
        etOtp5 = findViewById(R.id.etOtp5)
        etOtp6 = findViewById(R.id.etOtp6)
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp)
        tvResendOtp = findViewById(R.id.tvResendOtp)
        tvTimer = findViewById(R.id.tvTimer)
        progressBar = findViewById(R.id.progressBar)


        val tvPhone = findViewById<TextView>(R.id.tvPhoneNumber)
        tvPhone.text = "Code sent to $phone"


        etOtp1.requestFocus()
    }

    private fun setupListeners() {
        btnVerifyOtp.setOnClickListener {
            verifyOtp()
        }

        tvResendOtp.setOnClickListener {
            resendOtp()
        }

        setupOtpAutoMove()
    }

    private fun verifyOtp() {
        val otp = "${etOtp1.text}${etOtp2.text}${etOtp3.text}${etOtp4.text}${etOtp5.text}${etOtp6.text}"
        println("DEBUG: OTP entered: $otp")
        println("DEBUG: Email: $email")
        println("DEBUG: Phone: $phone")
        println("DEBUG: Received userId: $userId")
        println("DEBUG: First Name: $firstName")
        println("DEBUG: Last Name: $lastName")


        if (otp.length != 6) {
            Toast.makeText(this, "Please enter the 6-digit OTP", Toast.LENGTH_SHORT).show()
            return
        }

        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_LONG).show()
            return
        }

        showLoading(true)

        CoroutineScope(Dispatchers.IO).launch {
            try {

                val result = authRepository.verifyOtp(userId, otp)

                withContext(Dispatchers.Main) {
                    showLoading(false)

                    if (result.isSuccess) {
                        val response = result.getOrNull()
                        if (response != null && response.success) {

                            Toast.makeText(
                                this@OtpVerificationActivity,
                                "Account verified successfully!",
                                Toast.LENGTH_LONG
                            ).show()


                            navigateToShopCreation()
                        } else {
                            Toast.makeText(
                                this@OtpVerificationActivity,
                                response?.message ?: "Invalid OTP",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        val errorMessage =
                            result.exceptionOrNull()?.message ?: "Verification failed"
                        Toast.makeText(
                            this@OtpVerificationActivity,
                            errorMessage,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    Toast.makeText(
                        this@OtpVerificationActivity,
                        "Error: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun resendOtp() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_LONG).show()
            return
        }

        showLoading(true)

        CoroutineScope(Dispatchers.IO).launch {
            try {

                val result = authRepository.resendOtp(email)

                withContext(Dispatchers.Main) {
                    showLoading(false)

                    if (result.isSuccess) {
                        val response = result.getOrNull()
                        if (response != null && response.success) {
                            Toast.makeText(
                                this@OtpVerificationActivity,
                                "OTP resent successfully!",
                                Toast.LENGTH_LONG
                            ).show()
                            startOtpTimer()
                            clearOtpFields()
                        } else {
                            Toast.makeText(
                                this@OtpVerificationActivity,
                                response?.message ?: "Failed to resend OTP",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        val errorMessage =
                            result.exceptionOrNull()?.message ?: "Failed to resend OTP"
                        Toast.makeText(
                            this@OtpVerificationActivity,
                            errorMessage,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    Toast.makeText(
                        this@OtpVerificationActivity,
                        "Error: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun setupOtpAutoMove() {
        val otpFields = listOf(etOtp1, etOtp2, etOtp3, etOtp4, etOtp5, etOtp6)

        otpFields.forEachIndexed { index, editText ->
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (s?.length == 1 && index < otpFields.size - 1) {
                        otpFields[index + 1].requestFocus()
                    } else if (s?.length == 0 && index > 0) {
                        otpFields[index - 1].requestFocus()
                    }


                    if (otpFields.all { it.text.isNotEmpty() }) {
                        btnVerifyOtp.performClick()
                    }
                }
            })
        }
    }

    private fun clearOtpFields() {
        etOtp1.text.clear()
        etOtp2.text.clear()
        etOtp3.text.clear()
        etOtp4.text.clear()
        etOtp5.text.clear()
        etOtp6.text.clear()
        etOtp1.requestFocus()
    }

    private fun startOtpTimer() {

        tvResendOtp.isEnabled = false
        tvTimer.visibility = View.VISIBLE

        var secondsRemaining = 60

        val timer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                secondsRemaining--
                tvTimer.text = "Resend in $secondsRemaining seconds"
            }

            override fun onFinish() {
                tvResendOtp.isEnabled = true
                tvTimer.visibility = View.GONE
                tvTimer.text = ""
            }
        }
        timer.start()
    }

    private fun navigateToShopCreation() {
        // Navigate to ShopCreationActivity with user data
        val intent = Intent(this, ShopCreationActivity::class.java).apply {
            putExtra("USER_ID", userId)
            putExtra("EMAIL", email)
            putExtra("PHONE", phone)
            putExtra("FIRST_NAME", firstName)
            putExtra("LAST_NAME", lastName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        startActivity(intent)
        finish()
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        btnVerifyOtp.isEnabled = !isLoading
        tvResendOtp.isEnabled = !isLoading
    }
}