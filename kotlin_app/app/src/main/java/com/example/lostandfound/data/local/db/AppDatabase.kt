package com.example.lostandfound.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.lostandfound.data.local.db.dao.BqCategoryDao
import com.example.lostandfound.data.local.db.entities.BqCategoryEntity
import com.example.lostandfound.data.local.db.dao.LostItemDao
import com.example.lostandfound.data.local.db.dao.NotificationDao
import com.example.lostandfound.data.local.db.entities.LostItemEntity
import com.example.lostandfound.data.local.db.dao.PendingClaimDao
import com.example.lostandfound.data.local.db.dao.PendingNotificationFetchDao
import com.example.lostandfound.data.local.db.entities.NotificationEntity
import com.example.lostandfound.data.local.db.entities.PendingClaim
import com.example.lostandfound.data.local.db.entities.PendingNotificationFetch

@Database(
    entities = [
        // Entities from old DB #1
        BqCategoryEntity::class,
        LostItemEntity::class,

        // Entities from old DB #2
        PendingClaim::class,
        PendingNotificationFetch::class,
        NotificationEntity::class
    ],
    version = 3, // ← BUMP VERSION
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun bqCategoryDao(): BqCategoryDao
    abstract fun lostItemDao(): LostItemDao

    abstract fun pendingClaimDao(): PendingClaimDao
    abstract fun pendingNotificationFetchDao(): PendingNotificationFetchDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                val inst = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lost_found_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = inst
                inst
            }
    }
}
