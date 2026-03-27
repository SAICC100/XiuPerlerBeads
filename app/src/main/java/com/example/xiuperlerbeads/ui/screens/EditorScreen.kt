package com.example.xiuperlerbeads.ui.screens

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.xiuperlerbeads.domain.model.CanvasSize
import com.example.xiuperlerbeads.ui.components.*

/**
 * Main editor screen for pixel art creation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: EditorViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var showNewProjectDialog by remember { mutableStateOf(false) }
    var showReduceColorsDialog by remember { mutableStateOf(false) }
    var selectedCanvasSize by remember { mutableStateOf(CanvasSize.SIZE_32.dimension) }

    // Initialize ViewModel with context
    LaunchedEffect(Unit) {
        viewModel.initialize(context)
    }

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            context.contentResolver.openInputStream(it)?.use { stream ->
                val bitmap = BitmapFactory.decodeStream(stream)
                if (bitmap != null) {
                    viewModel.importImage(bitmap, selectedCanvasSize)
                }
            }
        }
    }

    // Show snackbar for messages
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("秀拼豆 - ${uiState.project?.name ?: "未创建项目"}") },
                actions = {
                    IconButton(onClick = { showNewProjectDialog = true }) {
                        Icon(Icons.Default.Add, "新建项目")
                    }
                    IconButton(onClick = { imagePickerLauncher.launch("image/*") }) {
                        Icon(Icons.Default.Image, "导入图片")
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
            if (uiState.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            if (uiState.project == null) {
                // Empty state
                EmptyState(
                    onCreateProject = { showNewProjectDialog = true },
                    onImportImage = { imagePickerLauncher.launch("image/*") }
                )
            } else {
                // Main editor content
                Column(modifier = Modifier.weight(1f)) {
                    // Drawing canvas
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        PixelCanvas(
                            project = uiState.project!!,
                            onPixelClick = { x, y -> viewModel.onPixelInteraction(x, y) },
                            onPixelDrag = { x, y -> viewModel.onPixelInteraction(x, y) },
                            showGrid = true
                        )
                    }

                    // Drawing toolbar
                    DrawingToolbar(
                        selectedTool = uiState.selectedTool,
                        onToolSelected = { viewModel.selectTool(it) },
                        brushSize = uiState.brushSize,
                        onBrushSizeChanged = { viewModel.setBrushSize(it) },
                        onUndo = { viewModel.undo() },
                        onRedo = { viewModel.redo() },
                        canUndo = uiState.undoStack.isNotEmpty(),
                        canRedo = uiState.redoStack.isNotEmpty()
                    )

                    Divider()

                    // Bottom action bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Color picker button
                        CompactColorPicker(
                            selectedColor = uiState.selectedColor,
                            onColorSelected = { viewModel.selectColor(it) }
                        )

                        IconButton(onClick = { viewModel.toggleColorPicker() }) {
                            Icon(
                                Icons.Default.Palette,
                                "选择颜色",
                                tint = if (uiState.showColorPicker)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                        }

                        IconButton(onClick = { viewModel.exportToPng() }) {
                            Icon(Icons.Default.Image, "导出PNG")
                        }

                        IconButton(onClick = { viewModel.exportToPdf() }) {
                            Icon(Icons.Default.PictureAsPdf, "导出PDF")
                        }

                        IconButton(onClick = { viewModel.toggleStatistics() }) {
                            Icon(
                                Icons.Default.PieChart,
                                "色号统计",
                                tint = if (uiState.showStatistics)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                        }

                        IconButton(onClick = { showReduceColorsDialog = true }) {
                            Icon(Icons.Default.Compress, "减少颜色")
                        }
                    }
                }

                // Color picker panel
                if (uiState.showColorPicker) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp)
                    ) {
                        ColorPicker(
                            selectedColor = uiState.selectedColor,
                            onColorSelected = {
                                viewModel.selectColor(it)
                                viewModel.toggleColorPicker()
                            }
                        )
                    }
                }

                // Statistics panel
                if (uiState.showStatistics) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                    ) {
                        ColorStatisticsPanel(statistics = uiState.colorStatistics)
                    }
                }
            }
        }
    }

    // New project dialog
    if (showNewProjectDialog) {
        NewProjectDialog(
            selectedSize = selectedCanvasSize,
            onSizeChanged = { selectedCanvasSize = it },
            onDismiss = { showNewProjectDialog = false },
            onConfirm = { name, size ->
                viewModel.createNewProject(name, size, size)
                showNewProjectDialog = false
            }
        )
    }

    // Reduce colors dialog
    if (showReduceColorsDialog) {
        ReduceColorsDialog(
            currentColorCount = uiState.colorStatistics.size,
            onDismiss = { showReduceColorsDialog = false },
            onReduce = { maxColors ->
                viewModel.reduceColors(maxColors)
                showReduceColorsDialog = false
            },
            onMergeSimilar = {
                viewModel.mergeSimilarColors()
                showReduceColorsDialog = false
            }
        )
    }
}

@Composable
private fun EmptyState(
    onCreateProject: () -> Unit,
    onImportImage: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.GridOn,
            contentDescription = null,
            modifier = Modifier.size(96.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "欢迎使用秀拼豆",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "创建新项目或导入图片开始创作",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onCreateProject,
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Icon(Icons.Default.Add, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("新建空白项目")
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onImportImage,
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Icon(Icons.Default.Image, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("导入图片生成图纸")
        }
    }
}

@Composable
private fun NewProjectDialog(
    selectedSize: Int,
    onSizeChanged: (Int) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (String, Int) -> Unit
) {
    var projectName by remember { mutableStateOf("我的拼豆") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新建项目") },
        text = {
            Column {
                OutlinedTextField(
                    value = projectName,
                    onValueChange = { projectName = it },
                    label = { Text("项目名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("画布尺寸", style = MaterialTheme.typography.labelMedium)

                Spacer(modifier = Modifier.height(8.dp))

                CanvasSizeSelector(
                    selectedSize = selectedSize,
onSizeSelected = onSizeChanged
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(projectName, selectedSize) }) {
                Text("创建")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun ReduceColorsDialog(
    currentColorCount: Int,
    onDismiss: () -> Unit,
    onReduce: (Int) -> Unit,
    onMergeSimilar: () -> Unit
) {
    var targetColors by remember { mutableStateOf((currentColorCount / 2).coerceAtLeast(10)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("减少颜色") },
        text = {
            Column {
                Text(
                    text = "当前使用 $currentColorCount 种颜色",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("目标颜色数量", style = MaterialTheme.typography.labelMedium)

                Slider(
                    value = targetColors.toFloat(),
                    onValueChange = { targetColors = it.toInt() },
                    valueRange = 5f..currentColorCount.toFloat().coerceAtLeast(10f),
                    steps = (currentColorCount - 5).coerceAtLeast(1)
                )

                Text(
                    text = "$targetColors 种颜色",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = onMergeSimilar,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("合并相近颜色")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onReduce(targetColors) }) {
                Text("精简颜色")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
