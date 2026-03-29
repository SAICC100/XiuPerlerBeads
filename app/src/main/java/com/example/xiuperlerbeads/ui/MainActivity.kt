package com.example.xiuperlerbeads.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
    
    // Screens that should show bottom bar
    val bottomBarRoutes = listOf(
        Screen.Home.route,
        Screen.Create.route,
        Screen.Inventory.route,
        Screen.Projects.route
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
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
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
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
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
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Folder, contentDescription = "项目") },
                        label = { Text("项目") },
                        selected = currentRoute == Screen.Projects.route,
                        onClick = {
                            navController.navigate(Screen.Projects.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
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
                val projectId = backStackEntry.arguments?.getString("projectId") ?: "-1"
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
                    onNavigateBack = { navController.popBackStack() }
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
                val projectId = backStackEntry.arguments?.getString("projectId") ?: "-1"
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
                    onTemplateSelected = { template ->
                        // TODO: Navigate to canvas with template
                        navController.popBackStack()
                    }
                )
            }
            
            // Brand Manager Screen
            composable(Screen.BrandManager.route) {
                BrandManagerScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaceholderScreen(
    title: String,
    description: String,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.Construction,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
