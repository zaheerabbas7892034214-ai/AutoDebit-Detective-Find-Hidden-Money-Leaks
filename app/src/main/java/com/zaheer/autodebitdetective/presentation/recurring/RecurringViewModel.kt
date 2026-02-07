package com.zaheer.autodebitdetective.presentation.recurring

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

data class RecurringListData(
    val items: List<RecurringItem>,
    val isPro: Boolean,
    val filteredCategory: String? = null,
    val searchQuery: String = ""
)

sealed class RecurringListState {
    data object Loading : RecurringListState()
    data class Success(val data: RecurringListData) : RecurringListState()
    data class Error(val message: String) : RecurringListState()
    data object Empty : RecurringListState()
}

class RecurringViewModel(
    private val recurringRepository: RecurringRepository,
    private val billingManager: BillingManager
) : ViewModel() {

    private val _state = MutableStateFlow<RecurringListState>(RecurringListState.Loading)
    val state: StateFlow<RecurringListState> = _state.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    private val _filteredCategory = MutableStateFlow<String?>(null)

    companion object {
        const val FREE_ITEM_LIMIT = 5
    }

    init {
        loadRecurringItems()
    }

    private fun loadRecurringItems() {
        viewModelScope.launch {
            try {
                combine(
                    recurringRepository.getAllRecurringItems(),
                    billingManager.subscriptionStatus,
                    _searchQuery,
                    _filteredCategory
                ) { items, subscriptionStatus, query, category ->
                    val isPro = subscriptionStatus is SubscriptionStatus.Active
                    
                    var filtered = items
                    
                    if (query.isNotBlank()) {
                        filtered = filtered.filter { 
                            it.merchant.contains(query, ignoreCase = true) ||
                            it.category.contains(query, ignoreCase = true)
                        }
                    }
                    
                    if (category != null) {
                        filtered = filtered.filter { it.category == category }
                    }
                    
                    RecurringListData(
                        items = filtered,
                        isPro = isPro,
                        filteredCategory = category,
                        searchQuery = query
                    )
                }.collect { data ->
                    if (data.items.isEmpty()) {
                        _state.value = RecurringListState.Empty
                    } else {
                        _state.value = RecurringListState.Success(data)
                    }
                }
            } catch (e: Exception) {
                _state.value = RecurringListState.Error(e.message ?: "Failed to load items")
            }
        }
    }

    fun search(query: String) {
        _searchQuery.value = query
    }

    fun filterByCategory(category: String?) {
        _filteredCategory.value = category
    }

    fun clearFilters() {
        _searchQuery.value = ""
        _filteredCategory.value = null
    }
}
