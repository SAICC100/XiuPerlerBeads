package com.example.xiuperlerbeads.ui.screens

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.xiuperlerbeads.domain.model.BeadColor
import com.example.xiuperlerbeads.domain.model.BeadColorManager
import com.example.xiuperlerbeads.ui.viewmodel.CanvasTool
import com.example.xiuperlerbeads.ui.viewmodel.CanvasViewModel
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CanvasScreen(
    projectId: String? = null,
    gridSize: Int = 32,
    projectName: String = "我的拼豆",
    onNavigateBack: () -> Unit = {},
    viewModel: CanvasViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    
    var showColorPicker by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }
    var showExportSuccess by remember { mutableStateOf(false) }
    
    // 初始化画布
    LaunchedEffect(projectId, gridSize, projectName) {
        if (projectId != null && projectId != com.example.xiuperlerbeads.ui.navigation.Screen.NEW_PROJECT_ID) {
            viewModel.loadProject(projectId)
        } else {
            viewModel.initializeCanvas(gridSize, projectName)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (state.isSaved) state.projectName else "${state.projectName} *",
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { 
                        if (!state.isSaved) {
                            showSaveDialog = true
                        } else {
                            onNavigateBack()
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                },
                actions = {
                    // 撤销
                    IconButton(
                        onClick = { viewModel.undo() },
                        enabled = viewModel.canUndo()
                    ) {
                        Icon(Icons.Default.Undo, "撤销")
                    }
                    // 重做
                    IconButton(
                        onClick = { viewModel.redo() },
                        enabled = viewModel.canRedo()
                    ) {
                        Icon(Icons.Default.Redo, "重做")
                    }
                    // 网格切换
                    IconButton(onClick = { viewModel.toggleGrid() }) {
                        Icon(
                            if (state.showGrid) Icons.Default.GridOn else Icons.Default.GridOff,
                            "网格"
                        )
                    }
                    // 保存
                    IconButton(onClick = { 
                        viewModel.saveProject { savedId ->
                            Toast.makeText(context, "项目已保存", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Icon(Icons.Default.Save, "保存")
                    }
                    // 更多操作
                    var showMenu by remember { mutableStateOf(false) }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, "更多")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("清空画布") },
                            onClick = { 
                                showMenu = false
                                showClearDialog = true 
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("导出 PNG") },
                            onClick = { 
                                showMenu = false
                                exportToPng(context, viewModel)
                                showExportSuccess = true
                            },
                            leadingIcon = { Icon(Icons.Default.FileDownload, null) }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 工具栏
            ToolBar(
                selectedTool = state.selectedTool,
                onToolSelected = { viewModel.selectTool(it) },
                onColorPickerClick = { showColorPicker = true },
                selectedColor = viewModel.getSelectedColor()
            )
            
            // 画布区域
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFF2C2C2C)),
                contentAlignment = Alignment.Center
            ) {
                CanvasView(
                    gridSize = state.gridSize,
                    canvasData = state.canvasData,
                    showGrid = state.showGrid,
                    colors = viewModel.getAvailableColors(),
                    onCellClick = { x, y -> viewModel.drawCell(x, y) }
                )
            }
            
            // 颜色调色板
            ColorPalette(
                colors = viewModel.getAvailableColors(),
                selectedIndex = state.selectedColorIndex,
                colorStats = state.colorStats,
                onColorSelected = { viewModel.selectColor(it) }
            )
        }
    }
    
    // 颜色选择对话框
    if (showColorPicker) {
        ColorPickerDialog(
            colors = viewModel.getAvailableColors(),
            selectedIndex = state.selectedColorIndex,
            onColorSelected = { 
                viewModel.selectColor(it)
                showColorPicker = false
            },
            onDismiss = { showColorPicker = false }
        )
    }
    
    // 确认清空对话框
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("清空画布") },
            text = { Text("确定要清空画布吗？此操作可以撤销。") },
            confirmButton = {
                TextButton(
                    onClick = { 
                        viewModel.clearCanvas()
                        showClearDialog = false
                    }
                ) {
                    Text("清空")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
    
    // 导出成功提示
    if (showExportSuccess) {
        LaunchedEffect(showExportSuccess) {
            kotlinx.coroutines.delay(2000)
            showExportSuccess = false
        }
        Toast.makeText(context, "图片已保存到相册", Toast.LENGTH_SHORT).show()
    }
}

@Composable
private fun CanvasView(
    gridSize: Int,
    canvasData: List<List<Int>>,
    showGrid: Boolean,
    colors: List<BeadColor>,
    onCellClick: (Int, Int) -> Unit
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(0.5f, 3f)
                    offset += pan
                }
            }
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                }
                .pointerInput(gridSize) {
                    detectTapGestures { tapOffset ->
                        val cellSize = minOf(size.width, size.height) / gridSize.toFloat()
                        val startX = (size.width - cellSize * gridSize) / 2
                        val startY = (size.height - cellSize * gridSize) / 2
                        
                        val adjustedX = (tapOffset.x - offset.x) / scale - startX
                        val adjustedY = (tapOffset.y - offset.y) / scale - startY
                        
                        if (adjustedX >= 0 && adjustedY >= 0) {
                            val cellX = (adjustedX / cellSize).toInt()
                            val cellY = (adjustedY / cellSize).toInt()
                            if (cellX in 0 until gridSize && cellY in 0 until gridSize) {
                                onCellClick(cellX, cellY)
                            }
                        }
                    }
                }
        ) {
            val cellSize = minOf(size.width, size.height) / gridSize.toFloat()
            val startX = (size.width - cellSize * gridSize) / 2
            val startY = (size.height - cellSize * gridSize) / 2
            
            // 绘制背景
            drawRect(
                color = Color(0xFF2C2C2C),
                topLeft = Offset(startX, startY),
                size = Size(cellSize * gridSize, cellSize * gridSize)
            )
            
            // 绘制格子
            for (y in 0 until gridSize) {
                for (x in 0 until gridSize) {
                    val colorIndex = canvasData[y][x]
                    val color = if (colorIndex >= 0 && colorIndex < colors.size) {
                        Color(colors[colorIndex].red, colors[colorIndex].green, colors[colorIndex].blue)
                    } else {
                        Color.White
                    }
                    
                    // 绘制格子背景
                    drawRect(
                        color = color,
                        topLeft = Offset(startX + x * cellSize, startY + y * cellSize),
                        size = Size(cellSize - 1, cellSize - 1)
                    )
                    
                    // 绘制网格线
                    if (showGrid) {
                        drawRect(
                            color = Color.Gray.copy(alpha = 0.3f),
                            topLeft = Offset(startX + x * cellSize, startY + y * cellSize),
                            size = Size(cellSize, cellSize),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ToolBar(
    selectedTool: CanvasTool,
    onToolSelected: (CanvasTool) -> Unit,
    onColorPickerClick: () -> Unit,
    selectedColor: BeadColor?
) {
    Surface(shadowElevation = 4.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ToolButton(
                icon = Icons.Default.Edit,
                label = "画笔",
                selected = selectedTool == CanvasTool.PENCIL,
                onClick = { onToolSelected(CanvasTool.PENCIL) }
            )
            ToolButton(
                icon = Icons.Default.CleaningServices,
                label = "橡皮",
                selected = selectedTool == CanvasTool.ERASER,
                onClick = { onToolSelected(CanvasTool.ERASER) }
            )
            ToolButton(
                icon = Icons.Default.FormatColorFill,
                label = "填充",
                selected = selectedTool == CanvasTool.FILL,
                onClick = { onToolSelected(CanvasTool.FILL) }
            )
            ToolButton(
                icon = Icons.Default.Colorize,
                label = "取色",
                selected = selectedTool == CanvasTool.PICKER,
                onClick = { onToolSelected(CanvasTool.PICKER) }
            )
            
            // 当前颜色预览
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        selectedColor?.let { 
                            Color(it.red, it.green, it.blue) 
                        } ?: Color.Gray
                    )
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    .clickable { onColorPickerClick() }
            )
        }
    }
}

@Composable
private fun ToolButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .background(
                if (selected) MaterialTheme.colorScheme.primaryContainer
                else Color.Transparent
            )
            .padding(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) MaterialTheme.colorScheme.primary
                   else MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ColorPalette(
    colors: List<BeadColor>,
    selectedIndex: Int,
    colorStats: Map<Int, Int>,
    onColorSelected: (Int) -> Unit
) {
    Surface(shadowElevation = 4.dp) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "调色板",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (colorStats.isNotEmpty()) {
                    Text(
                        "使用 ${colorStats.size} 种颜色",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                colors.take(48).forEachIndexed { index, color ->
                    val count = colorStats[index] ?: 0
                    Box(
                        modifier = Modifier
                            .size(if (selectedIndex == index) 44.dp else 40.dp)
                            .clip(CircleShape)
                            .background(Color(color.red, color.green, color.blue))
                            .border(
                                width = if (selectedIndex == index) 3.dp else 1.dp,
                                color = if (selectedIndex == index) 
                                    MaterialTheme.colorScheme.primary 
                                else Color.Gray.copy(alpha = 0.5f),
                                shape = CircleShape
                            )
                            .clickable { onColorSelected(index) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun ColorPickerDialog(
    colors: List<BeadColor>,
    selectedIndex: Int,
    onColorSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredColors = remember(searchQuery, colors) {
        if (searchQuery.isEmpty()) {
            colors
        } else {
            colors.filter {
                it.mardCode.contains(searchQuery, ignoreCase = true) ||
                it.colorName.contains(searchQuery, ignoreCase = true)
            }
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择颜色") },
        text = {
            Column {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("搜索色号") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, null) }
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(6),
                    modifier = Modifier.height(300.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
) {
                    items(filteredColors.size) { index ->
                        val color = filteredColors[index]
                        val originalIndex = colors.indexOf(color)
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(color.red, color.green, color.blue))
                                .border(
                                    width = if (selectedIndex == originalIndex) 2.dp else 0.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .clickable { onColorSelected(originalIndex) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
}

/**
 * 导出为 PNG 并保存到相册
 */
private fun exportToPng(context: Context, viewModel: CanvasViewModel) {
    try {
        val bitmap = viewModel.exportToBitmap()
        
        // 放大 bitmap 以获得更高质量
        val scale = 16
        val scaledBitmap = Bitmap.createScaledBitmap(
            bitmap, 
            bitmap.width * scale, 
            bitmap.height * scale, 
            true
        )
        
        val filename = "${viewModel.state.value.projectName}_${System.currentTimeMillis()}.png"
        
        val outputStream: OutputStream? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/XiuPerlerBeads")
            }
            val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            uri?.let { context.contentResolver.openOutputStream(it) }
        } else {
            val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val xiuperlerbeadsDir = File(imagesDir, "XiuPerlerBeads")
            if (!xiuperlerbeadsDir.exists()) xiuperlerbeadsDir.mkdirs()
            val imageFile = File(xiuperlerbeadsDir, filename)
            FileOutputStream(imageFile)
        }
        
        outputStream?.use { 
            scaledBitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        
        if (scaledBitmap != bitmap) {
            scaledBitmap.recycle()
        }
        bitmap.recycle()
        
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "导出失败: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
