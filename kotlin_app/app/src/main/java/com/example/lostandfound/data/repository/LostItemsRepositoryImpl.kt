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

        // 1) Subir imagen si existe
        val imageUrl: String? = if (imageFile != null && imageFile.exists()) {
            // usamos uploadImage()
            uploadImage(imageFile, user.id).getOrElse { e ->
                // Si falla la imagen, lanza error o deja null según tu UX.
                // Aquí prefiero lanzar para que el usuario sepa que falló subirla.
                throw IllegalStateException("No se pudo subir la imagen: ${e.message}", e)
            }
        } else {
            null
        }

        // 2) Insertar el registro con image_url (si subió)
        val payload = mapOf(
            "user_id" to user.id,
            "title" to title.trim(),
            "description" to description?.trim(),
            "location" to location?.trim(),
            "category" to category?.trim(),
            "lost_at" to lostAt,
            "image_url" to imageUrl // ← ahora sí guardamos la URL pública
        )

        supabase.postgrest["lost_items"].insert(payload)
        Log.d(TAG, "Insert OK en lost_items (image_url=${imageUrl ?: "null"})")

        // Devuelve un objeto local para refrescar UI
        LostItem(
            id = java.util.UUID.randomUUID().toString(),
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
