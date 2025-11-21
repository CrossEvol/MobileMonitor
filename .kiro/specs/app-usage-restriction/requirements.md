# Requirements Document

## Introduction

This feature enables users to monitor and restrict their usage of specific applications based on customizable rules. The system will track app usage patterns, enforce time-based and count-based restrictions, and provide a comprehensive interface for managing these rules. The feature includes a background monitoring service that intercepts app launches when restrictions are violated, redirecting users to a blocking screen.

## Glossary

- **App Usage Monitor**: The Android application that provides usage monitoring and restriction capabilities
- **Target App**: Any application on the device that the user wants to monitor or restrict
- **Usage Rule**: A configuration that defines when and how a Target App should be restricted
- **Time Range**: A specific period within a day (e.g., 9:00-17:00) during which restrictions apply
- **Day Pattern**: The days of the week (Monday-Sunday) when a rule applies
- **Monitoring Service**: A background Android service that tracks app launches and enforces restrictions
- **Blocking Screen**: The default interface shown when a user attempts to access a restricted Target App
- **Rule Database**: Local storage containing app information and usage rules
- **Workday**: Monday through Friday
- **Weekend**: Saturday and Sunday

## Requirements

### Requirement 1

**User Story:** As a user, I want to see a blocking screen when I violate usage restrictions, so that I am prevented from accessing apps I've restricted.

#### Acceptance Criteria

1. WHEN the Monitoring Service detects a restricted Target App launch THEN the App Usage Monitor SHALL display the Blocking Screen with usage statistics
2. WHEN displaying the Blocking Screen THEN the App Usage Monitor SHALL show the time period, access count, and access duration for the current restriction period
3. WHEN displaying the Blocking Screen THEN the App Usage Monitor SHALL show the Target App name that triggered the block
4. WHEN the Blocking Screen is displayed THEN the App Usage Monitor SHALL prevent the user from accessing the Target App

### Requirement 2

**User Story:** As a user, I want the monitoring service to run in the background, so that my app usage is continuously tracked and restrictions are enforced.

#### Acceptance Criteria

1. WHEN the App Usage Monitor starts THEN the system SHALL launch the Monitoring Service as a background process
2. WHEN the Monitoring Service starts THEN the system SHALL load all Usage Rules from the Rule Database into memory
3. WHEN a Target App is launched THEN the Monitoring Service SHALL evaluate all applicable Usage Rules within 100 milliseconds
4. WHEN evaluating rules for a Target App THEN the Monitoring Service SHALL check if the AppInfo enabled field is true before enforcing restrictions
5. WHEN the AppInfo enabled field is false THEN the Monitoring Service SHALL skip all Usage Rules for that Target App
6. WHEN a Usage Rule is violated THEN the Monitoring Service SHALL intercept the Target App launch and redirect to the Blocking Screen
7. WHEN evaluating rules THEN the Monitoring Service SHALL check app package name, enabled status, Day Pattern, Time Range, total duration, and access count in sequence

### Requirement 3

**User Story:** As a user, I want to define usage restrictions with time periods, durations, and access counts, so that I can control when and how much I use specific apps.

#### Acceptance Criteria

1. WHEN creating a Usage Rule THEN the App Usage Monitor SHALL require specification of Day Pattern and Time Range
2. WHEN creating a Usage Rule THEN the App Usage Monitor SHALL set default total duration to zero minutes
3. WHEN creating a Usage Rule THEN the App Usage Monitor SHALL set default access count to zero times
4. WHEN creating a Usage Rule THEN the App Usage Monitor SHALL allow the user to modify total duration and access count values
5. WHERE a user selects workday pattern THEN the App Usage Monitor SHALL create five identical Usage Rules for Monday through Friday
6. WHERE a user selects weekend pattern THEN the App Usage Monitor SHALL create two identical Usage Rules for Saturday and Sunday
7. WHERE a user selects custom pattern THEN the App Usage Monitor SHALL create Usage Rules for each selected day
8. WHEN saving Usage Rules THEN the App Usage Monitor SHALL allow multiple Time Ranges for the same Day Pattern

### Requirement 4

**User Story:** As a user, I want to store app information and usage rules in a database, so that my configurations persist across app restarts.

#### Acceptance Criteria

1. WHEN the App Usage Monitor initializes THEN the system SHALL create a Rule Database with AppInfo and AppRule tables
2. WHEN storing app information THEN the system SHALL save app_id, app_name, package_name, enabled, and created_time in the AppInfo table
3. WHEN creating a new AppInfo entry THEN the system SHALL set enabled to true by default
4. WHEN storing usage rules THEN the system SHALL save rule_id, day, time_range, total_time, total_count, created_time, and app_info_id in the AppRule table
5. WHEN a Usage Rule references an app THEN the system SHALL maintain referential integrity between AppRule and AppInfo tables
6. WHEN the App Usage Monitor starts THEN the system SHALL load all persisted data from the Rule Database

### Requirement 5

**User Story:** As a user, I want to view a list of monitored apps with their usage statistics, so that I can see which apps I'm tracking and access their settings.

#### Acceptance Criteria

1. WHEN the home screen loads THEN the App Usage Monitor SHALL display a list of all Target Apps from the Rule Database
2. WHEN displaying each Target App THEN the App Usage Monitor SHALL show app icon, package name, app name, total usage duration, and last opened time
3. WHEN a user taps a Target App list item THEN the App Usage Monitor SHALL navigate to the app detail screen

