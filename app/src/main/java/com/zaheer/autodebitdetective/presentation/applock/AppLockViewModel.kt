package com.zaheer.autodebitdetective.presentation.applock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zaheer.autodebitdetective.data.datastore.PreferencesManager
import com.zaheer.autodebitdetective.security.BiometricManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.security.MessageDigest

sealed class AppLockMode {
    data object Setup : AppLockMode()
    data object Verify : AppLockMode()
}

sealed class AppLockState {
    data object EnterPin : AppLockState()
    data object ConfirmPin : AppLockState()
    data object Success : AppLockState()
    data class Error(val message: String) : AppLockState()
}

class AppLockViewModel(
    private val preferencesManager: PreferencesManager,
    private val biometricManager: BiometricManager
) : ViewModel() {

    private val _mode = MutableStateFlow<AppLockMode>(AppLockMode.Verify)
    val mode: StateFlow<AppLockMode> = _mode.asStateFlow()

    private val _state = MutableStateFlow<AppLockState>(AppLockState.EnterPin)
    val state: StateFlow<AppLockState> = _state.asStateFlow()

    private val _pin = MutableStateFlow("")
    val pin: StateFlow<String> = _pin.asStateFlow()

    private var setupPin: String? = null

    init {
        checkSetupMode()
    }

    private fun checkSetupMode() {
        viewModelScope.launch {
            val storedPinHash = preferencesManager.pinHash.first()
            _mode.value = if (storedPinHash == null) {
                AppLockMode.Setup
            } else {
                AppLockMode.Verify
            }
        }
    }

    fun onDigitEntered(digit: Int) {
        if (_pin.value.length < 4) {
            _pin.value += digit.toString()
            
            if (_pin.value.length == 4) {
                processPin()
            }
        }
    }

    fun onBackspace() {
        if (_pin.value.isNotEmpty()) {
            _pin.value = _pin.value.dropLast(1)
        }
    }

    private fun processPin() {
        viewModelScope.launch {
            when (_mode.value) {
                is AppLockMode.Setup -> {
                    when (_state.value) {
                        is AppLockState.EnterPin -> {
                            setupPin = _pin.value
                            _state.value = AppLockState.ConfirmPin
                            _pin.value = ""
                        }
                        is AppLockState.ConfirmPin -> {
                            if (_pin.value == setupPin) {
                                val hash = hashPin(_pin.value)
                                preferencesManager.setPinHash(hash)
                                _state.value = AppLockState.Success
                            } else {
                                _state.value = AppLockState.Error("PINs don't match. Try again.")
                                setupPin = null
                                _pin.value = ""
                                kotlinx.coroutines.delay(1500)
                                _state.value = AppLockState.EnterPin
                            }
                        }
                        else -> {}
                    }
                }
                is AppLockMode.Verify -> {
                    val storedHash = preferencesManager.pinHash.first()
                    val enteredHash = hashPin(_pin.value)
                    
                    if (storedHash == enteredHash) {
                        _state.value = AppLockState.Success
                    } else {
                        _state.value = AppLockState.Error("Incorrect PIN")
                        _pin.value = ""
                        kotlinx.coroutines.delay(1000)
                        _state.value = AppLockState.EnterPin
                    }
                }
            }
        }
    }

    fun onBiometricSuccess() {
        _state.value = AppLockState.Success
    }

    fun onBiometricError(error: String) {
        _state.value = AppLockState.Error(error)
        viewModelScope.launch {
            kotlinx.coroutines.delay(1500)
            _state.value = AppLockState.EnterPin
        }
    }

    fun resetPin() {
        _pin.value = ""
        _state.value = AppLockState.EnterPin
    }

    private fun hashPin(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(pin.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }
}
