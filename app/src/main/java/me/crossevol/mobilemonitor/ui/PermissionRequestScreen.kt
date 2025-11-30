package me.crossevol.mobilemonitor.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.crossevol.mobilemonitor.ui.theme.MobileMonitorTheme

/**
 * Screen displayed when permissions are required
 * 
 * Shows an informative message about why permissions are needed
 * and provides buttons to grant permissions or retry permission check.
 */
@Composable
fun PermissionRequestScreen(
    onOpenSettings: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var hasOverlayPermission by remember { mutableStateOf(checkOverlayPermission(context)) }
    var hasAccessibilityPermission by remember { mutableStateOf(checkAccessibilityPermission(context)) }
    
    // Recheck permission when returning to the screen
    DisposableEffect(Unit) {
        onDispose {
            hasOverlayPermission = checkOverlayPermission(context)
            hasAccessibilityPermission = checkAccessibilityPermission(context)
        }
    }
    
    val scrollState = rememberScrollState()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Permission icon
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Permission required",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                // Title
                Text(
                    text = "Permissions Required",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                // Description
                Text(
                    text = "This app needs the following permissions",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Usage Stats Permission Section
                PermissionItem(
                    title = "Usage Access",
                    description = "Required to monitor app usage statistics",
                    isGranted = false,
                    onGrant = onOpenSettings
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Overlay Permission Section
                PermissionItem(
                    title = "Display over other apps",
                    description = "Required to block apps ",
                    isGranted = hasOverlayPermission,
                    onGrant = {
                        openOverlaySettings(context)
                        // Recheck after a delay when user returns
                        (context as? Activity)?.window?.decorView?.postDelayed({
                            hasOverlayPermission = checkOverlayPermission(context)
                        }, 1000)
                    }
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Accessibility Permission Section
                PermissionItem(
                    title = "Accessibility Service",
                    description = "Required to detect when restricted apps are launched",
                    isGranted = hasAccessibilityPermission,
                    onGrant = {
                        openAccessibilitySettings(context)
                        // Recheck after a delay when user returns
                        (context as? Activity)?.window?.decorView?.postDelayed({
                            hasAccessibilityPermission = checkAccessibilityPermission(context)
                        }, 1000)
                    }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Retry button
                OutlinedButton(
                    onClick = onRetry,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Check Permissions Again")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Additional help text
        Text(
            text = "Your data stays on your device and is not shared with anyone. " +
                    "These permissions are required for the app to monitor and control app usage.",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Individual permission item with grant button
 */
@Composable
private fun PermissionItem(
    title: String,
    description: String,
    isGranted: Boolean,
    onGrant: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isGranted) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isGranted) "Granted ✓" else description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isGranted) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
            }
            
            if (!isGranted) {
                Spacer(modifier = Modifier.width(12.dp))
                Button(onClick = onGrant) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Grant")
                }
            }
        }
    }
}

/**
 * Check if overlay permission is granted
 */
private fun checkOverlayPermission(context: android.content.Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        Settings.canDrawOverlays(context)
    } else {
        true // Not needed on older versions
    }
}

/**
 * Open overlay permission settings
 */
private fun openOverlaySettings(context: android.content.Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}

/**
 * Check if accessibility permission is granted
 */
private fun checkAccessibilityPermission(context: android.content.Context): Boolean {
    val accessibilityManager = context.getSystemService(android.content.Context.ACCESSIBILITY_SERVICE) as android.view.accessibility.AccessibilityManager
    val enabledServices = accessibilityManager.getEnabledAccessibilityServiceList(android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
    
    for (service in enabledServices) {
        if (service.resolveInfo.serviceInfo.packageName == context.packageName &&
            service.resolveInfo.serviceInfo.name.endsWith("AppMonitoringService")) {
            return true
        }
    }
    return false
}

/**
 * Open accessibility settings
 */
private fun openAccessibilitySettings(context: android.content.Context) {
    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    context.startActivity(intent)
}

@Preview(showBackground = true)
@Composable
fun PermissionRequestScreenPreview() {
    MobileMonitorTheme {
        PermissionRequestScreen(
            onOpenSettings = { },
            onRetry = { }
        )
    }
}