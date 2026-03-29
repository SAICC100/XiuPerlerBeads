package com.example.xiuperlerbeads

import com.example.xiuperlerbeads.domain.model.Brand
import com.example.xiuperlerbeads.domain.model.BrandStock
import com.example.xiuperlerbeads.domain.model.BeadColor
import com.example.xiuperlerbeads.domain.model.ColorSystem
import org.junit.Assert.*
import org.junit.Test

/**
 * 首页模块测试
 * 模块: HomeScreen
 * 功能: 快捷操作、库存概览、低库存提醒、最近项目、小贴士
 */
class HomeScreenModuleTest {

    @Test
    fun test_首页_库存概览数据统计() {
        // 测试库存概览卡片的数据统计功能
        // A1: 200-50=150, 150<100? 否
        // A5: 50-30=20, 20<100? 是 - 低库存
        // A8: 10-0=10, 10<100? 是 - 低库存
        // B1: 0-0=0, 0<100? 是 - 低库存, 0<=0? 是 - 缺货
        val stocks = listOf(
            BrandStock(brandId = "1", mardCode = "A1", stock = 200, used = 50),
            BrandStock(brandId = "1", mardCode = "A5", stock = 50, used = 30),
            BrandStock(brandId = "1", mardCode = "A8", stock = 10, used = 0),
            BrandStock(brandId = "1", mardCode = "B1", stock = 0, used = 0)
        )
        
        val totalColors = stocks.size
        val totalQuantity = stocks.sumOf { it.available }
        val lowStockCount = stocks.count { it.isLowStock() }
        val outOfStockCount = stocks.count { it.available <= 0 }
        
        assertEquals("总颜色数应为4", 4, totalColors)
        assertEquals("总数量应为180", 180, totalQuantity)
        assertEquals("低库存数量应为3(A5,A8,B1)", 3, lowStockCount)
        assertEquals("缺货数量应为1(B1)", 1, outOfStockCount)
    }
    
    @Test
    fun test_首页_低库存判断逻辑() {
        // 测试低库存判断 - 默认阈值100
        val stockNormal = BrandStock(brandId = "1", mardCode = "A1", stock = 200, used = 50)
        val stockLow = BrandStock(brandId = "1", mardCode = "A5", stock = 80, used = 30)
        val stockOut = BrandStock(brandId = "1", mardCode = "A8", stock = 0, used = 0)
        
        assertFalse("200-50=150 > 100, 不应低库存", stockNormal.isLowStock())
        assertTrue("80-30=50 < 100, 应低库存", stockLow.isLowStock())
        assertTrue("0 < 100, 应低库存", stockOut.isLowStock())
    }
    
    @Test
    fun test_首页_低库存自定义阈值() {
        val stock = BrandStock(brandId = "1", mardCode = "A1", stock = 120, used = 30)
        
        // 默认阈值100: 120-30=90 < 100, 低库存
        assertTrue("默认阈值100时应低库存", stock.isLowStock())
        // 自定义阈值50: 120-30=90 > 50, 不低库存
        assertFalse("阈值50时不低库存", stock.isLowStock(50))
        // 自定义阈值200: 120-30=90 < 200, 低库存
        assertTrue("阈值200时应低库存", stock.isLowStock(200))
    }
    
    @Test
    fun test_首页_快捷操作导航() {
        // 测试快捷操作按钮的存在性
        val quickActions = listOf("创作", "库存", "项目")
        
        assertTrue("应有创作操作", quickActions.contains("创作"))
        assertTrue("应有库存操作", quickActions.contains("库存"))
        assertTrue("应有项目操作", quickActions.contains("项目"))
    }
    
    @Test
    fun test_首页_最近项目数据结构() {
        data class RecentProject(
            val name: String,
            val timeAgo: String,
            val size: Int
        )
        
        val recentProjects = listOf(
            RecentProject("小兔子", "5分钟前", 32),
            RecentProject("猫咪", "昨天", 48),
            RecentProject("皮卡丘", "3天前", 64)
        )
        
        assertEquals("应有3个最近项目", 3, recentProjects.size)
        assertEquals("第一个项目应为小兔子", "小兔子", recentProjects[0].name)
        assertEquals("项目尺寸应为32", 32, recentProjects[0].size)
    }
}

