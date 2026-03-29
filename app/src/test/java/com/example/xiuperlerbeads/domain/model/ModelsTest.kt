package com.example.xiuperlerbeads.domain.model

import org.junit.Assert.*
import org.junit.Test

/**
 * Models 单元测试
 */
class ModelsTest {

    // ============================================================================
    // ColorSystem 测试
    // ============================================================================

    @Test
    fun `ColorSystem fromString returns MARD for unknown value`() {
        // 默认返回 MARD
        assertEquals(ColorSystem.MARD, ColorSystem.fromString("unknown"))
    }

    // ============================================================================
    // BrandStock 测试
    // ============================================================================

    @Test
    fun `BrandStock available calculates correctly`() {
        val stock = BrandStock(
            brandId = "brand1",
            mardCode = "M001",
            stock = 100,
            used = 30
        )
        assertEquals(70, stock.available)
    }

    @Test
    fun `BrandStock isLowStock returns true when below threshold`() {
        val stock = BrandStock(
            brandId = "brand1",
            mardCode = "M001",
            stock = 50,
            used = 0
        )
        assertTrue(stock.isLowStock(100))
    }

    @Test
    fun `BrandStock isLowStock returns false when above threshold`() {
        val stock = BrandStock(
            brandId = "brand1",
            mardCode = "M001",
            stock = 200,
            used = 0
        )
        assertFalse(stock.isLowStock(100))
    }

    @Test
    fun `BrandStock isLowStock uses custom threshold`() {
        val stock = BrandStock(
            brandId = "brand1",
            mardCode = "M001",
            stock = 150,
            used = 0
        )
        assertFalse(stock.isLowStock(100))
        assertTrue(stock.isLowStock(200))
    }

    // ============================================================================
    // BeadColor 测试
    // ============================================================================

    @Test
    fun `BeadColor toComposeColor creates Color object`() {
        val beadColor = BeadColor(
            mardCode = "M001",
            red = 255,
            green = 128,
            blue = 64
        )
        val composeColor = beadColor.toComposeColor()
        // 验证 Color 对象创建成功
        assertNotNull(composeColor)
    }

    @Test
    fun `BeadColor hasCode checks MARD code`() {
        val color = BeadColor(mardCode = "M001", cocoCode = "")
        assertTrue(color.hasCode(ColorSystem.MARD))
    }

    @Test
    fun `BeadColor hasCode returns false when code is empty`() {
        val color = BeadColor(mardCode = "M001", cocoCode = "")
        assertFalse(color.hasCode(ColorSystem.COCO))
    }

    // ============================================================================
    // ProjectRecord 测试
    // ============================================================================

    @Test
    fun `ProjectRecord calculates totalBeads correctly`() {
        val project = ProjectRecord(
            name = "Test Project",
            beadUsage = listOf(
                BeadUsage(colorCode = "M001", quantity = 100),
                BeadUsage(colorCode = "M002", quantity = 50),
                BeadUsage(colorCode = "M003", quantity = 25)
            )
        )
        assertEquals(175, project.totalBeads)
    }

    @Test
    fun `ProjectRecord totalBeads is zero for empty usage`() {
        val project = ProjectRecord(name = "Empty Project")
        assertEquals(0, project.totalBeads)
    }

    @Test
    fun `ProjectRecord colorCount returns correct value`() {
        val project = ProjectRecord(
            name = "Test",
            beadUsage = listOf(
                BeadUsage(colorCode = "M001", quantity = 100),
                BeadUsage(colorCode = "M002", quantity = 50)
            )
        )
        assertEquals(2, project.colorCount)
    }

    @Test
    fun `ProjectRecord colorCount is zero for empty usage`() {
        val project = ProjectRecord(name = "Empty Project")
        assertEquals(0, project.colorCount)
    }

    @Test
    fun `ProjectRecord has parent-child relationship`() {
        val parent = ProjectRecord(name = "Parent Folder")
        val child = ProjectRecord(name = "Child Project", parentId = parent.id)
        
        assertNull(parent.parentId)
        assertEquals(parent.id, child.parentId)
    }

    // ============================================================================
    // BeadUsage 测试
    // ============================================================================

    @Test
    fun `BeadUsage has correct values`() {
        val usage = BeadUsage(colorCode = "M001", quantity = 100)
        
        assertEquals("M001", usage.colorCode)
        assertEquals(100, usage.quantity)
        assertFalse(usage.isDeducted)
    }

    @Test
    fun `BeadUsage can track deduction status`() {
        val notDeducted = BeadUsage(colorCode = "M001", quantity = 100, isDeducted = false)
        val deducted = BeadUsage(colorCode = "M001", quantity = 100, isDeducted = true)
        
        assertFalse(notDeducted.isDeducted)
        assertTrue(deducted.isDeducted)
    }

    // ============================================================================
    // AIVendor 测试
    // ============================================================================

    @Test
    fun`AIVendor fromString returns OPENAI for unknown value`() {
        assertEquals(AIVendor.OPENAI, AIVendor.fromString("unknown"))
    }

    @Test
    fun `AIVendor has display names`() {
        assertEquals("OpenAI", AIVendor.OPENAI.displayName)
        assertEquals("Kimi", AIVendor.KIMI.displayName)
    }

    @Test
    fun `AIVendor has correct URLs`() {
        assertTrue(AIVendor.OPENAI.defaultBaseUrl.contains("openai.com"))
    }

    // ============================================================================
    // HistoryType 测试
    // ============================================================================

    @Test
    fun `HistoryType enum has stock types`() {
        assertNotNull(HistoryType.STOCK_ADD)
        assertNotNull(HistoryType.STOCK_UPDATE)
        assertNotNull(HistoryType.STOCK_DEDUCT)
    }

    @Test
    fun `HistoryType enum has brand types`() {
        assertNotNull(HistoryType.BRAND_ADD)
        assertNotNull(HistoryType.BRAND_UPDATE)
        assertNotNull(HistoryType.BRAND_DELETE)
    }

    @Test
    fun `HistoryType enum has project types`() {
        assertNotNull(HistoryType.PROJECT_ADD)
        assertNotNull(HistoryType.PROJECT_EXECUTE)
        assertNotNull(HistoryType.PROJECT_DELETE)
    }

    @Test
    fun `HistoryType enum has purchase types`() {
        assertNotNull(HistoryType.PURCHASE_ADD)
        assertNotNull(HistoryType.PURCHASE_COMPLETE)
    }

    // ============================================================================
    // PurchaseRecord 测试
    // ============================================================================

    @Test
    fun `PurchaseRecord has correct structure`() {
        val purchase = PurchaseRecord(
            name = "Test Purchase",
            brandId = "brand1",
            items = listOf(
                PurchaseItem(colorCode = "M001", quantity = 100)
            )
        )
        
        assertEquals("Test Purchase", purchase.name)
        assertEquals("brand1", purchase.brandId)
        assertEquals(1, purchase.items.size)
    }

    @Test
    fun `PurchaseItem has correct structure`() {
        val item = PurchaseItem(colorCode = "M001", quantity = 100)
        assertEquals("M001", item.colorCode)
        assertEquals(100, item.quantity)
    }
}
