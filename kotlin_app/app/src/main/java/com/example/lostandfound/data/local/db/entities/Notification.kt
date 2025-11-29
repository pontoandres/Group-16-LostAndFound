package com.example.lostandfound.data.local.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity (
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val message: String,
    val highlight: Boolean,
    val isRead: Boolean,
    val createdAt: String
)