package com.zaheer.autodebitdetective.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recurring_items")
data class RecurringEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val merchant: String,
    val avgAmount: Double,
    val cadenceType: String,
    val category: String,
    val lastChargeEpoch: Long,
    val nextPredictedEpoch: Long,
    val isAlertEnabled: Boolean
)
