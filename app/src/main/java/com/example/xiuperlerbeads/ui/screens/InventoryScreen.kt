package com.example.xiuperlerbeads.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.xiuperlerbeads.data.export.InventoryReportExporter
import com.example.xiuperlerbeads.domain.model.Brand
import com.example.xiuperlerbeads.domain.model.BrandStock
import com.example.xiuperlerbeads.domain.model.ColorSystem
import com.example.xiuperlerbeads.ui.theme.StockEnough
import com.example.xiuperlerbeads.ui.theme.StockLow
import com.example.xiuperlerbeads.ui.theme.StockOut
import com.example.xiuperlerbeads.ui.viewmodel.InventoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel = viewModel(),
    onNavigateToBrandManager: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var showAddStockDialog by remember { mutableStateOf(false) }
    var showEditStockDialog by remember { mutableStateOf<BrandStock?>(null) }
    var showAddBrandDialog by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var showExportMenu by remember { mutableStateOf(false) }
    
    val exporter = remember { InventoryReportExporter(context) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("库存管理") },
                actions = {
                    // Export button
                    IconButton(onClick = { showExportMenu = true }) {
                        Icon(Icons.Default.FileDownload, "导出报表")
                    }
                    DropdownMenu(
                        expanded = showExportMenu,
                        onDismissRequest = { showExportMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("导出为 PDF") },
                            onClick = {
                                showExportMenu = false
                                val uri = exporter.exportInventoryReportToPdf(
                                    state.brands,
                                    state.stocks,
                                    state.historyRecords
                                )
                                if (uri != null) {
                                    Toast.makeText(context, "PDF 已导出: ${uri.lastPathSegment}", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(context, "导出失败", Toast.LENGTH_SHORT).show()
                                }
                            },
                            leadingIcon = {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("导出为 CSV") },
                            onClick = {
                                showExportMenu = false
                                val uri = exporter.exportInventoryReportToCsv(
                                    state.brands,
                                    state.stocks
                                )
                                if (uri != null) {
                                    Toast.makeText(context, "CSV 已导出: ${uri.lastPathSegment}", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(context, "导出失败", Toast.LENGTH_SHORT).show()
                                }
                            },
                            leadingIcon = {
                                Icon(Icons.Default.TableChart, contentDescription = null)
                            }
                        )
                    }
                    
                    // Filter button
                    BadgedBox(
                        badge = {
                            if (state.lowStockOnly || state.selectedColorSystem != null) {
                                Badge()
                            }
                        }
                    ) {
                        IconButton(onClick = { showFilterSheet = true }) {
                            Icon(Icons.Default.FilterList, "筛选")
                        }
                    }
                    // Brand manager button
                    IconButton(onClick = onNavigateToBrandManager) {
                        Icon(Icons.Default.Settings, "品牌管理")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddStockDialog = true }
            ) {
                Icon(Icons.Default.Add, "添加库存")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Stats Summary
            StatsSummaryCard(
                totalColors = state.totalColors,
                totalQuantity = state.totalQuantity,
                lowStockCount = state.lowStockCount,
                outOfStockCount = state.outOfStockCount
            )
            
            // Brand Tabs
            if (state.brands.isNotEmpty()) {
                BrandTabs(
                    brands = state.brands,
                    selectedBrandId = state.selectedBrandId,
                    onBrandSelected = { viewModel.selectBrand(it) },
                    onAddBrand = { showAddBrandDialog = true }
                )
            } else {
                EmptyBrandsPrompt(
                    onAddBrand = { showAddBrandDialog = true }
                )
            }
            
            // Search Bar
            SearchBar(
                query = state.searchQuery,
                onQueryChange = { viewModel.updateSearchQuery(it) },
                lowStockOnly = state.lowStockOnly,
                onToggleLowStock = { viewModel.toggleLowStockOnly() }
            )
            
            // Stock List
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (state.filteredStocks.isEmpty()) {
                EmptyStockPrompt(
                    hasFilters = state.searchQuery.isNotEmpty() || 
                               state.lowStockOnly || 
                               state.selectedColorSystem != null,
                    onClearFilters = {
                        viewModel.updateSearchQuery("")
                        viewModel.toggleLowStockOnly()
                        viewModel.setColorSystem(null)
                    }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = state.filteredStocks,
                        key = { "${it.brandId}_${it.mardCode}" }
                    ) { stock ->
                        StockItemCard(
                            stock = stock,
                            colorInfo = viewModel.getColorInfo(stock.mardCode),
                            onClick = { showEditStockDialog = stock }
                        )
                    }
                }
            }
        }
    }
    
    // Dialogs
    if (showAddStockDialog) {
        AddStockDialog(
            brands = state.brands,
            selectedBrandId = state.selectedBrandId,
            onDismiss = { showAddStockDialog = false },
            onConfirm = { brandId, mardCode, quantity ->
                viewModel.addStock(brandId, mardCode, quantity)
                showAddStockDialog = false
            }
        )
    }
    
    showEditStockDialog?.let { stock ->
        EditStockDialog(
            stock = stock,
            colorInfo = viewModel.getColorInfo(stock.mardCode),
            onDismiss = { showEditStockDialog = null },
            onSave = { newQuantity ->
                viewModel.updateStockQuantity(stock.brandId, stock.mardCode, newQuantity)
                showEditStockDialog = null
            },
            onDelete = {
                // Handle delete
                showEditStockDialog = null
            }
        )
    }
    
    if (showAddBrandDialog) {
        AddBrandDialog(
            onDismiss = { showAddBrandDialog = false },
            onConfirm = { name, system, threshold ->
                viewModel.addBrand(name, system, threshold)
                showAddBrandDialog = false
            }
        )
    }
    
    if (showFilterSheet) {
        FilterBottomSheet(
            selectedSystem = state.selectedColorSystem,
            lowStockOnly = state.lowStockOnly,
            onSystemSelected = { viewModel.setColorSystem(it) },
            onLowStockToggle = { viewModel.toggleLowStockOnly() },
            onDismiss = { showFilterSheet = false }
        )
    }
    
    // Error snackbar
    state.error?.let { error ->
        LaunchedEffect(error) {
            // Show error then clear
            viewModel.clearError()
        }
    }
}

