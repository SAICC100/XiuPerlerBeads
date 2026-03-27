package com.example.xiuperlerbeads.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import com.example.xiuperlerbeads.domain.model.PerlerColor
import com.example.xiuperlerbeads.domain.model.PerlerProject
import com.example.xiuperlerbeads.ui.theme.GridLine
import com.example.xiuperlerbeads.ui.theme.GridLineDark

/**
 * Pixel art canvas component for editing Perler bead patterns
 */
@Composable
fun PixelCanvas(
    project: PerlerProject,
    onPixelClick: (Int, Int) -> Unit,
    onPixelDrag: (Int, Int) -> Unit,
    modifier: Modifier = Modifier,
    showGrid: Boolean = true,
    scale: Float = 1f,
    offset: Offset = Offset.Zero
) {
    var canvasSize by remember { mutableStateOf(Size.Zero) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    // Handle zoom and pan if needed
                }
            }
            .pointerInput(project) {
                detectTapGestures { offset ->
                    if (canvasSize.width > 0 && canvasSize.height > 0) {
                        val pixelWidth = canvasSize.width / project.width
                        val pixelHeight = canvasSize.height / project.height
                        val x = (offset.x / pixelWidth).toInt().coerceIn(0, project.width - 1)
                        val y = (offset.y / pixelHeight).toInt().coerceIn(0, project.height - 1)
                        onPixelClick(x, y)
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            canvasSize = size

            val pixelWidth = size.width / project.width
            val pixelHeight = size.height / project.height

            // Draw pixels
            for (y in 0 until project.height) {
                for (x in 0 until project.width) {
                    val color = project.pixels[y][x]
                    drawPixel(
                        x = x * pixelWidth,
                        y = y * pixelHeight,
                        width = pixelWidth,
                        height = pixelHeight,
                        color = color
                    )
                }
            }

            // Draw grid
            if (showGrid && pixelWidth > 4 && pixelHeight > 4) {
                drawGrid(
                    width = project.width,
                    height = project.height,
                    pixelWidth = pixelWidth,
                    pixelHeight = pixelHeight
                )
            }
        }
    }
}

private fun DrawScope.drawPixel(
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    color: PerlerColor?
) {
    // Draw background for empty pixels
    if (color == null) {
        drawRect(
            color = Color.White,
            topLeft = Offset(x, y),
            size = Size(width, height)
        )
    } else {
        drawRect(
            color = color.toComposeColor(),
            topLeft = Offset(x, y),
            size = Size(width, height)
        )
    }

    // Draw pixel border (for Perler bead effect)
    drawRect(
        color = Color(0x40000000),
        topLeft = Offset(x, y),
        size = Size(width, height),
        style = Stroke(width = 0.5f)
    )
}

private fun DrawScope.drawGrid(
    width: Int,
    height: Int,
    pixelWidth: Float,
    pixelHeight: Float
) {
    val gridColor = GridLine

    // Vertical lines
    for (x in 0..width) {
        drawLine(
            color = gridColor,
            start = Offset(x * pixelWidth, 0f),
            end = Offset(x * pixelWidth, height * pixelHeight),
            strokeWidth = 0.5f
        )
    }

    // Horizontal lines
    for (y in 0..height) {
        drawLine(
            color = gridColor,
            start = Offset(0f, y * pixelHeight),
            end = Offset(width * pixelWidth, y * pixelHeight),
            strokeWidth = 0.5f
        )
    }

    // Draw thicker borders every 8 pixels for easier counting
    val borderColor = GridLineDark
    for (x in 0..width step 8) {
        drawLine(
            color = borderColor,
            start = Offset(x * pixelWidth, 0f),
            end = Offset(x * pixelWidth, height * pixelHeight),
            strokeWidth = 1.5f
        )
    }
    for (y in 0..height step 8) {
        drawLine(
            color = borderColor,
            start = Offset(0f, y * pixelHeight),
            end = Offset(width * pixelWidth, y * pixelHeight),
            strokeWidth = 1.5f
        )
    }
}

/**
 * Preview canvas for displaying pixel art at smaller scales
 */
@Composable
fun PixelCanvasPreview(
    project: PerlerProject,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val pixelWidth = size.width / project.width
        val pixelHeight = size.height / project.height

        for (y in 0 until project.height) {
            for (x in 0 until project.width) {
                val color = project.pixels[y][x]
                if (color != null) {
                    drawRect(
                        color = color.toComposeColor(),
                        topLeft = Offset(x * pixelWidth, y * pixelHeight),
                        size = Size(pixelWidth, pixelHeight)
                    )
                }
            }
        }
    }
}
