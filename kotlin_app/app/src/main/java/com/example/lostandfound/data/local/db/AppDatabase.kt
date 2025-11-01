package com.example.lostandfound.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.lostandfound.data.local.db.dao.PendingClaimDao
import com.example.lostandfound.data.local.db.entities.PendingClaim

@Database(entities = [PendingClaim::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun pendingClaimDao(): PendingClaimDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lost_found_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
