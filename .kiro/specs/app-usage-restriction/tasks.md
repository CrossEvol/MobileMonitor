# Implementation Plan

- [ ] 1. Set up database layer with Room
  - Create AppInfoEntity and AppRuleEntity data classes with proper annotations
  - Implement AppInfoDao and AppRuleDao interfaces with all required queries
  - Create AppRestrictionDatabase class with Room configuration
  - Set up database migration strategy
  - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5_

- [ ] 1.1 Write property test for database persistence round-trip
  - **Property 10: Database persistence round-trip**
  - **Validates: Requirements 4.2, 4.4, 4.6**

- [ ] 1.2 Write property test for referential integrity
  - **Property 11: Referential integrity is maintained**
  - **Validates: Requirements 4.5**

- [ ] 2. Implement domain models and repository layer
  - Create AppInfo, AppRule, DayOfWeek, DayPattern, and RestrictionResult domain models
  - Implement AppRestrictionRepository interface
  - Create AppRestrictionRepositoryImpl with entity-to-domain mapping
  - Implement all repository methods for CRUD operations
  - Add checkRestriction method with rule evaluation logic
  - _Requirements: 2.7, 3.1, 4.2, 4.4_

- [ ] 2.1 Write property test for required field validation
  - **Property 5: Required fields are validated**
  - **Validates: Requirements 3.1**

- [ ] 2.2 Write property test for rule evaluation
  - **Property 4: Rule evaluation checks all factors**
  - **Validates: Requirements 2.7**

- [ ] 3. Create rule pattern expansion logic
  - Implement workday pattern expansion (Monday-Friday)
  - Implement weekend pattern expansion (Saturday-Sunday)
  - Implement custom pattern expansion for selected days
  - Add validation to allow multiple rules per day
  - _Requirements: 3.5, 3.6, 3.7, 3.8_

- [ ] 3.1 Write property test for workday pattern
  - **Property 6: Workday pattern creates five rules**
  - **Validates: Requirements 3.5**

- [ ] 3.2 Write property test for weekend pattern
  - **Property 7: Weekend pattern creates two rules**
  - **Validates: Requirements 3.6**

- [ ] 3.3 Write property test for custom pattern
  - **Property 8: Custom pattern creates rules for selected days**
  - **Validates: Requirements 3.7**

- [ ] 3.4 Write property test for multiple rules per day
  - **Property 9: Multiple rules per day are allowed**
  - **Validates: Requirements 3.8**

- [ ] 4. Implement monitoring service
  - Create AppMonitoringService extending AccessibilityService
  - Implement onServiceConnected to initialize service
  - Implement onAccessibilityEvent to detect app launches
  - Create rules cache with ConcurrentHashMap
  - Implement loadRulesIntoCache method
  - Implement checkAndEnforceRestrictions method
  - Add reloadRules method for cache updates
  - Create service configuration XML
  - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 2.7_



- [ ] 5. Create usage tracking system
  - Implement UsageTracker class to monitor app usage time and count
  - Integrate with UsageStatsManager for historical data
  - Add time range matching logic (handle midnight crossing)
  - Implement current usage calculation for restriction checking
  - _Requirements: 1.2, 2.7_

- [ ] 6. Implement blocking screen
  - Create BlockingActivity with Compose UI
  - Display app name, time period, access count, and duration
  - Add close button to return to home
  - Style according to Material Design 3
  - _Requirements: 1.1, 1.2, 1.3, 1.4_



- [ ] 7. Set up navigation with Jetpack Navigation Compose
  - Add Navigation Compose dependency
  - Create Screen sealed class with all routes
  - Implement NavHost with all screen destinations
  - Configure navigation arguments for app ID and rule ID
  - Add back stack handling
  - _Requirements: 9.1, 9.2, 9.3, 9.4_



- [ ] 8. Implement home screen
  - Create HomeScreen composable
  - Create HomeViewModel with StateFlow for UI state
  - Load all apps from repository
  - Display app list with LazyColumn
  - Show app icon, package name, app name, usage stats
  - Add settings icon in top app bar
  - Handle navigation to app detail screen
  - Handle navigation to settings screen
  - _Requirements: 5.1, 5.2, 5.3, 11.1, 11.2_



- [ ] 9. Implement app detail screen
  - Create AppDetailScreen composable
  - Create AppDetailViewModel with StateFlow for UI state
  - Display app information header
  - Add enable/disable toggle for the app
  - Load and display rules list with LazyColumn
  - Implement SwipeToDismiss for rule deletion
  - Add confirmation dialog for deletion
  - Add FAB for adding new rules
  - Handle navigation to add rule screen
  - Handle navigation to edit rule screen
  - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5, 6.6, 6.7, 6.8, 6.9, 10.1, 10.2, 10.3, 10.4_



- [ ] 10. Implement add rule screen
  - Create AddRuleScreen composable
  - Create AddRuleViewModel with StateFlow for form state
  - Add day pattern selector (Workday/Weekend/Custom)
  - Add custom day picker with checkboxes
  - Add time range pickers for start and end time
  - Add input fields for total time and total count
  - Set default values (0 for time and count)
  - Implement form validation
  - Handle save action with pattern expansion
  - Notify monitoring service after save
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7, 7.1, 7.2, 7.3, 7.4, 7.5, 7.6, 7.7_



- [ ] 11. Implement edit rule screen
  - Create EditRuleScreen composable
  - Create EditRuleViewModel with StateFlow for form state
  - Load existing rule data from database
  - Display editable fields for time range, total time, total count
  - Implement input validation (reject invalid values)
  - Handle save action with database update
  - Notify monitoring service after save
  - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5_



- [ ] 12. Implement settings screen
  - Create SettingsScreen composable
  - Create SettingsViewModel with StateFlow for settings state
  - Add global monitoring toggle
  - Implement SharedPreferences for monitoring preference
  - Add confirmation dialog for restart
  - Handle restart confirmation (save preference and restart app)
  - Handle restart cancellation (revert toggle)
  - _Requirements: 11.3, 11.4, 11.5, 11.6_



- [ ] 13. Implement global monitoring enforcement
  - Add monitoring enabled check in service
  - Skip all rule enforcement when monitoring is disabled
  - Enforce all active rules when monitoring is enabled
  - _Requirements: 11.7, 11.8_



- [ ] 14. Add dependency injection with Hilt
  - Add Hilt dependencies to build.gradle
  - Create Application class with @HiltAndroidApp
  - Create database module providing Room database
  - Create repository module providing repository instances
  - Annotate ViewModels with @HiltViewModel
  - Update MainActivity to use Hilt

- [ ] 15. Implement error handling
  - Add try-catch blocks for database operations
  - Implement retry logic for service crashes with WorkManager
  - Add permission check UI for accessibility service
  - Handle navigation failures gracefully
  - Add inline validation error messages in forms
  - Implement loading states in all ViewModels

- [ ] 16. Add permissions and manifest configuration
  - Add PACKAGE_USAGE_STATS permission to manifest
  - Add BIND_ACCESSIBILITY_SERVICE permission to manifest
  - Add FOREGROUND_SERVICE permission to manifest
  - Register AppMonitoringService in manifest
  - Register BlockingActivity in manifest
  - Create accessibility service configuration XML

- [ ] 17. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.
