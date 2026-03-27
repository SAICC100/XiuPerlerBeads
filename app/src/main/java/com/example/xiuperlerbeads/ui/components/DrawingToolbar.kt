package com.example.xiuperlerbeads.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.xiuperlerbeads.domain.model.DrawingTool

/**
 * Drawing toolbar with tool selection
 */
@Composable
fun DrawingToolbar(
    selectedTool: DrawingTool,
    onToolSelected: (DrawingTool) -> Unit,
    brushSize: Int,
    onBrushSizeChanged: (Int) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    canUndo: Boolean,
    canRedo: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(8.dp)
    ) {
        // Tool selection row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ToolButton(
                icon = Icons.Default.Brush,
                label = "画笔",
                isSelected = selectedTool == DrawingTool.BRUSH,
                onClick = { onToolSelected(DrawingTool.BRUSH) },
                enabled = canUndo
            )
            ToolButton(
                icon = Icons.Default.Circle,
                label = "橡皮擦",
                isSelected = selectedTool == DrawingTool.ERASER,
                onClick = { onToolSelected(DrawingTool.ERASER) },
                enabled = canUndo
            )
            ToolButton(
                icon = Icons.Default.FormatColorFill,
                label = "填充",
                isSelected = selectedTool == DrawingTool.FILL,
                onClick = { onToolSelected(DrawingTool.FILL) },
                enabled = canUndo
            )

            VerticalDivider(modifier = Modifier.height(32.dp))

            IconButton(
                onClick = onUndo,
                enabled = canUndo
            ) {
                Icon(
                    imageVector = Icons.Default.Undo,
                    contentDescription = "撤销"
                )
            }
            IconButton(
                onClick = onRedo,
                enabled = canRedo
            ) {
                Icon(
                    imageVector = Icons.Default.Redo,
                    contentDescription = "重做"
                )
            }
        }

        // Brush size slider (only show when brush is selected)
        if (selectedTool == DrawingTool.BRUSH) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "画笔大小:",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.width(8.dp))
                Slider(
                    value = brushSize.toFloat(),
                    onValueChange = { onBrushSizeChanged(it.toInt()) },
                    valueRange = 1f..5f,
                    steps = 3,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "$brushSize",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.width(24.dp)
                )
            }
        }
    }
}

@Composable
private fun ToolButton(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else Color.Transparent
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = when {
                !enabled -> Color.Gray.copy(alpha = 0.5f)
                isSelected -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurface
            }
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = when {
                !enabled -> Color.Gray.copy(alpha = 0.5f)
                isSelected -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurface
            }
        )
    }
}

/**
 * Canvas size selector
 */
@Composable
fun CanvasSizeSelector(
    selectedSize: Int,
    onSizeSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val sizes = listOf(16, 32, 48, 64, 128, 256)

    Column(modifier = modifier) {
        Text(
            text = "画布尺寸",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(8.dp)
        )

        sizes.chunked(3).forEach { rowSizes ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowSizes.forEach { size ->
                    FilterChip(
                        selected = size == selectedSize,
                        onClick = { onSizeSelected(size) },
                        label = { Text("${size}×$size") },
                        modifier = Modifier.weight(1f)
                    )
                }
                // Fill remaining space if row is incomplete
                repeat(3 - rowSizes.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

/**
 * Action buttons for project operations
 */
@Composable
fun ProjectActions(
    onExportPng: () -> Unit,
    onExportPdf: () -> Unit,
    onShowStatistics: () -> Unit,
    onReduceColors: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "操作",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(8.dp)
        )

        ActionButton(
            text = "导出PNG",
            icon = Icons.Default.Image,
            onClick = onExportPng
        )
        ActionButton(
            text = "导出PDF",
            icon = Icons.Default.PictureAsPdf,
            onClick = onExportPdf
        )
        ActionButton(
            text = "色号统计",
            icon = Icons.Default.PieChart,
            onClick = onShowStatistics
        )
        ActionButton(
            text = "减少颜色",
            icon = Icons.Default.Palette,
            onClick = onReduceColors
        )
    }
}

@Composable
private fun ActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text)
    }
}
