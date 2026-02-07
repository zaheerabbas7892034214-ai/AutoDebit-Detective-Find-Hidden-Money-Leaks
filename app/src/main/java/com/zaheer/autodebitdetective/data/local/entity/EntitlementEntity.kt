package com.zaheer.autodebitdetective.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "entitlement")
data class EntitlementEntity(
    @PrimaryKey
    val id: Int = 1,
    val isProCached: Boolean,
    val lastCheckEpoch: Long,
    val lastState: String
)
