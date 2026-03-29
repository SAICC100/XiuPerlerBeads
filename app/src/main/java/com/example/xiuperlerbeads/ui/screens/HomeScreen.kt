package com.example.xiuperlerbeads.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.xiuperlerbeads.domain.model.BrandStock
import com.example.xiuperlerbeads.domain.model.ProjectRecord
import com.example.xiuperlerbeads.ui.theme.StockLow
import com.example.xiuperlerbeads.ui.theme.StockOut
import com.example.xiuperlerbeads.ui.viewmodel.InventoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToInventory: () -> Unit = {},
    onNavigateToProjects: () -> Unit = {},
    onNavigateToCreate: () -> Unit = {},
    onNavigateToAIScan: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToTemplateLibrary: () -> Unit = {},
    onNavigateToStatistics: () -> Unit = {},
    onNavigateToRestock: () -> Unit = {},
    inventoryViewModel: InventoryViewModel = viewModel()
) {
    val state by inventoryViewModel.state.collectAsState()
    var showSearchDialog by remember { mutableStateOf(false) }
    
    // Mock recent projects for demo
    val recentProjects = remember {
        listOf(
            RecentProject("小兔子", "5分钟前", 32),
            RecentProject("猫咪", "昨天", 48),
            RecentProject("皮卡丘", "3天前", 64)
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "秀拼豆",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "拼豆创作好帮手",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showSearchDialog = true }) {
                        Icon(Icons.Default.Search, "搜索")
                    }
                    IconButton(onClick = onNavigateToAIScan) {
                        Icon(Icons.Default.AutoAwesome, "AI扫描")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, "设置")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Quick Actions
            item {
                QuickActionsRow(
                    onNavigateToCreate = onNavigateToCreate,
                    onNavigateToInventory = onNavigateToInventory,
                    onNavigateToProjects = onNavigateToProjects,
                    onNavigateToStatistics = onNavigateToStatistics,
                    onNavigateToRestock = onNavigateToRestock
                )
            }
            
            // Inventory Summary Card
            item {
                InventorySummaryCard(
                    totalColors = state.totalColors,
                    totalQuantity = state.totalQuantity,
                    lowStockCount = state.lowStockCount,
                    outOfStockCount = state.outOfStockCount,
                    onClick = onNavigateToInventory
                )
            }
            
            // Low Stock Alert
            if (state.lowStockCount > 0 || state.outOfStockCount > 0) {
                item {
                    LowStockAlert(
                        lowStockCount = state.lowStockCount,
                        outOfStockCount = state.outOfStockCount,
                        onClick = onNavigateToInventory
                    )
                }
            }
            
            // Low Stock Items Preview
            if (state.stocks.any { it.isLowStock() }) {
                item {
                    Text(
                        "低库存提醒",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                item {
                    LowStockPreview(
                        stocks = state.stocks.filter { it.isLowStock() }.take(5),
                        getColorInfo = { inventoryViewModel.getColorInfo(it) },
                        onClick = onNavigateToInventory
                    )
                }
            }
            
            // Recent Projects
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "最近项目",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    TextButton(onClick = onNavigateToProjects) {
                        Text("查看全部")
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            item {
                RecentProjectsRow(
                    projects = recentProjects,
                    onProjectClick = onNavigateToProjects
                )
            }
            
            // Tips Card
            item {
                TipsCard()
            }
        }
    }
    
    // Search Dialog
    if (showSearchDialog) {
        SearchDialog(
            onDismiss = { showSearchDialog = false },
            onNavigateToTemplateLibrary = {
                showSearchDialog = false
                onNavigateToTemplateLibrary()
            },
            onNavigateToProjects = {
                showSearchDialog = false
                onNavigateToProjects()
            }
        )
    }
}

@Composable
private fun QuickActionsRow(
    onNavigateToCreate: () -> Unit,
    onNavigateToInventory: () -> Unit,
    onNavigateToProjects: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    onNavigateToRestock: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionButton(
                icon = Icons.Default.Add,
                label = "创作",
                color = MaterialTheme.colorScheme.primary,
                onClick = onNavigateToCreate,
                modifier = Modifier.weight(1f)
            )
            QuickActionButton(
                icon = Icons.Default.Inventory2,
                label = "库存",
                color = MaterialTheme.colorScheme.secondary,
            onClick = onNavigateToInventory,
            modifier = Modifier.weight(1f)
        )
        QuickActionButton(
            icon = Icons.Default.Folder,
            label = "项目",
            color = MaterialTheme.colorScheme.tertiary,
            onClick = onNavigateToProjects,
            modifier = Modifier.weight(1f)
        )
        }
        
        // 第二行：统计和补货
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionButton(
                icon = Icons.Default.BarChart,
                label = "统计",
                color = Color(0xFF2196F3),
                onClick = onNavigateToStatistics,
                modifier = Modifier.weight(1f)
            )
            QuickActionButton(
                icon = Icons.Default.ShoppingCart,
                label = "补货",
                color = Color(0xFF4CAF50),
                onClick = onNavigateToRestock,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = color
            )
        }
    }
}

