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

        // 🔴 IMPORTANTE: ignoramos imageFile por ahora.
        // Solo insertamos el registro (como cuando te funcionaba sin imagen).
        val payload = mapOf(
            "user_id" to user.id,
            "title" to title.trim(),
            "description" to description?.trim(),
            "location" to location?.trim(),
            "category" to category?.trim(),
            "lost_at" to lostAt,
            "image_url" to null // sin imagen por ahora
        )

        supabase.postgrest["lost_items"].insert(payload)
        Log.d(TAG, "Insert OK en lost_items (sin imagen)")

        // Devolvemos un objeto local para refrescar UI
        LostItem(
            id = UUID.randomUUID().toString(),
            userId = user.id,
            title = title.trim(),
            description = description?.trim(),
            location = location?.trim(),
            category = category?.trim(),
            imageUrl = null,
            lostAt = lostAt,
            createdAt = java.time.Instant.now().toString()
        )
    }

    // La dejamos lista por si luego volvemos a habilitar subir imagen
    override suspend fun uploadImage(imageFile: File, userId: String): Result<String> = runCatching {
        val bucket = supabase.storage.from(BUCKET_NAME)
        val path = "lost/$userId/${System.currentTimeMillis()}.jpg"
        bucket.upload(path, imageFile.readBytes()) {
            contentType = ContentType.Image.JPEG
            upsert = true
        }
        bucket.publicUrl(path)
    }

    override suspend fun getLostItems(): Result<List<LostItem>> = runCatching {
        supabase.postgrest["lost_items"].select().decodeList<LostItem>()
    }
}
