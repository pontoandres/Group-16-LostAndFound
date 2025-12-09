package com.example.lostandfound.data.repository

import com.example.lostandfound.data.local.db.dao.BqCategoryDao
import com.example.lostandfound.data.local.db.entities.BqCategoryEntity
import com.example.lostandfound.data.remote.SupabaseBqApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class BqRepository(
    private val dao: BqCategoryDao,
    private val api: SupabaseBqApi,
    private val ttlMs: Long = 5 * 60 * 1000 // 5 min
) {
    fun observe(ws: Long, we: Long): Flow<List<BqCategoryEntity>> =
        dao.observeForWindow(ws, we)

    suspend fun refreshIfNeeded(ws: Long, we: Long, startIso: String, endIso: String, limit: Int = 10) {
        // TTL simple: si no hay datos o son viejos, refresca
        // (Para simplificar: siempre refrescamos; si quieres TTL real, haz un query a 1 fila y compara refreshedAt)
        refresh(ws, we, startIso, endIso, limit)
    }

    suspend fun refresh(ws: Long, we: Long, startIso: String, endIso: String, limit: Int) = withContext(Dispatchers.IO) {
        val rows = api.fetchTopCategories(startIso, endIso, limit)
        dao.clearWindow(ws, we)
        val now = System.currentTimeMillis()
        dao.upsertAll(rows.map {
            BqCategoryEntity(
                category = it.category,
                total = it.total,
                share = it.share,
                windowStart = ws,
                windowEnd = we,
                refreshedAt = now
            )
        })
    }
}
