package me.crossevol.mobilemonitor.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController

/**
 * Main app composable that sets up the navigation structure.
 * This should be called from MainActivity's setContent block.
 * 
 * Example usage in MainActivity:
 * ```
 * setContent {
 *     MobileMonitorTheme {
 *         AppNavigationContainer()
 *     }
 * }
 * ```
 */
@Composable
fun AppNavigationContainer() {
    val navController = rememberNavController()
    
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        AppNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

/**
 * Example of how to integrate navigation with existing MainActivity.
 * 
 * To use navigation in your MainActivity:
 * 
 * 1. Update onCreate to use AppNavigationContainer:
 * ```kotlin
 * override fun onCreate(savedInstanceState: Bundle?) {
 *     super.onCreate(savedInstanceState)
 *     enableEdgeToEdge()
 *     setContent {
 *         MobileMonitorTheme {
 *             AppNavigationContainer()
 *         }
 *     }
 * }
 * ```
 * 
 * 2. When implementing screens, pass navigation callbacks:
 * ```kotlin
 * composable(route = Screen.Home.route) {
 *     HomeScreen(
 *         onNavigateToAppDetail = { appId ->
 *             NavigationHelper.navigateToAppDetail(navController, appId)
 *         },
 *         onNavigateToSettings = {
 *             NavigationHelper.navigateToSettings(navController)
 *         }
 *     )
 * }
 * ```
 * 
 * 3. For screens with parameters, extract them from backStackEntry:
 * ```kotlin
 * composable(
 *     route = Screen.AppDetail.route,
 *     arguments = listOf(navArgument("appId") { type = NavType.LongType })
 * ) { backStackEntry ->
 *     val appId = backStackEntry.arguments?.getLong("appId") ?: 0L
 *     AppDetailScreen(
 *         appId = appId,
 *         onNavigateBack = { NavigationHelper.navigateBack(navController) }
 *     )
 * }
 * ```
 * 
 * 4. For back navigation, use NavigationHelper:
 * ```kotlin
 * Button(onClick = { NavigationHelper.navigateBack(navController) }) {
 *     Text("Back")
 * }
 * ```
 */
