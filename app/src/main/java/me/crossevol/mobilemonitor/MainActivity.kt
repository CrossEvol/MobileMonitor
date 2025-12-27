package me.crossevol.mobilemonitor

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import me.crossevol.mobilemonitor.repository.UsageStatsRepositoryImpl
import me.crossevol.mobilemonitor.ui.theme.MobileMonitorTheme
import me.crossevol.mobilemonitor.viewmodel.UsageStatsViewModel
import me.crossevol.mobilemonitor.viewmodel.UsageStatsViewModelFactory

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: UsageStatsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize ViewModel with repository
        val repository = UsageStatsRepositoryImpl(this)
        val factory = UsageStatsViewModelFactory(repository)
        viewModel = ViewModelProvider(
            this,
            factory
        )[UsageStatsViewModel::class.java]

        enableEdgeToEdge()
        setContent {
            MobileMonitorTheme {
                // Use the new navigation-based app structure
                me.crossevol.mobilemonitor.navigation.AppNavigationContainer()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Check permissions when app resumes (e.g., returning from settings)
        viewModel.checkPermissions()
    }

}