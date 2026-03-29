package com.example.xiuperlerbeads.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
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
import com.example.xiuperlerbeads.domain.model.BeadColor
import com.example.xiuperlerbeads.domain.model.BeadColorManager
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageImportScreen(
    onNavigateBack: () -> Unit = {},
    onImportComplete: (projectId: Long) -> Unit = {}
) {
    val context = LocalContext.current
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var targetSize by remember { mutableIntStateOf(32) }
    var isProcessing by remember { mutableStateOf(false) }
    var showPreview by remember { mutableStateOf(false) }
    var processedColors by remember { mutableStateOf<List<ColorPreview>>(emptyList()) }
    
    // Image picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                context.contentResolver.openInputStream(it)?.use { stream ->
                    val bitmap = BitmapFactory.decodeStream(stream)
                    selectedBitmap = bitmap
                    showPreview = false
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("图片转像素") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (selectedBitmap == null) {
                // No image selected
                ImageSelectionPrompt(
                    onSelectImage = { imagePickerLauncher.launch("image/*") }
                )
            } else {
                // Image selected
                if (isProcessing) {
                    ProcessingIndicator()
                } else if (showPreview && processedColors.isNotEmpty()) {
                    PreviewResult(
                        bitmap = selectedBitmap!!,
                        targetSize = targetSize,
                        colors = processedColors,
                        onBackToSelection = {
                            selectedBitmap = null
                            showPreview = false
                            processedColors = emptyList()
                        },
                        onSizeChange = { newSize ->
                            targetSize = newSize
                        },
                        onConfirm = {
                            // Create project with the image
                            onImportComplete(System.currentTimeMillis())
                        }
                    )
                } else {
                    ImagePreviewWithSettings(
                        bitmap = selectedBitmap!!,
                        targetSize = targetSize,
                        onSizeChange = { targetSize = it },
                        onSelectNew = { imagePickerLauncher.launch("image/*") },
                        onProcess = {
                            isProcessing = true
                            // Process image in background
                            processedColors = processImage(selectedBitmap!!, targetSize)
                            isProcessing = false
                            showPreview = true
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ImageSelectionPrompt(
    onSelectImage: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSelectImage() },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.AddPhotoAlternate,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "选择图片",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "从相册选择一张图片\n将其转换为拼豆图案",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onSelectImage) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("选择图片")
                }
            }
        }
    }
}

@Composable
private fun ProcessingIndicator() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "正在处理图片...",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            "分析颜色并匹配拼豆色号",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImagePreviewWithSettings(
    bitmap: Bitmap,
    targetSize: Int,
    onSizeChange: (Int) -> Unit,
    onSelectNew: () -> Unit,
    onProcess: () -> Unit
) {
    val sizes = listOf(16, 24, 32, 48, 64, 96, 128)
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Image Preview
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Selected image",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Fit
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Size Selection
        Text(
            "画布尺寸",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            sizes.forEach { size ->
                FilterChip(
                    selected = targetSize == size,
                    onClick = { onSizeChange(size) },
                    label = { Text("${size}×${size}") }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            "尺寸越大，细节越多，但需要的拼豆也越多",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onSelectNew,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("重新选择")
            }
            Button(
                onClick = onProcess,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("转换")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PreviewResult(
    bitmap: Bitmap,
    targetSize: Int,
    colors: List<ColorPreview>,
    onBackToSelection: () -> Unit,
    onSizeChange: (Int) -> Unit,
    onConfirm: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Preview
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "转换预览",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                // Downsampled preview
                val scaledBitmap = remember(bitmap, targetSize) {
                    Bitmap.createScaledBitmap(bitmap, targetSize, targetSize, true)
                }
                
                Image(
                    bitmap = scaledBitmap.asImageBitmap(),
                    contentDescription = "Preview",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Fit
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "${colors.sumOf { it.count }}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "总拼豆数",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "${colors.size}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "颜色种类",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "${targetSize}×${targetSize}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "画布尺寸",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Color List
        Text(
            "颜色分布",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            LazyColumn(
                contentPadding = PaddingValues(8.dp)
            ) {
                items(colors.sortedByDescending { it.count }) { color ->
                    ColorRow(color = color)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Size adjustment
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("调整尺寸:", style = MaterialTheme.typography.bodySmall)
            FilterChip(
                selected = targetSize == 32,
                onClick = { onSizeChange(32) },
                label = { Text("32") }
            )
            FilterChip(
                selected = targetSize == 48,
                onClick = { onSizeChange(48) },
                label = { Text("48") }
            )
            FilterChip(
                selected = targetSize == 64,
                onClick = { onSizeChange(64) },
                label = { Text("64") }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onBackToSelection,
                modifier = Modifier.weight(1f)
            ) {
                Text("重新选择")
            }
            Button(
                onClick = onConfirm,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("创建项目")
            }
        }
    }
}

@Composable
private fun ColorRow(color: ColorPreview) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color.color)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = color.mardCode,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            if (color.colorName.isNotEmpty()) {
                Text(
                    text = color.colorName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Text(
            text = "${color.count}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Process image and extract colors
 */
private fun processImage(bitmap: Bitmap, targetSize: Int): List<ColorPreview> {
    // Downsample image
    val scaledBitmap = Bitmap.createScaledBitmap(bitmap, targetSize, targetSize, true)
    
    // Group pixels by color
    val colorCounts = mutableMapOf<Int, Int>()
    
    for (y in 0 until scaledBitmap.height) {
        for (x in 0 until scaledBitmap.width) {
            val pixel = scaledBitmap.getPixel(x, y)
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF
            // Quantize to reduce color variations
            val qr = (r / 16) * 16
            val qg = (g / 16) * 16
            val qb = (b / 16) * 16
            val quantized = (0xFF shl 24) or (qr shl 16) or (qg shl 8) or qb
            colorCounts[quantized] = (colorCounts[quantized] ?: 0) + 1
        }
    }
    
    // Find closest bead colors
    return colorCounts.entries
        .sortedByDescending { it.value }
        .take(20) // Limit to 20 colors
        .mapNotNull { (pixel, count) ->
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF
            
            val beadColor = BeadColorManager.findClosestColor(r, g, b)
            beadColor?.let {
                ColorPreview(
                    color = it.toComposeColor(),
                    mardCode = it.mardCode,
                    colorName = it.colorName,
                    count = count
                )
            }
        }
}

data class ColorPreview(
    val color: Color,
    val mardCode: String,
    val colorName: String,
    val count: Int
)

private fun BeadColor.toComposeColor(): Color = Color(red, green, blue)
