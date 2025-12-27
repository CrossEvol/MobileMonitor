package me.crossevol.mobilemonitor.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import me.crossevol.mobilemonitor.ui.theme.Green40

/**
 * Individual cell in the time grid representing one hour of one day.
 * Displays different background colors based on rule coverage state.
 * 
 * @param isRuleCovered Whether this time slot is covered by any usage rule
 * @param modifier Optional modifier for the cell
 */
@Composable
fun TimeGridCell(
    isRuleCovered: Boolean,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isRuleCovered) {
        // Green color for covered time slots - using primary color for consistency
        Green40
    } else {
        // Gray color for uncovered time slots - using surface variant for accessibility
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }
    
    val contentDescriptionText = if (isRuleCovered) {
        "Time slot with usage rule applied"
    } else {
        "Time slot without usage rule"
    }
    
    Box(
        modifier = modifier
            .aspectRatio(1f) // Square cells for consistent grid appearance
            .clip(RoundedCornerShape(2.dp)) // Subtle rounding for modern look
            .background(backgroundColor)
            .semantics {
                contentDescription = contentDescriptionText
            }
    )
}