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
        // Home screen - displays list of monitored apps with usage statistics
        composable(route = Screen.Home.route) {
            val context = LocalContext.current
            
            // Set up AppRestrictionRepository for monitored apps
            val database = AppRestrictionDatabase.getDatabase(context)
            val restrictionRepository = AppRestrictionRepositoryImpl(
                appInfoDao = database.appInfoDao(),
                appRuleDao = database.appRuleDao(),
                context = context
            )
            val homeViewModel: HomeViewModel = viewModel(
                factory = HomeViewModelFactory(restrictionRepository)
            )
            
            // Set up UsageStatsRepository for usage statistics
            val usageStatsRepository = me.crossevol.mobilemonitor.repository.UsageStatsRepositoryImpl(context)
            val usageStatsViewModel: me.crossevol.mobilemonitor.viewmodel.UsageStatsViewModel = viewModel(
                factory = me.crossevol.mobilemonitor.viewmodel.UsageStatsViewModelFactory(usageStatsRepository)
            )
            
            HomeScreen(
                usageStatsViewModel = usageStatsViewModel,
                homeViewModel = homeViewModel,
                onNavigateToAppDetail = { packageName ->
                    // Navigate to app detail, creating new app entry if needed
                    navController.navigate(Screen.AppDetail.createRoute(packageName))
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onOpenPermissionSettings = {
                    // Open usage access settings
                    val intent = android.content.Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS)
                    context.startActivity(intent)
                }
            )
        }
        
        // App detail screen - shows app information and rules
        composable(
            route = Screen.AppDetail.route,
            arguments = listOf(
                navArgument("packageName") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val packageName = backStackEntry.arguments?.getString("packageName")
            
            // Handle missing or invalid package name
            if (packageName.isNullOrBlank()) {
                // Navigate back to home on error
                androidx.compose.runtime.LaunchedEffect(Unit) {
                    navController.popBackStack(Screen.Home.route, inclusive = false)
                }
                return@composable
            }
            
            val context = LocalContext.current
            val database = AppRestrictionDatabase.getDatabase(context)
            val repository = AppRestrictionRepositoryImpl(
                appInfoDao = database.appInfoDao(),
                appRuleDao = database.appRuleDao(),
                context = context
            )
            
            // Get or create app by package name
            val viewModel: AppDetailViewModel = viewModel(
                factory = AppDetailViewModelFactory(packageName, repository)
            )
            
            AppDetailScreen(
                viewModel = viewModel,
                onNavigateBack = { 
                    try {
                        navController.popBackStack()
                    } catch (e: Exception) {
                        // Handle navigation failure gracefully
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                },
                onNavigateToAddRule = { appId ->
                    try {
                        navController.navigate(Screen.AddRule.createRoute(appId))
                    } catch (e: Exception) {
                        // Log error but don't crash
                        e.printStackTrace()
                    }
                },
                onNavigateToEditRule = { ruleId ->
                    try {
                        navController.navigate(Screen.EditRule.createRoute(ruleId))
                    } catch (e: Exception) {
                        // Log error but don't crash
                        e.printStackTrace()
                    }
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
            
            // Handle invalid app ID
            if (appId == 0L) {
                androidx.compose.runtime.LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
                return@composable
            }
            
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
                onNavigateBack = { 
                    try {
                        navController.popBackStack()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                },
                onRuleSaved = { 
                    try {
                        navController.popBackStack()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
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
            
            // Handle invalid rule ID
            if (ruleId == 0L) {
                androidx.compose.runtime.LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
                return@composable
            }
            
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
                onNavigateBack = { 
                    try {
                        navController.popBackStack()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                },
                onRuleSaved = { 
                    try {
                        navController.popBackStack()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
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
