package com.example.lostandfound.data.repository

import android.util.Log
import com.example.lostandfound.SupabaseProvider
import com.example.lostandfound.model.LostItem
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import java.io.File
import java.util.UUID

class LostItemsRepositoryImpl : LostItemsRepository {

    private val supabase = SupabaseProvider.client
    private val TAG = "LostItemsRepo"

    // Si luego volvemos a subir imágenes, ajusta el nombre real del bucket
    private val BUCKET_NAME = "lost-items"

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

        Log.d(TAG, "Creating lost item for user: ${user.id}")

        // Upload image if provided
        val imageUrl = if (imageFile != null) {
            Log.d(TAG, "Uploading image: ${imageFile.absolutePath}")
            uploadImage(imageFile, user.id).getOrThrow()
        } else {
            Log.d(TAG, "No image provided")
            null
        }

        val payload = mapOf(
            "user_id" to user.id,
            "title" to title.trim(),
            "description" to description?.trim(),
            "location" to location?.trim(),
            "category" to category?.trim(),
            "lost_at" to lostAt,
            "image_url" to imageUrl,
            "created_at" to java.time.Instant.now().toString()
        )

        Log.d(TAG, "Inserting data: $payload")
        supabase.postgrest["lost_items"].insert(payload)
        Log.d(TAG, "Insert OK en lost_items")

        // Return local object for UI refresh
        LostItem(
            id = UUID.randomUUID().toString(),
            userId = user.id,
            title = title.trim(),
            description = description?.trim(),
            location = location?.trim(),
            category = category?.trim(),
            imageUrl = imageUrl,
            lostAt = lostAt,
            createdAt = java.time.Instant.now().toString()
        )
    }

    // La dejamos lista por si luego volvemos a habilitar subir imagen
    override suspend fun uploadImage(imageFile: File, userId: String): Result<String> = runCatching {
        Log.d(TAG, "Uploading image to bucket: $BUCKET_NAME")
        val bucket = supabase.storage.from(BUCKET_NAME)
        val path = "lost/$userId/${System.currentTimeMillis()}.jpg"
        
        Log.d(TAG, "Upload path: $path")
        bucket.upload(path, imageFile.readBytes()) {
            contentType = ContentType.Image.JPEG
            upsert = true
        }
        
        val publicUrl = bucket.publicUrl(path)
        Log.d(TAG, "Upload successful, public URL: $publicUrl")
        publicUrl
    }

    override suspend fun getLostItems(): Result<List<LostItem>> = runCatching {
        Log.d(TAG, "Fetching lost items from database")
        val items = supabase.postgrest["lost_items"].select().decodeList<LostItem>()
        Log.d(TAG, "Retrieved ${items.size} lost items")
        items
    }
}
