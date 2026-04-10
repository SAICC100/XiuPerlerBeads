package com.example.xiuperlerbeads.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// ─── 数据模型 ────────────────────────────────────────────────────────────────

private data class TutorialChapter(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val steps: List<TutorialStep>
)

private data class TutorialStep(
    val title: String,
    val content: String
)

private data class FaqItem(
    val question: String,
    val answer: String,
    val tag: String = ""
)

// ─── 内置数据 ────────────────────────────────────────────────────────────────

private val TUTORIAL_CHAPTERS = listOf(
    TutorialChapter(
        id = "start",
        title = "快速入门",
        icon = Icons.Default.RocketLaunch,
        steps = listOf(
            TutorialStep(
                "第一步：创建品牌",
                "进入「我的」→「品牌管理」，点击右下角「+」按钮，填写品牌名称并选择色号体系（MARD / COCO / 漫漫 / 卡卡等），完成后系统会自动为该品牌初始化所有标准色号的库存条目。"
            ),
            TutorialStep(
                "第二步：设置初始库存",
                "进入「库存」Tab，选择品牌后可看到所有色号。点击任意色号卡片进行编辑，输入实际库存数量。也可以用「批量设置」一次性更新多个色号。"
            ),
            TutorialStep(
                "第三步：创建第一个项目",
                "进入「创作」Tab，点击「新建」。可以手绘像素图、从图片导入或从模板库选取。创建完成后，项目会显示在「项目」列表中。"
            ),
            TutorialStep(
                "第四步：导出材料清单",
                "打开任意项目 → 右上角菜单 → 导出。选择「材料清单」格式，即可获得该作品所需的各色号豆子数量，方便对照库存补货。"
            )
        )
    ),
    TutorialChapter(
        id = "inventory",
        title = "库存管理",
        icon = Icons.Default.Inventory,
        steps = listOf(
            TutorialStep(
                "库存数字含义",
                "每个色号卡片显示三个数字：\n• 库存量：当前拥有的总颗数\n• 可用量：库存量减去所有项目「已分配」的用量\n• 状态：充足（绿）/ 低库存（橙）/ 缺货（红）\n\n低库存阈值可在品牌设置中自定义。"
            ),
            TutorialStep(
                "搜索与筛选",
                "「库存」页顶部搜索框支持按色号代码或颜色名称模糊搜索。右上角漏斗图标可切换「仅显示低库存」模式，快速定位需要补货的色号。"
            ),
            TutorialStep(
                "隐藏不常用色号",
                "长按色号卡片 → 隐藏。隐藏的色号不再显示在主列表中，但库存数据保留。可通过「品牌管理」→「管理隐藏色号」找回并取消隐藏。"
            ),
            TutorialStep(
                "自定义色号",
                "如果某品牌有标准色号体系之外的特殊色号，可以在「品牌管理」→「自定义色号」中添加，支持设置色号代码、名称和 HEX 颜色值。"
            ),
            TutorialStep(
                "查看操作历史与撤销",
                "「我的」→「用量统计」→「历史记录」Tab，可以看到所有库存变更记录。对于入库/出库/更新操作，点击记录右侧的「撤销」按钮可恢复到变更前的值。"
            )
        )
    ),
    TutorialChapter(
        id = "create",
        title = "创作功能",
        icon = Icons.Default.Palette,
        steps = listOf(
            TutorialStep(
                "画布操作",
                "双指捏合/展开可缩放画布；单指拖动可平移；选择颜色后单击格子涂色；右上角橡皮擦工具可清除颜色。画布大小在新建时选择，创建后无法更改。"
            ),
            TutorialStep(
                "从图片导入",
                "「创作」→「图片导入」，选取手机相册中的图片，系统会自动将其像素化并映射到最近的豆豆色号。可调整像素密度和色号品牌进行预览。"
            ),
            TutorialStep(
                "AI 扫描识别",
                "「创作」→「AI 扫描」，拍摄现有拼豆作品的照片，AI 会识别各区域的颜色并生成对应的色号分配方案。识别结果可继续在画布上编辑。"
            ),
            TutorialStep(
                "模板库",
                "「创作」→「模板库」提供 47 个内置像素图模板，分为动物、人物、植物、美食、物品、图案 6 大类别。点击模板即可在画布中使用并自由修改。"
            ),
            TutorialStep(
                "色号转换",
                "「我的」→「色号转换」，输入任意品牌的色号代码，可查看其在所有其他品牌中的对应色号，并标注当前库存状态。适合跨品牌换料时快速比对。"
            )
        )
    ),
    TutorialChapter(
        id = "project",
        title = "项目管理",
        icon = Icons.Default.GridView,
        steps = listOf(
            TutorialStep(
                "项目状态流转",
                "项目共 5 种状态：草稿 → 进行中 → 已完成 → 已归档 / 已放弃。在项目详情页右上角菜单可切换状态。完成时会记录完成日期，并显示在「成品日历」中。"
            ),
            TutorialStep(
                "成品日历",
                "「我的」→「成品日历」以月历格式展示每天完成的作品。有完成记录的日期会显示圆点标记，点击日期可查看当天完成的作品列表。"
            ),
            TutorialStep(
                "导出格式说明",
                "• PNG：导出画布的像素图，适合预览分享\n• PDF：导出带色号标注的高清图，适合打印参考\n• 材料清单：导出各色号所需颗数的文本清单，方便购买备料"
            )
        )
    ),
    TutorialChapter(
        id = "data",
        title = "数据管理",
        icon = Icons.Default.Storage,
        steps = listOf(
            TutorialStep(
                "备份与恢复",
                "「我的」→「备份与恢复」，点击「立即备份」可手动创建快照。系统最多保留 10 个版本，超出后自动删除最早的。每个快照显示时间、标签和数据统计，可随时一键恢复。"
            ),
            TutorialStep(
                "导入数据",
                "「我的」→「设置」→「导入数据」，选择 JSON 格式的备份文件。导入前会显示预览，包含品牌数、色号数、项目数及冲突情况。可选择「合并」（保留现有数据+新增）或「覆盖」（完全替换）。"
            ),
            TutorialStep(
                "导出数据",
                "「设置」→「导出数据」，将所有库存、项目、历史记录导出为 JSON 文件，可发送到任意位置保存或迁移到新设备。"
            ),
            TutorialStep(
                "购买订单追踪",
                "「我的」→「运输中订单」，记录已下单但未到货的豆子。到货后点击「确认收货」，系统会自动将数量加入对应品牌的库存并生成入库记录。"
            )
        )
    )
)

