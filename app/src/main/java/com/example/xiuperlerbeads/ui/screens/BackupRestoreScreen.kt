package com.example.xiuperlerbeads.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.xiuperlerbeads.domain.model.SnapshotInfo
import com.example.xiuperlerbeads.ui.viewmodel.InventoryViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * 备份与恢复页
 * 支持手动创建快照、查看历史快照（最多10个）、一键恢复
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupRestoreScreen(
    onNavigateBack: () -> Unit,
    inventoryViewModel: InventoryViewModel = viewModel()
) {
    val state by inventoryViewModel.state.collectAsState()
    val snapshots = state.snapshots

    var showCreateDialog by remember { mutableStateOf(false) }
    var restoringSnapshot by remember { mutableStateOf<SnapshotInfo?>(null) }
    var deletingSnapshot by remember { mutableStateOf<SnapshotInfo?>(null) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            snackbarMessage = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("备份与恢复", fontWeight = FontWeight.Bold)
                        Text(
                            "最多保留 10 个快照",
                            style = MaterialTheme.typography.labelSmall,
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
            ExtendedFloatingActionButton(
                onClick = { showCreateDialog = true },
                icon = { Icon(Icons.Default.AddCircle, contentDescription = null) },
                text = { Text("立即备份") }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = 8.dp, bottom = 88.dp
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 说明卡片
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp).padding(top = 1.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            "快照保存到应用私有目录，最多保留 10 个版本。超出时自动删除最早的快照。恢复操作会覆盖当前全部数据。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            if (snapshots.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Backup,
                                contentDescription = null,
                                modifier = Modifier.size(56.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                            Text(
                                "暂无备份快照",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "点击下方「立即备份」创建第一个快照",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                item {
                    Text(
                        "快照历史（${snapshots.size}/10）",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                    )
                }
                items(snapshots, key = { it.filename }) { snapshot ->
                    SnapshotCard(
                        snapshot = snapshot,
                        onRestore = { restoringSnapshot = snapshot },
                        onDelete = { deletingSnapshot = snapshot }
                    )
                }
            }
        }
    }

    // 创建快照对话框
    if (showCreateDialog) {
        CreateSnapshotDialog(
            currentStats = "${state.brands.size} 品牌 · ${state.stocks.filter { !it.isHidden }.size} 条库存 · ${state.projects.size} 项目",
            onDismiss = { showCreateDialog = false },
            onConfirm = { label ->
                inventoryViewModel.createSnapshot(label)
                showCreateDialog = false
                snackbarMessage = "快照创建成功"
            }
        )
    }

    // 恢复确认对话框
    restoringSnapshot?.let { snapshot ->
        AlertDialog(
            onDismissRequest = { restoringSnapshot = null },
            icon = { Icon(Icons.Default.Warning, contentDescription = null) },
            title = { Text("恢复此快照？") },
            text = {
                val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINESE)
                Text(
                    "将恢复到 ${fmt.format(Date(snapshot.snapshotTime))} 的数据状态。\n\n" +
                    "此操作会覆盖当前所有库存、品牌和项目数据，且无法撤销。\n\n" +
                    "建议先备份当前数据再执行恢复。"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        inventoryViewModel.restoreSnapshot(snapshot.filename)
                        restoringSnapshot = null
                        snackbarMessage = "已恢复到 ${
                            SimpleDateFormat("MM-dd HH:mm", Locale.CHINESE).format(Date(snapshot.snapshotTime))
                        } 的快照"
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("确认恢复")
                }
            },
            dismissButton = {
                TextButton(onClick = { restoringSnapshot = null }) { Text("取消") }
            }
        )
    }

    // 删除确认对话框
    deletingSnapshot?.let { snapshot ->
        AlertDialog(
            onDismissRequest = { deletingSnapshot = null },
            title = { Text("删除快照") },
            text = {
                val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINESE)
                Text("确认删除 ${fmt.format(Date(snapshot.snapshotTime))} 的快照？此操作不可撤销。")
            },
            confirmButton = {
                Button(
                    onClick = {
                        inventoryViewModel.deleteSnapshot(snapshot.filename)
                        deletingSnapshot = null
                        snackbarMessage = "快照已删除"
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingSnapshot = null }) { Text("取消") }
            }
        )
    }

    state.error?.let { error ->
        LaunchedEffect(error) {
            snackbarMessage = error
            inventoryViewModel.clearError()
        }
    }
}

@Composable
private fun SnapshotCard(
    snapshot: SnapshotInfo,
    onRestore: () -> Unit,
    onDelete: () -> Unit
) {
    val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINESE)
    val sizeKb = snapshot.fileSizeBytes / 1024

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 备份图标
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.SaveAlt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                if (snapshot.label.isNotEmpty()) {
                    Text(
                        snapshot.label,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    fmt.format(Date(snapshot.snapshotTime)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(2.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    StatChip("${snapshot.brandsCount} 品牌")
                    StatChip("${snapshot.stocksCount} 色")
                    StatChip("${snapshot.projectsCount} 项")
                    StatChip("${sizeKb}KB")
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.End
            ) {
                FilledTonalButton(
                    onClick = onRestore,
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp)
                ) {
                    Text("恢复", style = MaterialTheme.typography.labelMedium)
                }
                TextButton(
                    onClick = onDelete,
                    modifier = Modifier.height(28.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp)
                ) {
                    Text(
                        "删除",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun StatChip(text: String) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun CreateSnapshotDialog(
    currentStats: String,
    onDismiss: () -> Unit,
    onConfirm: (label: String) -> Unit
) {
    var label by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Backup, contentDescription = null) },
        title = { Text("创建备份快照") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "当前数据：$currentStats",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("备注标签（选填）") },
                    placeholder = { Text("如：整理前备份") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(label.trim()) }) { Text("创建") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}
