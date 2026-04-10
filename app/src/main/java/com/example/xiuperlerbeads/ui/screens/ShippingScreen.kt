package com.example.xiuperlerbeads.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.xiuperlerbeads.domain.model.Brand
import com.example.xiuperlerbeads.domain.model.PurchaseItem
import com.example.xiuperlerbeads.domain.model.PurchaseRecord
import com.example.xiuperlerbeads.ui.viewmodel.InventoryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 购买/物流跟踪页面
 * 记录已购买但尚未到货的豆子订单，到货后一键入库
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShippingScreen(
    onNavigateBack: () -> Unit,
    inventoryViewModel: InventoryViewModel = viewModel()
) {
    val state by inventoryViewModel.state.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var confirmRecord by remember { mutableStateOf<PurchaseRecord?>(null) }
    var deleteRecord by remember { mutableStateOf<PurchaseRecord?>(null) }

    LaunchedEffect(Unit) { inventoryViewModel.loadData() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("运输中订单", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "添加订单")
            }
        }
    ) { paddingValues ->
        if (state.purchaseRecords.isEmpty()) {
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
                        Icons.Default.LocalShipping,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                    Text(
                        "暂无运输中订单",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "点击右下角添加购买记录",
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
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        "共 ${state.purchaseRecords.size} 笔订单待到货",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                items(state.purchaseRecords, key = { it.id }) { record ->
                    val brand = state.brands.find { it.id == record.brandId }
                    PurchaseRecordCard(
                        record = record,
                        brand = brand,
                        onConfirmArrival = { confirmRecord = record },
                        onDelete = { deleteRecord = record }
                    )
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }

    // 添加订单对话框
    if (showAddDialog) {
        AddPurchaseDialog(
            brands = state.brands,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, brandId, items, note ->
                inventoryViewModel.addPurchaseRecord(name, brandId, items, note)
                showAddDialog = false
            }
        )
    }

    // 确认到货对话框
    confirmRecord?.let { record ->
        AlertDialog(
            onDismissRequest = { confirmRecord = null },
            icon = { Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("确认到货") },
            text = {
                val totalItems = record.items.sumOf { it.quantity }
                Text("确认「${record.name}」已到货？\n\n共 ${record.items.size} 种颜色，${totalItems} 颗豆子将自动入库。")
            },
            confirmButton = {
                Button(onClick = {
                    inventoryViewModel.completePurchaseRecord(record.id)
                    confirmRecord = null
                }) { Text("确认入库") }
            },
            dismissButton = {
                TextButton(onClick = { confirmRecord = null }) { Text("取消") }
            }
        )
    }

    // 删除确认对话框
    deleteRecord?.let { record ->
        AlertDialog(
            onDismissRequest = { deleteRecord = null },
            icon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("删除订单") },
            text = { Text("确定要删除订单「${record.name}」吗？此操作不会影响库存。") },
            confirmButton = {
                Button(
                    onClick = {
                        inventoryViewModel.deletePurchaseRecord(record.id)
                        deleteRecord = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("删除") }
            },
            dismissButton = {
                TextButton(onClick = { deleteRecord = null }) { Text("取消") }
            }
        )
    }
}

// ── 订单卡片 ──────────────────────────────────────────────────────────────────

@Composable
private fun PurchaseRecordCard(
    record: PurchaseRecord,
    brand: Brand?,
    onConfirmArrival: () -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = remember(record.date) {
        SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(Date(record.date))
    }
    val totalQty = record.items.sumOf { it.quantity }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 标题行
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.LocalShipping,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(record.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        buildString {
                            brand?.let { append(it.name) }
                            append(" · $dateStr")
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "删除", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(Modifier.height(8.dp))

            // 条目概览
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    if (record.items.size <= 4) {
                        record.items.forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(item.colorCode, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                                Text("${item.quantity} 颗", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    } else {
                        // 超过4条时仅显示摘要
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${record.items.size} 种颜色", style = MaterialTheme.typography.bodySmall)
                            Text("共 $totalQty 颗", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // 备注
            record.note?.takeIf { it.isNotBlank() }?.let { note ->
                Spacer(Modifier.height(6.dp))
                Text(note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(Modifier.height(10.dp))

            // 到货按钮
            Button(
                onClick = onConfirmArrival,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("确认到货 · 入库 $totalQty 颗")
            }
        }
    }
}

// ── 添加订单对话框 ────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddPurchaseDialog(
    brands: List<Brand>,
    onDismiss: () -> Unit,
    onConfirm: (name: String, brandId: String, items: List<PurchaseItem>, note: String?) -> Unit
) {
    var orderName by remember { mutableStateOf("") }
    var selectedBrandId by remember { mutableStateOf(brands.firstOrNull()?.id ?: "") }
    var pasteText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var brandMenuExpanded by remember { mutableStateOf(false) }

    // 解析粘贴文本 -> PurchaseItem 列表
    // 格式：A01 500 A02 300  或每行 "A01 500颗"
    val parsedItems = remember(pasteText) {
        parsePurchaseText(pasteText)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新增采购订单") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // 订单名称
                OutlinedTextField(
                    value = orderName,
                    onValueChange = { orderName = it },
                    label = { Text("订单名称") },
                    placeholder = { Text("如：4月补货") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // 品牌选择
                ExposedDropdownMenuBox(
                    expanded = brandMenuExpanded,
                    onExpandedChange = { brandMenuExpanded = it }
                ) {
                    OutlinedTextField(
                        value = brands.find { it.id == selectedBrandId }?.name ?: "选择品牌",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("入库品牌") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = brandMenuExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(expanded = brandMenuExpanded, onDismissRequest = { brandMenuExpanded = false }) {
                        brands.forEach { brand ->
                            DropdownMenuItem(
                                text = { Text(brand.name) },
                                onClick = { selectedBrandId = brand.id; brandMenuExpanded = false },
                                leadingIcon = if (brand.id == selectedBrandId) {
                                    { Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary) }
                                } else null
                            )
                        }
                    }
                }

                // 粘贴文本输入
                OutlinedTextField(
                    value = pasteText,
                    onValueChange = { pasteText = it },
                    label = { Text("色号清单") },
                    placeholder = { Text("A01 500\nA02 300\n（每行：色号 数量）") },
                    minLines = 3,
                    maxLines = 8,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // 解析预览
                if (parsedItems.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            "已识别 ${parsedItems.size} 种颜色，共 ${parsedItems.sumOf { it.quantity }} 颗",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                // 备注
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("备注（可选）") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (orderName.isNotBlank() && selectedBrandId.isNotEmpty() && parsedItems.isNotEmpty()) {
                        onConfirm(orderName, selectedBrandId, parsedItems, note.takeIf { it.isNotBlank() })
                    }
                },
                enabled = orderName.isNotBlank() && selectedBrandId.isNotEmpty() && parsedItems.isNotEmpty()
            ) { Text("添加") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

// ── 工具函数 ──────────────────────────────────────────────────────────────────

/**
 * 解析采购文本，支持格式：
 * - "A01 500" 或 "A01 500颗"（每行一条）
 * - "A01 500 A02 300"（空格分隔，交替色号和数量）
 */
private fun parsePurchaseText(text: String): List<PurchaseItem> {
    if (text.isBlank()) return emptyList()
    val items = mutableListOf<PurchaseItem>()

    text.lines().forEach { line ->
        val clean = line.trim().replace("颗", "").replace("，", " ").replace(",", " ")
        val parts = clean.split(Regex("\\s+")).filter { it.isNotBlank() }

        var i = 0
        while (i < parts.size - 1) {
            val code = parts[i].uppercase()
            val qty = parts[i + 1].toIntOrNull()
            if (qty != null && qty > 0 && code.matches(Regex("[A-Za-z][A-Za-z0-9]*"))) {
                items.add(PurchaseItem(colorCode = code, quantity = qty))
                i += 2
            } else {
                i++
            }
        }
    }

    return items.distinctBy { it.colorCode }
}
