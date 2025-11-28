package com.example.lostandfound.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.lostandfound.SupabaseProvider
import com.example.lostandfound.data.local.db.AppDatabase
import com.example.lostandfound.data.local.db.entities.ClaimUploadOff
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class ConnectivityReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        Log.d("ConnectivityDebug", "onReceive triggered. Intent: ${intent?.action}")
        if (context.isOnline()) {
            Log.d("ConnectivityDebug", "Device is online. Starting sync...")

            CoroutineScope(Dispatchers.IO).launch {
                val dao = AppDatabase.getDatabase(context).pendingClaimDao()
                val pendingClaims = dao.getAllPending()
                Log.d("ConnectivityDebug", "Found ${pendingClaims.size} pending claims.")

                for (claim in pendingClaims) {
                    try {
                        Log.d("ConnectivityDebug", "Uploading claim ${claim.localId} w uploadType = ${claim.uploadType}")
                        SupabaseProvider.client.from("claims").insert(
                            ClaimUploadOff(
                                userId = claim.userId,
                                itemId = claim.itemId,
                                message = claim.message,
                                code = claim.code,
                                status = claim.status,
                                createdAt = claim.createdAt,
                                uploadType = claim.uploadType
                            )
                        )

                        dao.deleteClaim(claim.localId)
                        Log.d("ConnectivityDebug", "Deleted claim ${claim.localId}")
                    } catch (e: Exception) {
                        Log.e("ConnectivityDebug", "Failed to upload claim ${claim.localId}", e)
                    }
                }
            }
        } else {
            Log.d("ConnectivityDebug", "Device is offline.")
        }
    }
}