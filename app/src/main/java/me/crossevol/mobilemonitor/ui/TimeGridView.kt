package me.crossevol.mobilemonitor.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import me.crossevol.mobilemonitor.model.AppRule
import me.crossevol.mobilemonitor.utils.RuleCoverageCalculator

/**
 * Main time grid view component that displays a 7x24 heatmap of usage rules.
 * Shows which hours of each day have restrictions applied using color coding.
 * Automatically updates when the rules list changes.
 *
 * @param rules List of AppRule objects to visualize
 * @param modifier Optional modifier for the component
 */
@Composable
fun TimeGridView(
    rules: List<AppRule>,
    modifier: Modifier = Modifier
) {
    // Calculate rule coverage using the utility - recalculates when rules change
    val timeGridState = remember(rules) {
        RuleCoverageCalculator.calculateCoverage(rules)
    }

    Column(
        modifier = modifier.padding(8.dp)
    ) {
        // Header with day names
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Empty space for hour labels column
            Spacer(
                modifier = Modifier.width(48.dp)
            )

            // Day headers
            TimeGridHeader(
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Main grid with hour labels and time cells
        // Grid automatically updates when timeGridState changes due to rule modifications
        LazyVerticalGrid(
            columns = GridCells.Fixed(8), // 1 column for hour labels + 7 columns for days
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Generate all grid items (24 hours * 8 columns = 192 items)
            items(24 * 8) { index ->
                val hour = index / 8
                val column = index % 8

                if (column == 0) {
                    // First column: hour label
                    HourLabel(
                        hour = hour,
                        modifier = Modifier
                            .padding(1.dp)
                    )
                } else {
                    // Columns 1-7: time cells for each day
                    val dayIndex = column - 1
                    TimeGridCell(
                        isRuleCovered = timeGridState.hasRuleCoverage(
                            dayIndex,
                            hour
                        ),
                        modifier = Modifier.padding(1.dp)
                    )
                }
            }
        }
    }
}

/**
 * Hour label component for displaying time in 24-hour format
 *
 * @param hour Hour value (0-23)
 * @param modifier Optional modifier for the label
 */
@SuppressLint("DefaultLocale")
@Composable
private fun HourLabel(
    hour: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(48.dp)
            .height(36.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = String.format(
                "%02d:00",
                hour
            ),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
        )
    }
}