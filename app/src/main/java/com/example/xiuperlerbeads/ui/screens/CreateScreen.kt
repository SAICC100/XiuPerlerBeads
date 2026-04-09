@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.xiuperlerbeads.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateScreen(
    onNavigateToCanvas: (Long) -> Unit,
    onNavigateToImport: () -> Unit,
    onNavigateToTemplateLibrary: () -> Unit = {}
) {
    var showNewProjectDialog by remember { mutableStateOf(false) }
    var selectedSize by remember { mutableStateOf(32) }
    var projectName by remember { mutableStateOf("我的拼豆") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "创作中心", 
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
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
            Text(
                "选择创作方式",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            // Hand Draw Option
            CreateOptionCard(
                icon = Icons.Default.Brush,
                title = "手绘画布",
                description = "从零开始自由绘制像素图案",
                onClick = { showNewProjectDialog = true }
            )
            
            // Import Image Option
            CreateOptionCard(
                icon = Icons.Default.Image,
                title = "图片转像素",
                description = "导入图片自动转换为拼豆图纸",
                onClick = onNavigateToImport
            )
            
            // Templates Option
            CreateOptionCard(
                icon = Icons.Default.Collections,
                title = "素材库",
                description = "动物 · 人物 · 植物 · 更多",
                onClick = onNavigateToTemplateLibrary
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Tips
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "小贴士",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "新手建议从32×32开始练习",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
    
    // New Project Dialog
    if (showNewProjectDialog) {
        NewProjectDialog(
            projectName = projectName,
            onProjectNameChange = { projectName = it },
            selectedSize = selectedSize,
            onSizeSelected = { selectedSize = it },
            onDismiss = { showNewProjectDialog = false },
            onConfirm = {
                // Create new project and navigate
                showNewProjectDialog = false
                onNavigateToCanvas(System.currentTimeMillis())
            }
        )
    }
}

@Composable
private fun CreateOptionCard(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NewProjectDialog(
    projectName: String,
    onProjectNameChange: (String) -> Unit,
    selectedSize: Int,
    onSizeSelected: (Int) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val sizes = listOf(16, 24, 32, 48, 64, 96, 128, 256)
    val sizeDescriptions = mapOf(
        16 to "小型钥匙扣",
        24 to "小型挂件",
        32 to "中型图案",
        48 to "中型摆件",
        64 to "大型图案",
        96 to "大型壁画",
        128 to "超大尺寸",
        256 to "专业级"
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.GridOn, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("新建项目")
            }
        },
        text = {
            Column {
                OutlinedTextField(
value = projectName,
                    onValueChange = onProjectNameChange,
                    label = { Text("项目名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    "画布尺寸",
                    style = MaterialTheme.typography.labelLarge
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(200.dp)
                ) {
                    items(sizes) { size ->
                        FilterChip(
                            selected = selectedSize == size,
                            onClick = { onSizeSelected(size) },
                            label = {
                                Text(
                                    "${size}×${size}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "建议: ${sizeDescriptions[selectedSize] ?: ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
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
