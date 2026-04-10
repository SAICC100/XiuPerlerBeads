package com.example.xiuperlerbeads.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.xiuperlerbeads.data.repository.InventoryRepository
import com.example.xiuperlerbeads.domain.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 库存管理状态
 */
data class InventoryState(
    val brands: List<Brand> = emptyList(),
    val stocks: List<BrandStock> = emptyList(),
    val projects: List<ProjectRecord> = emptyList(),
    val purchaseRecords: List<PurchaseRecord> = emptyList(),
    val historyRecords: List<HistoryRecord> = emptyList(),
    val customColors: List<CustomColor> = emptyList(),
    val snapshots: List<SnapshotInfo> = emptyList(),
    val selectedBrandId: String? = null,
    val searchQuery: String = "",
    val lowStockOnly: Boolean = false,
    val selectedColorSystem: ColorSystem? = null,
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val filteredStocks: List<BrandStock>
        get() {
            var result = stocks

            if (selectedBrandId != null) {
                result = result.filter { it.brandId == selectedBrandId }
            }

            selectedColorSystem?.let { system ->
                result = result.filter { stock ->
                    val color = BeadColorManager.findByMardCode(stock.mardCode)
                    color?.hasCode(system) == true
                }
            }

            if (searchQuery.isNotEmpty()) {
                val query = searchQuery.uppercase()
                result = result.filter { stock ->
                    stock.mardCode.uppercase().contains(query) ||
                    BeadColorManager.findByMardCode(stock.mardCode)?.colorName?.contains(query) == true
                }
            }

            if (lowStockOnly) {
                result = result.filter { it.isLowStock() }
            }

            return result.sortedBy { it.mardCode }
        }

    val selectedBrand: Brand?
        get() = brands.find { it.id == selectedBrandId }

    val lowStockCount: Int
        get() = stocks.count { it.isLowStock() }

    val outOfStockCount: Int
        get() = stocks.count { it.available <= 0 }

    val totalColors: Int
        get() = stocks.size

    val totalQuantity: Int
        get() = stocks.sumOf { it.available }
}

class InventoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = InventoryRepository(application)

    private val _state = MutableStateFlow(InventoryState())
    val state: StateFlow<InventoryState> = _state.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val brands = repository.brands
                val stocks = repository.brandStocks
                val selectedId = _state.value.selectedBrandId ?: brands.firstOrNull()?.id

                _state.update {
                    it.copy(
                        brands = brands,
                        stocks = stocks,
                        projects = repository.projects,
                        purchaseRecords = repository.purchaseRecords,
                        historyRecords = repository.getHistoryRecords(),
                        customColors = repository.customColors,
                        snapshots = repository.listSnapshots(),
                        selectedBrandId = selectedId,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "加载失败: ${e.message}"
                    )
                }
            }
        }
    }

    fun selectBrand(brandId: String?) {
        _state.update { it.copy(selectedBrandId = brandId) }
    }

    fun updateSearchQuery(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }

    fun toggleLowStockOnly() {
        _state.update { it.copy(lowStockOnly = !it.lowStockOnly) }
    }

    fun setColorSystem(system: ColorSystem?) {
        _state.update { it.copy(selectedColorSystem = system) }
    }

    fun addBrand(name: String, colorSystem: ColorSystem, threshold: Int = 100) {
        viewModelScope.launch {
            try {
                repository.addBrand(name, colorSystem, 0)
                loadData()
            } catch (e: Exception) {
                _state.update { it.copy(error = "添加失败: ${e.message}") }
            }
        }
    }

    fun updateBrand(brand: Brand) {
        viewModelScope.launch {
            try {
                repository.updateBrand(brand)
                loadData()
            } catch (e: Exception) {
                _state.update { it.copy(error = "更新失败: ${e.message}") }
            }
        }
    }

    fun deleteBrand(brandId: String) {
        viewModelScope.launch {
            try {
                repository.deleteBrand(brandId)
                if (_state.value.selectedBrandId == brandId) {
                    _state.update { it.copy(selectedBrandId = null) }
                }
                loadData()
            } catch (e: Exception) {
                _state.update { it.copy(error = "删除失败: ${e.message}") }
            }
        }
    }

    fun addStock(brandId: String, mardCode: String, quantity: Int) {
        viewModelScope.launch {
            try {
                repository.addStock(brandId, mardCode, quantity)
                loadData()
            } catch (e: Exception) {
                _state.update { it.copy(error = "添加失败: ${e.message}") }
            }
        }
    }

    fun updateStockQuantity(brandId: String, mardCode: String, newQuantity: Int) {
        viewModelScope.launch {
            try {
                repository.updateStock(brandId, mardCode, newQuantity)
                loadData()
            } catch (e: Exception) {
                _state.update { it.copy(error = "更新失败: ${e.message}") }
            }
        }
    }

    fun deductStock(brandId: String, mardCode: String, amount: Int) {
        viewModelScope.launch {
            try {
                repository.deductFromStock(brandId, mardCode, amount)
                loadData()
            } catch (e: Exception) {
                _state.update { it.copy(error = "扣减失败: ${e.message}") }
            }
        }
    }

    fun importStock(brandId: String, stockList: List<Pair<String, Int>>) {
        viewModelScope.launch {
            try {
                stockList.forEach { (mardCode, quantity) ->
                    repository.addStock(brandId, mardCode, quantity)
                }
                loadData()
            } catch (e: Exception) {
                _state.update { it.copy(error = "导入失败: ${e.message}") }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun getColorInfo(mardCode: String): BeadColor? {
        return BeadColorManager.findByMardCode(mardCode)
    }

    // ── 隐藏色号管理 ────────────────────────────────────────────────────────

    fun hideColor(brandId: String, mardCode: String) {
        viewModelScope.launch {
            try {
                repository.hideColor(brandId, mardCode)
                loadData()
            } catch (e: Exception) {
                _state.update { it.copy(error = "隐藏失败: ${e.message}") }
            }
        }
    }

    fun unhideColor(brandId: String, mardCode: String, defaultStock: Int = 1000) {
        viewModelScope.launch {
            try {
                repository.unhideColor(brandId, mardCode, defaultStock)
                loadData()
            } catch (e: Exception) {
                _state.update { it.copy(error = "取消隐藏失败: ${e.message}") }
            }
        }
    }

    fun getHiddenStocks(brandId: String): List<BrandStock> {
        return _state.value.stocks.filter { it.brandId == brandId && it.isHidden }
    }

    // ── 购买/运输记录 ────────────────────────────────────────────────────────

    fun addPurchaseRecord(name: String, brandId: String, items: List<PurchaseItem>, note: String? = null) {
        viewModelScope.launch {
            try {
                repository.addPurchaseRecord(name, brandId, items, note)
                loadData()
            } catch (e: Exception) {
                _state.update { it.copy(error = "添加采购记录失败: ${e.message}") }
            }
        }
    }

    fun completePurchaseRecord(recordId: String) {
        viewModelScope.launch {
            try {
                repository.completePurchaseRecord(recordId)
                loadData()
            } catch (e: Exception) {
                _state.update { it.copy(error = "确认到货失败: ${e.message}") }
            }
        }
    }

    fun deletePurchaseRecord(recordId: String) {
        viewModelScope.launch {
            try {
                repository.deletePurchaseRecord(recordId)
                loadData()
            } catch (e: Exception) {
                _state.update { it.copy(error = "删除失败: ${e.message}") }
            }
        }
    }

    // ── 自定义色号管理 ────────────────────────────────────────────────────────

    fun addCustomColor(colorCode: String, colorHex: String, colorName: String): Boolean {
        return try {
            val result = repository.addCustomColor(colorCode, colorHex, colorName)
            if (result != null) {
                loadData()
                true
            } else {
                _state.update { it.copy(error = "色号已存在") }
                false
            }
        } catch (e: Exception) {
            _state.update { it.copy(error = "添加失败: ${e.message}") }
            false
        }
    }

    fun updateCustomColor(colorId: String, colorCode: String, colorHex: String, colorName: String) {
        viewModelScope.launch {
            try {
                repository.updateCustomColor(colorId, colorCode, colorHex, colorName)
                loadData()
            } catch (e: Exception) {
                _state.update { it.copy(error = "更新失败: ${e.message}") }
            }
        }
    }

    fun deleteCustomColor(colorId: String) {
        viewModelScope.launch {
            try {
                repository.deleteCustomColor(colorId)
                loadData()
            } catch (e: Exception) {
                _state.update { it.copy(error = "删除失败: ${e.message}") }
            }
        }
    }

    // ── 本地快照管理 ──────────────────────────────────────────────────────────

    fun createSnapshot(label: String = "") {
        viewModelScope.launch {
            try {
                repository.createSnapshot(label)
                // 刷新快照列表
                _state.update { it.copy(snapshots = repository.listSnapshots()) }
            } catch (e: Exception) {
                _state.update { it.copy(error = "创建快照失败: ${e.message}") }
            }
        }
    }

    fun restoreSnapshot(filename: String) {
        viewModelScope.launch {
            try {
                val success = repository.restoreSnapshot(filename)
                if (success) {
                    loadData()
                } else {
                    _state.update { it.copy(error = "恢复快照失败") }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = "恢复失败: ${e.message}") }
            }
        }
    }

    fun deleteSnapshot(filename: String) {
        viewModelScope.launch {
            try {
                repository.deleteSnapshot(filename)
                _state.update { it.copy(snapshots = repository.listSnapshots()) }
            } catch (e: Exception) {
                _state.update { it.copy(error = "删除快照失败: ${e.message}") }
            }
        }
    }


    // ── 历史记录撤销 ──────────────────────────────────────────────────────────

    fun undoHistoryRecord(recordId: String) {
        viewModelScope.launch {
            try {
                val success = repository.undoHistoryRecord(recordId)
                if (success) {
                    loadData()
                } else {
                    _state.update { it.copy(error = "该操作不支持撤销") }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = "撤销失败: ${e.message}") }
            }
        }
    }

    // ── 品牌合并 ─────────────────────────────────────────────────────────────

    fun mergeBrand(sourceBrandId: String, targetBrandId: String) {
        viewModelScope.launch {
            try {
                val success = repository.mergeBrand(sourceBrandId, targetBrandId)
                if (success) {
                    loadData()
                } else {
                    _state.update { it.copy(error = "合并失败：品牌不存在") }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = "合并失败: ${e.message}") }
            }
        }
    }

}