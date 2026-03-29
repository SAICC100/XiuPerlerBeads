# 秀拼豆 (XiuPerlerBeads) 项目状态文档

> 文档更新时间: 2026-03-29
> 项目目标: 复刻 iOS 应用「啃豆小仓」功能

---

## 一、项目概述

### 1.1 项目信息
| 项目 | 值 |
|------|-----|
| 应用名称 | 秀拼豆 |
| 包名 | com.example.xiuperlerbeads |
| 最低 SDK | 24 (Android 7.0) |
| 目标 SDK | 34 (Android 14) |
| UI 框架 | Jetpack Compose + Material 3 |
| 架构模式 | MVVM + Clean Architecture |

### 1.2 项目结构
```
app/src/main/java/com/example/xiuperlerbeads/
├── data/
│   └── repository/
│       └── InventoryRepository.kt    # 数据持久化层 (898行)
├── domain/
│   └── model/
│       ├── Models.kt                 # 数据模型 (494行)
│       └── BeadColorManager.kt       # 颜色管理逻辑 (321行)
├── ui/
│   ├── screens/
│   │   ├── CreateScreen.kt           # 创作中心页面
│   │   └── ProjectsScreen.kt         # 我的项目页面
│   ├── navigation/
│   │   └── Screen.kt                 # 导航路由定义
│   ├── theme/
│   │   ├── Theme.kt                  # Material 3 主题
│   │   └── Color.kt                  # 颜色常量
│   └── MainActivity.kt               # 主入口
└── XiuPerlerBeadsApp.kt             # Application 类
```

---

## 二、已完成功能

### 2.1 已完成页面 ✅

| 页面 | 文件 | 功能 | 状态 |
|------|------|------|------|
| 创作中心 | CreateScreen.kt | 手绘画布入口、图片转像素入口、素材库入口、新建项目 | ✅ 可用 |
| 我的项目 | ProjectsScreen.kt | 项目列表、Tab分类、排序、卡片操作、AI识别入口 | ✅ 可用 |

### 2.2 已完成数据模型 ✅

| 模型 | 说明 | 测试状态 |
|------|------|---------|
| ColorSystem | 6大品牌色号体系 (MARD/COCO/漫漫/卡卡/盼盼/咪小窝) | ✅ |
| Brand | 品牌信息 | ✅ |
| BrandStock | 品牌库存 | ✅ |
| BeadColor | 多品牌色号对照 | ✅ |
| CustomColor | 自定义颜色 | ✅ |
| ProjectRecord | 项目记录 (支持父子层级) | ✅ |
| BeadUsage | 豆子用量 | ✅ |
| PurchaseRecord | 购买记录 | ✅ |
| PurchaseItem | 购买条目 | ✅ |
| HistoryRecord | 操作历史 | ✅ |
| AIVendor | AI服务商 (OpenAI/Kimi/Anthropic/Qwen/Gemini) | ✅ |
| AIRecognitionResult | AI识别结果 | ✅ |

### 2.3 已完成核心功能 ✅

| 功能 | 文件 | 说明 | 测试状态 |
|------|------|------|---------|
| 多品牌色号加载 | BeadColorManager.kt | 从 allcolors.json 加载6大品牌色号对照 | ✅ |
| 颜色查找 | BeadColorManager.kt | 按MARD色号/任意色号查找 | ✅ |
| 颜色转换 | BeadColorManager.kt | 跨品牌色号转换 | ✅ |
| RGB颜色匹配 | BeadColorManager.kt | 最近颜色查找 | ✅ |
| 数据持久化 | InventoryRepository.kt | SharedPreferences + JSON | ✅ |
| 品牌管理 | InventoryRepository.kt | CRUD操作 | ✅ |
| 库存管理 | InventoryRepository.kt | 库存增删改查 | ✅ |
| 项目管理 | InventoryRepository.kt | 父子项目层级 | ✅ |
| 历史记录 | InventoryRepository.kt | 操作历史追踪 | ✅ |
| 备份/恢复 | InventoryRepository.kt | JSON导出/导入 | ✅ |

---

## 三、待完成功能 (从啃豆小仓复刻)

### 3.1 核心功能待开发

| 功能 | 来源 | 优先级 | 状态 |
|------|------|--------|------|
| 库存管理页面 | 啃豆小仓 | 🔴 高 | ❌ 缺失 |
| 品牌管理页面 | 啃豆小仓 | 🔴 高 | ❌ 缺失 |
| AI扫描页面 | 啃豆小仓 | 🔴 高 | ❌ 缺失 |
| 画布编辑页面 | 啃豆小仓 | 🔴 高 | ❌ 缺失 |
| 图片导入页面 | 啃豆小仓 | 🟡 中 | ❌ 缺失 |
| 首页仪表盘 | 啃豆小仓 | 🟡 中 | ❌ 缺失 |
| 统计页面 | 啃豆小仓 | 🟡 中 | ❌ 缺失 |
| 设置页面 | 啃豆小仓 | 🟡 中 | ❌ 缺失 |
| 素材库页面 | 啃豆小仓 | 🟢 低 | ⚠️ 占位 |

