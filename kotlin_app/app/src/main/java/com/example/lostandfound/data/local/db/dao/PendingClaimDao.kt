package com.example.lostandfound.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.lostandfound.data.local.db.entities.PendingClaim

@Dao
interface PendingClaimDao {
    @Insert
    suspend fun insertClaim(claim: PendingClaim)

    @Query("SELECT * FROM pending_claims")
    suspend fun getAllPending(): List<PendingClaim>

    @Query("DELETE FROM pending_claims WHERE localId = :localId")
    suspend fun deleteClaim(localId: Int)
}
