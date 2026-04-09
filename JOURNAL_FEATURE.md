# 手帐功能模块开发文档

## 概述

本次开发为 XiuPerlerBeads 应用新增了「手帐」功能模块，包含 4 个新页面和完整的数据层，并对原有代码进行了 Bug 修复。

---

## 一、Bug 修复

### InventoryRepository.kt

| 问题 | 修复 |
|------|------|
| `deductFromStock` 未做上限保护，可能扣出负数 | 加入 `minOf(used + amount, stock)` 限制 |
| `newValue` 计算错误 | 修正扣除逻辑 |
| `importData` 中 `purchaseRecords` 未恢复 | 补充还原语句 |
| `getExecutedProjects` 包含了已归档项目 | 添加 `!isArchived` 过滤条件 |

### AIManager.kt

| 问题 | 修复 |
|------|------|
| Qwen API Key 放在 URL 参数中，存在泄露风险 | 移至 `Authorization` 请求头 |
| Anthropic 模型 ID 硬编码 | 改为读取 `config.model` |

### CanvasViewModel.kt

| 问题 | 修复 |
|------|------|
| Undo 栈无上限，长时间操作会内存溢出 | 新增 `MAX_UNDO_STACK_SIZE = 30`，`drawCell`/`floodFill`/`clearCanvas` 均调用 `.takeLast()` |

### ExportManager.kt

| 问题 | 修复 |
|------|------|
| PDF 导出内容超过一页时不分页，内容截断 | 实现自动分页逻辑（内容溢出时新建页面） |
| `shareContent` 参数不明确 | 添加显式 `mimeType` 参数 |

### InventoryViewModel.kt

| 问题 | 修复 |
|------|------|
| `deductStock` 调用了 `repository.updateStock()` 导致逻辑绕过 | 改为调用 `repository.deductFromStock()` |

---

## 二、新增文件清单

```
app/src/main/java/com/example/xiuperlerbeads/
├── domain/model/
│   └── JournalModels.kt          # 数据模型
├── data/repository/
│   └── JournalRepository.kt      # 数据持久化（SharedPreferences + JSON）
├── ui/viewmodel/
│   └── JournalViewModel.kt       # ViewModel + 状态管理
└── ui/screens/
    ├── JournalHomeScreen.kt       # 首页（动态流）
    ├── AddEntryScreen.kt          # 添加记录页
    ├── JournalSummaryScreen.kt    # 汇总统计页
    └── ProfileScreen.kt           # 个人中心页
```

---

## 三、数据模型（JournalModels.kt）

### 核心类型

```kotlin
data class JournalCollection(val id, val name, val createdAt)
// JournalCollection.DEFAULT — 内置"手帐"文集，id = "default_journal"

data class JournalTag(val id, val name, val colorHex)
// tag.toComposeColor() — 将 colorHex 转为 Compose Color

enum class AttachmentType { IMAGE, FILE }

data class JournalAttachment(val uri, val type, val fileName, val fileSize)

data class JournalEntry(
    val id, val collectionId, val content,
    val attachments,          // 图片/文件列表
    val entryTime,            // 手动选择的时间（可空）
    val location,             // 地点
    val tags,                 // Tag 列表
    val expense,              // 花费金额
    val createdAt
)
// entry.isHandZhang  → collectionId == "default_journal"
// entry.displayTime  → entryTime ?: createdAt
```

### 统计类型

```kotlin
data class LocationStat(val location, val count)
data class TagExpenseStat(val tag, val totalExpense)
```

---

## 四、数据层（JournalRepository.kt）

**存储**：SharedPreferences + JSON 序列化，与现有 InventoryRepository 保持同一模式。

**Key 常量**

| Key | 内容 |
|-----|------|
| `KEY_COLLECTIONS` | 文集列表 |
| `KEY_TAGS` | 标签列表 |
| `KEY_ENTRIES` | 记录列表 |

**重要方法**

| 方法 | 说明 |
|------|------|
| `ensureDefaultCollection()` | init 时调用，保证"手帐"文集始终存在 |
| `deleteCollection(id)` | 拒绝删除 DEFAULT_ID |
| `updateCollection(collection)` | 更新文集名称 |
| `getDaysWithEntries(year, month, collectionId)` | 返回该月有记录的日期集合 `Set<Int>` |
| `getEntriesByDate(year, month, day)` | 返回某天的所有记录 |
| `getLocationStats(startTime, endTime)` | 按地点分组，按次数降序排列 |
| `getTagExpenseStats(startTime, endTime)` | 按标签汇总花费 |

---

## 五、ViewModel（JournalViewModel.kt）

### JournalState（首页/添加页状态）

```kotlin
data class JournalState(
    val collections: List<JournalCollection>,
    val tags: List<JournalTag>,
    val entries: List<JournalEntry>,
    val selectedCollectionId: String,
    val isLoading: Boolean,
    val error: String?
)
// 计算属性 filteredEntries：按 selectedCollectionId 过滤
```

### SummaryState（汇总页状态）

```kotlin
data class SummaryState(
    val locationStats: List<LocationStat>,
    val tagExpenseStats: List<TagExpenseStat>,
    val selectedCollectionId: String,
    val filterStartTime: Long?,
    val filterEndTime: Long?
)
// 计算属性 totalExpense：所有标签花费总和
```

