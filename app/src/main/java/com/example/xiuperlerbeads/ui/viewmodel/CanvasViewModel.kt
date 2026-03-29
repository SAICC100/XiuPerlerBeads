package com.example.xiuperlerbeads.ui.viewmodel

import android.app.Application
import android.content.ContentValues
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.xiuperlerbeads.data.repository.InventoryRepository
import com.example.xiuperlerbeads.domain.model.BeadColor
import com.example.xiuperlerbeads.domain.model.BeadColorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

/**
 * 画布工具类型
 */
enum class CanvasTool {
    PENCIL,    // 画笔
    ERASER,    // 橡皮擦
    FILL,      // 填充
    PICKER     // 取色器
}

/**
 * 画布状态
 */
data class CanvasState(
    val gridSize: Int = 32,
    val projectName: String = "我的拼豆",
    val projectId: String? = null,
    // 画布数据: 每个格子存储 BeadColor 的 index，-1 表示空白
    val canvasData: List<List<Int>> = List(gridSize) { List(gridSize) { -1 } },
    // 当前选中的颜色 index
    val selectedColorIndex: Int = 0,
    // 当前工具
    val selectedTool: CanvasTool = CanvasTool.PENCIL,
    // 是否显示网格
    val showGrid: Boolean = true,
    // 是否已保存
    val isSaved: Boolean = true,
    // 缩放级别
    val zoomLevel: Float = 1f,
    // 撤销栈
    val undoStack: List<List<List<Int>>> = emptyList(),
    // 重做栈
    val redoStack: List<List<List<Int>>> = emptyList(),
    // 统计数据
    val colorStats: Map<Int, Int> = emptyMap()
)

/**
 * 画布 ViewModel
 * 管理画布状态、工具选择、撤销/重做等
 */
class CanvasViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = InventoryRepository(application)
    private val allColors = BeadColorManager.getAllColors()
    
    private val _state = MutableStateFlow(CanvasState())
    val state: StateFlow<CanvasState> = _state.asStateFlow()
    
    /**
     * 初始化画布
     */
    fun initializeCanvas(gridSize: Int, projectName: String = "我的拼豆", projectId: String? = null) {
        _state.update {
            CanvasState(
                gridSize = gridSize,
                projectName = projectName,
                projectId = projectId,
                canvasData = List(gridSize) { List(gridSize) { -1 } }
            )
        }
        updateColorStats()
    }
    
    /**
     * 加载项目
     */
    fun loadProject(projectId: String) {
        viewModelScope.launch {
            val projectJson = repository.getProjectJson(projectId)
            if (projectJson != null) {
                try {
                    val json = JSONObject(projectJson)
                    val gridSize = json.optInt("gridSize", 32)
                    val canvasData = parseCanvasData(json.optJSONArray("canvasData"), gridSize)
                    val projectName = json.optString("name", "未命名项目")
                    
                    _state.update {
                        it.copy(
                            gridSize = gridSize,
                            projectName = projectName,
                            projectId = projectId,
                            canvasData = canvasData,
                            isSaved = true
                        )
                    }
                    updateColorStats()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
    
    /**
     * 选择颜色
     */
    fun selectColor(colorIndex: Int) {
        _state.update { it.copy(selectedColorIndex = colorIndex) }
    }
    
    /**
     * 选择工具
     */
    fun selectTool(tool: CanvasTool) {
        _state.update { it.copy(selectedTool = tool) }
    }
    
    /**
     * 切换网格显示
     */
    fun toggleGrid() {
        _state.update { it.copy(showGrid = !it.showGrid) }
    }
    
    /**
     * 绘制格子
     */
    fun drawCell(x: Int, y: Int) {
        if (x < 0 || x >= _state.value.gridSize || y < 0 || y >= _state.value.gridSize) return
        
        val currentState = _state.value
        val currentColor = currentState.canvasData[y][x]
        val newColor = when (currentState.selectedTool) {
            CanvasTool.PENCIL -> currentState.selectedColorIndex
            CanvasTool.ERASER -> -1
            CanvasTool.FILL -> { 
                floodFill(x, y, currentColor, currentState.selectedColorIndex)
                return
            }
            CanvasTool.PICKER -> {
                // 取色器：将当前颜色设为选中颜色
                if (currentColor >= 0) {
                    _state.update { it.copy(selectedColorIndex = currentColor) }
                }
                return
            }
        }
        
        // 如果颜色没变，不保存撤销状态
        if (currentColor == newColor) return
        
        // 保存撤销状态
        val newUndoStack = currentState.undoStack + listOf(currentState.canvasData)
        
        // 应用绘制
        val newCanvasData = currentState.canvasData.toMutableList().map { it.toMutableList() }
        newCanvasData[y][x] = newColor
        
        _state.update {
            it.copy(
                canvasData = newCanvasData,
                undoStack = newUndoStack,
                redoStack = emptyList(), // 清空重做栈
                isSaved = false
            )
        }
        updateColorStats()
    }
    
    /**
     * 填充算法 (Flood Fill)
     */
    private fun floodFill(x: Int, y: Int, targetColor: Int, replacementColor: Int) {
        if (targetColor == replacementColor) return
        
        val currentState = _state.value
        val mutableCanvas = currentState.canvasData.map { it.toMutableList() }.toMutableList()
        
        val stack = mutableListOf(Pair(x, y))
        val visited = mutableSetOf<Pair<Int, Int>>()
        
        while (stack.isNotEmpty()) {
            val (cx, cy) = stack.removeLast()
            
            if (cx < 0 || cx >= currentState.gridSize || cy < 0 || cy >= currentState.gridSize) continue
            if (Pair(cx, cy) in visited) continue
            if (mutableCanvas[cy][cx] != targetColor) continue
            
            visited.add(Pair(cx, cy))
            mutableCanvas[cy][cx] = replacementColor
            
            stack.add(Pair(cx + 1, cy))
            stack.add(Pair(cx - 1, cy))
            stack.add(Pair(cx, cy + 1))
            stack.add(Pair(cx, cy - 1))
        }
        
        // 保存撤销状态
        val newUndoStack = currentState.undoStack + listOf(currentState.canvasData)
        
        _state.update {
            it.copy(
                canvasData = mutableCanvas,
                undoStack = newUndoStack,
                redoStack = emptyList(),
                isSaved = false
            )
        }
        updateColorStats()
    }
    
    /**
     * 撤销
     */
    fun undo() {
        val currentState = _state.value
        if (currentState.undoStack.isEmpty()) return
        
        val previousCanvas = currentState.undoStack.last()
        val newUndoStack = currentState.undoStack.dropLast(1)
        val newRedoStack = currentState.redoStack + listOf(currentState.canvasData)
        
        _state.update {
            it.copy(
                canvasData = previousCanvas,
                undoStack = newUndoStack,
                redoStack = newRedoStack,
                isSaved = false
            )
        }
        updateColorStats()
    }
    
    /**
     * 重做
     */
    fun redo() {
        val currentState = _state.value
        if (currentState.redoStack.isEmpty()) return
        
        val nextCanvas = currentState.redoStack.last()
        val newRedoStack = currentState.redoStack.dropLast(1)
        val newUndoStack = currentState.undoStack + listOf(currentState.canvasData)
        
        _state.update {
            it.copy(
                canvasData = nextCanvas,
                undoStack = newUndoStack,
                redoStack = newRedoStack,
                isSaved = false
            )
        }
        updateColorStats()
    }
    
    /**
     * 清空画布
     */
    fun clearCanvas() {
        val currentState = _state.value
        val newUndoStack = currentState.undoStack + listOf(currentState.canvasData)
        val size = currentState.gridSize
        
        _state.update {
            it.copy(
                canvasData = List(size) { List(size) { -1 } },
                undoStack = newUndoStack,
                redoStack = emptyList(),
                isSaved = false
            )
        }
        updateColorStats()
    }
    
    /**
     * 保存项目
     */
    fun saveProject(onComplete: (String) -> Unit) {
        viewModelScope.launch {
            val currentState = _state.value
            val projectId = currentState.projectId ?: java.util.UUID.randomUUID().toString()
            
            val json = JSONObject().apply {
                put("id", projectId)
                put("name", currentState.projectName)
                put("gridSize", currentState.gridSize)
                put("canvasData", canvasDataToJson(currentState.canvasData))
                put("updatedAt", System.currentTimeMillis())
            }
            
            repository.saveProjectJson(projectId, json.toString())
            
            _state.update { it.copy(projectId = projectId, isSaved = true) }
            onComplete(projectId)
        }
    }
    
    /**
     * 导出为 Bitmap
     */
    fun exportToBitmap(): Bitmap {
        val currentState = _state.value
        val bitmap = Bitmap.createBitmap(currentState.gridSize, currentState.gridSize, Bitmap.Config.ARGB_8888)
        
        for (y in 0 until currentState.gridSize) {
            for (x in 0 until currentState.gridSize) {
                val colorIndex = currentState.canvasData[y][x]
                val color = if (colorIndex >= 0 && colorIndex < allColors.size) {
                    val beadColor = allColors[colorIndex]
                    android.graphics.Color.argb(255, beadColor.red, beadColor.green, beadColor.blue)
                } else {
                    android.graphics.Color.WHITE
                }
                bitmap.setPixel(x, y, color)
            }
        }
        
        return bitmap
    }
    
    /**
     * 获取颜色统计
     */
    private fun updateColorStats() {
        val currentState = _state.value
        val stats = mutableMapOf<Int, Int>()
        
        for (row in currentState.canvasData) {
            for (colorIndex in row) {
                if (colorIndex >= 0) {
                    stats[colorIndex] = (stats[colorIndex] ?: 0) + 1
                }
            }
        }
        
        _state.update { it.copy(colorStats = stats) }
    }
    
    /**
     * 解析画布数据
     */
    private fun parseCanvasData(jsonArray: JSONArray?, gridSize: Int): List<List<Int>> {
        if (jsonArray == null) return List(gridSize) { List(gridSize) { -1 } }
        
        return try {
            (0 until gridSize).map { y ->
                (0 until gridSize).map { x ->
                    if (y < jsonArray.length() && x < jsonArray.getJSONArray(y).length()) {
                        jsonArray.getJSONArray(y).getInt(x)
                    } else -1
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            List(gridSize) { List(gridSize) { -1 } }
        }
    }
    
    /**
     * 画布数据转 JSON
     */
    private fun canvasDataToJson(canvasData: List<List<Int>>): JSONArray {
        return JSONArray().apply {
            for (row in canvasData) {
                put(JSONArray().apply {
                    for (cell in row) {
                        put(cell)
                    }
                })
            }
        }
    }
    
    /**
     * 获取可用颜色列表
     */
    fun getAvailableColors(): List<BeadColor> = allColors
    
    /**
     * 获取选中的颜色
     */
    fun getSelectedColor(): BeadColor? {
        val index = _state.value.selectedColorIndex
        return if (index >= 0 && index < allColors.size) allColors[index] else null
    }
    
    /**
     * 检查是否可以撤销
     */
    fun canUndo(): Boolean = _state.value.undoStack.isNotEmpty()
    
    /**
     * 检查是否可以重做
     */
    fun canRedo(): Boolean = _state.value.redoStack.isNotEmpty()
}
