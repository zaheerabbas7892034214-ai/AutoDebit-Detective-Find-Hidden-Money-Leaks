package com.zaheer.autodebitdetective.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zaheer.autodebitdetective.billing.BillingManager
import com.zaheer.autodebitdetective.billing.SubscriptionStatus
import com.zaheer.autodebitdetective.data.repository.RecurringRepository
import com.zaheer.autodebitdetective.domain.model.RecurringItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class DashboardData(
    val monthlyTotal: Double,
    val yearlyProjection: Double,
    val recurringCount: Int,
    val topMerchants: List<RecurringItem>,
    val isPro: Boolean
)

sealed class HomeState {
    data object Loading : HomeState()
    data class Success(val data: DashboardData) : HomeState()
    data class Error(val message: String) : HomeState()
    data object Empty : HomeState()
}

class HomeViewModel(
    private val recurringRepository: RecurringRepository,
    private val billingManager: BillingManager
) : ViewModel() {

    private val _state = MutableStateFlow<HomeState>(HomeState.Loading)
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        viewModelScope.launch {
            try {
                _state.value = HomeState.Loading
                
                combine(
                    recurringRepository.getAllRecurringItems(),
                    billingManager.subscriptionStatus
                ) { items, subscriptionStatus ->
                    Pair(items, subscriptionStatus)
                }.collect { (items, subscriptionStatus) ->
                    if (items.isEmpty()) {
                        _state.value = HomeState.Empty
                    } else {
                        val monthlyTotal = items.sumOf { it.avgAmount }
                        val yearlyProjection = monthlyTotal * 12
                        val topMerchants = items.sortedByDescending { it.avgAmount }.take(3)
                        val isPro = subscriptionStatus is SubscriptionStatus.Active

                        _state.value = HomeState.Success(
                            DashboardData(
                                monthlyTotal = monthlyTotal,
                                yearlyProjection = yearlyProjection,
                                recurringCount = items.size,
                                topMerchants = topMerchants,
                                isPro = isPro
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                _state.value = HomeState.Error(e.message ?: "Failed to load dashboard")
            }
        }
    }

    fun refresh() {
        loadDashboard()
    }
}
