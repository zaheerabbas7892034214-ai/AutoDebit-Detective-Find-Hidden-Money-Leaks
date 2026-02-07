package com.zaheer.autodebitdetective.presentation.permission

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zaheer.autodebitdetective.data.datastore.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class PermissionState {
    data object NotRequested : PermissionState()
    data object Granted : PermissionState()
    data object Denied : PermissionState()
    data object PermanentlyDenied : PermissionState()
}

class PermissionViewModel(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _permissionState = MutableStateFlow<PermissionState>(PermissionState.NotRequested)
    val permissionState: StateFlow<PermissionState> = _permissionState.asStateFlow()

    fun onPermissionResult(granted: Boolean, shouldShowRationale: Boolean) {
        _permissionState.value = when {
            granted -> PermissionState.Granted
            shouldShowRationale -> PermissionState.Denied
            else -> PermissionState.PermanentlyDenied
        }
    }

    fun onContinueWithoutPermission() {
        viewModelScope.launch {
            preferencesManager.setOnboardingCompleted(true)
        }
    }

    fun onPermissionGranted() {
        viewModelScope.launch {
            preferencesManager.setOnboardingCompleted(true)
        }
    }
}
