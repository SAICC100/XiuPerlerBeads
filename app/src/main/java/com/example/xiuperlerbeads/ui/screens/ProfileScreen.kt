package com.example.xiuperlerbeads.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.xiuperlerbeads.ui.viewmodel.InventoryViewModel

/**
 * 秀拼豆 — 「我的」页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToBrandManager: () -> Unit = {},
    onNavigateToStatistics: () -> Unit = {},
    onNavigateToRestock: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToColorConverter: () -> Unit = {},
    onNavigateToShipping: () -> Unit = {},
    onNavigateToCalendar: () -> Unit = {},
    onNavigateToBackupRestore: () -> Unit = {},
    onNavigateToHelpCenter: () -> Unit = {},
    inventoryViewModel: InventoryViewModel = viewModel()
) {
    val state by inventoryViewModel.state.collectAsState()
    val pendingOrderCount = state.purchaseRecords.size
    val completedCount = state.projects.count { it.completedDate != null }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的", fontWeight = FontWeight.Bold) }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // ── 数据概览卡片 ────────────────────────────────────────────────
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "库存概览",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            OverviewItem(
                                label = "品牌",
                                value = state.brands.size.toString(),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            VerticalDivider(
                                modifier = Modifier.height(40.dp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                            )
                            OverviewItem(
                                label = "库存条目",
                                value = state.stocks.filter { !it.isHidden }.size.toString(),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            VerticalDivider(
                                modifier = Modifier.height(40.dp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                            )
                            OverviewItem(
                                label = "完成作品",
                                value = completedCount.toString(),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(8.dp)) }

            // ── 工具入口 ───────────────────────────────────────────────────
            item {
                Text(
                    "工具",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
            item {
                EntryRow(
                    icon = Icons.Default.SwapHoriz,
                    iconTint = MaterialTheme.colorScheme.primary,
                    title = "色号转换",
                    subtitle = "跨品牌色号对照查询",
                    badge = null,
                    onClick = onNavigateToColorConverter
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
            }
            item {
                EntryRow(
                    icon = Icons.Default.LocalShipping,
                    iconTint = MaterialTheme.colorScheme.tertiary,
                    title = "运输中订单",
                    subtitle = "记录已购但未到货的豆子",
                    badge = if (pendingOrderCount > 0) pendingOrderCount.toString() else null,
                    onClick = onNavigateToShipping
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
            }
            item {
                EntryRow(
                    icon = Icons.Default.CalendarMonth,
                    iconTint = MaterialTheme.colorScheme.secondary,
                    title = "成品日历",
                    subtitle = "按日历查看完成的作品",
                    badge = null,
                    onClick = onNavigateToCalendar
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
            }
            item {
                EntryRow(
                    icon = Icons.Default.ShoppingCart,
                    iconTint = MaterialTheme.colorScheme.secondary,
                    title = "补货清单",
                    subtitle = "查看库存不足的色号",
                    badge = null,
                    onClick = onNavigateToRestock
                )
            }

            item { Spacer(Modifier.height(16.dp)) }

            // ── 管理入口 ───────────────────────────────────────────────────
            item {
                Text(
                    "管理",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
            item {
                EntryRow(
                    icon = Icons.Default.Category,
                    iconTint = MaterialTheme.colorScheme.primary,
                    title = "品牌管理",
                    subtitle = "添加、编辑豆豆品牌",
                    badge = null,
                    onClick = onNavigateToBrandManager
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
            }
            item {
                EntryRow(
                    icon = Icons.Default.BarChart,
                    iconTint = MaterialTheme.colorScheme.secondary,
                    title = "用量统计",
                    subtitle = "查看各品牌用量趋势",
                    badge = null,
                    onClick = onNavigateToStatistics
                )
            }

            item { Spacer(Modifier.height(16.dp)) }

            // ── 应用设置 ───────────────────────────────────────────────────
            item {
                Text(
                    "应用",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
            item {
                EntryRow(
                    icon = Icons.Default.Backup,
                    iconTint = MaterialTheme.colorScheme.tertiary,
                    title = "备份与恢复",
                    subtitle = "自动备份，多版本快照管理",
                    badge = null,
                    onClick = onNavigateToBackupRestore
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
            }
            item {
                EntryRow(
                    icon = Icons.Default.Settings,
                    iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                    title = "设置",
                    subtitle = "数据导入导出、AI 设置",
                    badge = null,
                    onClick = onNavigateToSettings
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
            }
            item {
                EntryRow(
                    icon = Icons.Default.HelpOutline,
                    iconTint = MaterialTheme.colorScheme.primary,
                    title = "帮助中心",
                    subtitle = "使用教程、常见问题",
                    badge = null,
                    onClick = onNavigateToHelpCenter
                )
            }
        }
    }
}

@Composable
private fun OverviewItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = color.copy(alpha = 0.7f))
    }
}

@Composable
private fun EntryRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    badge: String?,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = {
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        },
        leadingContent = {
            Box(
                modifier = Modifier.size(36.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
            }
        },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (badge != null) {
                    Badge { Text(badge) }
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        modifier = Modifier.then(
            Modifier.clickable(onClick = onClick)
        )
    )
}
