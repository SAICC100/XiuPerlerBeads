package com.example.xiuperlerbeads.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.xiuperlerbeads.domain.model.Brand
import com.example.xiuperlerbeads.domain.model.ColorSystem
import com.example.xiuperlerbeads.ui.viewmodel.InventoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrandManagerScreen(
    viewModel: InventoryViewModel = viewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToHiddenColors: (brandId: String) -> Unit = {},
    onNavigateToCustomColors: (brandId: String) -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingBrand by remember { mutableStateOf<Brand?>(null) }
    var deletingBrand by remember { mutableStateOf<Brand?>(null) }
    // 合并：选中的源品牌（要被合并走的品牌）
    var mergingBrand by remember { mutableStateOf<Brand?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("品牌管理") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, "添加品牌")
            }
        }
    ) { paddingValues ->
        if (state.brands.isEmpty()) {
            EmptyBrandList(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                onAddBrand = { showAddDialog = true }
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = state.brands,
                    key = { it.id }
                ) { brand ->
                    val hiddenCount = state.stocks.count { it.brandId == brand.id && it.isHidden }
                    val customCount = state.customColors.size
                    BrandCard(
                        brand = brand,
                        stockCount = state.stocks.count { it.brandId == brand.id && !it.isHidden },
                        hiddenCount = hiddenCount,
                        customColorCount = customCount,
                        canMerge = state.brands.size >= 2,
                        onEdit = { editingBrand = brand },
                        onDelete = { deletingBrand = brand },
                        onMerge = { mergingBrand = brand },
                        onManageHidden = if (hiddenCount > 0) {
                            { onNavigateToHiddenColors(brand.id) }
                        } else null,
                        onManageCustomColors = { onNavigateToCustomColors(brand.id) }
                    )
                }
            }
        }
    }

    // Add Brand Dialog
    if (showAddDialog) {
        BrandDialog(
            brand = null,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, system, threshold ->
                viewModel.addBrand(name, system, threshold)
                showAddDialog = false
            }
        )
    }

    // Edit Brand Dialog
    editingBrand?.let { brand ->
        BrandDialog(
            brand = brand,
            onDismiss = { editingBrand = null },
            onConfirm = { name, system, threshold ->
                viewModel.updateBrand(
                    brand.copy(
                        name = name,
                        colorSystem = system,
                        lowStockThreshold = threshold
                    )
                )
                editingBrand = null
            }
        )
    }

    // Delete Confirmation Dialog
    deletingBrand?.let { brand ->
        AlertDialog(
            onDismissRequest = { deletingBrand = null },
            icon = { Icon(Icons.Default.Warning, null) },
            title = { Text("删除品牌") },
            text = {
                Text("确定要删除品牌「${brand.name}」吗？\n\n这不会删除该品牌下的库存记录。")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteBrand(brand.id)
                        deletingBrand = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingBrand = null }) {
                    Text("取消")
                }
            }
        )
    }

    // Merge Brand Dialog
    mergingBrand?.let { source ->
        MergeBrandDialog(
            sourceBrand = source,
            otherBrands = state.brands.filter { it.id != source.id },
            onDismiss = { mergingBrand = null },
            onConfirm = { targetBrandId ->
                viewModel.mergeBrand(source.id, targetBrandId)
                mergingBrand = null
            }
        )
    }
}

@Composable
private fun EmptyBrandList(
    modifier: Modifier = Modifier,
    onAddBrand: () -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Business,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "还没有品牌",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "添加您的第一个品牌来开始管理库存",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onAddBrand) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("添加品牌")
        }
    }
}