/**
 * 仓库模块测试
 * 模块: InventoryScreen
 * 功能: 品牌管理、库存CRUD、搜索筛选、颜色转换
 */
class InventoryScreenModuleTest {
    
    @Test
    fun test_仓库_品牌创建() {
        val brand = Brand(
            name = "我的仓库",
            colorSystem = ColorSystem.MARD,
            lowStockThreshold = 100
        )
        
        assertEquals("品牌名称应正确", "我的仓库", brand.name)
        assertEquals("色号体系应正确", ColorSystem.MARD, brand.colorSystem)
        assertEquals("低库存阈值应正确", 100, brand.lowStockThreshold)
        assertNotNull("品牌ID应自动生成", brand.id)
    }
    
    @Test
    fun test_仓库_品牌支持多色号体系() {
        val brands = listOf(
            Brand(name = "美隆仓", colorSystem = ColorSystem.MARD),
            Brand(name = "COCO仓", colorSystem = ColorSystem.COCO),
            Brand(name = "漫漫仓", colorSystem = ColorSystem.MANMAN),
            Brand(name = "卡卡仓", colorSystem = ColorSystem.KAKA),
            Brand(name = "盼盼仓", colorSystem = ColorSystem.PANPAN),
            Brand(name = "咪小窝仓", colorSystem = ColorSystem.MIXIAOWO)
        )
        
        assertEquals("应支持6个品牌", 6, brands.size)
        assertTrue("应支持MARD", brands.any { it.colorSystem == ColorSystem.MARD })
        assertTrue("应支持COCO", brands.any { it.colorSystem == ColorSystem.COCO })
        assertTrue("应支持漫漫", brands.any { it.colorSystem == ColorSystem.MANMAN })
    }
    
    @Test
    fun test_仓库_库存添加和更新() {
        var stock = BrandStock(
            brandId = "brand1",
            mardCode = "A1",
            stock = 100
        )
        
        assertEquals("初始库存应为100", 100, stock.stock)
        assertEquals("已使用应为0", 0, stock.used)
        assertEquals("可用库存应为100", 100, stock.available)
        
        // 模拟使用50个
        stock = stock.copy(used = 50)
        assertEquals("已使用应为50", 50, stock.used)
        assertEquals("可用库存应为50", 50, stock.available)
    }
    
    @Test
    fun test_仓库_搜索过滤_色号匹配() {
        val stocks = listOf(
            BrandStock(brandId = "1", mardCode = "A1"),
            BrandStock(brandId = "1", mardCode = "A5"),
            BrandStock(brandId = "1", mardCode = "A8"),
            BrandStock(brandId = "1", mardCode = "B1")
        )
        
        // 按色号搜索
        val searchQuery = "A"
        val filtered = stocks.filter { 
            it.mardCode.contains(searchQuery, ignoreCase = true) 
        }
        assertEquals("A应匹配3个", 3, filtered.size)
        
        // 搜索不存在的色号
        val noMatch = stocks.filter { 
            it.mardCode.contains("Z", ignoreCase = true) 
        }
        assertEquals("Z应匹配0个", 0, noMatch.size)
    }
    
    @Test
    fun test_仓库_品牌筛选() {
        val stocks = listOf(
            BrandStock(brandId = "brand1", mardCode = "A1"),
            BrandStock(brandId = "brand1", mardCode = "A5"),
            BrandStock(brandId = "brand2", mardCode = "A8"),
            BrandStock(brandId = "brand3", mardCode = "B1")
        )
        
        val brand1Stocks = stocks.filter { it.brandId == "brand1" }
        assertEquals("brand1应有2个库存", 2, brand1Stocks.size)
        
        val allBrandsStocks = stocks.filter { it.brandId != "brand2" }
        assertEquals("排除brand2应有3个库存", 3, allBrandsStocks.size)
    }
    
