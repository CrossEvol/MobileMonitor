package me.crossevol.mobilemonitor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import me.crossevol.mobilemonitor.repository.AppRestrictionRepository

/**
 * Factory for creating HomeViewModel instances with dependencies
 * 
 * @param repository Repository for app restriction operations
 */
class HomeViewModelFactory(
    private val repository: AppRestrictionRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
