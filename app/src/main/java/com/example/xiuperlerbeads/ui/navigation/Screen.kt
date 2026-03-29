package com.example.xiuperlerbeads.ui.navigation

/**
 * Navigation routes for the app
 */
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Create : Screen("create")
    object Inventory : Screen("inventory")
    object Projects : Screen("projects")
    object Canvas : Screen("canvas/{projectId}") {
        fun createRoute(projectId: String = "-1") = "canvas/$projectId"
    }
    object ImageImport : Screen("image_import")
    object AIScan : Screen("ai_scan")
    object Statistics : Screen("statistics")
    object Settings : Screen("settings")
    object AISettings : Screen("ai_settings")
    object TemplateLibrary : Screen("template_library")
    object BrandManager : Screen("brand_manager")
    object Restock : Screen("restock")
    object Export : Screen("export/{projectId}") {
        fun createRoute(projectId: String) = "export/$projectId"
    }
}

/**
 * Bottom navigation items
 */
val bottomNavItems = listOf(
    BottomNavItem(
        route = Screen.Home.route,
        title = "首页",
        icon = "home"
    ),
    BottomNavItem(
        route = Screen.Create.route,
        title = "创作",
        icon = "create"
    ),
    BottomNavItem(
        route = Screen.Inventory.route,
        title = "库存",
        icon = "inventory"
    ),
    BottomNavItem(
        route = Screen.Projects.route,
        title = "项目",
        icon = "projects"
    )
)

data class BottomNavItem(
    val route: String,
    val title: String,
    val icon: String
)