### 3.2 待开发页面详情

#### 3.2.1 库存管理页面 (InventoryScreen)
**功能需求:**
- [ ] 品牌切换 Tab
- [ ] 颜色列表显示 (网格/列表视图)
- [ ] 库存数量编辑
- [ ] 低库存筛选/高亮
- [ ] 颜色搜索
- [ ] 批量增减库存

#### 3.2.2 品牌管理页面 (BrandManagerScreen)
**功能需求:**
- [ ] 品牌列表
- [ ] 添加/编辑/删除品牌
- [ ] 低库存阈值设置
- [ ] 默认色号体系选择

#### 3.2.3 AI扫描页面 (AIScreen)
**功能需求:**
- [ ] 多AI服务商支持 (OpenAI/Kimi/Anthropic/Qwen/Gemini)
- [ ] API Key 配置
- [ ] 图片上传/拍照
- [ ] 颜色识别结果展示
- [ ] 一键加入库存

#### 3.2.4 画布编辑页面 (CanvasScreen)
**功能需求:**
- [ ] 像素画布渲染
- [ ] 颜色选择器
- [ ] 画笔工具 (点/线/矩形/填充)
- [ ] 撤销/重做
- [ ] 缩放/平移
- [ ] 网格线显示
- [ ] 保存为项目

#### 3.2.5 图片导入页面 (ImageImportScreen)
**功能需求:**
- [ ] 图片选择/拍照
- [ ] 尺寸设置
- [ ] 预览处理结果
- [ ] 颜色数量/分布显示
- [ ] 导入到画布

#### 3.2.6 首页仪表盘 (HomeScreen)
**功能需求:**
- [ ] 库存概览统计
- [ ] 低库存提醒
- [ ] 最近项目
- [ ] 快捷操作入口
- [ ] 采购运输状态

#### 3.2.7 统计页面 (StatisticsScreen)
**功能需求:**
- [ ] 颜色使用排名
- [ ] 项目完成统计
- [ ] 月度/年度报表
- [ ] 消耗趋势图

#### 3.2.8 设置页面 (SettingsScreen)
**功能需求:**
- [ ] 主题切换 (浅色/深色/跟随系统)
- [ ] 默认色号体系
- [ ] 数据备份/恢复
- [ ] AI服务商配置
- [ ] 关于/版本信息

---

## 四、啃豆小仓功能对照表

### 4.1 啃豆小仓 iOS 功能 vs 秀拼豆 Android 状态

| 啃豆小仓功能 | Android 实现 | 状态 |
|-------------|-------------|------|
| **多品牌库存管理** | | |
| - MARD 色号 | ColorSystem.MARD | ✅ 模型完成 |
| - COCO 色号 | ColorSystem.COCO | ✅ 模型完成 |
| - 漫漫 色号 | ColorSystem.MANMAN | ✅ 模型完成 |
| - 卡卡 色号 | ColorSystem.KAKA | ✅ 模型完成 |
| - 盼盼 色号 | ColorSystem.PANPAN | ✅ 模型完成 |
| - 咪小窝 色号 | ColorSystem.MIXIAOWO | ✅ 模型完成 |
| - 品牌切换 | InventoryScreen | ❌ 缺失 |
| - 库存增删改查 | InventoryRepository | ✅ 逻辑完成 |
| **项目管理** | | |
| - 新建项目 | CreateScreen | ✅ 页面完成 |
| - 项目列表 | ProjectsScreen | ✅ 页面完成 |
| - 画布编辑 | CanvasScreen | ❌ 缺失 |
| - 项目执行 | CanvasScreen | ❌ 缺失 |
| - 项目归档 | ProjectRecord | ✅ 模型完成 |
| **AI 识别** | | |
| - OpenAI | AIVendor.OPENAI | ✅ 模型完成 |
| - Kimi | AIVendor.KIMI | ✅ 模型完成 |
| - Anthropic | AIVendor.ANTHROPIC | ✅ 模型完成 |
| - 通义千问 | AIVendor.QWEN | ✅ 模型完成 |
| - Gemini | AIVendor.GEMINI | ✅ 模型完成 |
| - 图片识别 | AIScreen | ❌ 缺失 |
| **数据管理** | | |
| - 历史记录 | HistoryRecord | ✅ 模型完成 |
| - 撤回功能 | InventoryRepository | ✅ 逻辑完成 |
| - 备份导出 | InventoryRepository | ✅ 逻辑完成 |
| - 恢复导入 | InventoryRepository | ✅ 逻辑完成 |
| **采购管理** | | |
| - 运输中订单 | PurchaseRecord | ✅ 模型完成 |
| - 到货确认 | PURCHASE_COMPLETE | ✅ 完成 |
| **自定义** | | |
| - 自定义颜色 | CustomColor | ✅ 模型完成 |

