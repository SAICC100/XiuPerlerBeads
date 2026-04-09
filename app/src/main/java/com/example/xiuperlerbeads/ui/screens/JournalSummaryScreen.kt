package com.example.xiuperlerbeads.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.xiuperlerbeads.domain.model.JournalCollection
import com.example.xiuperlerbeads.domain.model.JournalEntry
import com.example.xiuperlerbeads.domain.model.LocationStat
import com.example.xiuperlerbeads.domain.model.TagExpenseStat
import com.example.xiuperlerbeads.ui.viewmodel.JournalViewModel
import com.example.xiuperlerbeads.ui.viewmodel.SummaryState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalSummaryScreen(
    journalViewModel: JournalViewModel = viewModel()
) {
    val journalState by journalViewModel.state.collectAsStateWithLifecycle()
    val summaryState by journalViewModel.summaryState.collectAsStateWithLifecycle()

    // 当前日历年月
    val today = remember { Calendar.getInstance() }
    var calYear by remember { mutableIntStateOf(today.get(Calendar.YEAR)) }
    var calMonth by remember { mutableIntStateOf(today.get(Calendar.MONTH) + 1) }

    // 选中的日期（点击日历时赋值）
    var selectedDay by remember { mutableStateOf<Int?>(null) }
    var selectedDayEntries by remember { mutableStateOf(listOf<JournalEntry>()) }
    var showDayDetail by remember { mutableStateOf(false) }

    // 顶部 Tab 索引：0=日历  1=地点  2=花费
    var tabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("日历", "地点", "花费")

    // 日历有记录的天
    val daysWithEntries by remember(calYear, calMonth) {
        derivedStateOf { journalViewModel.getDaysWithEntries(calYear, calMonth) }
    }

    LaunchedEffect(Unit) {
        journalViewModel.loadSummaryData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("汇总", fontWeight = FontWeight.Bold) },
                actions = {
                    // 日期范围筛选（花费 Tab 有效）
                    if (tabIndex == 2) {
                        IconButton(onClick = { /* TODO: 日期筛选弹窗 */ }) {
                            Icon(Icons.Default.FilterList, contentDescription = "筛选")
                        }
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
            // 文集标签切换
            CollectionTabBar2(
                collections = journalState.collections,
                selectedId = summaryState.selectedCollectionId,
                onSelect = { journalViewModel.setSummaryCollection(it) }
            )

            // 功能 Tab
            TabRow(
                selectedTabIndex = tabIndex,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = tabIndex == index,
                        onClick = { tabIndex = index },
                        text = { Text(title, style = MaterialTheme.typography.labelLarge) }
                    )
                }
            }

            when (tabIndex) {
                0 -> CalendarTab(
                    year = calYear,
                    month = calMonth,
                    daysWithEntries = daysWithEntries,
                    onPrevMonth = {
                        if (calMonth == 1) { calYear--; calMonth = 12 } else calMonth--
                    },
                    onNextMonth = {
                        if (calMonth == 12) { calYear++; calMonth = 1 } else calMonth++
                    },
                    onDayClick = { day ->
                        selectedDay = day
                        selectedDayEntries = journalViewModel.getEntriesByDate(calYear, calMonth, day)
                        showDayDetail = true
                    }
                )
                1 -> LocationTab(locationStats = summaryState.locationStats, isLoading = summaryState.isLoading)
                2 -> ExpenseTab(tagExpenseStats = summaryState.tagExpenseStats, totalExpense = summaryState.totalExpense, isLoading = summaryState.isLoading)
            }
        }
    }

    // 日期详情弹窗
    if (showDayDetail && selectedDay != null) {
        DayDetailDialog(
            year = calYear,
            month = calMonth,
            day = selectedDay!!,
            entries = selectedDayEntries,
            onDismiss = { showDayDetail = false }
        )
    }
}

// ============================================================================
// 文集标签栏（简化版，不带添加按钮）
// ============================================================================

