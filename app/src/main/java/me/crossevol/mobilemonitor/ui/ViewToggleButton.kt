package me.crossevol.mobilemonitor.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.crossevol.mobilemonitor.model.ViewMode

/**
 * Toggle button for switching between list and grid views
 * 
 * Displays an icon indicating the current view mode and what view will be shown when tapped.
 * When in LIST mode, shows grid icon (indicating tapping will switch to grid).
 * When in GRID mode, shows list icon (indicating tapping will switch to list).
 * 
 * TODO: Replace placeholder icons with more appropriate grid/list icons when available
 * 
 * @param currentViewMode The current view mode (LIST or GRID)
 * @param onToggle Callback invoked when the toggle button is tapped
 * @param modifier Optional modifier for the button
 */
@Composable
fun ViewToggleButton(
    currentViewMode: ViewMode,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onToggle,
        modifier = modifier
    ) {
        when (currentViewMode) {
            ViewMode.LIST -> {
                // Currently showing list, so show grid icon to indicate switching to grid
                // TODO: Replace with proper grid icon (e.g., Apps, GridView, or TableChart)
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Switch to grid view",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            ViewMode.GRID -> {
                // Currently showing grid, so show list icon to indicate switching to list
                // TODO: Replace with proper list icon (e.g., List, FormatListBulleted)
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Switch to list view",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}