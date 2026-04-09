package com.example.xiuperlerbeads.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.xiuperlerbeads.data.export.ExportManager
import com.example.xiuperlerbeads.domain.model.JournalCollection
import com.example.xiuperlerbeads.domain.model.JournalTag
import com.example.xiuperlerbeads.ui.viewmodel.JournalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    journalViewModel: JournalViewModel = viewModel()
) {
    val context = LocalContext.current
    val state by journalViewModel.state.collectAsStateWithLifecycle()
    val exportManager = remember { ExportManager(context) }

    var showEditNameDialog by remember { mutableStateOf(false) }
    var showAddCollectionDialog by remember { mutableStateOf(false) }
    var showTagManageDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var collectionToDelete by remember { mutableStateOf<JournalCollection?>(null) }
    var collectionToRename by remember { mutableStateOf<JournalCollection?>(null) }

    // 账户名（简单本地存储，无登录体系）
    var displayName by remember { mutableStateOf("我的手帐") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("个人中心", fontWeight = FontWeight.Bold) }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // ── 账户信息卡片 ──────────────────────────────────────────────────
            item {
                AccountCard(
                    displayName = displayName,
                    entryCount = state.entries.size,
                    collectionCount = state.collections.size,
                    tagCount = state.tags.size,
                    onEditName = { showEditNameDialog = true }
                )
            }

            item { Spacer(Modifier.height(16.dp)) }

            // ── 文集管理标题 ──────────────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "我的文集",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = { showAddCollectionDialog = true }) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("新建", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            // ── 文集列表 ──────────────────────────────────────────────────────
            items(state.collections, key = { it.id }) { collection ->
                val entryCount = state.entries.count { it.collectionId == collection.id }
                CollectionRow(
                    collection = collection,
                    entryCount = entryCount,
                    onRename = { collectionToRename = collection },
                    onDelete = { collectionToDelete = collection }
                )
                Divider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }

            item { Spacer(Modifier.height(24.dp)) }

            // ── 其他设置入口 ──────────────────────────────────────────────────
            item {
                Text(
                    "其他",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(4.dp))
            }
            item {
                SettingsRow(
                    icon = Icons.Default.Palette,
                    iconTint = MaterialTheme.colorScheme.primary,
                    title = "标签管理",
                    subtitle = "${state.tags.size} 个标签",
                    onClick = { showTagManageDialog = true }
                )
                Divider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
            }
            item {
                SettingsRow(
                    icon = Icons.Default.Backup,
                    iconTint = MaterialTheme.colorScheme.secondary,
                    title = "数据备份",
                    subtitle = "导出手帐数据",
                    onClick = {
                        val u = exportManager.saveTextToFile(buildString {
                            appendLine("手帐数据备份"); appendLine("=".repeat(40))
                            val fmt = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
                            state.entries.sortedByDescending { it.createdAt }.forEach { e ->
                                appendLine("[${fmt.format(java.util.Date(e.displayTime))}]")
                                if (!e.location.isNullOrBlank()) appendLine("地点: ${e.location}")
                                if (e.expense > 0) appendLine("花费: ¥${e.expense}")
                                appendLine(e.content); appendLine()
                            }
                        }, "journal_backup")
                        if (u != null) context.startActivity(Intent.createChooser(exportManager.shareContent(u, "text/plain", "手帐数据备份"), "导出手帐数据"))
                        else Toast.makeText(context, "备份失败", Toast.LENGTH_SHORT).show()
                    }
                )
                Divider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
            }
            item {
                SettingsRow(
                    icon = Icons.Default.Info,
                    iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                    title = "关于",
                    subtitle = "版本 1.0.0",
                    onClick = { showAboutDialog = true }
                )
            }
        }
    }

    // ── 弹窗区域 ─────────────────────────────────────────────────────────────

    if (showEditNameDialog) {
        EditNameDialog(
            currentName = displayName,
            onConfirm = { newName ->
                displayName = newName
                showEditNameDialog = false
            },
            onDismiss = { showEditNameDialog = false }
        )
    }

    if (showAddCollectionDialog) {
        NewCollectionDialog2(
            onConfirm = { name ->
                journalViewModel.addCollection(name)
                showAddCollectionDialog = false
            },
            onDismiss = { showAddCollectionDialog = false }
        )
    }

    collectionToRename?.let { collection ->
        RenameCollectionDialog(
            currentName = collection.name,
            onConfirm = { newName ->
                journalViewModel.updateCollection(collection.copy(name = newName))
                collectionToRename = null
            },
            onDismiss = { collectionToRename = null }
        )
    }

    if (showTagManageDialog) {
        TagManageDialog(tags = state.tags, onAddTag = { name, colorHex -> journalViewModel.addTag(name, colorHex) }, onDeleteTag = { id -> journalViewModel.deleteTag(id) }, onDismiss = { showTagManageDialog = false })
    }
    if (showAboutDialog) {
        AlertDialog(onDismissRequest = { showAboutDialog = false }, icon = { Icon(Icons.Default.Info, null) }, title = { Text("关于") }, text = { Column { Text("秀拼豆  版本 1.0.0"); Spacer(modifier = Modifier.height(8.dp)); Text("拼豆手帐 & 库存管理工具") } }, confirmButton = { TextButton(onClick = { showAboutDialog = false }) { Text("好的") } })
    }
    collectionToDelete?.let { collection ->
        AlertDialog(
            onDismissRequest = { collectionToDelete = null },
            icon = { Icon(Icons.Default.DeleteOutline, contentDescription = null) },
            title = { Text("删除文集") },
            text = {
                Text("确定要删除「${collection.name}」吗？\n该文集下的记录不会被删除，但文集分类将丢失。")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        journalViewModel.deleteCollection(collection.id)
                        collectionToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("删除") }
            },
            dismissButton = {
                TextButton(onClick = { collectionToDelete = null }) { Text("取消") }
            }
        )
    }
}

