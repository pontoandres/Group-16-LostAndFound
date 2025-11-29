package com.example.lostandfound.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.lostandfound.data.local.db.entities.NotificationEntity

@Dao
interface NotificationDao {

    @Query("SELECT * FROM notifications ORDER BY createdAt DESC")
    suspend fun getAll(): List<NotificationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<NotificationEntity>)

    @Query("DELETE FROM notifications")
    suspend fun clear()
}
