package com.devbrian.osebo.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.devbrian.osebo.R
import com.devbrian.osebo.data.PreferenceManager
import com.devbrian.osebo.data.remote.dto.response.PreAuthData
import com.devbrian.osebo.data.remote.dto.response.SigninData
import com.devbrian.osebo.data.repository.AuthRepository
import com.devbrian.osebo.utils.NetworkUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SelectAccountActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PRE_AUTH_DATA = "PRE_AUTH_DATA"
        private const val TAG = "SelectAccountActivity"
    }

    private val authRepository = AuthRepository()
    private lateinit var preferenceManager: PreferenceManager

    private lateinit var accountsContainer: LinearLayout   // Changed from RadioGroup
    private lateinit var btnContinue: Button
    private lateinit var btnBackToLogin: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var preAuthData: PreAuthData
    private var selectedAccountId: String? = null
    private var selectedIndex: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_account)

        preferenceManager = PreferenceManager.getInstance(this)

        val data = intent.getParcelableExtra<PreAuthData>(EXTRA_PRE_AUTH_DATA)
        if (data == null || data.accounts.isEmpty()) {
            Toast.makeText(this, "No accounts found. Please log in again.", Toast.LENGTH_LONG).show()
            goBackToLogin()
            return
        }
        preAuthData = data
        Log.d(TAG, "Received PreAuthData: ${preAuthData.accounts.size} accounts")

        initViews()
        populateAccounts()
        setupListeners()
    }

    private fun initViews() {
        accountsContainer = findViewById(R.id.accountsContainer)   // Your LinearLayout ID
        btnContinue = findViewById(R.id.btnContinue)
        btnBackToLogin = findViewById(R.id.btnBackToLogin)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun populateAccounts() {
        accountsContainer.removeAllViews()

        preAuthData.accounts.forEachIndexed { index, account ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = dp(8) }
                setOnClickListener { onAccountSelected(index) }
            }

            val radioButton = RadioButton(this).apply {
                id = index
                text = "${account.ownerFirstName?.trim()} ${account.ownerLastName?.trim()}"
                textSize = 16f
                isClickable = false   // Let the row handle clicks
            }
            row.addView(radioButton)

            if (account.isOwner) {
                val badge = TextView(this).apply {
                    text = "Owner"
                    setPadding(dp(10), dp(4), dp(10), dp(4))
                    setBackgroundResource(android.R.drawable.editbox_background)
                }
                row.addView(badge)
            }

            accountsContainer.addView(row)
        }

        // Auto-select the first account
        if (accountsContainer.childCount > 0) {
            onAccountSelected(0)
        }
    }

    private fun onAccountSelected(index: Int) {
        // Update UI: uncheck all, then check the selected one
        for (i in 0 until accountsContainer.childCount) {
            val row = accountsContainer.getChildAt(i) as? LinearLayout
            row?.getChildAt(0)?.let {
                (it as? RadioButton)?.isChecked = (i == index)
            }
        }
        selectedIndex = index
        selectedAccountId = preAuthData.accounts[index].accountId
        Log.d(TAG, "Selected account: $selectedAccountId")
    }

    private fun setupListeners() {
        btnContinue.setOnClickListener { onContinueClicked() }
        btnBackToLogin.setOnClickListener { goBackToLogin() }
    }

    private fun onContinueClicked() {
        if (selectedAccountId == null) {
            Toast.makeText(this, "Please select an account", Toast.LENGTH_SHORT).show()
            return
        }
        Log.d(TAG, "Continuing with account: $selectedAccountId")
        selectAccount(preAuthData.preAuthToken, selectedAccountId!!)
    }

    private fun selectAccount(preAuthToken: String, accountId: String) {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_LONG).show()
            return
        }

        showLoading(true)
        Log.d(TAG, "Calling selectAccount with token: ${preAuthToken.take(20)}..., accountId: $accountId")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = authRepository.selectAccount(preAuthToken, accountId)

                withContext(Dispatchers.Main) {
                    showLoading(false)

                    if (result.isSuccess) {
                        val signinData = result.getOrNull()!!
                        Log.d(TAG, "Select account success, saving user data")
                        saveUserAndProceed(signinData)
                    } else {
                        val error = result.exceptionOrNull()
                        Log.e(TAG, "Select account failed", error)
                        Toast.makeText(
                            this@SelectAccountActivity,
                            "Failed to select account: ${error?.message ?: "Unknown error"}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception during selectAccount", e)
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    Toast.makeText(
                        this@SelectAccountActivity,
                        "Error: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun saveUserAndProceed(signinData: SigninData) {
        try {
            // Save tokens
            preferenceManager.saveAuthToken(signinData.accessToken)
            preferenceManager.saveRefreshToken(signinData.refreshToken)
            preferenceManager.setUserLoggedIn(true)

            // Save user data
            val user = signinData.user
            val fullName = "${user.firstName?.trim() ?: ""} ${user.lastName?.trim() ?: ""}".trim()
            preferenceManager.saveUserName(if (fullName.isNotEmpty()) fullName else "User")
            preferenceManager.saveFirstName(user.firstName?.trim() ?: "")
            preferenceManager.saveLastName(user.lastName?.trim() ?: "")
            preferenceManager.saveUserEmail(user.email ?: "")
            preferenceManager.saveUserPhone(user.phone ?: "")
            preferenceManager.saveUserId(user.id)

            Log.d(TAG, "User data saved, navigating to Main")
            navigateToMain()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving user data", e)
            Toast.makeText(this, "Error saving user data: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    private fun goBackToLogin() {
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        btnContinue.isEnabled = !show
        btnBackToLogin.isEnabled = !show
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}