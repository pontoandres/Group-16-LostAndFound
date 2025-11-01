package com.example.lostandfound.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity for local storage of lost items
 * Purpose: Store lost items for offline availability and eventual sync
 */
@Entity(tableName = "lost_items_cache")
data class LostItemEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val description: String? = null,
    val location: String? = null,
    val category: String? = null,
    val imageUrl: String? = null,
    val lostAt: String? = null,
    val createdAt: String,
    val isClaimed: Boolean = false,
    val claimedById: String? = null,
    val claimedAt: String? = null,
    // Sync metadata
    val syncedAt: Long? = null, // null = pending sync, timestamp = already synced
    val updatedAt: Long = System.currentTimeMillis() // TTL tracking
)

