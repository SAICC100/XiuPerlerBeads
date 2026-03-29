package com.example.xiuperlerbeads.domain.usecase

import android.graphics.Bitmap
import com.example.xiuperlerbeads.domain.model.CanvasSize
import com.example.xiuperlerbeads.domain.model.PerlerColor
import com.example.xiuperlerbeads.domain.model.PerlerColorPalette
import com.example.xiuperlerbeads.domain.model.PerlerProject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Use case for converting images to pixel art
 */
class ImageToPixelArtUseCase {

    /**
     * Convert a bitmap image to a Perler bead project
     */
    suspend fun execute(
        bitmap: Bitmap,
        targetWidth: Int = CanvasSize.SIZE_32.dimension,
        targetHeight: Int = CanvasSize.SIZE_32.dimension
    ): PerlerProject = withContext(Dispatchers.Default) {
        // Scale bitmap to target size
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)

        // Create pixel array
        val pixels = Array(targetHeight) { row ->
            Array<PerlerColor?>(targetWidth) { col ->
                val pixelColor = scaledBitmap.getPixel(col, row)
                val r = (pixelColor shr 16) and 0xFF
                val g = (pixelColor shr 8) and 0xFF
                val b = pixelColor and 0xFF
                PerlerColorPalette.findClosestColor(r, g, b)
            }
        }

        // Recycle scaled bitmap if different from original
        if (scaledBitmap != bitmap) {
            scaledBitmap.recycle()
        }

        PerlerProject(
            name = "拼豆图纸",
            width = targetWidth,
            height = targetHeight,
            pixels = pixels
        )
    }

    /**
     * Reduce the number of colors in a project
     */
    suspend fun reduceColors(project: PerlerProject, maxColors: Int): PerlerProject =
        withContext(Dispatchers.Default) {
            val currentColors = mutableSetOf<PerlerColor>()
            val colorCount = mutableMapOf<PerlerColor, Int>()

            // Count all colors
            for (row in project.pixels) {
                for (pixel in row) {
                    pixel?.let {
                        currentColors.add(it)
                        colorCount[it] = (colorCount[it] ?: 0) + 1
                    }
                }
            }

            // If already under limit, return as is
            if (currentColors.size <= maxColors) {
                return@withContext project
            }

            // Keep the most used colors and replace others
            val sortedColors = colorCount.entries.sortedByDescending { it.value }
            val colorsToKeep = sortedColors.take(maxColors).map { it.key }.toSet()

            val newPixels = Array(project.height) { row ->
                Array<PerlerColor?>(project.width) { col ->
                    val pixel = project.pixels[row][col]
                    if (pixel != null && pixel in colorsToKeep) {
                        pixel
                    } else {
                        // Find closest color from kept colors
                        pixel?.let { findClosestFromSet(it, colorsToKeep) }
                    }
                }
            }

            project.copy(
                pixels = newPixels,
                updatedAt = System.currentTimeMillis()
            )
        }

    private fun findClosestFromSet(
        color: PerlerColor,
        colorSet: Set<PerlerColor>
    ): PerlerColor {
        return colorSet.minByOrNull {
            kotlin.math.abs(it.red - color.red) +
            kotlin.math.abs(it.green - color.green) +
            kotlin.math.abs(it.blue - color.blue)
        } ?: colorSet.first()
    }

    /**
     * Merge similar colors
     */
    suspend fun mergeSimilarColors(project: PerlerProject, threshold: Int = 30): PerlerProject =
        withContext(Dispatchers.Default) {
            val colorMapping = mutableMapOf<PerlerColor, PerlerColor>()

            // Group similar colors
            val allColors = project.pixels.flatten().filterNotNull().distinct()
            val processed = mutableSetOf<PerlerColor>()

            for (color in allColors) {
                if (color in processed) continue

                val similarColors = allColors.filter { other ->
                    other != color && !processed.contains(other) &&
                    isSimilar(color, other, threshold)
                }

                // Map all similar colors to the first one
                val representative = color
                for (similar in similarColors) {
                    colorMapping[similar] = representative
                    processed.add(similar)
                }
                processed.add(representative)
            }

            // Apply mapping to pixels
            val newPixels = Array(project.height) { row ->
                Array<PerlerColor?>(project.width) { col ->
                    val pixel = project.pixels[row][col]
                    pixel?.let { colorMapping[it] ?: it }
                }
            }

            project.copy(
                pixels = newPixels,
                updatedAt = System.currentTimeMillis()
            )
        }

    private fun isSimilar(c1: PerlerColor, c2: PerlerColor, threshold: Int): Boolean {
        return kotlin.math.abs(c1.red - c2.red) <= threshold &&
                kotlin.math.abs(c1.green - c2.green) <= threshold &&
                kotlin.math.abs(c1.blue - c2.blue) <= threshold
    }
}
