package com.example.xiuperlerbeads.ui.navigation

/**
 * 秀拼豆 Navigation routes
 */
sealed class Screen(val route: String) {
    companion object {
        const val NEW_PROJECT_ID = "-1"
    }

    // 底部导航主页
    object Home : Screen("home")
    object Inventory : Screen("inventory")
    object Projects : Screen("projects")
    object Profile : Screen("profile")

    // 创作流程
    object Create : Screen("create")
    object Canvas : Screen("canvas/{projectId}") {
        fun createRoute(projectId: String = NEW_PROJECT_ID) = "canvas/$projectId"
    }
    object ImageImport : Screen("image_import")
    object AIScan : Screen("ai_scan")
    object TemplateLibrary : Screen("template_library")
    object Export : Screen("export/{projectId}") {
        fun createRoute(projectId: String) = "export/$projectId"
    }

    // 库存管理
    object BrandManager : Screen("brand_manager")
    object Restock : Screen("restock")
    object Statistics : Screen("statistics")
    object ColorConverter : Screen("color_converter")
    object HiddenColors : Screen("hidden_colors/{brandId}") {
        fun createRoute(brandId: String) = "hidden_colors/$brandId"
    }
    object CustomColors : Screen("custom_colors/{brandId}") {
        fun createRoute(brandId: String) = "custom_colors/$brandId"
    }
    object Shipping : Screen("shipping")
    object CompletionCalendar : Screen("completion_calendar")
    object BackupRestore : Screen("backup_restore")

    // 设置
    object Settings : Screen("settings")
    object AISettings : Screen("ai_settings")

    // 帮助中心
    object HelpCenter : Screen("help_center")
}

/**
 * 底部导航 Tab（首页 / 创作 / 库存 / 我的）
 */
val bottomNavItems = listOf(
    BottomNavItem(route = Screen.Home.route, title = "首页", icon = "home"),
    BottomNavItem(route = Screen.Create.route, title = "创作", icon = "palette"),
    BottomNavItem(route = Screen.Inventory.route, title = "库存", icon = "inventory"),
    BottomNavItem(route = Screen.Profile.route, title = "我的", icon = "person")
)

data class BottomNavItem(
    val route: String,
    val title: String,
    val icon: String
)
