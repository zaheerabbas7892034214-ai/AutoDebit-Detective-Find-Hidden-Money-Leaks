package com.zaheer.autodebitdetective.domain.model

data class Transaction(
    val id: Long = 0,
    val timestamp: Long,
    val amount: Double,
    val merchant: String,
    val rawSnippetHash: String,
    val source: String
)
