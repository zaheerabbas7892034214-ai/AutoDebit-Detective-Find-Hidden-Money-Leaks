package com.zaheer.autodebitdetective.billing

sealed class SubscriptionStatus {
    data object Active : SubscriptionStatus()
    data object Expired : SubscriptionStatus()
    data object Pending : SubscriptionStatus()
    data object Unknown : SubscriptionStatus()
}
