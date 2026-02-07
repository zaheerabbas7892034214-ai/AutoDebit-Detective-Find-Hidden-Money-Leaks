package com.zaheer.autodebitdetective.parser

import android.util.Log
import com.zaheer.autodebitdetective.domain.model.Transaction
import java.util.concurrent.TimeUnit
import kotlin.math.abs

enum class CadenceType {
    MONTHLY, YEARLY, UNKNOWN
}

enum class RecurringCategory {
    SUBSCRIPTION, EMI, INSURANCE, MEMBERSHIP, UTILITY, UNKNOWN
}

data class RecurringPattern(
    val merchant: String,
    val category: RecurringCategory,
    val cadenceType: CadenceType,
    val averageAmount: Double,
    val lastChargeTimestamp: Long,
    val nextPredictedTimestamp: Long,
    val transactionIds: List<Long>
)

class RecurringDetector {
    
    fun detectRecurringPatterns(
        transactions: List<Transaction>,
        minOccurrences: Int = 2
    ): List<RecurringPattern> {
        val patterns = mutableListOf<RecurringPattern>()
        
        val groupedByMerchant = transactions
            .groupBy { it.merchant.lowercase().trim() }
            .filter { it.value.size >= minOccurrences }
        
        for ((merchant, txnList) in groupedByMerchant) {
            val sortedTxns = txnList.sortedBy { it.timestamp }
            
            val intervals = mutableListOf<Long>()
            for (i in 0 until sortedTxns.size - 1) {
                val interval = sortedTxns[i + 1].timestamp - sortedTxns[i].timestamp
                intervals.add(interval)
            }
            
            if (intervals.isEmpty()) continue
            
            val cadence = determineCadence(intervals)
            if (cadence == CadenceType.UNKNOWN) continue
            
            val avgAmount = sortedTxns.map { it.amount }.average()
            val amountVariance = calculateAmountVariance(sortedTxns, avgAmount)
            
            if (amountVariance > 0.3) continue
            
            val category = categorizeRecurring(merchant, sortedTxns)
            val lastCharge = sortedTxns.last().timestamp
            val nextPredicted = predictNextCharge(lastCharge, cadence, intervals.average().toLong())
            
            patterns.add(
                RecurringPattern(
                    merchant = merchant,
                    category = category,
                    cadenceType = cadence,
                    averageAmount = avgAmount,
                    lastChargeTimestamp = lastCharge,
                    nextPredictedTimestamp = nextPredicted,
                    transactionIds = sortedTxns.map { it.id }
                )
            )
        }
        
        return patterns
    }
    
    private fun determineCadence(intervals: List<Long>): CadenceType {
        if (intervals.isEmpty()) return CadenceType.UNKNOWN
        
        val avgIntervalDays = TimeUnit.MILLISECONDS.toDays(intervals.average().toLong())
        
        return when {
            avgIntervalDays in 28..31 -> {
                val variance = calculateIntervalVariance(intervals)
                if (variance < 0.15) CadenceType.MONTHLY else CadenceType.UNKNOWN
            }
            avgIntervalDays in 350..380 -> {
                val variance = calculateIntervalVariance(intervals)
                if (variance < 0.1) CadenceType.YEARLY else CadenceType.UNKNOWN
            }
            else -> CadenceType.UNKNOWN
        }
    }
    
    private fun calculateIntervalVariance(intervals: List<Long>): Double {
        if (intervals.size < 2) return 0.0
        
        val avgInterval = intervals.average()
        val variance = intervals.map { interval ->
            abs(interval - avgInterval) / avgInterval
        }.average()
        
        return variance
    }
    
    private fun calculateAmountVariance(transactions: List<Transaction>, avgAmount: Double): Double {
        if (transactions.size < 2 || avgAmount == 0.0) return 0.0
        
        val variance = transactions.map { txn ->
            abs(txn.amount - avgAmount) / avgAmount
        }.average()
        
        return variance
    }
    
    private fun categorizeRecurring(merchant: String, transactions: List<Transaction>): RecurringCategory {
        val merchantLower = merchant.lowercase()
        
        if (EMI_KEYWORDS.any { merchantLower.contains(it) }) {
            return RecurringCategory.EMI
        }
        
        if (INSURANCE_KEYWORDS.any { merchantLower.contains(it) }) {
            return RecurringCategory.INSURANCE
        }
        
        if (SUBSCRIPTION_SERVICES.any { merchantLower.contains(it) }) {
            return RecurringCategory.SUBSCRIPTION
        }
        
        if (UTILITY_KEYWORDS.any { merchantLower.contains(it) }) {
            return RecurringCategory.UTILITY
        }
        
        if (MEMBERSHIP_KEYWORDS.any { merchantLower.contains(it) }) {
            return RecurringCategory.MEMBERSHIP
        }
        
        val avgAmount = transactions.map { it.amount }.average()
        return when {
            avgAmount < 500 -> RecurringCategory.SUBSCRIPTION
            avgAmount > 5000 -> RecurringCategory.EMI
            else -> RecurringCategory.UNKNOWN
        }
    }
    
    private fun predictNextCharge(
        lastChargeTimestamp: Long,
        cadence: CadenceType,
        avgInterval: Long
    ): Long {
        return when (cadence) {
            CadenceType.MONTHLY -> {
                lastChargeTimestamp + avgInterval
            }
            CadenceType.YEARLY -> {
                lastChargeTimestamp + avgInterval
            }
            CadenceType.UNKNOWN -> {
                lastChargeTimestamp + TimeUnit.DAYS.toMillis(30)
            }
        }
    }
    
    fun isEMI(merchant: String): Boolean {
        val merchantLower = merchant.lowercase()
        return EMI_KEYWORDS.any { merchantLower.contains(it) }
    }
    
    fun isInsurance(merchant: String): Boolean {
        val merchantLower = merchant.lowercase()
        return INSURANCE_KEYWORDS.any { merchantLower.contains(it) }
    }
    
    fun isSubscription(merchant: String): Boolean {
        val merchantLower = merchant.lowercase()
        return SUBSCRIPTION_SERVICES.any { merchantLower.contains(it) }
    }
    
    companion object {
        private const val TAG = "RecurringDetector"
        
        private val EMI_KEYWORDS = listOf(
            "emi", "ecs", "nach", "loan", "instalment", "installment",
            "mortgage", "finance", "credit", "bajaj", "hdfc emi"
        )
        
        private val INSURANCE_KEYWORDS = listOf(
            "insurance", "premium", "policy", "lic", "icici pru",
            "max life", "hdfc life", "sbi life", "star health",
            "care health", "bajaj allianz"
        )
        
        private val SUBSCRIPTION_SERVICES = listOf(
            "netflix", "amazon prime", "prime video", "hotstar",
            "disney", "spotify", "youtube", "google one", "icloud",
            "apple music", "zee5", "sonyliv", "voot", "mx player",
            "gaana", "jio saavn", "audible", "kindle", "scribd",
            "medium", "linkedin premium", "evernote", "dropbox",
            "onedrive", "adobe", "microsoft 365", "office 365",
            "zoom", "canva", "grammarly", "notion"
        )
        
        private val UTILITY_KEYWORDS = listOf(
            "electricity", "water", "gas", "broadband", "internet",
            "wifi", "mobile", "recharge", "bill", "tata power",
            "bses", "adani", "airtel", "jio", "vodafone", "vi",
            "mtnl", "bsnl"
        )
        
        private val MEMBERSHIP_KEYWORDS = listOf(
            "membership", "gym", "fitness", "club", "society",
            "maintenance", "cult fit", "gold gym", "talwalkars"
        )
    }
}