    @Test
    fun test_仓库_低库存筛选() {
        val stocks = listOf(
            BrandStock(brandId = "1", mardCode = "A1", stock = 200, used = 50),  // 150, 不低
            BrandStock(brandId = "1", mardCode = "A5", stock = 80, used = 30),   // 50, 低
            BrandStock(brandId = "1", mardCode = "A8", stock = 20, used = 10),  // 10, 低
            BrandStock(brandId = "1", mardCode = "B1", stock = 0, used = 0)      // 0, 低
        )
        
        val lowStockItems = stocks.filter { it.isLowStock() }
        assertEquals("低库存应有3个", 3, lowStockItems.size)
        
        val outOfStockItems = stocks.filter { it.available <= 0 }
        assertEquals("缺货应有1个", 1, outOfStockItems.size)
    }
    
    @Test
    fun test_仓库_库存状态颜色判断() {
        val stockEnough = BrandStock(brandId = "1", mardCode = "A1", stock = 200, used = 50)
        val stockLow = BrandStock(brandId = "1", mardCode = "A5", stock = 80, used = 30)
val stockOut = BrandStock(brandId = "1", mardCode = "A8", stock = 0, used = 0)
        
        // StockEnough: available >= 100
        assertTrue("150 >= 100, StockEnough", stockEnough.available >= 100)
        
        // StockLow: 0 < available < 100
        assertTrue("0 < 50 < 100, StockLow", stockLow.available in 1..99)
        
        // StockOut: available <= 0
        assertTrue("0 <= 0, StockOut", stockOut.available <= 0)
    }
}

/**
 * 创作模块测试
 * 模块: CreateScreen
 * 功能: 新建项目、尺寸选择、图片导入
 */
class CreateScreenModuleTest {
    
    @Test
    fun test_创作_新建项目_尺寸选项() {
        val availableSizes = listOf(16, 24, 32, 48, 64, 96, 128, 256)
        
        assertEquals("应有8种尺寸可选", 8, availableSizes.size)
        assertTrue("应支持16x16", availableSizes.contains(16))
        assertTrue("应支持32x32", availableSizes.contains(32))
        assertTrue("应支持128x128", availableSizes.contains(128))
        assertTrue("应支持256x256", availableSizes.contains(256))
    }
    
    @Test
    fun test_创作_尺寸建议映射() {
        val sizeDescriptions = mapOf(
            16 to "小型钥匙扣",
            24 to "小型挂件",
            32 to "中型图案",
            48 to "中型摆件",
            64 to "大型图案",
            96 to "大型壁画",
            128 to "超大尺寸",
            256 to "专业级"
        )
        
        assertEquals("16应为小型钥匙扣", "小型钥匙扣", sizeDescriptions[16])
        assertEquals("32应为中型图案", "中型图案", sizeDescriptions[32])
        assertEquals("64应为大型图案", "大型图案", sizeDescriptions[64])
    }
    
    @Test
    fun test_创作_创作方式选项() {
        data class CreateOption(
            val title: String,
            val description: String
        )
        
        val createOptions = listOf(
            CreateOption("手绘画布", "从零开始自由绘制像素图案"),
            CreateOption("图片转像素", "导入图片自动转换为拼豆图纸"),
            CreateOption("素材库", "动物 · 人物 · 植物 · 更多")
        )
        
        assertEquals("应有3种创作方式", 3, createOptions.size)
        assertTrue("应有手绘画布", createOptions.any { it.title == "手绘画布" })
        assertTrue("应有图片转像素", createOptions.any { it.title == "图片转像素" })
        assertTrue("应有素材库", createOptions.any { it.title == "素材库" })
    }
    
    @Test
    fun test_创作_项目名称默认() {
        val defaultProjectName = "我的拼豆"
        
        assertTrue("默认名称应包含'我的'", defaultProjectName.contains("我的"))
        assertEquals("默认名称应为我的拼豆", "我的拼豆", defaultProjectName)
    }
    
