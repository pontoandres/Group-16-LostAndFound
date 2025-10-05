package com.example.lostandfound.model

data class Claim (
    val id: String,
    val lostItemId: String,
    val claimantEmail: String,
    val code: String,
    val status: String = "PENDING" // or VERIFIED, REJECTED
)