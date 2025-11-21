package me.crossevol.mobilemonitor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import me.crossevol.mobilemonitor.repository.AppRestrictionRepository

/**
 * Factory for creating AddRuleViewModel instances with dependencies
 * 
 * @param appId The ID of the app to create rules for
 * @param repository Repository for app restriction operations
 */
class AddRuleViewModelFactory(
    private val appId: Long,
    private val repository: AppRestrictionRepository
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddRuleViewModel::class.java)) {
            return AddRuleViewModel(appId, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
