package com.example.lostandfound.model

import kotlinx.serialization.Serializable

@Serializable
data class Notification(
    val id: String,
    val user_id: String,
    val title: String,
    val message: String,
    val highlight: Boolean,
    val is_read: Boolean,
    val created_at: String
)