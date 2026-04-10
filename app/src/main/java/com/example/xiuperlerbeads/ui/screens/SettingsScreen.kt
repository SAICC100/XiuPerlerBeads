package com.example.xiuperlerbeads.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.xiuperlerbeads.XiuPerlerBeadsApp
import com.example.xiuperlerbeads.domain.model.ImportPreview
import com.example.xiuperlerbeads.ui.viewmodel.InventoryViewModel
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToAISettings: () -> Unit = {},
    inventoryViewModel: InventoryViewModel = viewModel()
) {
    val context = LocalContext.current
    var showAboutDialog by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }
    var showExportSuccess by remember { mutableStateOf(false) }
    var importPreview by remember { mutableStateOf<ImportPreview?>(null) }
    var pendingImportJson by remember { mutableStateOf<String?>(null) }

    // 文件选择：导出
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            try {
                context.contentResolver.openOutputStream(it)?.use { outputStream ->
                    OutputStreamWriter(outputStream).use { writer ->
                        val app = context.applicationContext as? XiuPerlerBeadsApp
                        app?.let { application ->
                            writer.write(application.repository.exportAllData())
                            showExportSuccess = true
                        }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "导出失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 文件选择：导入（读取 JSON 后先展示预览）
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            try {
                context.contentResolver.openInputStream(it)?.use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream)).use { reader ->
                        val json = reader.readText()
                        val app = context.applicationContext as? XiuPerlerBeadsApp
                        app?.let { application ->
                            val preview = application.repository.previewImport(json)
                            if (preview != null) {
                                pendingImportJson = json
                                importPreview = preview
                            } else {
                                Toast.makeText(context, "导入失败: 数据格式错误", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "导入失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(showExportSuccess) {
        if (showExportSuccess) {
            Toast.makeText(context, "数据导出成功", Toast.LENGTH_SHORT).show()
            showExportSuccess = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            // AI 设置
            item { SettingsSection(title = "AI 功能") }
            item {
                SettingsItem(
                    icon = Icons.Default.AutoAwesome,
                    title = "API 设置",
                    subtitle = "配置 AI 识别 API",
                    onClick = onNavigateToAISettings
                )
            }

            // 数据管理
            item { SettingsSection(title = "数据管理") }
            item {
                SettingsItem(
                    icon = Icons.Default.Upload,
                    title = "导出数据",
                    subtitle = "备份所有数据到 JSON 文件",
                    onClick = {
                        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                        val fileName = "xiuperlerbeads_backup_${dateFormat.format(Date())}.json"
                        exportLauncher.launch(fileName)
                    }
                )
            }
            item {
                SettingsItem(
                    icon = Icons.Default.Download,
                    title = "导入数据",
                    subtitle = "从 JSON 文件恢复或合并数据",
                    onClick = {
                        importLauncher.launch(arrayOf("application/json"))
                    }
                )
            }
            item {
                SettingsItem(
                    icon = Icons.Default.Delete,
                    title = "清除数据",
                    subtitle = "清除所有本地数据",
                    onClick = { showClearDataDialog = true },
                    danger = true
                )
            }

            // 关于
            item { SettingsSection(title = "关于") }
            item {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "关于秀拼豆",
                    subtitle = "版本 1.0.0",
                    onClick = { showAboutDialog = true }
                )
            }
            item {
                SettingsItem(
                    icon = Icons.Default.Code,
                    title = "开源许可",
                    subtitle = "查看第三方库许可",
                    onClick = { }
                )
            }
        }
    }

    // ── 导入预览对话框 ──────────────────────────────────────────────────────
    importPreview?.let { preview ->
        ImportPreviewDialog(
            preview = preview,
            onDismiss = {
                importPreview = null
                pendingImportJson = null
            },
            onOverwrite = {
                val json = pendingImportJson ?: return@ImportPreviewDialog
                val app = context.applicationContext as? XiuPerlerBeadsApp
                app?.let { application ->
                    val success = application.repository.importData(json)
                    if (success) {
                        inventoryViewModel.loadData()
                        Toast.makeText(context, "数据已覆盖导入成功", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "导入失败", Toast.LENGTH_SHORT).show()
                    }
                }
                importPreview = null
                pendingImportJson = null
            },
            onMerge = {
                val json = pendingImportJson ?: return@ImportPreviewDialog
                val app = context.applicationContext as? XiuPerlerBeadsApp
                app?.let { application ->
                    val success = application.repository.importDataMerge(json)
                    if (success) {
                        inventoryViewModel.loadData()
                        Toast.makeText(context, "数据已合并导入成功", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "合并导入失败", Toast.LENGTH_SHORT).show()
                    }
                }
                importPreview = null
                pendingImportJson = null
            }
        )
    }

    // 清除数据确认对话框
    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            icon = { Icon(Icons.Default.Warning, null) },
            title = { Text("清除所有数据") },
            text = { Text("确定要清除所有数据吗？此操作不可恢复。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearDataDialog = false
                        val app = context.applicationContext as? XiuPerlerBeadsApp
                        app?.let { application ->
                            application.repository.clearAllData()
                            Toast.makeText(context, "数据已清除，请重启应用", Toast.LENGTH_LONG).show()
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("清除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) { Text("取消") }
            }
        )
    }

    // 关于对话框
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            icon = { Icon(Icons.Default.GridOn, null) },
            title = { Text("秀拼豆") },
            text = {
                Column {
                    Text("版本: 1.0.0")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("秀拼豆是一款专为拼豆爱好者设计的创作与管理应用。")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("支持手绘画布、图片转像素、多品牌库存管理等功能。")
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) { Text("确定") }
            }
        )
    }
}

/**
 * 导入预览与冲突处理对话框
 */
@Composable
private fun ImportPreviewDialog(
    preview: ImportPreview,
    onDismiss: () -> Unit,
    onOverwrite: () -> Unit,
    onMerge: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Download, contentDescription = null) },
        title = { Text("导入预览") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // 导入文件统计
                Text("导入文件包含：", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        PreviewRow("品牌", preview.brandsCount, preview.existingBrandsCount)
                        PreviewRow("库存记录", preview.stocksCount, preview.existingStocksCount)
                        PreviewRow("项目", preview.projectsCount, preview.existingProjectsCount)
                        if (preview.customColorsCount > 0)
                            PreviewRow("自定义色号", preview.customColorsCount, 0)
                        if (preview.purchaseRecordsCount > 0)
                            PreviewRow("采购记录", preview.purchaseRecordsCount, 0)
                    }
                }

                // 冲突提示
                if (preview.hasConflicts) {
                    Spacer(Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.errorContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    "发现 ${preview.conflictBrandNames.size} 个品牌名称冲突",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    preview.conflictBrandNames.joinToString("、"),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    "选「合并」时，冲突品牌将被跳过",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))
                Text(
                    "请选择导入模式：",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = onMerge,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.MergeType, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("合并（保留现有数据）")
                }
                OutlinedButton(
                    onClick = onOverwrite,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("覆盖（清空现有数据）")
                }
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("取消")
                }
            }
        },
        dismissButton = null
    )
}

