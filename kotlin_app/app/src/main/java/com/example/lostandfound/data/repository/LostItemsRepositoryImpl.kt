package com.example.lostandfound.data.repository

import com.example.lostandfound.SupabaseProvider
import com.example.lostandfound.model.LostItem
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.serialization.json.Json
import java.io.File
import java.util.UUID

class LostItemsRepositoryImpl : LostItemsRepository {
    
    private val supabase = SupabaseProvider.client
    private val json = Json { ignoreUnknownKeys = true }
    
    override suspend fun createLostItem(
        title: String,
        description: String?,
        location: String?,
        category: String?,
        lostAt: String?,
        imageFile: File?
    ): Result<LostItem> = runCatching {
        val user = supabase.auth.currentUserOrNull() 
            ?: error("No hay sesión activa")
        
        // Upload image if provided
        val imageUrl = if (imageFile != null) {
            uploadImage(imageFile, user.id).getOrThrow()
        } else null
        
        // Create lost item data
        val lostItemData = mapOf(
            "user_id" to user.id,
            "title" to title.trim(),
            "description" to description?.trim(),
            "location" to location?.trim(),
            "category" to category?.trim(),
            "lost_at" to lostAt,
            "image_url" to imageUrl
        )
        
        // Insert into database
        val response = supabase.from("lost_items").insert(lostItemData).select().single()
        val data = response.decodeToString()
        
        json.decodeFromString<LostItem>(data)
    }
    
    override suspend fun uploadImage(imageFile: File, userId: String): Result<String> = runCatching {
        val timestamp = System.currentTimeMillis()
        val fileName = "lost_${userId}_${timestamp}.jpg"
        val path = "lost/$userId/$fileName"
        
        // Upload to Supabase Storage
        supabase.storage.from("lost-items").upload(
            path = path,
            file = imageFile
        )
        
        // Get public URL
        supabase.storage.from("lost-items").getPublicUrl(path)
    }
    
    override suspend fun getLostItems(): Result<List<LostItem>> = runCatching {
        val response = supabase.from("lost_items")
            .select()
            .order("created_at", ascending = false)
        
        val data = response.decodeToString()
        json.decodeFromString<List<LostItem>>(data)
    }
}
