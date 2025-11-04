package me.crossevol.mobilemonitor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import me.crossevol.mobilemonitor.repository.UsageStatsRepository

/**
 * Factory class for creating UsageStatsViewModel instances with dependency injection
 * 
 * This factory is required because the ViewModel has constructor parameters that need
 * to be provided when the ViewModel is created by the ViewModelProvider.
 */
class UsageStatsViewModelFactory(
    private val repository: UsageStatsRepository
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UsageStatsViewModel::class.java)) {
            return UsageStatsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}