package com.zaheer.autodebitdetective.domain.usecase

import com.zaheer.autodebitdetective.domain.model.CadenceType
import com.zaheer.autodebitdetective.domain.model.RecurringItem
import com.zaheer.autodebitdetective.domain.model.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.abs

class DetectRecurringUseCase {

    suspend operator fun invoke(transactions: List<Transaction>): Result<List<RecurringItem>> = withContext(Dispatchers.Default) {
        try {
            val merchantGroups = transactions.groupBy { it.merchant }
            val recurringItems = mutableListOf<RecurringItem>()

            for ((merchant, txns) in merchantGroups) {
                if (txns.size < 2) continue

                val sortedTxns = txns.sortedBy { it.timestamp }
                val intervals = mutableListOf<Long>()
                
                for (i in 1 until sortedTxns.size) {
                    val interval = sortedTxns[i].timestamp - sortedTxns[i - 1].timestamp
                    intervals.add(interval)
                }

                val avgInterval = intervals.average().toLong()
                val cadenceType = determineCadenceType(avgInterval)

                if (cadenceType != CadenceType.UNKNOWN && isConsistentPattern(intervals, avgInterval)) {
                    val avgAmount = txns.map { it.amount }.average()
                    val lastCharge = sortedTxns.last().timestamp
                    val nextPredicted = lastCharge + avgInterval
                    val category = categorizeByMerchant(merchant)

                    val recurringItem = RecurringItem(
                        merchant = merchant,
                        avgAmount = avgAmount,
                        cadenceType = cadenceType,
                        category = category,
                        lastChargeEpoch = lastCharge,
                        nextPredictedEpoch = nextPredicted,
                        isAlertEnabled = true,
                        totalSpent = txns.sumOf { it.amount },
                        transactionCount = txns.size
                    )
                    recurringItems.add(recurringItem)
                }
            }

            Result.success(recurringItems)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun determineCadenceType(avgIntervalMillis: Long): CadenceType {
        val days = avgIntervalMillis / (1000 * 60 * 60 * 24)

        return when {
            days in 6..8 -> CadenceType.WEEKLY
            days in 27..33 -> CadenceType.MONTHLY
            days in 85..95 -> CadenceType.QUARTERLY
            days in 350..380 -> CadenceType.YEARLY
            else -> CadenceType.UNKNOWN
        }
    }

    private fun isConsistentPattern(intervals: List<Long>, avgInterval: Long): Boolean {
        if (intervals.isEmpty()) return false

        val threshold = 0.20
        val deviations = intervals.map { interval ->
            abs(interval - avgInterval).toDouble() / avgInterval
        }

        val consistentCount = deviations.count { it <= threshold }
        return consistentCount.toDouble() / intervals.size >= 0.7
    }

    private fun categorizeByMerchant(merchant: String): String {
        val lowerMerchant = merchant.lowercase()

        return when {
            lowerMerchant.contains("netflix") || lowerMerchant.contains("spotify") || 
            lowerMerchant.contains("prime") || lowerMerchant.contains("youtube") ||
            lowerMerchant.contains("disney") -> "Entertainment"
            
            lowerMerchant.contains("gym") || lowerMerchant.contains("fitness") ||
            lowerMerchant.contains("health") || lowerMerchant.contains("medical") -> "Health & Fitness"
            
            lowerMerchant.contains("insurance") -> "Insurance"
            
            lowerMerchant.contains("electricity") || lowerMerchant.contains("water") ||
            lowerMerchant.contains("gas") || lowerMerchant.contains("utility") -> "Utilities"
            
            lowerMerchant.contains("mobile") || lowerMerchant.contains("telecom") ||
            lowerMerchant.contains("internet") || lowerMerchant.contains("broadband") -> "Telecom"
            
            lowerMerchant.contains("loan") || lowerMerchant.contains("emi") ||
            lowerMerchant.contains("credit") -> "Finance"
            
            else -> "Other"
        }
    }
}
