package com.example.lostandfound.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    val id: String,
    val name: String,
    @SerialName("university_id") val universityId: String,
    @SerialName("avatar_url") val avatarUrl: String? = null
)
