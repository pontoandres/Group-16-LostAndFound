package com.example.lostandfound.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class LostItem(
    val id: String,
    @SerialName("user_id")
    val userId: String,
    val title: String,
    val description: String? = null,
    val location: String? = null,
    val category: String? = null,
    @SerialName("image_url")
    val imageUrl: String? = null,
    @SerialName("lost_at")
    val lostAt: String? = null,
    @SerialName("created_at")
    val createdAt: String?,
    @SerialName("is_claimed")
    val isClaimed: Boolean = false,
    @SerialName("claimed_by_id")
    val claimedById: String? = null,
    @SerialName("claimed_at")
    val claimedAt: String? = null,
    // Legacy fields for backward compatibility
    val legacyName: String? = null,
    val legacyPostedBy: String? = null,
    val legacyImageRes: Int? = null
) {
    // Computed properties for backward compatibility
    fun getName(): String = legacyName ?: title
    fun getPostedBy(): String = legacyPostedBy ?: userId
    fun getImageRes(): Int = legacyImageRes ?: 0
}
