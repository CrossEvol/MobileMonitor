package me.crossevol.mobilemonitor.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import me.crossevol.mobilemonitor.repository.AppRestrictionRepository

/**
 * Factory for creating AppDetailViewModel instances
 * 
 * @param packageName The package name of the app to display
 * @param repository Repository for app restriction operations
 * @param context Application context for accessing SharedPreferences
 */
class AppDetailViewModelFactory(
    private val packageName: String,
    private val repository: AppRestrictionRepository,
    private val context: Context
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppDetailViewModel::class.java)) {
            return AppDetailViewModel(packageName, repository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
