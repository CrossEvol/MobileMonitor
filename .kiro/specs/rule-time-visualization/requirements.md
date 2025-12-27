# Requirements Document

## Introduction

This feature enhances the existing app detail screen by adding a visual time grid representation of usage rules. Users can view their app restriction rules as an intuitive 7x24 heatmap showing which hours of each day have restrictions applied. The feature provides a toggle between the existing rule list view and the new visual grid view, making it easier to understand and manage time-based app restrictions.

## Glossary

- **App_Detail_Screen**: The existing screen showing app information and usage rules
- **Rule_List_View**: The current display showing usage rules in a vertical list format
- **Time_Grid_View**: The new visual representation showing rules as a 7x24 heatmap
- **Time_Cell**: Individual cell in the grid representing one hour of one day
- **Rule_Coverage**: Whether a specific hour is covered by any usage rule
- **View_Toggle**: UI control allowing users to switch between Rule_List_View and Time_Grid_View
- **Heatmap**: Visual representation using colors to indicate data density or coverage
- **Usage_Rule**: Existing data structure defining time-based app restrictions

## Requirements

### Requirement 1

**User Story:** As a user, I want to see my app usage rules visualized as a time grid, so that I can quickly understand which hours of the week have restrictions.

#### Acceptance Criteria

1. WHEN the Time_Grid_View is displayed, THE App_Detail_Screen SHALL show a 7x24 grid representing days of the week and hours of the day
2. THE Time_Grid_View SHALL display days of the week as column headers (Monday through Sunday)
3. THE Time_Grid_View SHALL display hours of the day as row headers (00:00 through 23:00)
4. WHEN a Time_Cell has no rule coverage, THE Time_Grid_View SHALL display it with gray background color
5. WHEN a Time_Cell has rule coverage, THE Time_Grid_View SHALL display it with green background color
6. THE Time_Grid_View SHALL calculate rule coverage by checking if any Usage_Rule applies to that specific day and hour
7. THE Time_Grid_View SHALL update automatically when Usage_Rules are added, modified, or deleted

### Requirement 2

**User Story:** As a user, I want to toggle between the rule list and time grid views, so that I can choose the most convenient way to view my restrictions.

#### Acceptance Criteria

1. WHEN the App_Detail_Screen loads, THE system SHALL display the Rule_List_View by default
2. THE App_Detail_Screen SHALL provide a View_Toggle control next to the delete icon
3. WHEN a user taps the View_Toggle control, THE App_Detail_Screen SHALL switch between Rule_List_View and Time_Grid_View
4. WHEN switching to Time_Grid_View, THE App_Detail_Screen SHALL hide the Rule_List_View and show the Time_Grid_View
5. WHEN switching to Rule_List_View, THE App_Detail_Screen SHALL hide the Time_Grid_View and show the Rule_List_View
6. THE View_Toggle control SHALL display an appropriate icon indicating the current view mode
7. THE View_Toggle control SHALL display an appropriate icon indicating what view will be shown when tapped

### Requirement 3

**User Story:** As a user, I want the time grid to be responsive and properly sized, so that I can view it clearly on my mobile device.

#### Acceptance Criteria

1. THE Time_Grid_View SHALL fit within the available screen width without horizontal scrolling
2. THE Time_Grid_View SHALL size Time_Cells proportionally to maintain readability
3. THE Time_Grid_View SHALL display day headers clearly above each column
4. THE Time_Grid_View SHALL display hour headers clearly to the left of each row
5. WHEN the device orientation changes, THE Time_Grid_View SHALL adjust its layout appropriately
6. THE Time_Grid_View SHALL maintain consistent spacing between Time_Cells
7. THE Time_Grid_View SHALL use appropriate text size for headers that remains readable

### Requirement 4

**User Story:** As a user, I want the time grid to accurately reflect my current usage rules, so that I can trust the visual representation.

#### Acceptance Criteria

1. WHEN calculating rule coverage, THE Time_Grid_View SHALL check all existing Usage_Rules for the current app
2. WHEN a Usage_Rule has a time range spanning multiple hours, THE Time_Grid_View SHALL mark all covered hours as green
3. WHEN multiple Usage_Rules overlap the same time period, THE Time_Grid_View SHALL still display the Time_Cell as green
4. WHEN a Usage_Rule applies to multiple days (e.g., workday pattern), THE Time_Grid_View SHALL mark the corresponding Time_Cells for all applicable days
5. THE Time_Grid_View SHALL handle edge cases where time ranges cross midnight boundaries
6. THE Time_Grid_View SHALL update immediately when the underlying Usage_Rules data changes
7. WHEN no Usage_Rules exist for an app, THE Time_Grid_View SHALL display all Time_Cells with gray background

### Requirement 5

**User Story:** As a user, I want the time grid interface to be intuitive and accessible, so that I can easily understand and use the visualization.

#### Acceptance Criteria

1. THE Time_Grid_View SHALL use sufficient color contrast between gray and green backgrounds for accessibility
2. THE Time_Grid_View SHALL provide clear visual separation between different days and hours
3. THE Time_Grid_View SHALL display day names in the user's preferred language format
4. THE Time_Grid_View SHALL display hour labels in 24-hour format for consistency
5. THE Time_Grid_View SHALL maintain visual consistency with the existing app design theme
6. THE Time_Grid_View SHALL provide smooth transitions when switching between views
7. WHEN the Time_Grid_View is displayed, THE App_Detail_Screen SHALL maintain access to existing functionality like adding and editing rules