package me.crossevol.mobilemonitor.navigation

/**
 * Sealed class representing all navigation destinations in the app.
 * Each screen has a route string used for navigation.
 */
sealed class Screen(val route: String) {
    /**
     * Home screen showing list of monitored apps
     */
    object Home : Screen("home")
    
    /**
     * App detail screen showing app information and rules
     * Route includes appId parameter
     */
    object AppDetail : Screen("app_detail/{appId}") {
        fun createRoute(appId: Long) = "app_detail/$appId"
    }
    
    /**
     * Add rule screen for creating new usage rules
     * Route includes appId parameter
     */
    object AddRule : Screen("add_rule/{appId}") {
        fun createRoute(appId: Long) = "add_rule/$appId"
    }
    
    /**
     * Edit rule screen for modifying existing rules
     * Route includes ruleId parameter
     */
    object EditRule : Screen("edit_rule/{ruleId}") {
        fun createRoute(ruleId: Long) = "edit_rule/$ruleId"
    }
    
    /**
     * Settings screen for global app configuration
     */
    object Settings : Screen("settings")
    
    /**
     * Blocking screen shown when app usage is restricted
     */
    object Blocking : Screen("blocking")
}
