package com.zaheer.autodebitdetective.domain.model

data class UpcomingCharge(
    val merchant: String,
    val amount: Double,
    val predictedDate: Long,
    val cadenceType: CadenceType,
    val category: String,
    val daysUntilCharge: Int,
    val recurringItemId: Long
)
