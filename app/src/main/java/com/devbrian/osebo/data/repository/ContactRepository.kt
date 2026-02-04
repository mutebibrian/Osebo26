package com.devbrian.osebo.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.devbrian.osebo.data.ApiClient
import com.devbrian.osebo.data.ApiService
import com.devbrian.osebo.models.ApiResponse
import com.devbrian.osebo.models.contact.*
import dagger.hilt.android.qualifiers.ApplicationContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class ContactRepository @Inject constructor(
    @ApplicationContext context: Context
) {
    private val appContext = context.applicationContext

    // SharedPreferences
    private val prefs: SharedPreferences by lazy {
        appContext.getSharedPreferences("OseboPrefs", Context.MODE_PRIVATE)
    }

    // Helper methods for SharedPreferences
    private fun getAuthToken(): String = prefs.getString("auth_token", "") ?: ""
    private fun getCurrentShopId(): String = prefs.getString("current_shop_id", "") ?: ""
    private fun getUserId(): String = prefs.getString("user_id", "") ?: ""

    // Network check (simplified - you can implement proper network check later)
    private fun isNetworkAvailable(): Boolean {
        // For now, return true - implement proper network check if needed
        return true
    }

    // Helper to get API service with auth header
    private fun createServiceWithAuth(): ApiService {
        val token = getAuthToken()
        return ApiClient.createWithAuth(token)
    }

    // Helper to get API service without auth (for public endpoints)
    private fun createService(): ApiService {
        return ApiClient.create()
    }

    // ==================== SUPPORT TICKETS ====================

    fun createSupportTicket(
        category: String,
        subject: String,
        message: String,
        priority: String = "normal",
        onSuccess: (SupportTicket) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!isNetworkAvailable()) {
            onError("No internet connection")
            return
        }

        val token = getAuthToken()
        val shopId = getCurrentShopId()

        if (token.isEmpty()) {
            onError("Please login to create a support ticket")
            return
        }

        val request = SupportTicketRequest(
            category = category,
            subject = subject,
            message = message,
            shopId = if (shopId.isNotEmpty()) shopId else null,
            priority = priority
        )

        val apiService = createServiceWithAuth()

        apiService.createSupportTicket(token, request).enqueue(object : Callback<SupportTicket> {
            override fun onResponse(call: Call<SupportTicket>, response: Response<SupportTicket>) {
                if (response.isSuccessful) {
                    response.body()?.let { onSuccess(it) } ?: run {
                        onError("Failed to create support ticket: Empty response")
                    }
                } else {
                    onError(getErrorMessage(response.code(), "Failed to create support ticket"))
                }
            }

            override fun onFailure(call: Call<SupportTicket>, t: Throwable) {
                onError("Network error: ${t.message ?: "Unknown error"}")
            }
        })
    }

    fun getSupportTickets(
        status: String? = null,
        onSuccess: (List<SupportTicket>) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!isNetworkAvailable()) {
            onError("No internet connection")
            return
        }

        val token = getAuthToken()
        val shopId = getCurrentShopId()
        val userId = getUserId()

        if (token.isEmpty()) {
            onError("Please login to view support tickets")
            return
        }

        val apiService = createServiceWithAuth()

        // Use the actual API method from your ApiService
        apiService.getSupportTickets(
            token,
            shopId = if (shopId.isNotEmpty()) shopId else null,
            status = status,
            userId = if (userId.isNotEmpty()) userId else null
        ).enqueue(object : Callback<List<SupportTicket>> {
            override fun onResponse(call: Call<List<SupportTicket>>, response: Response<List<SupportTicket>>) {
                if (response.isSuccessful) {
                    response.body()?.let { onSuccess(it) } ?: run {
                        onSuccess(emptyList())
                    }
                } else {
                    onError(getErrorMessage(response.code(), "Failed to load support tickets"))
                }
            }

            override fun onFailure(call: Call<List<SupportTicket>>, t: Throwable) {
                onError("Network error: ${t.message ?: "Unknown error"}")
            }
        })
    }

    fun getTicketDetails(
        ticketId: String,
        onSuccess: (SupportTicketResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!isNetworkAvailable()) {
            onError("No internet connection")
            return
        }

        val token = getAuthToken()

        if (token.isEmpty()) {
            onError("Please login to view ticket details")
            return
        }

        val apiService = createServiceWithAuth()

        apiService.getTicketDetails(token, ticketId).enqueue(object : Callback<SupportTicketResponse> {
            override fun onResponse(call: Call<SupportTicketResponse>, response: Response<SupportTicketResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { onSuccess(it) } ?: run {
                        onError("Failed to load ticket details: Empty response")
                    }
                } else {
                    onError(getErrorMessage(response.code(), "Failed to load ticket details"))
                }
            }

            override fun onFailure(call: Call<SupportTicketResponse>, t: Throwable) {
                onError("Network error: ${t.message ?: "Unknown error"}")
            }
        })
    }

    fun addTicketMessage(
        ticketId: String,
        message: String,
        onSuccess: (TicketMessage) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!isNetworkAvailable()) {
            onError("No internet connection")
            return
        }

        val token = getAuthToken()

        if (token.isEmpty()) {
            onError("Please login to send message")
            return
        }

        val request = mapOf(
            "message" to message,
            "ticket_id" to ticketId
        )

        val apiService = createServiceWithAuth()

        apiService.addTicketMessage(token, ticketId, request).enqueue(object : Callback<TicketMessage> {
            override fun onResponse(call: Call<TicketMessage>, response: Response<TicketMessage>) {
                if (response.isSuccessful) {
                    response.body()?.let { onSuccess(it) } ?: run {
                        onError("Failed to send message: Empty response")
                    }
                } else {
                    onError(getErrorMessage(response.code(), "Failed to send message"))
                }
            }

            override fun onFailure(call: Call<TicketMessage>, t: Throwable) {
                onError("Network error: ${t.message ?: "Unknown error"}")
            }
        })
    }

    // ==================== FAQs ====================

    fun getFAQs(
        category: String? = null,
        language: String = "en",
        onSuccess: (List<FAQ>) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!isNetworkAvailable()) {
            // Return cached FAQs if available
            val cachedFAQs = getCachedFAQs()
            if (cachedFAQs.isNotEmpty()) {
                val filteredFAQs = if (category != null) {
                    cachedFAQs.filter { it.category.equals(category, ignoreCase = true) }
                } else {
                    cachedFAQs
                }
                onSuccess(filteredFAQs)
            } else {
                onError("No internet connection")
            }
            return
        }

        val apiService = createService()

        apiService.getFAQs(category, language).enqueue(object : Callback<List<FAQ>> {
            override fun onResponse(call: Call<List<FAQ>>, response: Response<List<FAQ>>) {
                if (response.isSuccessful) {
                    response.body()?.let { faqs ->
                        // Cache the FAQs
                        cacheFAQs(faqs)
                        val filteredFAQs = if (category != null) {
                            faqs.filter { it.category.equals(category, ignoreCase = true) }
                        } else {
                            faqs
                        }
                        onSuccess(filteredFAQs)
                    } ?: run {
                        // Try to return cached data
                        val cachedFAQs = getCachedFAQs()
                        val filteredFAQs = if (category != null) {
                            cachedFAQs.filter { it.category.equals(category, ignoreCase = true) }
                        } else {
                            cachedFAQs
                        }
                        if (filteredFAQs.isNotEmpty()) {
                            onSuccess(filteredFAQs)
                        } else {
                            onError("Failed to load FAQs: Empty response")
                        }
                    }
                } else {
                    // Try to return cached data
                    val cachedFAQs = getCachedFAQs()
                    val filteredFAQs = if (category != null) {
                        cachedFAQs.filter { it.category.equals(category, ignoreCase = true) }
                    } else {
                        cachedFAQs
                    }
                    if (filteredFAQs.isNotEmpty()) {
                        onSuccess(filteredFAQs)
                    } else {
                        onError(getErrorMessage(response.code(), "Failed to load FAQs"))
                    }
                }
            }

            override fun onFailure(call: Call<List<FAQ>>, t: Throwable) {
                // Try to return cached data
                val cachedFAQs = getCachedFAQs()
                val filteredFAQs = if (category != null) {
                    cachedFAQs.filter { it.category.equals(category, ignoreCase = true) }
                } else {
                    cachedFAQs
                }
                if (filteredFAQs.isNotEmpty()) {
                    onSuccess(filteredFAQs)
                } else {
                    onError("Network error: ${t.message ?: "Unknown error"}")
                }
            }
        })
    }

    // ==================== CONTACT INFORMATION ====================

    fun getContactInfo(
        onSuccess: (ContactInfo) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!isNetworkAvailable()) {
            // Return default contact info when offline
            onSuccess(getDefaultContactInfo())
            return
        }

        val apiService = createService()

        apiService.getContactInfo().enqueue(object : Callback<ContactInfo> {
            override fun onResponse(call: Call<ContactInfo>, response: Response<ContactInfo>) {
                if (response.isSuccessful) {
                    response.body()?.let { onSuccess(it) } ?: run {
                        // Return default contact info
                        onSuccess(getDefaultContactInfo())
                    }
                } else {
                    // Return default contact info
                    onSuccess(getDefaultContactInfo())
                }
            }

            override fun onFailure(call: Call<ContactInfo>, t: Throwable) {
                // Return default contact info
                onSuccess(getDefaultContactInfo())
            }
        })
    }

    // ==================== CONTACT FORM ====================


    // In ContactRepository.kt
    suspend fun getContactInfoSuspend(): ContactInfo {
        return suspendCoroutine { continuation ->
            getContactInfo(
                onSuccess = { contactInfo ->
                    continuation.resume(contactInfo)
                },
                onError = { error ->
                    continuation.resumeWithException(Exception(error))
                }
            )
        }
    }
    fun submitContactForm(
        name: String,
        email: String,
        subject: String,
        message: String,
        phone: String? = null,
        onSuccess: (ApiResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!isNetworkAvailable()) {
            onError("No internet connection")
            return
        }

        val shopId = getCurrentShopId()

        val request = ContactFormRequest(
            name = name,
            email = email,
            phone = phone,
            subject = subject,
            message = message,
            shopId = if (shopId.isNotEmpty()) shopId else null
        )

        val apiService = createService()

        apiService.sendSupportMessage(request).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { onSuccess(it) } ?: run {
                        onError("Failed to submit contact form: Empty response")
                    }
                } else {
                    onError(getErrorMessage(response.code(), "Failed to submit contact form"))
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                onError("Network error: ${t.message ?: "Unknown error"}")
            }
        })
    }

    // ==================== HELP CATEGORIES ====================

    fun getHelpCategories(
        onSuccess: (List<String>) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!isNetworkAvailable()) {
            // Return default categories when offline
            onSuccess(getDefaultHelpCategories())
            return
        }

        val apiService = createService()

        apiService.getHelpCategories().enqueue(object : Callback<List<String>> {
            override fun onResponse(call: Call<List<String>>, response: Response<List<String>>) {
                if (response.isSuccessful) {
                    response.body()?.let { onSuccess(it) } ?: run {
                        // Return default categories
                        onSuccess(getDefaultHelpCategories())
                    }
                } else {
                    // Return default categories
                    onSuccess(getDefaultHelpCategories())
                }
            }

            override fun onFailure(call: Call<List<String>>, t: Throwable) {
                // Return default categories
                onSuccess(getDefaultHelpCategories())
            }
        })
    }

    // ==================== PRIVATE HELPER METHODS ====================

    private fun getErrorMessage(code: Int, defaultMessage: String): String {
        return when (code) {
            400 -> "Invalid request data"
            401 -> "Session expired. Please login again"
            403 -> "You don't have permission to perform this action"
            404 -> "Resource not found"
            409 -> "Conflict: Resource already exists"
            422 -> "Validation error"
            429 -> "Too many requests. Please try again later"
            500 -> "Server error. Please try again later"
            502 -> "Bad gateway"
            503 -> "Service unavailable"
            504 -> "Gateway timeout"
            else -> defaultMessage
        }
    }

    private fun getDefaultContactInfo(): ContactInfo {
        return ContactInfo(
            email = "support@osebo.com",
            phone = "+1234567890",
            whatsapp = "+1234567890", // Add this line
            address = "123 Business St, City",
            workingHours = "Mon-Fri: 9AM - 6PM"
        )
    }

    private fun getDefaultHelpCategories(): List<String> {
        return listOf(
            "Account & Registration",
            "Subscription & Billing",
            "Inventory Management",
            "Sales & Invoicing",
            "Reports & Analytics",
            "User Roles & Permissions",
            "Technical Issues",
            "Feature Request",
            "Other"
        )
    }

    // Simple caching for FAQs
    private fun cacheFAQs(faqs: List<FAQ>) {
        val prefs = appContext.getSharedPreferences("OseboCache", Context.MODE_PRIVATE)
        val editor = prefs.edit()

        // Convert FAQs to JSON string (simplified caching)
        val faqJson = faqs.joinToString("|||") {
            "${it.id}|${it.category}|${it.question}|${it.answer}|${it.createdAt}|${it.updatedAt}"
        }

        editor.putString("cached_faqs", faqJson)
        editor.putLong("faqs_cache_time", System.currentTimeMillis())
        editor.apply()
    }

    private fun getCachedFAQs(): List<FAQ> {
        val prefs = appContext.getSharedPreferences("OseboCache", Context.MODE_PRIVATE)
        val cachedTime = prefs.getLong("faqs_cache_time", 0)
        val currentTime = System.currentTimeMillis()

        // Cache valid for 24 hours
        if (currentTime - cachedTime > 24 * 60 * 60 * 1000) {
            return emptyList()
        }

        val faqString = prefs.getString("cached_faqs", "") ?: ""
        if (faqString.isEmpty()) return emptyList()

        return try {
            faqString.split("|||").mapNotNull { part ->
                val fields = part.split("|", limit = 6)
                if (fields.size >= 6) {
                    FAQ(
                        id = fields[0],
                        category = fields[1],
                        question = fields[2],
                        answer = fields[3],
                        createdAt = fields[4],
                        updatedAt = fields[5]
                    )
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}