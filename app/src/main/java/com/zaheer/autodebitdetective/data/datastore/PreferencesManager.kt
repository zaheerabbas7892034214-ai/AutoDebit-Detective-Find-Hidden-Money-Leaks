package com.zaheer.autodebitdetective.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")

class PreferencesManager(private val context: Context) {

    companion object {
        private val ALERT_ENABLED = booleanPreferencesKey("alert_enabled")
        private val NOTIFICATION_PRIVACY = booleanPreferencesKey("notification_privacy")
        private val APP_LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled")
        private val APP_LOCK_PIN = stringPreferencesKey("app_lock_pin")
        private val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        private val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        private val LAST_SYNC_TIMESTAMP = longPreferencesKey("last_sync_timestamp")
        private val ALERT_DAYS_BEFORE = intPreferencesKey("alert_days_before")
        private val THEME_MODE = stringPreferencesKey("theme_mode")
        private val EXPORT_FORMAT = stringPreferencesKey("export_format")
        private val LARGE_CHARGE_THRESHOLD = doublePreferencesKey("large_charge_threshold")
        private val AUTO_LOCK_TIMEOUT = intPreferencesKey("auto_lock_timeout")
        private val LAST_UNLOCK_TIME = longPreferencesKey("last_unlock_time")
        private val PIN_HASH = stringPreferencesKey("pin_hash")
    }

    val alertsEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[ALERT_ENABLED] ?: true }

    val hideNotificationContent: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[NOTIFICATION_PRIVACY] ?: false }

    val appLockEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[APP_LOCK_ENABLED] ?: false }

    val appLockPin: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[APP_LOCK_PIN] }

    val biometricEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[BIOMETRIC_ENABLED] ?: false }

    val onboardingCompleted: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[ONBOARDING_COMPLETED] ?: false }

    val lastSyncTimestamp: Flow<Long> = context.dataStore.data
        .map { preferences -> preferences[LAST_SYNC_TIMESTAMP] ?: 0L }

    val alertDaysBefore: Flow<Int> = context.dataStore.data
        .map { preferences -> preferences[ALERT_DAYS_BEFORE] ?: 3 }

    val themeMode: Flow<String> = context.dataStore.data
        .map { preferences -> preferences[THEME_MODE] ?: "system" }

    val exportFormat: Flow<String> = context.dataStore.data
        .map { preferences -> preferences[EXPORT_FORMAT] ?: "csv" }

    val largeChargeThreshold: Flow<Double> = context.dataStore.data
        .map { preferences -> preferences[LARGE_CHARGE_THRESHOLD] ?: 5000.0 }

    val autoLockTimeout: Flow<Int> = context.dataStore.data
        .map { preferences -> preferences[AUTO_LOCK_TIMEOUT] ?: 5 }

    val lastUnlockTime: Flow<Long> = context.dataStore.data
        .map { preferences -> preferences[LAST_UNLOCK_TIME] ?: 0L }

    val pinHash: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[PIN_HASH] }

    suspend fun setAlertEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ALERT_ENABLED] = enabled
        }
    }

    suspend fun setNotificationPrivacy(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATION_PRIVACY] = enabled
        }
    }

    suspend fun setAppLockEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[APP_LOCK_ENABLED] = enabled
        }
    }

    suspend fun setAppLockPin(pin: String?) {
        context.dataStore.edit { preferences ->
            if (pin != null) {
                preferences[APP_LOCK_PIN] = pin
            } else {
                preferences.remove(APP_LOCK_PIN)
            }
        }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[BIOMETRIC_ENABLED] = enabled
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun setLastSyncTimestamp(timestamp: Long) {
        context.dataStore.edit { preferences ->
            preferences[LAST_SYNC_TIMESTAMP] = timestamp
        }
    }

    suspend fun setAlertDaysBefore(days: Int) {
        context.dataStore.edit { preferences ->
            preferences[ALERT_DAYS_BEFORE] = days
        }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE] = mode
        }
    }

    suspend fun setExportFormat(format: String) {
        context.dataStore.edit { preferences ->
            preferences[EXPORT_FORMAT] = format
        }
    }

    suspend fun setLargeChargeThreshold(threshold: Double) {
        context.dataStore.edit { preferences ->
            preferences[LARGE_CHARGE_THRESHOLD] = threshold
        }
    }

    suspend fun setAutoLockTimeout(minutes: Int) {
        context.dataStore.edit { preferences ->
            preferences[AUTO_LOCK_TIMEOUT] = minutes
        }
    }

    suspend fun setLastUnlockTime(timestamp: Long) {
        context.dataStore.edit { preferences ->
            preferences[LAST_UNLOCK_TIME] = timestamp
        }
    }

    suspend fun setPinHash(hash: String?) {
        context.dataStore.edit { preferences ->
            if (hash != null) {
                preferences[PIN_HASH] = hash
            } else {
                preferences.remove(PIN_HASH)
            }
        }
    }

    suspend fun clearAllPreferences() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
