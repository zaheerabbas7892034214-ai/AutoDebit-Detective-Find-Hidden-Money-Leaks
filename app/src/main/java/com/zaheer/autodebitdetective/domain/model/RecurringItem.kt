package com.zaheer.autodebitdetective.domain.model

data class RecurringItem(
    val id: Long = 0,
    val merchant: String,
    val avgAmount: Double,
    val cadenceType: CadenceType,
    val category: String,
    val lastChargeEpoch: Long,
    val nextPredictedEpoch: Long,
    val isAlertEnabled: Boolean,
    val totalSpent: Double = 0.0,
    val transactionCount: Int = 0
)

enum class CadenceType(val displayName: String) {
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    QUARTERLY("Quarterly"),
    YEARLY("Yearly"),
    UNKNOWN("Unknown");

    companion object {
        fun fromString(value: String): CadenceType {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: UNKNOWN
        }
    }
}