@Composable
private fun StatsSummaryCard(
    totalColors: Int,
    totalQuantity: Int,
    lowStockCount: Int,
    outOfStockCount: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
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
            StatItem(
                icon = Icons.Default.ColorLens,
                value = totalColors.toString(),
                label = "颜色数",
                color = MaterialTheme.colorScheme.primary
            )
            StatItem(
                icon = Icons.Default.Inventory,
                value = totalQuantity.toString(),
                label = "总数量",
                color = MaterialTheme.colorScheme.secondary
            )
            StatItem(
                icon = Icons.Default.Warning,
                value = lowStockCount.toString(),
                label = "低库存",
                color = StockLow
            )
            StatItem(
                icon = Icons.Default.Error,
                value = outOfStockCount.toString(),
                label = "缺货",
                color = StockOut
            )
        }
    }
}

@Composable
private fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier =Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BrandTabs(
    brands: List<Brand>,
    selectedBrandId: String?,
    onBrandSelected: (String) -> Unit,
    onAddBrand: () -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(brands) { brand ->
            FilterChip(
                selected = brand.id == selectedBrandId,
                onClick = { onBrandSelected(brand.id) },
                label = { Text(brand.name) }
            )
        }
        item {
            AssistChip(
                onClick = onAddBrand,
                label = { Text("添加") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }
    }
}

@Composable
private fun EmptyBrandsPrompt(onAddBrand: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Business,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "还没有品牌",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                "添加品牌来管理您的库存",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onAddBrand) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("添加品牌")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    lowStockOnly: Boolean,
    onToggleLowStock: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("搜索色号或颜色名") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Default.Clear, "清除")
                    }
                }
            },
            singleLine = true
        )
        Spacer(modifier = Modifier.width(8.dp))
        FilterChip(
            selected = lowStockOnly,
            onClick = onToggleLowStock,
            label = { Text("低库存") },
            leadingIcon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        )
    }
}

@Composable
private fun EmptyStockPrompt(
    hasFilters: Boolean,
    onClearFilters: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Inventory2,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                if (hasFilters) "没有匹配的库存" else "库存为空",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (hasFilters) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onClearFilters) {
                    Text("清除筛选")
                }
            }
        }
    }
}

