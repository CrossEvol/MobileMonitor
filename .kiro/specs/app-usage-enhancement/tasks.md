# Implementation Plan

- [x] 1. Create data models and enums
  - Define AppUsageInfo data class with package name, app name, icon, last used time, and total usage time
  - Create TimeFilter enum with Daily, Weekly, Monthly, Yearly options and their corresponding day values
  - Define UsageStatsState sealed class for Loading, Success, Error, and PermissionRequired states
  - _Requirements: 1.1, 1.2, 1.3, 2.1, 2.2_

- [x] 2. Implement UsageStatsRepository for data access




  - Create repository interface with methods for getting usage stats and checking permissions
  - Implement concrete repository class using UsageStatsManager and PackageManager
  - Add method to calculate time ranges based on TimeFilter selection
  - Implement data aggregation logic to sum usage times for same applications
  - Add app metadata retrieval using PackageManager for app names and icons
  - _Requirements: 2.3, 2.4, 3.1, 3.2, 3.3_

- [ ] 3. Create UsageStatsViewModel with state management
  - Implement ViewModel with StateFlow for UI state and selected filter
  - Add coroutine-based data loading with proper error handling
  - Implement permission checking and state updates
  - Add refresh functionality and filter change handling
  - Handle loading states and error scenarios
  - _Requirements: 2.2, 3.4, 5.1, 5.3_

- [ ] 4. Implement time formatting utility
  - Create utility function to format milliseconds into readable time strings
  - Handle seconds format for usage less than 1 minute
  - Handle hours and minutes format for longer usage times
  - Handle zero usage case display
  - _Requirements: 1.2, 1.3, 1.5_

- [ ] 5. Create RecyclerView adapter and ViewHolder
  - Implement AppUsageAdapter extending RecyclerView.Adapter
  - Create AppUsageViewHolder with proper view binding
  - Add data binding logic in ViewHolder to display app info, usage time, and last used time
  - Implement list update functionality with proper data handling
  - _Requirements: 1.1, 1.2, 1.3_

- [ ] 6. Implement sorting logic for app usage list
  - Add sorting by total usage time in descending order
  - Implement secondary sorting by last used time for apps with same usage
  - Handle zero usage apps placement at bottom of list
  - Ensure consistent sorting across different filter selections
  - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5_

- [ ] 7. Create time filter UI component
  - Implement dropdown or spinner for time filter selection
  - Add filter options: Daily, Weekly, Monthly, Yearly
  - Handle filter selection changes and update ViewModel
  - Ensure proper UI state updates when filter changes
  - _Requirements: 2.1, 2.2_

- [ ] 8. Implement permission handling UI
  - Create permission request UI when usage access is not granted
  - Add button to open system settings for granting usage access
  - Implement permission check on app resume/start
  - Handle permission denial with appropriate error messages
  - Add auto-refresh when returning from settings
  - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5_

- [ ] 9. Update MainActivity to integrate all components
  - Set up RecyclerView with adapter and layout manager
  - Integrate ViewModel with UI using lifecycle-aware observers
  - Handle different UI states (loading, success, error, permission required)
  - Implement filter selection handling
  - Add proper lifecycle management for coroutines
  - _Requirements: 2.4, 3.4, 3.5_

- [ ] 10. Add performance optimizations
  - Implement background processing using coroutines and Dispatchers.IO
  - Add basic caching for app metadata to reduce PackageManager calls
  - Implement loading indicators during data fetch operations
  - Add debouncing for filter changes to prevent excessive API calls
  - _Requirements: 3.1, 3.4_

- [ ] 11. Handle edge cases and error scenarios
  - Add proper exception handling for SecurityException and other system errors
  - Implement empty state UI when no usage data is available
  - Handle cases where apps are uninstalled but have usage data
  - Add retry functionality for failed data loads
  - Ensure graceful handling of system service unavailability
  - _Requirements: 3.5, 5.4, 5.5_

- [ ] 12. Final integration and UI polish
  - Ensure all components work together seamlessly
  - Verify data flow from repository through ViewModel to UI
  - Add proper loading states and smooth transitions
  - Implement proper error message display
  - Ensure consistent Material Design styling
  - _Requirements: 1.1, 2.4, 3.4, 4.4_