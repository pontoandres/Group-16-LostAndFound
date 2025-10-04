package com.example.lostandfound.data.repository

import com.example.lostandfound.model.LostItem
import java.io.File

interface LostItemsRepository {
    suspend fun createLostItem(
        title: String,
        description: String?,
        location: String?,
        category: String?,
        lostAt: String?,
        imageFile: File?
    ): Result<LostItem>
    
    suspend fun uploadImage(imageFile: File, userId: String): Result<String>
    
    suspend fun getLostItems(): Result<List<LostItem>>
}
