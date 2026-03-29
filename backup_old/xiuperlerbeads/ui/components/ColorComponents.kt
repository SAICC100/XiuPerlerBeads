package com.example.xiuperlerbeads.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.xiuperlerbeads.domain.model.PerlerColor
import com.example.xiuperlerbeads.domain.model.PerlerColorPalette

/**
 * Color picker component for selecting Perler bead colors
 */
@Composable
fun ColorPicker(
    selectedColor: PerlerColor?,
    onColorSelected: (PerlerColor) -> Unit,
    modifier: Modifier = Modifier,
    colors: List<PerlerColor> = PerlerColorPalette.allColors
) {
    Column(modifier = modifier) {
        // Selected color preview
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "当前颜色:",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.width(8.dp))
            if (selectedColor != null) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(selectedColor.toComposeColor())
                        .border(2.dp, Color.Gray, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${selectedColor.name} (${selectedColor.colorCode})",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Text(
                    text = "橡皮擦模式",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }

        Divider()

        // Color grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(8),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 300.dp),
            contentPadding = PaddingValues(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(colors) { color ->
                ColorItem(
                    color = color,
                    isSelected = color == selectedColor,
                    onClick = { onColorSelected(color) }
                )
            }
        }
    }
}

@Composable
private fun ColorItem(
    color: PerlerColor,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(color.toComposeColor())
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) Color(0xFF6B5CEB) else Color.Gray,
                shape = CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = if (isLightColor(color)) Color.Black else Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * Helper function to determine if a color is light or dark
 */
private fun isLightColor(color: PerlerColor): Boolean {
    val luminance = (0.299 * color.red + 0.587 * color.green + 0.114 * color.blue) / 255
    return luminance > 0.5
}

/**
 * Compact color picker for toolbar
 */
@Composable
fun CompactColorPicker(
    selectedColor: PerlerColor?,
    onColorSelected: (PerlerColor) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        // Current color button
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(selectedColor?.toComposeColor() ?: Color.LightGray)
                .border(2.dp, Color.Gray, RoundedCornerShape(8.dp))
                .clickable { expanded = true },
            contentAlignment = Alignment.Center
        ) {
            if (selectedColor == null) {
                Text(
                    text = "×",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
            }
        }

        // Dropdown color picker
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            ColorPicker(
                selectedColor = selectedColor,
                onColorSelected = {
                    onColorSelected(it)
                    expanded = false
                },
                modifier = Modifier.width(280.dp)
            )
        }
    }
}

/**
 * Color statistics display
 */
@Composable
fun ColorStatisticsPanel(
    statistics: List<com.example.xiuperlerbeads.domain.model.ColorStatistics>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "色号统计",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(8.dp)
        )

        Divider()

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(statistics) { stat ->
                ColorStatItem(stat = stat)
            }
        }
    }
}

@Composable
private fun ColorStatItem(
    stat: com.example.xiuperlerbeads.domain.model.ColorStatistics
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(stat.color.toComposeColor())
                .border(1.dp, Color.Gray, CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stat.color.name,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = stat.color.colorCode,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }
        Text(
            text = "${stat.count}",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
