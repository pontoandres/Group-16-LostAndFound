package com.example.lostandfound.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.lostandfound.data.local.db.DbProvider
import com.example.lostandfound.data.remote.SupabaseBqApi
import com.example.lostandfound.data.repository.BqRepository

class BqRefreshWorker(
    ctx: Context,
    params: WorkerParameters
) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        val ws = inputData.getLong("ws", 0L)
        val we = inputData.getLong("we", 0L)
        val startIso = inputData.getString("startIso") ?: return Result.failure()
        val endIso   = inputData.getString("endIso") ?: return Result.failure()
        val limit    = inputData.getInt("limit", 10)

        val db = DbProvider.get(applicationContext)
        val repo = BqRepository(db.bqCategoryDao(), SupabaseBqApi())

        return try {
            repo.refresh(ws, we, startIso, endIso, limit)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
