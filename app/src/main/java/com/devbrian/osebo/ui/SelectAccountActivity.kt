package com.devbrian.osebo.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.devbrian.osebo.R
import com.devbrian.osebo.data.PreferenceManager
import com.devbrian.osebo.data.remote.dto.response.PreAuthData
import com.devbrian.osebo.data.repository.AuthRepository
import com.devbrian.osebo.utils.NetworkUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SelectAccountActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PRE_AUTH_DATA = "PRE_AUTH_DATA"
    }

    private val authRepository = AuthRepository()
    private lateinit var preferenceManager: PreferenceManager

    private lateinit var rgAccounts: RadioGroup
    private lateinit var btnContinue: Button
    private lateinit var btnBackToLogin: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var preAuthData: PreAuthData

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

        initViews()
        populateAccounts()
        setupListeners()
    }
    private fun setupListeners() {
        btnContinue.setOnClickListener { onContinueClicked() }
        btnBackToLogin.setOnClickListener { goBackToLogin() }
    }

    private fun initViews() {
        rgAccounts = findViewById(R.id.rgAccounts)
        btnContinue = findViewById(R.id.btnContinue)
        btnBackToLogin = findViewById(R.id.btnBackToLogin)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun populateAccounts() {
        rgAccounts.removeAllViews()

        preAuthData.accounts.forEachIndexed { index, account ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = dp(8) }
            }

            val radioButton = RadioButton(this).apply {
                id = index
                text = "${account.ownerFirstName?.trim()} ${account.ownerLastName?.trim()}"
                textSize = 16f
            }
            row.addView(radioButton)

            if (account.isOwner) {
                val badge = TextView(this).apply {
                    text = "Owner"
                    setPadding(dp(10), dp(4), dp(10), dp(4))
                    setBackgroundResource(android.R.drawable.editbox_background) // swap for your own drawable later
                }
                row.addView(badge)
            }

            rgAccounts.addView(row)
        }

        if (rgAccounts.childCount > 0) {
            (rgAccounts.getChildAt(0) as? LinearLayout)?.getChildAt(0)?.let {
                (it as? RadioButton)?.isChecked = true
            }
        }
    }

    private fun onContinueClicked() {
        val checkedIndex = rgAccounts.checkedRadioButtonId
        if (checkedIndex == -1 || checkedIndex >= preAuthData.accounts.size) {
            Toast.makeText(this, "Please select an account", Toast.LENGTH_SHORT).show()
            return
        }

        val account = preAuthData.accounts[checkedIndex]
        selectAccount(preAuthData.preAuthToken, account.accountId)
    }



    private fun selectAccount(preAuthToken: String, accountId: String) {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_LONG).show()
            return
        }

        showLoading(true)

        CoroutineScope(Dispatchers.IO).launch {
            val result = authRepository.selectAccount(preAuthToken, accountId)

            withContext(Dispatchers.Main) {
                showLoading(false)

                if (result.isSuccess) {
                    val signinData = result.getOrNull()!!
                    saveAuth(signinData)
                    navigateToMain()
                } else {
                    val message = result.exceptionOrNull()?.message ?: "Account selection failed"
                    Toast.makeText(this@SelectAccountActivity, message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun saveAuth(data: com.devbrian.osebo.data.remote.dto.response.SigninData) {
        preferenceManager.saveAuthToken(data.accessToken)
        preferenceManager.saveRefreshToken(data.refreshToken)
        preferenceManager.saveUserId(data.user.id)
        preferenceManager.setUserLoggedIn(true)
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