@Composable
private fun CollectionTabBar2(
    collections: List<JournalCollection>,
    selectedId: String,
    onSelect: (String) -> Unit
) {
    if (collections.size <= 1) return
    ScrollableTabRow(
        selectedTabIndex = collections.indexOfFirst { it.id == selectedId }.coerceAtLeast(0),
        edgePadding = 12.dp,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        collections.forEachIndexed { index, collection ->
            Tab(
                selected = collection.id == selectedId,
                onClick = { onSelect(collection.id) },
                text = {
                    Text(
                        collection.name,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (collection.id == selectedId)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
        }
    }
}

// ============================================================================
// Tab 1: 日历视图
// ============================================================================

@Composable
private fun CalendarTab(
    year: Int,
    month: Int,
    daysWithEntries: Set<Int>,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDayClick: (Int) -> Unit
) {
    val monthNames = listOf("", "1月", "2月", "3月", "4月", "5月", "6月",
        "7月", "8月", "9月", "10月", "11月", "12月")
    val today = Calendar.getInstance()
    val isCurrentMonth = today.get(Calendar.YEAR) == year && today.get(Calendar.MONTH) + 1 == month
    val todayDay = today.get(Calendar.DAY_OF_MONTH)

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        // 月份导航
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onPrevMonth) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "上个月")
            }
            Text(
                "$year年 ${monthNames[month]}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onNextMonth) {
                Icon(Icons.Default.ChevronRight, contentDescription = "下个月")
            }
        }

        Spacer(Modifier.height(8.dp))

        // 星期标题
        val weekDays = listOf("日", "一", "二", "三", "四", "五", "六")
        Row(modifier = Modifier.fillMaxWidth()) {
            weekDays.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (day == "日" || day == "六")
                        MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        // 计算该月第一天是星期几
        val firstDayCal = Calendar.getInstance().apply {
            set(year, month - 1, 1)
        }
        val firstDayOfWeek = firstDayCal.get(Calendar.DAY_OF_WEEK) - 1 // 0=日
        val daysInMonth = firstDayCal.getActualMaximum(Calendar.DAY_OF_MONTH)

        // 渲染日历格子
        val totalCells = firstDayOfWeek + daysInMonth
        val rows = (totalCells + 6) / 7

        for (row in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0 until 7) {
                    val cellIndex = row * 7 + col
                    val day = cellIndex - firstDayOfWeek + 1

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (day in 1..daysInMonth) {
                            val hasEntry = daysWithEntries.contains(day)
                            val isToday = isCurrentMonth && day == todayDay

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isToday -> MaterialTheme.colorScheme.primary
                                            hasEntry -> MaterialTheme.colorScheme.primaryContainer
                                            else -> Color.Transparent
                                        }
                                    )
                                    .clickable(enabled = hasEntry || isToday) { onDayClick(day) },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = day.toString(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = when {
                                            isToday -> Color.White
                                            hasEntry -> MaterialTheme.colorScheme.onPrimaryContainer
                                            else -> MaterialTheme.colorScheme.onSurface
                                        },
                                        fontWeight = if (isToday || hasEntry) FontWeight.Bold else FontWeight.Normal
                                    )
                                    // 有记录时显示小点
                                    if (hasEntry && !isToday) {
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// Tab 2: 地点统计
// ============================================================================

@Composable
private fun LocationTab(locationStats: List<LocationStat>, isLoading: Boolean) {
    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (locationStats.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.LocationOff, contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(12.dp))
                Text("暂无地点记录", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        return
    }

    val maxCount = locationStats.maxOf { it.count }.toFloat()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("最常去地点",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
        }
        items(locationStats.take(20)) { stat ->
            LocationStatRow(stat = stat, maxCount = maxCount, rank = locationStats.indexOf(stat) + 1)
        }
    }
}

@Composable
private fun LocationStatRow(stat: LocationStat, maxCount: Float, rank: Int) {
    val progress = if (maxCount > 0) stat.count / maxCount else 0f
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        // 排名
        Text(
            text = rank.toString(),
            style = MaterialTheme.typography.labelLarge,
            color = when (rank) {
                1 -> Color(0xFFFFD700)
                2 -> Color(0xFFC0C0C0)
                3 -> Color(0xFFCD7F32)
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.width(24.dp),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(stat.location,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Text("${stat.count}次",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                color = MaterialTheme.colorScheme.tertiary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

// ============================================================================
// Tab 3: 花费统计
// ============================================================================

@Composable
private fun ExpenseTab(
    tagExpenseStats: List<TagExpenseStat>,
    totalExpense: Double,
    isLoading: Boolean
) {
    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (tagExpenseStats.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.MoneyOff, contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(12.dp))
                Text("暂无花费记录", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 总计卡片
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("总花费",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text(
                            "¥ ${String.format("%.2f", totalExpense)}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        // 饼图（Canvas 实现）
        item {
            Text("各标签花费",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            ExpensePieChart(stats = tagExpenseStats, total = totalExpense)
        }

        // 标签明细列表
        item {
            Text("明细",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold)
        }
        items(tagExpenseStats) { stat ->
            TagExpenseRow(stat = stat, total = totalExpense)
        }
    }
}

@Composable
private fun ExpensePieChart(stats: List<TagExpenseStat>, total: Double) {
    if (total <= 0) return

    // 简单条形图代替饼图（避免 Canvas 复杂度）
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            stats.forEach { stat ->
                val fraction = (stat.totalExpense / total).toFloat()
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(stat.tag.toComposeColor())
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(stat.tag.name,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .width(120.dp)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(fraction)
                                .background(stat.tag.toComposeColor())
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text("${(fraction * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.width(32.dp),
                        textAlign = TextAlign.End)
                }
            }
        }
    }
}

@Composable
private fun TagExpenseRow(stat: TagExpenseStat, total: Double) {
    val fraction = if (total > 0) (stat.totalExpense / total * 100).toInt() else 0
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(stat.tag.toComposeColor())
        )
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(stat.tag.name, style = MaterialTheme.typography.bodyMedium)
            Text("${stat.entryCount}条记录",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                "¥ ${String.format("%.2f", stat.totalExpense)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text("$fraction%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ============================================================================
// 日期详情弹窗
// ============================================================================

@Composable
private fun DayDetailDialog(
    year: Int,
    month: Int,
    day: Int,
    entries: List<JournalEntry>,
    onDismiss: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${year}年${month}月${day}日") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (entries.isEmpty()) {
                    Text("当天暂无记录", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    entries.forEach { entry ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    dateFormat.format(Date(entry.displayTime)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                entry.location?.let { loc ->
                                    Spacer(Modifier.width(8.dp))
                                    Icon(Icons.Default.LocationOn, contentDescription = null,
                                        tint = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.size(12.dp))
                                    Text(loc,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.tertiary)
                                }
                                if (entry.expense > 0) {
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "¥${String.format("%.0f", entry.expense)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                            if (entry.content.isNotBlank()) {
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    entry.content,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        HorizontalDivider(thickness = 0.5.dp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("关闭") }
        }
    )
}