private val FAQ_LIST = listOf(
    FaqItem(
        question = "添加品牌后为什么库存都是 0？",
        answer = "新建品牌时，初始库存默认为 0（实际持有量未知）。请进入「库存」Tab，选中该品牌后逐一填写实际库存，或使用「批量设置」功能快速初始化。",
        tag = "库存"
    ),
    FaqItem(
        question = "可用量和库存量有什么区别？",
        answer = "库存量是你实际拥有的颗数。可用量 = 库存量 - 所有「进行中」项目已分配的用量。如果可用量为负，说明库存不足以支撑当前的项目需求。",
        tag = "库存"
    ),
    FaqItem(
        question = "色号转换找不到对应色号怎么办？",
        answer = "部分特殊色号或新出的颜色可能没有跨品牌对应。可以在「品牌管理」→「自定义色号」中手动添加该色号，并填写 HEX 颜色值便于识别。",
        tag = "色号"
    ),
    FaqItem(
        question = "导入数据时选「合并」还是「覆盖」？",
        answer = "• 合并：现有数据保留，只添加新文件中有而当前没有的内容，同名品牌/重复色号会跳过。适合从旧备份补充数据。\n• 覆盖：完全替换当前所有数据，操作不可撤销。适合在新设备初始化或修复数据错误。",
        tag = "数据"
    ),
    FaqItem(
        question = "备份快照最多保留几个？",
        answer = "最多保留 10 个快照版本。手动备份超过 10 个后，系统会自动删除时间最早的快照。建议定期在重要操作前手动备份并填写标签说明。",
        tag = "数据"
    ),
    FaqItem(
        question = "历史记录的撤销有什么限制？",
        answer = "只有「入库」「出库」「更新库存」三种操作支持撤销，且只能恢复到变更前的值。品牌删除、项目操作、购买记录等不支持撤销。建议重要操作前先创建快照备份。",
        tag = "历史"
    ),
    FaqItem(
        question = "画布创建后能改变大小吗？",
        answer = "画布尺寸（如 16×16、32×32）在创建时确定，之后无法直接修改。如需更改，可以新建一个目标尺寸的画布，然后参照原图手动重绘，或使用「图片导入」功能重新生成。",
        tag = "创作"
    ),
    FaqItem(
        question = "AI 扫描识别精度不高怎么办？",
        answer = "建议在光线均匀的环境下拍摄，避免阴影和反光。拍摄时让作品平铺，相机垂直对准。识别后可以在画布上手动修正错误的色号，识别结果仅作为参考底图。",
        tag = "创作"
    ),
    FaqItem(
        question = "如何把数据迁移到新手机？",
        answer = "在旧手机：「设置」→「导出数据」，将 JSON 文件发送到新手机（邮件、微信等均可）。在新手机安装 App 后：「设置」→「导入数据」，选择 JSON 文件，选择「覆盖」模式完成迁移。",
        tag = "数据"
    ),
    FaqItem(
        question = "模板库的模板可以修改吗？",
        answer = "可以。点击模板库中的模板后，会在画布中打开，之后可以像普通项目一样自由修改颜色、添加细节。模板只是提供初始的像素布局，不影响最终效果。",
        tag = "创作"
    ),
    FaqItem(
        question = "品牌合并后能恢复吗？",
        answer = "品牌合并操作不可撤销——合并完成后源品牌会被删除，库存累加到目标品牌。建议合并前先手动创建一个备份快照（「备份与恢复」→「立即备份」），以便需要时恢复。",
        tag = "品牌"
    ),
    FaqItem(
        question = "成品日历只显示已完成的项目吗？",
        answer = "是的，成品日历只显示状态为「已完成」且有完成日期的项目。将项目状态修改为「已完成」时，系统会自动记录当天日期。如需修改完成日期，可以在项目详情页编辑。",
        tag = "项目"
    )
)

