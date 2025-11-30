package me.crossevol.mobilemonitor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.crossevol.mobilemonitor.model.TimeFilter
import me.crossevol.mobilemonitor.model.UsageStatsState
import me.crossevol.mobilemonitor.repository.UsageStatsRepository

/**
 * ViewModel for managing usage statistics UI state and business logic
 * 
 * Handles data loading, permission checking, filter changes, and error states
 * using coroutines and StateFlow for reactive UI updates.
 */
class UsageStatsViewModel(
    private val repository: UsageStatsRepository
) : ViewModel() {
    
    // Private mutable state for UI state
    private val _uiState = MutableStateFlow<UsageStatsState>(UsageStatsState.Loading)
    
    /**
     * Public read-only StateFlow for UI state
     * Emits Loading, Success, Error, or PermissionRequired states
     */
    val uiState: StateFlow<UsageStatsState> = _uiState.asStateFlow()
    
    // Private mutable state for selected time filter
    private val _selectedFilter = MutableStateFlow(TimeFilter.DAILY)
    
    /**
     * Public read-only StateFlow for selected time filter
     * Emits the currently selected time filter option
     */
    val selectedFilter: StateFlow<TimeFilter> = _selectedFilter.asStateFlow()
    
    init {
        // Load initial data when ViewModel is created
        loadUsageStats(_selectedFilter.value)
    }
    
    /**
     * Loads usage statistics for the specified time filter
     * 
     * Updates UI state to Loading, then Success/Error/PermissionRequired based on result.
     * Handles permission checking and error scenarios gracefully.
     * 
     * @param filter The time filter to apply when loading usage statistics
     */
    fun loadUsageStats(filter: TimeFilter) {
        viewModelScope.launch {
            try {
                // Set loading state
                _uiState.value = UsageStatsState.Loading
                
                // Update selected filter
                _selectedFilter.value = filter
                
                // Check both permissions first
                if (!repository.hasUsageStatsPermission() || !repository.hasOverlayPermission()) {
                    _uiState.value = UsageStatsState.PermissionRequired
                    return@launch
                }
                
                // Load usage statistics
                val result = repository.getUsageStats(filter)
                
                result.fold(
                    onSuccess = { apps ->
                        _uiState.value = UsageStatsState.Success(apps)
                    },
                    onFailure = { exception ->
                        when (exception) {
                            is SecurityException -> {
                                _uiState.value = UsageStatsState.PermissionRequired
                            }
                            else -> {
                                _uiState.value = UsageStatsState.Error(
                                    exception.message ?: "Failed to load usage statistics"
                                )
                            }
                        }
                    }
                )
            } catch (e: Exception) {
                // Handle any unexpected exceptions
                _uiState.value = UsageStatsState.Error(
                    "An unexpected error occurred: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Refreshes the current usage statistics data
     * 
     * Reloads data using the currently selected time filter.
     * Useful for pull-to-refresh functionality or manual refresh buttons.
     */
    fun refreshData() {
        loadUsageStats(_selectedFilter.value)
    }
    
    /**
     * Changes the time filter and loads new data
     * 
     * Updates the selected filter and triggers a new data load.
     * UI state will transition through Loading -> Success/Error/PermissionRequired.
     * 
     * @param newFilter The new time filter to apply
     */
    fun changeFilter(newFilter: TimeFilter) {
        if (newFilter != _selectedFilter.value) {
            loadUsageStats(newFilter)
        }
    }
    
    /**
     * Checks current permission status and updates UI state accordingly
     * 
     * Should be called when returning from system settings or when app resumes.
     * If both permissions are granted, automatically refreshes data.
     */
    fun checkPermissions() {
        viewModelScope.launch {
            try {
                val hasUsageStats = repository.hasUsageStatsPermission()
                val hasOverlay = repository.hasOverlayPermission()
                
                if (hasUsageStats && hasOverlay) {
                    // Both permissions are now available, refresh data
                    refreshData()
                } else {
                    // One or both permissions still not granted
                    _uiState.value = UsageStatsState.PermissionRequired
                }
            } catch (e: Exception) {
                _uiState.value = UsageStatsState.Error(
                    "Failed to check permissions: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Handles retry functionality for error states
     * 
     * Attempts to reload data with the current filter.
     * Useful for retry buttons in error UI states.
     */
    fun retry() {
        refreshData()
    }
}