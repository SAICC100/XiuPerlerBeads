package com.example.xiuperlerbeads.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.xiuperlerbeads.domain.model.ProjectRecord
import com.example.xiuperlerbeads.ui.viewmodel.InventoryViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * 成品日历页
 * 月历视图展示每日完成的作品，点击查看当日详情
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompletionCalendarScreen(
    onNavigateBack: () -> Unit,
    inventoryViewModel: InventoryViewModel = viewModel()
) {
    val state by inventoryViewModel.state.collectAsState()

    // 已完成的项目（有 completedDate）
    val completedProjects = remember(state.projects) {
        state.projects.filter { it.completedDate != null }
    }

    // 当前显示的年月
    var displayCalendar by remember {
        mutableStateOf(Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
        })
    }

    // 选中日期
    var selectedDate by remember { mutableStateOf<Calendar?>(null) }

    // 当月按日期分组的项目
    val projectsByDay = remember(completedProjects, displayCalendar) {
        buildProjectsByDay(completedProjects, displayCalendar)
    }

    // 选中日期的项目
    val selectedDayProjects = remember(selectedDate, completedProjects) {
        selectedDate?.let { cal ->
            completedProjects.filter { p ->
                p.completedDate?.let { ts ->
                    val c = Calendar.getInstance().apply { timeInMillis = ts }
                    c.get(Calendar.YEAR) == cal.get(Calendar.YEAR) &&
                    c.get(Calendar.MONTH) == cal.get(Calendar.MONTH) &&
                    c.get(Calendar.DAY_OF_MONTH) == cal.get(Calendar.DAY_OF_MONTH)
                } ?: false
            }
        } ?: emptyList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("成品日历") },
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
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // 月份切换栏
            item {
                MonthNavigator(
                    calendar = displayCalendar,
                    onPreviousMonth = {
                        displayCalendar = Calendar.getInstance().apply {
                            timeInMillis = displayCalendar.timeInMillis
                            add(Calendar.MONTH, -1)
                            set(Calendar.DAY_OF_MONTH, 1)
                        }
                        selectedDate = null
                    },
                    onNextMonth = {
                        displayCalendar = Calendar.getInstance().apply {
                            timeInMillis = displayCalendar.timeInMillis
                            add(Calendar.MONTH, 1)
                            set(Calendar.DAY_OF_MONTH, 1)
                        }
                        selectedDate = null
                    }
                )
            }

            // 当月完成统计
            item {
                val monthCount = projectsByDay.values.sumOf { it.size }
                if (monthCount > 0) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Default.EmojiEvents,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                "本月共完成 $monthCount 件作品",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            // 星期标题行
            item {
                WeekdayHeader()
            }

            // 日历格子
            item {
                CalendarGrid(
                    calendar = displayCalendar,
                    projectsByDay = projectsByDay,
                    selectedDate = selectedDate,
                    onDateSelected = { cal ->
                        selectedDate = if (selectedDate?.let {
                            it.get(Calendar.YEAR) == cal.get(Calendar.YEAR) &&
                            it.get(Calendar.MONTH) == cal.get(Calendar.MONTH) &&
                            it.get(Calendar.DAY_OF_MONTH) == cal.get(Calendar.DAY_OF_MONTH)
                        } == true) null else cal
                    }
                )
            }

            // 选中日期的详情列表
            if (selectedDate != null && selectedDayProjects.isNotEmpty()) {
                item {
                    val fmt = SimpleDateFormat("M月d日", Locale.CHINESE)
                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "${fmt.format(Date(selectedDate!!.timeInMillis))} 完成的作品",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
                items(selectedDayProjects) { project ->
                    CompletedProjectRow(project = project)
                }
            } else if (selectedDate != null) {
                item {
                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "该天暂无完成作品",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            // 空状态
            if (completedProjects.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.CalendarMonth,
                                contentDescription = null,
                                modifier = Modifier.size(56.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                            Text(
                                "还没有完成的作品",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "完成项目后会在日历上显示",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── 月份导航栏 ──────────────────────────────────────────────────────────────

@Composable
private fun MonthNavigator(
    calendar: Calendar,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val sdf = SimpleDateFormat("yyyy年 M月", Locale.CHINESE)
    val today = Calendar.getInstance()
    val isCurrentMonth = calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                         calendar.get(Calendar.MONTH) == today.get(Calendar.MONTH)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(Icons.Default.ChevronLeft, contentDescription = "上个月")
        }
        Text(
            sdf.format(Date(calendar.timeInMillis)),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        IconButton(
            onClick = onNextMonth,
            enabled = !isCurrentMonth
        ) {
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "下个月",
                tint = if (!isCurrentMonth) LocalContentColor.current
                       else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        }
    }
}

// ── 星期标题 ─────────────────────────────────────────────────────────────────

@Composable
private fun WeekdayHeader() {
    val days = listOf("日", "一", "二", "三", "四", "五", "六")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        days.forEach { d ->
            Text(
                d,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ── 日历网格 ─────────────────────────────────────────────────────────────────

@Composable
private fun CalendarGrid(
    calendar: Calendar,
    projectsByDay: Map<Int, List<ProjectRecord>>,
    selectedDate: Calendar?,
    onDateSelected: (Calendar) -> Unit
) {
    val today = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)

    // 计算本月第一天是星期几（0=周日）
    val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val totalCells = firstDayOfWeek + daysInMonth
    val rows = (totalCells + 6) / 7

    Column(modifier = Modifier.padding(horizontal = 8.dp)) {
        repeat(rows) { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(7) { col ->
                    val cellIndex = row * 7 + col
                    val day = cellIndex - firstDayOfWeek + 1
                    val isValid = day in 1..daysInMonth

                    if (!isValid) {
                        Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                    } else {
                        val dayProjects = projectsByDay[day] ?: emptyList()
                        val isToday = today.get(Calendar.YEAR) == year &&
                                      today.get(Calendar.MONTH) == month &&
                                      today.get(Calendar.DAY_OF_MONTH) == day
                        val isSelected = selectedDate?.let {
                            it.get(Calendar.YEAR) == year &&
                            it.get(Calendar.MONTH) == month &&
                            it.get(Calendar.DAY_OF_MONTH) == day
                        } ?: false

                        CalendarDay(
                            day = day,
                            projectCount = dayProjects.size,
                            isToday = isToday,
                            isSelected = isSelected,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                val cal = Calendar.getInstance().apply {
                                    set(year, month, day)
                                }
                                onDateSelected(cal)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDay(
    day: Int,
    projectCount: Int,
    isToday: Boolean,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bgColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isToday -> MaterialTheme.colorScheme.primaryContainer
        else -> Color.Transparent
    }
    val textColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        isToday -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "$day",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                color = textColor
            )
            if (projectCount > 0) {
                Spacer(Modifier.height(1.dp))
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.primary
                        )
                )
            }
        }
    }
}

// ── 完成作品行 ────────────────────────────────────────────────────────────────

@Composable
private fun CompletedProjectRow(project: ProjectRecord) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 缩略图或占位图
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                if (project.thumbnailBase64 != null) {
                    // 有缩略图时可解码 Base64 显示，此处用占位符代替
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Image,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                } else {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.GridOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    project.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (project.totalBeads > 0) {
                        Text(
                            "${project.totalBeads} 颗",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (project.colorCount > 0) {
                        Text(
                            "${project.colorCount} 色",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ── 工具函数 ──────────────────────────────────────────────────────────────────

private fun buildProjectsByDay(
    projects: List<ProjectRecord>,
    calendar: Calendar
): Map<Int, List<ProjectRecord>> {
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val result = mutableMapOf<Int, MutableList<ProjectRecord>>()

    for (project in projects) {
        val ts = project.completedDate ?: continue
        val cal = Calendar.getInstance().apply { timeInMillis = ts }
        if (cal.get(Calendar.YEAR) == year && cal.get(Calendar.MONTH) == month) {
            val day = cal.get(Calendar.DAY_OF_MONTH)
            result.getOrPut(day) { mutableListOf() }.add(project)
        }
    }
    return result
}
