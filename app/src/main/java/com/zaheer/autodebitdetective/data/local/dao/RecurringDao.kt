package com.zaheer.autodebitdetective.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.zaheer.autodebitdetective.data.local.entity.RecurringEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringDao {
    @Query("SELECT * FROM recurring_items ORDER BY nextPredictedEpoch ASC")
    fun getAllRecurring(): Flow<List<RecurringEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurring(recurring: RecurringEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurringItems(items: List<RecurringEntity>)

    @Update
    suspend fun updateRecurring(recurring: RecurringEntity)

    @Delete
    suspend fun deleteRecurring(recurring: RecurringEntity)

    @Query("DELETE FROM recurring_items")
    suspend fun deleteAll()

    @Query("SELECT * FROM recurring_items WHERE id = :id")
    suspend fun getRecurringById(id: Long): RecurringEntity?

    @Query("SELECT * FROM recurring_items WHERE merchant = :merchant LIMIT 1")
    suspend fun getRecurringByMerchant(merchant: String): RecurringEntity?
}
