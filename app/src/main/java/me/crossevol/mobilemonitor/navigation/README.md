# Navigation Setup

This package contains the navigation infrastructure for the Mobile Monitor app using Jetpack Navigation Compose.

## Components

### Screen.kt
Sealed class defining all navigation routes in the app:
- `Home` - Main screen with app list
- `AppDetail` - App details and rules (requires appId parameter)
- `AddRule` - Create new usage rule (requires appId parameter)
- `EditRule` - Edit existing rule (requires ruleId parameter)
- `Settings` - Global app settings
- `Blocking` - Restriction violation screen

Each screen with parameters provides a `createRoute()` helper method for type-safe navigation.

### AppNavHost.kt
Main navigation host that defines the navigation graph. Contains:
- All composable destinations
- Navigation argument configurations
- TODO comments for screen implementations

### NavigationHelper.kt
Utility object providing common navigation operations:
- `navigateToAppDetail()` - Navigate to app detail with ID
- `navigateToAddRule()` - Navigate to add rule with app ID
- `navigateToEditRule()` - Navigate to edit rule with rule ID
- `navigateToSettings()` - Navigate to settings
- `navigateToBlocking()` - Navigate to blocking screen
- `navigateBack()` - Pop back stack
- `navigateToHomeAndClearBackStack()` - Return to home and clear history

### NavigationIntegration.kt
Contains `AppNavigationContainer` composable and integration examples showing how to:
- Set up navigation in MainActivity
- Pass navigation callbacks to screens
- Extract route parameters
- Handle back navigation

## Usage

### In MainActivity
```kotlin
setContent {
    MobileMonitorTheme {
        AppNavigationContainer()
    }
}
```

### In Screen Implementations
When implementing screens (tasks 8-12), use the navigation callbacks:

```kotlin
// In AppNavHost.kt, replace TODO with actual screen
composable(route = Screen.Home.route) {
    HomeScreen(
        onNavigateToAppDetail = { appId ->
            NavigationHelper.navigateToAppDetail(navController, appId)
        },
        onNavigateToSettings = {
            NavigationHelper.navigateToSettings(navController)
        }
    )
}
```

### Extracting Parameters
```kotlin
composable(
    route = Screen.AppDetail.route,
    arguments = listOf(navArgument("appId") { type = NavType.LongType })
) { backStackEntry ->
    val appId = backStackEntry.arguments?.getLong("appId") ?: 0L
    AppDetailScreen(appId = appId, ...)
}
```

## Requirements Satisfied

This implementation satisfies requirements:
- **9.1**: Navigation library configured (Navigation Compose)
- **9.2**: All screen transitions use navigation library
- **9.3**: Parameters passed through navigation system (appId, ruleId)
- **9.4**: Back stack handling with popBackStack()

## Next Steps

When implementing screens (tasks 8-12):
1. Create the screen composable
2. Update the corresponding TODO in AppNavHost.kt
3. Pass navigation callbacks as parameters
4. Use NavigationHelper for navigation operations
