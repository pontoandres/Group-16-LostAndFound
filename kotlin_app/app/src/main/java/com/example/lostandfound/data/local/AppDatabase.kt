package com.example.lostandfound.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [BqCategoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bqCategoryDao(): BqCategoryDao
}
