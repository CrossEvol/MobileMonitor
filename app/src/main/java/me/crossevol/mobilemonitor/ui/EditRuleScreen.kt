package me.crossevol.mobilemonitor.ui

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import me.crossevol.mobilemonitor.viewmodel.EditRuleViewModel
import java.time.LocalTime

/**
 * Edit rule screen for modifying existing usage rules
 * 
 * @param viewModel ViewModel managing the form state
 * @param onNavigateBack Callback when user presses back
 * @param onRuleSaved Callback when rule is successfully saved
 * @param modifier Optional modifier for the screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRuleScreen(
    viewModel: EditRuleViewModel,
    onNavigateBack: () -> Unit,
    onRuleSaved: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formState by viewModel.formState.collectAsState()
    
    // Navigate back when save is successful
    LaunchedEffect(formState.saveSuccess) {
        if (formState.saveSuccess) {
            onRuleSaved()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Edit Usage Rule",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                formState.isLoading -> {
                    // Show loading indicator while loading rule
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                formState.rule == null && formState.errorMessage != null -> {
                    // Show error if rule couldn't be loaded
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = formState.errorMessage ?: "Failed to load rule",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                            Button(onClick = onNavigateBack) {
                                Text("Go Back")
                            }
                        }
                    }
                }
                formState.isSaving -> {
                    // Show loading indicator while saving
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                else -> {
                    // Show form
                    EditRuleFormContent(
                        formState = formState,
                        onTimeRangeStartChange = viewModel::updateTimeRangeStart,
                        onTimeRangeEndChange = viewModel::updateTimeRangeEnd,
                        onTotalTimeChange = viewModel::updateTotalTime,
                        onTotalCountChange = viewModel::updateTotalCount,
                        onSave = viewModel::saveRule
                    )
                }
            }
        }
    }
}

/**
 * Main form content for editing a rule
 */
@Composable
private fun EditRuleFormContent(
    formState: me.crossevol.mobilemonitor.viewmodel.EditRuleFormState,
    onTimeRangeStartChange: (LocalTime) -> Unit,
    onTimeRangeEndChange: (LocalTime) -> Unit,
    onTotalTimeChange: (Int) -> Unit,
    onTotalCountChange: (Int) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Display day information (read-only)
        formState.rule?.let { rule ->
            DayInfoCard(dayName = rule.day.name.lowercase().replaceFirstChar { it.uppercase() })
        }
        
        // Time Range Pickers
        EditTimeRangePickers(
            startTime = formState.timeRangeStart,
            endTime = formState.timeRangeEnd,
            onStartTimeChange = onTimeRangeStartChange,
            onEndTimeChange = onTimeRangeEndChange
        )
        
        // Total Time Input
        EditTotalTimeInput(
            totalTime = formState.totalTime,
            onTotalTimeChange = onTotalTimeChange
        )
        
        // Total Count Input
        EditTotalCountInput(
            totalCount = formState.totalCount,
            onTotalCountChange = onTotalCountChange
        )
        
        // Error message
        if (formState.errorMessage != null) {
            Text(
                text = formState.errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Save Button
        Button(
            onClick = onSave,
            enabled = formState.isValid && !formState.isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = "Save Changes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Display day information (read-only)
 */
@Composable
private fun DayInfoCard(
    dayName: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Day",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = dayName,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "Day cannot be changed when editing",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Time range pickers for start and end time
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditTimeRangePickers(
    startTime: LocalTime,
    endTime: LocalTime,
    onStartTimeChange: (LocalTime) -> Unit,
    onEndTimeChange: (LocalTime) -> Unit,
    modifier: Modifier = Modifier
) {
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Time Range",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Start Time
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Start Time",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                MaterialTheme.shapes.medium
                            )
                            .clickable { showStartTimePicker = true }
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = String.format("%02d:%02d", startTime.hour, startTime.minute),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                // End Time
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "End Time",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                MaterialTheme.shapes.medium
                            )
                            .clickable { showEndTimePicker = true }
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = String.format("%02d:%02d", endTime.hour, endTime.minute),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
    
    // Start Time Picker Dialog
    if (showStartTimePicker) {
        EditTimePickerDialog(
            title = "Select Start Time",
            initialTime = startTime,
            onDismiss = { showStartTimePicker = false },
            onConfirm = { time ->
                onStartTimeChange(time)
                showStartTimePicker = false
            }
        )
    }
    
    // End Time Picker Dialog
    if (showEndTimePicker) {
        EditTimePickerDialog(
            title = "Select End Time",
            initialTime = endTime,
            onDismiss = { showEndTimePicker = false },
            onConfirm = { time ->
                onEndTimeChange(time)
                showEndTimePicker = false
            }
        )
    }
}

/**
 * Time picker dialog
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditTimePickerDialog(
    title: String,
    initialTime: LocalTime,
    onDismiss: () -> Unit,
    onConfirm: (LocalTime) -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
        is24Hour = true
    )
    
    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TimePicker(state = timePickerState)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    TextButton(
                        onClick = {
                            val time = LocalTime.of(
                                timePickerState.hour,
                                timePickerState.minute
                            )
                            onConfirm(time)
                        }
                    ) {
                        Text("OK")
                    }
                }
            }
        }
    }
}

/**
 * Total time input field
 */
@Composable
private fun EditTotalTimeInput(
    totalTime: Int,
    onTotalTimeChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Maximum Usage Time",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = totalTime.toString(),
                onValueChange = { value ->
                    value.toIntOrNull()?.let { onTotalTimeChange(it) }
                },
                label = { Text("Minutes") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "Set to 0 for no time limit",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Total count input field
 */
@Composable
private fun EditTotalCountInput(
    totalCount: Int,
    onTotalCountChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Maximum Access Count",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = totalCount.toString(),
                onValueChange = { value ->
                    value.toIntOrNull()?.let { onTotalCountChange(it) }
                },
                label = { Text("Times") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "Set to 0 for no access limit",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