@Composable
private fun InventorySummaryCard(
    totalColors: Int,
    totalQuantity: Int,
    lowStockCount: Int,
    outOfStockCount: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "库存概览",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
StatColumn(
                    value = totalColors.toString(),
                    label = "颜色数",
                    color = MaterialTheme.colorScheme.primary
                )
                StatColumn(
                    value = totalQuantity.toString(),
                    label = "总数量",
                    color = MaterialTheme.colorScheme.secondary
                )
                StatColumn(
                    value = lowStockCount.toString(),
                    label = "低库存",
                    color = StockLow
                )
                StatColumn(
                    value = outOfStockCount.toString(),
                    label = "缺货",
                    color = StockOut
                )
            }
        }
    }
}

@Composable
private fun StatColumn(
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LowStockAlert(
    lowStockCount: Int,
    outOfStockCount: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "库存提醒",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    buildString {
                        if (outOfStockCount > 0) {
                            append("${outOfStockCount}种颜色缺货")
                        }
                        if (outOfStockCount > 0 && lowStockCount > 0) {
                            append("，")
                        }
                        if (lowStockCount > 0) {
                            append("${lowStockCount}种颜色库存不足")
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
private fun LowStockPreview(
    stocks: List<BrandStock>,
    getColorInfo: (String) -> com.example.xiuperlerbeads.domain.model.BeadColor?,
    onClick: () -> Unit
) {
    Card {
        Column {
            stocks.forEach { stock ->
                val colorInfo = getColorInfo(stock.mardCode)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onClick() }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(colorInfo?.toComposeColor() ?: Color.Gray)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stock.mardCode,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        if (colorInfo?.colorName?.isNotEmpty() == true) {
                            Text(
                                text = colorInfo.colorName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Text(
                        text = "${stock.available}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (stock.available <= 0) StockOut else StockLow
                    )
                }
                if (stock != stocks.last()) {
                    Divider(modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    }
}

@Composable
private fun RecentProjectsRow(
    projects: List<RecentProject>,
    onProjectClick: () -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(projects) { project ->
            Card(
                modifier = Modifier
                    .width(140.dp)
                    .clickable { onProjectClick() }
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.GridOn,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                    }
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = project.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                        Text(
                            text = "${project.size}×${project.size}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = project.timeAgo,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TipsCard() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                Icons.Default.Lightbulb,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    "小贴士",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "新手建议从32×32的小图案开始练习，使用AI扫描功能可以快速识别图纸中的颜色和数量！",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private data class RecentProject(
    val name: String,
    val timeAgo: String,
    val size: Int
)

/**
 * Search Dialog for quick navigation and search
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchDialog(
    onDismiss: () -> Unit,
    onNavigateToTemplateLibrary: () -> Unit,
    onNavigateToProjects: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    
    val quickActions = listOf(
        QuickAction("素材库", "浏览拼豆图案素材", Icons.Default.GridOn, onNavigateToTemplateLibrary),
        QuickAction("我的项目", "查看已保存的项目", Icons.Default.Folder, onNavigateToProjects),
        QuickAction("库存管理", "管理拼豆颜色库存", Icons.Default.Inventory2, onNavigateToTemplateLibrary),
        QuickAction("AI扫描", "使用AI识别图片", Icons.Default.AutoAwesome, onNavigateToTemplateLibrary)
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("搜索功能...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true
            )
        },
        text = {
            Column {
                Text(
                    "快捷功能",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                quickActions.forEach { action ->
                    ListItem(
                        headlineContent = { Text(action.title) },
                        supportingContent = { Text(action.description) },
                        leadingContent = {
                            Icon(action.icon, null, tint = MaterialTheme.colorScheme.primary)
                        },
                        modifier = Modifier.clickable { action.onClick() }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
}

private data class QuickAction(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)
