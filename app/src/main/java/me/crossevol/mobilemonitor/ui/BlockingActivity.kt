package me.crossevol.mobilemonitor.ui

import android.content.Intent
import android.os.Bundle
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.crossevol.mobilemonitor.MainActivity
import me.crossevol.mobilemonitor.model.AppRule
import me.crossevol.mobilemonitor.model.DayOfWeek
import me.crossevol.mobilemonitor.model.RestrictionResult
import me.crossevol.mobilemonitor.ui.theme.MobileMonitorTheme
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Activity displayed when a user attempts to access a restricted app
 * Shows restriction details and prevents access to the blocked app
 */
class BlockingActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Handle back button press using OnBackPressedDispatcher
        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Prevent back button from returning to blocked app
                returnToHome()
            }
        })
        
        // Get restriction result from intent
        val restrictionResult = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("RESTRICTION_RESULT", RestrictionResult::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra<RestrictionResult>("RESTRICTION_RESULT")
        }
        
        enableEdgeToEdge()
        setContent {
            MobileMonitorTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    BlockingScreen(
                        restrictionResult = restrictionResult,
                        onClose = { returnToHome() },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
    
    /**
     * Returns user to home screen (launcher)
     */
    private fun returnToHome() {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
        finish()
    }
}

@Composable
fun BlockingScreen(
    restrictionResult: RestrictionResult?,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Block icon
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "App Blocked",
                modifier = Modifier
                    .padding(bottom = 24.dp)
                    .height(80.dp)
                    .fillMaxWidth(),
                tint = MaterialTheme.colorScheme.error
            )
            
            // Title
            Text(
                text = "App Access Restricted",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // App name
            if (restrictionResult != null) {
                Text(
                    text = restrictionResult.appName,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Restriction details card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Usage Details",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        // Time period
                        restrictionResult.violatedRule?.let { rule ->
                            RestrictionDetailRow(
                                label = "Time Period",
                                value = formatTimeRange(rule.timeRangeStart, rule.timeRangeEnd)
                            )
                        }
                        
                        // Access count
                        RestrictionDetailRow(
                            label = "Access Count",
                            value = "${restrictionResult.currentUsageCount} times"
                        )
                        
                        // Duration
                        RestrictionDetailRow(
                            label = "Usage Duration",
                            value = formatDuration(restrictionResult.currentUsageTime)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Message
                Text(
                    text = "You've reached your usage limit for this app during the current time period.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            } else {
                // Fallback if no restriction result provided
                Text(
                    text = "This app is currently restricted.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Close button
            Button(
                onClick = onClose,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Close",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun RestrictionDetailRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Formats time range for display
 */
private fun formatTimeRange(start: LocalTime, end: LocalTime): String {
    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    return "${start.format(formatter)} - ${end.format(formatter)}"
}

/**
 * Formats duration in minutes to human-readable format
 */
private fun formatDuration(minutes: Int): String {
    return when {
        minutes < 1 -> "Less than 1 minute"
        minutes < 60 -> "$minutes minute${if (minutes != 1) "s" else ""}"
        else -> {
            val hours = minutes / 60
            val remainingMinutes = minutes % 60
            if (remainingMinutes == 0) {
                "$hours hour${if (hours != 1) "s" else ""}"
            } else {
                "$hours hour${if (hours != 1) "s" else ""} $remainingMinutes minute${if (remainingMinutes != 1) "s" else ""}"
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BlockingScreenPreview() {
    MobileMonitorTheme {
        val sampleRule = AppRule(
            id = 1,
            day = DayOfWeek.MONDAY,
            timeRangeStart = LocalTime.of(9, 0),
            timeRangeEnd = LocalTime.of(17, 0),
            totalTime = 30,
            totalCount = 5,
            appInfoId = 1
        )
        
        val sampleResult = RestrictionResult(
            isRestricted = true,
            appName = "Instagram",
            violatedRule = sampleRule,
            currentUsageTime = 35,
            currentUsageCount = 6
        )
        
        BlockingScreen(
            restrictionResult = sampleResult,
            onClose = { }
        )
    }
}
