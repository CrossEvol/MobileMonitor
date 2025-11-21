package me.crossevol.mobilemonitor

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import me.crossevol.mobilemonitor.model.TimeFilter
import me.crossevol.mobilemonitor.model.UsageStatsState
import me.crossevol.mobilemonitor.repository.UsageStatsRepositoryImpl
import me.crossevol.mobilemonitor.ui.AppUsageList
import me.crossevol.mobilemonitor.ui.ErrorScreen
import me.crossevol.mobilemonitor.ui.LoadingScreen
import me.crossevol.mobilemonitor.ui.PermissionRequestScreen
import me.crossevol.mobilemonitor.ui.TimeFilterDropdown
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
        viewModel = ViewModelProvider(this, factory)[UsageStatsViewModel::class.java]
        
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
    
    /**
     * Opens the system settings page for granting usage access permission
     */
    private fun openUsageAccessSettings() {
        try {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            startActivity(intent)
        } catch (e: Exception) {
            // Fallback to general settings if usage access settings not available
            val intent = Intent(Settings.ACTION_SETTINGS)
            startActivity(intent)
        }
    }
}

@Composable
fun UsageStatsScreen(
    viewModel: UsageStatsViewModel,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    
    when (uiState) {
        is UsageStatsState.Loading -> {
            LoadingScreen(modifier = modifier)
        }
        
        is UsageStatsState.PermissionRequired -> {
            PermissionRequestScreen(
                onOpenSettings = onOpenSettings,
                onRetry = { viewModel.checkPermissions() },
                modifier = modifier
            )
        }
        
        is UsageStatsState.Error -> {
            ErrorScreen(
                errorMessage = (uiState as UsageStatsState.Error).message,
                onRetry = { viewModel.retry() },
                modifier = modifier
            )
        }
        
        is UsageStatsState.Success -> {
            Column(
                modifier = modifier.fillMaxSize()
            ) {
                // Header section with title and filter
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Title
                    Text(
                        text = "App Usage Statistics",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Time filter dropdown
                    TimeFilterDropdown(
                        selectedFilter = selectedFilter,
                        onFilterChanged = { newFilter ->
                            viewModel.changeFilter(newFilter)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                // App usage list
                val apps = (uiState as UsageStatsState.Success).apps
                if (apps.isEmpty()) {
                    // Empty state
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "No usage data available",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Try using some apps and check back later",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    AppUsageList(
                        apps = apps,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UsageStatsScreenPreview() {
    MobileMonitorTheme {
        // Preview with mock data - just showing the time filter
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "App Usage Statistics",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            TimeFilterDropdown(
                selectedFilter = TimeFilter.DAILY,
                onFilterChanged = { },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}