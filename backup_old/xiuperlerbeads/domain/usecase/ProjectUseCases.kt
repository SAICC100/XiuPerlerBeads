package com.example.xiuperlerbeads.domain.usecase

import com.example.xiuperlerbeads.domain.model.ColorStatistics
import com.example.xiuperlerbeads.domain.model.PerlerColor
import com.example.xiuperlerbeads.domain.model.PerlerProject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Use case for calculating color statistics
 */
class ColorStatisticsUseCase {

    /**
     * Calculate statistics for all colors used in the project
     */
    suspend fun execute(project: PerlerProject): List<ColorStatistics> =
        withContext(Dispatchers.Default) {
            val colorCount = mutableMapOf<PerlerColor, Int>()
            var totalCount = 0

            for (row in project.pixels) {
                for (pixel in row) {
                    pixel?.let {
                        colorCount[it] = (colorCount[it] ?: 0) + 1
                        totalCount++
                    }
                }
            }

            colorCount.map { (color, count) ->
                ColorStatistics(
                    color = color,
                    count = count,
                    percentage = if (totalCount > 0) count.toFloat() / totalCount * 100 else 0f
                )
            }.sortedByDescending { it.count }
        }

    /**
     * Get total bead count for the project
     */
    suspend fun getTotalBeadCount(project: PerlerProject): Int =
        withContext(Dispatchers.Default) {
            project.pixels.flatten().count { it != null }
        }
}

/**
 * Use case for flood fill algorithm
 */
class FloodFillUseCase {

    /**
     * Fill an area with the target color using flood fill algorithm
     */
    suspend fun execute(
        project: PerlerProject,
        startX: Int,
        startY: Int,
        newColor: PerlerColor
    ): PerlerProject = withContext(Dispatchers.Default) {
        val width = project.width
        val height = project.height

        if (startX < 0 || startX >= width || startY < 0 || startY >= height) {
            return@withContext project
        }

        val targetColor = project.pixels[startY][startX]

        // If target color is same as new color, nothing to do
        if (targetColor == newColor) {
            return@withContext project
        }

        // Create a mutable copy of pixels
        val newPixels = Array(height) { y ->
            Array<PerlerColor?>(width) { x ->
                project.pixels[y][x]
            }
        }

        // BFS flood fill
        val queue = ArrayDeque<Pair<Int, Int>>()
        val visited = Array(height) { BooleanArray(width) }

        queue.add(Pair(startX, startY))
        visited[startY][startX] = true

        while (queue.isNotEmpty()) {
            val (x, y) = queue.removeFirst()

            if (newPixels[y][x] == targetColor) {
                newPixels[y][x] = newColor

                // Check 4 neighbors
                listOf(
                    Pair(x - 1, y),
                    Pair(x + 1, y),
                    Pair(x, y - 1),
                    Pair(x, y + 1)
                ).forEach { (nx, ny) ->
                    if (nx in 0 until width && ny in 0 until height && !visited[ny][nx]) {
                        visited[ny][nx] = true
                        queue.add(Pair(nx, ny))
                    }
                }
            }
        }

        project.copy(
            pixels = newPixels,
            updatedAt = System.currentTimeMillis()
        )
    }
}

/**
 * Use case for drawing operations
 */
class DrawingUseCase {

    /**
     * Draw a single pixel
     */
    fun drawPixel(project: PerlerProject, x: Int, y: Int, color: PerlerColor?): PerlerProject {
        if (x < 0 || x >= project.width || y < 0 || y >= project.height) {
            return project
        }

        val newPixels = Array(project.height) { row ->
            Array<PerlerColor?>(project.width) { col ->
                project.pixels[row][col]
            }
        }

        newPixels[y][x] = color

        return project.copy(
            pixels = newPixels,
            updatedAt = System.currentTimeMillis()
        )
    }

    /**
     * Draw a brush stroke (multiple pixels)
     */
    fun drawBrushStroke(
        project: PerlerProject,
        x: Int,
        y: Int,
        brushSize: Int,
        color: PerlerColor?
    ): PerlerProject {
        var updatedProject = project

        val halfSize = brushSize / 2
        for (dy in -halfSize until (brushSize - halfSize)) {
            for (dx in -halfSize until (brushSize - halfSize)) {
                updatedProject = drawPixel(updatedProject, x + dx, y + dy, color)
            }
        }

        return updatedProject
    }

    /**
     * Create an empty project
     */
    fun createEmptyProject(
        name: String,
        width: Int,
        height: Int
    ): PerlerProject {
        return PerlerProject(
            name = name,
            width = width,
            height = height,
            pixels = Array(height) { arrayOfNulls(width) }
        )
    }
}
