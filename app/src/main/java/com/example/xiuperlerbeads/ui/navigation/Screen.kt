package com.example.xiuperlerbeads.ui.navigation

/**
 * Navigation routes for the app
 */
sealed class Screen(val route: String) {
    companion object {
        /** Sentinel value for a new canvas project (no existing project ID). */
        const val NEW_PROJECT_ID = "-1"
    }

    object Home : Screen("home")
    object Create : Screen("create")
    object Inventory : Screen("inventory")
    object Projects : Screen("projects")
    object JournalHome : Screen("journal_home")
    object AddEntry : Screen("add_entry")
    object JournalSummary : Screen("journal_summary")
    object Profile : Screen("profile")
    object Canvas : Screen("canvas/{projectId}") {
        fun createRoute(projectId: String = NEW_PROJECT_ID) = "canvas/$projectId"
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
        route = Screen.JournalHome.route,
        title = "首页",
        icon = "home"
    ),
    BottomNavItem(
        route = Screen.JournalSummary.route,
        title = "汇总",
        icon = "bar_chart"
    ),
    BottomNavItem(
        route = Screen.Inventory.route,
        title = "库存",
        icon = "inventory"
    ),
    BottomNavItem(
        route = Screen.Profile.route,
        title = "我的",
        icon = "person"
    )
)

data class BottomNavItem(
    val route: String,
    val title: String,
    val icon: String
)
