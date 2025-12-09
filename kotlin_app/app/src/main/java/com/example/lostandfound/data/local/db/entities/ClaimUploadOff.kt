package com.example.lostandfound.data.local.db.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ClaimUploadOff(
    @SerialName("user_id") val userId: String,
    @SerialName("item_id") val itemId: String?,
    val message: String,
    @SerialName("verification_code") val code: String,
    val status: String = "PENDING",
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("upload_type") val uploadType: String? = null
)
