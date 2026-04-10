package com.example.xiuperlerbeads.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.xiuperlerbeads.ui.navigation.Screen
import com.example.xiuperlerbeads.ui.screens.*
import com.example.xiuperlerbeads.ui.theme.XiuPerlerBeadsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            XiuPerlerBeadsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomBarRoutes = listOf(
        Screen.Home.route,
        Screen.Create.route,
        Screen.Inventory.route,
        Screen.Profile.route
    )

    val showBottomBar = currentRoute in bottomBarRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "首页") },
                        label = { Text("首页") },
                        selected = currentRoute == Screen.Home.route,
                        onClick = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Palette, contentDescription = "创作") },
                        label = { Text("创作") },
                        selected = currentRoute == Screen.Create.route,
                        onClick = {
                            navController.navigate(Screen.Create.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Inventory2, contentDescription = "库存") },
                        label = { Text("库存") },
                        selected = currentRoute == Screen.Inventory.route,
                        onClick = {
                            navController.navigate(Screen.Inventory.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Person, contentDescription = "我的") },
                        label = { Text("我的") },
                        selected = currentRoute == Screen.Profile.route,
                        onClick = {
                            navController.navigate(Screen.Profile.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            // Home Screen
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToInventory = { navController.navigate(Screen.Inventory.route) },
                    onNavigateToProjects = { navController.navigate(Screen.Projects.route) },
                    onNavigateToCreate = { navController.navigate(Screen.Create.route) },
                    onNavigateToAIScan = { navController.navigate(Screen.AIScan.route) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    onNavigateToStatistics = { navController.navigate(Screen.Statistics.route) },
                    onNavigateToRestock = { navController.navigate(Screen.Restock.route) },
                    onNavigateToTemplateLibrary = { navController.navigate(Screen.TemplateLibrary.route) }
                )
            }

            // Create Screen
            composable(Screen.Create.route) {
                CreateScreen(
                    onNavigateToCanvas = { projectId ->
                        navController.navigate(Screen.Canvas.createRoute(projectId.toString()))
                    },
                    onNavigateToImport = {
                        navController.navigate(Screen.ImageImport.route)
                    },
                    onNavigateToTemplateLibrary = {
                        navController.navigate(Screen.TemplateLibrary.route)
                    }
                )
            }

            // Inventory Screen
            composable(Screen.Inventory.route) {
                InventoryScreen(
                    onNavigateToBrandManager = {
                        navController.navigate(Screen.BrandManager.route)
                    }
                )
            }

            // Profile Screen
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onNavigateToBrandManager = { navController.navigate(Screen.BrandManager.route) },
                    onNavigateToStatistics = { navController.navigate(Screen.Statistics.route) },
                    onNavigateToRestock = { navController.navigate(Screen.Restock.route) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    onNavigateToColorConverter = { navController.navigate(Screen.ColorConverter.route) },
                    onNavigateToShipping = { navController.navigate(Screen.Shipping.route) },
                    onNavigateToCalendar = { navController.navigate(Screen.CompletionCalendar.route) },
                    onNavigateToBackupRestore = { navController.navigate(Screen.BackupRestore.route) },
                    onNavigateToHelpCenter = { navController.navigate(Screen.HelpCenter.route) }
                )
            }

            // Projects Screen
            composable(Screen.Projects.route) {
                ProjectsScreen(
                    onNavigateToCanvas = { projectId ->
                        navController.navigate(Screen.Canvas.createRoute(projectId.toString()))
                    }
                )
            }

            // Canvas Screen
            composable(Screen.Canvas.route) { backStackEntry ->
                val projectId = backStackEntry.arguments?.getString("projectId") ?: Screen.NEW_PROJECT_ID
                CanvasScreen(
                    projectId = projectId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Image Import Screen
            composable(Screen.ImageImport.route) {
                ImageImportScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onImportComplete = { projectId ->
                        navController.navigate(Screen.Canvas.createRoute(projectId.toString())) {
                            popUpTo(Screen.Create.route)
                        }
                    }
                )
            }

            // AI Scan Screen
            composable(Screen.AIScan.route) {
                AIScanScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToCanvas = { projectId ->
                        navController.navigate(Screen.Canvas.createRoute(projectId))
                    }
                )
            }

            // Statistics Screen
            composable(Screen.Statistics.route) {
                StatisticsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Restock Screen
            composable(Screen.Restock.route) {
                RestockScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Export Screen
            composable(Screen.Export.route) { backStackEntry ->
                val projectId = backStackEntry.arguments?.getString("projectId") ?: Screen.NEW_PROJECT_ID
                ExportScreen(
                    projectId = projectId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Settings Screen
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToAISettings = { navController.navigate(Screen.AISettings.route) }
                )
            }

            // AI Settings Screen
            composable(Screen.AISettings.route) {
                AISettingsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Template Library Screen
            composable(Screen.TemplateLibrary.route) {
                TemplateLibraryScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onTemplateSelected = { _ ->
                        navController.navigate(Screen.Canvas.createRoute(Screen.NEW_PROJECT_ID)) {
                            popUpTo(Screen.TemplateLibrary.route) { inclusive = true }
                        }
                    }
                )
            }

            // Brand Manager Screen
            composable(Screen.BrandManager.route) {
                BrandManagerScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToHiddenColors = { brandId ->
                        navController.navigate(Screen.HiddenColors.createRoute(brandId))
                    },
                    onNavigateToCustomColors = { brandId ->
                        navController.navigate(Screen.CustomColors.createRoute(brandId))
                    }
                )
            }

            // Color Converter Screen
            composable(Screen.ColorConverter.route) {
                ColorConverterScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Hidden Colors Screen
            composable(Screen.HiddenColors.route) { backStackEntry ->
                val brandId = backStackEntry.arguments?.getString("brandId") ?: ""
                HiddenColorsScreen(
                    brandId = brandId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Custom Colors Screen
            composable(Screen.CustomColors.route) { backStackEntry ->
                val brandId = backStackEntry.arguments?.getString("brandId") ?: ""
                CustomColorsScreen(
                    brandId = brandId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Shipping Screen
            composable(Screen.Shipping.route) {
                ShippingScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Completion Calendar Screen
            composable(Screen.CompletionCalendar.route) {
                CompletionCalendarScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Backup & Restore Screen
            composable(Screen.HelpCenter.route) {
                HelpCenterScreen(onNavigateBack = { navController.popBackStack() })
            }

            composable(Screen.BackupRestore.route) {
                BackupRestoreScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
