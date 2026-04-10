package com.example.xiuperlerbeads.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.xiuperlerbeads.domain.model.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * 库存数据仓库
 * 管理所有库存、品牌、项目等数据
 * 使用 SharedPreferences + JSON 文件存储
 */
class InventoryRepository(private val context: Context) {

    // Helper extension for optional nullable strings
    private fun JSONObject.optNullableString(key: String): String? {
        return optString(key).takeIf { it.isNotEmpty() }
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // 内存缓存
    private val _brands = mutableListOf<Brand>()
    private val _brandStocks = mutableListOf<BrandStock>()
    private val _projects = mutableListOf<ProjectRecord>()
    private val _customColors = mutableListOf<CustomColor>()
    private val _purchaseRecords = mutableListOf<PurchaseRecord>()
    private val _historyRecords = mutableListOf<HistoryRecord>()

    // 当前选中的品牌
    private var _currentBrandId: String? = null

    val brands: List<Brand> get() = _brands.toList()
    val brandStocks: List<BrandStock> get() = _brandStocks.toList()
    val projects: List<ProjectRecord> get() = _projects.toList()
    val customColors: List<CustomColor> get() = _customColors.toList()
    val purchaseRecords: List<PurchaseRecord> get() = _purchaseRecords.toList()

    val currentBrandId: String? get() = _currentBrandId

    val currentBrand: Brand?
        get() = _currentBrandId?.let { id -> _brands.find { it.id == id } }

    val currentBrandStocks: List<BrandStock>
        get() {
            return _currentBrandId?.let { brandId ->
                _brandStocks.filter { it.brandId == brandId && !it.isHidden }
            } ?: emptyList()
        }

    // ============================================================================
    // 初始化
    // ============================================================================

    init {
        loadAllData()
    }

    private fun loadAllData() {
        _currentBrandId = prefs.getString(KEY_CURRENT_BRAND_ID, null)

        // 加载品牌
        loadBrands()

        // 加载库存
        loadBrandStocks()

        // 加载项目
        loadProjects()

        // 加载自定义颜色
        loadCustomColors()

        // 加载购买记录
        loadPurchaseRecords()

        // 加载历史记录
        loadHistoryRecords()
    }

    private fun loadBrands() {
        val json = prefs.getString(KEY_BRANDS, null) ?: return
        try {
            val array = JSONArray(json)
            _brands.clear()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                _brands.add(parseBrand(obj))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadBrandStocks() {
        val json = prefs.getString(KEY_BRAND_STOCKS, null) ?: return
        try {
            val array = JSONArray(json)
            _brandStocks.clear()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                _brandStocks.add(parseBrandStock(obj))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadProjects() {
        val json = prefs.getString(KEY_PROJECTS, null) ?: return
        try {
            val array = JSONArray(json)
            _projects.clear()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                _projects.add(parseProjectRecord(obj))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadCustomColors() {
        val json = prefs.getString(KEY_CUSTOM_COLORS, null) ?: return
        try {
            val array = JSONArray(json)
            _customColors.clear()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                _customColors.add(parseCustomColor(obj))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadPurchaseRecords() {
        val json = prefs.getString(KEY_PURCHASE_RECORDS, null) ?: return
        try {
            val array = JSONArray(json)
            _purchaseRecords.clear()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                _purchaseRecords.add(parsePurchaseRecord(obj))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadHistoryRecords() {
        val json = prefs.getString(KEY_HISTORY_RECORDS, null) ?: return
        try {
            val array = JSONArray(json)
            _historyRecords.clear()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                _historyRecords.add(parseHistoryRecord(obj))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ============================================================================
    // 保存数据
    // ============================================================================

    private fun saveBrands() {
        val array = JSONArray()
        for (brand in _brands) {
            array.put(toJson(brand))
        }
        prefs.edit().putString(KEY_BRANDS, array.toString()).apply()
    }

    private fun saveBrandStocks() {
        val array = JSONArray()
        for (stock in _brandStocks) {
            array.put(toJson(stock))
        }
        prefs.edit().putString(KEY_BRAND_STOCKS, array.toString()).apply()
    }

    private fun saveProjects() {
        val array = JSONArray()
        for (project in _projects) {
            array.put(toJson(project))
        }
        prefs.edit().putString(KEY_PROJECTS, array.toString()).apply()
    }

    private fun saveCustomColors() {
        val array = JSONArray()
        for (color in _customColors) {
            array.put(toJson(color))
        }
        prefs.edit().putString(KEY_CUSTOM_COLORS, array.toString()).apply()
    }

    private fun savePurchaseRecords() {
        val array = JSONArray()
        for (record in _purchaseRecords) {
            array.put(toJson(record))
        }
        prefs.edit().putString(KEY_PURCHASE_RECORDS, array.toString()).apply()
    }

    private fun saveHistoryRecords() {
        val array = JSONArray()
        for (record in _historyRecords) {
            array.put(toJson(record))
        }
        prefs.edit().putString(KEY_HISTORY_RECORDS, array.toString()).apply()
    }

    private fun saveCurrentBrandId() {
        prefs.edit().putString(KEY_CURRENT_BRAND_ID, _currentBrandId).apply()
    }

    // ============================================================================
    // 品牌管理
    // ============================================================================

    fun addBrand(name: String, colorSystem: ColorSystem = ColorSystem.MARD, defaultStock: Int = 1000): Brand {
        val maxOrder = _brands.maxOfOrNull { it.sortOrder } ?: -1
        val brand = Brand(
            name = name,
            sortOrder = maxOrder + 1,
            colorSystem = colorSystem
        )
        _brands.add(brand)

        // 为新品牌初始化库存
        initializeStockForBrand(brand.id, defaultStock)

        // 如果没有当前品牌，设为当前品牌
        if (_currentBrandId == null) {
            _currentBrandId = brand.id
            saveCurrentBrandId()
        }

        saveBrands()
        saveBrandStocks()

        addHistoryRecord(HistoryType.BRAND_ADD, description = "添加品牌: $name", brandId = brand.id, brandName = name)

        return brand
    }

    fun updateBrand(brand: Brand) {
        val index = _brands.indexOfFirst { it.id == brand.id }
        if (index >= 0) {
            _brands[index] = brand
            saveBrands()
        }
    }

    fun deleteBrand(brandId: String): Boolean {
        val brand = _brands.find { it.id == brandId } ?: return false

        // 删除品牌库存
        _brandStocks.removeAll { it.brandId == brandId }

        // 更新关联项目的品牌
        for (i in _projects.indices) {
            if (_projects[i].brandId == brandId) {
                _projects[i] = _projects[i].copy(brandId = null)
            }
        }

        // 删除品牌
        _brands.removeAll { it.id == brandId }

        // 如果删除的是当前品牌，切换到第一个
        if (_currentBrandId == brandId) {
            _currentBrandId = _brands.firstOrNull()?.id
            saveCurrentBrandId()
        }

        saveBrands()
        saveBrandStocks()
        saveProjects()

        addHistoryRecord(HistoryType.BRAND_DELETE, description = "删除品牌: ${brand.name}", brandName = brand.name)

        return true
    }

    fun selectBrand(brandId: String) {
        if (_brands.any { it.id == brandId }) {
            _currentBrandId = brandId
            saveCurrentBrandId()
        }
    }

    private fun initializeStockForBrand(brandId: String, defaultStock: Int) {
        val allColors = BeadColorManager.getAllColors()
        for (color in allColors) {
            val stock = BrandStock(
                brandId = brandId,
                mardCode = color.mardCode,
                stock = defaultStock,
                isHidden = false
            )
            _brandStocks.add(stock)
        }

        // 为自定义颜色初始化库存
        for (customColor in _customColors) {
            val stock = BrandStock(
                brandId = brandId,
                mardCode = customColor.mardCode,
                stock = 0,
                isHidden = true
            )
            _brandStocks.add(stock)
        }
    }

    // ============================================================================
    // 库存管理
    // ============================================================================

    fun getStock(brandId: String, mardCode: String): BrandStock? {
        return _brandStocks.find { it.brandId == brandId && it.mardCode == mardCode }
    }

    fun updateStock(brandId: String, mardCode: String, newStock: Int) {
        val index = _brandStocks.indexOfFirst { it.brandId == brandId && it.mardCode == mardCode }
        if (index >= 0) {
            val oldStock = _brandStocks[index]
            _brandStocks[index] = oldStock.copy(stock = maxOf(0, newStock), isHidden = false)
            saveBrandStocks()

            addHistoryRecord(
                HistoryType.STOCK_UPDATE,
                description = "更新库存: $mardCode = $newStock",
                brandId = brandId,
                mardCode = mardCode,
                oldValue = oldStock.stock,
                newValue = newStock
            )
        }
    }

    fun addStock(brandId: String, mardCode: String, amount: Int) {
        val index = _brandStocks.indexOfFirst { it.brandId == brandId && it.mardCode == mardCode }
        if (index >= 0) {
            // 更新现有库存
            val oldStock = _brandStocks[index]
            val newStock = oldStock.stock + amount
            _brandStocks[index] = oldStock.copy(stock = newStock, isHidden = false)
            saveBrandStocks()

            if (amount > 0) {
                addHistoryRecord(
                    HistoryType.STOCK_ADD,
                    description = "增加库存: $mardCode +$amount",
                    brandId = brandId,
                    mardCode = mardCode,
                    oldValue = oldStock.stock,
                    newValue = newStock,
                    changeAmount = amount
                )
            }
        } else {
            // 创建新库存记录
            val newStock = BrandStock(
                brandId = brandId,
                mardCode = mardCode,
                stock = amount,
                used = 0,
                isHidden = false
            )
            _brandStocks.add(newStock)
            saveBrandStocks()
            if (amount > 0) {
                addHistoryRecord(
                    HistoryType.STOCK_ADD,
                    description = "添加库存: $mardCode +$amount",
                    brandId = brandId,
                    mardCode = mardCode,
                    oldValue = 0,
                    newValue = amount,
                    changeAmount = amount
                )
            }
        }
    }

    fun deductFromStock(brandId: String, colorCode: String, amount: Int): Boolean {
        val mardCode = BeadColorManager.findByAnyCode(colorCode)?.mardCode ?: return false
        val index = _brandStocks.indexOfFirst { it.brandId == brandId && it.mardCode == mardCode }
        if (index >= 0) {
            val oldStock = _brandStocks[index]
            // used 不能超过 stock，防止 available 出现负数
            val newUsed = minOf(oldStock.used + amount, oldStock.stock)
            _brandStocks[index] = oldStock.copy(used = newUsed, isHidden = false)
            saveBrandStocks()

            val actualDeducted = newUsed - oldStock.used
            addHistoryRecord(
                HistoryType.STOCK_DEDUCT,
                description = "扣减库存: $colorCode -$actualDeducted",
                brandId = brandId,
                mardCode = mardCode,
                oldValue = oldStock.available,
                newValue = oldStock.stock - newUsed,
                changeAmount = -actualDeducted
            )
            return true
        }
        return false
    }

    fun hideColor(brandId: String, mardCode: String) {
        val index = _brandStocks.indexOfFirst { it.brandId == brandId && it.mardCode == mardCode }
        if (index >= 0) {
            val stock = _brandStocks[index]
            _brandStocks[index] = stock.copy(stock = 0, used = 0, isHidden = true)
            saveBrandStocks()
        }
    }

    fun unhideColor(brandId: String, mardCode: String, defaultStock: Int = 1000) {
        val index = _brandStocks.indexOfFirst { it.brandId == brandId && it.mardCode == mardCode }
        if (index >= 0) {
            val stock = _brandStocks[index]
            _brandStocks[index] = stock.copy(stock = defaultStock, used = 0, isHidden = false)
            saveBrandStocks()
        }
    }

    // ============================================================================
    // 品牌统计
    // ============================================================================

    fun totalStock(brandId: String): Int {
        return _brandStocks.filter { it.brandId == brandId && !it.isHidden }.sumOf { it.stock }
    }

    fun totalUsed(brandId: String): Int {
        return _brandStocks.filter { it.brandId == brandId && !it.isHidden }.sumOf { it.used }
    }

    fun totalAvailable(brandId: String): Int {
        return _brandStocks.filter { it.brandId == brandId && !it.isHidden }.sumOf { it.available }
    }

    fun lowStockColors(brandId: String, threshold: Int = 100): List<BrandStock> {
        return _brandStocks.filter { it.brandId == brandId && !it.isHidden && it.available < threshold }
    }

    fun hiddenColorCount(brandId: String): Int {
        return _brandStocks.filter { it.brandId == brandId && it.isHidden }.size
    }

    // ============================================================================
    // 项目管理
    // ============================================================================

    fun addProject(name: String, beadUsage: List<BeadUsage>, colorSystem: ColorSystem = ColorSystem.MARD): ProjectRecord {
        val project = ProjectRecord(
            name = name,
            beadUsage = beadUsage,
            colorSystem = colorSystem
        )
        _projects.add(0, project)
        saveProjects()

        addHistoryRecord(HistoryType.PROJECT_ADD, description = "添加项目: $name", projectId = project.id, projectName = name)

        return project
    }

    fun executeProject(projectId: String, brandId: String) {
        val index = _projects.indexOfFirst { it.id == projectId }
        if (index >= 0) {
            val project = _projects[index]

            // 扣减库存
            for (usage in project.beadUsage) {
                deductFromStock(brandId, usage.colorCode, usage.quantity)
            }

            // 更新项目状态
            val executedProject = project.copy(
                brandId = brandId,
                isPlanned = false,
                executedDate = System.currentTimeMillis()
            )
            _projects[index] = executedProject
            saveProjects()

            addHistoryRecord(HistoryType.PROJECT_EXECUTE, description = "执行项目: ${project.name}", projectId = projectId, projectName = project.name)
        }
    }

    fun deleteProject(projectId: String) {
        val project = _projects.find { it.id == projectId } ?: return
        _projects.removeAll { it.id == projectId }
        saveProjects()

        addHistoryRecord(HistoryType.PROJECT_DELETE, description = "删除项目: ${project.name}", projectId = projectId, projectName = project.name)
    }

    fun archiveProject(projectId: String) {
        val index = _projects.indexOfFirst { it.id == projectId }
        if (index >= 0) {
            val project = _projects[index]
            _projects[index] = project.copy(isArchived = true)
            saveProjects()

            addHistoryRecord(HistoryType.PROJECT_ARCHIVE, description = "归档项目: ${project.name}", projectId = projectId, projectName = project.name)
        }
    }

    fun updateProjectName(projectId: String, newName: String) {
        val index = _projects.indexOfFirst { it.id == projectId }
        if (index >= 0) {
            _projects[index] = _projects[index].copy(name = newName)
            saveProjects()
        }
    }

    fun getPlannedProjects(): List<ProjectRecord> {
        return _projects.filter { it.isPlanned && it.parentId == null }
    }

    fun getExecutedProjects(): List<ProjectRecord> {
        // 已执行 = isPlanned 为 false 且未归档，避免与已归档项目重叠
        return _projects.filter { !it.isPlanned && !it.isArchived }
    }

    fun getArchivedProjects(): List<ProjectRecord> {
        return _projects.filter { it.isArchived }
    }

    // ============================================================================
    // 自定义颜色
    // ============================================================================

    fun addCustomColor(colorCode: String, colorHex: String, colorName: String = ""): CustomColor? {
        // 检查是否已存在
        if (_customColors.any { it.colorCode.uppercase() == colorCode.uppercase() }) {
            return null
        }

        val customColor = CustomColor(
            colorCode = colorCode.uppercase(),
            colorHex = colorHex.removePrefix("#"),
            colorName = colorName
        )
        _customColors.add(customColor)

        // 为所有品牌初始化该颜色的库存
        for (brand in _brands) {
            val stock = BrandStock(
                brandId = brand.id,
                mardCode = customColor.mardCode,
                stock = 0,
                isHidden = true
            )
            _brandStocks.add(stock)
        }

        saveCustomColors()
        saveBrandStocks()

        return customColor
    }

    fun deleteCustomColor(colorId: String) {
        val color = _customColors.find { it.id == colorId } ?: return
        _customColors.removeAll { it.id == colorId }

        // 删除所有品牌中该颜色的库存
        _brandStocks.removeAll { it.mardCode == color.mardCode }

        saveCustomColors()
        saveBrandStocks()
    }


    fun updateCustomColor(colorId: String, colorCode: String, colorHex: String, colorName: String) {
        val index = _customColors.indexOfFirst { it.id == colorId }
        if (index == -1) return
        val old = _customColors[index]
        val newMardCode = if (colorCode.uppercase().startsWith("#")) colorCode.uppercase() else "#${colorCode.uppercase()}"
        // 更新所有品牌中该颜色的 mardCode
        if (old.mardCode != newMardCode) {
            _brandStocks.replaceAll { if (it.mardCode == old.mardCode) it.copy(mardCode = newMardCode) else it }
        }
        _customColors[index] = old.copy(
            colorCode = colorCode.uppercase(),
            colorHex = colorHex.removePrefix("#"),
            colorName = colorName,
            updatedAt = System.currentTimeMillis()
        )
        saveCustomColors()
        saveBrandStocks()
    }
    // ============================================================================
    // 购买记录
    // ============================================================================

    fun addPurchaseRecord(name: String, brandId: String, items: List<PurchaseItem>, note: String? = null): PurchaseRecord {
        val record = PurchaseRecord(
            name = name,
            brandId = brandId,
            items = items,
            note = note
        )
        _purchaseRecords.add(0, record)
        savePurchaseRecords()

        addHistoryRecord(HistoryType.PURCHASE_ADD, description = "添加采购: $name")

        return record
    }

    fun completePurchaseRecord(recordId: String) {
        val index = _purchaseRecords.indexOfFirst { it.id == recordId }
        if (index >= 0) {
            val record = _purchaseRecords[index]

            // 入库
            for (item in record.items) {
                addStock(record.brandId, item.colorCode, item.quantity)
            }

            // 删除记录
            _purchaseRecords.removeAt(index)
            savePurchaseRecords()

            addHistoryRecord(HistoryType.PURCHASE_COMPLETE, description = "采购到货: ${record.name}")
        }
    }

    fun deletePurchaseRecord(recordId: String) {
        val record = _purchaseRecords.find { it.id == recordId } ?: return
        _purchaseRecords.removeAll { it.id == recordId }
        savePurchaseRecords()
    }

    // ============================================================================
    // 历史记录
    // ============================================================================

    fun getHistoryRecords(limit: Int = 100): List<HistoryRecord> {
        return _historyRecords.take(limit)
    }

    private fun addHistoryRecord(
        type: HistoryType,
        description: String,
        brandId: String? = null,
        mardCode: String? = null,
        projectId: String? = null,
        oldValue: Int? = null,
        newValue: Int? = null,
        changeAmount: Int? = null,
        brandName: String? = null,
        projectName: String? = null
    ) {
        val record = HistoryRecord(
            type = type,
            description = description,
            brandId = brandId,
            mardCode = mardCode,
            projectId = projectId,
            oldValue = oldValue,
            newValue = newValue,
            changeAmount = changeAmount,
            brandName = brandName,
            projectName = projectName
        )
        _historyRecords.add(0, record)

        // 只保留最近 500 条记录
        if (_historyRecords.size > 500) {
            _historyRecords.removeAt(_historyRecords.lastIndex)
        }

        saveHistoryRecords()
    }


    /**
     * 撤销指定历史记录（仅支持库存相关操作）
     * @return true=成功, false=无法撤销
     */
    fun undoHistoryRecord(recordId: String): Boolean {
        val record = _historyRecords.find { it.id == recordId } ?: return false
        val canUndo = record.type in listOf(HistoryType.STOCK_ADD, HistoryType.STOCK_UPDATE, HistoryType.STOCK_DEDUCT)
        if (!canUndo) return false

        val brandId = record.brandId ?: return false
        val mardCode = record.mardCode ?: return false
        val restoreValue = record.oldValue ?: return false

        // 恢复到操作前的库存值
        val index = _brandStocks.indexOfFirst { it.brandId == brandId && it.mardCode == mardCode }
        if (index == -1) return false
        // STOCK_DEDUCT 记录的 oldValue 是扣减前的 available（stock-used），恢复时需还原 used
        // STOCK_ADD/STOCK_UPDATE 记录的 oldValue 是 stock，直接还原 stock
        if (record.type == HistoryType.STOCK_DEDUCT) {
            val current = _brandStocks[index]
            val restoredUsed = current.stock - restoreValue
            _brandStocks[index] = current.copy(used = restoredUsed.coerceAtLeast(0))
        } else {
            _brandStocks[index] = _brandStocks[index].copy(stock = restoreValue)
        }
        saveBrandStocks()

        // 标记此历史记录为已撤销（从列表中移除）
        _historyRecords.removeAll { it.id == recordId }
        saveHistoryRecords()

        return true
    }

    // ============================================================================
    // 备份与恢复
    // ============================================================================

    fun exportBackup(): BackupData {
        return BackupData(
            backupDate = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                .format(java.util.Date()),
            appVersion = "1.0.0",
            brands = _brands.toList(),
            brandStocks = _brandStocks.toList(),
            projects = _projects.toList(),
            customColors = _customColors.toList(),
            purchaseRecords = _purchaseRecords.toList(),
            currentBrandId = _currentBrandId,
            stats = BackupStats(
                brandsCount = _brands.size,
                stocksCount = _brandStocks.size,
                projectsCount = _projects.size,
                customColorsCount = _customColors.size,
                purchaseRecordsCount = _purchaseRecords.size
            )
        )
    }

    fun importBackup(backup: BackupData) {
        _brands.clear()
        _brands.addAll(backup.brands)

        _brandStocks.clear()
        _brandStocks.addAll(backup.brandStocks)

        _projects.clear()
        _projects.addAll(backup.projects)

        _customColors.clear()
        _customColors.addAll(backup.customColors)

        _purchaseRecords.clear()
        _purchaseRecords.addAll(backup.purchaseRecords)

        _currentBrandId = backup.currentBrandId

        saveBrands()
        saveBrandStocks()
        saveProjects()
        saveCustomColors()
        savePurchaseRecords()
        saveCurrentBrandId()
    }

    // ============================================================================
    // JSON 解析工具
    // ============================================================================

    private fun parseBrand(obj: JSONObject): Brand {
        return Brand(
            id = obj.optString("id", java.util.UUID.randomUUID().toString()),
            name = obj.optString("name", ""),
            sortOrder = obj.optInt("sortOrder", 0),
            createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
            lowStockThreshold = obj.optInt("lowStockThreshold", 100),
            colorSystem = ColorSystem.fromString(obj.optString("colorSystem", "MARD"))
        )
    }

    private fun parseBrandStock(obj: JSONObject): BrandStock {
        return BrandStock(
            id = obj.optString("id", java.util.UUID.randomUUID().toString()),
            brandId = obj.optString("brandId", ""),
            mardCode = obj.optString("mardCode", ""),
            stock = obj.optInt("stock", 0),
            used = obj.optInt("used", 0),
            isHidden = obj.optBoolean("isHidden", false)
        )
    }

    private fun parseProjectRecord(obj: JSONObject): ProjectRecord {
        val usageArray = obj.optJSONArray("beadUsage") ?: JSONArray()
        val beadUsage = mutableListOf<BeadUsage>()
        for (i in 0 until usageArray.length()) {
            val usageObj = usageArray.getJSONObject(i)
            beadUsage.add(BeadUsage(
                id = usageObj.optString("id", java.util.UUID.randomUUID().toString()),
                colorCode = usageObj.optString("colorCode", ""),
                brandId = usageObj.optNullableString("brandId"),
                quantity = usageObj.optInt("quantity", 0),
                isDeducted = usageObj.optBoolean("isDeducted", false)
            ))
        }

        return ProjectRecord(
            id = obj.optString("id", java.util.UUID.randomUUID().toString()),
            name = obj.optString("name", ""),
            date = obj.optLong("date", System.currentTimeMillis()),
            beadUsage = beadUsage,
            brandId = obj.optNullableString("brandId"),
            isArchived = obj.optBoolean("isArchived", false),
            parentId = obj.optNullableString("parentId"),
            isPlanned = obj.optBoolean("isPlanned", true),
            executedDate = if (obj.has("executedDate") && !obj.isNull("executedDate")) obj.optLong("executedDate") else null,
            thumbnailBase64 = obj.optNullableString("thumbnailBase64"),
            finishedImageBase64 = obj.optNullableString("finishedImageBase64"),
            completedDate = if (obj.has("completedDate") && !obj.isNull("completedDate")) obj.optLong("completedDate") else null,
            colorSystem = ColorSystem.fromString(obj.optString("colorSystem", "MARD"))
        )
    }

    private fun parseCustomColor(obj: JSONObject): CustomColor {
        return CustomColor(
            id = obj.optString("id", java.util.UUID.randomUUID().toString()),
            colorCode = obj.optString("colorCode", ""),
            colorHex = obj.optString("colorHex", ""),
            colorName = obj.optString("colorName", ""),
            createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
            updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
        )
    }

    private fun parsePurchaseRecord(obj: JSONObject): PurchaseRecord {
        val itemsArray = obj.optJSONArray("items") ?: JSONArray()
        val items = mutableListOf<PurchaseItem>()
        for (i in 0 until itemsArray.length()) {
            val itemObj = itemsArray.getJSONObject(i)
            items.add(PurchaseItem(
                id = itemObj.optString("id", java.util.UUID.randomUUID().toString()),
                colorCode = itemObj.optString("colorCode", ""),
                quantity = itemObj.optInt("quantity", 0)
            ))
        }

        return PurchaseRecord(
            id = obj.optString("id", java.util.UUID.randomUUID().toString()),
            name = obj.optString("name", ""),
            date = obj.optLong("date", System.currentTimeMillis()),
            brandId = obj.optString("brandId", ""),
            items = items,
            note = obj.optNullableString("note")
        )
    }

    private fun parseHistoryRecord(obj: JSONObject): HistoryRecord {
        return HistoryRecord(
            id = obj.optString("id", java.util.UUID.randomUUID().toString()),
            type = try { HistoryType.valueOf(obj.optString("type", "STOCK_ADD")) } catch (e: Exception) { HistoryType.STOCK_ADD },
            timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
            description = obj.optString("description", ""),
            brandId = obj.optNullableString("brandId"),
            mardCode = obj.optNullableString("mardCode"),
            projectId = obj.optNullableString("projectId"),
            oldValue = if (obj.has("oldValue") && !obj.isNull("oldValue")) obj.optInt("oldValue") else null,
            newValue = if (obj.has("newValue") && !obj.isNull("newValue")) obj.optInt("newValue") else null,
            changeAmount = if (obj.has("changeAmount") && !obj.isNull("changeAmount")) obj.optInt("changeAmount") else null,
            brandName = obj.optNullableString("brandName"),
            projectName = obj.optNullableString("projectName")
        )
    }

    // ============================================================================
    // JSON 转换工具
    // ============================================================================

    private fun toJson(brand: Brand): JSONObject {
        return JSONObject().apply {
            put("id", brand.id)
            put("name", brand.name)
            put("sortOrder", brand.sortOrder)
            put("createdAt", brand.createdAt)
            put("lowStockThreshold", brand.lowStockThreshold)
            put("colorSystem", brand.colorSystem.name)
        }
    }

    private fun toJson(stock: BrandStock): JSONObject {
        return JSONObject().apply {
            put("id", stock.id)
            put("brandId", stock.brandId)
            put("mardCode", stock.mardCode)
            put("stock", stock.stock)
            put("used", stock.used)
            put("isHidden", stock.isHidden)
        }
    }

    private fun toJson(project: ProjectRecord): JSONObject {
        val usageArray = JSONArray()
        for (usage in project.beadUsage) {
            usageArray.put(JSONObject().apply {
                put("id", usage.id)
                put("colorCode", usage.colorCode)
                put("brandId", usage.brandId)
                put("quantity", usage.quantity)
                put("isDeducted", usage.isDeducted)
            })
        }

        return JSONObject().apply {
            put("id", project.id)
            put("name", project.name)
            put("date", project.date)
            put("beadUsage", usageArray)
            put("brandId", project.brandId)
            put("isArchived", project.isArchived)
            put("parentId", project.parentId)
            put("isPlanned", project.isPlanned)
            put("executedDate", project.executedDate)
            put("thumbnailBase64", project.thumbnailBase64)
            put("finishedImageBase64", project.finishedImageBase64)
            put("completedDate", project.completedDate)
            put("colorSystem", project.colorSystem.name)
        }
    }

    private fun toJson(color: CustomColor): JSONObject {
        return JSONObject().apply {
            put("id", color.id)
            put("colorCode", color.colorCode)
            put("colorHex", color.colorHex)
            put("colorName", color.colorName)
            put("createdAt", color.createdAt)
            put("updatedAt", color.updatedAt)
        }
    }

    private fun toJson(record: PurchaseRecord): JSONObject {
        val itemsArray = JSONArray()
        for (item in record.items) {
            itemsArray.put(JSONObject().apply {
                put("id", item.id)
                put("colorCode", item.colorCode)
                put("quantity", item.quantity)
            })
        }

        return JSONObject().apply {
            put("id", record.id)
            put("name", record.name)
            put("date", record.date)
            put("brandId", record.brandId)
            put("items", itemsArray)
            put("note", record.note)
        }
    }

    private fun toJson(record: HistoryRecord): JSONObject {
        return JSONObject().apply {
            put("id", record.id)
            put("type", record.type.name)
            put("timestamp", record.timestamp)
            put("description", record.description)
            put("brandId", record.brandId)
            put("mardCode", record.mardCode)
            put("projectId", record.projectId)
            put("oldValue", record.oldValue)
            put("newValue", record.newValue)
            put("changeAmount", record.changeAmount)
            put("brandName", record.brandName)
            put("projectName", record.projectName)
        }
    }

    // ============================================================================
    // 画布项目 JSON (用于 CanvasEditor)
    // ============================================================================

    // 画布项目存储在单独的文件中
    private val canvasProjectsDir: File
        get() {
            val dir = File(context.filesDir, "canvas_projects")
            if (!dir.exists()) dir.mkdirs()
            return dir
        }

    /**
     * 获取画布项目 JSON
     */
    fun getProjectJson(projectId: String): String? {
        return try {
            val file = File(canvasProjectsDir, "$projectId.json")
            if (file.exists()) file.readText() else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 保存画布项目 JSON
     */
    fun saveProjectJson(projectId: String, json: String) {
        try {
            val file = File(canvasProjectsDir, "$projectId.json")
            file.writeText(json)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 删除画布项目
     */
    fun deleteProjectJson(projectId: String) {
        try {
            val file = File(canvasProjectsDir, "$projectId.json")
            if (file.exists()) file.delete()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 获取所有画布项目列表
     */
    fun getAllCanvasProjects(): List<Pair<String, String>> {
        return try {
            canvasProjectsDir.listFiles()
                ?.filter { it.extension == "json" }
                ?.map { Pair(it.nameWithoutExtension, it.readText()) }
                ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // ============================================================================
    // 数据备份和恢复
    // ============================================================================

    /**
     * 导出所有数据为 JSON
     */
    fun exportAllData(): String {
        val data = JSONObject().apply {
            put("brands", prefs.getString(KEY_BRANDS, "[]"))
            put("brandStocks", prefs.getString(KEY_BRAND_STOCKS, "[]"))
            put("projects", prefs.getString(KEY_PROJECTS, "[]"))
            put("customColors", prefs.getString(KEY_CUSTOM_COLORS, "[]"))
            put("purchaseRecords", prefs.getString(KEY_PURCHASE_RECORDS, "[]"))
            put("exportDate", System.currentTimeMillis())
        }
        return data.toString(2)
    }

    /**
     * 导入数据
     */
    fun importData(json: String): Boolean {
        return try {
            val data = JSONObject(json)
            
            // 恢复品牌
            data.optJSONArray("brands")?.let { array ->
                val brandsJson = array.toString()
                prefs.edit().putString(KEY_BRANDS, brandsJson).apply()
                loadBrands()
            }
            
            // 恢复库存
            data.optJSONArray("brandStocks")?.let { array ->
                val stocksJson = array.toString()
                prefs.edit().putString(KEY_BRAND_STOCKS, stocksJson).apply()
                loadBrandStocks()
            }
            
            // 恢复项目
            data.optJSONArray("projects")?.let { array ->
                val projectsJson = array.toString()
                prefs.edit().putString(KEY_PROJECTS, projectsJson).apply()
                loadProjects()
            }
            
            // 恢复自定义颜色
            data.optJSONArray("customColors")?.let { array ->
                val colorsJson = array.toString()
                prefs.edit().putString(KEY_CUSTOM_COLORS, colorsJson).apply()
                loadCustomColors()
            }

            // 恢复采购记录（原来遗漏了此项）
            data.optJSONArray("purchaseRecords")?.let { array ->
                val recordsJson = array.toString()
                prefs.edit().putString(KEY_PURCHASE_RECORDS, recordsJson).apply()
                loadPurchaseRecords()
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * 清除所有数据
     */
    fun clearAllData() {
        _brands.clear()
        _brandStocks.clear()
        _projects.clear()
        _customColors.clear()
        _purchaseRecords.clear()
        _historyRecords.clear()
        _currentBrandId = null
        
        // 清除画布项目
        canvasProjectsDir.listFiles()?.forEach { it.delete() }
        
        // 清除 SharedPreferences
        prefs.edit().clear().apply()
    }


    // ============================================================================
    // 本地快照管理（最多保留 10 个版本）
    // ============================================================================

    private val snapshotsDir: java.io.File
        get() = java.io.File(context.filesDir, "snapshots").also { it.mkdirs() }

    /**
     * 创建本地快照，自动维护最多 10 个版本（超出则删最旧的）
     * @param label 快照标签，默认为当前时间
     * @return 是否成功
     */
    fun createSnapshot(label: String = ""): Boolean {
        return try {
            val ts = System.currentTimeMillis()
            val sdf = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault())
            val filename = "snapshot_${sdf.format(java.util.Date(ts))}.json"
            val file = java.io.File(snapshotsDir, filename)

            val json = org.json.JSONObject().apply {
                put("brands", prefs.getString(KEY_BRANDS, "[]"))
                put("brandStocks", prefs.getString(KEY_BRAND_STOCKS, "[]"))
                put("projects", prefs.getString(KEY_PROJECTS, "[]"))
                put("customColors", prefs.getString(KEY_CUSTOM_COLORS, "[]"))
                put("purchaseRecords", prefs.getString(KEY_PURCHASE_RECORDS, "[]"))
                put("snapshotTime", ts)
                put("label", label)
                put("brandsCount", _brands.size)
                put("stocksCount", _brandStocks.size)
                put("projectsCount", _projects.size)
            }
            file.writeText(json.toString(2))

            // 只保留最新 10 个快照
            val allSnapshots = snapshotsDir.listFiles { f -> f.name.startsWith("snapshot_") && f.name.endsWith(".json") }
                ?.sortedBy { it.lastModified() } ?: emptyList()
            if (allSnapshots.size > 10) {
                allSnapshots.take(allSnapshots.size - 10).forEach { it.delete() }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 获取所有本地快照列表（按时间倒序）
     */
    fun listSnapshots(): List<SnapshotInfo> {
        return snapshotsDir.listFiles { f -> f.name.startsWith("snapshot_") && f.name.endsWith(".json") }
            ?.mapNotNull { file ->
                try {
                    val json = org.json.JSONObject(file.readText())
                    SnapshotInfo(
                        filename = file.name,
                        snapshotTime = json.optLong("snapshotTime", file.lastModified()),
                        label = json.optString("label", ""),
                        brandsCount = json.optInt("brandsCount", 0),
                        stocksCount = json.optInt("stocksCount", 0),
                        projectsCount = json.optInt("projectsCount", 0),
                        fileSizeBytes = file.length()
                    )
                } catch (e: Exception) { null }
            }
            ?.sortedByDescending { it.snapshotTime }
            ?: emptyList()
    }

    /**
     * 从快照恢复数据
     */
    fun restoreSnapshot(filename: String): Boolean {
        return try {
            val file = java.io.File(snapshotsDir, filename)
            if (!file.exists()) return false
            importData(file.readText())
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 删除指定快照
     */
    fun deleteSnapshot(filename: String): Boolean {
        return try {
            java.io.File(snapshotsDir, filename).delete()
        } catch (e: Exception) {
            false
        }
    }


    /**
     * 预览导入数据（不实际写入）
     * @return ImportPreview 包含数量统计和冲突信息
     */
    fun previewImport(json: String): ImportPreview? {
        return try {
            val data = org.json.JSONObject(json)
            val brandsArray = data.optJSONArray("brands")
            val stocksArray = data.optJSONArray("brandStocks")
            val projectsArray = data.optJSONArray("projects")
            val customColorsArray = data.optJSONArray("customColors")
            val purchaseArray = data.optJSONArray("purchaseRecords")

            val incomingBrandCount = brandsArray?.length() ?: 0
            val incomingStockCount = stocksArray?.length() ?: 0
            val incomingProjectCount = projectsArray?.length() ?: 0
            val incomingCustomColorCount = customColorsArray?.length() ?: 0

            // 检测品牌名称冲突
            val conflictBrandNames = mutableListOf<String>()
            if (brandsArray != null) {
                for (i in 0 until brandsArray.length()) {
                    val incomingName = brandsArray.getJSONObject(i).optString("name", "")
                    if (_brands.any { it.name == incomingName }) {
                        conflictBrandNames.add(incomingName)
                    }
                }
            }

            ImportPreview(
                brandsCount = incomingBrandCount,
                stocksCount = incomingStockCount,
                projectsCount = incomingProjectCount,
                customColorsCount = incomingCustomColorCount,
                purchaseRecordsCount = purchaseArray?.length() ?: 0,
                existingBrandsCount = _brands.size,
                existingStocksCount = _brandStocks.size,
                existingProjectsCount = _projects.size,
                conflictBrandNames = conflictBrandNames
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 合并导入：将新数据合并到现有数据（品牌名不冲突时添加，冲突时跳过）
     */
    fun importDataMerge(json: String): Boolean {
        return try {
            val data = org.json.JSONObject(json)

            data.optJSONArray("brands")?.let { array ->
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val brand = parseBrand(obj)
                    if (_brands.none { it.name == brand.name }) {
                        _brands.add(brand)
                    }
                }
                saveBrands()
            }

            data.optJSONArray("brandStocks")?.let { array ->
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val stock = parseBrandStock(obj)
                    if (_brandStocks.none { it.brandId == stock.brandId && it.mardCode == stock.mardCode }) {
                        _brandStocks.add(stock)
                    }
                }
                saveBrandStocks()
            }

            data.optJSONArray("projects")?.let { array ->
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val project = parseProjectRecord(obj)
                    if (_projects.none { it.id == project.id }) {
                        _projects.add(project)
                    }
                }
                saveProjects()
            }

            data.optJSONArray("customColors")?.let { array ->
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val color = parseCustomColor(obj)
                    if (_customColors.none { it.colorCode.equals(color.colorCode, ignoreCase = true) }) {
                        _customColors.add(color)
                    }
                }
                saveCustomColors()
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // ============================================================================
    // 品牌合并
    // ============================================================================

    /**
     * 将 sourceBrandId 的库存累加到 targetBrandId，然后删除源品牌。
     * 规则：同色号数量相加；目标品牌无此色号时直接迁移（重新关联 brandId）。
     * @return true 成功，false 品牌不存在
     */
    fun mergeBrand(sourceBrandId: String, targetBrandId: String): Boolean {
        val source = _brands.find { it.id == sourceBrandId } ?: return false
        val target = _brands.find { it.id == targetBrandId } ?: return false

        // 遍历源品牌所有库存，合并到目标品牌
        val sourceStocks = _brandStocks.filter { it.brandId == sourceBrandId }
        for (srcStock in sourceStocks) {
            val tgtIndex = _brandStocks.indexOfFirst {
                it.brandId == targetBrandId && it.mardCode == srcStock.mardCode
            }
            if (tgtIndex >= 0) {
                // 目标品牌已有此色号：数量相加
                val tgt = _brandStocks[tgtIndex]
                _brandStocks[tgtIndex] = tgt.copy(
                    stock = tgt.stock + srcStock.stock,
                    used = tgt.used + srcStock.used,
                    isHidden = tgt.isHidden && srcStock.isHidden
                )
            } else {
                // 目标品牌没有此色号：直接迁移
                _brandStocks.add(srcStock.copy(id = srcStock.id, brandId = targetBrandId))
            }
        }

        // 删除源品牌所有库存记录
        _brandStocks.removeAll { it.brandId == sourceBrandId }

        // 迁移项目
        for (i in _projects.indices) {
            if (_projects[i].brandId == sourceBrandId) {
                _projects[i] = _projects[i].copy(brandId = targetBrandId)
            }
        }

        // 删除源品牌
        _brands.removeAll { it.id == sourceBrandId }
        if (_currentBrandId == sourceBrandId) {
            _currentBrandId = targetBrandId
            saveCurrentBrandId()
        }

        saveBrands()
        saveBrandStocks()
        saveProjects()

        addHistoryRecord(
            HistoryType.BRAND_DELETE,
            description = "合并品牌「${source.name}」→「${target.name}」",
            brandId = targetBrandId,
            brandName = target.name
        )

        return true
    }

    companion object {
        private const val PREFS_NAME = "inventory_prefs"
        private const val KEY_BRANDS = "brands"
        private const val KEY_BRAND_STOCKS = "brand_stocks"
        private const val KEY_PROJECTS = "projects"
        private const val KEY_CUSTOM_COLORS = "custom_colors"
        private const val KEY_PURCHASE_RECORDS = "purchase_records"
        private const val KEY_HISTORY_RECORDS = "history_records"
        private const val KEY_CURRENT_BRAND_ID = "current_brand_id"
    }
}
