@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.xiuperlerbeads.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.xiuperlerbeads.domain.model.AttachmentType
import com.example.xiuperlerbeads.domain.model.JournalEntry
import com.example.xiuperlerbeads.domain.model.JournalCollection
import com.example.xiuperlerbeads.ui.viewmodel.JournalViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun JournalHomeScreen(
    onNavigateToAddEntry: () -> Unit,
    onNavigateToProfile: () -> Unit,
    journalViewModel: JournalViewModel = viewModel()
) {
    val state by journalViewModel.state.collectAsStateWithLifecycle()
    var showNewCollectionDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { journalViewModel.loadData() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.selectedCollection?.name ?: "手帐",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "个人中心")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddEntry,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加记录", tint = Color.White)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 文集标签栏
            CollectionTabBar(
                collections = state.collections,
                selectedId = state.selectedCollectionId,
                onSelect = { journalViewModel.selectCollection(it) },
                onAddCollection = { showNewCollectionDialog = true }
            )

            Divider(thickness = 0.5.dp)

            // 内容 Feed
            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.filteredEntries.isEmpty()) {
                EmptyFeedPlaceholder(onAddClick = onNavigateToAddEntry)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(state.filteredEntries, key = { it.id }) { entry ->
                        EntryCard(entry = entry)
                        Divider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }

    // 新建文集弹窗
    if (showNewCollectionDialog) {
        NewCollectionDialog(
            onConfirm = { name ->
                journalViewModel.addCollection(name)
                showNewCollectionDialog = false
            },
            onDismiss = { showNewCollectionDialog = false }
        )
    }
}

// ============================================================================
// 文集标签栏
// ============================================================================

@Composable
private fun CollectionTabBar(
    collections: List<JournalCollection>,
    selectedId: String,
    onSelect: (String) -> Unit,
    onAddCollection: () -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(collections, key = { it.id }) { collection ->
            val isSelected = collection.id == selectedId
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(collection.id) },
                label = {
                    Text(
                        text = collection.name,
                        style = MaterialTheme.typography.labelLarge
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = Color.White
                )
            )
        }
        item {
            // 添加文集按钮
            IconButton(
                onClick = onAddCollection,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "添加文集",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// ============================================================================
// 条目卡片
// ============================================================================

@Composable
private fun EntryCard(entry: JournalEntry) {
    val dateFormat = remember { SimpleDateFormat("MM月dd日 HH:mm", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* TODO: 跳转详情 */ }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // 顶部：头像 + 时间
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // 头像占位
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = dateFormat.format(Date(entry.displayTime)),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                // 手帐扩展信息行
                if (entry.isHandZhang) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        entry.location?.let { loc ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(Modifier.width(2.dp))
                                Text(
                                    text = loc,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        if (entry.expense > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.AttachMoney,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = String.format("%.2f", entry.expense),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // 正文内容
        if (entry.content.isNotBlank()) {
            Text(
                text = entry.content,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 5,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 46.dp)
            )
            Spacer(Modifier.height(6.dp))
        }

        // 图片预览（最多4张）
        val images = entry.images
        if (images.isNotEmpty()) {
            val displayImages = images.take(4)
            Row(
                modifier = Modifier
                    .padding(start = 46.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                displayImages.forEach { attachment ->
                    AsyncImage(
                        model = attachment.uri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(if (images.size == 1) 160.dp else 80.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                }
                // 更多图片提示
                if (images.size > 4) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+${images.size - 4}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
        }

        // 附件提示（非图片文件）
        val files = entry.files
        if (files.isNotEmpty()) {
            Row(
                modifier = Modifier.padding(start = 46.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.AttachFile,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "${files.size}个附件",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(6.dp))
        }

        // Tags
        if (entry.tags.isNotEmpty()) {
            Row(
                modifier = Modifier.padding(start = 46.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                entry.tags.forEach { tag ->
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = tag.toComposeColor().copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "# ${tag.name}",
                            style = MaterialTheme.typography.labelSmall,
                            color = tag.toComposeColor(),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// 空状态
// ============================================================================

@Composable
private fun EmptyFeedPlaceholder(onAddClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.BookmarkBorder,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.outlineVariant
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "还没有记录",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onAddClick) {
                Text("点击添加第一条")
            }
        }
    }
}

// ============================================================================
// 新建文集弹窗
// ============================================================================

@Composable
private fun NewCollectionDialog(onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
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
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}
