package com.zaheer.autodebitdetective.parser

import android.util.Log
import java.util.regex.Pattern

data class ParsedTransaction(
    val amount: Double,
    val merchant: String,
    val timestamp: Long,
    val source: String
)

class SMSParser {
    
    fun parseSMS(
        address: String,
        body: String,
        timestamp: Long
    ): ParsedTransaction? {
        if (!isTransactionSMS(body)) {
            return null
        }
        
        val amount = extractAmount(body) ?: return null
        val merchant = extractMerchant(body) ?: return null
        val source = extractSource(address)
        
        return ParsedTransaction(
            amount = amount,
            merchant = merchant,
            timestamp = timestamp,
            source = source
        )
    }
    
    private fun isTransactionSMS(body: String): Boolean {
        val bodyLower = body.lowercase()
        return DEBIT_KEYWORDS.any { keyword -> bodyLower.contains(keyword) }
    }
    
    private fun extractAmount(body: String): Double? {
        for (pattern in AMOUNT_PATTERNS) {
            val matcher = pattern.matcher(body)
            if (matcher.find()) {
                try {
                    val amountStr = matcher.group(1)?.replace(",", "")
                    return amountStr?.toDoubleOrNull()
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing amount: ${e.message}")
                }
            }
        }
        return null
    }
    
    private fun extractMerchant(body: String): String? {
        for ((keyword, pattern) in MERCHANT_PATTERNS) {
            val matcher = pattern.matcher(body)
            if (matcher.find()) {
                val merchant = matcher.group(1)?.trim()
                if (!merchant.isNullOrBlank() && merchant.length > 2) {
                    return cleanMerchantName(merchant)
                }
            }
        }
        
        for (pattern in FALLBACK_MERCHANT_PATTERNS) {
            val matcher = pattern.matcher(body)
            if (matcher.find()) {
                val merchant = matcher.group(1)?.trim()
                if (!merchant.isNullOrBlank() && merchant.length > 2) {
                    return cleanMerchantName(merchant)
                }
            }
        }
        
        return extractFromUPI(body)
    }
    
    private fun extractFromUPI(body: String): String? {
        val upiPattern = Pattern.compile(
            "(?:to|from|@)\\s+([A-Za-z0-9@._-]+@[A-Za-z]+)",
            Pattern.CASE_INSENSITIVE
        )
        val matcher = upiPattern.matcher(body)
        if (matcher.find()) {
            val upiId = matcher.group(1)?.trim()
            if (!upiId.isNullOrBlank()) {
                return upiId.substringBefore("@").replace(".", " ").trim()
            }
        }
        
        return null
    }
    
    private fun cleanMerchantName(name: String): String {
        var cleaned = name
            .replace(Regex("[^A-Za-z0-9\\s.-]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
        
        if (cleaned.length > 50) {
            cleaned = cleaned.substring(0, 50)
        }
        
        NOISE_WORDS.forEach { noise ->
            cleaned = cleaned.replace(Regex("\\b$noise\\b", RegexOption.IGNORE_CASE), "")
        }
        
        cleaned = cleaned.replace(Regex("\\s+"), " ").trim()
        
        return if (cleaned.isNotBlank()) cleaned else "Unknown Merchant"
    }
    
    private fun extractSource(address: String): String {
        val cleanAddress = address.uppercase().replace(Regex("[^A-Z]"), "")
        
        return BANK_PREFIXES.entries
            .firstOrNull { (_, prefixes) ->
                prefixes.any { prefix -> cleanAddress.contains(prefix) }
            }
            ?.key ?: "Unknown Bank"
    }
    
    companion object {
        private const val TAG = "SMSParser"
        
        private val DEBIT_KEYWORDS = listOf(
            "debited", "spent", "paid", "purchase", "withdrawn",
            "upi", "txn", "transaction", "debit", "payment",
            "emi", "ecs", "nach", "autopay"
        )
        
        private val AMOUNT_PATTERNS = listOf(
            Pattern.compile("(?:rs\\.?|inr|₹)\\s*([0-9,]+\\.?[0-9]*)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?:amount|amt)\\s*(?:of)?\\s*(?:rs\\.?|inr|₹)?\\s*([0-9,]+\\.?[0-9]*)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("([0-9,]+\\.?[0-9]*)\\s*(?:rs|inr|₹|rupees)", Pattern.CASE_INSENSITIVE)
        )
        
        private val MERCHANT_PATTERNS = mapOf(
            "at" to Pattern.compile("(?:at|to)\\s+([A-Za-z0-9\\s&.-]+?)(?:\\s+on|\\.|,|\\s+for|\\s+ref)", Pattern.CASE_INSENSITIVE),
            "to" to Pattern.compile("(?:paid to|sent to|transfer to)\\s+([A-Za-z0-9\\s&.-]+?)(?:\\s+on|\\.|,|\\s+for|\\s+ref)", Pattern.CASE_INSENSITIVE),
            "merchant" to Pattern.compile("(?:merchant|payee)\\s*:?\\s*([A-Za-z0-9\\s&.-]+?)(?:\\s+on|\\.|,|\\s+ref)", Pattern.CASE_INSENSITIVE),
            "via" to Pattern.compile("(?:via|using)\\s+([A-Za-z0-9\\s&.-]+?)(?:\\s+on|\\.|,|\\s+for)", Pattern.CASE_INSENSITIVE)
        )
        
        private val FALLBACK_MERCHANT_PATTERNS = listOf(
            Pattern.compile("(?:for|towards)\\s+([A-Za-z0-9\\s&.-]+?)(?:\\s+on|\\.|,)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?:payment|txn)\\s+(?:to|at)\\s+([A-Za-z0-9\\s&.-]+?)(?:\\s+|\\.|,)", Pattern.CASE_INSENSITIVE)
        )
        
        private val NOISE_WORDS = listOf(
            "pvt", "ltd", "limited", "private", "india", "services",
            "payment", "transaction", "ref", "reference", "upi", "vpa"
        )
        
        private val BANK_PREFIXES = mapOf(
            "SBI" to listOf("SBI", "SBIINB", "SBMSMS"),
            "HDFC" to listOf("HDFC", "HDFCBK"),
            "ICICI" to listOf("ICICI", "ICICIB"),
            "Axis" to listOf("AXIS", "AXISBK"),
            "Kotak" to listOf("KOTAK", "KOTAKB"),
            "PNB" to listOf("PNB", "PNBSMS"),
            "BOB" to listOf("BOB", "BOBSMS"),
            "Canara" to listOf("CANARA", "CANBK"),
            "IDBI" to listOf("IDBI", "IDBIB"),
            "Yes Bank" to listOf("YESBNK", "YESBAN"),
            "IndusInd" to listOf("INDUS", "INDUSB"),
            "Paytm" to listOf("PAYTM", "PYTM"),
            "PhonePe" to listOf("PHONEPE", "PHNPE"),
            "Google Pay" to listOf("GPAY", "GOOGLEPAY"),
            "Amazon Pay" to listOf("AZNPAY", "AMAZON")
        )
    }
}
