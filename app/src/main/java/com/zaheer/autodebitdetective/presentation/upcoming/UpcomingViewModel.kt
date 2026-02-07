package com.zaheer.autodebitdetective.presentation.upcoming

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zaheer.autodebitdetective.data.repository.RecurringRepository
import com.zaheer.autodebitdetective.domain.model.UpcomingCharge
import com.zaheer.autodebitdetective.domain.usecase.PredictUpcomingChargesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class UpcomingState {
    data object Loading : UpcomingState()
    data class Success(val charges: List<UpcomingCharge>) : UpcomingState()
    data class Error(val message: String) : UpcomingState()
    data object Empty : UpcomingState()
}

class UpcomingViewModel(
    private val recurringRepository: RecurringRepository,
    private val predictUpcomingChargesUseCase: PredictUpcomingChargesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<UpcomingState>(UpcomingState.Loading)
    val state: StateFlow<UpcomingState> = _state.asStateFlow()

    init {
        loadUpcomingCharges()
    }

    private fun loadUpcomingCharges() {
        viewModelScope.launch {
            try {
                _state.value = UpcomingState.Loading
                
                recurringRepository.getAllRecurringItems().collect { items ->
                    val result = predictUpcomingChargesUseCase(items)
                    
                    if (result.isSuccess) {
                        val charges = result.getOrNull() ?: emptyList()
                        _state.value = if (charges.isEmpty()) {
                            UpcomingState.Empty
                        } else {
                            UpcomingState.Success(charges.sortedBy { it.predictedDate })
                        }
                    } else {
                        _state.value = UpcomingState.Error(
                            result.exceptionOrNull()?.message ?: "Failed to predict charges"
                        )
                    }
                }
            } catch (e: Exception) {
                _state.value = UpcomingState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun toggleAlert(chargeId: Long, enabled: Boolean) {
        viewModelScope.launch {
        }
    }

    fun refresh() {
        loadUpcomingCharges()
    }
}
