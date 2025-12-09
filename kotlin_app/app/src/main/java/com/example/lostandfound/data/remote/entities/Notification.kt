package com.example.lostandfound.data.remote.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NotificationRemote(
    val id: String,

    @SerialName("user_id")
    val userId: String,

    val title: String,
    val message: String,

    @SerialName("is_read")
    val isRead: Boolean = false,

    val highlight: Boolean = false,

    @SerialName("created_at")
    val createdAt: String
)