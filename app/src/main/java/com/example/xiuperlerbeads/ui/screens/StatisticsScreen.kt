package com.example.xiuperlerbeads.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.xiuperlerbeads.data.export.ExportManager
import com.example.xiuperlerbeads.domain.model.HistoryRecord
import com.example.xiuperlerbeads.domain.model.HistoryType
import com.example.xiuperlerbeads.ui.theme.StockEnough
import com.example.xiuperlerbeads.ui.theme.StockLow
import com.example.xiuperlerbeads.ui.theme.StockOut
import com.example.xiuperlerbeads.ui.viewmodel.InventoryViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * 数据统计屏幕
 * 展示库存统计、使用趋势、历史记录等
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    onNavigateBack: () -> Unit = {},
    inventoryViewModel: InventoryViewModel = viewModel()
) {
    val context = LocalContext.current
    val state by inventoryViewModel.state.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("概览", "使用趋势", "历史记录")
    val exportManager = remember { ExportManager(context) }
    
    // 计算统计数据
    val totalColors = state.stocks.count { !it.isHidden }
    val totalQuantity = state.stocks.sumOf { it.available }
    val lowStockCount = state.stocks.count { it.available in 1..50 && !it.isHidden }
    val outOfStockCount = state.stocks.count { it.available <= 0 && !it.isHidden }
    val enoughCount = state.stocks.count { it.available > 50 && !it.isHidden }
    
    // 历史记录
    val historyRecords = state.historyRecords
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("数据统计") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        try {
                            val sb = StringBuilder()
                            val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                            sb.appendLine("库存统计报表")
                            sb.appendLine("生成时间: ${fmt.format(Date())}")
                            sb.appendLine("=".repeat(30))
                            sb.appendLine("总颜色数: $totalColors 种")
                            sb.appendLine("总库存量: $totalQuantity 颗")
                            sb.appendLine("充足 (>50): $enoughCount 种")
                            sb.appendLine("偏少 (1-50): $lowStockCount 种")
                            sb.appendLine("缺货 (0): $outOfStockCount 种")
                            val uri = exportManager.saveTextToFile(sb.toString(), "inventory_report")
                            if (uri != null) {
                                val shareIntent = exportManager.shareContent(uri, "text/plain", "库存统计报表")
                                context.startActivity(Intent.createChooser(shareIntent, "分享报表"))
                            } else {
                                Toast.makeText(context, "导出失败", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "导出失败: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Icon(Icons.Default.Share, "导出")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab 切换
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }
            
            when (selectedTab) {
                0 -> OverviewTab(
                    totalColors = totalColors,
                    totalQuantity = totalQuantity,
                    lowStockCount = lowStockCount,
                    outOfStockCount = outOfStockCount,
                    enoughCount = enoughCount,
                    stocks = state.stocks
                )
                1 -> TrendTab(historyRecords = historyRecords)
                2 -> HistoryTab(
                    historyRecords = historyRecords,
                    onUndo = { recordId -> inventoryViewModel.undoHistoryRecord(recordId) }
                )
            }
        }
    }
}

