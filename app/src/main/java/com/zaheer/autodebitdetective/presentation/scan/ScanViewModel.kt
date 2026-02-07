package com.zaheer.autodebitdetective.presentation.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zaheer.autodebitdetective.data.repository.RecurringRepository
import com.zaheer.autodebitdetective.data.repository.SMSRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ScanState {
    data object Idle : ScanState()
    data class Scanning(val progress: Int, val message: String) : ScanState()
    data class Success(val itemsFound: Int) : ScanState()
    data class Error(val message: String) : ScanState()
}

class ScanViewModel(
    private val smsRepository: SMSRepository,
    private val recurringRepository: RecurringRepository
) : ViewModel() {

    private val _state = MutableStateFlow<ScanState>(ScanState.Idle)
    val state: StateFlow<ScanState> = _state.asStateFlow()

    private var scanJob: kotlinx.coroutines.Job? = null

    fun startScan() {
        scanJob?.cancel()
        scanJob = viewModelScope.launch {
            try {
                _state.value = ScanState.Scanning(0, "Scanning SMS messages...")
                kotlinx.coroutines.delay(500)

                val messages = smsRepository.scanTransactionSMS()
                
                _state.value = ScanState.Scanning(33, "Parsing ${messages.size} messages...")
                kotlinx.coroutines.delay(500)

                val transactions = smsRepository.parseTransactions(messages)
                
                _state.value = ScanState.Scanning(66, "Detecting recurring patterns...")
                kotlinx.coroutines.delay(500)

                val result = recurringRepository.detectAndSaveRecurringPatterns(transactions)
                
                if (result.isSuccess) {
                    val recurringItems = result.getOrNull() ?: emptyList()
                    _state.value = ScanState.Success(recurringItems.size)
                } else {
                    _state.value = ScanState.Error(result.exceptionOrNull()?.message ?: "Scan failed")
                }
            } catch (e: Exception) {
                _state.value = ScanState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun cancelScan() {
        scanJob?.cancel()
        _state.value = ScanState.Idle
    }

    fun resetState() {
        _state.value = ScanState.Idle
    }
}
