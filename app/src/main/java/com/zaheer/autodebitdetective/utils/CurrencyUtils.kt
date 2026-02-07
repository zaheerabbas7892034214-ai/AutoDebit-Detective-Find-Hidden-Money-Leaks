package com.zaheer.autodebitdetective.utils

import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

object CurrencyUtils {
    
    private val indianLocale = Locale("en", "IN")
    private val currencyFormat = NumberFormat.getCurrencyInstance(indianLocale).apply {
        maximumFractionDigits = 2
        minimumFractionDigits = 2
    }
    
    fun formatAmount(amount: Double): String {
        return try {
            formatIndianCurrency(amount)
        } catch (e: Exception) {
            "${Constants.CURRENCY_SYMBOL_INR}${String.format("%.2f", amount)}"
        }
    }
    
    fun formatAmountWithoutSymbol(amount: Double): String {
        return formatAmount(amount).replace(Constants.CURRENCY_SYMBOL_INR, "").trim()
    }
    
    private fun formatIndianCurrency(amount: Double): String {
        val isNegative = amount < 0
        val absoluteAmount = kotlin.math.abs(amount)
        
        val formatter = DecimalFormat("#,##,##0.00")
        val formattedNumber = formatter.format(absoluteAmount)
        
        val result = "${Constants.CURRENCY_SYMBOL_INR}$formattedNumber"
        return if (isNegative) "-$result" else result
    }
    
    fun formatAmountCompact(amount: Double): String {
        val absAmount = kotlin.math.abs(amount)
        val isNegative = amount < 0
        
        val formatted = when {
            absAmount >= 10_000_000 -> {
                "${String.format("%.2f", absAmount / 10_000_000)}Cr"
            }
            absAmount >= 100_000 -> {
                "${String.format("%.2f", absAmount / 100_000)}L"
            }
            absAmount >= 1_000 -> {
                "${String.format("%.2f", absAmount / 1_000)}K"
            }
            else -> {
                String.format("%.2f", absAmount)
            }
        }
        
        val result = "${Constants.CURRENCY_SYMBOL_INR}$formatted"
        return if (isNegative) "-$result" else result
    }
    
    fun parseAmount(amountString: String): Double? {
        return try {
            val cleaned = amountString
                .replace(Constants.CURRENCY_SYMBOL_INR, "")
                .replace("Rs.", "")
                .replace("Rs", "")
                .replace("INR", "")
                .replace(",", "")
                .replace(" ", "")
                .trim()
            
            if (cleaned.isEmpty()) {
                null
            } else {
                cleaned.toDoubleOrNull()
            }
        } catch (e: Exception) {
            null
        }
    }
    
    fun formatAmountWithSign(amount: Double): String {
        val formatted = formatAmount(kotlin.math.abs(amount))
        return if (amount >= 0) "+$formatted" else "-$formatted"
    }
    
    fun formatPercentage(value: Double): String {
        return String.format("%.1f%%", value)
    }
    
    fun formatAmountRange(minAmount: Double, maxAmount: Double): String {
        return "${formatAmount(minAmount)} - ${formatAmount(maxAmount)}"
    }
    
    fun roundToTwoDecimals(amount: Double): Double {
        return kotlin.math.round(amount * 100) / 100.0
    }
    
    fun calculatePercentage(part: Double, total: Double): Double {
        return if (total == 0.0) {
            0.0
        } else {
            (part / total) * 100
        }
    }
    
    fun calculatePercentageChange(oldValue: Double, newValue: Double): Double {
        return if (oldValue == 0.0) {
            if (newValue == 0.0) 0.0 else 100.0
        } else {
            ((newValue - oldValue) / oldValue) * 100
        }
    }
    
    fun formatAmountForExport(amount: Double): String {
        return String.format(Locale.US, "%.2f", amount)
    }
    
    fun sumAmounts(amounts: List<Double>): Double {
        return amounts.sum()
    }
    
    fun averageAmount(amounts: List<Double>): Double {
        return if (amounts.isEmpty()) 0.0 else amounts.average()
    }
    
    fun maxAmount(amounts: List<Double>): Double {
        return amounts.maxOrNull() ?: 0.0
    }
    
    fun minAmount(amounts: List<Double>): Double {
        return amounts.minOrNull() ?: 0.0
    }
    
    fun isValidAmount(amount: Double): Boolean {
        return amount.isFinite() && amount >= 0
    }
    
    fun formatAmountWords(amount: Double): String {
        val absAmount = kotlin.math.abs(amount).toLong()
        val isNegative = amount < 0
        
        val words = when {
            absAmount == 0L -> "Zero"
            absAmount >= 10_000_000 -> {
                val crores = absAmount / 10_000_000
                val lakhs = (absAmount % 10_000_000) / 100_000
                when {
                    lakhs == 0L -> "$crores Crore${if (crores > 1) "s" else ""}"
                    else -> "$crores Crore${if (crores > 1) "s" else ""} $lakhs Lakh${if (lakhs > 1) "s" else ""}"
                }
            }
            absAmount >= 100_000 -> {
                val lakhs = absAmount / 100_000
                val thousands = (absAmount % 100_000) / 1_000
                when {
                    thousands == 0L -> "$lakhs Lakh${if (lakhs > 1) "s" else ""}"
                    else -> "$lakhs Lakh${if (lakhs > 1) "s" else ""} $thousands Thousand"
                }
            }
            absAmount >= 1_000 -> {
                val thousands = absAmount / 1_000
                "$thousands Thousand"
            }
            else -> absAmount.toString()
        }
        
        return if (isNegative) "Negative $words" else words
    }
    
    fun compareAmounts(amount1: Double, amount2: Double, tolerance: Double = 0.01): Int {
        val diff = amount1 - amount2
        return when {
            kotlin.math.abs(diff) <= tolerance -> 0
            diff > 0 -> 1
            else -> -1
        }
    }
    
    fun areAmountsSimilar(amount1: Double, amount2: Double, variancePercent: Double = 10.0): Boolean {
        if (amount1 == 0.0 && amount2 == 0.0) return true
        if (amount1 == 0.0 || amount2 == 0.0) return false
        
        val diff = kotlin.math.abs(amount1 - amount2)
        val avg = (amount1 + amount2) / 2
        val varianceAmount = (avg * variancePercent) / 100
        
        return diff <= varianceAmount
    }
}
