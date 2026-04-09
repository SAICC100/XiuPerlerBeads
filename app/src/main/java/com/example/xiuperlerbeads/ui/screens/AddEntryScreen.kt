package com.example.xiuperlerbeads.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.xiuperlerbeads.domain.model.AttachmentType
import com.example.xiuperlerbeads.domain.model.JournalAttachment
import com.example.xiuperlerbeads.domain.model.JournalCollection
import com.example.xiuperlerbeads.domain.model.JournalTag
import com.example.xiuperlerbeads.ui.viewmodel.JournalViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEntryScreen(
    onNavigateBack: () -> Unit,
    journalViewModel: JournalViewModel = viewModel()
) {
    val state by journalViewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // 表单状态
    var selectedCollectionId by remember { mutableStateOf(JournalCollection.DEFAULT_ID) }
    var content by remember { mutableStateOf("") }
    var attachments by remember { mutableStateOf(listOf<JournalAttachment>()) }
    var selectedTags by remember { mutableStateOf(listOf<JournalTag>()) }

    // 手帐专属字段
    var useCustomTime by remember { mutableStateOf(false) }
    var selectedTimeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var location by remember { mutableStateOf("") }
    var expense by remember { mutableStateOf("0") }

    // 弹窗状态
    var showTagDialog by remember { mutableStateOf(false) }
    var showCollectionDropdown by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val isHandZhang = selectedCollectionId == JournalCollection.DEFAULT_ID
    val dateTimeFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }

    // 图片选择器（多选）
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        val newImages = uris.map { uri ->
            JournalAttachment(
                type = AttachmentType.IMAGE,
                uri = uri.toString(),
                fileName = uri.lastPathSegment ?: "image"
            )
        }
        attachments = attachments + newImages
    }

    // 文件选择器（多选）
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        val newFiles = uris.map { uri ->
            val fileName = uri.lastPathSegment ?: "file"
            JournalAttachment(
                type = AttachmentType.FILE,
                uri = uri.toString(),
                fileName = fileName
            )
        }
        attachments = attachments + newFiles
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("添加记录") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Close, contentDescription = "关闭")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            journalViewModel.addEntry(
                                collectionId = selectedCollectionId,
                                content = content,
                                attachments = attachments,
                                tags = selectedTags,
                                entryTime = if (useCustomTime) selectedTimeMillis else null,
                                location = location.takeIf { it.isNotBlank() },
                                expense = expense.toDoubleOrNull() ?: 0.0
                            )
                            onNavigateBack()
                        },
                        enabled = content.isNotBlank() || attachments.isNotEmpty()
                    ) {
                        Text("发布", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // ----------------------------------------------------------------
            // 上区：图片/附件区（1/4 高度参考）
            // ----------------------------------------------------------------
            AttachmentZone(
                attachments = attachments,
                onAddImage = { imagePickerLauncher.launch("image/*") },
                onAddFile = { filePickerLauncher.launch("*/*") },
                onRemove = { id -> attachments = attachments.filter { it.id != id } }
            )

            HorizontalDivider(thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))

            // ----------------------------------------------------------------
            // 中区：文字输入
            // ----------------------------------------------------------------
            TextField(
                value = content,
                onValueChange = { content = it },
                placeholder = { Text("记录你的想法、心情、日常…", color = MaterialTheme.colorScheme.outlineVariant) },
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 160.dp),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent
                )
            )

            HorizontalDivider(thickness = 0.5.dp)

            // ----------------------------------------------------------------
            // 下区：元数据表单
            // ----------------------------------------------------------------

            // 文集选择（必填）
            ListItem(
                headlineContent = { Text("文集") },
                leadingContent = {
                    Icon(Icons.Default.LibraryBooks, contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary)
                },
                trailingContent = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { showCollectionDropdown = true }
                    ) {
                        Text(
                            text = state.collections.find { it.id == selectedCollectionId }?.name ?: "手帐",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Icon(Icons.Default.ChevronRight, contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                modifier = Modifier.clickable { showCollectionDropdown = true }
            )

            // 手帐专属字段
            if (isHandZhang) {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                // 时间
                ListItem(
                    headlineContent = { Text("时间") },
                    leadingContent = {
                        Icon(Icons.Default.Schedule, contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary)
                    },
                    trailingContent = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (useCustomTime) dateTimeFormat.format(Date(selectedTimeMillis))
                                else "现在",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(Modifier.width(4.dp))
                            Icon(Icons.Default.ChevronRight, contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    },
                    modifier = Modifier.clickable { showDatePicker = true }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                // 地点
                ListItem(
                    headlineContent = {
                        TextField(
                            value = location,
                            onValueChange = { location = it },
                            placeholder = { Text("添加地点（选填）") },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                unfocusedContainerColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    leadingContent = {
                        Icon(Icons.Default.LocationOn, contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary)
                    }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // Tag（所有文集都有）
            ListItem(
                headlineContent = {
                    if (selectedTags.isEmpty()) {
                        Text("添加标签（选填）", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            selectedTags.forEach { tag ->
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = tag.toComposeColor().copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "# ${tag.name}",
                                        color = tag.toComposeColor(),
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                },
                leadingContent = {
                    Icon(Icons.Default.Tag, contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary)
                },
                trailingContent = {
                    Icon(Icons.Default.ChevronRight, contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                modifier = Modifier.clickable { showTagDialog = true }
            )

            // 手帐专属：花费
            if (isHandZhang) {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                ListItem(
                    headlineContent = {
                        TextField(
                            value = expense,
                            onValueChange = { v ->
                                // 金额格式过滤：只允许数字和小数点
                                if (v.isEmpty() || v.matches(Regex("^\\d{0,8}(\\.\\d{0,2})?\$"))) {
                                    expense = v
                                }
                            },
                            placeholder = { Text("花费金额（选填）") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = TextFieldDefaults.colors(
                                unfocusedContainerColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    leadingContent = {
                        Icon(Icons.Default.AttachMoney, contentDescription = null,
                            tint = MaterialTheme.colorScheme.error)
                    }
                )
            }

            Spacer(Modifier.height(80.dp))
        }
    }

    // ---- 文集选择下拉 ----
    if (showCollectionDropdown) {
        CollectionPickerDialog(
            collections = state.collections,
            selectedId = selectedCollectionId,
            onSelect = { selectedCollectionId = it; showCollectionDropdown = false },
            onDismiss = { showCollectionDropdown = false }
        )
    }

    // ---- Tag 选择 ----
    if (showTagDialog) {
        TagPickerDialog(
            allTags = state.tags,
            selectedTags = selectedTags,
            onConfirm = { tags ->
                selectedTags = tags
                showTagDialog = false
            },
            onAddTag = { name, hex -> journalViewModel.addTag(name, hex) },
            onDismiss = { showTagDialog = false }
        )
    }

    // ---- 日期选择 ----
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedTimeMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        // 保留时间部分，只替换日期
                        val cal = Calendar.getInstance().apply { timeInMillis = selectedTimeMillis }
                        val dateCal = Calendar.getInstance().apply { timeInMillis = millis }
                        cal.set(dateCal.get(Calendar.YEAR), dateCal.get(Calendar.MONTH), dateCal.get(Calendar.DAY_OF_MONTH))
                        selectedTimeMillis = cal.timeInMillis
                        useCustomTime = true
                    }
                    showDatePicker = false
                    showTimePicker = true
                }) { Text("下一步") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("取消") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    // ---- 时间选择 ----
    if (showTimePicker) {
        val cal = Calendar.getInstance().apply { timeInMillis = selectedTimeMillis }
        val timePickerState = rememberTimePickerState(
            initialHour = cal.get(Calendar.HOUR_OF_DAY),
            initialMinute = cal.get(Calendar.MINUTE)
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("选择时间") },
            text = { TimePicker(state = timePickerState) },
            confirmButton = {
                TextButton(onClick = {
                    val newCal = Calendar.getInstance().apply {
                        timeInMillis = selectedTimeMillis
                        set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        set(Calendar.MINUTE, timePickerState.minute)
                    }
                    selectedTimeMillis = newCal.timeInMillis
                    showTimePicker = false
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("取消") }
            }
        )
    }
}

// ============================================================================
// 附件区
// ============================================================================

@Composable
private fun AttachmentZone(
    attachments: List<JournalAttachment>,
    onAddImage: () -> Unit,
    onAddFile: () -> Unit,
    onRemove: (String) -> Unit
) {
    Column(modifier = Modifier.padding(12.dp)) {
        if (attachments.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(attachments, key = { it.id }) { attachment ->
                    Box {
                        if (attachment.type == AttachmentType.IMAGE) {
                            AsyncImage(
                                model = attachment.uri,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.InsertDriveFile, contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(28.dp))
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = attachment.fileName.take(10),
                                        style = MaterialTheme.typography.labelSmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    )
                                }
                            }
                        }
                        // 删除按钮
                        IconButton(
                            onClick = { onRemove(attachment.id) },
                            modifier = Modifier
                                .size(22.dp)
                                .align(Alignment.TopEnd)
                                .offset(x = 4.dp, y = (-4).dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "删除",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        // 添加按钮行
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = onAddImage,
                modifier = Modifier.height(40.dp)
            ) {
                Icon(Icons.Default.Image, contentDescription = null,
                    modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("图片", style = MaterialTheme.typography.labelMedium)
            }
            OutlinedButton(
                onClick = onAddFile,
                modifier = Modifier.height(40.dp)
            ) {
                Icon(Icons.Default.AttachFile, contentDescription = null,
                    modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("附件", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

// ============================================================================
// 文集选择弹窗
// ============================================================================

@Composable
private fun CollectionPickerDialog(
    collections: List<com.example.xiuperlerbeads.domain.model.JournalCollection>,
    selectedId: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择文集") },
        text = {
            Column {
                collections.forEach { collection ->
                    ListItem(
                        headlineContent = { Text(collection.name) },
                        trailingContent = {
                            if (collection.id == selectedId) {
                                Icon(Icons.Default.Check, contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary)
                            }
                        },
                        modifier = Modifier.clickable { onSelect(collection.id) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

// ============================================================================
// Tag 选择弹窗
// ============================================================================

@Composable
private fun TagPickerDialog(
    allTags: List<JournalTag>,
    selectedTags: List<JournalTag>,
    onConfirm: (List<JournalTag>) -> Unit,
    onAddTag: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var currentSelected by remember { mutableStateOf(selectedTags.toMutableList()) }
    var showAddTag by remember { mutableStateOf(false) }
    var newTagName by remember { mutableStateOf("") }
    var newTagColor by remember { mutableStateOf("FF6B6B") }

    // 预设颜色
    val presetColors = listOf(
        "FF6B6B", "FFD93D", "6BCB77", "4D96FF",
        "C77DFF", "FF9F1C", "2EC4B6", "E71D36"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择标签") },
        text = {
            Column {
                if (allTags.isEmpty() && !showAddTag) {
                    Text("还没有标签，点击下方添加",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    // 标签列表（可多选）
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        allTags.forEach { tag ->
                            val isSelected = currentSelected.any { it.id == tag.id }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        currentSelected = if (isSelected) {
                                            currentSelected.filter { it.id != tag.id }.toMutableList()
                                        } else {
                                            (currentSelected + tag).toMutableList()
                                        }
                                    }
                                    .padding(vertical = 6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(tag.toComposeColor())
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(tag.name, modifier = Modifier.weight(1f))
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // 新增 Tag 区
                if (showAddTag) {
                    Column {
                        OutlinedTextField(
                            value = newTagName,
                            onValueChange = { newTagName = it },
                            label = { Text("标签名") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("选择颜色", style = MaterialTheme.typography.labelMedium)
                        Spacer(Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            presetColors.forEach { hex ->
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(Color(android.graphics.Color.parseColor("#$hex")))
                                        .clickable { newTagColor = hex }
                                        .then(
                                            if (newTagColor == hex) Modifier.border(
                                                2.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                            else Modifier
                                        )
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Row {
                            TextButton(onClick = {
                                if (newTagName.isNotBlank()) {
                                    onAddTag(newTagName.trim(), newTagColor)
                                    newTagName = ""
                                    showAddTag = false
                                }
                            }) { Text("添加") }
                            TextButton(onClick = { showAddTag = false }) { Text("取消") }
                        }
                    }
                } else {
                    TextButton(onClick = { showAddTag = true }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("新建标签")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(currentSelected) }) { Text("确定") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}
