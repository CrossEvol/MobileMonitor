package me.crossevol.mobilemonitor.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import me.crossevol.mobilemonitor.model.AppInfo
import me.crossevol.mobilemonitor.model.AppRule
import me.crossevol.mobilemonitor.viewmodel.AppDetailViewModel
import java.time.format.DateTimeFormatter

/**
 * App detail screen displaying app information and usage rules
 * 
 * @param viewModel ViewModel managing the app detail state
 * @param onNavigateBack Callback when user presses back
 * @param onNavigateToAddRule Callback when user wants to add a new rule
 * @param onNavigateToEditRule Callback when user wants to edit a rule
 * @param modifier Optional modifier for the screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDetailScreen(
    viewModel: AppDetailViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToAddRule: (Long) -> Unit,
    onNavigateToEditRule: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.app?.appName ?: "App Details",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Navigate back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            // Only show FAB when app is loaded
            if (uiState.app != null && !uiState.isLoading) {
                FloatingActionButton(
                    onClick = { uiState.app?.let { onNavigateToAddRule(it.id) } },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add new rule"
                    )
                }
            }
        },
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    LoadingScreen()
                }
                uiState.error != null && uiState.app == null -> {
                    ErrorScreen(
                        errorMessage = uiState.error ?: "Unknown error",
                        onRetry = { viewModel.retry() }
                    )
                }
                uiState.app != null -> {
                    AppDetailContent(
                        app = uiState.app!!,
                        rules = uiState.rules,
                        showDeleteDialog = uiState.showDeleteDialog,
                        ruleToDelete = uiState.ruleToDelete,
                        onToggleEnabled = { enabled -> viewModel.toggleAppEnabled(enabled) },
                        onRuleClick = onNavigateToEditRule,
                        onRuleDelete = { rule -> viewModel.showDeleteDialog(rule) }
                    )
                }
            }
        }
    }

    // Delete confirmation dialog
    if (uiState.showDeleteDialog && uiState.ruleToDelete != null) {
        DeleteRuleDialog(
            rule = uiState.ruleToDelete!!,
            onConfirm = { viewModel.confirmDeleteRule() },
            onDismiss = { viewModel.hideDeleteDialog() }
        )
    }
}

/**
 * Main content displaying app information and rules list
 */
@Composable
private fun AppDetailContent(
    app: AppInfo,
    rules: List<AppRule>,
    showDeleteDialog: Boolean,
    ruleToDelete: AppRule?,
    onToggleEnabled: (Boolean) -> Unit,
    onRuleClick: (Long) -> Unit,
    onRuleDelete: (AppRule) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        // App information header
        item {
            AppInfoHeader(
                app = app,
                onToggleEnabled = onToggleEnabled
            )
        }
        
        // Rules section header
        item {
            Text(
                text = "Usage Rules",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        
        // Rules list
        if (rules.isEmpty()) {
            item {
                EmptyRulesMessage()
            }
        } else {
            items(rules, key = { it.id }) { rule ->
                SwipeableRuleListItem(
                    rule = rule,
                    showDeleteDialog = showDeleteDialog,
                    isRuleToDelete = ruleToDelete?.id == rule.id,
                    onClick = { onRuleClick(rule.id) },
                    onDelete = { onRuleDelete(rule) }
                )
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(80.dp)) // Space for FAB
        }
    }
}


/**
 * App information header with icon, name, and enable toggle
 */
@Composable
private fun AppInfoHeader(
    app: AppInfo,
    onToggleEnabled: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (app.enabled) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // App icon
                app.icon?.let { drawable ->
                    val bitmap = drawable.toBitmap()
                    androidx.compose.foundation.Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "App icon for ${app.appName}",
                        modifier = Modifier.size(64.dp)
                    )
                } ?: run {
                    // Placeholder if no icon
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                MaterialTheme.shapes.medium
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = app.appName.take(1).uppercase(),
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // App name and package
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = app.appName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = app.packageName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Usage statistics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                if (app.totalTimeInForeground > 0) {
                    UsageStatItem(
                        label = "Total Usage",
                        value = formatDuration(app.totalTimeInForeground)
                    )
                }
                
                if (app.lastTimeUsed > 0) {
                    UsageStatItem(
                        label = "Last Used",
                        value = formatLastUsed(app.lastTimeUsed)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Enable/Disable toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Monitoring Enabled",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (app.enabled) {
                            "Rules are being enforced"
                        } else {
                            "Rules are not being enforced"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Switch(
                    checked = app.enabled,
                    onCheckedChange = onToggleEnabled
                )
            }
        }
    }
}

/**
 * Usage statistic item
 */
@Composable
private fun UsageStatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

/**
 * Empty state message when no rules exist
 */
@Composable
private fun EmptyRulesMessage(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "No Rules Yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Tap the + button to add your first usage rule",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Swipeable rule list item with swipe-to-delete functionality
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableRuleListItem(
    rule: AppRule,
    showDeleteDialog: Boolean,
    isRuleToDelete: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                // Return false to prevent auto-dismiss, let the dialog handle it
                false
            } else {
                false
            }
        }
    )

    // Reset the dismiss state when the dialog is dismissed without confirming
    // This happens when showDeleteDialog becomes false for this specific rule
    LaunchedEffect(showDeleteDialog, isRuleToDelete) {
        if (!showDeleteDialog && dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
            dismissState.reset()
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromEndToStart = true,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            // Red background when swiping
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.error)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        modifier = modifier
    ) {
        RuleListItem(
            rule = rule,
            onClick = onClick
        )
    }
}

/**
 * Individual rule list item
 */
@Composable
private fun RuleListItem(
    rule: AppRule,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rule details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Day
                Text(
                    text = rule.day.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Time range
                val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
                Text(
                    text = "${rule.timeRangeStart.format(timeFormatter)} - ${rule.timeRangeEnd.format(timeFormatter)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Restrictions
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (rule.totalTime > 0) {
                        Text(
                            text = "Max: ${rule.totalTime} min",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    if (rule.totalCount > 0) {
                        Text(
                            text = "Count: ${rule.totalCount}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    if (rule.totalTime == 0 && rule.totalCount == 0) {
                        Text(
                            text = "No limits",
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
 * Delete confirmation dialog
 */
@Composable
private fun DeleteRuleDialog(
    rule: AppRule,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Delete Rule?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "Are you sure you want to delete this rule for ${rule.day.name.lowercase().replaceFirstChar { it.uppercase() }}? This action cannot be undone.",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = "Delete",
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
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
