package com.devbrian.osebo.data.remote.dto.request

import com.google.gson.annotations.SerializedName

data class TransferApprovalRequest(
    @SerializedName("status")
    val status: String
) {
    companion object {
        const val APPROVED = "approved"
        const val REJECTED = "rejected"
    }
}