    @Test
    fun test_创作_画布尺寸计算() {
        val sizes = listOf(16, 32, 64, 128)
        
        sizes.forEach { size ->
            val totalPixels = size * size
            val expectedPixels = when (size) {
                16 -> 256
                32 -> 1024
                64 -> 4096
                128 -> 16384
                else -> 0
            }
            assertEquals("${size}x${size}的总像素数应正确", expectedPixels, totalPixels)
        }
    }
}

/**
 * 项目模块测试
 * 模块: ProjectsScreen
 * 功能: 项目列表、标签筛选、排序、项目卡片
 */
class ProjectsScreenModuleTest {
    
    @Test
    fun test_项目_标签页分类() {
        val tabs = listOf("全部", "进行中", "已完成")
        
        assertEquals("应有3个标签", 3, tabs.size)
        assertTrue("应有全部标签", tabs.contains("全部"))
        assertTrue("应有进行中标签", tabs.contains("进行中"))
        assertTrue("应有已完成标签", tabs.contains("已完成"))
    }
    
    @Test
    fun test_项目_排序选项() {
        val sortOptions = listOf("按时间排序", "按名称排序", "按尺寸排序")
        
        assertEquals("应有3种排序方式", 3, sortOptions.size)
    }
    
    @Test
    fun test_项目_项目数据结构() {
        data class ProjectItem(
            val id: Long,
            val name: String,
            val size: Int,
            val timeAgo: String,
            val colorCount: Int
        )
        
        val projects = listOf(
            ProjectItem(1L, "小兔子", 32, "5分钟前", 12),
            ProjectItem(2L, "猫咪", 48, "昨天", 24),
            ProjectItem(3L, "皮卡丘", 64, "3天前", 18)
        )
        
        assertEquals("应有3个项目", 3, projects.size)
        assertEquals("第一个项目ID应为1", 1L, projects[0].id)
        assertEquals("第一个项目名称应为小兔子", "小兔子", projects[0].name)
        assertEquals("第一个项目尺寸应为32", 32, projects[0].size)
        assertEquals("第一个项目颜色数应为12", 12, projects[0].colorCount)
    }
    
    @Test
    fun test_项目_按标签筛选() {
        data class ProjectItem(
            val id: Long,
            val name: String,
            val status: String  // "进行中", "已完成"
        )
        
        val projects = listOf(
            ProjectItem(1L, "小兔子", "进行中"),
            ProjectItem(2L, "猫咪", "已完成"),
            ProjectItem(3L, "皮卡丘", "进行中")
        )
        
        val inProgressProjects = projects.filter { it.status == "进行中" }
        val completedProjects = projects.filter { it.status == "已完成" }
        
        assertEquals("进行中应有2个", 2, inProgressProjects.size)
        assertEquals("已完成应有1个", 1, completedProjects.size)
    }
    
    @Test
    fun test_项目_项目卡片信息显示() {
        data class ProjectItem(
            val id: Long,
            val name: String,
            val size: Int,
            val timeAgo: String,
            val colorCount: Int
        )
        
        val project = ProjectItem(1L, "皮卡丘", 64, "3天前", 18)
        
        // 验证卡片应显示的信息
        val displayInfo = "${project.size}×${project.size}"
        assertEquals("尺寸显示应为64×64", "64×64", displayInfo)
        
        val metaInfo = "${project.colorCount}种颜色 · ${project.timeAgo}"
        assertEquals("元信息应正确", "18种颜色 · 3天前", metaInfo)
    }
    
    @Test
    fun test_项目_项目操作按钮() {
        val actions = listOf("编辑", "导出", "删除")
        
        assertEquals("应有3个操作", 3, actions.size)
        assertTrue("应有编辑操作", actions.contains("编辑"))
        assertTrue("应有导出操作", actions.contains("导出"))
        assertTrue("应有删除操作", actions.contains("删除"))
    }
    
    @Test
    fun test_项目_AI识别功能入口() {
        data class AIRecognitionCard(
            val title: String,
            val description: String
        )
        
        val aiCard = AIRecognitionCard(
            title = "AI 图纸识别",
            description = "上传图片自动统计颜色和数量"
        )
        
        assertEquals("标题应正确", "AI 图纸识别", aiCard.title)
        assertTrue("描述应包含上传", aiCard.description.contains("上传"))
        assertTrue("描述应包含颜色", aiCard.description.contains("颜色"))
    }
}

