package com.example.lostandfound.data.local.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "pending_claims")
data class PendingClaim (
    @PrimaryKey(autoGenerate = true) val localId: Int = 0,

    @SerialName("user_id")
    val userId: String,

    @SerialName("item_id")
    val itemId: String?,


    val message: String,

    @SerialName("verification_code")
    val code: String,
    val status: String = "PENDING", // or VERIFIED, REJECTED

    @SerialName("created_at")
    val createdAt: String? = null,

    @SerialName("upload_type")
    val uploadType: String? = null
)