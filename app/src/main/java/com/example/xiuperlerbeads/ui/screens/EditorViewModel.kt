package com.example.xiuperlerbeads.ui.screens

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.xiuperlerbeads.data.repository.ExportRepository
import com.example.xiuperlerbeads.domain.model.ColorStatistics
import com.example.xiuperlerbeads.domain.model.DrawingTool
import com.example.xiuperlerbeads.domain.model.PerlerColor
import com.example.xiuperlerbeads.domain.model.PerlerColorPalette
import com.example.xiuperlerbeads.domain.model.PerlerProject
import com.example.xiuperlerbeads.domain.usecase.ColorStatisticsUseCase
import com.example.xiuperlerbeads.domain.usecase.DrawingUseCase
import com.example.xiuperlerbeads.domain.usecase.FloodFillUseCase
import com.example.xiuperlerbeads.domain.usecase.ImageToPixelArtUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI State for the editor screen
 */
data class EditorUiState(
    val project: PerlerProject? = null,
    val selectedColor: PerlerColor? = PerlerColorPalette.allColors.first(),
    val selectedTool: DrawingTool = DrawingTool.BRUSH,
    val brushSize: Int = 1,
    val showColorPicker: Boolean = false,
    val showStatistics: Boolean = false,
    val colorStatistics: List<ColorStatistics> = emptyList(),
    val isLoading: Boolean = false,
    val message: String? = null,
    val undoStack: List<PerlerProject> = emptyList(),
    val redoStack: List<PerlerProject> = emptyList(),
    val showOriginalImage: Boolean = false
)

/**
 * ViewModel for the pixel art editor
 */
class EditorViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    private val imageToPixelArtUseCase = ImageToPixelArtUseCase()
    private val colorStatisticsUseCase = ColorStatisticsUseCase()
    private val floodFillUseCase = FloodFillUseCase()
    private val drawingUseCase = DrawingUseCase()

    private var exportRepository: ExportRepository? = null

    fun initialize(context: Context) {
        exportRepository = ExportRepository(context)
    }

    /**
     * Create a new empty project
     */
    fun createNewProject(name: String, width: Int, height: Int) {
        val project = drawingUseCase.createEmptyProject(name, width, height)
        _uiState.value = _uiState.value.copy(
            project = project,
            undoStack = emptyList(),
            redoStack = emptyList()
        )
        updateStatistics()
    }

    /**
     * Import an image and convert to pixel art
     */
    fun importImage(bitmap: Bitmap, targetSize: Int = 32) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val project = imageToPixelArtUseCase.execute(bitmap, targetSize, targetSize)
                _uiState.value = _uiState.value.copy(
                    project = project,
                    isLoading = false,
                    undoStack = emptyList(),
                    redoStack = emptyList()
                )
                updateStatistics()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = "转换失败: ${e.message}"
                )
            }
        }
    }

    /**
     * Handle pixel click/drag
     */
    fun onPixelInteraction(x: Int, y: Int) {
        val state = _uiState.value
        val project = state.project ?: return

        viewModelScope.launch {
            val newProject = when (state.selectedTool) {
                DrawingTool.BRUSH -> {
                    drawingUseCase.drawBrushStroke(
                        project, x, y, state.brushSize, state.selectedColor
                    )
                }
                DrawingTool.ERASER -> {
                    drawingUseCase.drawBrushStroke(project, x, y, state.brushSize, null)
                }
                DrawingTool.FILL -> {
                    floodFillUseCase.execute(project, x, y, state.selectedColor!!)
                }
            }

            // Save to undo stack
            val newUndoStack = state.undoStack + project
            _uiState.value = state.copy(
                project = newProject,
                undoStack = newUndoStack.takeLast(20), // Keep last 20 states
                redoStack = emptyList()
            )
            updateStatistics()
        }
    }

    /**
     * Undo last action
     */
    fun undo() {
        val state = _uiState.value
        if (state.undoStack.isEmpty()) return

        val currentProject = state.project ?: return
        val previousProject = state.undoStack.last()
        val newUndoStack = state.undoStack.dropLast(1)
        val newRedoStack = state.redoStack + currentProject

        _uiState.value = state.copy(
            project = previousProject,
            undoStack = newUndoStack,
            redoStack = newRedoStack
        )
        updateStatistics()
    }

    /**
     * Redo last undone action
     */
    fun redo() {
        val state = _uiState.value
        if (state.redoStack.isEmpty()) return

        val currentProject = state.project ?: return
        val nextProject = state.redoStack.last()
        val newRedoStack = state.redoStack.dropLast(1)
        val newUndoStack = state.undoStack + currentProject

        _uiState.value = state.copy(
            project = nextProject,
            undoStack = newUndoStack,
            redoStack = newRedoStack
        )
        updateStatistics()
    }

    /**
     * Select a color
     */
    fun selectColor(color: PerlerColor?) {
        _uiState.value = _uiState.value.copy(selectedColor = color)
    }

    /**
     * Select a tool
     */
    fun selectTool(tool: DrawingTool) {
        _uiState.value = _uiState.value.copy(selectedTool = tool)
    }

    /**
     * Set brush size
     */
    fun setBrushSize(size: Int) {
        _uiState.value = _uiState.value.copy(brushSize = size)
    }

    /**
     * Toggle color picker visibility
     */
    fun toggleColorPicker() {
        _uiState.value = _uiState.value.copy(
            showColorPicker = !_uiState.value.showColorPicker
        )
    }

    /**
     * Toggle statistics panel
     */
    fun toggleStatistics() {
        _uiState.value = _uiState.value.copy(
            showStatistics = !_uiState.value.showStatistics
        )
    }

    /**
     * Toggle original image comparison
     */
    fun toggleOriginalImage() {
        _uiState.value = _uiState.value.copy(
            showOriginalImage = !_uiState.value.showOriginalImage
        )
    }

    /**
     * Reduce colors in the project
     */
    fun reduceColors(maxColors: Int) {
        val project = _uiState.value.project ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val reduced = imageToPixelArtUseCase.reduceColors(project, maxColors)
                val newUndoStack = _uiState.value.undoStack + project
                _uiState.value = _uiState.value.copy(
                    project = reduced,
                    isLoading = false,
                    undoStack = newUndoStack.takeLast(20),
                    redoStack = emptyList()
                )
                updateStatistics()
                _uiState.value = _uiState.value.copy(message = "颜色已精简为 $maxColors 种")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = "精简失败: ${e.message}"
                )
            }
        }
    }

    /**
     * Merge similar colors
     */
    fun mergeSimilarColors(threshold: Int = 30) {
        val project = _uiState.value.project ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val merged = imageToPixelArtUseCase.mergeSimilarColors(project, threshold)
                val newUndoStack = _uiState.value.undoStack + project
                _uiState.value = _uiState.value.copy(
                    project = merged,
                    isLoading = false,
                    undoStack = newUndoStack.takeLast(20),
                    redoStack = emptyList()
                )
                updateStatistics()
                _uiState.value = _uiState.value.copy(message = "相近颜色已合并")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = "合并失败: ${e.message}"
                )
            }
        }
    }

    /**
     * Export to PNG
     */
    fun exportToPng() {
        val project = _uiState.value.project ?: return
        val repository = exportRepository ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = repository.exportToPng(project, project.name)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                message = result.getOrElse { "导出失败: ${it.message}" }
            )
        }
    }

    /**
     * Export to PDF
     */
    fun exportToPdf() {
        val project = _uiState.value.project ?: return
        val repository = exportRepository ?: return
        val statistics = _uiState.value.colorStatistics

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = repository.exportToPdf(project, project.name, statistics)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                message = result.getOrElse { "导出失败: ${it.message}" }
            )
        }
    }

    /**
     * Clear message
     */
    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }

    /**
     * Update color statistics
     */
    private fun updateStatistics() {
        val project = _uiState.value.project ?: return
        viewModelScope.launch {
            val statistics = colorStatisticsUseCase.execute(project)
            _uiState.value = _uiState.value.copy(colorStatistics = statistics)
        }
    }
}