// ============================================================================
// 账户信息卡片
// ============================================================================

@Composable
private fun AccountCard(
    displayName: String,
    entryCount: Int,
    collectionCount: Int,
    tagCount: Int,
    onEditName: () -> Unit
) {
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                // 头像
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = displayName.take(1),
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "点击修改名称",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                    )
                }
                IconButton(onClick = onEditName) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "修改名称",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Divider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
            Spacer(Modifier.height(16.dp))

            // 统计数字
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(label = "记录", value = entryCount.toString())
                Divider(
                    modifier = Modifier.height(36.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                )
                StatItem(label = "文集", value = collectionCount.toString())
                Divider(
                    modifier = Modifier.height(36.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                )
                StatItem(label = "标签", value = tagCount.toString())
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
    }
}

// ============================================================================
// 文集行
// ============================================================================

@Composable
private fun CollectionRow(
    collection: JournalCollection,
    entryCount: Int,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    ListItem(
        headlineContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(collection.name, style = MaterialTheme.typography.bodyLarge)
                if (collection.isDefault) {
                    Spacer(Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    ) {
                        Text(
                            "默认",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        },
        supportingContent = {
            Text(
                "$entryCount 条记录",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingContent = {
            Icon(
                if (collection.isDefault) Icons.Default.Book else Icons.Default.BookmarkBorder,
                contentDescription = null,
                tint = if (collection.isDefault)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingContent = {
            if (!collection.isDefault) {
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "更多操作",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("重命名") },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null)
                            },
                            onClick = {
                                showMenu = false
                                onRename()
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text("删除", color = MaterialTheme.colorScheme.error)
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = {
                                showMenu = false
                                onDelete()
                            }
                        )
                    }
                }
            }
        }
    )
}

// ============================================================================
// 通用设置行
// ============================================================================

@Composable
private fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = {
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconTint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            }
        },
        trailingContent = {
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

// ============================================================================
// 弹窗
// ============================================================================

@Composable
private fun EditNameDialog(currentName: String, onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf(currentName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("修改名称") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("显示名称") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onConfirm(name.trim()) },
                enabled = name.isNotBlank()
            ) { Text("保存") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}

@Composable
private fun NewCollectionDialog2(onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新建文集") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("文集名称") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onConfirm(name.trim()) },
                enabled = name.isNotBlank()
            ) { Text("创建") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}

@Composable
private fun RenameCollectionDialog(currentName: String, onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf(currentName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("重命名文集") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("文集名称") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onConfirm(name.trim()) },
                enabled = name.isNotBlank()
            ) { Text("保存") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}

// ============================================================================
// 标签管理弹窗
// ============================================================================

private val TAG_PRESET_COLORS = listOf("#E57373", "#FFD54F", "#81C784", "#64B5F6", "#BA68C8", "#FF8A65", "#4DD0E1", "#C62828")

@Composable
private fun TagManageDialog(
    tags: List<JournalTag>,
    onAddTag: (String, String) -> Unit,
    onDeleteTag: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var newTagName by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(TAG_PRESET_COLORS[0]) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Palette, null) },
        title = { Text("标签管理") },
        text = {
            Column {
                if (tags.isNotEmpty()) {
                    tags.forEach { tag ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(14.dp).clip(RoundedCornerShape(3.dp)).background(tag.toComposeColor()))
                            Spacer(Modifier.width(8.dp))
                            Text(tag.name, modifier = Modifier.weight(1f))
                            IconButton(onClick = { onDeleteTag(tag.id) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                }
                OutlinedTextField(value = newTagName, onValueChange = { newTagName = it }, label = { Text("新标签名称") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TAG_PRESET_COLORS.forEach { hex ->
                        Box(
                            modifier = Modifier.size(24.dp).clip(CircleShape)
                                .background(try { Color(android.graphics.Color.parseColor(hex)) } catch (e: Exception) { Color.Gray })
                                .clickable { selectedColor = hex }
                                .then(if (selectedColor == hex) Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape) else Modifier)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { if (newTagName.isNotBlank()) { onAddTag(newTagName.trim(), selectedColor); newTagName = "" } }, enabled = newTagName.isNotBlank()) { Text("添加") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("完成") } }
    )
}
