package me.crossevol.mobilemonitor.viewmodel

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * UI state for the settings screen
 * 
 * @param monitoringEnabled Whether global monitoring is enabled
 * @param showRestartDialog Whether to show the restart confirmation dialog
 */
data class SettingsUiState(
    val monitoringEnabled: Boolean = true,
    val showRestartDialog: Boolean = false
)

/**
 * ViewModel for the settings screen
 * Manages global monitoring preferences and restart confirmation
 * 
 * @param context Application context for accessing SharedPreferences
 */
class SettingsViewModel(
    private val context: Context
) : ViewModel() {

    companion object {
        private const val PREFS_NAME = "app_monitoring_prefs"
        private const val KEY_MONITORING_ENABLED = "monitoring_enabled"
    }

    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            monitoringEnabled = getMonitoringEnabled()
        )
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    /**
     * Get the current monitoring enabled state from SharedPreferences
     */
    private fun getMonitoringEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_MONITORING_ENABLED, true)
    }

    /**
     * Called when user toggles the monitoring switch
     * Shows the restart confirmation dialog
     */
    fun onMonitoringToggleChanged(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(
            monitoringEnabled = enabled,
            showRestartDialog = true
        )
    }

    /**
     * Called when user confirms the restart
     * Saves the preference and returns true to indicate app should restart
     */
    fun onRestartConfirmed(): Boolean {
        sharedPreferences.edit()
            .putBoolean(KEY_MONITORING_ENABLED, _uiState.value.monitoringEnabled)
            .apply()
        
        _uiState.value = _uiState.value.copy(showRestartDialog = false)
        return true
    }

    /**
     * Called when user cancels the restart
     * Reverts the toggle to its previous state
     */
    fun onRestartCancelled() {
        _uiState.value = _uiState.value.copy(
            monitoringEnabled = getMonitoringEnabled(),
            showRestartDialog = false
        )
    }

    /**
     * Dismiss the restart dialog without making changes
     */
    fun dismissRestartDialog() {
        onRestartCancelled()
    }
}