// ─── 主 Screen ───────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpCenterScreen(onNavigateBack: () -> Unit = {}) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("教程", "FAQ")

    // 搜索过滤
    val filteredChapters = remember(searchQuery) {
        if (searchQuery.isBlank()) TUTORIAL_CHAPTERS
        else TUTORIAL_CHAPTERS.filter { chapter ->
            chapter.title.contains(searchQuery, ignoreCase = true) ||
                chapter.steps.any {
                    it.title.contains(searchQuery, ignoreCase = true) ||
                        it.content.contains(searchQuery, ignoreCase = true)
                }
        }
    }
    val filteredFaq = remember(searchQuery) {
        if (searchQuery.isBlank()) FAQ_LIST
        else FAQ_LIST.filter {
            it.question.contains(searchQuery, ignoreCase = true) ||
                it.answer.contains(searchQuery, ignoreCase = true) ||
                it.tag.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("帮助中心") },
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
            // 搜索框
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("搜索教程和常见问题…") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "清除")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Tab
            if (searchQuery.isBlank()) {
                TabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) }
                        )
                    }
                }
            }

            // 内容
            if (searchQuery.isNotBlank()) {
                // 搜索结果：同时展示教程步骤和 FAQ
                SearchResultContent(
                    chapters = filteredChapters,
                    faqItems = filteredFaq,
                    query = searchQuery
                )
            } else {
                when (selectedTab) {
                    0 -> TutorialContent(chapters = TUTORIAL_CHAPTERS)
                    1 -> FaqContent(faqItems = FAQ_LIST)
                }
            }
        }
    }
}

