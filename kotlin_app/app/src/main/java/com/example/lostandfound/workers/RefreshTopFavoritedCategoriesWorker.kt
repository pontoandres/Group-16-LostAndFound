package com.example.lostandfound.workers

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.lostandfound.SupabaseProvider
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.Serializable

class RefreshTopFavoritedCategoriesWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val client = SupabaseProvider.client

        return try {
            val rows = client.postgrest["v_top_favorited_categories_30d"]
                .select()
                .decodeList<FavoritedCategoryRow>()

            // Por ahora solo lo dejamos registrado en logs;
            // el gráfico lo ves en Supabase Studio.
            Log.d("BQ_Favorites", "Top favorited categories (last 30d): $rows")

            Result.success()
        } catch (e: Exception) {
            Log.e("BQ_Favorites", "Error refreshing top favorited categories", e)
            Result.retry()
        }
    }
}

@Serializable
data class FavoritedCategoryRow(
    val category: String,
    val favorites_count: Long
)

fun enqueueBqRefreshTopFavoritedCategories(context: Context) {
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    val request = OneTimeWorkRequestBuilder<RefreshTopFavoritedCategoriesWorker>()
        .setConstraints(constraints)
        .build()

    WorkManager.getInstance(context).enqueueUniqueWork(
        "bq_refresh_top_favorited_categories_30d",
        ExistingWorkPolicy.REPLACE,
        request
    )
}
