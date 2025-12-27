# Design Document: Rule Time Visualization

## Overview

This feature adds a visual time grid representation to the existing app detail screen, allowing users to view their app usage rules as an intuitive 7x24 heatmap. The design integrates seamlessly with the current Android application built using Jetpack Compose, providing a toggle between the existing rule list view and the new visual grid view.

The implementation leverages Jetpack Compose's LazyVerticalGrid for efficient rendering and maintains consistency with the existing Material Design theme. The feature enhances user understanding of time-based restrictions through visual representation while preserving all existing functionality.

## Architecture

The feature follows the existing MVVM architecture pattern and integrates with the current app detail screen components:

```
AppDetailScreen (UI Layer)
    ├── AppDetailViewModel (ViewModel Layer)
    ├── RuleListView (Existing Component)
    └── TimeGridView (New Component)
        ├── TimeGridHeader (Day Labels)
        ├── TimeGridBody (7x24 Grid)
        └── TimeGridCell (Individual Hour Cell)

Data Flow:
UsageRules (Database) → Repository → ViewModel → UI State → TimeGridView
```

The design maintains separation of concerns by:
- **UI Layer**: Handles view state management and user interactions
- **ViewModel Layer**: Processes rule data and calculates grid coverage
- **Repository Layer**: Provides existing rule data access (no changes needed)

## Components and Interfaces

### TimeGridView Component

**Purpose**: Main composable that renders the 7x24 time grid visualization

**Interface**:
```kotlin
@Composable
fun TimeGridView(
    rules: List<AppRule>,
    modifier: Modifier = Modifier
)
```

**Responsibilities**:
- Render the complete time grid layout
- Calculate rule coverage for each time cell
- Handle responsive sizing for mobile screens
- Maintain visual consistency with app theme

### TimeGridHeader Component

**Purpose**: Displays day-of-week column headers

**Interface**:
```kotlin
@Composable
fun TimeGridHeader(
    modifier: Modifier = Modifier
)
```

**Responsibilities**:
- Display abbreviated day names (Mon, Tue, Wed, etc.)
- Maintain consistent spacing with grid columns
- Support localization for day names

### TimeGridCell Component

**Purpose**: Individual cell representing one hour of one day

**Interface**:
```kotlin
@Composable
fun TimeGridCell(
    isRuleCovered: Boolean,
    modifier: Modifier = Modifier
)
```

**Responsibilities**:
- Display appropriate background color (gray/green)
- Maintain consistent cell sizing
- Provide accessibility support

### ViewToggleButton Component

**Purpose**: Toggle control for switching between list and grid views

**Interface**:
```kotlin
@Composable
fun ViewToggleButton(
    isGridView: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
)
```

**Responsibilities**:
- Display appropriate icon for current view mode
- Handle toggle interactions
- Provide visual feedback for state changes

## Data Models

### ViewMode Enum

```kotlin
enum class ViewMode {
    LIST,
    GRID
}
```

**Purpose**: Represents the current view state of the app detail screen

### TimeGridState Data Class

```kotlin
data class TimeGridState(
    val coverage: Array<BooleanArray> // [day][hour] coverage matrix
)
```

**Purpose**: Holds the calculated rule coverage data for the time grid

**Structure**:
- 7 days (Monday=0 to Sunday=6)
- 24 hours (00:00=0 to 23:00=23)
- Boolean values indicating rule coverage

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

Now I need to analyze the acceptance criteria to determine which ones can be converted into testable properties.

Based on the prework analysis, I can identify the following testable properties:

### Property 1: Grid Structure Consistency
*For any* time grid display, the grid should have exactly 7 columns (days) and 24 rows (hours) with proper headers
**Validates: Requirements 1.1, 1.2, 1.3**

### Property 2: Rule Coverage Color Mapping
*For any* time cell and rule set, cells with rule coverage should display green background and cells without coverage should display gray background
**Validates: Requirements 1.4, 1.5**

