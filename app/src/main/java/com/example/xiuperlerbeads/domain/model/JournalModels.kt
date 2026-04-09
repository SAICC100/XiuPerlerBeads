package com.example.xiuperlerbeads.domain.model

import androidx.compose.ui.graphics.Color
import java.util.UUID

/**
 * 文集（分类标签），用户可自定义创建
 * 默认文集：【手帐】，具有时间/地点/花费等扩展字段
 */
data class JournalCollection(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val isDefault: Boolean = false,          // 是否为【手帐】默认文集
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val DEFAULT_ID = "default_journal"
        val DEFAULT = JournalCollection(
            id = DEFAULT_ID,
            name = "手帐",
            isDefault = true,
            sortOrder = 0
        )
    }
}

/**
 * Tag（自定义颜色+名称）
 */
data class JournalTag(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val colorHex: String = "FF6B6B",        // 默认橘红色
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toComposeColor(): Color {
        return try {
            Color(android.graphics.Color.parseColor("#$colorHex"))
        } catch (e: Exception) {
            Color(0xFFFF6B6B)
        }
    }
}

/**
 * 附件类型
 */
enum class AttachmentType {
    IMAGE, FILE
}

/**
 * 附件
 */
data class JournalAttachment(
    val id: String = UUID.randomUUID().toString(),
    val type: AttachmentType,
    val uri: String,                          // 文件 URI（持久化权限 URI）
    val fileName: String = "",
    val fileSizeBytes: Long = 0L
)

/**
 * 手帐条目
 * collectionId == JournalCollection.DEFAULT_ID 时，handZhang 扩展字段有效
 */
data class JournalEntry(
    val id: String = UUID.randomUUID().toString(),
    val collectionId: String = JournalCollection.DEFAULT_ID,
    val content: String = "",
    val attachments: List<JournalAttachment> = emptyList(),
    val tags: List<JournalTag> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),

    // ---- 【手帐】文集专属字段 ----
    val entryTime: Long? = null,              // 记录时间（默认当时）
    val location: String? = null,             // 地点
    val expense: Double = 0.0                 // 花费金额
) {
    val isHandZhang: Boolean get() = collectionId == JournalCollection.DEFAULT_ID

    /** 只有图片类型的附件 */
    val images: List<JournalAttachment> get() = attachments.filter { it.type == AttachmentType.IMAGE }

    /** 非图片的附件 */
    val files: List<JournalAttachment> get() = attachments.filter { it.type == AttachmentType.FILE }

    /** 实际记录时间（优先 entryTime，其次 createdAt） */
    val displayTime: Long get() = entryTime ?: createdAt
}

/**
 * 地点访问统计
 */
data class LocationStat(
    val location: String,
    val count: Int
)

/**
 * Tag 花费统计
 */
data class TagExpenseStat(
    val tag: JournalTag,
    val totalExpense: Double,
    val entryCount: Int
)
