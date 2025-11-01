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

    suspend fun saveItem(context: Context, item: LostItem) {
        val existing = loadAll(context).toMutableList()

        existing.removeAll { it.id == item.id }

        existing.add(0, item)

        val limited = existing.take(10)

        val json = Json.encodeToString(limited)
        context.itemDataStore.edit { prefs ->
            prefs[ITEMS_KEY] = json
        }
    }

    suspend fun loadAll(context: Context): List<LostItem> {
        val prefs = context.itemDataStore.data.first()
        val json = prefs[ITEMS_KEY]
        return if (json != null) Json.decodeFromString(json) else emptyList()
    }

    suspend fun clear(context: Context) {
        context.itemDataStore.edit { it.clear() }
    }
}
