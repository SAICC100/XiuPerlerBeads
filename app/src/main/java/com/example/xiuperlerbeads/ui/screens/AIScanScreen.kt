package com.example.xiuperlerbeads.ui.screens

import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.xiuperlerbeads.data.ai.AIManager
import com.example.xiuperlerbeads.data.ai.RecognizedColor
import com.example.xiuperlerbeads.domain.model.BeadColorManager
import com.example.xiuperlerbeads.ui.viewmodel.InventoryViewModel
import kotlinx.coroutines.launch

/**
 * AI 扫描屏幕
 * 上传图片，使用 AI 识别拼豆颜色
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIScanScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToCanvas: (String) -> Unit = {},
    inventoryViewModel: InventoryViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isRecognizing by remember { mutableStateOf(false) }
    var recognitionResult by remember { mutableStateOf<List<RecognizedColor>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var gridSize by remember { mutableStateOf(32) }
    var useLocalRecognition by remember { mutableStateOf(false) }
    
    val aiManager = remember { AIManager(context) }
    val config = remember { aiManager.getConfig() }
    
    // 检查 API 配置
    val isApiConfigured = config.isConfigured()
    
    // 图片选择器
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedImageUri = it
            // 加载图片
            try {
                context.contentResolver.openInputStream(it)?.use { inputStream ->
                    val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                    selectedBitmap = bitmap
                }
            } catch (e: Exception) {
                Toast.makeText(context, "图片加载失败", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    // 识别颜色
    fun recognizeColors() {
        val bitmap = selectedBitmap ?: return
        
        scope.launch {
            isRecognizing = true
            errorMessage = null
            recognitionResult = emptyList()
            
            val result = if (useLocalRecognition) {
                aiManager.localColorRecognition(bitmap, gridSize)
            } else {
                aiManager.recognizeColors(bitmap, gridSize)
            }
            
            if (result.success) {
                recognitionResult = result.colors
                Toast.makeText(context, "识别成功！识别到 ${result.colors.size} 种颜色", Toast.LENGTH_SHORT).show()
            } else {
                errorMessage = result.errorMessage
            }
            
            isRecognizing = false
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI 扫描") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                },
                actions = {
                    if (recognitionResult.isNotEmpty()) {
                        IconButton(onClick = { /* 保存项目 */ }) {
                            Icon(Icons.Default.Save, "保存")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // API 配置提示
            if (!isApiConfigured && !useLocalRecognition) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "API 未配置",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    "请先在设置中配置 AI API，或使用本地识别",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Switch(
                                checked = useLocalRecognition,
                                onCheckedChange = { useLocalRecognition = it }
                            )
                        }
                    }
                }
            }
            
            // 图片选择区域
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "步骤 1: 选择图片",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        if (selectedBitmap != null) {
                            // 显示选中的图片
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.outline,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { imagePickerLauncher.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    bitmap = selectedBitmap!!.asImageBitmap(),
                                    contentDescription = "选中的图片",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
                                IconButton(
                                    onClick = { imagePickerLauncher.launch("image/*") },
                                    modifier = Modifier.align(Alignment.TopEnd)
                                ) {
                                    Icon(
                                        Icons.Default.Refresh,
                                        contentDescription = "重新选择",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        } else {
                            // 选择图片占位
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { imagePickerLauncher.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Default.AddPhotoAlternate,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "点击选择图片",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        "支持 PNG, JPG 等格式",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // 设置区域
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "步骤 2: 设置参数",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // 画布尺寸
                        Text(
                            "目标画布尺寸",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(16, 32, 48, 64).forEach { size ->
                                FilterChip(
                                    selected = gridSize == size,
                                    onClick = { gridSize = size },
                                    label = { Text("${size}×$size") }
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // 识别方式
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "使用本地识别",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    "无需 API，快速但精度较低",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = useLocalRecognition,
                                onCheckedChange = { useLocalRecognition = it }
                            )
                        }
                    }
                }
            }
            
            // 识别按钮
            item {
                Button(
                    onClick = { recognizeColors() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = selectedBitmap != null && !isRecognizing
                ) {
                    if (isRecognizing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("识别中...")
                    } else {
                        Icon(Icons.Default.AutoAwesome, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("开始识别")
                    }
                }
            }
            
            // 错误信息
            if (errorMessage != null) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                errorMessage!!,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
            
            // 识别结果
            if (recognitionResult.isNotEmpty()) {
                item {
                    Text(
                        "识别结果 (${recognitionResult.size} 种颜色)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                items(recognitionResult) { color ->
                    RecognitionColorItem(color = color)
                }
                
                // 创建项目按钮
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { /* 导出材料清单 */ },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.ListAlt, null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("导出清单")
                        }
                        Button(
                            onClick ={ /* 创建项目 */ },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Add, null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("创建项目")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecognitionColorItem(color: RecognizedColor) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 颜色预览
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(parseColor(color.colorHex))
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = color.colorName ?: "未知颜色",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = color.colorHex,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // 进度条
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = color.percentage / 100f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = parseColor(color.colorHex),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    "${color.count}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "颗",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    String.format("%.1f%%", color.percentage),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * 解析颜色字符串为 Color
 */
private fun parseColor(hex: String?): Color {
    if (hex.isNullOrBlank()) return Color.Gray
    return try {
        val cleanHex = hex.removePrefix("#")
        val colorInt = cleanHex.toLong(16).toInt()
        Color(colorInt or 0xFF000000.toInt())
    } catch (e: Exception) {
        Color.Gray
    }
}
