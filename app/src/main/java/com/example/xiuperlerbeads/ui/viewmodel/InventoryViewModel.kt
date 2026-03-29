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
    val historyRecords: List<HistoryRecord> = emptyList(),
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
                        historyRecords = repository.getHistoryRecords(),
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
                val stock = _state.value.stocks.find {
                    it.brandId == brandId && it.mardCode == mardCode
                } ?: return@launch
                val newQuantity = (stock.stock - amount).coerceAtLeast(0)
                repository.updateStock(brandId, mardCode, newQuantity)
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
}
