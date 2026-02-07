package com.zaheer.autodebitdetective.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zaheer.autodebitdetective.billing.BillingManager
import com.zaheer.autodebitdetective.billing.SubscriptionStatus
import com.zaheer.autodebitdetective.data.datastore.PreferencesManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class SplashState {
    data object Loading : SplashState()
    data object NavigateToOnboarding : SplashState()
    data object NavigateToHome : SplashState()
    data class Error(val message: String) : SplashState()
}

class SplashViewModel(
    private val preferencesManager: PreferencesManager,
    private val billingManager: BillingManager
) : ViewModel() {

    private val _state = MutableStateFlow<SplashState>(SplashState.Loading)
    val state: StateFlow<SplashState> = _state.asStateFlow()

    init {
        initialize()
    }

    private fun initialize() {
        viewModelScope.launch {
            try {
                delay(1500)
                
                billingManager.initialize()
                
                val isOnboardingCompleted = preferencesManager.onboardingCompleted.first()
                
                if (isOnboardingCompleted) {
                    _state.value = SplashState.NavigateToHome
                } else {
                    _state.value = SplashState.NavigateToOnboarding
                }
            } catch (e: Exception) {
                _state.value = SplashState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun retryInitialization() {
        _state.value = SplashState.Loading
        initialize()
    }
}
