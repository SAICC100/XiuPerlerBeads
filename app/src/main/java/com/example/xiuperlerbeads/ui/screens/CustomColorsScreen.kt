package com.example.xiuperlerbeads.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.xiuperlerbeads.domain.model.CustomColor
import com.example.xiuperlerbeads.ui.viewmodel.InventoryViewModel

/**
 * 自定义色号管理页
 * 在指定品牌下添加、编辑、删除自定义色号
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomColorsScreen(
    brandId: String,
    onNavigateBack: () -> Unit,
    inventoryViewModel: InventoryViewModel = viewModel()
) {
    val state by inventoryViewModel.state.collectAsState()
    val brandName = state.brands.find { it.id == brandId }?.name ?: "品牌"

    // 只展示该品牌的自定义色号（mardCode 以 # 开头）
    val customColors = state.customColors

    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingColor by remember { mutableStateOf<CustomColor?>(null) }
    var deletingColor by remember { mutableStateOf<CustomColor?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredColors = remember(customColors, searchQuery) {
        if (searchQuery.isBlank()) customColors
        else customColors.filter {
            it.colorCode.contains(searchQuery, ignoreCase = true) ||
            it.colorName.contains(searchQuery, ignoreCase = true)
        }
    }

    LaunchedEffect(brandId) {
        inventoryViewModel.loadData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("自定义色号", fontWeight = FontWeight.Bold)
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
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingColor = null
                    showAddEditDialog = true
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加自定义色号")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 搜索框
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("搜索色号或名称") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "清除")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (filteredColors.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Palette,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                        Text(
                            if (searchQuery.isBlank()) "暂无自定义色号" else "未找到匹配的色号",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (searchQuery.isBlank()) {
                            Text(
                                "点击右下角 + 添加自定义色号",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(
                        start = 16.dp, end = 16.dp,
                        top = 4.dp, bottom = 88.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    item {
                        Text(
                            "共 ${filteredColors.size} 个自定义色号",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    items(filteredColors, key = { it.id }) { color ->
                        CustomColorItem(
                            customColor = color,
                            onEdit = {
                                editingColor = color
                                showAddEditDialog = true
                            },
                            onDelete = { deletingColor = color }
                        )
                    }
                }
            }
        }
    }

    // 添加/编辑对话框
    if (showAddEditDialog) {
        CustomColorEditDialog(
            initialColor = editingColor,
            onDismiss = { showAddEditDialog = false },
            onConfirm = { code, hex, name ->
                if (editingColor == null) {
                    inventoryViewModel.addCustomColor(code, hex, name)
                } else {
                    inventoryViewModel.updateCustomColor(editingColor!!.id, code, hex, name)
                }
                showAddEditDialog = false
            }
        )
    }

    // 删除确认对话框
    if (deletingColor != null) {
        AlertDialog(
            onDismissRequest = { deletingColor = null },
            title = { Text("删除色号") },
            text = {
                Text("确认删除色号「${deletingColor!!.colorCode}」？\n删除后该色号在所有品牌中的库存记录也会一并删除。")
            },
            confirmButton = {
                Button(
                    onClick = {
                        inventoryViewModel.deleteCustomColor(deletingColor!!.id)
                        deletingColor = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingColor = null }) {
                    Text("取消")
                }
            }
        )
    }

    // 错误提示
    state.error?.let { error ->
        LaunchedEffect(error) {
            inventoryViewModel.clearError()
        }
    }
}

@Composable
private fun CustomColorItem(
    customColor: CustomColor,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val hex = customColor.colorHex.removePrefix("#")
    val composeColor = try {
        val r = hex.substring(0, 2).toInt(16)
        val g = hex.substring(2, 4).toInt(16)
        val b = hex.substring(4, 6).toInt(16)
        Color(r, g, b)
    } catch (e: Exception) {
        null
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 色块
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(composeColor ?: MaterialTheme.colorScheme.surfaceVariant)
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        CircleShape
                    )
            )

            // 色号信息
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    customColor.colorCode,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (customColor.colorName.isNotEmpty()) {
                        Text(
                            customColor.colorName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        "#${customColor.colorHex.uppercase()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }

            // 操作按钮
            IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "编辑",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "删除",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomColorEditDialog(
    initialColor: CustomColor?,
    onDismiss: () -> Unit,
    onConfirm: (code: String, hex: String, name: String) -> Unit
) {
    val isEdit = initialColor != null
    var colorCode by remember { mutableStateOf(initialColor?.colorCode ?: "") }
    var colorHex by remember { mutableStateOf(initialColor?.colorHex?.removePrefix("#") ?: "") }
    var colorName by remember { mutableStateOf(initialColor?.colorName ?: "") }
    var hexError by remember { mutableStateOf(false) }

    val previewColor = remember(colorHex) {
        try {
            val hex = colorHex.removePrefix("#").padStart(6, '0').take(6)
            val r = hex.substring(0, 2).toInt(16)
            val g = hex.substring(2, 4).toInt(16)
            val b = hex.substring(4, 6).toInt(16)
            Color(r, g, b)
        } catch (e: Exception) {
            null
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEdit) "编辑自定义色号" else "添加自定义色号") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // 色号代码
                OutlinedTextField(
                    value = colorCode,
                    onValueChange = { colorCode = it.uppercase() },
                    label = { Text("色号代码 *") },
                    placeholder = { Text("如 C001") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    modifier = Modifier.fillMaxWidth()
                )

                // 十六进制颜色值 + 预览
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = colorHex,
                        onValueChange = { v ->
                            val clean = v.removePrefix("#").filter { it.isLetterOrDigit() }.take(6).uppercase()
                            colorHex = clean
                            hexError = clean.length == 6 && try {
                                clean.toLong(16)
                                false
                            } catch (e: Exception) { true }
                        },
                        label = { Text("HEX 色值 *") },
                        placeholder = { Text("如 FF5733") },
                        prefix = { Text("#") },
                        singleLine = true,
                        isError = hexError,
                        supportingText = if (hexError) ({ Text("无效的十六进制颜色值") }) else null,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                        modifier = Modifier.weight(1f)
                    )
                    // 颜色预览
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(previewColor ?: MaterialTheme.colorScheme.surfaceVariant)
                            .border(
                                2.dp,
                                MaterialTheme.colorScheme.outline,
                                CircleShape
                            )
                    )
                }

                // 颜色名称（可选）
                OutlinedTextField(
                    value = colorName,
                    onValueChange = { colorName = it },
                    label = { Text("颜色名称（选填）") },
                    placeholder = { Text("如 珊瑚红") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (colorCode.isNotBlank() && colorHex.length == 6 && !hexError) {
                        onConfirm(colorCode.trim(), colorHex.trim(), colorName.trim())
                    }
                },
                enabled = colorCode.isNotBlank() && colorHex.length == 6 && !hexError
            ) {
                Text(if (isEdit) "保存" else "添加")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}