### 主要方法

| 方法 | 用途 |
|------|------|
| `loadData()` | 加载文集/标签/记录 |
| `loadSummaryData()` | 加载汇总统计数据 |
| `selectCollection(id)` | 切换当前文集 |
| `addCollection(name)` | 新建文集 |
| `updateCollection(collection)` | 重命名文集 |
| `deleteCollection(id)` | 删除文集（默认文集不可删） |
| `addTag(name, colorHex)` | 新建标签 |
| `deleteTag(id)` | 删除标签 |
| `addEntry(...)` | 新增记录 |
| `deleteEntry(id)` | 删除记录 |
| `getEntry(id)` | 查询单条记录 |
| `getDaysWithEntries(year, month)` | 日历标记用 |
| `getEntriesByDate(year, month, day)` | 日历点击查看当日记录 |
| `setSummaryCollection(id)` | 切换汇总页文集 |
| `setSummaryDateFilter(start, end)` | 设置日期过滤范围 |

---

## 六、页面说明

### 1. 首页（JournalHomeScreen）

- 顶部 `FilterChip` 文集标签栏，末尾有"+"按钮新建文集
- `LazyColumn` 动态流展示记录卡片
- 每张卡片：头像圆圈 + 时间戳 + 地点/花费行 + 正文 + 图片网格（最多4张，超出显示 +N）+ 文件数量 + 标签
- 右下角 FAB 跳转添加记录页
- 空状态展示书签图标提示

### 2. 添加记录页（AddEntryScreen）

- 上方附件区：横向缩略图滚动条，支持多选图片（`image/*`）和多选文件（`*/*`）
- 中间文本输入框
- 下方表单（条件渲染）：
  - 所有文集：文集选择（必填）、标签选择
  - 仅【手帐】文集：额外显示时间（默认当前时间）、地点、花费（金额格式，默认 0）
- 标签支持新建，提供 8 种预设颜色：红/黄/绿/蓝/紫/橙/青/深红
- 时间选择：`DatePickerDialog` → `TimePickerDialog` 两步弹窗

### 3. 汇总页（JournalSummaryScreen）

**3 个子标签：**

#### 日历标签
- 手动实现的日历网格（纯 Compose，无第三方库）
- 今日：主色圆圈高亮
- 有记录的日期：primaryContainer 背景 + 小圆点
- 点击日期弹出当日详情对话框

#### 地点标签
- 按访问次数排序
- `LinearProgressIndicator` 条形图可视化
- 前三名金/银/铜配色

#### 花费标签
- 总花费汇总卡片
- 横向 Bar Chart 模拟饼图（纯 Compose 实现）
- 各标签花费明细列表
- 支持日期范围筛选

### 4. 个人中心页（ProfileScreen）

- 账户卡片：头像（姓名首字母）+ 记录数/文集数/标签数统计
- 文集列表：内置"手帐"有专属标记，不可删除；其他文集支持重命名和删除（下拉菜单）
- 设置区域（预留扩展位）

---

## 七、导航变更（Screen.kt / MainActivity.kt）

### 新增路由

| 路由对象 | 路由字符串 |
|----------|------------|
| `Screen.JournalHome` | `journal_home` |
| `Screen.AddEntry` | `add_entry` |
| `Screen.JournalSummary` | `journal_summary` |
| `Screen.Profile` | `profile` |

### 底部导航栏

原有 4 栏（首页/创作/库存/项目）更换为：

| 图标 | 标签 | 路由 |
|------|------|------|
| Home | 首页 | JournalHome |
| BarChart | 汇总 | JournalSummary |
| Inventory2 | 库存 | Inventory |
| Person | 我的 | Profile |

**启动页** 由 `Screen.Home` 改为 `Screen.JournalHome`。

---

## 八、新增依赖（build.gradle.kts）

```kotlin
// 图片加载
implementation("io.coil-kt:coil-compose:2.5.0")
```

用于首页记录卡片和添加页预览区的 `AsyncImage` 图片显示。

---

## 九、Application 初始化（XiuPerlerBeadsApp.kt）

```kotlin
override fun onCreate() {
    super.onCreate()
    instance = this
    BeadColorManager.loadFromAssets(this)  // Bug修复：补充了此调用
    repository = InventoryRepository(this)
    journalRepository = JournalRepository(this)  // 新增
}
```

---

## 十、技术说明

| 技术点 | 实现方式 |
|--------|----------|
| 数据持久化 | SharedPreferences + JSON（与 Inventory 同模式） |
| 图片/文件选择 | `ActivityResultContracts.GetMultipleContents()` |
| 图片显示 | Coil `AsyncImage` |
| 日历视图 | 纯 Compose 手动布局（无第三方库） |
| 图表 | 纯 Compose 绘制（LinearProgressIndicator + 自定义 Bar） |
| 状态管理 | `AndroidViewModel` + `StateFlow` |
| 异步操作 | 协程 `withContext(Dispatchers.IO)` |
| 时间选择 | `DatePickerDialog` + `AlertDialog(TimePicker)` |
