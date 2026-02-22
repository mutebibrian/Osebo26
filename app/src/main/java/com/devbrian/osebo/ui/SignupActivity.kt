package com.devbrian.osebo.ui

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.devbrian.osebo.R
import com.devbrian.osebo.data.remote.dto.request.SignUpRequest
import com.devbrian.osebo.data.remote.dto.response.SignUpResponse
import com.devbrian.osebo.data.repository.AuthRepository
import com.devbrian.osebo.utils.NetworkUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SignUpActivity : AppCompatActivity() {

    private lateinit var etFirstName: EditText
    private lateinit var etLastName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhone: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnSignUp: Button
    private lateinit var tvLogin: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvPhoneHint: TextView

    // Repository instance
    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        initViews()
        setupListeners()
        setupTextChangeListeners()
    }

    private fun initViews() {
        etFirstName = findViewById(R.id.etFirstName)
        etLastName = findViewById(R.id.etLastName)
        etEmail = findViewById(R.id.etEmail)
        etPhone = findViewById(R.id.etPhone)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnSignUp = findViewById(R.id.btnSignUp)
        tvLogin = findViewById(R.id.tvLogin)
        progressBar = findViewById(R.id.progressBar)
        tvPhoneHint = findViewById(R.id.tvPhoneHint)

        // Set phone hint
        tvPhoneHint.text = "Format: +2567XXXXXXXX or 07XXXXXXXX"
    }

    private fun setupListeners() {
        btnSignUp.setOnClickListener {
            validateAndSignUp()
        }

        tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun validateAndSignUp() {
        val firstName = etFirstName.text.toString().trim()
        val lastName = etLastName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        var phone = etPhone.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        // Format phone number for Ugandan format
        phone = formatUgandanPhone(phone)

        // Validation
        if (!validateInputs(firstName, lastName, email, phone, password, confirmPassword)) {
            return
        }

        // Check internet connection
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_LONG).show()
            return
        }

        // Show progress
        showLoading(true)

        // Create request object
        val signUpRequest = SignUpRequest(
            firstName = firstName,
            lastName = lastName,
            email = email,
            password = password,
            phone = phone,
            title = "Shop Owner"
        )

        // Make API call using Coroutines
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = authRepository.signUp(signUpRequest)

                withContext(Dispatchers.Main) {
                    showLoading(false)

                    // Handle the Result wrapper
                    when {
                        result.isSuccess -> {
                            val response = result.getOrNull()
                            if (response != null && response.success) {
                                // SUCCESS - Extract userId from response
                                val userId = extractUserId(response)

                                if (userId.isNullOrEmpty()) {
                                    // If userId not found, show error
                                    Toast.makeText(
                                        this@SignUpActivity,
                                        "Registration successful but user ID not received",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    // Navigate to login as fallback
                                    startActivity(Intent(this@SignUpActivity, LoginActivity::class.java))
                                    finish()
                                } else {
                                    // Success with userId
                                    Toast.makeText(
                                        this@SignUpActivity,
                                        "Account created! Please verify with OTP sent via SMS",
                                        Toast.LENGTH_LONG
                                    ).show()

                                    // Navigate to OTP VERIFICATION with userId
                                    val intent = Intent(this@SignUpActivity, OtpVerificationActivity::class.java)
                                    intent.putExtra("EMAIL", email)
                                    intent.putExtra("PHONE", phone)
                                    intent.putExtra("USER_ID", userId)
                                    startActivity(intent)
                                    finish()
                                }
                            } else {
                                // API returned success=false
                                val errorMessage = response?.message ?: "Sign up failed"
                                handleSignUpError(errorMessage)
                            }
                        }
                        result.isFailure -> {
                            // Network or other error
                            val errorMessage = result.exceptionOrNull()?.message ?: "Sign up failed"
                            handleSignUpError(errorMessage)
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    handleSignUpError("Unexpected error: ${e.message}")
                }
            }
        }
    }

    /**
     * Extract userId from the signup response
     * Based on your API response structure
     */
    private fun extractUserId(response: SignUpResponse): String? {
        // Method 1: Check if userId is directly in response
        if (!response.userId.isNullOrEmpty()) {
            println("DEBUG: Found userId in response.userId: ${response.userId}")
            return response.userId
        }

        // Method 2: Check data field if it's a map
        if (response.data != null) {
            when (response.data) {
                is Map<*, *> -> {
                    val dataMap = response.data as Map<*, *>
                    println("DEBUG: Data is a Map, keys: ${dataMap.keys}")

                    // Try common userId field names
                    val userId = dataMap["userId"]?.toString()
                        ?: dataMap["id"]?.toString()
                        ?: dataMap["user_id"]?.toString()

                    if (!userId.isNullOrEmpty()) {
                        println("DEBUG: Found userId in data map: $userId")
                        return userId
                    }
                }
                is String -> {
                    // Data might be a JSON string
                    val dataString = response.data as String
                    println("DEBUG: Data is a String: $dataString")

                    // Try to extract userId from JSON string
                    if (dataString.contains("userId") || dataString.contains("\"id\"")) {
                        // Simple regex extraction (for debugging)
                        val patterns = listOf(
                            "\"userId\"\\s*:\\s*\"([^\"]+)\"",
                            "\"id\"\\s*:\\s*\"([^\"]+)\"",
                            "\"user_id\"\\s*:\\s*\"([^\"]+)\""
                        )

                        for (pattern in patterns) {
                            val regex = pattern.toRegex()
                            val match = regex.find(dataString)
                            if (match != null) {
                                val foundId = match.groupValues[1]
                                println("DEBUG: Extracted userId from JSON string: $foundId")
                                return foundId
                            }
                        }
                    }
                }
                else -> {
                    println("DEBUG: Data is of type: ${response.data.javaClass.name}")
                }
            }
        }

        // Method 3: Check message or other fields
        if (!response.message.isNullOrEmpty()) {
            println("DEBUG: Message field: ${response.message}")
            // Sometimes userId might be in message
            if (response.message!!.contains("id:", ignoreCase = true)) {
                val idPattern = "id:\\s*([\\w-]+)".toRegex(RegexOption.IGNORE_CASE)
                val match = idPattern.find(response.message!!)
                if (match != null) {
                    val foundId = match.groupValues[1]
                    println("DEBUG: Found userId in message: $foundId")
                    return foundId
                }
            }
        }

        println("DEBUG: Could not extract userId from response")
        println("DEBUG: Full response: success=${response.success}, message=${response.message}, data=${response.data}")
        return null
    }

    private fun validateInputs(
        firstName: String,
        lastName: String,
        email: String,
        phone: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        var isValid = true

        if (firstName.isEmpty()) {
            etFirstName.error = "First name is required"
            isValid = false
        }

        if (lastName.isEmpty()) {
            etLastName.error = "Last name is required"
            isValid = false
        }

        if (email.isEmpty()) {
            etEmail.error = "Email is required"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Enter a valid email"
            isValid = false
        }

        if (phone.isEmpty()) {
            etPhone.error = "Phone number is required"
            isValid = false
        } else if (!isValidUgandanPhone(phone)) {
            etPhone.error = "Enter a valid Ugandan phone number"
            tvPhoneHint.visibility = View.VISIBLE
            isValid = false
        } else {
            tvPhoneHint.visibility = View.GONE
        }

        if (password.isEmpty()) {
            etPassword.error = "Password is required"
            isValid = false
        } else if (password.length < 6) {
            etPassword.error = "Password must be at least 6 characters"
            isValid = false
        }

        if (confirmPassword.isEmpty()) {
            etConfirmPassword.error = "Confirm your password"
            isValid = false
        } else if (password != confirmPassword) {
            etConfirmPassword.error = "Passwords do not match"
            isValid = false
        }

        return isValid
    }



    private fun handleSignUpError(errorMessage: String) {
        // Check for specific errors
        when {
            errorMessage.contains("already exists", ignoreCase = true) -> {
                etEmail.error = "Email already registered"
                Toast.makeText(this, "Email already exists. Please login instead.", Toast.LENGTH_LONG).show()
            }
            errorMessage.contains("network", ignoreCase = true) -> {
                Toast.makeText(this, "Network error. Please check your connection.", Toast.LENGTH_LONG).show()
            }
            errorMessage.contains("Invalid request", ignoreCase = true) -> {
                Toast.makeText(this, "Invalid data. Please check your information.", Toast.LENGTH_LONG).show()
            }
            errorMessage.contains("Server error", ignoreCase = true) -> {
                Toast.makeText(this, "Server error. Please try again later.", Toast.LENGTH_LONG).show()
            }
            errorMessage.contains("Validation Error", ignoreCase = true) -> {
                // Show validation errors from API
                Toast.makeText(this, "Please check all fields and try again", Toast.LENGTH_LONG).show()
            }
            errorMessage.contains("phone", ignoreCase = true) && errorMessage.contains("Ugandan", ignoreCase = true) -> {
                etPhone.error = "Invalid Ugandan phone number"
                tvPhoneHint.visibility = View.VISIBLE
                Toast.makeText(this, "Please use format: +2567XXXXXXXX or 07XXXXXXXX", Toast.LENGTH_LONG).show()
            }
            else -> {
                Toast.makeText(this, "Sign up failed: $errorMessage", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        btnSignUp.isEnabled = !isLoading
        etFirstName.isEnabled = !isLoading
        etLastName.isEnabled = !isLoading
        etEmail.isEnabled = !isLoading
        etPhone.isEnabled = !isLoading
        etPassword.isEnabled = !isLoading
        etConfirmPassword.isEnabled = !isLoading
    }

    // Clear errors when user starts typing
    private fun setupTextChangeListeners() {
        val textWatcher = object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                // Clear error when user starts typing
                etFirstName.error = null
                etLastName.error = null
                etEmail.error = null
                etPhone.error = null
                etPassword.error = null
                etConfirmPassword.error = null
                tvPhoneHint.visibility = View.GONE
            }
        }

        etFirstName.addTextChangedListener(textWatcher)
        etLastName.addTextChangedListener(textWatcher)
        etEmail.addTextChangedListener(textWatcher)
        etPhone.addTextChangedListener(textWatcher)
        etPassword.addTextChangedListener(textWatcher)
        etConfirmPassword.addTextChangedListener(textWatcher)

        // Special listener for phone field to auto-format
        etPhone.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                s?.let {
                    val currentText = it.toString()
                    if (currentText.isNotEmpty() && !currentText.startsWith("+")) {
                        // Auto-suggest +256 prefix when user types 07...
                        if (currentText.startsWith("07") && currentText.length == 10) {
                            val formatted = "+256${currentText.substring(1)}"
                            etPhone.removeTextChangedListener(this)
                            etPhone.setText(formatted)
                            etPhone.setSelection(formatted.length)
                            etPhone.addTextChangedListener(this)
                        }
                    }
                }
            }
        })
    }

    // Phone validation helper functions
    private fun isValidUgandanPhone(phone: String): Boolean {
        // Remove any spaces, dashes, parentheses
        val cleanPhone = phone.replace("[\\s-()]".toRegex(), "")

        // Check for valid Ugandan phone patterns
        return when {
            // +2567XXXXXXXX (13 characters)
            cleanPhone.startsWith("+2567") && cleanPhone.length == 13 -> true
            // 2567XXXXXXXX (12 characters)
            cleanPhone.startsWith("2567") && cleanPhone.length == 12 -> true
            // 07XXXXXXXX (10 characters)
            cleanPhone.startsWith("07") && cleanPhone.length == 10 -> true
            // 7XXXXXXXX (9 characters) - less common
            cleanPhone.startsWith("7") && cleanPhone.length == 9 -> true
            else -> false
        }
    }

    private fun formatUgandanPhone(phone: String): String {
        var formatted = phone.trim()

        // Remove any spaces, dashes, parentheses
        formatted = formatted.replace("[\\s-()]".toRegex(), "")

        return when {
            // Already in +256 format
            formatted.startsWith("+2567") && formatted.length == 13 -> formatted
            // 256 format - add +
            formatted.startsWith("2567") && formatted.length == 12 -> "+$formatted"
            // 07 format - convert to +256
            formatted.startsWith("07") && formatted.length == 10 -> "+256${formatted.substring(1)}"
            // 7 format - add +256
            formatted.startsWith("7") && formatted.length == 9 -> "+256$formatted"
            // 0 format (if starts with 0 but not 07) - convert to +256
            formatted.startsWith("0") && formatted.length == 10 -> "+256${formatted.substring(1)}"
            // Return as-is for API to validate
            else -> formatted
        }
    }

    // Debug function to see what's in the response
    private fun debugResponse(response: SignUpResponse) {
        println("DEBUG SIGNUP RESPONSE:")
        println("  Success: ${response.success}")
        println("  Message: ${response.message}")
        println("  UserId field: ${response.userId}")
        println("  Data type: ${response.data?.javaClass?.name}")

        if (response.data != null) {
            when (response.data) {
                is Map<*, *> -> {
                    val map = response.data as Map<*, *>
                    println("  Data Map Contents:")
                    for ((key, value) in map) {
                        println("    $key = $value (${value?.javaClass?.name})")
                    }
                }
                is String -> {
                    println("  Data String: ${response.data}")
                }
                else -> {
                    println("  Data: ${response.data}")
                }
            }
        }
    }
}