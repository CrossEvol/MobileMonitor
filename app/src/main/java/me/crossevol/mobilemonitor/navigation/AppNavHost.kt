package me.crossevol.mobilemonitor.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import me.crossevol.mobilemonitor.data.database.AppRestrictionDatabase
import me.crossevol.mobilemonitor.repository.AppRestrictionRepositoryImpl
import me.crossevol.mobilemonitor.ui.AddRuleScreen
import me.crossevol.mobilemonitor.ui.AppDetailScreen
import me.crossevol.mobilemonitor.ui.EditRuleScreen
import me.crossevol.mobilemonitor.ui.HomeScreen
import me.crossevol.mobilemonitor.viewmodel.AddRuleViewModel
import me.crossevol.mobilemonitor.viewmodel.AddRuleViewModelFactory
import me.crossevol.mobilemonitor.viewmodel.AppDetailViewModel
import me.crossevol.mobilemonitor.viewmodel.AppDetailViewModelFactory
import me.crossevol.mobilemonitor.viewmodel.EditRuleViewModel
import me.crossevol.mobilemonitor.viewmodel.EditRuleViewModelFactory
import me.crossevol.mobilemonitor.viewmodel.HomeViewModel
import me.crossevol.mobilemonitor.viewmodel.HomeViewModelFactory

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
            val context = LocalContext.current
            val database = AppRestrictionDatabase.getDatabase(context)
            val repository = AppRestrictionRepositoryImpl(
                appInfoDao = database.appInfoDao(),
                appRuleDao = database.appRuleDao(),
                context = context
            )
            val viewModel: HomeViewModel = viewModel(
                factory = HomeViewModelFactory(repository)
            )
            
            HomeScreen(
                viewModel = viewModel,
                onNavigateToAppDetail = { appId ->
                    navController.navigate(Screen.AppDetail.createRoute(appId))
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
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
            val context = LocalContext.current
            val database = AppRestrictionDatabase.getDatabase(context)
            val repository = AppRestrictionRepositoryImpl(
                appInfoDao = database.appInfoDao(),
                appRuleDao = database.appRuleDao(),
                context = context
            )
            val viewModel: AppDetailViewModel = viewModel(
                factory = AppDetailViewModelFactory(appId, repository)
            )
            
            AppDetailScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddRule = { appId ->
                    navController.navigate(Screen.AddRule.createRoute(appId))
                },
                onNavigateToEditRule = { ruleId ->
                    navController.navigate(Screen.EditRule.createRoute(ruleId))
                }
            )
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
            val context = LocalContext.current
            val database = AppRestrictionDatabase.getDatabase(context)
            val repository = AppRestrictionRepositoryImpl(
                appInfoDao = database.appInfoDao(),
                appRuleDao = database.appRuleDao(),
                context = context
            )
            val viewModel: AddRuleViewModel = viewModel(
                factory = AddRuleViewModelFactory(appId, repository)
            )
            
            AddRuleScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onRuleSaved = { navController.popBackStack() }
            )
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
            val context = LocalContext.current
            val database = AppRestrictionDatabase.getDatabase(context)
            val repository = AppRestrictionRepositoryImpl(
                appInfoDao = database.appInfoDao(),
                appRuleDao = database.appRuleDao(),
                context = context
            )
            val viewModel: EditRuleViewModel = viewModel(
                factory = EditRuleViewModelFactory(ruleId, repository)
            )
            
            EditRuleScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onRuleSaved = { navController.popBackStack() }
            )
        }
        
        // Settings screen - global app configuration
        composable(route = Screen.Settings.route) {
            val context = LocalContext.current
            val viewModel: me.crossevol.mobilemonitor.viewmodel.SettingsViewModel = viewModel(
                factory = me.crossevol.mobilemonitor.viewmodel.SettingsViewModelFactory(context)
            )
            
            me.crossevol.mobilemonitor.ui.SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
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