### Requirement 6

**User Story:** As a user, I want to view and manage rules for a specific app, so that I can see all restrictions I've configured and modify them.

#### Acceptance Criteria

1. WHEN opening an app detail screen THEN the App Usage Monitor SHALL display app icon, package name, app name, usage duration, and last opened time
2. WHEN opening an app detail screen THEN the App Usage Monitor SHALL load existing Usage Rules from the Rule Database if the app exists
3. WHEN opening an app detail screen for a new app THEN the App Usage Monitor SHALL create a new AppInfo entry in the Rule Database
4. WHEN displaying the app detail screen THEN the App Usage Monitor SHALL show a list of Usage Rules below the app information
5. WHEN displaying each Usage Rule THEN the App Usage Monitor SHALL show day, time_range, total_time, and total_count
6. WHEN displaying the app detail screen THEN the App Usage Monitor SHALL provide a floating action button to add new Usage Rules
7. WHEN a user swipes a Usage Rule THEN the App Usage Monitor SHALL display a confirmation dialog before deletion
8. WHEN a user confirms rule deletion THEN the App Usage Monitor SHALL remove the Usage Rule from the Rule Database
9. WHEN a user taps a Usage Rule THEN the App Usage Monitor SHALL navigate to the rule edit screen

### Requirement 7

**User Story:** As a user, I want to add new usage rules with quick presets or custom configurations, so that I can easily set up restrictions.

#### Acceptance Criteria

1. WHEN the add rule screen opens THEN the App Usage Monitor SHALL display Day Pattern selection options: workday, weekend, and custom
2. WHEN the add rule screen opens THEN the App Usage Monitor SHALL display Time Range selection controls
3. WHEN the add rule screen opens THEN the App Usage Monitor SHALL set total_time to zero minutes by default
4. WHEN the add rule screen opens THEN the App Usage Monitor SHALL set total_count to zero times by default
5. WHEN the add rule screen opens THEN the App Usage Monitor SHALL allow the user to modify total_time and total_count values
6. WHEN a user saves a new rule THEN the App Usage Monitor SHALL persist it to the Rule Database with the associated app_info_id
7. WHEN a user saves a new rule THEN the App Usage Monitor SHALL notify the Monitoring Service to reload rules from the Rule Database

### Requirement 8

**User Story:** As a user, I want to edit existing usage rules, so that I can adjust restrictions as my needs change.

#### Acceptance Criteria

1. WHEN the edit rule screen opens THEN the App Usage Monitor SHALL load the selected Usage Rule data from the Rule Database
2. WHEN the edit rule screen displays THEN the App Usage Monitor SHALL show editable fields for time_range, total_time, and total_count
3. WHEN a user modifies rule values THEN the App Usage Monitor SHALL validate the input before allowing save
4. WHEN a user saves rule changes THEN the App Usage Monitor SHALL update the Usage Rule in the Rule Database
5. WHEN a user saves rule changes THEN the App Usage Monitor SHALL notify the Monitoring Service to reload rules from the Rule Database

### Requirement 9

**User Story:** As a user, I want navigation between screens to be managed consistently, so that I have a smooth experience moving through the app.

#### Acceptance Criteria

1. WHEN the App Usage Monitor initializes THEN the system SHALL configure a navigation library to manage screen transitions
2. WHEN navigating between screens THEN the App Usage Monitor SHALL use the navigation library for all route operations
3. WHEN navigating to a screen with parameters THEN the App Usage Monitor SHALL pass required data through the navigation system
4. WHEN a user presses back THEN the App Usage Monitor SHALL navigate to the previous screen in the navigation stack

### Requirement 10

**User Story:** As a user, I want to enable or disable monitoring for individual apps, so that I can temporarily suspend restrictions for specific apps without deleting their rules.

#### Acceptance Criteria

1. WHEN viewing an app detail screen THEN the App Usage Monitor SHALL display a toggle to enable or disable monitoring for that Target App
2. WHEN a user changes the app enabled toggle THEN the App Usage Monitor SHALL update the enabled field in the AppInfo table
3. WHEN the enabled field is updated THEN the App Usage Monitor SHALL notify the Monitoring Service to reload rules from the Rule Database
4. WHEN an app is disabled THEN the App Usage Monitor SHALL visually indicate the disabled state on the app detail screen
5. WHEN an app is disabled THEN the Monitoring Service SHALL not enforce any Usage Rules for that Target App

### Requirement 11

**User Story:** As a user, I want to enable or disable monitoring globally, so that I can temporarily suspend all restrictions without deleting my rules.

#### Acceptance Criteria

1. WHEN the home screen displays THEN the App Usage Monitor SHALL show a settings action icon in the top-right corner
2. WHEN a user taps the settings icon THEN the App Usage Monitor SHALL navigate to the settings screen
3. WHEN the settings screen displays THEN the App Usage Monitor SHALL show a toggle for enabling or disabling monitoring
4. WHEN a user changes the monitoring toggle THEN the App Usage Monitor SHALL display a confirmation dialog asking if the user wants to restart the application
5. WHEN a user confirms restart THEN the App Usage Monitor SHALL save the monitoring preference and restart the application
6. WHEN a user cancels restart THEN the App Usage Monitor SHALL revert the toggle to its previous state
7. WHEN monitoring is disabled THEN the Monitoring Service SHALL not enforce any Usage Rules
8. WHEN monitoring is enabled THEN the Monitoring Service SHALL enforce all active Usage Rules
