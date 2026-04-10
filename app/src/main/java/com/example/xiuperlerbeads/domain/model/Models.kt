package com.example.xiuperlerbeads.domain.model

import androidx.compose.ui.graphics.Color
import java.util.UUID

// ============================================================================
// 色号体系枚举 - 支持多种品牌色号
// ============================================================================

/**
 * 色号体系
 * 支持 MARD (美隆), COCO, 漫漫, 卡卡, 盼盼, 咪小窝 等品牌
 */
enum class ColorSystem(val displayName: String, val prefix: String) {
    MARD("MARD", "M"),
    COCO("COCO", "C"),
    MANMAN("漫漫", "MM"),
    KAKA("卡卡", "K"),
    PANPAN("盼盼", "PP"),
    MIXIAOWO("咪小窝", "MXW");

    companion object {
        fun fromString(value: String): ColorSystem {
            return entries.find { it.name == value || it.displayName == value } ?: MARD
        }
    }
}

// ============================================================================
// 品牌模型 - 管理不同品牌的库存
// ============================================================================

/**
 * 品牌
 * 每个品牌有独立的色号体系和库存
 */
data class Brand(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val lowStockThreshold: Int = 100,  // 低库存阈值
    val colorSystem: ColorSystem = ColorSystem.MARD
)

// ============================================================================
// 品牌库存模型 - 记录每个品牌的每种颜色库存
// ============================================================================

/**
 * 品牌库存
 * 记录某个品牌下某种颜色的库存数量
 */
data class BrandStock(
    val id: String = UUID.randomUUID().toString(),
    val brandId: String,
    val mardCode: String,  // 使用 MARD 色号作为统一标识
    val stock: Int = 0,   // 当前库存
    val used: Int = 0,    // 已使用数量
    val isHidden: Boolean = false  // 是否隐藏（库存为0且用户选择隐藏）
) {
    /**
     * 可用库存 = 当前库存 - 已使用
     */
    val available: Int
        get() = stock - used

    /**
     * 是否低库存
     */
    fun isLowStock(threshold: Int = 100): Boolean = available < threshold
}

// ============================================================================
// 拼豆颜色模型 - 支持多品牌色号对照
// ============================================================================

/**
 * 拼豆颜色
 * 包含颜色信息和多种品牌的色号对照
 */
data class BeadColor(
    val id: String = UUID.randomUUID().toString(),
    val mardCode: String,      // MARD 色号 (唯一标识)
    val cocoCode: String = "",     // COCO 色号
    val manmanCode: String = "",   // 漫漫 色号
    val panpanCode: String = "",    // 盼盼 色号
    val mixiaowoCode: String = "",  // 咪小窝 色号
    val kakaCode: String = "",      // 卡卡 色号 (B/P/R + 数字)
    val colorName: String = "",
    val red: Int = 0,
    val green: Int = 0,
    val blue: Int = 0
) {
    /**
     * 转换为 Compose Color
     */
    fun toComposeColor(): Color = Color(red, green, blue)

    /**
     * 转换为 ARGB Int
     */
    fun toArgb(): Int = (0xFF shl 24) or (red shl 16) or (green shl 8) or blue

    /**
     * 根据色号体系获取显示色号
     */
    fun displayCode(system: ColorSystem): String {
        return when (system) {
            ColorSystem.MARD -> mardCode
            ColorSystem.COCO -> cocoCode.ifEmpty { mardCode }
            ColorSystem.MANMAN -> manmanCode.ifEmpty { mardCode }
            ColorSystem.KAKA -> kakaCode.ifEmpty { mardCode }
            ColorSystem.PANPAN -> panpanCode.ifEmpty { mardCode }
            ColorSystem.MIXIAOWO -> mixiaowoCode.ifEmpty { mardCode }
        }
    }

    /**
     * 检查是否有指定体系的色号
     */
    fun hasCode(system: ColorSystem): Boolean {
        return when (system) {
            ColorSystem.MARD -> mardCode.isNotEmpty()
            ColorSystem.COCO -> cocoCode.isNotEmpty()
            ColorSystem.MANMAN -> manmanCode.isNotEmpty()
            ColorSystem.KAKA -> kakaCode.isNotEmpty()
            ColorSystem.PANPAN -> panpanCode.isNotEmpty()
            ColorSystem.MIXIAOWO -> mixiaowoCode.isNotEmpty()
        }
    }

    companion object {
        fun fromArgb(argb: Int): Triple<Int, Int, Int> {
            return Triple(
                (argb shr 16) and 0xFF,
                (argb shr 8) and 0xFF,
                argb and 0xFF
            )
        }
    }
}

// ============================================================================
// 自定义颜色模型
// ============================================================================

/**
 * 自定义颜色
 * 用户自己添加的色号
 */
