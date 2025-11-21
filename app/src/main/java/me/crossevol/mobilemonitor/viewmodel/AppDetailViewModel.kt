package me.crossevol.mobilemonitor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import me.crossevol.mobilemonitor.model.AppInfo
import me.crossevol.mobilemonitor.model.AppRule
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
 */
data class AppDetailUiState(
    val app: AppInfo? = null,
    val rules: List<AppRule> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showDeleteDialog: Boolean = false,
    val ruleToDelete: AppRule? = null
)

/**
 * ViewModel for the app detail screen
 * Manages app information, rules list, and user interactions
 * 
 * @param packageName The package name of the app to display (will create if not exists)
 * @param repository Repository for app restriction operations
 */
class AppDetailViewModel(
    private val packageName: String,
    private val repository: AppRestrictionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppDetailUiState(isLoading = true))
    val uiState: StateFlow<AppDetailUiState> = _uiState.asStateFlow()
    
    private var currentAppId: Long = 0

    init {
        loadAppDetails()
    }

    /**
     * Load app information and rules from the repository
     * Creates a new app entry if it doesn't exist
     */
    private fun loadAppDetails() {
        viewModelScope.launch {
            _uiState.value = AppDetailUiState(isLoading = true)
            
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
                        error = "Failed to load app"
                    )
                    return@launch
                }
                
                // Combine app info with rules flow
                repository.getRulesForApp(currentAppId)
                    .catch { exception ->
                        _uiState.value = AppDetailUiState(
                            app = app,
                            isLoading = false,
                            error = exception.message ?: "Failed to load rules"
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
                    error = e.message ?: "Failed to load app details"
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
     * Retry loading app details after an error
     */
    fun retry() {
        loadAppDetails()
    }
}
