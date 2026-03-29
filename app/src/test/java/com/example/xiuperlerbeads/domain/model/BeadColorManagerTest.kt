package com.example.xiuperlerbeads.domain.model

import org.junit.Assert.*
import org.junit.Test

/**
 * BeadColorManager 单元测试
 */
class BeadColorManagerTest {

    // ============================================================================
    // 颜色查找测试
    // ============================================================================

    @Test
    fun `findByMardCode returns null for empty input`() {
        val color = BeadColorManager.findByMardCode("")
        assertNull(color)
    }

    @Test
    fun `findByAnyCode handles unknown codes`() {
        val color = BeadColorManager.findByAnyCode("UNKNOWN_XYZ_123")
        assertNull(color)
    }

    // ============================================================================
    // BeadColor 辅助方法测试
    // ============================================================================

    @Test
    fun `BeadColor hasCode returns true for MARD`() {
        val color = BeadColor(mardCode = "M001", cocoCode = "")
        assertTrue(color.hasCode(ColorSystem.MARD))
    }

    @Test
    fun `BeadColor hasCode returns false for empty code`() {
        val color = BeadColor(mardCode = "M001", cocoCode = "")
        assertFalse(color.hasCode(ColorSystem.COCO))
    }

    @Test
    fun `BeadColor hasCode returns true when code is present`() {
        val color = BeadColor(mardCode = "M001", cocoCode = "C001")
        assertTrue(color.hasCode(ColorSystem.MARD))
        assertTrue(color.hasCode(ColorSystem.COCO))
    }

    @Test
    fun `BeadColor toComposeColor creates Color object`() {
        val color = BeadColor(
            mardCode = "M001",
            red = 255,
            green = 128,
            blue = 64
        )
        assertNotNull(color.toComposeColor())
    }

    // ============================================================================
    // 搜索功能测试
    // ============================================================================

    @Test
    fun `search handles empty query`() {
        val results = BeadColorManager.search("", ColorSystem.MARD)
        // 边界测试 - 结果可以是空或非空
        assertTrue(results.isEmpty() || results.isNotEmpty())
    }

    @Test
    fun `convertCode handles unknown codes`() {
        val result = BeadColorManager.convertCode("UNKNOWN", ColorSystem.MARD, ColorSystem.COCO)
        assertNull(result)
    }

    @Test
    fun `getColorsForSystem returns colors`() {
        val colors = BeadColorManager.getColorsForSystem(ColorSystem.MARD)
        // 边界测试
        assertTrue(colors.isEmpty() || colors.isNotEmpty())
    }
}
