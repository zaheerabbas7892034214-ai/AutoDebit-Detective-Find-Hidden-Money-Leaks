package com.zaheer.autodebitdetective.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.zaheer.autodebitdetective.data.local.AutoDebitDatabase
import com.zaheer.autodebitdetective.data.local.entity.RecurringEntity
import com.zaheer.autodebitdetective.data.local.entity.TransactionEntity
import com.zaheer.autodebitdetective.parser.RecurringDetector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest

class RecurringScanWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    private val database = AutoDebitDatabase.getDatabase(context)
    private val transactionDao = database.transactionDao()
    private val recurringDao = database.recurringDao()
    private val detector = RecurringDetector()
    
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting recurring scan...")
            
            val allTransactions = transactionDao.getAllTransactionsList()
            Log.d(TAG, "Analyzing ${allTransactions.size} transactions")
            
            val domainTransactions = allTransactions.map { entity ->
                com.zaheer.autodebitdetective.domain.model.Transaction(
                    id = entity.id,
                    timestamp = entity.timestamp,
                    amount = entity.amount,
                    merchant = entity.merchant,
                    rawSnippetHash = entity.rawSnippetHash,
                    source = entity.source
                )
            }
            
            val patterns = detector.detectRecurringPatterns(
                transactions = domainTransactions,
                minOccurrences = 2
            )
            
            Log.d(TAG, "Detected ${patterns.size} recurring patterns")
            
            val existingRecurring = recurringDao.getAllRecurringItems()
            val existingMerchants = existingRecurring.map { it.merchant.lowercase().trim() }.toSet()
            
            var newPatternsCount = 0
            var updatedPatternsCount = 0
            
            for (pattern in patterns) {
                val merchantKey = pattern.merchant.lowercase().trim()
                
                if (merchantKey in existingMerchants) {
                    val existing = existingRecurring.first { 
                        it.merchant.lowercase().trim() == merchantKey 
                    }
                    
                    val updated = existing.copy(
                        avgAmount = pattern.averageAmount,
                        lastChargeEpoch = pattern.lastChargeTimestamp,
                        nextPredictedEpoch = pattern.nextPredictedTimestamp,
                        cadenceType = pattern.cadenceType.name,
                        category = pattern.category.name
                    )
                    
                    if (updated != existing) {
                        recurringDao.update(updated)
                        updatedPatternsCount++
                    }
                } else {
                    val newRecurring = RecurringEntity(
                        merchant = pattern.merchant,
                        avgAmount = pattern.averageAmount,
                        cadenceType = pattern.cadenceType.name,
                        category = pattern.category.name,
                        lastChargeEpoch = pattern.lastChargeTimestamp,
                        nextPredictedEpoch = pattern.nextPredictedTimestamp,
                        isAlertEnabled = true
                    )
                    
                    recurringDao.insert(newRecurring)
                    newPatternsCount++
                }
            }
            
            Log.d(TAG, "Scan complete: $newPatternsCount new, $updatedPatternsCount updated")
            Result.success()
            
        } catch (e: Exception) {
            Log.e(TAG, "Error during recurring scan", e)
            Result.retry()
        }
    }
    
    companion object {
        private const val TAG = "RecurringScanWorker"
        const val WORK_NAME = "recurring_scan_work"
    }
}
