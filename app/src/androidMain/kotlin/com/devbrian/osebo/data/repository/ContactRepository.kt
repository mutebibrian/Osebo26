package com.devbrian.osebo.data.repository

import com.devbrian.osebo.models.contact.ContactInfo

interface ContactRepository {
    fun getContactInfo(
        onSuccess: (ContactInfo) -> Unit,
        onError: (String) -> Unit
    )

    fun sendSupportMessage(
        name: String,
        email: String,
        subject: String,
        message: String,
        phone: String? = null,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    )
}


