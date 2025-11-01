package com.example.lostandfound.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [BqCategoryEntity::class, LostItemEntity::class],
    version = 2, // Incremented version for new entity
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bqCategoryDao(): BqCategoryDao
    abstract fun lostItemDao(): LostItemDao
}
