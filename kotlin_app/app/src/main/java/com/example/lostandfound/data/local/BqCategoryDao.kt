package com.example.lostandfound.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BqCategoryDao {
    @Query("SELECT * FROM bq_category_cache WHERE windowStart = :ws AND windowEnd = :we ORDER BY total DESC, category ASC")
    fun observeForWindow(ws: Long, we: Long): Flow<List<BqCategoryEntity>>

    @Query("DELETE FROM bq_category_cache WHERE windowStart = :ws AND windowEnd = :we")
    suspend fun clearWindow(ws: Long, we: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(rows: List<BqCategoryEntity>)
}