@Composable
private fun StockItemCard(
    stock: BrandStock,
    colorInfo: com.example.xiuperlerbeads.domain.model.BeadColor?,
    onClick: () -> Unit
) {
    val statusColor = when {
        stock.available <= 0 -> StockOut
        stock.isLowStock() -> StockLow
        else -> StockEnough
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color preview
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        colorInfo?.toComposeColor() 
                            ?: Color.Gray
                    )
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(8.dp)
                    )
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stock.mardCode,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                if (colorInfo?.colorName?.isNotEmpty() == true) {
                    Text(
                        text = colorInfo.colorName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "库存: ${stock.available}",
                        style = MaterialTheme.typography.bodySmall,
                        color = statusColor
                    )
                    if (stock.used > 0) {
                        Text(
                            text = " (已用: ${stock.used})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Total stock
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${stock.stock}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
                Text(
                    text = "总量",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun FilterBottomSheet(
    selectedSystem: ColorSystem?,
    lowStockOnly: Boolean,
    onSystemSelected: (ColorSystem?) -> Unit,
    onLowStockToggle: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                "筛选",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Low stock toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("仅显示低库存", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "只显示低于阈值的颜色",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = lowStockOnly,
                    onCheckedChange = { onLowStockToggle() }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Color system filter
            Text("色号体系", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(8.dp))
            
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedSystem == null,
                    onClick = { onSystemSelected(null) },
                    label = { Text("全部") }
                )
                ColorSystem.entries.forEach { system ->
                    FilterChip(
                        selected = selectedSystem == system,
                        onClick = { onSystemSelected(system) },
                        label = { Text(system.displayName) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddStockDialog(
    brands: List<Brand>,
    selectedBrandId: String?,
    onDismiss: () -> Unit,
    onConfirm: (brandId: String, mardCode: String, quantity: Int) -> Unit
) {
    var brandId by remember { mutableStateOf(selectedBrandId ?: brands.firstOrNull()?.id ?: "") }
    var mardCode by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加库存") },
        text = {
            Column {
                if (brands.isNotEmpty()) {
                    var expanded by remember { mutableStateOf(false) }
                    val selectedBrand = brands.find { it.id == brandId }
                    
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedBrand?.name ?: "选择品牌",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("品牌") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            brands.forEach { brand ->
                                DropdownMenuItem(
                                    text = { Text(brand.name) },
                                    onClick = {
                                        brandId = brand.id
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                OutlinedTextField(
                    value = mardCode,
                    onValueChange = { mardCode = it.uppercase() },
                    label = { Text("MARD 色号") },
                    placeholder = { Text("例如: A1, B5") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it.filter { c -> c.isDigit() } },
                    label = { Text("数量") },
                    placeholder = { Text("输入数量") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = quantity.toIntOrNull() ?: 0
                    if (mardCode.isNotBlank() && qty > 0 && brandId.isNotBlank()) {
                        onConfirm(brandId, mardCode, qty)
                    }
                },
                enabled = mardCode.isNotBlank() && (quantity.toIntOrNull() ?: 0) > 0 && brandId.isNotBlank()
            ) {
                Text("添加")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun EditStockDialog(
    stock: BrandStock,
    colorInfo: com.example.xiuperlerbeads.domain.model.BeadColor?,
    onDismiss: () -> Unit,
    onSave: (Int) -> Unit,
    onDelete: () -> Unit
) {
    var quantity by remember { mutableStateOf(stock.stock.toString()) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(colorInfo?.toComposeColor() ?: Color.Gray)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stock.mardCode)
            }
        },
        text = {
            Column {
                if (colorInfo?.colorName?.isNotEmpty() == true) {
                    Text(
                        text = colorInfo.colorName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it.filter { c -> c.isDigit() } },
                    label = { Text("库存数量") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row {
                    Text(
                        "可用: ${stock.available}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (stock.available < 100) StockLow else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        "已用: ${stock.used}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    quantity.toIntOrNull()?.let { onSave(it) }
                }
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            Row {
                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("删除")
                }
                TextButton(onClick = onDismiss) {
                    Text("取消")
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun AddBrandDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, system: ColorSystem, threshold: Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedSystem by remember { mutableStateOf(ColorSystem.MARD) }
    var threshold by remember { mutableStateOf("100") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加品牌") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("品牌名称") },
                    placeholder = { Text("例如: 我的仓库") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("色号体系", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ColorSystem.entries.forEach { system ->
                        FilterChip(
                            selected = selectedSystem == system,
                            onClick = { selectedSystem = system },
                            label = { Text(system.displayName) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = threshold,
                    onValueChange = { threshold = it.filter { c -> c.isDigit() } },
                    label = { Text("低库存阈值") },
                    placeholder = { Text("100") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val th = threshold.toIntOrNull() ?: 100
                    if (name.isNotBlank()) {
                        onConfirm(name, selectedSystem, th)
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("添加")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