@Composable
private fun BrandCard(
    brand: Brand,
    stockCount: Int,
    hiddenCount: Int = 0,
    customColorCount: Int = 0,
    canMerge: Boolean = false,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMerge: () -> Unit = {},
    onManageHidden: (() -> Unit)? = null,
    onManageCustomColors: (() -> Unit)? = null
) {
    val brandColor = when (brand.colorSystem) {
        ColorSystem.MARD -> MaterialTheme.colorScheme.primary
        ColorSystem.COCO -> MaterialTheme.colorScheme.secondary
        ColorSystem.MANMAN -> MaterialTheme.colorScheme.tertiary
        ColorSystem.KAKA -> MaterialTheme.colorScheme.primary
        ColorSystem.PANPAN -> MaterialTheme.colorScheme.secondary
        ColorSystem.MIXIAOWO -> MaterialTheme.colorScheme.tertiary
    }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Brand indicator
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = brandColor.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = brand.name.take(2),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = brandColor
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Brand info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = brand.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AssistChip(
                            onClick = {},
                            label = { Text(brand.colorSystem.displayName) },
                            modifier = Modifier.height(24.dp)
                        )
                        Text(
                            text = "$stockCount 种颜色",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "低库存阈值: ${brand.lowStockThreshold}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Actions
                Column {
                    IconButton(onClick = onEdit) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "编辑",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "删除",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // 自定义色号入口（始终显示）
            if (onManageCustomColors != null) {
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(thickness = 0.5.dp)
                Spacer(Modifier.height(4.dp))
                TextButton(
                    onClick = onManageCustomColors,
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 0.dp, vertical = 4.dp)
                ) {
                    Icon(
                        Icons.Default.Palette,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        if (customColorCount > 0) "自定义色号（$customColorCount 个）" else "自定义色号",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Spacer(Modifier.weight(1f))
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // 隐藏色号入口（有隐藏色号时显示）
            if (hiddenCount > 0 && onManageHidden != null) {
                TextButton(
                    onClick = onManageHidden,
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 0.dp, vertical = 4.dp)
                ) {
                    Icon(
                        Icons.Default.VisibilityOff,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "管理 $hiddenCount 个隐藏色号",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Spacer(Modifier.weight(1f))
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // 合并品牌入口（有多个品牌时显示）
            if (canMerge) {
                TextButton(
                    onClick = onMerge,
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 0.dp, vertical = 4.dp)
                ) {
                    Icon(
                        Icons.Default.MergeType,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "合并到其他品牌",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.weight(1f))
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * 品牌合并对话框：选择目标品牌，确认后执行合并
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MergeBrandDialog(
    sourceBrand: Brand,
    otherBrands: List<Brand>,
    onDismiss: () -> Unit,
    onConfirm: (targetBrandId: String) -> Unit
) {
    var selectedTargetId by remember { mutableStateOf(otherBrands.firstOrNull()?.id ?: "") }
    var expanded by remember { mutableStateOf(false) }
    val selectedTarget = otherBrands.find { it.id == selectedTargetId }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.MergeType, contentDescription = null) },
        title = { Text("合并品牌") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "将「${sourceBrand.name}」的所有库存数据合并到目标品牌，合并后「${sourceBrand.name}」将被删除。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // 目标品牌选择器
                Text("合并到：", style = MaterialTheme.typography.labelMedium)
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedTarget?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("目标品牌") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        otherBrands.forEach { brand ->
                            DropdownMenuItem(
                                text = { Text(brand.name) },
                                onClick = {
                                    selectedTargetId = brand.id
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                // 警告提示
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            "此操作不可撤销。同色号库存将累加，源品牌「${sourceBrand.name}」将被删除。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedTargetId) },
                enabled = selectedTargetId.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("确认合并")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BrandDialog(
    brand: Brand?,
    onDismiss: () -> Unit,
    onConfirm: (name: String, system: ColorSystem, threshold: Int) -> Unit
) {
    var name by remember { mutableStateOf(brand?.name ?: "") }
    var selectedSystem by remember { mutableStateOf(brand?.colorSystem ?: ColorSystem.MARD) }
    var threshold by remember { mutableStateOf(brand?.lowStockThreshold?.toString() ?: "100") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (brand == null) "添加品牌" else "编辑品牌") },
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

                Text(
                    "色号体系",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ColorSystem.entries.chunked(3).forEach { row ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            row.forEach { system ->
                                FilterChip(
                                    selected = selectedSystem == system,
                                    onClick = { selectedSystem = system },
                                    label = { Text(system.displayName) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            repeat(3 - row.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = threshold,
                    onValueChange = { threshold = it.filter { c -> c.isDigit() } },
                    label = { Text("低库存阈值") },
                    placeholder = { Text("100") },
                    supportingText = { Text("低于此数量将显示为低库存") },
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
                Text(if (brand == null) "添加" else "保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