// ─── 教程内容 ─────────────────────────────────────────────────────────────────

@Composable
private fun TutorialContent(chapters: List<TutorialChapter>) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(chapters, key = { it.id }) { chapter ->
            TutorialChapterCard(chapter = chapter)
        }
    }
}

@Composable
private fun TutorialChapterCard(chapter: TutorialChapter) {
    var expanded by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column {
            // 章节标题行
            ListItem(
                headlineContent = {
                    Text(chapter.title, fontWeight = FontWeight.SemiBold)
                },
                supportingContent = {
                    Text("${chapter.steps.size} 个步骤")
                },
                leadingContent = {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Icon(
                            chapter.icon,
                            contentDescription = null,
                            modifier = Modifier.padding(8.dp).size(20.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                },
                trailingContent = {
                    Icon(
                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "收起" else "展开"
                    )
                },
                modifier = Modifier.clickable { expanded = !expanded }
            )

            // 步骤列表（展开时显示）
            AnimatedVisibility(visible = expanded) {
                Column {
                    HorizontalDivider(thickness = 0.5.dp)
                    chapter.steps.forEachIndexed { index, step ->
                        TutorialStepItem(index = index + 1, step = step)
                        if (index < chapter.steps.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                thickness = 0.5.dp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TutorialStepItem(index: Int, step: TutorialStep) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 步骤序号圆圈
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.size(28.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = index.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = step.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = step.content,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ─── FAQ 内容 ─────────────────────────────────────────────────────────────────

@Composable
private fun FaqContent(faqItems: List<FaqItem>) {
    // 按标签分组
    val groups = faqItems.groupBy { it.tag }
    val tagOrder = listOf("库存", "色号", "创作", "项目", "品牌", "历史", "数据")

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tagOrder.forEach { tag ->
            val items = groups[tag] ?: return@forEach
            item {
                Text(
                    tag,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            items(items, key = { it.question }) { faq ->
                FaqCard(faq = faq)
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun FaqCard(faq: FaqItem) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (expanded)
                MaterialTheme.colorScheme.surfaceVariant
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.clickable { expanded = !expanded }) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.QuestionAnswer,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = faq.question,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column {
                    HorizontalDivider(thickness = 0.5.dp)
                    Text(
                        text = faq.answer,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }
            }
        }
    }
}

// ─── 搜索结果 ─────────────────────────────────────────────────────────────────

@Composable
private fun SearchResultContent(
    chapters: List<TutorialChapter>,
    faqItems: List<FaqItem>,
    query: String
) {
    val matchedSteps = chapters.flatMap { chapter ->
        chapter.steps
            .filter {
                it.title.contains(query, ignoreCase = true) ||
                    it.content.contains(query, ignoreCase = true)
            }
            .map { chapter to it }
    }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (matchedSteps.isNotEmpty()) {
            item {
                Text(
                    "教程（${matchedSteps.size} 条）",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            items(matchedSteps, key = { (ch, st) -> "${ch.id}_${st.title}" }) { (chapter, step) ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                chapter.icon,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                chapter.title,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(step.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            step.content,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
        }

        if (faqItems.isNotEmpty()) {
            item {
                Text(
                    "FAQ（${faqItems.size} 条）",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            items(faqItems, key = { it.question }) { faq ->
                FaqCard(faq = faq)
            }
        }

        if (matchedSteps.isEmpty() && faqItems.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.SearchOff,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "没有找到「$query」相关内容",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
