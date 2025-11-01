package com.example.lostandfound.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.lostandfound.data.local.DbProvider
import com.example.lostandfound.data.repository.LostItemsRepositoryImpl

/**
 * Worker for eventual connectivity: syncs pending lost items when connection is restored
 * Uses Constraints(NetworkType.CONNECTED) to only run when online
 * Implements exponential backoff via WorkRequest
 */
class LostItemSyncWorker(
    ctx: Context,
    params: WorkerParameters
) : CoroutineWorker(ctx, params) {

    private val TAG = "LostItemSyncWorker"

    override suspend fun doWork(): Result {
        val itemId = inputData.getString("itemId")
        
        if (itemId != null) {
            // Sync specific item
            return syncSingleItem(itemId)
        } else {
            // Sync all pending items
            return syncAllPending()
        }
    }

    private suspend fun syncSingleItem(itemId: String): Result {
        return try {
            Log.d(TAG, "Syncing single item: $itemId")
            val db = DbProvider.get(applicationContext)
            val repo = LostItemsRepositoryImpl(
                applicationContext,
                db.lostItemDao()
            )

            val result = repo.syncPendingItems()
            result.fold(
                onSuccess = { count ->
                    Log.d(TAG, "Sync successful: $count items synced")
                    Result.success()
                },
                onFailure = { error ->
                    Log.e(TAG, "Sync failed: ${error.message}")
                    Result.retry() // Retry with exponential backoff
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error during sync: ${e.message}", e)
            Result.retry()
        }
    }

    private suspend fun syncAllPending(): Result {
        return try {
            Log.d(TAG, "Syncing all pending items")
            val db = DbProvider.get(applicationContext)
            val repo = LostItemsRepositoryImpl(
                applicationContext,
                db.lostItemDao()
            )

            val result = repo.syncPendingItems()
            result.fold(
                onSuccess = { count ->
                    Log.d(TAG, "Sync complete: $count items synced")
                    Result.success()
                },
                onFailure = { error ->
                    Log.e(TAG, "Sync failed: ${error.message}")
                    Result.retry()
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error during sync: ${e.message}", e)
            Result.retry()
        }
    }
}

