package com.example.xiuperlerbeads.data.repository

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.xiuperlerbeads.domain.model.ColorStatistics
import com.example.xiuperlerbeads.domain.model.PerlerProject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

/**
 * Repository for exporting projects to various formats
 */
class ExportRepository(private val context: Context) {

    /**
     * Export project as PNG image
     */
    suspend fun exportToPng(
        project: PerlerProject,
        fileName: String,
        pixelSize: Int = 10
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val bitmap = createBitmapFromProject(project, pixelSize)
            
            val outputStream = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, "$fileName.png")
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/秀拼豆")
                }
                val uri = context.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                )
                uri?.let { context.contentResolver.openOutputStream(it) }
            } else {
                val directory = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    "秀拼豆"
                )
                if (!directory.exists()) directory.mkdirs()
                val file = File(directory, "$fileName.png")
                FileOutputStream(file)
            }

            outputStream?.use { stream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            }
            bitmap.recycle()

            Result.success("PNG已保存到: /Pictures/秀拼豆/$fileName.png")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Export project as PDF with color statistics
     */
    suspend fun exportToPdf(
        project: PerlerProject,
        fileName: String,
        statistics: List<ColorStatistics>,
        pixelSize: Int = 10
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val document = PdfDocument()

            // Calculate page size based on content
            val pageWidth = 595 // A4 width in points
            val pageHeight = 842 // A4 height in points
            val margin = 40

            // Create bitmap for the pattern
            val patternWidth = project.width * pixelSize
            val patternHeight = project.height * pixelSize

            // Pattern fits on first page
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            // Draw title
            val titlePaint = Paint().apply {
                color = Color.BLACK
                textSize = 24f
                isFakeBoldText = true
            }
            canvas.drawText(project.name, margin.toFloat(), margin + 24f, titlePaint)

            // Draw pattern info
            val infoPaint = Paint().apply {
                color = Color.GRAY
                textSize = 12f
            }
            canvas.drawText(
                "尺寸: ${project.width} x ${project.height} | 总豆数: ${statistics.sumOf { it.count }}",
                margin.toFloat(),
                margin + 50f,
                infoPaint
            )

            // Center the pattern
            val startX = (pageWidth - patternWidth) / 2f
            val startY = margin + 70f

            // Draw the pattern bitmap
            val patternBitmap = createBitmapFromProject(project, pixelSize)
            canvas.drawBitmap(patternBitmap, startX, startY, null)
            patternBitmap.recycle()

            // Draw color legend below pattern
            var legendY = startY + patternHeight + 30f
            val legendTitlePaint = Paint().apply {
                color = Color.BLACK
                textSize = 14f
                isFakeBoldText = true
            }
            canvas.drawText("色号清单:", margin.toFloat(), legendY, legendTitlePaint)
            legendY += 20f

            val legendPaint = Paint().apply {
                textSize = 11f
            }

            for (stat in statistics) {
                if (legendY > pageHeight - margin) break

                // Draw color swatch
                val colorPaint = Paint().apply {
                    color = stat.color.toArgb()
                }
                canvas.drawRect(
                    margin.toFloat(),
                    legendY - 10f,
                    margin + 15f,
                    legendY + 2f,
                    colorPaint
                )

                // Draw text
                legendPaint.color = Color.BLACK
                canvas.drawText(
                    "${stat.color.name} (${stat.color.colorCode})- ${stat.count}颗",
                    margin + 20f,
                    legendY,
                    legendPaint
                )

                legendY += 18f
            }

            document.finishPage(page)
            document.close()

            // Save PDF
            val outputStream = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, "$fileName.pdf")
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/秀拼豆")
                }
                val uri = context.contentResolver.insert(
                    MediaStore.Files.getContentUri("external"),
                    contentValues
                )
                uri?.let { context.contentResolver.openOutputStream(it) }
            } else {
                val directory = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                    "秀拼豆"
                )
                if (!directory.exists()) directory.mkdirs()
                val file = File(directory, "$fileName.pdf")
                FileOutputStream(file)
            }

            outputStream?.use { stream ->
                document.writeTo(stream)
            }

            Result.success("PDF已保存到: /Documents/秀拼豆/$fileName.pdf")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Create a bitmap from project data
     */
    private fun createBitmapFromProject(project: PerlerProject, pixelSize: Int): Bitmap {
        val width = project.width * pixelSize
        val height = project.height * pixelSize

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        for (y in 0 until project.height) {
            for (x in 0 until project.width) {
                val color = project.pixels[y][x]
                val pixelColor = if (color != null) {
                    Color.argb(255, color.red, color.green, color.blue)
                } else {
                    Color.WHITE
                }

                // Fill the pixel area
                for (py in 0 until pixelSize) {
                    for (px in 0 until pixelSize) {
                        bitmap.setPixel(
                            x * pixelSize + px,
                            y * pixelSize + py,
                            pixelColor
                        )
                    }
                }
            }
        }

        return bitmap
    }
}
