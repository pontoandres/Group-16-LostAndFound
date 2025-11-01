package com.example.lostandfound.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.lostandfound.data.local.db.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class ConnectivityReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (context.isOnline()) {
            // Launch a coroutine to sync pending claims
            CoroutineScope(Dispatchers.IO).launch {
                val dao = AppDatabase.getDatabase(context).pendingClaimDao()
                val pendingClaims = dao.getAllPending()

                for (claim in pendingClaims) {
                    try {
                        dao.deleteClaim(claim.localId)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
}