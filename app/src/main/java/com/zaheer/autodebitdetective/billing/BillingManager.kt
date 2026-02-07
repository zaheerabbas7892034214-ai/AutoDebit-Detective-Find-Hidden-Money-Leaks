package com.zaheer.autodebitdetective.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

class BillingManager(
    private val context: Context
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    private val _subscriptionStatus = MutableStateFlow<SubscriptionStatus>(SubscriptionStatus.Unknown)
    val subscriptionStatus: StateFlow<SubscriptionStatus> = _subscriptionStatus.asStateFlow()
    
    private val _billingError = MutableStateFlow<String?>(null)
    val billingError: StateFlow<String?> = _billingError.asStateFlow()
    
    private var billingClient: BillingClient? = null
    private var productDetails: ProductDetails? = null
    
    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { purchase ->
                    handlePurchase(purchase)
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                Log.d(TAG, "User canceled purchase")
                _billingError.value = "Purchase canceled"
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                Log.d(TAG, "Item already owned")
                scope.launch { queryPurchases() }
            }
            else -> {
                val errorMessage = "Purchase failed: ${billingResult.debugMessage}"
                Log.e(TAG, errorMessage)
                _billingError.value = errorMessage
            }
        }
    }
    
    suspend fun initialize(): Boolean = withContext(Dispatchers.Main) {
        if (billingClient?.isReady == true) {
            return@withContext true
        }
        
        suspendCancellableCoroutine { continuation ->
            billingClient = BillingClient.newBuilder(context)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build()
            
            billingClient?.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        Log.d(TAG, "Billing client connected")
                        scope.launch {
                            queryProductDetails()
                            queryPurchases()
                        }
                        continuation.resume(true)
                    } else {
                        val error = "Billing setup failed: ${billingResult.debugMessage}"
                        Log.e(TAG, error)
                        _billingError.value = error
                        continuation.resume(false)
                    }
                }
                
                override fun onBillingServiceDisconnected() {
                    Log.w(TAG, "Billing service disconnected")
                    _subscriptionStatus.value = SubscriptionStatus.Unknown
                }
            })
        }
    }
    
    private suspend fun queryProductDetails() = withContext(Dispatchers.IO) {
        val client = billingClient ?: run {
            Log.e(TAG, "BillingClient not initialized")
            return@withContext
        }
        
        if (!client.isReady) {
            Log.e(TAG, "BillingClient not ready")
            return@withContext
        }
        
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_ID)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )
        
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()
        
        val result = client.queryProductDetails(params)
        
        when (result.billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                val details = result.productDetailsList?.firstOrNull()
                if (details != null) {
                    productDetails = details
                    Log.d(TAG, "Product details retrieved: ${details.productId}")
                } else {
                    Log.e(TAG, "No product details found for $PRODUCT_ID")
                }
            }
            else -> {
                val error = "Failed to query product details: ${result.billingResult.debugMessage}"
                Log.e(TAG, error)
                _billingError.value = error
            }
        }
    }
    
    suspend fun queryPurchases() = withContext(Dispatchers.IO) {
        val client = billingClient ?: run {
            Log.e(TAG, "BillingClient not initialized")
            return@withContext
        }
        
        if (!client.isReady) {
            Log.e(TAG, "BillingClient not ready")
            return@withContext
        }
        
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()
        
        val result = client.queryPurchasesAsync(params)
        
        when (result.billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                val activePurchase = result.purchasesList.firstOrNull { purchase ->
                    purchase.products.contains(PRODUCT_ID) &&
                    purchase.purchaseState == Purchase.PurchaseState.PURCHASED
                }
                
                if (activePurchase != null) {
                    _subscriptionStatus.value = SubscriptionStatus.Active
                    if (!activePurchase.isAcknowledged) {
                        acknowledgePurchase(activePurchase)
                    }
                } else {
                    val pendingPurchase = result.purchasesList.firstOrNull { purchase ->
                        purchase.products.contains(PRODUCT_ID) &&
                        purchase.purchaseState == Purchase.PurchaseState.PENDING
                    }
                    
                    _subscriptionStatus.value = if (pendingPurchase != null) {
                        SubscriptionStatus.Pending
                    } else {
                        SubscriptionStatus.Expired
                    }
                }
                
                Log.d(TAG, "Purchase query completed. Status: ${_subscriptionStatus.value}")
            }
            else -> {
                val error = "Failed to query purchases: ${result.billingResult.debugMessage}"
                Log.e(TAG, error)
                _billingError.value = error
                _subscriptionStatus.value = SubscriptionStatus.Unknown
            }
        }
    }
    
    fun launchBillingFlow(activity: Activity): Boolean {
        val client = billingClient
        if (client == null || !client.isReady) {
            val error = "Billing client not ready"
            Log.e(TAG, error)
            _billingError.value = error
            return false
        }
        
        val details = productDetails
        if (details == null) {
            val error = "Product details not available"
            Log.e(TAG, error)
            _billingError.value = error
            return false
        }
        
        val offerToken = details.subscriptionOfferDetails?.firstOrNull()?.offerToken
        if (offerToken == null) {
            val error = "No subscription offer available"
            Log.e(TAG, error)
            _billingError.value = error
            return false
        }
        
        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(details)
                .setOfferToken(offerToken)
                .build()
        )
        
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()
        
        val billingResult = client.launchBillingFlow(activity, billingFlowParams)
        
        return when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                Log.d(TAG, "Billing flow launched successfully")
                _billingError.value = null
                true
            }
            else -> {
                val error = "Failed to launch billing flow: ${billingResult.debugMessage}"
                Log.e(TAG, error)
                _billingError.value = error
                false
            }
        }
    }
    
    private fun handlePurchase(purchase: Purchase) {
        scope.launch(Dispatchers.IO) {
            when (purchase.purchaseState) {
                Purchase.PurchaseState.PURCHASED -> {
                    _subscriptionStatus.value = SubscriptionStatus.Active
                    if (!purchase.isAcknowledged) {
                        acknowledgePurchase(purchase)
                    }
                    Log.d(TAG, "Purchase completed successfully")
                }
                Purchase.PurchaseState.PENDING -> {
                    _subscriptionStatus.value = SubscriptionStatus.Pending
                    Log.d(TAG, "Purchase pending")
                }
                else -> {
                    Log.w(TAG, "Unknown purchase state: ${purchase.purchaseState}")
                }
            }
        }
    }
    
    private suspend fun acknowledgePurchase(purchase: Purchase) = withContext(Dispatchers.IO) {
        val client = billingClient ?: return@withContext
        
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        
        val result = client.acknowledgePurchase(params)
        
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                Log.d(TAG, "Purchase acknowledged")
            }
            else -> {
                val error = "Failed to acknowledge purchase: ${result.debugMessage}"
                Log.e(TAG, error)
                _billingError.value = error
            }
        }
    }
    
    suspend fun restorePurchases(): Boolean {
        queryPurchases()
        return _subscriptionStatus.value == SubscriptionStatus.Active
    }
    
    fun clearError() {
        _billingError.value = null
    }
    
    fun endConnection() {
        billingClient?.endConnection()
        billingClient = null
        productDetails = null
    }
    
    companion object {
        private const val TAG = "BillingManager"
        private const val PRODUCT_ID = "autodebit_pro_yearly"
    }
}
