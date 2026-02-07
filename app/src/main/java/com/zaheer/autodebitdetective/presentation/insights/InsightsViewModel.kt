package com.zaheer.autodebitdetective.presentation.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zaheer.autodebitdetective.data.repository.RecurringRepository
import com.zaheer.autodebitdetective.domain.model.RecurringItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CategoryData(
    val category: String,
    val amount: Double,
    val percentage: Float,
    val count: Int
)

data class MerchantData(
    val merchant: String,
    val amount: Double,
    val count: Int
)

data class InsightsData(
    val categoryBreakdown: List<CategoryData>,
    val topMerchants: List<MerchantData>,
    val totalMonthly: Double,
    val totalYearly: Double
)

sealed class InsightsState {
    data object Loading : InsightsState()
    data class Success(val data: InsightsData) : InsightsState()
    data class Error(val message: String) : InsightsState()
    data object Empty : InsightsState()
}

class InsightsViewModel(
    private val recurringRepository: RecurringRepository
) : ViewModel() {

    private val _state = MutableStateFlow<InsightsState>(InsightsState.Loading)
    val state: StateFlow<InsightsState> = _state.asStateFlow()

    init {
        loadInsights()
    }

    private fun loadInsights() {
        viewModelScope.launch {
            try {
                _state.value = InsightsState.Loading
                
                recurringRepository.getAllRecurringItems().collect { items ->
                    if (items.isEmpty()) {
                        _state.value = InsightsState.Empty
                    } else {
                        val totalMonthly = items.sumOf { it.avgAmount }
                        
                        val categoryMap = items.groupBy { it.category }
                        val categoryBreakdown = categoryMap.map { (category, categoryItems) ->
                            val amount = categoryItems.sumOf { it.avgAmount }
                            CategoryData(
                                category = category,
                                amount = amount,
                                percentage = (amount / totalMonthly * 100).toFloat(),
                                count = categoryItems.size
                            )
                        }.sortedByDescending { it.amount }
                        
                        val topMerchants = items.map { item ->
                            MerchantData(
                                merchant = item.merchant,
                                amount = item.avgAmount,
                                count = item.transactionCount
                            )
                        }.sortedByDescending { it.amount }.take(10)
                        
                        _state.value = InsightsState.Success(
                            InsightsData(
                                categoryBreakdown = categoryBreakdown,
                                topMerchants = topMerchants,
                                totalMonthly = totalMonthly,
                                totalYearly = totalMonthly * 12
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                _state.value = InsightsState.Error(e.message ?: "Failed to load insights")
            }
        }
    }

    fun refresh() {
        loadInsights()
    }
}
