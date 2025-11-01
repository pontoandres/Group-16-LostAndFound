package com.example.lostandfound.data.remote

import com.example.lostandfound.SupabaseProvider
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.Serializable
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Serializable
data class BqCategoryRowDto(
    val category: String,
    val total: Long,
    val share: Double
)

class SupabaseBqApi {

    @OptIn(InternalSerializationApi::class) // 👈 para decodeList
    suspend fun fetchTopCategories(
        startIso: String,
        endIso: String,
        limit: Int = 10
    ): List<BqCategoryRowDto> {

        // 👇 Payload como JsonObject (NO Map)
        val payload: JsonObject = buildJsonObject {
            put("p_start", JsonPrimitive(startIso))
            put("p_end", JsonPrimitive(endIso))
            put("p_limit", JsonPrimitive(limit))
        }

        return SupabaseProvider.client.postgrest
            .rpc(
                function = "reports_per_category",
                parameters = payload
            )
            .decodeList<BqCategoryRowDto>()
    }
}
