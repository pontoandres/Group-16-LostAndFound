package com.example.lostandfound.workers

import android.content.Context
import androidx.work.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

private fun toIsoUtc(ms: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US)
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    return sdf.format(Date(ms))
}

fun enqueueBqRefreshLast30Days(context: Context, limit: Int = 10) {
    val now = System.currentTimeMillis()
    val ws  = now - 30L * 24 * 60 * 60 * 1000
    val startIso = toIsoUtc(ws)
    val endIso   = toIsoUtc(now)

    val data = workDataOf(
        "ws" to ws,
        "we" to now,
        "startIso" to startIso,
        "endIso" to endIso,
        "limit" to limit
    )

    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED) // solo si hay red
        .build()

    val req = OneTimeWorkRequestBuilder<BqRefreshWorker>()
        .setInputData(data)
        .setConstraints(constraints)
        .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
        .addTag("bq_refresh_last30")
        .build()

    WorkManager.getInstance(context)
        .enqueueUniqueWork("bq_refresh_last30", ExistingWorkPolicy.REPLACE, req)
}

/**
 * Enqueue sync worker for a specific lost item
 * Uses eventual connectivity: only runs when NetworkType.CONNECTED
 * Implements exponential backoff for retries
 */
fun enqueueLostItemSync(context: Context, itemId: String) {
    val data = workDataOf("itemId" to itemId)

    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED) // Only run when online
        .build()

    val req = OneTimeWorkRequestBuilder<LostItemSyncWorker>()
        .setInputData(data)
        .setConstraints(constraints)
        .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
        .addTag("lost_item_sync")
        .build()

    WorkManager.getInstance(context)
        .enqueue(req)
    
    android.util.Log.d("LostItemSync", "Queued sync for item: $itemId")
}

/**
 * Enqueue sync worker for all pending lost items
 */
fun enqueueLostItemSyncAll(context: Context) {
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    val req = OneTimeWorkRequestBuilder<LostItemSyncWorker>()
        .setConstraints(constraints)
        .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
        .addTag("lost_item_sync_all")
        .build()

    WorkManager.getInstance(context)
        .enqueueUniqueWork("lost_item_sync_all", ExistingWorkPolicy.KEEP, req)
    
    android.util.Log.d("LostItemSync", "Queued sync for all pending items")
}