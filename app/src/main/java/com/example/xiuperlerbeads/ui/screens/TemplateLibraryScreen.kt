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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * 素材分类
 */
enum class TemplateCategory(val displayName: String, val emoji: String) {
    ANIMALS("动物", ""),
    CHARACTERS("人物", ""),
    PLANTS("植物", ""),
    FOODS("美食", ""),
    OBJECTS("物品", ""),
    PATTERNS("图案", "")
}

/**
 * 素材模板
 */
data class TemplateItem(
    val id: String,
    val name: String,
    val category: TemplateCategory,
    val previewColor: Long, // 预览颜色
    val size: Int = 32
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateLibraryScreen(
    onNavigateBack: () -> Unit = {},
    onTemplateSelected: (TemplateItem) -> Unit = {}
) {
    var selectedCategory by remember { mutableStateOf<TemplateCategory?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    
    // 模拟素材数据
    val templates = remember {
        listOf(
            // 动物
            TemplateItem("1", "小兔子", TemplateCategory.ANIMALS, 0xFFFFB6C1),
            TemplateItem("2", "小猫", TemplateCategory.ANIMALS, 0xFFFFA500),
            TemplateItem("3", "小狗", TemplateCategory.ANIMALS, 0xFFDEB887),
            TemplateItem("4", "小熊", TemplateCategory.ANIMALS, 0xFF8B4513),
            TemplateItem("5", "熊猫", TemplateCategory.ANIMALS, 0xFFFFFFFF),
            TemplateItem("6", "狐狸", TemplateCategory.ANIMALS, 0xFFFF6600),
            TemplateItem("7", "猫咪", TemplateCategory.ANIMALS, 0xFFFFB6C1),
            TemplateItem("8", "小鸟", TemplateCategory.ANIMALS, 0xFF87CEEB),
            // 人物
            TemplateItem("9", "小女孩", TemplateCategory.CHARACTERS, 0xFFFFC0CB),
            TemplateItem("10", "小男孩", TemplateCategory.CHARACTERS, 0xFFADD8E6),
            TemplateItem("11", "爷爷", TemplateCategory.CHARACTERS, 0xFFDCDCDC),
            TemplateItem("12", "奶奶", TemplateCategory.CHARACTERS, 0xFFFFE4E1),
            // 植物
            TemplateItem("13", "向日葵", TemplateCategory.PLANTS, 0xFFFFD700),
            TemplateItem("14", "玫瑰", TemplateCategory.PLANTS, 0xFFFF0000),
            TemplateItem("15", "樱花", TemplateCategory.PLANTS, 0xFFFFB7C5),
            TemplateItem("16", "多肉", TemplateCategory.PLANTS, 0xFF90EE90),
            // 美食
            TemplateItem("17", "蛋糕", TemplateCategory.FOODS, 0xFFFFB6C1),
            TemplateItem("18", "冰淇淋", TemplateCategory.FOODS, 0xFFFFA07A),
            TemplateItem("19", "披萨", TemplateCategory.FOODS, 0xFFFFD700),
            TemplateItem("20", "汉堡", TemplateCategory.FOODS, 0xFF8B4513),
            // 物品
            TemplateItem("21", "爱心", TemplateCategory.OBJECTS, 0xFFFF0000),
            TemplateItem("22", "星星", TemplateCategory.OBJECTS, 0xFFFFFF00),
            TemplateItem("23", "彩虹", TemplateCategory.OBJECTS, 0xFFFF00FF),
            TemplateItem("24", "汽车", TemplateCategory.OBJECTS, 0xFFFF0000),
            // 图案
            TemplateItem("25", "方格", TemplateCategory.PATTERNS, 0xFF0000FF),
            TemplateItem("26", "条纹", TemplateCategory.PATTERNS, 0xFFFF0000),
            TemplateItem("27", "波点", TemplateCategory.PATTERNS, 0xFFFF69B4),
            TemplateItem("28", "心形", TemplateCategory.PATTERNS, 0xFFFF1493),
        )
    }
    
    val filteredTemplates = remember(selectedCategory, searchQuery, templates) {
        templates.filter { template ->
            (selectedCategory == null || template.category == selectedCategory) &&
            (searchQuery.isEmpty() || template.name.contains(searchQuery, ignoreCase = true))
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("素材库") },
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
        ) {
            // 搜索栏
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("搜索素材...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true,
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, "清除")
                        }
                    }
                }
            )
            
            // 分类标签
            ScrollableTabRow(
                selectedTabIndex = TemplateCategory.entries.indexOf(selectedCategory),
                modifier = Modifier.fillMaxWidth(),
                edgePadding = 16.dp
            ) {
                Tab(
                    selected = selectedCategory == null,
                    onClick = { selectedCategory = null },
                    text = { Text("全部") }
                )
                TemplateCategory.entries.forEach { category ->
                    Tab(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        text = { Text(category.displayName) }
                    )
                }
            }
            
            // 素材网格
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredTemplates) { template ->
                    TemplateCard(
                        template = template,
                        onClick = { onTemplateSelected(template) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TemplateCard(
    template: TemplateItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 预览区域
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(
                        androidx.compose.ui.graphics.Color(template.previewColor),
                        RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.GridOn,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.7f)
                )
            }
            
            // 信息区域
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = template.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${template.size}×${template.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
