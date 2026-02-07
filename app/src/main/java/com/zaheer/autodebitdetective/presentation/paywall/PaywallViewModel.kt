package com.zaheer.autodebitdetective.presentation.paywall

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zaheer.autodebitdetective.billing.BillingManager
import com.zaheer.autodebitdetective.billing.SubscriptionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class PaywallState {
    data object Idle : PaywallState()
    data object Loading : PaywallState()
    data object Purchasing : PaywallState()
    data object Success : PaywallState()
    data class Error(val message: String) : PaywallState()
    data object AlreadySubscribed : PaywallState()
}

class PaywallViewModel(
    private val billingManager: BillingManager
) : ViewModel() {

    private val _state = MutableStateFlow<PaywallState>(PaywallState.Idle)
    val state: StateFlow<PaywallState> = _state.asStateFlow()

    private val _subscriptionStatus = MutableStateFlow<SubscriptionStatus>(SubscriptionStatus.Unknown)
    val subscriptionStatus: StateFlow<SubscriptionStatus> = _subscriptionStatus.asStateFlow()

    init {
        checkSubscriptionStatus()
    }

    private fun checkSubscriptionStatus() {
        viewModelScope.launch {
            billingManager.subscriptionStatus.collect { status ->
                _subscriptionStatus.value = status
                if (status is SubscriptionStatus.Active) {
                    _state.value = PaywallState.AlreadySubscribed
                }
            }
        }
    }

    fun launchBillingFlow(activity: Activity) {
        viewModelScope.launch {
            try {
                _state.value = PaywallState.Purchasing
                
                val result = billingManager.launchBillingFlow(activity)
                
                if (result.isSuccess) {
                    _state.value = PaywallState.Success
                } else {
                    _state.value = PaywallState.Error(
                        result.exceptionOrNull()?.message ?: "Purchase failed"
                    )
                }
            } catch (e: Exception) {
                _state.value = PaywallState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun restorePurchases() {
        viewModelScope.launch {
            try {
                _state.value = PaywallState.Loading
                
                val result = billingManager.restorePurchases()
                
                if (result.isSuccess) {
                    val hasActiveSub = result.getOrNull() ?: false
                    _state.value = if (hasActiveSub) {
                        PaywallState.Success
                    } else {
                        PaywallState.Error("No active subscription found")
                    }
                } else {
                    _state.value = PaywallState.Error(
                        result.exceptionOrNull()?.message ?: "Restore failed"
                    )
                }
            } catch (e: Exception) {
                _state.value = PaywallState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun dismissError() {
        _state.value = PaywallState.Idle
    }
}
