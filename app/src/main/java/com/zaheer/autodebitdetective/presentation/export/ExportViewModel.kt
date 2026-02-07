package com.zaheer.autodebitdetective.presentation.export

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zaheer.autodebitdetective.data.repository.ExportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

data class ExportHistoryItem(
    val fileName: String,
    val format: String,
    val timestamp: Long,
    val filePath: String
)

sealed class ExportState {
    data object Idle : ExportState()
    data object Exporting : ExportState()
    data class Success(val filePath: String, val format: String) : ExportState()
    data class Error(val message: String) : ExportState()
}

data class ExportScreenData(
    val history: List<ExportHistoryItem>,
    val exportState: ExportState
)

class ExportViewModel(
    private val exportRepository: ExportRepository
) : ViewModel() {

    private val _state = MutableStateFlow<ExportState>(ExportState.Idle)
    val state: StateFlow<ExportState> = _state.asStateFlow()

    private val _history = MutableStateFlow<List<ExportHistoryItem>>(emptyList())
    val history: StateFlow<List<ExportHistoryItem>> = _history.asStateFlow()

    init {
        loadExportHistory()
    }

    private fun loadExportHistory() {
        viewModelScope.launch {
            try {
                val historyItems = exportRepository.getExportHistory()
                _history.value = historyItems
            } catch (e: Exception) {
            }
        }
    }

    fun exportCSV() {
        viewModelScope.launch {
            try {
                _state.value = ExportState.Exporting
                
                val result = exportRepository.exportToCSV()
                
                if (result.isSuccess) {
                    val file = result.getOrNull()
                    if (file != null) {
                        _state.value = ExportState.Success(file.absolutePath, "CSV")
                        loadExportHistory()
                    } else {
                        _state.value = ExportState.Error("Failed to create CSV file")
                    }
                } else {
                    _state.value = ExportState.Error(
                        result.exceptionOrNull()?.message ?: "Export failed"
                    )
                }
            } catch (e: Exception) {
                _state.value = ExportState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun exportPDF() {
        viewModelScope.launch {
            try {
                _state.value = ExportState.Exporting
                
                val result = exportRepository.exportToPDF()
                
                if (result.isSuccess) {
                    val file = result.getOrNull()
                    if (file != null) {
                        _state.value = ExportState.Success(file.absolutePath, "PDF")
                        loadExportHistory()
                    } else {
                        _state.value = ExportState.Error("Failed to create PDF file")
                    }
                } else {
                    _state.value = ExportState.Error(
                        result.exceptionOrNull()?.message ?: "Export failed"
                    )
                }
            } catch (e: Exception) {
                _state.value = ExportState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun resetState() {
        _state.value = ExportState.Idle
    }
}
