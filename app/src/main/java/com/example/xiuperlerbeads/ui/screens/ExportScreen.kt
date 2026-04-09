package com.example.xiuperlerbeads.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.xiuperlerbeads.data.export.ExportManager
import com.example.xiuperlerbeads.domain.model.BeadUsage
import com.example.xiuperlerbeads.ui.viewmodel.InventoryViewModel

/**
 * 导出屏幕
 * 支持导出 PNG、PDF、材料清单
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(
    projectId: String,
    onNavigateBack: () -> Unit = {},
    inventoryViewModel: InventoryViewModel = viewModel()
) {
    val state by inventoryViewModel.state.collectAsState()
    val context = LocalContext.current
    
    val project = remember(state.projects, projectId) {
        state.projects.find { it.id == projectId }
    }
    
    var isExporting by remember { mutableStateOf(false) }
    
    // 初始化 ExportManager
    val exportManager = remember { ExportManager(context) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("导出") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (project == null) {
            // 项目不存在
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "项目不存在",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 项目信息卡片
                item {
                    ProjectInfoCard(
                        name = project.name,
                        colorCount = project.beadUsage.size,
                        totalBeads = project.beadUsage.sumOf { it.quantity },
                        date = project.date
                    )
                }
                
                // 导出选项
                item {
                    Text(
                        "导出格式",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                // PNG 导出
                item {
                    ExportOptionCard(
                        icon = Icons.Default.Image,
                        title = "PNG 图片",
                        description = "请在画布编辑器中使用导出功能，以保留完整图案数据",
                        onClick = {
                            Toast.makeText(context, "请在画布编辑器中导出 PNG 图案", Toast.LENGTH_LONG).show()
                        },
                        isLoading = false
                    )
                }
                
                // PDF 导出
                item {
                    ExportOptionCard(
                        icon = Icons.Default.PictureAsPdf,
                        title = "PDF 材料图纸",
                        description = "导出包含材料清单的文件",
                        onClick = {
                            isExporting = true
                            val content = exportManager.exportMaterialList(project, project.beadUsage) { code -> inventoryViewModel.getColorInfo(code) }
                            val uri = exportManager.saveTextToFile(content, "${project.name}_materials_pdf")
                            if (uri != null) {
                                val i = exportManager.shareContent(uri, "text/plain", "${project.name} 材料图纸")
                                context.startActivity(Intent.createChooser(i, "导出材料图纸"))
                            } else {
                                Toast.makeText(context, "导出失败", Toast.LENGTH_SHORT).show()
                            }
                            isExporting = false
                        },
                        isLoading = isExporting
                    )
                }
                
                // 材料清单
                item {
                    ExportOptionCard(
                        icon = Icons.Default.ListAlt,
                        title = "材料清单",
                        description = "导出所需材料的文字清单",
                        onClick = {
                            isExporting = true
                            try {
                                val materialList = exportManager.exportMaterialList(
                                    project = project,
                                    beadUsage = project.beadUsage,
                                    getColorInfo = { code -> inventoryViewModel.getColorInfo(code) }
                                )
                                val uri = exportManager.saveTextToFile(
                                    content = materialList,
                                    fileName = "${project.name}_materials"
                                )
                                if (uri != null) {
                                    val shareIntent = exportManager.shareContent(uri, "text/plain", "${project.name} 材料清单")
                                    context.startActivity(Intent.createChooser(shareIntent, "分享材料清单"))
                                } else {
                                    Toast.makeText(context, "导出失败", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "导出失败: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                            isExporting = false
                        },
                        isLoading = isExporting
                    )
                }
                
                // 批量导出
                item {
                    ExportOptionCard(
                        icon = Icons.Default.FolderZip,
                        title = "全部导出",
                        description = "导出材料清单（PNG 图案请在画布中导出）",
                        onClick = {
                            isExporting = true
                            val c = exportManager.exportMaterialList(project, project.beadUsage) { inventoryViewModel.getColorInfo(it) }
                            val u = exportManager.saveTextToFile(c, "${project.name}_all_export")
                            isExporting = false
                            if (u != null) context.startActivity(Intent.createChooser(exportManager.shareContent(u, "text/plain", "${project.name} 导出"), "全部导出"))
                            else Toast.makeText(context, "导出失败", Toast.LENGTH_SHORT).show()
                        },
                        isLoading = isExporting
                    )
                }
                
                // 材料预览
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "材料预览",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                // 材料列表
                items(project.beadUsage.sortedByDescending { it.quantity }.take(10)) { usage ->
                    val colorInfo = inventoryViewModel.getColorInfo(usage.colorCode)
                    MaterialItem(
                        mardCode = colorInfo?.mardCode ?: usage.colorCode,
                        colorName = colorInfo?.colorName ?: "-",
                        quantity = usage.quantity,
                        colorPreview = colorInfo?.let { 
                            Color(it.red, it.green, it.blue) 
                        } ?: Color.Gray
                    )
                }
                
                if (project.beadUsage.size > 10) {
                    item {
                        Text(
                            "... 还有 ${project.beadUsage.size - 10} 种颜色",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }
        }
        
        // 加载指示器
        if (isExporting) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun ProjectInfoCard(
    name: String,
    colorCount: Int,
    totalBeads: Int,
    date: Long
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                InfoChip(
                    icon = Icons.Default.Palette,
                    text = "$colorCount 种颜色"
                )
                InfoChip(
                    icon = Icons.Default.GridOn,
                    text = "$totalBeads 颗"
                )
            }
        }
    }
}

@Composable
private fun InfoChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ExportOptionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    isLoading: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isLoading) { onClick() }
    ) {
        Row(
modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
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
private fun MaterialItem(
    mardCode: String,
    colorName: String,
    quantity: Int,
    colorPreview: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(colorPreview)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    mardCode,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    colorName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Text(
                "${quantity}颗",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
