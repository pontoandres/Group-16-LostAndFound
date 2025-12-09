package com.example.lostandfound.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import com.example.lostandfound.model.LostItem

val Context.itemDataStore by preferencesDataStore("lost_items_cache")

object ItemCache {

    private val ITEMS_KEY = stringPreferencesKey("cached_items")
    private const val EXPIRY_MS = 14L * 24L * 60L * 60L * 1000L
    suspend fun saveItem(context: Context, item: LostItem) {
        val now = System.currentTimeMillis()
        val updatedItem = item.copy(viewedAt = now)

        val existing = loadAll(context).toMutableList()

        val threshold = now - EXPIRY_MS
        existing.removeAll { it.viewedAt < threshold }

        existing.removeAll { it.id == updatedItem.id }

        existing.add(0, updatedItem)

        val limited = existing.take(10)

        val json = Json.encodeToString(existing)
        context.itemDataStore.edit { prefs ->
            prefs[ITEMS_KEY] = json
        }
    }

    suspend fun loadAll(context: Context): List<LostItem> {
        val prefs = context.itemDataStore.data.first()
        val json = prefs[ITEMS_KEY]
        return if (json != null) Json.decodeFromString(json) else emptyList()
    }

    suspend fun loadRecent(context: Context): List<LostItem> {
        val prefs = context.itemDataStore.data.first()
        val json = prefs[ITEMS_KEY]
        return if (json != null) Json.decodeFromString(json) else emptyList()
    }

    suspend fun clear(context: Context) {
        context.itemDataStore.edit { it.clear() }
    }
}
