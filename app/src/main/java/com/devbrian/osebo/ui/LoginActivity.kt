package com.devbrian.osebo.ui

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.devbrian.osebo.R

class LoginActivity : AppCompatActivity() {

    private lateinit var rbEmail: RadioButton
    private lateinit var rbPhone: RadioButton
    private lateinit var emailLayout: LinearLayout
    private lateinit var phoneLayout: LinearLayout
    private lateinit var etEmail: EditText
    private lateinit var etPhone: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvSignUp: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initViews()
        setupListeners()
    }

    private fun initViews() {
        rbEmail = findViewById(R.id.rbEmail)
        rbPhone = findViewById(R.id.rbPhone)
        emailLayout = findViewById(R.id.emailLayout)
        phoneLayout = findViewById(R.id.phoneLayout)
        etEmail = findViewById(R.id.etEmail)
        etPhone = findViewById(R.id.etPhone)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvSignUp = findViewById(R.id.tvSignUp)
    }

    private fun setupListeners() {

        rbEmail.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                emailLayout.visibility = View.VISIBLE
                phoneLayout.visibility = View.GONE
            }
        }

        rbPhone.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                phoneLayout.visibility = View.VISIBLE
                emailLayout.visibility = View.GONE
            }
        }

        btnLogin.setOnClickListener {
            validateAndLogin()
        }

        tvSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }

    private fun validateAndLogin() {
        val password = etPassword.text.toString().trim()

        if (rbEmail.isChecked) {
            val email = etEmail.text.toString().trim()

            if (email.isEmpty()) {
                etEmail.error = "Email is required"
                return
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.error = "Enter a valid email"
                return
            }

        } else {
            val phone = etPhone.text.toString().trim()

            if (phone.isEmpty()) {
                etPhone.error = "Phone number is required"
                return
            }

            if (phone.length < 9) {
                etPhone.error = "Enter a valid phone number"
                return
            }
        }

        if (password.isEmpty()) {
            etPassword.error = "Password is required"
            return
        }

        if (password.length < 6) {
            etPassword.error = "Password must be at least 6 characters"
            return
        }

        // TODO: Connect to API / Firebase Authentication
        Toast.makeText(this, "Login successful (demo)", Toast.LENGTH_SHORT).show()
    }
}