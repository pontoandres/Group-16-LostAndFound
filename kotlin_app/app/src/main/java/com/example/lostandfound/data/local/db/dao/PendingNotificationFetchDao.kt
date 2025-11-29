package com.example.lostandfound.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.lostandfound.data.local.db.entities.PendingNotificationFetch

@Dao
interface PendingNotificationFetchDao {

    @Insert
    suspend fun insertFetchRequest(request: PendingNotificationFetch)

    @Query("SELECT * FROM pending_notification_fetch")
    suspend fun getAll(): List<PendingNotificationFetch>

    @Query("DELETE FROM pending_notification_fetch WHERE id = :id")
    suspend fun delete(id: Int)

    @Query("DELETE FROM pending_notification_fetch")
    suspend fun clearAll()
}