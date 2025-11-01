package com.example.lostandfound.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bq_category_cache")
data class BqCategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String,
    val total: Long,
    val share: Double,
    val windowStart: Long,
    val windowEnd: Long,
    val refreshedAt: Long
)
