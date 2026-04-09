package com.example.xiuperlerbeads.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.xiuperlerbeads.domain.model.AttachmentType
import com.example.xiuperlerbeads.domain.model.JournalAttachment
import com.example.xiuperlerbeads.domain.model.JournalCollection
import com.example.xiuperlerbeads.domain.model.JournalEntry
import com.example.xiuperlerbeads.domain.model.JournalTag
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * 手帐数据仓库
 * 使用 SharedPreferences + JSON 持久化
 */
class JournalRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // 内存缓存
    private val _collections = mutableListOf<JournalCollection>()
    private val _entries = mutableListOf<JournalEntry>()
    private val _tags = mutableListOf<JournalTag>()

    val collections: List<JournalCollection> get() = _collections.toList()
    val entries: List<JournalEntry> get() = _entries.toList()
    val tags: List<JournalTag> get() = _tags.toList()

    init {
        loadAll()
        ensureDefaultCollection()
    }

    // ============================================================================
    // 初始化
    // ============================================================================

    private fun loadAll() {
        loadCollections()
        loadTags()
        loadEntries()
    }

    /** 确保【手帐】默认文集始终存在 */
    private fun ensureDefaultCollection() {
        if (_collections.none { it.id == JournalCollection.DEFAULT_ID }) {
            _collections.add(0, JournalCollection.DEFAULT)
            saveCollections()
        }
    }

    private fun loadCollections() {
        val json = prefs.getString(KEY_COLLECTIONS, null) ?: return
        try {
            val arr = JSONArray(json)
            _collections.clear()
            for (i in 0 until arr.length()) {
                _collections.add(parseCollection(arr.getJSONObject(i)))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadTags() {
        val json = prefs.getString(KEY_TAGS, null) ?: return
        try {
            val arr = JSONArray(json)
            _tags.clear()
            for (i in 0 until arr.length()) {
                _tags.add(parseTag(arr.getJSONObject(i)))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadEntries() {
        val json = prefs.getString(KEY_ENTRIES, null) ?: return
        try {
            val arr = JSONArray(json)
            _entries.clear()
            for (i in 0 until arr.length()) {
                _entries.add(parseEntry(arr.getJSONObject(i)))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ============================================================================
    // 文集管理
    // ============================================================================

    fun addCollection(name: String): JournalCollection {
        val maxOrder = _collections.maxOfOrNull { it.sortOrder } ?: 0
        val collection = JournalCollection(name = name, sortOrder = maxOrder + 1)
        _collections.add(collection)
        saveCollections()
        return collection
    }

    fun updateCollection(collection: JournalCollection) {
        val idx = _collections.indexOfFirst { it.id == collection.id }
        if (idx >= 0) {
            _collections[idx] = collection
            saveCollections()
        }
    }

    fun deleteCollection(collectionId: String): Boolean {
        // 默认文集不允许删除
        if (collectionId == JournalCollection.DEFAULT_ID) return false
        _collections.removeAll { it.id == collectionId }
        saveCollections()
        return true
    }

    // ============================================================================
    // Tag 管理
    // ============================================================================

    fun addTag(name: String, colorHex: String): JournalTag {
        val tag = JournalTag(name = name, colorHex = colorHex)
        _tags.add(tag)
        saveTags()
        return tag
    }

    fun deleteTag(tagId: String) {
        _tags.removeAll { it.id == tagId }
        saveTags()
    }

    fun getTagById(tagId: String): JournalTag? = _tags.find { it.id == tagId }

    // ============================================================================
    // 条目管理
    // ============================================================================

    fun addEntry(entry: JournalEntry): JournalEntry {
        _entries.add(0, entry)
        saveEntries()
        return entry
    }

    fun updateEntry(entry: JournalEntry) {
        val idx = _entries.indexOfFirst { it.id == entry.id }
        if (idx >= 0) {
            _entries[idx] = entry.copy(updatedAt = System.currentTimeMillis())
            saveEntries()
        }
    }

    fun deleteEntry(entryId: String) {
        _entries.removeAll { it.id == entryId }
        saveEntries()
    }

    fun getEntry(entryId: String): JournalEntry? = _entries.find { it.id == entryId }

    fun getEntriesByCollection(collectionId: String): List<JournalEntry> {
        return _entries.filter { it.collectionId == collectionId }
            .sortedByDescending { it.displayTime }
    }

    /** 获取某天的所有手帐条目（按 displayTime 匹配日期） */
    fun getEntriesByDate(year: Int, month: Int, day: Int): List<JournalEntry> {
        return _entries.filter { entry ->
            val cal = java.util.Calendar.getInstance().apply {
                timeInMillis = entry.displayTime
            }
            cal.get(java.util.Calendar.YEAR) == year &&
                cal.get(java.util.Calendar.MONTH) + 1 == month &&
                cal.get(java.util.Calendar.DAY_OF_MONTH) == day
        }.sortedByDescending { it.displayTime }
    }

    /** 获取某月所有有条目的日期（返回 day 列表） */
    fun getDaysWithEntries(year: Int, month: Int, collectionId: String? = null): Set<Int> {
        return _entries.filter { entry ->
            val inCollection = collectionId == null || entry.collectionId == collectionId
            if (!inCollection) return@filter false
            val cal = java.util.Calendar.getInstance().apply { timeInMillis = entry.displayTime }
            cal.get(java.util.Calendar.YEAR) == year &&
                cal.get(java.util.Calendar.MONTH) + 1 == month
        }.map { entry ->
            java.util.Calendar.getInstance().apply { timeInMillis = entry.displayTime }
                .get(java.util.Calendar.DAY_OF_MONTH)
        }.toSet()
    }

    // ============================================================================
    // 统计
    // ============================================================================

    /** 最常去地点（按次数降序） */
    fun getLocationStats(
        startTime: Long? = null,
        endTime: Long? = null
    ): List<com.example.xiuperlerbeads.domain.model.LocationStat> {
        return _entries.filter { entry ->
            entry.isHandZhang &&
                !entry.location.isNullOrBlank() &&
                (startTime == null || entry.displayTime >= startTime) &&
                (endTime == null || entry.displayTime <= endTime)
        }.groupBy { it.location!! }
            .map { (loc, list) ->
                com.example.xiuperlerbeads.domain.model.LocationStat(loc, list.size)
            }
            .sortedByDescending { it.count }
    }

    /** 各 Tag 花费合计 */
    fun getTagExpenseStats(
        startTime: Long? = null,
        endTime: Long? = null
    ): List<com.example.xiuperlerbeads.domain.model.TagExpenseStat> {
        val filteredEntries = _entries.filter { entry ->
            entry.isHandZhang &&
                (startTime == null || entry.displayTime >= startTime) &&
                (endTime == null || entry.displayTime <= endTime)
        }
        return _tags.map { tag ->
            val tagEntries = filteredEntries.filter { e -> e.tags.any { it.id == tag.id } }
            com.example.xiuperlerbeads.domain.model.TagExpenseStat(
                tag = tag,
                totalExpense = tagEntries.sumOf { it.expense },
                entryCount = tagEntries.size
            )
        }.filter { it.entryCount > 0 }
            .sortedByDescending { it.totalExpense }
    }

    // ============================================================================
    // 持久化
    // ============================================================================

    private fun saveCollections() {
        val arr = JSONArray()
        _collections.forEach { arr.put(toJson(it)) }
        prefs.edit().putString(KEY_COLLECTIONS, arr.toString()).apply()
    }

    private fun saveTags() {
        val arr = JSONArray()
        _tags.forEach { arr.put(toJson(it)) }
        prefs.edit().putString(KEY_TAGS, arr.toString()).apply()
    }

    private fun saveEntries() {
        val arr = JSONArray()
        _entries.forEach { arr.put(toJson(it)) }
        prefs.edit().putString(KEY_ENTRIES, arr.toString()).apply()
    }

    // ============================================================================
    // JSON 序列化
    // ============================================================================

    private fun toJson(c: JournalCollection): JSONObject = JSONObject().apply {
        put("id", c.id)
        put("name", c.name)
        put("isDefault", c.isDefault)
        put("sortOrder", c.sortOrder)
        put("createdAt", c.createdAt)
    }

    private fun toJson(t: JournalTag): JSONObject = JSONObject().apply {
        put("id", t.id)
        put("name", t.name)
        put("colorHex", t.colorHex)
        put("createdAt", t.createdAt)
    }

    private fun toJson(a: JournalAttachment): JSONObject = JSONObject().apply {
        put("id", a.id)
        put("type", a.type.name)
        put("uri", a.uri)
        put("fileName", a.fileName)
        put("fileSizeBytes", a.fileSizeBytes)
    }

    private fun toJson(e: JournalEntry): JSONObject = JSONObject().apply {
        put("id", e.id)
        put("collectionId", e.collectionId)
        put("content", e.content)
        put("createdAt", e.createdAt)
        put("updatedAt", e.updatedAt)
        put("entryTime", e.entryTime)
        put("location", e.location)
        put("expense", e.expense)

        val attachArr = JSONArray()
        e.attachments.forEach { attachArr.put(toJson(it)) }
        put("attachments", attachArr)

        val tagArr = JSONArray()
        e.tags.forEach { tagArr.put(toJson(it)) }
        put("tags", tagArr)
    }

    // ============================================================================
    // JSON 反序列化
    // ============================================================================

    private fun parseCollection(o: JSONObject): JournalCollection = JournalCollection(
        id = o.optString("id", UUID.randomUUID().toString()),
        name = o.optString("name", ""),
        isDefault = o.optBoolean("isDefault", false),
        sortOrder = o.optInt("sortOrder", 0),
        createdAt = o.optLong("createdAt", System.currentTimeMillis())
    )

    private fun parseTag(o: JSONObject): JournalTag = JournalTag(
        id = o.optString("id", UUID.randomUUID().toString()),
        name = o.optString("name", ""),
        colorHex = o.optString("colorHex", "FF6B6B"),
        createdAt = o.optLong("createdAt", System.currentTimeMillis())
    )

    private fun parseAttachment(o: JSONObject): JournalAttachment = JournalAttachment(
        id = o.optString("id", UUID.randomUUID().toString()),
        type = try { AttachmentType.valueOf(o.optString("type", "IMAGE")) } catch (e: Exception) { AttachmentType.IMAGE },
        uri = o.optString("uri", ""),
        fileName = o.optString("fileName", ""),
        fileSizeBytes = o.optLong("fileSizeBytes", 0L)
    )

    private fun parseEntry(o: JSONObject): JournalEntry {
        val attachArr = o.optJSONArray("attachments") ?: JSONArray()
        val attachments = (0 until attachArr.length()).map { parseAttachment(attachArr.getJSONObject(it)) }

        val tagArr = o.optJSONArray("tags") ?: JSONArray()
        val tags = (0 until tagArr.length()).map { parseTag(tagArr.getJSONObject(it)) }

        return JournalEntry(
            id = o.optString("id", UUID.randomUUID().toString()),
            collectionId = o.optString("collectionId", JournalCollection.DEFAULT_ID),
            content = o.optString("content", ""),
            attachments = attachments,
            tags = tags,
            createdAt = o.optLong("createdAt", System.currentTimeMillis()),
            updatedAt = o.optLong("updatedAt", System.currentTimeMillis()),
            entryTime = if (o.has("entryTime") && !o.isNull("entryTime")) o.optLong("entryTime") else null,
            location = o.optString("location", "").takeIf { it.isNotEmpty() },
            expense = o.optDouble("expense", 0.0)
        )
    }

    companion object {
        private const val PREFS_NAME = "journal_prefs"
        private const val KEY_COLLECTIONS = "collections"
        private const val KEY_TAGS = "tags"
        private const val KEY_ENTRIES = "entries"
    }
}