data class CustomColor(
    val id: String = UUID.randomUUID().toString(),
    val colorCode: String,  // 色号（如 "C001"）
    val colorHex: String,   // 十六进制颜色值（如 "FF5733"）
    val colorName: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * 转换为 MARD 格式的色号（用于统一管理）
     */
    val mardCode: String
        get() = if (colorCode.startsWith("#")) colorCode else "#$colorCode"

    /**
     * 获取 RGB 值
     */
    fun getRgb(): Triple<Int, Int, Int>? {
        return try {
            val hex = colorHex.removePrefix("#")
            Triple(
                hex.substring(0, 2).toInt(16),
                hex.substring(2, 4).toInt(16),
                hex.substring(4, 6).toInt(16)
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 转换为 BeadColor
     */
    fun toBeadColor(): BeadColor {
        val rgb = getRgb() ?: Triple(128, 128, 128)
        return BeadColor(
            mardCode = mardCode,
            colorName = colorName,
            red = rgb.first,
            green = rgb.second,
            blue = rgb.third
        )
    }
}

// ============================================================================
// 用量记录模型
// ============================================================================

/**
 * 豆子用量
 * 记录项目中使用的每种颜色的数量
 */
data class BeadUsage(
    val id: String = UUID.randomUUID().toString(),
    val colorCode: String,  // 色号
    val brandId: String? = null,  // 关联的品牌 ID
    val quantity: Int,      // 数量
    val isDeducted: Boolean = false  // 是否已从库存扣减
)

// ============================================================================
// 项目记录模型 - 支持层级结构（父项目/子项目）
// ============================================================================

/**
 * 项目记录
 * 支持父项目（文件夹）和子项目（实际项目）的层级结构
 */
data class ProjectRecord(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val date: Long = System.currentTimeMillis(),
    val beadUsage: List<BeadUsage> = emptyList(),
    val brandId: String? = null,      // 执行时关联的品牌
    val isArchived: Boolean = false,  // 是否归档
    val parentId: String? = null,      // 父项目 ID
    val isPlanned: Boolean = true,    // 是否为计划中（未执行）
    val executedDate: Long? = null,    // 执行日期
    val thumbnailBase64: String? = null,  // 缩略图（Base64）
    val finishedImageBase64: String? = null,  // 成品图（Base64）
    val completedDate: Long? = null,   // 完成日期
    val colorSystem: ColorSystem = ColorSystem.MARD  // 项目使用的色号体系
) {
    /**
     * 总豆子数量
     */
    val totalBeads: Int
        get() = beadUsage.sumOf { it.quantity }

    /**
     * 颜色数量
     */
    val colorCount: Int
        get() = beadUsage.size
}

// ============================================================================
// 购买记录模型 - 运输中的订单
// ============================================================================

/**
 * 购买条目
 */
data class PurchaseItem(
    val id: String = UUID.randomUUID().toString(),
    val colorCode: String,
    val quantity: Int
)

/**
 * 购买记录
 * 记录正在运输中的购买订单
 */
data class PurchaseRecord(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val date: Long = System.currentTimeMillis(),
    val brandId: String,
    val items: List<PurchaseItem> = emptyList(),
    val note: String? = null
)

// ============================================================================
// 历史记录模型 - 操作历史
// ============================================================================

/**
 * 历史记录类型
 */
enum class HistoryType {
    // 库存相关
    STOCK_ADD,      // 库存增加
    STOCK_UPDATE,   // 库存更新
    STOCK_DEDUCT,   // 库存扣减

    // 品牌相关
    BRAND_ADD,      // 添加品牌
    BRAND_UPDATE,   // 更新品牌
    BRAND_DELETE,   // 删除品牌

    // 项目相关
    PROJECT_ADD,    // 添加项目
    PROJECT_EXECUTE, // 执行项目
    PROJECT_DELETE,  // 删除项目
    PROJECT_ARCHIVE, // 归档项目
    PROJECT_UNARCHIVE, // 取消归档

    // 采购相关
    PURCHASE_ADD,   // 添加采购
    PURCHASE_COMPLETE; // 采购到货
}

/**
 * 历史记录
 * 记录所有操作历史，支持撤回
 */
data class HistoryRecord(
    val id: String = UUID.randomUUID().toString(),
    val type: HistoryType,
    val timestamp: Long = System.currentTimeMillis(),
    val description: String,

    // 相关 ID（用于撤回）
    val brandId: String? = null,
    val mardCode: String? = null,
    val projectId: String? = null,

    // 变更详情
    val oldValue: Int? = null,
    val newValue: Int? = null,
    val changeAmount: Int? = null,

    // 品牌/项目名称（便于显示）
    val brandName: String? = null,
    val projectName: String? = null
)

// ============================================================================
// AI 识别模型
// ============================================================================

/**
 * AI 识别结果条目
 */
data class AIRecognizedItem(
    val colorCode: String,
    val quantity: Int
)

// ============================================================================
// 备份数据模型
// ============================================================================

/**
 * 备份数据
 * 用于导出/导入完整数据
 */
data class BackupData(
    val backupDate: String,
    val appVersion: String,
    val backupType: String = "manual",
    val brands: List<Brand>,
    val brandStocks: List<BrandStock>,
    val projects: List<ProjectRecord>,
    val customColors: List<CustomColor>,
    val purchaseRecords: List<PurchaseRecord>,
    val currentBrandId: String?,
    val stats: BackupStats
)

data class BackupStats(
    val brandsCount: Int,
    val stocksCount: Int,
    val projectsCount: Int,
    val customColorsCount: Int,
    val purchaseRecordsCount: Int
)

// ============================================================================
// 统计模型
// ============================================================================

/**
 * 品牌统计
 */
data class BrandStats(
    val brand: Brand,
    val totalStock: Int,
    val totalUsed: Int,
    val totalAvailable: Int,
    val lowStockCount: Int,
    val hiddenCount: Int
)

/**
 * 颜色使用排行
 */
data class ColorUsageRank(
    val beadColor: BeadColor,
    val brandStock: BrandStock,
    val rank: Int
)

// ============================================================================
// 工具类
// ============================================================================

/**
 * 库存状态
 */
enum class InventoryStatus {
    OUT_OF_STOCK,  // 库存为0
    LOW,           // 低库存 (< 阈值)
    ENOUGH         // 充足
}

/**
 * 绘图工具
 */
enum class DrawingTool {
    BRUSH,
    ERASER,
    FILL,
    ZOOM
}

/**
 * 画布尺寸预设
 */
enum class CanvasSize(val width: Int, val height: Int, val displayName: String) {
    SIZE_16(16, 16, "16×16"),
    SIZE_24(24, 24, "24×24"),
    SIZE_32(32, 32, "32×32"),
    SIZE_48(48, 48, "48×48"),
    SIZE_64(64, 64, "64×64"),
    SIZE_96(96, 96, "96×96"),
    SIZE_128(128, 128, "128×128"),
    SIZE_256(256, 256, "256×256")
}

/**
 * 库存记录
 */
data class InventoryRecord(
    val id: String = UUID.randomUUID().toString(),
    val colorId: String,
    val type: RecordType,
    val quantity: Int,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

enum class RecordType {
    IN,  // 入库
    OUT  // 出库
}

// ============================================================================
// AI 相关模型
// ============================================================================

/**
 * AI 服务商
 */
enum class AIVendor(
    val displayName: String,
    val defaultBaseUrl: String,
    val defaultModel: String,
    val hint: String
) {
    OPENAI("OpenAI", "https://api.openai.com/v1", "gpt-4o", "使用 OpenAI 的 GPT-4 Vision 模型，支持图片识别"),
    KIMI("Kimi", "https://api.moonshot.cn/v1", "moonshot-v1-128k", "使用 Kimi 的 Moonshot Vision 模型"),
    ANTHROPIC("Anthropic", "https://api.anthropic.com", "claude-3-5-sonnet-20241022", "使用 Claude 3.5 Sonnet 模型"),
    QWEN("通义千问", "https://dashscope.aliyuncs.com/compatible-mode/v1", "qwen-vl-max", "使用阿里云通义千问视觉模型"),
    GEMINI("Gemini", "https://generativelanguage.googleapis.com/v1beta", "gemini-1.5-pro", "使用 Google Gemini 视觉模型");

    companion object {
        fun fromString(value: String): AIVendor {
            return entries.find { it.name == value } ?: OPENAI
        }
    }
}

/**
 * AI 识别结果
 */
data class AIRecognitionResult(
    val id: String = UUID.randomUUID().toString(),
    val colors: List<RecognizedColor>,
    val totalBeads: Int,
    val timestamp: Long = System.currentTimeMillis()
) {
    data class RecognizedColor(
        val mardCode: String,
        val colorName: String,
        val red: Int,
        val green: Int,
        val blue: Int,
        val count: Int,
        val confidence: Float
    )
}
/**
 * 本地快照信息
 */
data class SnapshotInfo(
    val filename: String,
    val snapshotTime: Long,
    val label: String,
    val brandsCount: Int,
    val stocksCount: Int,
    val projectsCount: Int,
    val fileSizeBytes: Long
)

/**
 * 导入数据预览信息
 */
data class ImportPreview(
    val brandsCount: Int,
    val stocksCount: Int,
    val projectsCount: Int,
    val customColorsCount: Int,
    val purchaseRecordsCount: Int,
    val existingBrandsCount: Int,
    val existingStocksCount: Int,
    val existingProjectsCount: Int,
    val conflictBrandNames: List<String>
) {
    val hasConflicts: Boolean get() = conflictBrandNames.isNotEmpty()
}
