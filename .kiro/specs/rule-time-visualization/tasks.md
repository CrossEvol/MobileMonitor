# Implementation Plan: Rule Time Visualization

## Overview

This implementation plan converts the rule time visualization design into discrete coding tasks. The approach builds incrementally from core data structures to UI components, ensuring each step validates functionality before proceeding. The implementation integrates with the existing AppDetailScreen and maintains consistency with the current MVVM architecture.

## Tasks

- [x] 1. Create core data structures and enums
  - Create ViewMode enum for tracking list/grid view state
  - Create TimeGridState data class for rule coverage matrix
  - Add view mode state to AppDetailViewModel
  - _Requirements: 2.1, 2.3_

- [ ]* 1.1 Write property test for ViewMode state management
  - **Property 4: View Toggle Mutual Exclusivity**
  - **Validates: Requirements 2.4, 2.5**

- [x] 2. Implement rule coverage calculation logic
  - Create RuleCoverageCalculator utility class
  - Implement calculateCoverage method that processes AppRule list into 7x24 boolean matrix
  - Handle time range expansion, day pattern expansion, and overlapping rules
  - _Requirements: 1.6, 4.1, 4.2, 4.3, 4.4_

- [ ]* 2.1 Write property test for rule coverage calculation
  - **Property 3: Rule Coverage Calculation Accuracy**
  - **Validates: Requirements 1.6, 4.1, 4.2, 4.3, 4.4**

- [ ]* 2.2 Write property test for edge case handling
  - **Property 8: Edge Case Handling**
  - **Validates: Requirements 4.5**

- [ ]* 2.3 Write property test for empty state handling
  - **Property 9: Empty State Handling**
  - **Validates: Requirements 4.7**

- [x] 3. Create TimeGridCell composable component
  - Implement TimeGridCell with isRuleCovered parameter
  - Apply gray/green background colors based on coverage state
  - Ensure accessibility compliance with sufficient color contrast
  - _Requirements: 1.4, 1.5, 5.1_

- [ ]* 3.1 Write property test for color mapping
  - **Property 2: Rule Coverage Color Mapping**
  - **Validates: Requirements 1.4, 1.5**

- [ ]* 3.2 Write property test for accessibility compliance
  - **Property 10: Accessibility Compliance**
  - **Validates: Requirements 5.1**

- [x] 4. Create TimeGridHeader composable component
  - Implement day-of-week column headers (Mon, Tue, Wed, etc.)
  - Support localization for day names
  - Ensure proper alignment with grid columns
  - _Requirements: 1.2, 3.3, 5.3_

- [x] 5. Create TimeGridView composable component
  - Implement main time grid layout using LazyVerticalGrid
  - Create 7x24 grid structure with hour row headers (00:00-23:00)
  - Integrate TimeGridHeader and TimeGridCell components
  - Implement responsive sizing that fits screen width without horizontal scrolling
  - _Requirements: 1.1, 1.3, 3.1, 3.2, 3.6, 5.4_

- [ ]* 5.1 Write property test for grid structure
  - **Property 1: Grid Structure Consistency**
  - **Validates: Requirements 1.1, 1.2, 1.3**

- [ ]* 5.2 Write property test for responsive layout
  - **Property 6: Responsive Layout Behavior**
  - **Validates: Requirements 3.1, 3.2, 3.5, 3.6**

- [ ] 6. Create ViewToggleButton composable component
  - Implement toggle button with appropriate icons for list/grid modes
  - Handle toggle interactions and state changes
  - Display icons that indicate current mode and next action
  - _Requirements: 2.2, 2.6, 2.7_

- [ ]* 6.1 Write property test for toggle state consistency
  - **Property 5: Toggle State Consistency**
  - **Validates: Requirements 2.6, 2.7**

- [x] 7. Integrate components into AppDetailScreen
  - Add ViewToggleButton next to existing delete icon
  - Implement view mode switching logic
  - Ensure mutual exclusivity between Rule_List_View and Time_Grid_View
  - Maintain access to existing functionality (add/edit rules) in both views
  - _Requirements: 2.2, 2.3, 2.4, 2.5, 5.7_

- [ ] 8. Implement data reactivity and updates
  - Connect TimeGridView to AppDetailViewModel rule data
  - Ensure grid updates automatically when rules are added, modified, or deleted
  - Implement proper state management for view mode persistence
  - _Requirements: 1.7, 4.6_

- [ ]* 8.1 Write property test for data reactivity
  - **Property 7: Data Reactivity**
  - **Validates: Requirements 1.7, 4.6**

- [ ] 9. Add error handling and edge cases
  - Implement error handling for invalid rule data
  - Handle layout calculation failures gracefully
  - Add fallback behaviors for missing theme colors or resources
  - _Requirements: Error handling from design document_

- [ ]* 9.1 Write unit tests for error conditions
  - Test invalid time ranges, missing data, and UI failures
  - Test fallback behaviors and error recovery

- [ ] 10. Checkpoint - Ensure all tests pass and integration works
  - Ensure all tests pass, ask the user if questions arise.
  - Verify smooth view transitions and proper state management
  - Test on different screen sizes and orientations

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Property tests validate universal correctness properties using Kotest framework
- Unit tests validate specific examples and edge cases
- Implementation builds incrementally with early validation at each step