package com.example.xiuperlerbeads.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.xiuperlerbeads.domain.model.BeadColorManager
import com.example.xiuperlerbeads.domain.model.BrandStock
import com.example.xiuperlerbeads.ui.viewmodel.InventoryViewModel

/**
 * 隐藏色号管理页
 * 查看并恢复当前品牌下被隐藏的色号
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HiddenColorsScreen(
    brandId: String,
    onNavigateBack: () -> Unit,
    inventoryViewModel: InventoryViewModel = viewModel()
) {
    val state by inventoryViewModel.state.collectAsState()
    val hiddenStocks = remember(state.stocks, brandId) {
        inventoryViewModel.getHiddenStocks(brandId)
    }
    val brandName = state.brands.find { it.id == brandId }?.name ?: "品牌"

    // 批量选择状态
    var selectedMardCodes by remember { mutableStateOf(setOf<String>()) }
    var isSelectMode by remember { mutableStateOf(false) }

    // 恢复库存对话框
    var showRestoreDialog by remember { mutableStateOf(false) }
    var restoreSingleStock by remember { mutableStateOf<BrandStock?>(null) }
    var restoreQuantityInput by remember { mutableStateOf("1000") }

    LaunchedEffect(brandId) {
        inventoryViewModel.loadData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("隐藏色号", fontWeight = FontWeight.Bold)
                        Text(
                            brandName,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (hiddenStocks.isNotEmpty()) {
                        if (isSelectMode) {
                            TextButton(
                                onClick = {
                                    selectedMardCodes = if (selectedMardCodes.size == hiddenStocks.size) {
                                        emptySet()
                                    } else {
                                        hiddenStocks.map { it.mardCode }.toSet()
                                    }
                                }
                            ) {
                                Text(if (selectedMardCodes.size == hiddenStocks.size) "取消全选" else "全选")
                            }
                            IconButton(onClick = {
                                isSelectMode = false
                                selectedMardCodes = emptySet()
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "退出选择")
                            }
                        } else {
                            IconButton(onClick = { isSelectMode = true }) {
                                Icon(Icons.Default.Checklist, contentDescription = "批量选择")
                            }
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (isSelectMode && selectedMardCodes.isNotEmpty()) {
                Surface(
                    tonalElevation = 3.dp,
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                // 批量恢复，使用默认库存 1000
                                selectedMardCodes.forEach { mardCode ->
                                    inventoryViewModel.unhideColor(brandId, mardCode, 1000)
                                }
                                isSelectMode = false
                                selectedMardCodes = emptySet()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.VisibilityOff, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("恢复 ${selectedMardCodes.size} 个（默认1000颗）")
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        if (hiddenStocks.isEmpty()) {
            // 空状态
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Visibility,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                    Text(
                        "没有隐藏的色号",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "在库存页面可以隐藏不需要的色号",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(
                    start = 16.dp, end = 16.dp,
                    top = 8.dp,
                    bottom = if (isSelectMode && selectedMardCodes.isNotEmpty()) 80.dp else 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                item {
                    Text(
                        "共 ${hiddenStocks.size} 个隐藏色号",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                items(hiddenStocks, key = { it.mardCode }) { stock ->
                    val beadColor = BeadColorManager.findByMardCode(stock.mardCode)
                    val composeColor = beadColor?.toComposeColor()
                    val isSelected = stock.mardCode in selectedMardCodes

                    HiddenColorItem(
                        stock = stock,
                        colorName = beadColor?.colorName ?: "",
                        composeColor = composeColor,
                        isSelectMode = isSelectMode,
                        isSelected = isSelected,
                        onToggleSelect = {
                            selectedMardCodes = if (isSelected) {
                                selectedMardCodes - stock.mardCode
                            } else {
                                selectedMardCodes + stock.mardCode
                            }
                        },
                        onRestore = {
                            restoreSingleStock = stock
                            restoreQuantityInput = "1000"
                            showRestoreDialog = true
                        }
                    )
                }
            }
        }
    }

    // 恢复库存对话框
    if (showRestoreDialog && restoreSingleStock != null) {
        val stock = restoreSingleStock!!
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = { Text("恢复色号 ${stock.mardCode}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "恢复后该色号将重新出现在库存列表中。",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedTextField(
                        value = restoreQuantityInput,
                        onValueChange = { v -> restoreQuantityInput = v.filter { it.isDigit() } },
                        label = { Text("初始库存（颗）") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val qty = restoreQuantityInput.toIntOrNull()?.coerceAtLeast(0) ?: 1000
                    inventoryViewModel.unhideColor(brandId, stock.mardCode, qty)
                    showRestoreDialog = false
                    restoreSingleStock = null
                }) {
                    Text("恢复")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showRestoreDialog = false
                    restoreSingleStock = null
                }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun HiddenColorItem(
    stock: BrandStock,
    colorName: String,
    composeColor: androidx.compose.ui.graphics.Color?,
    isSelectMode: Boolean,
    isSelected: Boolean,
    onToggleSelect: () -> Unit,
    onRestore: () -> Unit
) {
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = if (isSelected) CardDefaults.outlinedCardBorder() else null,
        onClick = if (isSelectMode) onToggleSelect else onRestore
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 选择框 / 色块
            if (isSelectMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onToggleSelect() }
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(composeColor ?: MaterialTheme.colorScheme.surfaceVariant)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            CircleShape
                        )
                )
            }

            // 色号信息
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stock.mardCode,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                if (colorName.isNotEmpty()) {
                    Text(
                        colorName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 隐藏标签
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.errorContainer
            ) {
                Text(
                    "已隐藏",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }

            // 恢复按钮（非选择模式时显示）
            if (!isSelectMode) {
                IconButton(onClick = onRestore, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.Visibility,
                        contentDescription = "恢复",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
