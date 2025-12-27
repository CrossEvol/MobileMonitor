package me.crossevol.mobilemonitor.viewmodel

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import me.crossevol.mobilemonitor.model.AppInfo
import me.crossevol.mobilemonitor.model.AppRule
import me.crossevol.mobilemonitor.model.ViewMode
import me.crossevol.mobilemonitor.repository.AppRestrictionRepository

/**
 * UI state for the app detail screen
 * 
 * @param app The app being displayed
 * @param rules List of usage rules for this app
 * @param isLoading Whether data is currently being loaded
 * @param error Error message if loading failed
 * @param showDeleteDialog Whether to show the delete confirmation dialog
 * @param ruleToDelete The rule pending deletion
 * @param viewMode Current view mode (list or grid)
 */
data class AppDetailUiState(
    val app: AppInfo? = null,
    val rules: List<AppRule> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showDeleteDialog: Boolean = false,
    val ruleToDelete: AppRule? = null,
    val showDeleteAllDialog: Boolean = false,
    val viewMode: ViewMode = ViewMode.LIST
)

/**
 * ViewModel for the app detail screen
 * Manages app information, rules list, and user interactions
 * Supports view mode persistence using SharedPreferences
 * 
 * @param packageName The package name of the app to display (will create if not exists)
 * @param repository Repository for app restriction operations
 * @param context Application context for accessing SharedPreferences
 */
class AppDetailViewModel(
    private val packageName: String,
    private val repository: AppRestrictionRepository,
    private val context: Context
) : ViewModel() {

    companion object {
        private const val PREFS_NAME = "app_detail_prefs"
        private const val KEY_VIEW_MODE = "view_mode"
    }

    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(AppDetailUiState(
        isLoading = true,
        viewMode = getPersistedViewMode()
    ))
    val uiState: StateFlow<AppDetailUiState> = _uiState.asStateFlow()
    
    private var currentAppId: Long = 0

    init {
        loadAppDetails()
    }

    /**
     * Load app information and rules from the repository
     * Creates a new app entry if it doesn't exist
     * Sets up reactive data flow for automatic UI updates
     */
    private fun loadAppDetails() {
        viewModelScope.launch {
            _uiState.value = AppDetailUiState(
                isLoading = true,
                viewMode = getPersistedViewMode()
            )
            
            try {
                // Try to load existing app by package name
                var app = repository.getAppByPackageName(packageName)
                
                // If app doesn't exist, create it using repository's getAppInfo
                if (app == null) {
                    // Repository should handle getting app info from system
                    val appInfo = repository.getAppByPackageName(packageName)
                    
                    if (appInfo != null) {
                        currentAppId = appInfo.id
                        app = appInfo
                    } else {
                        // Create a basic app entry - repository will fill in details
                        val newApp = AppInfo(
                            appName = packageName, // Will be updated by repository
                            packageName = packageName
                        )
                        currentAppId = repository.saveApp(newApp)
                        app = repository.getAppById(currentAppId)
                    }
                } else {
                    currentAppId = app.id
                }
                
                if (app == null) {
                    _uiState.value = AppDetailUiState(
                        isLoading = false,
                        error = "Failed to load app",
                        viewMode = getPersistedViewMode()
                    )
                    return@launch
                }
                
                // Set up reactive data flow - rules will automatically update UI when changed
                repository.getRulesForApp(currentAppId)
                    .catch { exception ->
                        _uiState.value = AppDetailUiState(
                            app = app,
                            isLoading = false,
                            error = exception.message ?: "Failed to load rules",
                            viewMode = _uiState.value.viewMode
                        )
                    }
                    .collect { rules ->
                        _uiState.value = _uiState.value.copy(
                            app = app,
                            rules = rules,
                            isLoading = false,
                            error = null
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = AppDetailUiState(
                    isLoading = false,
                    error = e.message ?: "Failed to load app details",
                    viewMode = getPersistedViewMode()
                )
            }
        }
    }

    /**
     * Toggle the enabled state of the app
     * 
     * @param enabled New enabled state
     */
    fun toggleAppEnabled(enabled: Boolean) {
        viewModelScope.launch {
            try {
                repository.updateAppEnabled(currentAppId, enabled)
                // Update local state
                _uiState.value = _uiState.value.copy(
                    app = _uiState.value.app?.copy(enabled = enabled)
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to update app status"
                )
            }
        }
    }

    /**
     * Show the delete confirmation dialog for a rule
     * 
     * @param rule The rule to delete
     */
    fun showDeleteDialog(rule: AppRule) {
        _uiState.value = _uiState.value.copy(
            showDeleteDialog = true,
            ruleToDelete = rule
        )
    }

    /**
     * Hide the delete confirmation dialog
     */
    fun hideDeleteDialog() {
        _uiState.value = _uiState.value.copy(
            showDeleteDialog = false,
            ruleToDelete = null
        )
    }

    /**
     * Confirm deletion of the pending rule
     */
    fun confirmDeleteRule() {
        val rule = _uiState.value.ruleToDelete ?: return
        
        viewModelScope.launch {
            try {
                repository.deleteRule(rule.id)
                hideDeleteDialog()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to delete rule",
                    showDeleteDialog = false,
                    ruleToDelete = null
                )
            }
        }
    }

    /**
     * Show the delete all confirmation dialog
     */
    fun showDeleteAllDialog() {
        _uiState.value = _uiState.value.copy(showDeleteAllDialog = true)
    }

    /**
     * Hide the delete all confirmation dialog
     */
    fun hideDeleteAllDialog() {
        _uiState.value = _uiState.value.copy(showDeleteAllDialog = false)
    }

    /**
     * Confirm deletion of all rules for the current app
     */
    fun confirmDeleteAllRules() {
        viewModelScope.launch {
            try {
                repository.deleteAllRulesForApp(currentAppId)
                hideDeleteAllDialog()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to delete all rules",
                    showDeleteAllDialog = false
                )
            }
        }
    }

    /**
     * Retry loading app details after an error
     */
    fun retry() {
        loadAppDetails()
    }
    
    /**
     * Clear error state
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    /**
     * Toggle between list and grid view modes
     * Persists the selected view mode to SharedPreferences
     */
    fun toggleViewMode() {
        val currentMode = _uiState.value.viewMode
        val newMode = when (currentMode) {
            ViewMode.LIST -> ViewMode.GRID
            ViewMode.GRID -> ViewMode.LIST
        }
        
        // Update UI state
        _uiState.value = _uiState.value.copy(viewMode = newMode)
        
        // Persist the view mode preference
        persistViewMode(newMode)
    }
    
    /**
     * Get the persisted view mode from SharedPreferences
     * 
     * @return The persisted ViewMode, defaults to LIST if not found
     */
    private fun getPersistedViewMode(): ViewMode {
        val modeString = sharedPreferences.getString(KEY_VIEW_MODE, ViewMode.LIST.name)
        return try {
            ViewMode.valueOf(modeString ?: ViewMode.LIST.name)
        } catch (e: IllegalArgumentException) {
            ViewMode.LIST
        }
    }
    
    /**
     * Persist the view mode to SharedPreferences
     * 
     * @param viewMode The ViewMode to persist
     */
    private fun persistViewMode(viewMode: ViewMode) {
        sharedPreferences.edit()
            .putString(KEY_VIEW_MODE, viewMode.name)
            .apply()
    }
}
