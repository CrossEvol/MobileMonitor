package me.crossevol.mobilemonitor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import me.crossevol.mobilemonitor.repository.AppRestrictionRepository

/**
 * Factory for creating EditRuleViewModel instances with dependencies
 * 
 * @param ruleId The ID of the rule to edit
 * @param repository Repository for app restriction operations
 */
class EditRuleViewModelFactory(
    private val ruleId: Long,
    private val repository: AppRestrictionRepository
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditRuleViewModel::class.java)) {
            return EditRuleViewModel(ruleId, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
