package me.crossevol.mobilemonitor.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import me.crossevol.mobilemonitor.R
import me.crossevol.mobilemonitor.model.AppUsageInfo
import me.crossevol.mobilemonitor.utils.TimeFormatter
import java.text.SimpleDateFormat
import java.util.*

/**
 * Compose component for displaying a list of app usage information.
 * Uses LazyColumn for efficient rendering of large lists.
 */
@Composable
fun AppUsageList(
    apps: List<AppUsageInfo>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(apps) { app ->
            AppUsageItem(appUsageInfo = app)
        }
    }
}

/**
 * Individual item component for displaying app usage information.
 */
@Composable
fun AppUsageItem(
    appUsageInfo: AppUsageInfo,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App icon
            if (appUsageInfo.icon != null) {
                val bitmap = appUsageInfo.icon.toBitmap(
                    width = 48.dp.value.toInt(),
                    height = 48.dp.value.toInt()
                )
                Image(
                    painter = BitmapPainter(bitmap.asImageBitmap()),
                    contentDescription = "App icon for ${appUsageInfo.appName}",
                    modifier = Modifier.size(48.dp)
                )
            } else {
                Image(
                    painter = painterResource(id = R.mipmap.ic_launcher),
                    contentDescription = "Default app icon",
                    modifier = Modifier.size(48.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // App info column
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = appUsageInfo.appName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = formatLastUsedTime(appUsageInfo.lastTimeUsed),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Usage time
            Text(
                text = TimeFormatter.formatUsageTime(appUsageInfo.totalTimeInForeground),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * Formats the last used timestamp into a human-readable string.
 */
@Composable
private fun formatLastUsedTime(lastTimeUsed: Long): String {
    val context = LocalContext.current
    
    if (lastTimeUsed <= 0) {
        return context.getString(R.string.never_used)
    }
    
    val currentTime = System.currentTimeMillis()
    val timeDifference = currentTime - lastTimeUsed
    
    return when {
        // Less than 1 minute ago
        timeDifference < 60_000 -> "Just now"
        
        // Less than 1 hour ago
        timeDifference < 3_600_000 -> {
            val minutes = timeDifference / 60_000
            "$minutes minute${if (minutes != 1L) "s" else ""} ago"
        }
        
        // Less than 24 hours ago
        timeDifference < 86_400_000 -> {
            val hours = timeDifference / 3_600_000
            "$hours hour${if (hours != 1L) "s" else ""} ago"
        }
        
        // Less than 7 days ago
        timeDifference < 604_800_000 -> {
            val days = timeDifference / 86_400_000
            "$days day${if (days != 1L) "s" else ""} ago"
        }
        
        // More than 7 days ago - show actual date
        else -> {
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            context.getString(R.string.last_used_format, dateFormat.format(Date(lastTimeUsed)))
        }
    }
}