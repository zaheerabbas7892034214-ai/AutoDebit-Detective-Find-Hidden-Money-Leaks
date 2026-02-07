package com.zaheer.autodebitdetective.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.zaheer.autodebitdetective.data.local.entity.EntitlementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EntitlementDao {
    @Query("SELECT * FROM entitlement WHERE id = 1 LIMIT 1")
    fun getEntitlement(): Flow<EntitlementEntity?>

    @Query("SELECT * FROM entitlement WHERE id = 1 LIMIT 1")
    suspend fun getEntitlementSync(): EntitlementEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntitlement(entitlement: EntitlementEntity)

    @Update
    suspend fun updateEntitlement(entitlement: EntitlementEntity)
}
