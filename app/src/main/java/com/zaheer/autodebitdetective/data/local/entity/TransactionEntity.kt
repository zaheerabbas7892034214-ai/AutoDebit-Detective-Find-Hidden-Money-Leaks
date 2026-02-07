package com.zaheer.autodebitdetective.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val amount: Double,
    val merchant: String,
    val rawSnippetHash: String,
    val source: String
)
