package com.zaheer.autodebitdetective.data.repository

import com.zaheer.autodebitdetective.data.local.dao.RecurringDao
import com.zaheer.autodebitdetective.data.local.entity.RecurringEntity
import com.zaheer.autodebitdetective.domain.model.CadenceType
import com.zaheer.autodebitdetective.domain.model.RecurringItem
import com.zaheer.autodebitdetective.domain.model.Transaction
import com.zaheer.autodebitdetective.domain.usecase.DetectRecurringUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class RecurringRepository(
    private val recurringDao: RecurringDao
) {

    fun getAllRecurringItems(): Flow<List<RecurringItem>> {
        return recurringDao.getAllRecurring().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun detectAndSaveRecurringPatterns(transactions: List<Transaction>): Result<List<RecurringItem>> = withContext(Dispatchers.IO) {
        try {
            val detectUseCase = DetectRecurringUseCase()
            val result = detectUseCase(transactions)

            if (result.isSuccess) {
                val recurringItems = result.getOrNull() ?: emptyList()
                
                recurringItems.forEach { item ->
                    val existing = recurringDao.getRecurringByMerchant(item.merchant)
                    
                    if (existing != null) {
                        val updated = existing.copy(
                            avgAmount = item.avgAmount,
                            cadenceType = item.cadenceType.name,
                            category = item.category,
                            lastChargeEpoch = item.lastChargeEpoch,
                            nextPredictedEpoch = item.nextPredictedEpoch
                        )
                        recurringDao.updateRecurring(updated)
                    } else {
                        recurringDao.insertRecurring(item.toEntity())
                    }
                }

                Result.success(recurringItems)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Failed to detect patterns"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun insertRecurringItem(item: RecurringItem): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val id = recurringDao.insertRecurring(item.toEntity())
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateRecurringItem(item: RecurringItem): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            recurringDao.updateRecurring(item.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteRecurringItem(item: RecurringItem): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            recurringDao.deleteRecurring(item.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAllRecurringItems(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            recurringDao.deleteAll()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleAlert(itemId: Long, enabled: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val item = recurringDao.getRecurringById(itemId)
            if (item != null) {
                val updated = item.copy(isAlertEnabled = enabled)
                recurringDao.updateRecurring(updated)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Recurring item not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun RecurringEntity.toDomain(): RecurringItem {
        return RecurringItem(
            id = id,
            merchant = merchant,
            avgAmount = avgAmount,
            cadenceType = CadenceType.fromString(cadenceType),
            category = category,
            lastChargeEpoch = lastChargeEpoch,
            nextPredictedEpoch = nextPredictedEpoch,
            isAlertEnabled = isAlertEnabled
        )
    }

    private fun RecurringItem.toEntity(): RecurringEntity {
        return RecurringEntity(
            id = id,
            merchant = merchant,
            avgAmount = avgAmount,
            cadenceType = cadenceType.name,
            category = category,
            lastChargeEpoch = lastChargeEpoch,
            nextPredictedEpoch = nextPredictedEpoch,
            isAlertEnabled = isAlertEnabled
        )
    }
}
