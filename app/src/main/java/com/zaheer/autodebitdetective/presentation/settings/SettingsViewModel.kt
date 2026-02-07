package com.zaheer.autodebitdetective.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zaheer.autodebitdetective.billing.BillingManager
import com.zaheer.autodebitdetective.billing.SubscriptionStatus
import com.zaheer.autodebitdetective.data.datastore.PreferencesManager
import com.zaheer.autodebitdetective.data.repository.RecurringRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class SettingsData(
    val subscriptionStatus: SubscriptionStatus,
    val alertsEnabled: Boolean,
    val notificationPrivacy: Boolean,
    val appLockEnabled: Boolean,
    val biometricEnabled: Boolean
)

sealed class SettingsState {
    data object Loading : SettingsState()
    data class Success(val data: SettingsData) : SettingsState()
    data class Error(val message: String) : SettingsState()
}

sealed class DataOperationState {
    data object Idle : DataOperationState()
    data object Processing : DataOperationState()
    data object Success : DataOperationState()
    data class Error(val message: String) : DataOperationState()
}

class SettingsViewModel(
    private val preferencesManager: PreferencesManager,
    private val billingManager: BillingManager,
    private val recurringRepository: RecurringRepository
) : ViewModel() {

    private val _state = MutableStateFlow<SettingsState>(SettingsState.Loading)
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    private val _dataOperationState = MutableStateFlow<DataOperationState>(DataOperationState.Idle)
    val dataOperationState: StateFlow<DataOperationState> = _dataOperationState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            try {
                combine(
                    billingManager.subscriptionStatus,
                    preferencesManager.alertsEnabled,
                    preferencesManager.hideNotificationContent,
                    preferencesManager.appLockEnabled,
                    preferencesManager.biometricEnabled
                ) { subscription, alerts, privacy, appLock, biometric ->
                    SettingsData(
                        subscriptionStatus = subscription,
                        alertsEnabled = alerts,
                        notificationPrivacy = privacy,
                        appLockEnabled = appLock,
                        biometricEnabled = biometric
                    )
                }.collect { data ->
                    _state.value = SettingsState.Success(data)
                }
            } catch (e: Exception) {
                _state.value = SettingsState.Error(e.message ?: "Failed to load settings")
            }
        }
    }

    fun toggleAlerts(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setAlertsEnabled(enabled)
        }
    }

    fun toggleNotificationPrivacy(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setHideNotificationContent(enabled)
        }
    }

    fun toggleAppLock(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setAppLockEnabled(enabled)
        }
    }

    fun toggleBiometric(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setBiometricEnabled(enabled)
        }
    }

    fun restorePurchases() {
        viewModelScope.launch {
            try {
                _dataOperationState.value = DataOperationState.Processing
                val result = billingManager.restorePurchases()
                
                if (result.isSuccess) {
                    _dataOperationState.value = DataOperationState.Success
                } else {
                    _dataOperationState.value = DataOperationState.Error(
                        result.exceptionOrNull()?.message ?: "Restore failed"
                    )
                }
            } catch (e: Exception) {
                _dataOperationState.value = DataOperationState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun deleteAllData() {
        viewModelScope.launch {
            try {
                _dataOperationState.value = DataOperationState.Processing
                recurringRepository.deleteAllRecurringItems()
                _dataOperationState.value = DataOperationState.Success
            } catch (e: Exception) {
                _dataOperationState.value = DataOperationState.Error(e.message ?: "Failed to delete data")
            }
        }
    }

    fun resetDataOperationState() {
        _dataOperationState.value = DataOperationState.Idle
    }
}