@Composable
private fun PreviewRow(label: String, incoming: Int, existing: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall)
        Text(
            if (existing > 0) "$incoming 条（当前 $existing 条）"
            else "$incoming 条",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SettingsSection(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
private fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    danger: Boolean = false
) {
    ListItem(
        headlineContent = {
            Text(
                text = title,
                color = if (danger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
        },
        supportingContent = {
            Text(
                text = subtitle,
                color = if (danger) MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                        else MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (danger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        },
        trailingContent = {
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        modifier = Modifier.clickable { onClick() }
    )
}

/**
 * AI 设置页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AISettingsScreen(
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    var selectedProvider by remember { mutableStateOf("OpenAI") }
    var apiKey by remember { mutableStateOf("") }

    val providers = listOf(
        "OpenAI" to "GPT-4 Vision",
        "Kimi" to "Moonshot Vision",
        "Anthropic" to "Claude Vision",
        "Qwen" to "通义千问",
        "Gemini" to "Google Gemini"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("API 设置") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                "选择 AI 提供商",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(16.dp))

            providers.forEach { (id, name) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedProvider = id }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedProvider == id,
                        onClick = { selectedProvider = id }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(name, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            id,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it },
                label = { Text("API Key") },
                placeholder = { Text("输入 API Key") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    Toast.makeText(context, "API 设置已保存", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("保存设置")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("提示", fontWeight = FontWeight.Medium)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "API Key 仅存储在本地设备，不会被上传到任何服务器。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