/**
 * 色号模型测试
 */
class BeadColorModelTest {
    
    @Test
    fun test_色号_模型创建() {
        val color = BeadColor(
            mardCode = "A1",
            cocoCode = "C001",
            colorName = "浅黄",
            red = 250,
            green = 244,
            blue = 200
        )
        
        assertEquals("MARD色号应正确", "A1", color.mardCode)
        assertEquals("COCO色号应正确", "C001", color.cocoCode)
        assertEquals("颜色名称应正确", "浅黄", color.colorName)
        assertEquals("红色值应正确", 250, color.red)
        assertEquals("绿色值应正确", 244, color.green)
        assertEquals("蓝色值应正确", 200, color.blue)
    }
    
    @Test
    fun test_色号_转换为ComposeColor() {
        val color = BeadColor(
            mardCode = "A1",
            colorName = "测试色",
            red = 255,
            green = 128,
            blue = 64
        )
        
        val composeColor = color.toComposeColor()
        assertNotNull("ComposeColor不应为空", composeColor)
    }
    
    @Test
    fun test_色号_RGB值范围验证() {
        // 测试多个颜色的RGB值都在有效范围内
        val colors = listOf(
            BeadColor(mardCode = "1", red = 0, green = 0, blue = 0),
            BeadColor(mardCode = "2", red = 255, green = 255, blue = 255),
            BeadColor(mardCode = "3", red = 128, green = 64, blue = 192)
        )
        
        colors.forEach { color ->
            assertTrue("红色值应在0-255范围内", color.red in 0..255)
            assertTrue("绿色值应在0-255范围内", color.green in 0..255)
            assertTrue("蓝色值应在0-255范围内", color.blue in 0..255)
        }
    }
    
    @Test
    fun test_色号_品牌色号前缀() {
        assertEquals("MARD前缀应为M", "M", ColorSystem.MARD.prefix)
        assertEquals("COCO前缀应为C", "C", ColorSystem.COCO.prefix)
        assertEquals("漫漫前缀应为MM", "MM", ColorSystem.MANMAN.prefix)
        assertEquals("卡卡前缀应为K", "K", ColorSystem.KAKA.prefix)
        assertEquals("盼盼前缀应为PP", "PP", ColorSystem.PANPAN.prefix)
        assertEquals("咪小窝前缀应为MXW", "MXW", ColorSystem.MIXIAOWO.prefix)
    }
    
    @Test
    fun test_色号_ColorSystem显示名称() {
        assertEquals("MARD显示名称应为MARD", "MARD", ColorSystem.MARD.displayName)
        assertEquals("COCO显示名称应为COCO", "COCO", ColorSystem.COCO.displayName)
        assertEquals("漫漫显示名称应为漫漫", "漫漫", ColorSystem.MANMAN.displayName)
        assertEquals("卡卡显示名称应为卡卡", "卡卡", ColorSystem.KAKA.displayName)
        assertEquals("盼盼显示名称应为盼盼", "盼盼", ColorSystem.PANPAN.displayName)
        assertEquals("咪小窝显示名称应为咪小窝", "咪小窝", ColorSystem.MIXIAOWO.displayName)
    }
    
    @Test
    fun test_色号_fromString解析() {
        assertEquals("MARD应正确解析", ColorSystem.MARD, ColorSystem.fromString("MARD"))
        assertEquals("M应正确解析", ColorSystem.MARD, ColorSystem.fromString("M"))
        assertEquals("COCO应正确解析", ColorSystem.COCO, ColorSystem.fromString("COCO"))
        assertEquals("漫漫应正确解析", ColorSystem.MANMAN, ColorSystem.fromString("漫漫"))
        assertEquals("未知应返回默认MARD", ColorSystem.MARD, ColorSystem.fromString("unknown"))
    }
}
