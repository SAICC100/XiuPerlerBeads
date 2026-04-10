# XiuPerlerBeads 待开发功能清单

> 参考基准：BeadInventory (iOS)
> 更新日期：2026-04-10

---

## 优先级说明

- 🔴 高优先级：核心业务功能，用户高频使用
- 🟡 中优先级：数据管理功能，影响完整性
- 🟢 低优先级：辅助/体验功能

---

## 🔴 高优先级

### 1. 色号转换工具 `ColorConverterScreen`
**对应 iOS：** `ColorConverterView.swift`

**功能描述：**
- 输入任意品牌色号（MARD / COCO / 漫漫 / 卡卡 / 盼盼 / 咪小窝）
- 显示对应的所有其他品牌色号映射关系
- 标注每个色号当前的库存状态（充足/低库存/缺货/未持有）
- 支持从库存页快速跳转（点击某色号后"查看其他品牌对应色号"）

**技术要点：**
- 数据来源：`BeadColorManager.convertCode()`，已有完整色号映射数据
- UI：输入框 + 色号体系选择器 + 结果列表（按品牌分组）
- 入口：底部导航「库存」Tab → TopAppBar 操作，或独立入口

**路由：** `Screen.ColorConverter`

---

### 2. 隐藏色号管理页 `HiddenColorsScreen`
**对应 iOS：** `HiddenColorsManageView.swift`

**功能描述：**
- 列出当前品牌中所有 `isHidden = true` 的色号
- 支持单个/批量取消隐藏
- 取消隐藏时设置恢复初始库存量（默认 0）
- 入口：`BrandManagerScreen` → 品牌详情 → 「管理隐藏色号」

**技术要点：**
- `BrandStock.isHidden` 字段已存在，`InventoryRepository.unhideColor()` 已实现
- 只需新增 UI Screen，无需改动数据层

**路由：** `Screen.HiddenColors`（携带 brandId 参数）

---

### 3. 自定义色号 `CustomColorsScreen` + `CustomColorEditScreen`
**对应 iOS：** `CustomColorsView.swift` + `CustomColorEditView.swift`

**功能描述：**
- 在品牌下添加自定义色号（色号代码 + 名称 + HEX 色值）
- 支持搜索、编辑、删除自定义色号
- 自定义色号参与库存管理和 AI 识别匹配
- 入口：`BrandManagerScreen` → 品牌详情 → 「自定义色号」

**技术要点：**
- `CustomColor` 数据模型已存在
- `InventoryRepository` 已有 `addCustomColor / deleteCustomColor`
- 需要完善编辑功能（`updateCustomColor`）

**路由：** `Screen.CustomColors`（携带 brandId）

---

## 🟡 中优先级

### 4. 购买/物流跟踪 `ShippingScreen`
**对应 iOS：** `ShippingView.swift`

**功能描述：**
- 记录已购买但尚未到货的豆子订单
- 字段：品牌 / 色号 / 数量 / 购买日期 / 备注
- 支持文本粘贴快速录入（解析格式："A01 500颗 A02 300颗"）
- 到货确认后自动入库（批量操作）
- 入口：「我的」→ 「运输中订单」，或首页快捷卡片

**技术要点：**
- `PurchaseRecord` / `PurchaseItem` 数据模型已存在
- `InventoryRepository.addPurchaseRecord / completePurchaseRecord` 已实现
- 需要新建 UI Screen

**路由：** `Screen.Shipping`

---

### 5. 成品日历 `CompletionCalendarScreen`
**对应 iOS：** `CalendarView.swift`

**功能描述：**
- 月历视图展示每天完成（状态=已完成）的项目
- 日期格子显示当日完成项目数量/缩略图
- 点击某天查看当日完成的所有项目详情
- 入口：「我的」→「成品日历」

**技术要点：**
- `ProjectRecord.completedDate` 字段已存在
- 需引入日历 UI 组件（可手写月历 Grid，或用第三方库）
- 无需改动数据层

