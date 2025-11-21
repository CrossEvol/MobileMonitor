package me.crossevol.mobilemonitor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import me.crossevol.mobilemonitor.model.AppInfo
import me.crossevol.mobilemonitor.repository.AppRestrictionRepository

/**
 * UI state for the home screen
 * 
 * @param apps List of monitored apps
 * @param isLoading Whether data is currently being loaded
 * @param error Error message if loading failed
 */
data class HomeUiState(
    val apps: List<AppInfo> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel for the home screen
 * Manages the list of monitored apps and their state
 * 
 * @param repository Repository for app restriction operations
 */
class HomeViewModel(
    private val repository: AppRestrictionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadApps()
    }

    /**
     * Load all apps from the repository
     */
    fun loadApps() {
        viewModelScope.launch {
            _uiState.value = HomeUiState(isLoading = true)
            
            repository.getAllApps()
                .catch { exception ->
                    _uiState.value = HomeUiState(
                        isLoading = false,
                        error = exception.message ?: "Failed to load apps"
                    )
                }
                .collect { apps ->
                    _uiState.value = HomeUiState(
                        apps = apps,
                        isLoading = false,
                        error = null
                    )
                }
        }
    }

    /**
     * Retry loading apps after an error
     */
    fun retry() {
        loadApps()
    }
}
