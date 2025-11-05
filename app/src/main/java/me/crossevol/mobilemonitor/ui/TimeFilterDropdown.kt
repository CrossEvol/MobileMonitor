package me.crossevol.mobilemonitor.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.crossevol.mobilemonitor.model.TimeFilter

/**
 * Time filter dropdown component for selecting usage statistics time periods
 * 
 * Provides a dropdown menu with Daily, Weekly, Monthly, and Yearly options.
 * Handles filter selection changes and updates the provided callback.
 * 
 * @param selectedFilter Currently selected time filter
 * @param onFilterChanged Callback invoked when filter selection changes
 * @param modifier Optional modifier for styling the component
 */
@Composable
fun TimeFilterDropdown(
    selectedFilter: TimeFilter,
    onFilterChanged: (TimeFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box(modifier = modifier) {
        // Dropdown trigger button
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = selectedFilter.displayName,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Expand time filter options"
            )
        }
        
        // Dropdown menu with filter options
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            TimeFilter.values().forEach { filter ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = filter.displayName,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    onClick = {
                        onFilterChanged(filter)
                        expanded = false
                    }
                )
            }
        }
    }
}