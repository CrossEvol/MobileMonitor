package me.crossevol.mobilemonitor.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import me.crossevol.mobilemonitor.model.AppUsageInfo
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
                    
                    if (apps.isEmpty()) {
                        EmptyAppsScreen()
                    } else {
                        AppUsageList(
                            apps = apps,
                            monitoredPackages = monitoredPackages,
                            onAppClick = onNavigateToAppDetail
                        )
                    }
                }
            }
        }
    }
}

/**
 * List of apps displayed in a LazyColumn
 * Uses AppUsageInfo from UsageStatsViewModel
 */
@Composable
private fun AppUsageList(
    apps: List<AppUsageInfo>,
    monitoredPackages: Set<String>,
    onAppClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        items(apps, key = { it.packageName }) { app ->
            AppUsageListItem(
                app = app,
                isMonitored = monitoredPackages.contains(app.packageName),
                onClick = { onAppClick(app.packageName) }
            )
        }
        
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

/**
 * Individual app list item card showing usage information
 */
@Composable
private fun AppUsageListItem(
    app: AppUsageInfo,
    isMonitored: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isMonitored) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App icon
            app.icon?.let { drawable ->
                val bitmap = drawable.toBitmap()
                androidx.compose.foundation.Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "App icon for ${app.appName}",
                    modifier = Modifier.size(48.dp)
                )
            } ?: run {
                // Placeholder if no icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = app.appName.take(1).uppercase(),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // App information
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = app.appName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    
                    // Show indicator if app is monitored
                    if (isMonitored) {
                        Text(
                            text = "●",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Usage statistics
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Total time
                    if (app.totalTimeInForeground > 0) {
                        Text(
                            text = formatDuration(app.totalTimeInForeground),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    // Last opened
                    if (app.lastTimeUsed > 0) {
                        Text(
                            text = "Last: ${formatLastUsed(app.lastTimeUsed)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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

/**
 * Format duration in milliseconds to human-readable string
 */
private fun formatDuration(millis: Long): String {
    val seconds = millis / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    
    return when {
        hours > 0 -> "${hours}h ${minutes % 60}m"
        minutes > 0 -> "${minutes}m"
        else -> "${seconds}s"
    }
}

/**
 * Format last used timestamp to human-readable string
 */
private fun formatLastUsed(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    
    return when {
        days > 0 -> "${days}d ago"
        hours > 0 -> "${hours}h ago"
        minutes > 0 -> "${minutes}m ago"
        else -> "Just now"
    }
}

@Preview(showBackground = true)
@Composable
fun EmptyAppsScreenPreview() {
    MobileMonitorTheme {
        EmptyAppsScreen()
    }
}
