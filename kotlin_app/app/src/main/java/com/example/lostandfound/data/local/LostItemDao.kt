package com.example.lostandfound.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO for lost items local storage
 * Supports TTL (Time To Live) for cache freshness
 */
@Dao
interface LostItemDao {
    
    /**
     * Observe all lost items (for offline feed)
     * Returns Flow for reactive UI updates
     */
    @Query("SELECT * FROM lost_items_cache ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<LostItemEntity>>
    
    /**
     * Get all items, including pending sync items
     */
    @Query("SELECT * FROM lost_items_cache ORDER BY updatedAt DESC")
    suspend fun getAll(): List<LostItemEntity>
    
    /**
     * Get items pending sync (syncedAt IS NULL)
     */
    @Query("SELECT * FROM lost_items_cache WHERE syncedAt IS NULL ORDER BY updatedAt ASC")
    suspend fun getPendingSync(): List<LostItemEntity>
    
    /**
     * Get items that need refresh (based on TTL)
     * TTL: 5 minutes (300000 ms)
     */
    @Query("SELECT * FROM lost_items_cache WHERE updatedAt < :ttlThreshold ORDER BY updatedAt ASC")
    suspend fun getStaleItems(ttlThreshold: Long): List<LostItemEntity>
    
    /**
     * Get a specific item by ID
     */
    @Query("SELECT * FROM lost_items_cache WHERE id = :itemId LIMIT 1")
    suspend fun getById(itemId: String): LostItemEntity?
    
    /**
     * Insert or replace item (upsert)
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: LostItemEntity)
    
    /**
     * Insert multiple items
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<LostItemEntity>)
    
    /**
     * Mark item as synced
     */
    @Query("UPDATE lost_items_cache SET syncedAt = :timestamp WHERE id = :itemId")
    suspend fun markAsSynced(itemId: String, timestamp: Long)
    
    /**
     * Delete item by ID
     */
    @Query("DELETE FROM lost_items_cache WHERE id = :itemId")
    suspend fun deleteById(itemId: String)
    
    /**
     * Clear all items
     */
    @Query("DELETE FROM lost_items_cache")
    suspend fun clearAll()
}

