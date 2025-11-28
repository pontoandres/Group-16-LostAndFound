package com.example.lostandfound.model

import co.touchlab.kermit.Message

data class Claim (
    val id: String,
    val userId: String,
    val itemId: String,
    val message: String,
    // val claimantEmail: String, //commented for DB integrity
    val code: String,
    val status: String = "PENDING", // or VERIFIED, REJECTED
    val uploadType: String? //
)