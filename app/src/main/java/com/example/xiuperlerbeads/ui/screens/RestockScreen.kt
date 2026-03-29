package com.example.xiuperlerbeads.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.xiuperlerbeads.domain.model.BrandStock
import com.example.xiuperlerbeads.domain.model.ProjectRecord
import com.example.xiuperlerbeads.ui.theme.StockLow
import com.example.xiuperlerbeads.ui.theme.StockOut
import com.example.xiuperlerbeads.ui.viewmodel.InventoryViewModel

/**
 * 补货建议屏幕
 * 根据项目和当前库存推荐需要补货的颜色
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestockScreen(
    onNavigateBack: () -> Unit = {},
    inventoryViewModel: InventoryViewModel = viewModel()
) {
    val state by inventoryViewModel.state.collectAsState()
    var selectedProjectId by remember { mutableStateOf<String?>(null) }
    
    // 获取所有项目
    val projects = remember { state.projects }
    
    // 根据选中项目计算补货建议
    val restockSuggestions = remember(selectedProjectId, projects, state.stocks) {
        if (selectedProjectId == null) {
            // 全局补货建议：库存不足的颜色
            state.stocks
                .filter { it.available < 50 && !it.isHidden }
                .sortedBy { it.available }
                .map { stock ->
                    val colorInfo = inventoryViewModel.getColorInfo(stock.mardCode)
                    RestockItem(
                        mardCode = stock.mardCode,
                        colorName = colorInfo?.colorName ?: stock.mardCode,
                        currentStock = stock.available,
                        suggestedStock = 500, // 建议补到500颗
                        neededAmount = maxOf(0, 500 - stock.available)
                    )
                }
        } else {
            // 特定项目的补货建议
            val project = projects.find { it.id == selectedProjectId }
            project?.let { p ->
                p.beadUsage.mapNotNull { usage ->
                    val stock = state.stocks.find { 
                        it.mardCode == usage.colorCode || 
                        it.mardCode == inventoryViewModel.getColorInfo(usage.colorCode)?.mardCode 
                    }
                    val colorInfo = inventoryViewModel.getColorInfo(usage.colorCode)
                    if (stock != null && stock.available < usage.quantity) {
                        RestockItem(
                            mardCode = colorInfo?.mardCode ?: usage.colorCode,
                            colorName = colorInfo?.colorName ?: usage.colorCode,
                            currentStock = stock.available,
                            suggestedStock = usage.quantity,
                            neededAmount = usage.quantity - stock.available
                        )
                    } else null
                }.sortedByDescending { it.neededAmount }
            } ?: emptyList()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("补货建议") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { /* 刷新 */ }) {
                        Icon(Icons.Default.Refresh, "刷新")
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
            // 项目选择器
            if (projects.isNotEmpty()) {
                ProjectSelector(
                    projects = projects,
                    selectedProjectId = selectedProjectId,
                    onProjectSelected = { selectedProjectId = it }
                )
            }
            
            // 统计概览
            if (restockSuggestions.isNotEmpty()) {
                RestockSummary(suggestions = restockSuggestions)
            }
            
            // 补货列表
            if (restockSuggestions.isEmpty()) {
                EmptyRestockState()
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(restockSuggestions) { suggestion ->
                        RestockItemCard(
                            suggestion = suggestion,
                            onAddToCart = { 
                                // TODO: 添加到购物清单
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProjectSelector(
    projects: List<ProjectRecord>,
    selectedProjectId: String?,
    onProjectSelected: (String?) -> Unit
) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            "选择参考项目",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyColumn(
            modifier = Modifier.heightIn(max = 150.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onProjectSelected(null) }
                        .background(
                            if (selectedProjectId == null) MaterialTheme.colorScheme.primaryContainer
                            else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedProjectId == null,
                        onClick = { onProjectSelected(null) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("全部库存不足")
                }
            }
            
            items(projects.filter { it.isPlanned }) { project ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onProjectSelected(project.id) }
                        .background(
                            if (selectedProjectId == project.id) MaterialTheme.colorScheme.primaryContainer
                            else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedProjectId == project.id,
                        onClick = { onProjectSelected(project.id) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            project.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "${project.beadUsage.size}种颜色",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        
        Divider(modifier = Modifier.padding(vertical = 8.dp))
    }
}

@Composable
private fun RestockSummary(suggestions: List<RestockItem>) {
    val totalNeeded = suggestions.sumOf { it.neededAmount }
    val totalColors = suggestions.size
    val urgentCount = suggestions.count { it.currentStock < 10 }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SummaryItem(
                value = totalColors.toString(),
                label = "需要补货",
                unit = "种颜色"
            )
            SummaryItem(
                value = totalNeeded.toString(),
                label = "共需补货",
                unit = "颗"
            )
            SummaryItem(
                value = urgentCount.toString(),
                label = "紧急缺货",
                unit = "种",
                isUrgent = urgentCount > 0
            )
        }
    }
    
    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
private fun SummaryItem(
    value: String,
    label: String,
    unit: String,
    isUrgent: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = if (isUrgent) MaterialTheme.colorScheme.error 
                        else MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = unit,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun RestockItemCard(
    suggestion: RestockItem,
    onAddToCart: () -> Unit
) {
    val statusColor = when {
        suggestion.currentStock <= 0 -> StockOut
        suggestion.currentStock < 10 -> StockOut
        suggestion.currentStock < 50 -> StockLow
        else -> Color(0xFF4CAF50)
    }
    
    Card {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 颜色预览
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    suggestion.mardCode.take(3),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    suggestion.colorName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    Text(
                        "当前: ${suggestion.currentStock}颗",
                        style = MaterialTheme.typography.bodySmall,
                        color = statusColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "建议: ${suggestion.suggestedStock}颗",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = (suggestion.currentStock.toFloat() / suggestion.suggestedStock).coerceIn(0f, 1f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = statusColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    "+${suggestion.neededAmount}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "颗",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EmptyRestockState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color(0xFF4CAF50)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "库存充足！",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "当前库存可以满足所有项目的需求",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 补货项目数据类
 */
data class RestockItem(
    val mardCode: String,
    val colorName: String,
    val currentStock: Int,
    val suggestedStock: Int,
    val neededAmount: Int
)
