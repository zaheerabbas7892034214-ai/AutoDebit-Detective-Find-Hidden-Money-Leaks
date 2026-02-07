package com.zaheer.autodebitdetective.data.repository

import android.content.Context
import com.zaheer.autodebitdetective.data.local.dao.TransactionDao
import com.zaheer.autodebitdetective.data.local.entity.TransactionEntity
import com.zaheer.autodebitdetective.domain.model.Transaction
import com.zaheer.autodebitdetective.domain.usecase.ScanSMSUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SMSRepository(
    private val context: Context,
    private val transactionDao: TransactionDao
) {

    fun getAllTransactions(): Flow<List<Transaction>> {
        return transactionDao.getAllTransactions().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getTransactionsSince(startTimestamp: Long): Flow<List<Transaction>> {
        return transactionDao.getTransactionsSince(startTimestamp).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun scanAndSaveTransactions(): Result<List<Transaction>> = withContext(Dispatchers.IO) {
        try {
            val scanSMSUseCase = ScanSMSUseCase(context)
            val result = scanSMSUseCase()

            if (result.isSuccess) {
                val transactions = result.getOrNull() ?: emptyList()
                
                val existingHashes = getExistingHashes()
                
                val newTransactions = transactions.filter { txn ->
                    !existingHashes.contains(txn.rawSnippetHash)
                }

                if (newTransactions.isNotEmpty()) {
                    val entities = newTransactions.map { it.toEntity() }
                    transactionDao.insertTransactions(entities)
                }

                Result.success(newTransactions)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Failed to scan SMS"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun getExistingHashes(): Set<String> = withContext(Dispatchers.IO) {
        kotlinx.coroutines.flow.first(transactionDao.getAllTransactions())
            .map { it.rawSnippetHash }
            .toSet()
    }

    suspend fun insertTransaction(transaction: Transaction): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            transactionDao.insertTransaction(transaction.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAllTransactions(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            transactionDao.deleteAllTransactions()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun TransactionEntity.toDomain(): Transaction {
        return Transaction(
            id = id,
            timestamp = timestamp,
            amount = amount,
            merchant = merchant,
            rawSnippetHash = rawSnippetHash,
            source = source
        )
    }

    private fun Transaction.toEntity(): TransactionEntity {
        return TransactionEntity(
            id = id,
            timestamp = timestamp,
            amount = amount,
            merchant = merchant,
            rawSnippetHash = rawSnippetHash,
            source = source
        )
    }
}
