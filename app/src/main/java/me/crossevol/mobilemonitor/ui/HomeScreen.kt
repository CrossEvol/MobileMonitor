package me.crossevol.mobilemonitor.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.crossevol.mobilemonitor.model.UsageStatsState
import me.crossevol.mobilemonitor.ui.theme.MobileMonitorTheme
import me.crossevol.mobilemonitor.viewmodel.HomeViewModel
import me.crossevol.mobilemonitor.viewmodel.UsageStatsViewModel

/**
 * Home screen displaying list of monitored apps with usage statistics
 * 
 * Integrates UsageStatsViewModel for loading app usage data and HomeViewModel
 * for managing monitored apps from the database.
 * 
 * @param usageStatsViewModel ViewModel for loading usage statistics
 * @param homeViewModel ViewModel for managing monitored apps
 * @param onNavigateToAppDetail Callback when user taps an app
 * @param onNavigateToSettings Callback when user taps settings icon
 * @param onOpenPermissionSettings Callback to open usage access settings
 * @param modifier Optional modifier for the screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    usageStatsViewModel: UsageStatsViewModel,
    homeViewModel: HomeViewModel,
    onNavigateToAppDetail: (String) -> Unit, // Changed to packageName
    onNavigateToSettings: () -> Unit,
    onOpenPermissionSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val usageStatsState by usageStatsViewModel.uiState.collectAsState()
    val homeUiState by homeViewModel.uiState.collectAsState()
    val selectedFilter by usageStatsViewModel.selectedFilter.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "App Usage Monitor",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Use existing state handling from UsageStatsViewModel
            when (usageStatsState) {
                is UsageStatsState.Loading -> {
                    LoadingScreen()
                }
                is UsageStatsState.PermissionRequired -> {
                    PermissionRequestScreen(
                        onOpenSettings = onOpenPermissionSettings,
                        onRetry = { usageStatsViewModel.checkPermissions() }
                    )
                }
                is UsageStatsState.Error -> {
                    ErrorScreen(
                        errorMessage = (usageStatsState as UsageStatsState.Error).message,
                        onRetry = { usageStatsViewModel.retry() }
                    )
                }
                is UsageStatsState.Success -> {
                    val apps = (usageStatsState as UsageStatsState.Success).apps
                    val monitoredPackages = homeUiState.apps.map { it.packageName }.toSet()
                    
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Time filter dropdown
                        TimeFilterDropdown(
                            selectedFilter = selectedFilter,
                            onFilterChanged = { newFilter ->
                                usageStatsViewModel.changeFilter(newFilter)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                        
                        if (apps.isEmpty()) {
                            EmptyAppsScreen()
                        } else {
                            AppUsageList(
                                apps = apps,
                                monitoredPackages = monitoredPackages,
                                onAppClick = onNavigateToAppDetail,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Empty state screen when no usage data is available
 */
@Composable
private fun EmptyAppsScreen(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No Usage Data Available",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Try using some apps and check back later. Tap any app to add monitoring rules.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EmptyAppsScreenPreview() {
    MobileMonitorTheme {
        EmptyAppsScreen()
    }
}

