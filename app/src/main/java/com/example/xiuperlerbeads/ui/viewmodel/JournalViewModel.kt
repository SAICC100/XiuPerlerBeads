package com.example.xiuperlerbeads.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.xiuperlerbeads.XiuPerlerBeadsApp
import com.example.xiuperlerbeads.domain.model.JournalAttachment
import com.example.xiuperlerbeads.domain.model.JournalCollection
import com.example.xiuperlerbeads.domain.model.JournalEntry
import com.example.xiuperlerbeads.domain.model.JournalTag
import com.example.xiuperlerbeads.domain.model.LocationStat
import com.example.xiuperlerbeads.domain.model.TagExpenseStat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// ============================================================================
// UI State
// ============================================================================

data class JournalState(
    val collections: List<JournalCollection> = emptyList(),
    val tags: List<JournalTag> = emptyList(),
    val entries: List<JournalEntry> = emptyList(),
    val selectedCollectionId: String = JournalCollection.DEFAULT_ID,
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val selectedCollection: JournalCollection?
        get() = collections.find { it.id == selectedCollectionId }

    val filteredEntries: List<JournalEntry>
        get() = entries.filter { it.collectionId == selectedCollectionId }
            .sortedByDescending { it.displayTime }
}

data class SummaryState(
    val locationStats: List<LocationStat> = emptyList(),
    val tagExpenseStats: List<TagExpenseStat> = emptyList(),
    val selectedCollectionId: String = JournalCollection.DEFAULT_ID,
    /** null 表示不筛选 */
    val filterStartTime: Long? = null,
    val filterEndTime: Long? = null,
    val isLoading: Boolean = false
) {
    val totalExpense: Double get() = tagExpenseStats.sumOf { it.totalExpense }
}

// ============================================================================
// ViewModel
// ============================================================================

class JournalViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = (application as XiuPerlerBeadsApp).journalRepository

    private val _state = MutableStateFlow(JournalState())
    val state: StateFlow<JournalState> = _state.asStateFlow()

    private val _summaryState = MutableStateFlow(SummaryState())
    val summaryState: StateFlow<SummaryState> = _summaryState.asStateFlow()

    init {
        loadData()
    }

    // ============================================================================
    // 数据加载
    // ============================================================================

    fun loadData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            withContext(Dispatchers.IO) {
                val collections = repo.collections
                val tags = repo.tags
                val entries = repo.entries
                _state.update {
                    it.copy(
                        collections = collections,
                        tags = tags,
                        entries = entries,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun loadSummaryData() {
        viewModelScope.launch {
            _summaryState.update { it.copy(isLoading = true) }
            withContext(Dispatchers.IO) {
                val s = _summaryState.value
                val locationStats = repo.getLocationStats(s.filterStartTime, s.filterEndTime)
                val tagExpenseStats = repo.getTagExpenseStats(s.filterStartTime, s.filterEndTime)
                _summaryState.update {
                    it.copy(
                        locationStats = locationStats,
                        tagExpenseStats = tagExpenseStats,
                        isLoading = false
                    )
                }
            }
        }
    }

    // ============================================================================
    // 文集操作
    // ============================================================================

    fun selectCollection(collectionId: String) {
        _state.update { it.copy(selectedCollectionId = collectionId) }
    }

    fun addCollection(name: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { repo.addCollection(name) }
            loadData()
        }
    }

    fun updateCollection(collection: JournalCollection) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { repo.updateCollection(collection) }
            loadData()
        }
    }

    fun deleteCollection(collectionId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { repo.deleteCollection(collectionId) }
            // 如果删除的是当前选中文集，切回默认
            if (_state.value.selectedCollectionId == collectionId) {
                _state.update { it.copy(selectedCollectionId = JournalCollection.DEFAULT_ID) }
            }
            loadData()
        }
    }

    // ============================================================================
    // Tag 操作
    // ============================================================================

    fun addTag(name: String, colorHex: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { repo.addTag(name, colorHex) }
            loadData()
        }
    }

    fun deleteTag(tagId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { repo.deleteTag(tagId) }
            loadData()
        }
    }

    // ============================================================================
    // 条目操作
    // ============================================================================

    fun addEntry(
        collectionId: String,
        content: String,
        attachments: List<JournalAttachment>,
        tags: List<JournalTag>,
        entryTime: Long?,
        location: String?,
        expense: Double
    ) {
        viewModelScope.launch {
            val entry = JournalEntry(
                collectionId = collectionId,
                content = content,
                attachments = attachments,
                tags = tags,
                entryTime = entryTime,
                location = location?.takeIf { it.isNotBlank() },
                expense = expense
            )
            withContext(Dispatchers.IO) { repo.addEntry(entry) }
            loadData()
        }
    }

    fun deleteEntry(entryId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { repo.deleteEntry(entryId) }
            loadData()
        }
    }

    fun getEntry(entryId: String): JournalEntry? = repo.getEntry(entryId)

    // ============================================================================
    // 日历查询（供 SummaryScreen 使用）
    // ============================================================================

    fun getDaysWithEntries(year: Int, month: Int): Set<Int> {
        return repo.getDaysWithEntries(year, month, JournalCollection.DEFAULT_ID)
    }

    fun getEntriesByDate(year: Int, month: Int, day: Int): List<JournalEntry> {
        return repo.getEntriesByDate(year, month, day)
    }

    // ============================================================================
    // 汇总筛选
    // ============================================================================

    fun setSummaryCollection(collectionId: String) {
        _summaryState.update { it.copy(selectedCollectionId = collectionId) }
        loadSummaryData()
    }

    fun setSummaryDateFilter(startTime: Long?, endTime: Long?) {
        _summaryState.update { it.copy(filterStartTime = startTime, filterEndTime = endTime) }
        loadSummaryData()
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