**路由：** `Screen.CompletionCalendar`

---

### 6. 操作历史撤销 `HistoryUndoSupport`
**对应 iOS：** `HistoryView` + undo 机制

**功能描述：**
- 在现有 `StatisticsScreen` 历史记录列表上增加撤销操作
- 支持撤销最近一次库存变更（恢复到变更前的值）
- 历史记录保存操作前的快照数据（`previousValue`）

**技术要点：**
- `HistoryRecord` 已有 `details: String` 字段，需扩展存储前值快照
- `InventoryRepository` 需新增 `undoHistoryRecord(id)` 方法

---

### 7. 自动备份与多版本恢复 `AutoBackupManager`
**对应 iOS：** `BackupRestoreView.swift`

**功能描述：**
- 每次重要操作后自动生成本地快照（最多保留 10 个版本）
- 备份管理页：查看所有快照（时间/大小/概要统计）
- 一键恢复到指定快照
- 入口：「设置」→「备份与恢复」

**技术要点：**
- 现有 `exportAllData / importData` 可复用
- 新增定时/触发式自动保存逻辑
- 文件存储于 `context.filesDir/backups/`

**路由：** `Screen.BackupRestore`

---

### 8. 数据导入预览与冲突处理
**对应 iOS：** `ImportFullDataView.swift`

**功能描述：**
- 导入 JSON 文件时先显示预览：品牌数/色号数/项目数
- 检测并展示冲突（已存在的品牌/色号）
- 提供「合并」或「覆盖」两种导入模式
- 当前 `SettingsScreen` 的导入是直接覆盖，无预览

---

## 🟢 低优先级

### 9. 品牌合并功能
**对应 iOS：** `BrandManagerView` 中的合并操作

**功能描述：**
- 在 `BrandManagerScreen` 中选择两个品牌，将其中一个的库存数据合并到另一个
- 合并后删除源品牌

---

### 10. 模板库内容填充
**当前状态：** `TemplateLibraryScreen.kt` 框架已存在，`templates = emptyList()`

**需要做的：**
- 提供一批内置模板数据（存于 assets）
- 模板格式：名称 / 类别 / 缩略图 / 像素数据（IntArray）
- 解析并加载到 `TemplateLibraryScreen`

---

### 11. 帮助中心 `HelpCenterScreen`
**对应 iOS：** `HelpCenterView + FAQView + TutorialDetailView`

**功能描述：**
- 分章节展示 App 使用教程（图文）
- 常见问题 FAQ
- 全文搜索

---

## 已完成功能

| 功能 | 状态 |
|------|------|
| 4 Tab 底部导航（首页/创作/库存/我的） | ✅ |
| 库存管理（按品牌/搜索/筛选/增删改） | ✅ |
| 品牌管理 | ✅ |
| 像素画布编辑器 | ✅ |
| 图片导入转像素图 | ✅ |
| AI 扫描识别 | ✅ |
| 项目管理（列表/分类/排序） | ✅ |
| 补货建议 | ✅ |
| 数据统计（概览/历史记录） | ✅ |
| 设置（导入/导出/清除） | ✅ |
| 导出（PNG/PDF/材料清单） | ✅ |
| 模板库（UI 框架，内容待填充） | ⚠️ 骨架 |
| 色号转换工具 `ColorConverterScreen` | ✅ |
| 隐藏色号管理页 `HiddenColorsScreen` | ✅ |
| 购买/物流跟踪 `ShippingScreen` | ✅ |
| 自定义色号 `CustomColorsScreen` | ✅ |
| 成品日历 `CompletionCalendarScreen` | ✅ |
| 自动备份与多版本恢复 `BackupRestoreScreen` | ✅ |
| 操作历史撤销 `HistoryUndoSupport` | ✅ |
| 数据导入预览与冲突处理 | ✅ |
| 品牌合并功能 | ✅ |
| 模板库内容填充（47 个内置模板） | ✅ |
| 帮助中心 `HelpCenterScreen` | ✅ |
