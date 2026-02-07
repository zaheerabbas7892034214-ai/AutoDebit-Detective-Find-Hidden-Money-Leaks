package com.zaheer.autodebitdetective.security

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.zaheer.autodebitdetective.data.datastore.PreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.MessageDigest

sealed class AuthenticationResult {
    data object Success : AuthenticationResult()
    data class Error(val message: String) : AuthenticationResult()
    data object Failed : AuthenticationResult()
    data object Cancelled : AuthenticationResult()
}

sealed class BiometricAvailability {
    data object Available : BiometricAvailability()
    data object NotAvailable : BiometricAvailability()
    data object NotEnrolled : BiometricAvailability()
    data object SecurityNotEnabled : BiometricAvailability()
}

class BiometricManager(
    private val context: Context
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val preferencesManager = PreferencesManager(context)
    
    private val _authenticationState = MutableStateFlow<AuthenticationResult?>(null)
    val authenticationState: StateFlow<AuthenticationResult?> = _authenticationState.asStateFlow()
    
    private val _isLocked = MutableStateFlow(false)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()
    
    private var lastUnlockTime = 0L
    
    fun checkBiometricAvailability(): BiometricAvailability {
        val biometricManager = BiometricManager.from(context)
        
        return when (biometricManager.canAuthenticate(AUTHENTICATORS)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                Log.d(TAG, "Biometric authentication available")
                BiometricAvailability.Available
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Log.w(TAG, "No biometric hardware available")
                BiometricAvailability.NotAvailable
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Log.w(TAG, "Biometric hardware unavailable")
                BiometricAvailability.NotAvailable
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                Log.w(TAG, "No biometric credentials enrolled")
                BiometricAvailability.NotEnrolled
            }
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {
                Log.w(TAG, "Security update required")
                BiometricAvailability.SecurityNotEnabled
            }
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> {
                Log.w(TAG, "Biometric not supported")
                BiometricAvailability.NotAvailable
            }
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> {
                Log.w(TAG, "Biometric status unknown")
                BiometricAvailability.NotAvailable
            }
            else -> {
                Log.w(TAG, "Unknown biometric availability status")
                BiometricAvailability.NotAvailable
            }
        }
    }
    
    fun showBiometricPrompt(
        activity: FragmentActivity,
        title: String = "Unlock AutoDebit Detective",
        subtitle: String = "Authenticate to access your data",
        negativeButtonText: String = "Cancel"
    ) {
        val executor = ContextCompat.getMainExecutor(context)
        
        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    Log.d(TAG, "Authentication succeeded")
                    handleSuccessfulAuthentication()
                }
                
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    
                    when (errorCode) {
                        BiometricPrompt.ERROR_USER_CANCELED,
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                        BiometricPrompt.ERROR_CANCELED -> {
                            Log.d(TAG, "Authentication cancelled")
                            _authenticationState.value = AuthenticationResult.Cancelled
                        }
                        BiometricPrompt.ERROR_LOCKOUT,
                        BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> {
                            Log.w(TAG, "Authentication lockout: $errString")
                            _authenticationState.value = AuthenticationResult.Error("Too many attempts. Please try again later.")
                        }
                        else -> {
                            Log.e(TAG, "Authentication error: $errorCode - $errString")
                            _authenticationState.value = AuthenticationResult.Error(errString.toString())
                        }
                    }
                }
                
                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Log.d(TAG, "Authentication failed")
                    _authenticationState.value = AuthenticationResult.Failed
                }
            }
        )
        
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText(negativeButtonText)
            .setAllowedAuthenticators(AUTHENTICATORS)
            .build()
        
        biometricPrompt.authenticate(promptInfo)
    }
    
    private fun handleSuccessfulAuthentication() {
        lastUnlockTime = System.currentTimeMillis()
        _isLocked.value = false
        _authenticationState.value = AuthenticationResult.Success
        
        scope.launch {
            preferencesManager.setLastUnlockTime(lastUnlockTime)
        }
    }
    
    suspend fun validatePIN(enteredPIN: String): Boolean = withContext(Dispatchers.IO) {
        val storedPINHash = preferencesManager.pinHash.first()
        
        if (storedPINHash.isNullOrBlank()) {
            Log.w(TAG, "No PIN configured")
            return@withContext false
        }
        
        val enteredPINHash = hashPIN(enteredPIN)
        val isValid = enteredPINHash == storedPINHash
        
        if (isValid) {
            withContext(Dispatchers.Main) {
                handleSuccessfulAuthentication()
            }
        }
        
        isValid
    }
    
    suspend fun setPIN(pin: String): Boolean = withContext(Dispatchers.IO) {
        if (pin.length !in 4..6 || !pin.all { it.isDigit() }) {
            Log.w(TAG, "Invalid PIN format")
            return@withContext false
        }
        
        val pinHash = hashPIN(pin)
        preferencesManager.setPinHash(pinHash)
        Log.d(TAG, "PIN set successfully")
        true
    }
    
    suspend fun clearPIN() {
        preferencesManager.setPinHash(null)
        Log.d(TAG, "PIN cleared")
    }
    
    suspend fun checkAutoLock(): Boolean {
        val autoLockEnabled = preferencesManager.biometricEnabled.first()
        if (!autoLockEnabled) {
            _isLocked.value = false
            return false
        }
        
        val autoLockTimeout = preferencesManager.autoLockTimeout.first()
        if (autoLockTimeout <= 0) {
            _isLocked.value = false
            return false
        }
        
        val lastUnlock = preferencesManager.lastUnlockTime.first()
        val currentTime = System.currentTimeMillis()
        val timeoutMillis = autoLockTimeout * 60 * 1000L
        
        val shouldLock = (currentTime - lastUnlock) > timeoutMillis
        _isLocked.value = shouldLock
        
        if (shouldLock) {
            Log.d(TAG, "App auto-locked after timeout")
        }
        
        return shouldLock
    }
    
    fun manualLock() {
        _isLocked.value = true
        Log.d(TAG, "App manually locked")
    }
    
    fun clearAuthenticationState() {
        _authenticationState.value = null
    }
    
    private fun hashPIN(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(pin.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
    
    companion object {
        private const val TAG = "BiometricManager"
        
        private const val AUTHENTICATORS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        } else {
            BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        }
    }
}
