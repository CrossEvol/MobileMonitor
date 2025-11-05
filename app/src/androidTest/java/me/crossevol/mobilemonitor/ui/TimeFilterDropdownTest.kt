package me.crossevol.mobilemonitor.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import me.crossevol.mobilemonitor.model.TimeFilter
import me.crossevol.mobilemonitor.ui.theme.MobileMonitorTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for TimeFilterDropdown component
 * 
 * Tests the basic functionality of the time filter dropdown including:
 * - Display of selected filter
 * - Dropdown menu expansion
 * - Filter selection changes
 */
@RunWith(AndroidJUnit4::class)
class TimeFilterDropdownTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun timeFilterDropdown_displaysSelectedFilter() {
        // Given
        val selectedFilter = TimeFilter.WEEKLY
        
        // When
        composeTestRule.setContent {
            MobileMonitorTheme {
                TimeFilterDropdown(
                    selectedFilter = selectedFilter,
                    onFilterChanged = { }
                )
            }
        }
        
        // Then
        composeTestRule
            .onNodeWithText(selectedFilter.displayName)
            .assertIsDisplayed()
    }
    
    @Test
    fun timeFilterDropdown_showsAllOptionsWhenExpanded() {
        // Given
        var selectedFilter = TimeFilter.DAILY
        
        // When
        composeTestRule.setContent {
            MobileMonitorTheme {
                TimeFilterDropdown(
                    selectedFilter = selectedFilter,
                    onFilterChanged = { selectedFilter = it }
                )
            }
        }
        
        // Expand the dropdown
        composeTestRule
            .onNodeWithText(selectedFilter.displayName)
            .performClick()
        
        // Then - all filter options should be visible
        TimeFilter.values().forEach { filter ->
            composeTestRule
                .onNodeWithText(filter.displayName)
                .assertIsDisplayed()
        }
    }
}