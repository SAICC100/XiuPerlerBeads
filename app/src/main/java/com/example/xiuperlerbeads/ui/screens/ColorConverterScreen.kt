package com.example.xiuperlerbeads.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.xiuperlerbeads.domain.model.BeadColor
import com.example.xiuperlerbeads.domain.model.BeadColorManager
import com.example.xiuperlerbeads.domain.model.BrandStock
import com.example.xiuperlerbeads.domain.model.ColorSystem
import com.example.xiuperlerbeads.ui.viewmodel.InventoryViewModel

/**
 * 色号转换工具
 * 输入任意品牌色号，查看对应的所有其他品牌色号映射，并标注库存状态
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorConverterScreen(
    onNavigateBack: () -> Unit,
    inventoryViewModel: InventoryViewModel = viewModel()
) {
    val state by inventoryViewModel.state.collectAsState()
    val focusManager = LocalFocusManager.current

    var inputCode by remember { mutableStateOf("") }
    var selectedSystem by remember { mutableStateOf(ColorSystem.MARD) }
    var searchResult by remember { mutableStateOf<BeadColor?>(null) }
    var hasSearched by remember { mutableStateOf(false) }
    var systemMenuExpanded by remember { mutableStateOf(false) }

    // 执行查找
    fun doSearch() {
        val query = inputCode.trim()
        searchResult = if (query.isNotEmpty()) {
            BeadColorManager.findByCode(query, selectedSystem)
                ?: BeadColorManager.findByAnyCode(query)
        } else null
        hasSearched = query.isNotEmpty()
        focusManager.clearFocus()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("色号转换", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // ── 搜索区域 ─────────────────────────────────────────────────────
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "输入色号",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))

                        // 色号体系选择器
                        ExposedDropdownMenuBox(
                            expanded = systemMenuExpanded,
                            onExpandedChange = { systemMenuExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = selectedSystem.displayName,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("色号品牌") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = systemMenuExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = systemMenuExpanded,
                                onDismissRequest = { systemMenuExpanded = false }
                            ) {
                                ColorSystem.entries.forEach { system ->
                                    DropdownMenuItem(
                                        text = { Text(system.displayName) },
                                        onClick = {
                                            selectedSystem = system
                                            systemMenuExpanded = false
                                            // 重新搜索
                                            if (inputCode.isNotBlank()) doSearch()
                                        },
                                        leadingIcon = if (system == selectedSystem) {
                                            { Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                                        } else null
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // 色号输入框
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = inputCode,
                                onValueChange = {
                                    inputCode = it.uppercase()
                                    if (it.isBlank()) {
                                        searchResult = null
                                        hasSearched = false
                                    }
                                },
                                label = { Text("色号（如 A01、B12）") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Characters,
                                    imeAction = ImeAction.Search
                                ),
                                keyboardActions = KeyboardActions(onSearch = { doSearch() }),
                                trailingIcon = {
                                    if (inputCode.isNotEmpty()) {
                                        IconButton(onClick = {
                                            inputCode = ""
                                            searchResult = null
                                            hasSearched = false
                                        }) {
                                            Icon(Icons.Default.Clear, contentDescription = "清空")
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            FilledIconButton(
                                onClick = { doSearch() },
                                modifier = Modifier.size(56.dp)
                            ) {
                                Icon(Icons.Default.Search, contentDescription = "查找")
                            }
                        }
                    }
                }
            }

            // ── 搜索结果 ──────────────────────────────────────────────────────
            when {
                searchResult != null -> {
                    val color = searchResult!!
                    val composeColor = color.toComposeColor()

                    // 颜色预览卡片
                    item {
                        ColorPreviewCard(
                            color = color,
                            composeColor = composeColor,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }

                    item {
                        Text(
                            "各品牌对应色号",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    // 各品牌色号映射列表
                    val mappings = buildColorMappings(color, state.stocks)
                    items(mappings) { mapping ->
                        ColorMappingRow(
                            mapping = mapping,
                            composeColor = composeColor,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                        )
                    }
                }

                hasSearched -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.SearchOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "未找到色号「$inputCode」",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "请确认色号和品牌是否正确",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                else -> {
                    item {
                        // 使用提示
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.SwapHoriz,
                                contentDescription = null,
                                modifier = Modifier.size(56.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "输入任意品牌的色号",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "查看对应的所有品牌色号及库存状态",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── 数据类 ────────────────────────────────────────────────────────────────────

data class ColorMapping(
    val system: ColorSystem,
    val code: String,          // 对应品牌的色号（空表示无映射）
    val stockStatus: StockStatus
)

enum class StockStatus {
    SUFFICIENT,   // 充足（>=低库存阈值）
    LOW,          // 低库存
    OUT,          // 缺货（available <= 0）
    NOT_TRACKED   // 未持有（无库存记录）
}

// ── 私有辅助函数 ──────────────────────────────────────────────────────────────

private fun buildColorMappings(
    color: BeadColor,
    stocks: List<BrandStock>
): List<ColorMapping> {
    // 根据 mardCode 在所有品牌库存中查找持有状态
    val stockByBrand: Map<String, StockStatus> = stocks
        .filter { it.mardCode == color.mardCode && !it.isHidden }
        .associate { stock ->
            stock.brandId to when {
                stock.available <= 0 -> StockStatus.OUT
                stock.isLowStock(100) -> StockStatus.LOW
                else -> StockStatus.SUFFICIENT
            }
        }

    return ColorSystem.entries.mapNotNull { system ->
        val code = when (system) {
            ColorSystem.MARD -> color.mardCode
            ColorSystem.COCO -> color.cocoCode
            ColorSystem.MANMAN -> color.manmanCode
            ColorSystem.KAKA -> color.kakaCode
            ColorSystem.PANPAN -> color.panpanCode
            ColorSystem.MIXIAOWO -> color.mixiaowoCode
        }
        if (code.isEmpty()) return@mapNotNull null

        // 在已持有的品牌中找是否持有该色号（按品牌名无法直接对应，以库存中是否存在 mardCode 判断）
        // 实际库存状态：如果任意品牌持有该 mardCode，则显示最优状态
        val bestStatus = if (stockByBrand.isEmpty()) {
            StockStatus.NOT_TRACKED
        } else {
            stockByBrand.values.minByOrNull { it.ordinal } ?: StockStatus.NOT_TRACKED
        }

        ColorMapping(system = system, code = code, stockStatus = bestStatus)
    }
}

// ── Composable 组件 ───────────────────────────────────────────────────────────

@Composable
private fun ColorPreviewCard(
    color: BeadColor,
    composeColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(composeColor)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    color.mardCode,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (color.colorName.isNotEmpty()) {
                    Text(
                        color.colorName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    "RGB(${color.red}, ${color.green}, ${color.blue})",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ColorMappingRow(
    mapping: ColorMapping,
    composeColor: Color,
    modifier: Modifier = Modifier
) {
    val (statusColor, statusText) = when (mapping.stockStatus) {
        StockStatus.SUFFICIENT -> MaterialTheme.colorScheme.primary to "充足"
        StockStatus.LOW -> MaterialTheme.colorScheme.tertiary to "低库存"
        StockStatus.OUT -> MaterialTheme.colorScheme.error to "缺货"
        StockStatus.NOT_TRACKED -> MaterialTheme.colorScheme.onSurfaceVariant to "未持有"
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 色块
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(composeColor)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), CircleShape)
            )
            Spacer(Modifier.width(12.dp))

            // 品牌名
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    mapping.system.displayName,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    mapping.code,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }

            // 库存状态标签
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = statusColor.copy(alpha = 0.12f)
            ) {
                Text(
                    statusText,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = statusColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
