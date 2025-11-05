package me.crossevol.mobilemonitor.ui

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
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
        var selectedFilter = TimeFilter.WEEKLY // Use WEEKLY to avoid confusion with DAILY appearing twice
        
        // When
        composeTestRule.setContent {
            MobileMonitorTheme {
                TimeFilterDropdown(
                    selectedFilter = selectedFilter,
                    onFilterChanged = { selectedFilter = it }
                )
            }
        }
        
        // Expand the dropdown by clicking the button
        composeTestRule
            .onNodeWithText(selectedFilter.displayName, useUnmergedTree = true)
            .performClick()

        // Then - all filter options should be visible in the dropdown menu
        // Check that we have the expected number of nodes for each filter
        TimeFilter.entries.forEach { filter ->
            val nodes = composeTestRule.onAllNodesWithText(filter.displayName)
            
            if (filter == selectedFilter) {
                // Selected filter appears twice: in button and in menu
                nodes.assertCountEquals(2)
            } else {
                // Other filters appear once: only in menu
                nodes.assertCountEquals(1)
            }
            
            // Verify at least one instance is displayed
            nodes[0].assertIsDisplayed()
        }
    }
    
    @Test
    fun timeFilterDropdown_selectsNewFilter() {
        // Given
        var selectedFilter = TimeFilter.DAILY
        var callbackInvoked = false
        
        // When
        composeTestRule.setContent {
            MobileMonitorTheme {
                TimeFilterDropdown(
                    selectedFilter = selectedFilter,
                    onFilterChanged = { newFilter ->
                        selectedFilter = newFilter
                        callbackInvoked = true
                    }
                )
            }
        }
        
        // Expand the dropdown
        composeTestRule
            .onNodeWithText(selectedFilter.displayName, useUnmergedTree = true)
            .performClick()
        
        // Select a different filter (MONTHLY)
        val targetFilter = TimeFilter.MONTHLY
        composeTestRule
            .onAllNodesWithText(targetFilter.displayName)[0]
            .performClick()
        
        // Then - callback should be invoked and filter should change
        assert(callbackInvoked) { "Filter change callback was not invoked" }
        assert(selectedFilter == targetFilter) { "Selected filter was not updated" }
    }
}