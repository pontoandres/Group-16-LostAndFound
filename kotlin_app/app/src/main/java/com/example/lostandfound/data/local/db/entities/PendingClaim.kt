package com.example.lostandfound.data.local.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import co.touchlab.kermit.Message

@Entity(tableName = "pending_claims")
data class PendingClaim (
    @PrimaryKey(autoGenerate = true) val localId: Int = 0,
    val userId: String,
    val itemId: String?,
    val message: String,
    val code: String,
    val status: String = "PENDING" // or VERIFIED, REJECTED
)