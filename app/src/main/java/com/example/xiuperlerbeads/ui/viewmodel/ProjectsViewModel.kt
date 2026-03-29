package com.example.xiuperlerbeads.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.xiuperlerbeads.data.repository.InventoryRepository
import com.example.xiuperlerbeads.domain.model.ProjectRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 项目列表排序方式
 */
enum class ProjectSortType(val displayName: String) {
    DATE_DESC("最新优先"),
    DATE_ASC("最早优先"),
    NAME_ASC("名称 A-Z"),
    NAME_DESC("名称 Z-A"),
    COLOR_DESC("颜色数量从多到少"),
    COLOR_ASC("颜色数量从少到多"),
    STATUS("进行中优先")
}

/**
 * 项目列表状态
 */
data class ProjectsState(
    val projects: List<ProjectRecord> = emptyList(),
    val filteredProjects: List<ProjectRecord> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedTab: Int = 0, // 0=全部, 1=进行中, 2=已完成
    val sortType: ProjectSortType = ProjectSortType.DATE_DESC,
    val searchQuery: String = ""
)

/**
 * 项目列表 ViewModel
 */
class ProjectsViewModel(application: Application) : AndroidViewModel(application) {
    
    private val _state = MutableStateFlow(ProjectsState())
    val state: StateFlow<ProjectsState> = _state.asStateFlow()
    
    private val repository: InventoryRepository = InventoryRepository(application)
    
    init {
        loadProjects()
    }
    
    fun loadProjects() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                // 获取所有非归档项目
                val planned = repository.getPlannedProjects()
                val executed = repository.getExecutedProjects()
                val allProjects = planned + executed
                
                _state.value = _state.value.copy(
                    projects = allProjects,
                    isLoading = false
                )
                applyFilters()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
    
    fun setTab(tabIndex: Int) {
        _state.value = _state.value.copy(selectedTab = tabIndex)
        applyFilters()
    }
    
    fun setSortType(sortType: ProjectSortType) {
        _state.value = _state.value.copy(sortType = sortType)
        applyFilters()
    }
    
    fun setSearchQuery(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
        applyFilters()
    }
    
    private fun applyFilters() {
        val current = _state.value
        var filtered = current.projects
        
        // 按Tab筛选
        filtered = when (current.selectedTab) {
            1 -> filtered.filter { it.isPlanned && !it.isArchived }
            2 -> filtered.filter { !it.isPlanned }
            else -> filtered.filter { !it.isArchived }
        }
        
        // 按搜索查询筛选
        if (current.searchQuery.isNotBlank()) {
            filtered = filtered.filter { 
                it.name.contains(current.searchQuery, ignoreCase = true)
            }
        }
        
        // 排序
        filtered = when (current.sortType) {
            ProjectSortType.DATE_DESC -> filtered.sortedByDescending { it.date }
            ProjectSortType.DATE_ASC -> filtered.sortedBy { it.date }
            ProjectSortType.NAME_ASC -> filtered.sortedBy { it.name.lowercase() }
            ProjectSortType.NAME_DESC -> filtered.sortedByDescending { it.name.lowercase() }
            ProjectSortType.COLOR_DESC -> filtered.sortedByDescending { it.colorCount }
            ProjectSortType.COLOR_ASC -> filtered.sortedBy { it.colorCount }
            ProjectSortType.STATUS -> filtered.sortedBy { it.completedDate ?: 0L }
        }
        
        _state.value = _state.value.copy(filteredProjects = filtered)
    }
    
    fun deleteProject(projectId: String) {
        viewModelScope.launch {
            try {
                repository.deleteProject(projectId)
                loadProjects()
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }
    
    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
