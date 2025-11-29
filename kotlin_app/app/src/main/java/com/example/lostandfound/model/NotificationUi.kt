package com.example.lostandfound.model

data class NotificationUi(
    val id: String,
    val title: String,
    val message: String,
    val highlight: Boolean,
    val isRead: Boolean,
    val createdAt: String
)