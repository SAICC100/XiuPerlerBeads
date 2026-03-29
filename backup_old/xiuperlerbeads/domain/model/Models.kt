package com.example.xiuperlerbeads.domain.model

import androidx.compose.ui.graphics.Color

/**
 * Represents a Perler bead color with its name and RGB values
 */
data class PerlerColor(
    val id: Int,
    val name: String,
    val red: Int,
    val green: Int,
    val blue: Int,
    val colorCode: String // e.g., "P001" for Perler code
) {
    fun toComposeColor(): Color = Color(red, green, blue)

    fun toArgb(): Int = (0xFF shl 24) or (red shl 16) or (green shl 8) or blue

    companion object {
        fun fromArgb(argb: Int): Triple<Int, Int, Int> {
            return Triple(
                (argb shr 16) and 0xFF,
                (argb shr 8) and 0xFF,
                argb and 0xFF
            )
        }
    }
}

/**
 * Represents a pixel in the canvas
 */
data class Pixel(
    val x: Int,
    val y: Int,
    val color: PerlerColor?
)

/**
 * Represents a Perler bead project/canvas
 */
data class PerlerProject(
    val id: Long = System.currentTimeMillis(),
    val name: String,
    val width: Int,
    val height: Int,
    val pixels: Array<Array<PerlerColor?>>,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as PerlerProject
        if (id != other.id) return false
        return true
    }

    override fun hashCode(): Int = id.hashCode()
}

/**
 * Statistics for color usage in a project
 */
data class ColorStatistics(
    val color: PerlerColor,
    val count: Int,
    val percentage: Float
)

/**
 * Drawing tool types
 */
enum class DrawingTool {
    BRUSH,
    ERASER,
    FILL
}

/**
 * Canvas size presets
 */
enum class CanvasSize(val dimension: Int) {
    SIZE_16(16),
    SIZE_32(32),
    SIZE_48(48),
    SIZE_64(64),
    SIZE_128(128),
    SIZE_256(256)
}
