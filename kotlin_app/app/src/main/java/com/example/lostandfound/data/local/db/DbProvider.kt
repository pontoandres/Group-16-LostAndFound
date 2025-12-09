package com.example.lostandfound.data.local.db

import android.content.Context
import androidx.room.Room
import com.example.lostandfound.data.local.db.AppDatabase

object DbProvider {
    @Volatile private var instance: AppDatabase? = null

    fun get(context: Context): AppDatabase =
        instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "unifind.db"
            ).build().also { instance = it }
        }
}