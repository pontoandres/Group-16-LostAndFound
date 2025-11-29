package com.example.lostandfound.data.repository

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.lostandfound.SupabaseProvider
import com.example.lostandfound.data.local.DbProvider
import com.example.lostandfound.data.local.LostItemDao
import com.example.lostandfound.data.local.LostItemEntity
import com.example.lostandfound.model.LostItem
import com.example.lostandfound.workers.enqueueLostItemSync
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.filter.*
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

// ===== TECHNIQUE #4: RXJAVA =====
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.runBlocking

class LostItemsRepositoryImpl(
    private val context: Context,
    private val dao: LostItemDao = DbProvider.get(context).lostItemDao(),
    private val ttlMs: Long = 5 * 60 * 1000L // 5 minutes TTL
) : LostItemsRepository {

    private val supabase = SupabaseProvider.client
    private val TAG = "LostItemsRepo"

    // Si luego volvemos a subir imágenes, ajusta el nombre real del bucket
    private val BUCKET_NAME = "lost-items"

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun createLostItem(
        title: String,
        description: String?,
        location: String?,
        category: String?,
        lostAt: String?,
        imageFile: File?
    ): Result<LostItem> = withContext(Dispatchers.IO) {
        runCatching {
            val user = supabase.auth.currentUserOrNull()
                ?: error("No hay sesión activa")

            Log.d(TAG, "Creating lost item for user: ${user.id}")

            val itemId = UUID.randomUUID().toString()
            val createdAt = java.time.Instant.now().toString()

            // Upload image if provided and online (otherwise queue for later)
            val imageUrl = if (imageFile != null) {
                Log.d(TAG, "Uploading image: ${imageFile.absolutePath}")
                // Try upload, but don't fail if offline - image will be synced later
                uploadImage(imageFile, user.id).getOrNull()
            } else {
                Log.d(TAG, "No image provided")
                null
            }

            // Always save locally first (offline-first approach)
            val entity = LostItemEntity(
                id = itemId,
                userId = user.id,
                title = title.trim(),
                description = description?.trim(),
                location = location?.trim(),
                category = category?.trim(),
                imageUrl = imageUrl,
                lostAt = lostAt,
                createdAt = createdAt,
                isClaimed = false,
                syncedAt = null, // Mark as pending sync
                updatedAt = System.currentTimeMillis()
                // isFavorite y campos de favorito usan sus defaults (false/null/null)
            )

            // Save to local database
            dao.upsert(entity)
            Log.d(TAG, "Saved lost item locally: $itemId")

            // Try to sync to remote (if online)
            try {
                val payload = mapOf(
                    "user_id" to user.id,
                    "title" to title.trim(),
                    "description" to description?.trim(),
                    "location" to location?.trim(),
                    "category" to category?.trim(),
                    "lost_at" to lostAt,
                    "image_url" to imageUrl,
                    "created_at" to createdAt
                )

                Log.d(TAG, "Attempting to sync to remote: $payload")
                supabase.postgrest["lost_items"].insert(payload)
                Log.d(TAG, "Sync successful - marked as synced")

                // Mark as synced
                dao.markAsSynced(itemId, System.currentTimeMillis())
            } catch (e: Exception) {
                Log.w(TAG, "Sync failed (likely offline), queuing for later: ${e.message}")
                // Queue sync worker for eventual connectivity
                enqueueLostItemSync(context, itemId)
            }

            // Return local object for UI refresh
            entity.toLostItem()
        }
    }

    /**
     * Extension function to convert LostItemEntity to LostItem model
     */
    private fun LostItemEntity.toLostItem(): LostItem = LostItem(
        id = id,
        userId = userId,
        title = title,
        description = description,
        location = location,
        category = category,
        imageUrl = imageUrl,
        lostAt = lostAt,
        createdAt = createdAt,
        isClaimed = isClaimed,
        claimedById = claimedById,
        claimedAt = claimedAt,
        // legacy fields se van con defaults
        isFavorite = isFavorite
    )

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

    // ===== TECHNIQUE #4: RXJAVA / RXKOTLIN =====
    // Uses multithreading: Explicitly controls threading with subscribeOn and observeOn
    // subscribeOn(Schedulers.io()) - work happens on I/O thread pool
    // observeOn(AndroidSchedulers.mainThread()) - results delivered to Main thread

    fun uploadImageWithRxJava(imageFile: File, userId: String): Single<String> {
        Log.d(TAG, "Uploading image using RxJava pattern")

        return Single.fromCallable {
            // This block runs on the I/O thread pool (specified by subscribeOn)
            Log.d(
                TAG,
                "RxJava: Uploading image to bucket: $BUCKET_NAME on thread: ${Thread.currentThread().name}"
            )

            // Use runBlocking to call suspend function from RxJava context
            runBlocking {
                val bucket = supabase.storage.from(BUCKET_NAME)
                val path = "lost/$userId/${System.currentTimeMillis()}.jpg"

                Log.d(TAG, "RxJava: Upload path: $path")
                bucket.upload(path, imageFile.readBytes()) {
                    contentType = ContentType.Image.JPEG
                    upsert = true
                }

                val publicUrl = bucket.publicUrl(path)
                Log.d(TAG, "RxJava: Upload successful, public URL: $publicUrl")
                publicUrl
            }
        }
            .subscribeOn(Schedulers.io()) // 1. Do the work on the I/O thread pool
            .observeOn(AndroidSchedulers.mainThread()) // 2. Deliver result to the Main thread
    }

    override suspend fun getLostItems(): Result<List<LostItem>> = withContext(Dispatchers.IO) {
        runCatching {
            // Check TTL: if local data is stale, try to refresh
            val ttlThreshold = System.currentTimeMillis() - ttlMs
            val staleItems = dao.getStaleItems(ttlThreshold)

            if (staleItems.isNotEmpty()) {
                Log.d(TAG, "Found ${staleItems.size} stale items, attempting refresh")
                try {
                    // Try to refresh from remote
                    refreshFromRemote()
                } catch (e: Exception) {
                    Log.w(TAG, "Refresh failed (likely offline), using cached data: ${e.message}")
                }
            } else {
                Log.d(TAG, "Local data is fresh, skipping remote fetch")
            }

            // Always return local data (offline-first)
            val entities = dao.getAll()
            val items = entities.map { it.toLostItem() }
            Log.d(TAG, "Retrieved ${items.size} lost items (from local cache)")
            items
        }
    }

    /**
     * Observe lost items as Flow (for reactive UI updates)
     */
    fun observeLostItems(): Flow<List<LostItem>> = dao.observeAll().map { entities ->
        entities.map { it.toLostItem() }
    }

    /**
     * Refresh data from remote and update local cache
     */
    suspend fun refreshFromRemote() = withContext(Dispatchers.IO) {
        Log.d(TAG, "Refreshing lost items from remote")
        val items = supabase.postgrest["lost_items"].select().decodeList<LostItem>()
        Log.d(TAG, "Retrieved ${items.size} items from remote")

        val entities = items.map { item ->
            // Conservamos el estado de favorito si ya existe en la DB local
            val localExisting = item.id?.let { dao.getById(it) }

            LostItemEntity(
                id = item.id ?: UUID.randomUUID().toString(),
                userId = item.userId,
                title = item.title,
                description = item.description,
                location = item.location,
                category = item.category,
                imageUrl = item.imageUrl,
                lostAt = item.lostAt,
                createdAt = item.createdAt,
                isClaimed = item.isClaimed,
                claimedById = item.claimedById,
                claimedAt = item.claimedAt,
                syncedAt = System.currentTimeMillis(), // Mark as synced
                updatedAt = System.currentTimeMillis(),
                isFavorite = localExisting?.isFavorite ?: false,
                favoriteUpdatedAt = localExisting?.favoriteUpdatedAt,
                favoriteSyncedAt = localExisting?.favoriteSyncedAt
            )
        }

        dao.upsertAll(entities)
        Log.d(TAG, "Updated local cache with ${entities.size} items")
    }

    /**
     * Sync pending items to remote (used by worker)
     */
    suspend fun syncPendingItems(): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val pending = dao.getPendingSync()
            Log.d(TAG, "Syncing ${pending.size} pending items")

            var syncedCount = 0
            for (entity in pending) {
                try {
                    val payload = mapOf(
                        "user_id" to entity.userId,
                        "title" to entity.title,
                        "description" to entity.description,
                        "location" to entity.location,
                        "category" to entity.category,
                        "lost_at" to entity.lostAt,
                        "image_url" to entity.imageUrl,
                        "created_at" to entity.createdAt
                    )

                    supabase.postgrest["lost_items"].insert(payload)
                    dao.markAsSynced(entity.id, System.currentTimeMillis())
                    syncedCount++
                    Log.d(TAG, "Synced item: ${entity.id}")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to sync item ${entity.id}: ${e.message}")
                    // Continue with next item
                }
            }

            Log.d(TAG, "Sync complete: $syncedCount/${pending.size} items synced")
            syncedCount
        }
    }

    /**
     * Observe user's lost items as Flow (for My Reports - reactive UI updates)
     * Uses Flow for multithreading strategy #3
     */
    override fun observeUserItems(userId: String): Flow<List<LostItem>> = 
        dao.observeByUserId(userId)
            .map { entities ->
                entities.map { it.toLostItem() }
            }
            .flowOn(Dispatchers.Default) // Process mapping on Default dispatcher

    /**
     * Refresh user's items from remote (with eventual connectivity support)
     */
    override suspend fun refreshUserItems(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            Log.d(TAG, "Refreshing user items from remote for userId: $userId")
            
            // Fetch from Supabase filtered by user_id
            val items = supabase.postgrest["lost_items"]
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<LostItem>()
            
            Log.d(TAG, "Retrieved ${items.size} items from remote for user")

            // Update local cache
            val entities = items.mapNotNull { item ->
                // Skip items with null id
                val itemId = item.id ?: return@mapNotNull null
                LostItemEntity(
                    id = itemId,
                    userId = item.userId,
                    title = item.title,
                    description = item.description,
                    location = item.location,
                    category = item.category,
                    imageUrl = item.imageUrl,
                    lostAt = item.lostAt,
                    createdAt = item.createdAt,
                    isClaimed = item.isClaimed,
                    claimedById = item.claimedById,
                    claimedAt = item.claimedAt,
                    syncedAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            }

            dao.upsertAll(entities)
            Log.d(TAG, "Updated local cache with ${entities.size} user items")
            Unit // Explicitly return Unit for Result<Unit>
        }
    }
    // =============== NUEVO: helpers para favoritos =================

    /**
     * Obtiene un ítem específico por ID desde la base local (cache).
     * Usado por ItemDetailActivity para saber si está en favoritos.
     */
    suspend fun getItemById(id: String): Result<LostItem> = withContext(Dispatchers.IO) {
        runCatching {
            val entity = dao.getById(id) ?: error("Item not found in local DB")
            entity.toLostItem()
        }
    }

    /**
     * Marca / desmarca un ítem como favorito (offline-first).
     * - Actualiza Room.
     * - (Opcionalmente) podrías encolar un worker de sync con Supabase.
     */

    suspend fun toggleFavorite(itemId: String, markFavorite: Boolean): Result<LostItem> =
        withContext(Dispatchers.IO) {
            runCatching {
                // 1. Cargar el item de Room
                val entity = dao.getById(itemId) ?: error("Item not found locally")

                // 2. Actualizar el flag de favorito en la base local
                val updatedEntity = entity.copy(
                    isFavorite = markFavorite,
                    updatedAt = System.currentTimeMillis()
                )
                dao.upsert(updatedEntity)

                // 3. Si se marcó como favorito, registrar el evento en Supabase (tabla favorites)
                if (markFavorite) {
                    val user = supabase.auth.currentUserOrNull()
                    if (user != null) {
                        try {
                            val payload = mapOf(
                                "user_id" to user.id,
                                "item_id" to itemId
                            )
                            // Insertamos un "evento" de favorito; si ya existía, Supabase aplica unique constraint
                            supabase.postgrest["favorites"].insert(payload)
                            Log.d(TAG, "Favorite event synced to Supabase for item=$itemId user=${user.id}")
                        } catch (e: Exception) {
                            Log.w(TAG, "Failed to sync favorite to Supabase: ${e.message}")
                            // No re-lanzamos: el favorito local sigue funcionando
                        }
                    } else {
                        Log.w(TAG, "toggleFavorite: no current user session, skipping Supabase sync")
                    }
                }

                // 4. Devolvemos el modelo de dominio para actualizar UI
                updatedEntity.toLostItem()
            }
        }

    /**
     * (Opcional) Flow de favoritos para una pantalla 'Mis favoritos'.
     */
    fun observeFavoriteItems(): Flow<List<LostItem>> =
        dao.observeFavorites().map { list -> list.map { it.toLostItem() } }

    // Guarda/actualiza un item en el cache local de Room a partir del modelo de dominio
    suspend fun upsertLocalFromModel(item: LostItem) = withContext(Dispatchers.IO) {
        val entity = LostItemEntity(
            id = item.id ?: UUID.randomUUID().toString(),
            userId = item.userId,
            title = item.title,
            description = item.description,
            location = item.location,
            category = item.category,
            imageUrl = item.imageUrl,
            lostAt = item.lostAt,
            createdAt = item.createdAt,
            isClaimed = item.isClaimed,
            claimedById = item.claimedById,
            claimedAt = item.claimedAt,
            isFavorite = item.isFavorite,
            syncedAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        dao.upsert(entity)
    }

}
