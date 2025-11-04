# Requirements Document

## Introduction

This feature enhances the existing Android app usage statistics application by adding total usage time display for each application and improving the time filter functionality. The app currently displays a list of applications with their icons, full names, and last used timestamps, but lacks total usage time information and comprehensive filtering options.

## Glossary

- **UsageStatsManager**: Android system service that provides access to device usage history and statistics
- **UsageStats**: Data class containing usage information for a specific application during a time period
- **App_Usage_List**: The main UI component displaying the list of applications with their usage statistics
- **Time_Filter**: UI component allowing users to select different time periods for usage statistics
- **Usage_Time_Display**: UI component showing the total time an application has been used
- **Application_Entry**: Individual list item containing app icon, name, last used time, and total usage time

## Requirements

### Requirement 1

**User Story:** As a user, I want to see the total usage time for each application in the list, so that I can understand how much time I spend on each app.

#### Acceptance Criteria

1. WHEN the App_Usage_List is displayed, THE Application_Entry SHALL show total usage time alongside existing information
2. THE Usage_Time_Display SHALL format time in hours and minutes (e.g., "2h 45m")
3. WHEN usage time is less than one minute, THE Usage_Time_Display SHALL show seconds (e.g., "30s")
4. THE Usage_Time_Display SHALL calculate total time based on the selected time filter period
5. WHEN no usage data is available, THE Usage_Time_Display SHALL show "0m"

### Requirement 2

**User Story:** As a user, I want to filter app usage statistics by different time periods, so that I can analyze my usage patterns over various timeframes.

#### Acceptance Criteria

1. THE Time_Filter SHALL provide four options: Daily, Weekly, Monthly, and Yearly
2. WHEN a time filter option is selected, THE App_Usage_List SHALL update to show statistics for the selected period
3. WHEN Daily filter is selected, THE App_Usage_List SHALL show usage statistics for the current day
4. WHEN Weekly filter is selected, THE App_Usage_List SHALL show usage statistics for the current week
5. WHEN Monthly filter is selected, THE App_Usage_List SHALL show usage statistics for the current month
6. WHEN Yearly filter is selected, THE App_Usage_List SHALL show usage statistics for the current year

### Requirement 3

**User Story:** As a user, I want the app usage data to be retrieved efficiently from the system, so that the interface remains responsive.

#### Acceptance Criteria

1. THE App_Usage_List SHALL use UsageStatsManager to query usage statistics
2. WHEN querying usage data, THE App_Usage_List SHALL request data for the appropriate time range based on the selected filter
3. THE App_Usage_List SHALL aggregate usage statistics from multiple UsageStats entries for the same application
4. WHEN usage data is being loaded, THE App_Usage_List SHALL display loading indicators
5. THE App_Usage_List SHALL handle cases where usage access permission is not granted

### Requirement 4

**User Story:** As a user, I want the application list to be sorted by usage time, so that I can quickly identify my most-used applications.

#### Acceptance Criteria

1. THE App_Usage_List SHALL sort applications by total usage time in descending order
2. WHEN multiple applications have the same usage time, THE App_Usage_List SHALL sort by last used time as secondary criteria
3. WHEN an application has zero usage time, THE App_Usage_List SHALL place it at the bottom of the list
4. THE App_Usage_List SHALL maintain consistent sorting across different time filter selections
5. THE App_Usage_List SHALL update sorting when the time filter changes

### Requirement 5

**User Story:** As a user, I want the app to handle system permissions properly, so that I can grant usage access when needed.

#### Acceptance Criteria

1. WHEN the app lacks usage access permission, THE App_Usage_List SHALL display a permission request message
2. THE App_Usage_List SHALL provide a button to open system settings for granting usage access
3. WHEN usage access is granted, THE App_Usage_List SHALL automatically refresh the data
4. THE App_Usage_List SHALL handle permission checks gracefully without crashing
5. WHEN permission is denied, THE App_Usage_List SHALL show an appropriate error message