@Composable
private fun OverviewTab(
    totalColors: Int,
    totalQuantity: Int,
    lowStockCount: Int,
    outOfStockCount: Int,
    enoughCount: Int,
    stocks: List<com.example.xiuperlerbeads.domain.model.BrandStock>
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 总体统计卡片
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "库存概览",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatBox(
                            value = totalColors.toString(),
                            label = "颜色数",
                            color = MaterialTheme.colorScheme.primary
                        )
                        StatBox(
                            value = totalQuantity.toString(),
                            label = "总数量",
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 状态分布图
                    if (totalColors > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 饼图
                            DonutChart(
                                data = listOf(
                                    ChartData("充足", enoughCount, StockEnough),
                                    ChartData("不足", lowStockCount, StockLow),
                                    ChartData("缺货", outOfStockCount, StockOut)
                                ),
                                modifier = Modifier.size(120.dp)
                            )
                            
                            // 图例
                            Column {
                                LegendItem(color = StockEnough, label = "充足 (>50)", count = enoughCount)
                                LegendItem(color = StockLow, label = "不足 (10-50)", count = lowStockCount)
                                LegendItem(color = StockOut, label = "缺货 (<10)", count = outOfStockCount)
                            }
                        }
                    }
                }
            }
        }
        
        // 颜色使用排名
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "颜色使用排名",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // 库存柱状图
                    val topStocks = stocks
                        .filter { !it.isHidden }
                        .sortedByDescending { it.used }
                        .take(10)
                    
                    if (topStocks.isNotEmpty()) {
                        val maxUsed = topStocks.maxOfOrNull { it.used } ?: 1
                        
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            topStocks.forEach { stock ->
                                val percentage = if (maxUsed > 0) stock.used.toFloat() / maxUsed else 0f
                                val color = when {
                                    stock.available <= 0 -> StockOut
                                    stock.available < 10 -> StockOut
                                    stock.available < 50 -> StockLow
                                    else -> StockEnough
                                }
                                
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            stock.mardCode,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Text(
                                            "已用 ${stock.used}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = percentage,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                                        color = color,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            "暂无使用数据",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        
        // 库存健康度
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "库存健康度",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val healthPercentage = if (totalColors > 0) {
                        (enoughCount.toFloat() / totalColors * 100).toInt()
                    } else 0
                    
                    val healthColor = when {
                        healthPercentage >= 80 -> Color(0xFF4CAF50)
                        healthPercentage >= 50 -> StockLow
                        else -> StockOut
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            DonutChart(
                                data = listOf(
                                    ChartData("健康", healthPercentage, healthColor),
                                    ChartData("不健康", 100 - healthPercentage, MaterialTheme.colorScheme.surfaceVariant)
                                ),
                                modifier = Modifier.fillMaxSize(),
                                strokeWidth = 12f
                            )
                            Text(
                                "$healthPercentage%",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = healthColor
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(24.dp))
                        
                        Column {
                            Text(
                                when {
                                    healthPercentage >= 80 -> "库存状态良好"
                                    healthPercentage >= 50 -> "库存需要注意"
                                    else -> "库存告急"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium,
                                color = healthColor
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                when {
                                    healthPercentage >= 80 -> "大部分颜色库存充足"
                                    healthPercentage >= 50 -> "${lowStockCount + outOfStockCount}种颜色需要关注"
                                    else -> "建议尽快补货"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TrendTab(historyRecords: List<HistoryRecord>) {
    val groupedByDate = remember(historyRecords) {
        historyRecords
            .groupBy { record ->
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                sdf.format(Date(record.timestamp))
            }
            .toList()
            .sortedByDescending { it.first }
            .take(7)
    }
    
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "最近7天活动",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (groupedByDate.isNotEmpty()) {
                        // 简易柱状图
                        val maxCount = groupedByDate.maxOfOrNull { it.second.size } ?: 1
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            groupedByDate.forEach { (date, records) ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    val height = (records.size.toFloat() / maxCount * 80).dp
                                    Box(
                                        modifier = Modifier
                                            .width(24.dp)
                                            .height(height)
                                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                            .background(MaterialTheme.colorScheme.primary)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        date.takeLast(5),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            "暂无活动记录",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        
        // 操作类型统计
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "操作统计",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val typeStats = historyRecords
                        .groupBy { it.type }
                        .mapValues { it.value.size }
                        .toList()
                        .sortedByDescending { it.second }
                    
                    typeStats.take(5).forEach { (type, count) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    when (type) {
                                        HistoryType.STOCK_ADD -> Icons.Default.Add
                                        HistoryType.STOCK_DEDUCT -> Icons.Default.Remove
                                        HistoryType.STOCK_UPDATE -> Icons.Default.Edit
                                        HistoryType.PROJECT_ADD -> Icons.Default.Add
                                        HistoryType.PROJECT_EXECUTE -> Icons.Default.PlayArrow
                                        HistoryType.PURCHASE_COMPLETE -> Icons.Default.ShoppingCart
                                        else -> Icons.Default.Info
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    type.displayName,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            Text(
                                count.toString(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        if (type != typeStats.take(5).last().first) {
                            Divider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryTab(historyRecords: List<HistoryRecord>, onUndo: (recordId: String) -> Unit = {}) {
    if (historyRecords.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.History,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "暂无历史记录",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(historyRecords.take(50)) { record ->
                HistoryItem(record = record, onUndo = onUndo)
            }
        }
    }
}

@Composable
private fun HistoryItem(record: HistoryRecord, onUndo: (recordId: String) -> Unit = {}) {
    val dateFormat = remember { SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()) }
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                when (record.type) {
                    HistoryType.STOCK_ADD -> Icons.Default.Add
                    HistoryType.STOCK_DEDUCT -> Icons.Default.Remove
                    HistoryType.STOCK_UPDATE -> Icons.Default.Edit
                    HistoryType.BRAND_ADD -> Icons.Default.Add
                    HistoryType.PROJECT_ADD -> Icons.Default.Add
                    HistoryType.PROJECT_EXECUTE -> Icons.Default.PlayArrow
                    HistoryType.PURCHASE_COMPLETE -> Icons.Default.ShoppingCart
                    else -> Icons.Default.Info
                },
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    record.description,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    dateFormat.format(Date(record.timestamp)),
                    style = MaterialTheme.typography.bodySmall,
color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // 显示变化量 + 撤销按钮
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                record.changeAmount?.let { amount ->
                    Text(
                        if (amount >= 0) "+$amount" else "$amount",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (amount >= 0) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                    )
                }
                val canUndo = record.type in listOf(HistoryType.STOCK_ADD, HistoryType.STOCK_UPDATE, HistoryType.STOCK_DEDUCT)
                        && record.oldValue != null && record.brandId != null && record.mardCode != null
                if (canUndo) {
                    IconButton(
                        onClick = { onUndo(record.id) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Undo,
                            contentDescription = "撤销",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatBox(
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
private fun DonutChart(
    data: List<ChartData>,
    modifier: Modifier = Modifier,
    strokeWidth: Float = 24f
) {
    Canvas(modifier = modifier) {
        val canvasSize = size.minDimension
        val radius = (canvasSize - strokeWidth) / 2
        val center = Offset(size.width / 2, size.height / 2)
        
        var startAngle = -90f
        
        data.forEach { item ->
            if (item.value > 0) {
                val sweepAngle = (item.value.toFloat() / data.sumOf { it.value } * 360f).coerceAtLeast(1f)
                
                drawArc(
                    color = item.color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                )
                
                startAngle += sweepAngle
            }
        }
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String,
    count: Int
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            "$label: $count",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

data class ChartData(
    val label: String,
    val value: Int,
    val color: Color
)

// 扩展 HistoryType 添加 displayName
val HistoryType.displayName: String
    get() = when (this) {
        HistoryType.STOCK_ADD -> "库存增加"
        HistoryType.STOCK_DEDUCT -> "库存扣减"
        HistoryType.STOCK_UPDATE -> "库存更新"
        HistoryType.BRAND_ADD -> "添加品牌"
        HistoryType.BRAND_DELETE -> "删除品牌"
        HistoryType.PROJECT_ADD -> "添加项目"
        HistoryType.PROJECT_DELETE -> "删除项目"
        HistoryType.PROJECT_EXECUTE -> "执行项目"
        HistoryType.PROJECT_ARCHIVE -> "归档项目"
        HistoryType.PURCHASE_ADD -> "添加采购"
        HistoryType.PURCHASE_COMPLETE -> "采购到货"
        else -> "其他操作"
    }
