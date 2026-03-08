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

        
        phone = formatUgandanPhone(phone)

        
        if (!validateInputs(firstName, lastName, email, phone, password, confirmPassword)) {
            return
        }

        
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_LONG).show()
            return
        }

        
        showLoading(true)

        
        val signUpRequest = SignUpRequest(
            firstName = firstName,
            lastName = lastName,
            email = email,
            password = password,
            phone = phone,
            title = "Shop Owner"
        )

        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = authRepository.signUp(signUpRequest)

                withContext(Dispatchers.Main) {
                    showLoading(false)

                    
                    when {
                        result.isSuccess -> {
                            val response = result.getOrNull()
                            if (response != null && response.success) {
                                
                                val userId = extractUserId(response)

                                if (userId.isNullOrEmpty()) {
                                    
                                    Toast.makeText(
                                        this@SignUpActivity,
                                        "Registration successful but user ID not received",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    
                                    startActivity(Intent(this@SignUpActivity, LoginActivity::class.java))
                                    finish()
                                } else {
                                    
                                    Toast.makeText(
                                        this@SignUpActivity,
                                        "Account created! Please verify with OTP sent via SMS",
                                        Toast.LENGTH_LONG
                                    ).show()

                                    
                                    val intent = Intent(this@SignUpActivity, OtpVerificationActivity::class.java)
                                    intent.putExtra("EMAIL", email)
                                    intent.putExtra("PHONE", phone)
                                    intent.putExtra("USER_ID", userId)
                                    startActivity(intent)
                                    finish()
                                }
                            } else {
                                
                                val errorMessage = response?.message ?: "Sign up failed"
                                handleSignUpError(errorMessage)
                            }
                        }
                        result.isFailure -> {
                            
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

    
    private fun extractUserId(response: SignUpResponse): String? {
        
        if (!response.userId.isNullOrEmpty()) {
            println("DEBUG: Found userId in response.userId: ${response.userId}")
            return response.userId
        }

        
        if (response.data != null) {
            when (response.data) {
                is Map<*, *> -> {
                    val dataMap = response.data as Map<*, *>
                    println("DEBUG: Data is a Map, keys: ${dataMap.keys}")

                    
                    val userId = dataMap["userId"]?.toString()
                        ?: dataMap["id"]?.toString()
                        ?: dataMap["user_id"]?.toString()

                    if (!userId.isNullOrEmpty()) {
                        println("DEBUG: Found userId in data map: $userId")
                        return userId
                    }
                }
                is String -> {
                    
                    val dataString = response.data as String
                    println("DEBUG: Data is a String: $dataString")

                    
                    if (dataString.contains("userId") || dataString.contains("\"id\"")) {
                        
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

        
        if (!response.message.isNullOrEmpty()) {
            println("DEBUG: Message field: ${response.message}")
            
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

    
    private fun setupTextChangeListeners() {
        val textWatcher = object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                
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

        
        etPhone.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                s?.let {
                    val currentText = it.toString()
                    if (currentText.isNotEmpty() && !currentText.startsWith("+")) {
                        
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

    
    private fun isValidUgandanPhone(phone: String): Boolean {
        
        val cleanPhone = phone.replace("[\\s-()]".toRegex(), "")

        
        return when {
            
            cleanPhone.startsWith("+2567") && cleanPhone.length == 13 -> true
            
            cleanPhone.startsWith("2567") && cleanPhone.length == 12 -> true
            
            cleanPhone.startsWith("07") && cleanPhone.length == 10 -> true
            
            cleanPhone.startsWith("7") && cleanPhone.length == 9 -> true
            else -> false
        }
    }

    private fun formatUgandanPhone(phone: String): String {
        var formatted = phone.trim()

        
        formatted = formatted.replace("[\\s-()]".toRegex(), "")

        return when {
            
            formatted.startsWith("+2567") && formatted.length == 13 -> formatted
            
            formatted.startsWith("2567") && formatted.length == 12 -> "+$formatted"
            
            formatted.startsWith("07") && formatted.length == 10 -> "+256${formatted.substring(1)}"
            
            formatted.startsWith("7") && formatted.length == 9 -> "+256$formatted"
            
            formatted.startsWith("0") && formatted.length == 10 -> "+256${formatted.substring(1)}"
            
            else -> formatted
        }
    }

    
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

