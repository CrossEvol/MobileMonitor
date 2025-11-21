package me.crossevol.mobilemonitor.navigation

import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder

/**
 * Helper object providing navigation utilities and common navigation patterns.
 * Centralizes navigation logic to ensure consistent behavior across the app.
 */
object NavigationHelper {
    
    /**
     * Navigate to home screen and clear the entire back stack.
     * Used when returning to the main screen from blocking or other terminal states.
     */
    fun navigateToHomeAndClearBackStack(navController: NavController) {
        navController.navigate(Screen.Home.route) {
            popUpTo(Screen.Home.route) { inclusive = true }
            launchSingleTop = true
        }
    }
    
    /**
     * Navigate to app detail screen with the specified app ID.
     */
    fun navigateToAppDetail(navController: NavController, appId: Long) {
        navController.navigate(Screen.AppDetail.createRoute(appId))
    }
    
    /**
     * Navigate to add rule screen with the specified app ID.
     */
    fun navigateToAddRule(navController: NavController, appId: Long) {
        navController.navigate(Screen.AddRule.createRoute(appId))
    }
    
    /**
     * Navigate to edit rule screen with the specified rule ID.
     */
    fun navigateToEditRule(navController: NavController, ruleId: Long) {
        navController.navigate(Screen.EditRule.createRoute(ruleId))
    }
    
    /**
     * Navigate to settings screen.
     */
    fun navigateToSettings(navController: NavController) {
        navController.navigate(Screen.Settings.route)
    }
    
    /**
     * Navigate to blocking screen.
     * Typically called from the monitoring service.
     */
    fun navigateToBlocking(navController: NavController) {
        navController.navigate(Screen.Blocking.route)
    }
    
    /**
     * Navigate back to the previous screen in the back stack.
     * Returns true if navigation was successful, false if back stack is empty.
     */
    fun navigateBack(navController: NavController): Boolean {
        return navController.popBackStack()
    }
    
    /**
     * Navigate back and execute a callback after successful navigation.
     * Useful for triggering actions after returning to a previous screen.
     */
    fun navigateBackWithResult(
        navController: NavController,
        onNavigatedBack: () -> Unit
    ): Boolean {
        val result = navController.popBackStack()
        if (result) {
            onNavigatedBack()
        }
        return result
    }
    
    /**
     * Check if the back stack can be popped (i.e., there's a previous screen to return to).
     */
    fun canNavigateBack(navController: NavController): Boolean {
        return navController.previousBackStackEntry != null
    }
    
    /**
     * Navigate to a destination with single top launch mode.
     * Prevents multiple instances of the same screen in the back stack.
     */
    fun navigateSingleTop(navController: NavController, route: String) {
        navController.navigate(route) {
            launchSingleTop = true
        }
    }
    
    /**
     * Navigate and pop up to a specific destination.
     * Useful for clearing intermediate screens from the back stack.
     */
    fun navigateAndPopUpTo(
        navController: NavController,
        route: String,
        popUpToRoute: String,
        inclusive: Boolean = false
    ) {
        navController.navigate(route) {
            popUpTo(popUpToRoute) {
                this.inclusive = inclusive
            }
        }
    }
}
