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
    
    val templates: List<TemplateItem> = remember {
        listOf(
            // 动物
            TemplateItem("cat_16", "小猫咪", TemplateCategory.ANIMALS, 0xFFFFB3C1L, 16),
            TemplateItem("dog_16", "小狗狗", TemplateCategory.ANIMALS, 0xFFD4A76AL, 16),
            TemplateItem("panda_20", "熊猫", TemplateCategory.ANIMALS, 0xFFE8E8E8L, 20),
            TemplateItem("rabbit_16", "小兔子", TemplateCategory.ANIMALS, 0xFFF4C2C2L, 16),
            TemplateItem("fox_20", "小狐狸", TemplateCategory.ANIMALS, 0xFFFF8C42L, 20),
            TemplateItem("penguin_20", "企鹅", TemplateCategory.ANIMALS, 0xFF4A4A6AL, 20),
            TemplateItem("duck_16", "小鸭子", TemplateCategory.ANIMALS, 0xFFFFD700L, 16),
            TemplateItem("owl_24", "猫头鹰", TemplateCategory.ANIMALS, 0xFF8B5E3CL, 24),
            TemplateItem("deer_24", "小鹿", TemplateCategory.ANIMALS, 0xFFC68642L, 24),
            TemplateItem("frog_16", "青蛙", TemplateCategory.ANIMALS, 0xFF5DBB63L, 16),
            // 人物
            TemplateItem("girl_20", "少女", TemplateCategory.CHARACTERS, 0xFFFFB3DEL, 20),
            TemplateItem("boy_20", "少年", TemplateCategory.CHARACTERS, 0xFF87CEEBL, 20),
            TemplateItem("princess_24", "公主", TemplateCategory.CHARACTERS, 0xFFFFD1DCL, 24),
            TemplateItem("knight_24", "骑士", TemplateCategory.CHARACTERS, 0xFF708090L, 24),
            TemplateItem("witch_20", "小女巫", TemplateCategory.CHARACTERS, 0xFF9B59B6L, 20),
            TemplateItem("astronaut_24", "宇航员", TemplateCategory.CHARACTERS, 0xFF34495EL, 24),
            // 植物
            TemplateItem("rose_16", "玫瑰花", TemplateCategory.PLANTS, 0xFFFF6B6BL, 16),
            TemplateItem("sunflower_20", "向日葵", TemplateCategory.PLANTS, 0xFFFFD93DL, 20),
            TemplateItem("cactus_16", "仙人掌", TemplateCategory.PLANTS, 0xFF6BCB77L, 16),
            TemplateItem("mushroom_16", "蘑菇", TemplateCategory.PLANTS, 0xFFFF6348L, 16),
            TemplateItem("clover_16", "四叶草", TemplateCategory.PLANTS, 0xFF2ED573L, 16),
            TemplateItem("cherry_blossom_20", "樱花", TemplateCategory.PLANTS, 0xFFFFB7C5L, 20),
            TemplateItem("tree_20", "圣诞树", TemplateCategory.PLANTS, 0xFF2ECC71L, 20),
            // 美食
            TemplateItem("cake_16", "蛋糕", TemplateCategory.FOODS, 0xFFFF9FF3L, 16),
            TemplateItem("icecream_16", "冰淇淋", TemplateCategory.FOODS, 0xFFFFD8A8L, 16),
            TemplateItem("strawberry_16", "草莓", TemplateCategory.FOODS, 0xFFFF4757L, 16),
            TemplateItem("watermelon_20", "西瓜", TemplateCategory.FOODS, 0xFF7BED9FL, 20),
            TemplateItem("donut_16", "甜甜圈", TemplateCategory.FOODS, 0xFFFFA502L, 16),
            TemplateItem("pizza_20", "披萨", TemplateCategory.FOODS, 0xFFECCC68L, 20),
            TemplateItem("sushi_16", "寿司", TemplateCategory.FOODS, 0xFFF8F8F8L, 16),
            TemplateItem("ramen_20", "拉面", TemplateCategory.FOODS, 0xFFFF6348L, 20),
            // 物品
            TemplateItem("star_16", "星星", TemplateCategory.OBJECTS, 0xFFFFD700L, 16),
            TemplateItem("heart_16", "爱心", TemplateCategory.OBJECTS, 0xFFFF4757L, 16),
            TemplateItem("diamond_16", "钻石", TemplateCategory.OBJECTS, 0xFF74B9FFL, 16),
            TemplateItem("crown_20", "皇冠", TemplateCategory.OBJECTS, 0xFFFFD700L, 20),
            TemplateItem("moon_16", "月亮", TemplateCategory.OBJECTS, 0xFFFFF3CDL, 16),
            TemplateItem("rocket_20", "火箭", TemplateCategory.OBJECTS, 0xFF6C5CE7L, 20),
            TemplateItem("house_20", "小房子", TemplateCategory.OBJECTS, 0xFFEDC5ABL, 20),
            TemplateItem("gift_16", "礼物盒", TemplateCategory.OBJECTS, 0xFFFF7675L, 16),
            // 图案
            TemplateItem("checkerboard_16", "棋盘格", TemplateCategory.PATTERNS, 0xFF2D3436L, 16),
            TemplateItem("stripe_h_20", "横条纹", TemplateCategory.PATTERNS, 0xFF0984E3L, 20),
            TemplateItem("stripe_v_20", "竖条纹", TemplateCategory.PATTERNS, 0xFF00B894L, 20),
            TemplateItem("polka_dot_20", "圆点图案", TemplateCategory.PATTERNS, 0xFFE17055L, 20),
            TemplateItem("zigzag_20", "锯齿纹", TemplateCategory.PATTERNS, 0xFF6C5CE7L, 20),
            TemplateItem("pixel_art_24", "像素艺术框", TemplateCategory.PATTERNS, 0xFFDFE6E9L, 24),
            TemplateItem("gradient_32", "渐变色块", TemplateCategory.PATTERNS, 0xFF81ECECL, 32),
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
                selectedTabIndex = if (selectedCategory == null) 0
                    else TemplateCategory.entries.indexOf(selectedCategory) + 1,
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
            
            if (filteredTemplates.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Collections,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "素材库正在建设中",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "敬请期待更多精美拼豆图案",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            } else {
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
}

@Composable
private fun TemplateCard(
    template: TemplateItem,
    onClick: () -> Unit
) {
    val categoryIcon = when (template.category) {
        TemplateCategory.ANIMALS -> Icons.Default.Pets
        TemplateCategory.CHARACTERS -> Icons.Default.Person
        TemplateCategory.PLANTS -> Icons.Default.LocalFlorist
        TemplateCategory.FOODS -> Icons.Default.LocalDining
        TemplateCategory.OBJECTS -> Icons.Default.Star
        TemplateCategory.PATTERNS -> Icons.Default.GridOn
    }

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
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        categoryIcon,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.85f)
                    )
                    Spacer(Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.25f)
                    ) {
                        Text(
                            text = "${template.size}×${template.size}",
                            style = MaterialTheme.typography.labelSmall,
                            color = androidx.compose.ui.graphics.Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // 信息区域
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = template.name,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = template.category.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
