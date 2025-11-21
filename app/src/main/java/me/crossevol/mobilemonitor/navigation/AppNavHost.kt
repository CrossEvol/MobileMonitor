package me.crossevol.mobilemonitor.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

/**
 * Main navigation host for the app.
 * Defines all navigation routes and their corresponding composable screens.
 * 
 * @param navController The navigation controller managing the navigation stack
 * @param modifier Optional modifier for the NavHost
 * @param startDestination The initial screen to display (defaults to Home)
 */
@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Screen.Home.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Home screen - displays list of monitored apps
        composable(route = Screen.Home.route) {
            // TODO: Implement HomeScreen composable
            // HomeScreen(
            //     onNavigateToAppDetail = { appId ->
            //         navController.navigate(Screen.AppDetail.createRoute(appId))
            //     },
            //     onNavigateToSettings = {
            //         navController.navigate(Screen.Settings.route)
            //     }
            // )
        }
        
        // App detail screen - shows app information and rules
        composable(
            route = Screen.AppDetail.route,
            arguments = listOf(
                navArgument("appId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val appId = backStackEntry.arguments?.getLong("appId") ?: 0L
            // TODO: Implement AppDetailScreen composable
            // AppDetailScreen(
            //     appId = appId,
            //     onNavigateBack = { navController.popBackStack() },
            //     onNavigateToAddRule = { appId ->
            //         navController.navigate(Screen.AddRule.createRoute(appId))
            //     },
            //     onNavigateToEditRule = { ruleId ->
            //         navController.navigate(Screen.EditRule.createRoute(ruleId))
            //     }
            // )
        }
        
        // Add rule screen - create new usage rules
        composable(
            route = Screen.AddRule.route,
            arguments = listOf(
                navArgument("appId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val appId = backStackEntry.arguments?.getLong("appId") ?: 0L
            // TODO: Implement AddRuleScreen composable
            // AddRuleScreen(
            //     appId = appId,
            //     onNavigateBack = { navController.popBackStack() },
            //     onRuleSaved = { navController.popBackStack() }
            // )
        }
        
        // Edit rule screen - modify existing rules
        composable(
            route = Screen.EditRule.route,
            arguments = listOf(
                navArgument("ruleId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val ruleId = backStackEntry.arguments?.getLong("ruleId") ?: 0L
            // TODO: Implement EditRuleScreen composable
            // EditRuleScreen(
            //     ruleId = ruleId,
            //     onNavigateBack = { navController.popBackStack() },
            //     onRuleSaved = { navController.popBackStack() }
            // )
        }
        
        // Settings screen - global app configuration
        composable(route = Screen.Settings.route) {
            // TODO: Implement SettingsScreen composable
            // SettingsScreen(
            //     onNavigateBack = { navController.popBackStack() }
            // )
        }
        
        // Blocking screen - shown when app usage is restricted
        composable(route = Screen.Blocking.route) {
            // TODO: Implement BlockingScreen composable
            // BlockingScreen(
            //     onClose = {
            //         // Return to home screen and clear back stack
            //         navController.navigate(Screen.Home.route) {
            //             popUpTo(Screen.Home.route) { inclusive = true }
            //         }
            //     }
            // )
        }
    }
}
