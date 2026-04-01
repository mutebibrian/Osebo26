package com.devbrian.osebo.ui

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.devbrian.osebo.R
import com.devbrian.osebo.data.PreferenceManager
import com.devbrian.osebo.data.repository.ShopRepositoryImpl
import com.devbrian.osebo.models.PermissionType
import com.devbrian.osebo.ui.viewmodels.LoginViewModel
import com.devbrian.osebo.utils.NetworkUtils
import com.devbrian.osebo.utils.PermissionManager
import com.devbrian.osebo.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    @Inject
    lateinit var preferenceManager: PreferenceManager

    @Inject
    lateinit var shopRepository: ShopRepositoryImpl

    private lateinit var permissionManager: PermissionManager

    private lateinit var imgLogo: ImageView
    private lateinit var cardLogin: CardView
    private lateinit var rgLoginMethod: RadioGroup
    private lateinit var rbEmail: RadioButton
    private lateinit var rbPhone: RadioButton
    private lateinit var emailLayout: LinearLayout
    private lateinit var phoneLayout: LinearLayout
    private lateinit var etEmail: EditText
    private lateinit var etPhone: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvSignUp: TextView
    private lateinit var tvForgotPassword: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvError: TextView

    private val viewModel: LoginViewModel by viewModels()
    private var errorRunnable: Runnable? = null
    private var isShopsLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize PermissionManager
        permissionManager = PermissionManager(this)

        // Check if already logged in
        if (preferenceManager.isLoggedIn()) {
            // If already logged in, check if we need to refresh shops
            checkAndRefreshShopsBeforeMain()
            return
        }

        setContentView(R.layout.activity_login)

        initViews()
        setupListeners()
        observeLoginState()

        // Load saved credentials
        loadSavedCredentials()
    }

    private fun initViews() {
        imgLogo = findViewById(R.id.imgLogo)
        cardLogin = findViewById(R.id.cardLogin)
        rgLoginMethod = findViewById(R.id.rgLoginMethod)
        rbEmail = findViewById(R.id.rbEmail)
        rbPhone = findViewById(R.id.rbPhone)
        emailLayout = findViewById(R.id.emailLayout)
        phoneLayout = findViewById(R.id.phoneLayout)
        etEmail = findViewById(R.id.etEmail)
        etPhone = findViewById(R.id.etPhone)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvSignUp = findViewById(R.id.tvSignUp)
        tvForgotPassword = findViewById(R.id.tvForgotPassword)
        progressBar = findViewById(R.id.progressBar)
        tvError = findViewById(R.id.tvError)

        etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        tvError.visibility = View.GONE

        rbEmail.isChecked = true
        emailLayout.visibility = View.VISIBLE
        phoneLayout.visibility = View.GONE
    }

    private fun setupListeners() {
        rgLoginMethod.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbEmail -> {
                    emailLayout.visibility = View.VISIBLE
                    phoneLayout.visibility = View.GONE
                    clearErrors()
                }
                R.id.rbPhone -> {
                    phoneLayout.visibility = View.VISIBLE
                    emailLayout.visibility = View.GONE
                    clearErrors()
                }
            }
        }

        btnLogin.setOnClickListener {
            validateAndLogin()
        }

        tvSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        tvForgotPassword.setOnClickListener {
            Toast.makeText(this, getString(R.string.forgot_password_coming_soon), Toast.LENGTH_SHORT).show()
        }

        etEmail.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) clearErrors()
        }

        etPhone.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) clearErrors()
        }

        etPassword.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) clearErrors()
        }
    }

    private fun loadSavedCredentials() {
        val savedEmail = preferenceManager.getSavedEmail()
        val savedPhone = preferenceManager.getSavedPhone()

        if (savedEmail.isNotEmpty() && preferenceManager.isRememberMeEnabled()) {
            rbEmail.isChecked = true
            etEmail.setText(savedEmail)
            etPassword.requestFocus()
        } else if (savedPhone.isNotEmpty() && preferenceManager.isRememberMeEnabled()) {
            rbPhone.isChecked = true
            etPhone.setText(savedPhone)
            etPassword.requestFocus()
        }
    }

    private fun observeLoginState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loginState.collect { state ->
                    when (state) {
                        is LoginViewModel.LoginState.Idle -> {
                            showLoading(false)
                        }
                        is LoginViewModel.LoginState.Loading -> {
                            showLoading(true)
                        }
                        is LoginViewModel.LoginState.Success -> {
                            showLoading(false)
                            onLoginSuccess(state.authData)
                        }
                        is LoginViewModel.LoginState.Error -> {
                            showLoading(false)
                            showError(state.message)
                        }
                    }
                }
            }
        }
    }

    private fun validateAndLogin() {
        clearErrors()

        val password = etPassword.text.toString().trim()
        val identifier: String
        val isEmailLogin: Boolean

        if (rbEmail.isChecked) {
            val email = etEmail.text.toString().trim()

            if (email.isEmpty()) {
                etEmail.error = getString(R.string.error_email_required)
                etEmail.requestFocus()
                return
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.error = getString(R.string.error_valid_email)
                etEmail.requestFocus()
                return
            }

            identifier = email
            isEmailLogin = true

        } else {
            val phone = etPhone.text.toString().trim()

            if (phone.isEmpty()) {
                etPhone.error = getString(R.string.error_phone_required)
                etPhone.requestFocus()
                return
            }

            if (phone.length < 9 || phone.length > 15) {
                etPhone.error = getString(R.string.error_valid_phone)
                etPhone.requestFocus()
                return
            }

            if (!phone.matches(Regex("^\\+?[0-9]+$"))) {
                etPhone.error = getString(R.string.error_phone_digits_only)
                etPhone.requestFocus()
                return
            }

            identifier = phone
            isEmailLogin = false
        }

        if (password.isEmpty()) {
            etPassword.error = getString(R.string.error_password_required)
            etPassword.requestFocus()
            return
        }

        if (password.length < 6) {
            etPassword.error = getString(R.string.error_password_length)
            etPassword.requestFocus()
            return
        }

        if (!NetworkUtils.isNetworkAvailable(this)) {
            showError(getString(R.string.error_no_internet))
            return
        }

        trackLoginAttempt("started", if (isEmailLogin) "email" else "phone")
        saveCredentials(identifier, isEmailLogin)
        viewModel.login(identifier, password)
    }

    private fun saveCredentials(identifier: String, isEmail: Boolean) {
        preferenceManager.setRememberMeEnabled(true)
        if (isEmail) {
            preferenceManager.saveEmail(identifier)
        } else {
            preferenceManager.savePhone(identifier)
        }
    }

    private fun onLoginSuccess(authData: LoginViewModel.DomainAuthData) {
        try {
            trackLoginAttempt("success", if (rbEmail.isChecked) "email" else "phone")
            saveUserData(authData)
            clearOldShopData()


            preferenceManager.saveShopCount(1)

            // Check if user is owner based on role
            val isOwner = authData.user.role.equals("owner", ignoreCase = true) ||
                    authData.user.role.equals("admin", ignoreCase = true)

            if (!isOwner) {
                permissionManager.saveUserPermissions(
                    listOf(
                        PermissionType.VIEW_INVENTORY,
                        PermissionType.VIEW_SALES,
                        PermissionType.PROCESS_SALES,
                        PermissionType.VIEW_CUSTOMERS
                    )
                )
            }

            showWelcomeMessage(authData.user.name)

            // CRITICAL FIX: Load shops immediately after login
            loadShopsAfterLogin()

        } catch (e: Exception) {
            println("❌ Error in onLoginSuccess: ${e.message}")
            e.printStackTrace()
            showError("Error: ${e.message}")
        }
    }

    /**
     * Load shops from API and save subscription info before navigating to MainActivity
     * This fixes the issue where subscription status was showing as expired initially
     */
    private fun loadShopsAfterLogin() {
        if (isShopsLoading) return
        isShopsLoading = true

        showLoading(true)

        lifecycleScope.launch {
            try {
                println("🔄 Loading shops after successful login...")

                val result = shopRepository.refreshShops()

                when (result) {
                    is Resource.Success -> {
                        println("✅ Shops loaded successfully")

                        val shopsResult = shopRepository.getShops()
                        when (shopsResult) {
                            is Resource.Success -> {
                                val shopList = shopsResult.data ?: emptyList()
                                println("📊 Found ${shopList.size} shops for user")

                                // Log all shops for debugging
                                shopList.forEachIndexed { index, shop ->
                                    println("   Shop[$index] - ID: ${shop.id}, Name: ${shop.name}, Status: ${shop.subscriptionStatus}")
                                    println("        Is Valid UUID: ${isValidUUID(shop.id)}")
                                }

                                // Find active shop (with ACTIVE or TRIAL subscription)
                                val activeShop = shopList.find { shop ->
                                    shop.subscriptionStatus.equals("ACTIVE", ignoreCase = true) ||
                                            shop.subscriptionStatus.equals("TRIAL", ignoreCase = true)
                                }

                                if (activeShop != null) {
                                    // CRITICAL: Save the actual shop UUID, not "shop_1"
                                    preferenceManager.saveCurrentShopId(activeShop.id)  // This should be the UUID
                                    preferenceManager.saveCurrentShopName(activeShop.name)
                                    preferenceManager.saveCurrentShopUuid(activeShop.id)  // This should also be the UUID
                                    preferenceManager.saveHasShop(true)

                                    // Save subscription info
                                    preferenceManager.saveSubscriptionStatus(activeShop.subscriptionStatus.uppercase())
                                    preferenceManager.saveSubscriptionExpiry(activeShop.subscriptionExpiry ?: "")
                                    preferenceManager.saveSubscriptionType(activeShop.subscriptionType ?: "")

                                    println("✅ Active shop found: ${activeShop.name}")
                                    println("✅ Shop UUID saved: ${activeShop.id}")
                                    println("✅ Is Valid UUID: ${isValidUUID(activeShop.id)}")
                                    println("✅ Subscription status saved: ${activeShop.subscriptionStatus}")
                                } else if (shopList.isNotEmpty()) {
                                    // No active shop, select the first one
                                    val firstShop = shopList.first()
                                    preferenceManager.saveCurrentShopId(firstShop.id)
                                    preferenceManager.saveCurrentShopName(firstShop.name)
                                    preferenceManager.saveCurrentShopUuid(firstShop.id)
                                    preferenceManager.saveHasShop(true)
                                    preferenceManager.saveSubscriptionStatus("INACTIVE")

                                    println("⚠️ No active shop found, selected first shop: ${firstShop.name}")
                                    println("⚠️ Shop UUID: ${firstShop.id}")
                                } else {
                                    println("⚠️ No shops found for user")
                                    preferenceManager.saveHasShop(false)
                                    preferenceManager.saveSubscriptionStatus("INACTIVE")
                                }

                                preferenceManager.debugSubscriptionInfo()
                            }
                            is Resource.Error -> {
                                println("⚠️ Failed to get shops from database: ${shopsResult.message}")
                            }
                            is Resource.Loading -> {
                                println("⏳ Loading shops from database...")
                            }
                        }

                        navigateToMainActivity()
                    }

                    is Resource.Error -> {
                        println("❌ Failed to load shops: ${result.message}")
                        preferenceManager.saveSubscriptionStatus("INACTIVE")
                        navigateToMainActivity()
                    }

                    is Resource.Loading -> {
                        println("⏳ Loading shops from API...")
                    }
                }

            } catch (e: Exception) {
                println("❌ Error loading shops: ${e.message}")
                e.printStackTrace()
                preferenceManager.saveSubscriptionStatus("INACTIVE")
                navigateToMainActivity()
            } finally {
                isShopsLoading = false
                showLoading(false)
            }
        }
    }

    private fun isValidUUID(uuid: String): Boolean {
        return try {
            java.util.UUID.fromString(uuid)
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }

    /**
     * Check and refresh shops when app is reopened and user is already logged in
     */
    private fun checkAndRefreshShopsBeforeMain() {
        lifecycleScope.launch {
            try {
                println("🔄 Checking shops for already logged in user...")

                // Refresh shops to get latest subscription status
                val result = shopRepository.refreshShops()

                when (result) {
                    is Resource.Success -> {
                        val shopsResult = shopRepository.getShops()
                        when (shopsResult) {
                            is Resource.Success -> {
                                val shopList = shopsResult.data ?: emptyList()
                                val activeShop = shopList.find { shop ->
                                    shop.subscriptionStatus.equals("ACTIVE", ignoreCase = true) ||
                                            shop.subscriptionStatus.equals("TRIAL", ignoreCase = true)
                                }

                                if (activeShop != null) {
                                    preferenceManager.saveSubscriptionStatus(activeShop.subscriptionStatus.uppercase())
                                    preferenceManager.saveSubscriptionExpiry(activeShop.subscriptionExpiry ?: "")
                                    println("✅ Subscription refreshed: ${activeShop.subscriptionStatus}")
                                }
                            }
                            is Resource.Error -> {
                                println("⚠️ Failed to get shops: ${shopsResult.message}")
                            }
                            is Resource.Loading -> {
                                // Handle loading state if needed
                                println("⏳ Loading shops...")
                            }
                        }
                    }
                    is Resource.Error -> {
                        println("❌ Error refreshing shops: ${result.message}")
                    }
                    is Resource.Loading -> {
                        println("⏳ Refreshing shops...")
                    }
                }
            } catch (e: Exception) {
                println("❌ Error refreshing shops: ${e.message}")
            }

            // Navigate to MainActivity
            navigateToMainActivity()
        }
    }

    private fun saveUserData(authData: LoginViewModel.DomainAuthData) {
        println("🎉 LoginSuccess - Saving user data with PreferenceManager")

        val userName = authData.user.name
        val displayName = if (userName.isNotEmpty()) userName else authData.user.email

        // Split name into first and last for full data saving
        val parts = displayName.split(" ", limit = 2)
        val firstName = parts.getOrNull(0) ?: displayName
        val lastName = parts.getOrNull(1) ?: ""

        // Use existing PreferenceManager methods
        preferenceManager.saveAuthToken(authData.token)
        println("✅ Token saved: ${authData.token.take(20)}...")

        // Save user full data using existing method
        preferenceManager.saveUserFullData(
            userId = authData.user.id,
            email = authData.user.email,
            firstName = firstName,
            lastName = lastName,
            phone = authData.user.phone
        )

        preferenceManager.saveUserRole(authData.user.role)
        preferenceManager.setLastLoginTimestamp(System.currentTimeMillis())
        preferenceManager.setUserLoggedIn(true)

        println("✅ User data saved successfully")
    }

    private fun showWelcomeMessage(userName: String) {
        val welcomeMessage = if (userName.isNotEmpty()) {
            getString(R.string.welcome_back_name, userName)
        } else {
            getString(R.string.welcome)
        }
        Toast.makeText(this, welcomeMessage, Toast.LENGTH_SHORT).show()
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun trackLoginAttempt(status: String, method: String) {
        println("📊 Login attempt: $status, method: $method")
    }

    private fun clearOldShopData() {
        val currentShopId = preferenceManager.getCurrentShopId()
        val currentShopUuid = preferenceManager.getCurrentShopUuid()

        if (currentShopId == "shop_1" || currentShopUuid == "shop_1") {
            println("⚠️ Clearing old shop data with invalid ID: $currentShopId")
            preferenceManager.clearCurrentShop()
            preferenceManager.saveHasShop(false)
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        btnLogin.isEnabled = !show
        btnLogin.text = if (show) getString(R.string.logging_in) else getString(R.string.sign_in)

        etEmail.isEnabled = !show
        etPhone.isEnabled = !show
        etPassword.isEnabled = !show
        rbEmail.isEnabled = !show
        rbPhone.isEnabled = !show
        tvSignUp.isEnabled = !show
        tvForgotPassword.isEnabled = !show
    }

    private fun showError(message: String) {
        tvError.text = message
        tvError.visibility = View.VISIBLE

        errorRunnable?.let { tvError.removeCallbacks(it) }

        errorRunnable = Runnable {
            if (!isFinishing && !isDestroyed) {
                tvError.visibility = View.GONE
            }
        }
        tvError.postDelayed(errorRunnable, 5000)

        tvError.animate()
            .translationXBy(10f)
            .setDuration(100)
            .withEndAction {
                tvError.animate()
                    .translationXBy(-20f)
                    .setDuration(100)
                    .withEndAction {
                        tvError.animate()
                            .translationXBy(10f)
                            .setDuration(100)
                            .start()
                    }
                    .start()
            }
            .start()
    }

    private fun clearErrors() {
        errorRunnable?.let { tvError.removeCallbacks(it) }
        tvError.visibility = View.GONE
        etEmail.error = null
        etPhone.error = null
        etPassword.error = null
    }

    override fun onBackPressed() {
        if (viewModel.loginState.value is LoginViewModel.LoginState.Loading || isShopsLoading) {
            return
        }

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.exit_app_title))
            .setMessage(getString(R.string.exit_app_message))
            .setPositiveButton(getString(R.string.exit)) { _, _ ->
                super.onBackPressed()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        errorRunnable?.let { tvError.removeCallbacks(it) }
        viewModel.resetState()
    }
}