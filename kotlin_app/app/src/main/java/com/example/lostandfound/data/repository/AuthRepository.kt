package com.example.lostandfound.data.repository

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun register(email: String, password: String, name: String, uniId: String): Result<Unit>
}