---

## 五、测试覆盖

### 5.1 单元测试 ✅

| 测试文件 | 测试数 | 通过数 | 状态 |
|---------|-------|-------|------|
| ModelsTest.kt | 23 | 23 | ✅ |
| BeadColorManagerTest.kt | 8 | 8 | ✅ |
| **总计** | **31** | **31** | ✅ |

### 5.2 测试结果
```
BUILD SUCCESSFUL
44 actionable tasks
> Task :app:testDebugUnitTest PASSED
> Task :app:testReleaseUnitTest PASSED
```

### 5.3 测试覆盖范围
- ✅ ColorSystem 枚举转换
- ✅ BrandStock 库存计算
- ✅ BeadColor 颜色属性
- ✅ ProjectRecord 项目统计
- ✅ BeadUsage 用量追踪
- ✅ AIVendor AI服务商
- ✅ HistoryType 历史类型
- ✅ PurchaseRecord 购买记录
- ✅ BeadColorManager 颜色查找
- ✅ 颜色搜索功能

---

## 六、构建状态

### 6.1 APK 构建 ✅
```
BUILD SUCCESSFUL in 9s
33 actionable tasks: 5 executed, 28 up-to-date
```

**APK 位置:** `app/build/outputs/apk/debug/app-debug.apk`

### 6.2 编译警告 (已修复)
- ✅ InventoryRepository.kt 类型警告 (11个) - 已修复
- ✅ Models.kt 枚举语法错误 - 已修复
- ✅ Theme.kt Material3 颜色引用 - 已修复

### 6.3 资源文件
- ✅ allcolors.json (多品牌色号对照) - 存在于 assets/

---

## 七、待办事项

### 7.1 短期 (核心功能)
- [ ] 实现 InventoryScreen (库存管理)
- [ ] 实现 BrandManagerScreen (品牌管理)
- [ ] 实现 HomeScreen (首页仪表盘)
- [ ] 实现 CanvasScreen (画布编辑)
- [ ] 实现 ImageImportScreen (图片导入)

### 7.2 中期 (AI功能)
- [ ] 实现 AIScreen (AI扫描)
- [ ] 集成 OpenAI API
- [ ] 集成 Kimi API
- [ ] 集成其他 AI 服务商

### 7.3 长期 (完善功能)
- [ ] 实现 StatisticsScreen (统计页面)
- [ ] 实现 SettingsScreen (设置页面)
- [ ] 实现素材库
- [ ] 完善单元测试覆盖
- [ ] 添加 UI 测试

---

## 八、API 参考

### 8.1 数据持久化接口 (InventoryRepository)

```kotlin
// 品牌管理
fun getBrands(): List<Brand>
fun addBrand(brand: Brand)
fun updateBrand(brand: Brand)
fun deleteBrand(brandId: String)

// 库存管理
fun getAllStocks(): List<BrandStock>
fun getStockByBrand(brandId: String): List<BrandStock>
fun addStock(stock: BrandStock)
fun updateStock(stock: BrandStock)
fun deductStock(brandId: String, mardCode: String, amount: Int)

// 项目管理
fun getProjects(): List<ProjectRecord>
fun addProject(project: ProjectRecord)
fun updateProject(project: ProjectRecord)
fun deleteProject(projectId: String)

// 备份恢复
fun exportData(): String
fun importData(json: String): Boolean
```

### 8.2 颜色管理接口 (BeadColorManager)

```kotlin
fun getAllColors(): List<BeadColor>
fun findByMardCode(code: String): BeadColor?
fun findByCode(code: String, system: ColorSystem): BeadColor?
fun findClosestColor(r: Int, g: Int, b: Int): BeadColor?
fun convertCode(code: String, from: ColorSystem, to: ColorSystem): String?
fun search(query: String, system: ColorSystem): List<BeadColor>
```

---

## 九、技术栈

| 类别 | 技术 | 版本 |
|------|------|------|
| 语言 | Kotlin | 1.9.x |
| UI | Jetpack Compose | BOM 2024.02.00 |
| 设计 | Material 3 | Latest |
| 架构 | MVVM | - |
| 导航 | Navigation Compose | 2.7.x |
| 数据 | SharedPreferences + JSON | - |
| 测试 | JUnit 4 | 4.13.2 |
| 构建 | Gradle (Kotlin DSL) | 8.2 |

---

## 十、联系方式

- 项目路径: `/Users/cedric/PycharmProjects/XiuPerlerBeads`
- 源码: `app/src/main/java/com/example/xiuperlerbeads/`
- 测试: `app/src/test/java/com/example/xiuperlerbeads/`
- 资源: `app/src/main/res/` + `app/src/main/assets/`
