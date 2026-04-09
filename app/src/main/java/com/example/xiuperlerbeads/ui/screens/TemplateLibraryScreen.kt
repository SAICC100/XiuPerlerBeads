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
    
    val templates: List<TemplateItem> = emptyList()

    val filteredTemplates = remember(selectedCategory, searchQuery) {
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
