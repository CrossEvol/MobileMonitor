package me.crossevol.mobilemonitor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import me.crossevol.mobilemonitor.repository.AppRestrictionRepository

/**
 * Factory for creating AppDetailViewModel instances with dependencies
 * 
 * @param appId The ID of the app to display
 * @param repository Repository for app restriction operations
 */
class AppDetailViewModelFactory(
    private val appId: Long,
    private val repository: AppRestrictionRepository
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppDetailViewModel::class.java)) {
            return AppDetailViewModel(appId, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