### Property 3: Rule Coverage Calculation Accuracy
*For any* set of usage rules and time period, the coverage calculation should correctly identify all hours covered by any rule, including multi-hour ranges and overlapping rules
**Validates: Requirements 1.6, 4.1, 4.2, 4.3, 4.4**

### Property 4: View Toggle Mutual Exclusivity
*For any* view mode state, exactly one view (list or grid) should be visible while the other is hidden
**Validates: Requirements 2.4, 2.5**

### Property 5: Toggle State Consistency
*For any* toggle button state, the displayed icon should correctly indicate the current view mode and the action that will occur when tapped
**Validates: Requirements 2.6, 2.7**

### Property 6: Responsive Layout Behavior
*For any* screen size and orientation, the time grid should fit within available width with proportional cell sizing and consistent spacing
**Validates: Requirements 3.1, 3.2, 3.5, 3.6**

### Property 7: Data Reactivity
*For any* change to the underlying usage rules data, the time grid display should update immediately to reflect the new rule coverage
**Validates: Requirements 1.7, 4.6**

### Property 8: Edge Case Handling
*For any* time range that crosses midnight boundaries, the coverage calculation should correctly mark cells in both the ending and starting days
**Validates: Requirements 4.5**

### Property 9: Empty State Handling
*For any* app with no usage rules, all time cells should display gray background
**Validates: Requirements 4.7**

### Property 10: Accessibility Compliance
*For any* color combination used in the time grid, the contrast ratio should meet accessibility standards for visual distinction
**Validates: Requirements 5.1**

## Error Handling

The feature implements comprehensive error handling to ensure robust operation:

### Rule Data Processing Errors
- **Invalid Time Ranges**: When rules contain invalid time ranges (e.g., start > end), the system logs warnings and skips the invalid rule
- **Missing Rule Data**: When rule data is null or incomplete, the system treats it as no coverage for affected time periods
- **Database Access Errors**: When rule data cannot be loaded, the system displays an error state and falls back to showing all gray cells

### UI Rendering Errors
- **Layout Calculation Failures**: When screen dimensions cannot be determined, the system uses default minimum cell sizes
- **Color Resource Errors**: When theme colors are unavailable, the system falls back to hardcoded accessible color values
- **Orientation Change Errors**: When layout recalculation fails during orientation changes, the system maintains the previous layout until successful recalculation

### State Management Errors
- **View Toggle Failures**: When view mode switching fails, the system maintains the current view and logs the error
- **Data Update Failures**: When rule data updates fail to propagate to the UI, the system retries the update operation

## Testing Strategy

The testing approach combines unit tests for specific functionality with property-based tests for comprehensive coverage validation.

### Unit Testing Approach
Unit tests focus on specific examples, edge cases, and integration points:

- **Component Rendering**: Test that individual components render correctly with known inputs
- **Edge Cases**: Test midnight boundary crossings, empty rule sets, and invalid data
- **Integration Points**: Test interaction between view toggle and data updates
- **Error Conditions**: Test error handling for invalid rule data and UI failures

### Property-Based Testing Approach
Property tests validate universal properties across randomized inputs using **Kotest Property Testing** framework:

- **Minimum 100 iterations** per property test to ensure comprehensive coverage
- **Random rule generation** to test various combinations of time ranges, days, and patterns
- **Screen size variation** to test responsive layout behavior
- **Rule modification sequences** to test data reactivity

Each property test references its corresponding design document property using the tag format:
**Feature: rule-time-visualization, Property {number}: {property_text}**

### Test Configuration
- **Framework**: Kotest for property-based testing, JUnit for unit tests
- **UI Testing**: Compose UI testing framework for component interaction tests
- **Coverage Target**: 90% code coverage for new components
- **Performance**: Grid rendering should complete within 100ms for typical rule sets

The dual testing approach ensures both concrete correctness (unit tests) and universal property validation (property tests), providing comprehensive confidence in the feature's reliability across all possible usage scenarios.