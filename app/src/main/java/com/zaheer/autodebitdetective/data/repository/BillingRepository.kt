package com.zaheer.autodebitdetective.data.repository

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.zaheer.autodebitdetective.data.local.dao.EntitlementDao
import com.zaheer.autodebitdetective.domain.usecase.ValidateSubscriptionUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class BillingRepository(
    private val entitlementDao: EntitlementDao
) {

    private var billingClient: BillingClient? = null
    private val validateSubscriptionUseCase = ValidateSubscriptionUseCase(entitlementDao)

    fun isProUser(): Flow<Boolean> {
        return entitlementDao.getEntitlement().map { entitlement ->
            entitlement?.isProCached ?: false
        }
    }

    suspend fun checkSubscriptionStatus(): Result<Boolean> = withContext(Dispatchers.IO) {
        validateSubscriptionUseCase()
    }

    suspend fun updateProStatus(isPro: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        validateSubscriptionUseCase.updateProStatus(isPro)
    }

    suspend fun initializeBillingClient(
        client: BillingClient,
        onPurchasesUpdated: PurchasesUpdatedListener
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            billingClient = client
            
            var connectionResult: Result<Unit>? = null
            
            client.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        connectionResult = Result.success(Unit)
                    } else {
                        connectionResult = Result.failure(
                            Exception("Billing setup failed: ${billingResult.debugMessage}")
                        )
                    }
                }

                override fun onBillingServiceDisconnected() {
                    connectionResult = Result.failure(Exception("Billing service disconnected"))
                }
            })

            while (connectionResult == null) {
                kotlinx.coroutines.delay(100)
            }

            connectionResult!!
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun queryPurchases(): Result<List<Purchase>> = withContext(Dispatchers.IO) {
        try {
            val client = billingClient ?: return@withContext Result.failure(
                Exception("Billing client not initialized")
            )

            val params = com.android.billingclient.api.QueryPurchasesParams.newBuilder()
                .setProductType(com.android.billingclient.api.BillingClient.ProductType.SUBS)
                .build()

            val purchasesResult = client.queryPurchasesAsync(params)
            
            if (purchasesResult.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Result.success(purchasesResult.purchasesList)
            } else {
                Result.failure(Exception("Failed to query purchases: ${purchasesResult.billingResult.debugMessage}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifyPurchase(purchase: Purchase): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val isValid = purchase.purchaseState == Purchase.PurchaseState.PURCHASED
            
            if (isValid) {
                updateProStatus(true)
            }

            Result.success(isValid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun endConnection() {
        billingClient?.endConnection()
        billingClient = null
    }
}
