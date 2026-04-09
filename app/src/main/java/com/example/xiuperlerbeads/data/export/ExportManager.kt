package com.example.xiuperlerbeads.data.export

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.example.xiuperlerbeads.domain.model.BeadColor
import com.example.xiuperlerbeads.domain.model.BeadUsage
import com.example.xiuperlerbeads.domain.model.ProjectRecord
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * 导出管理器
 * 负责导出 PNG、PDF、材料清单等
 */
class ExportManager(private val context: Context) {
    
    private val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
    
    /**
     * 导出项目为 PNG 图片
     */
    fun exportToPng(
        canvasData: List<List<Int>>,
        colors: List<BeadColor>,
        fileName: String
    ): Uri? {
        return try {
            val size = canvasData.size
            val bitmap = Bitmap.createBitmap(size * 10, size * 10, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val paint = Paint()
            
            // 绘制每个像素
            for (y in canvasData.indices) {
                for (x in canvasData[y].indices) {
                    val colorIndex = canvasData[y][x]
                    if (colorIndex >= 0 && colorIndex < colors.size) {
                        paint.color = colors[colorIndex].toArgb()
                    } else {
                        paint.color = Color.WHITE
                    }
                    canvas.drawRect(
                        (x * 10).toFloat(),
                        (y * 10).toFloat(),
                        ((x + 1) * 10).toFloat(),
                        ((y + 1) * 10).toFloat(),
                        paint
                    )
                }
            }
            
            // 添加网格线
            paint.color = Color.LTGRAY
            paint.strokeWidth = 1f
            paint.style = Paint.Style.STROKE
            for (i in 0..size) {
                canvas.drawLine((i * 10).toFloat(), 0f, (i * 10).toFloat(), (size * 10).toFloat(), paint)
                canvas.drawLine(0f, (i * 10).toFloat(), (size * 10).toFloat(), (i * 10).toFloat(), paint)
            }
            
            // 保存
            val file = File(context.cacheDir, "$fileName.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            bitmap.recycle()
            
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * 导出项目为 PDF 图纸
     */
    fun exportToPdf(
        project: ProjectRecord,
        canvasData: List<List<Int>>,
        colors: List<BeadColor>,
        beadUsage: List<BeadUsage>,
        getColorInfo: (String) -> BeadColor?
    ): Uri? {
        return try {
            val document = PdfDocument()
            val pageWidth = 595 // A4 width in points
            val pageHeight = 842 // A4 height in points
            
            // 第一页：拼豆图纸
            val pageInfo1 = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page1 = document.startPage(pageInfo1)
            val canvas1 = page1.canvas
            
            val titlePaint = Paint().apply {
                color = Color.BLACK
                textSize = 24f
                isFakeBoldText = true
            }
            val subtitlePaint = Paint().apply {
                color = Color.GRAY
                textSize = 14f
            }
            val gridPaint = Paint().apply {
                style = Paint.Style.STROKE
                strokeWidth = 1f
                color = Color.LTGRAY
            }
            val pixelPaint = Paint().apply {
                style = Paint.Style.FILL
            }
            
            // 标题
            canvas1.drawText(project.name, 50f, 50f, titlePaint)
            canvas1.drawText("${project.beadUsage.size}种颜色 | ${canvasData.size}×${canvasData.size}", 50f, 75f, subtitlePaint)
            
            // 绘制画布
            val gridSize = canvasData.size
            val maxGridSize = 400 // 最大显示尺寸
            val cellSize = maxGridSize / gridSize
            val gridOffsetX = 50f
            val gridOffsetY = 100f
            
            for (y in canvasData.indices) {
                for (x in canvasData[y].indices) {
                    val colorIndex = canvasData[y][x]
                    if (colorIndex >= 0 && colorIndex < colors.size) {
                        pixelPaint.color = colors[colorIndex].toArgb()
                    } else {
                        pixelPaint.color = Color.WHITE
                    }
                    canvas1.drawRect(
                        gridOffsetX + x * cellSize,
                        gridOffsetY + y * cellSize,
                        gridOffsetX + (x + 1) * cellSize,
                        gridOffsetY + (y + 1) * cellSize,
                        pixelPaint
                    )
                }
            }
            
            // 绘制网格
            for (i in 0..gridSize) {
                canvas1.drawLine(
                    gridOffsetX + i * cellSize,
                    gridOffsetY,
                    gridOffsetX + i * cellSize,
                    gridOffsetY + gridSize * cellSize,
                    gridPaint
                )
                canvas1.drawLine(
                    gridOffsetX,
                    gridOffsetY + i * cellSize,
                    gridOffsetX + gridSize * cellSize,
                    gridOffsetY + i * cellSize,
                    gridPaint
                )
            }
            
            document.finishPage(page1)
            
            // 第二页：材料清单
            val pageInfo2 = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 2).create()
            val page2 = document.startPage(pageInfo2)
            val canvas2 = page2.canvas
            
            // 标题
            canvas2.drawText("材料清单", 50f, 50f, titlePaint)
            canvas2.drawText("共${beadUsage.size}种颜色", 50f, 75f, subtitlePaint)
            
            // 表格头部
            val headerPaint = Paint().apply {
                color = Color.DKGRAY
                textSize = 12f
                isFakeBoldText = true
            }
            val cellPaint = Paint().apply {
                color = Color.BLACK
                textSize = 11f
            }
            val colorPaint = Paint().apply {
                style = Paint.Style.FILL
            }
            
            var yPos = 110f
            var currentPage = page2
            var currentCanvas = canvas2
            var pageIndex = 2

            fun drawTableHeader(canvas: android.graphics.Canvas) {
                canvas.drawText("颜色预览", 50f, 110f, headerPaint)
                canvas.drawText("色号", 120f, 110f, headerPaint)
                canvas.drawText("颜色名称", 200f, 110f, headerPaint)
                canvas.drawText("数量", 350f, 110f, headerPaint)
                canvas.drawText("状态", 430f, 110f, headerPaint)
                canvas.drawLine(50f, 120f, pageWidth - 50f, 120f, gridPaint)
            }

            drawTableHeader(currentCanvas)
            yPos = 140f

            // 材料列表，内容超出当前页时自动换页
            beadUsage.sortedByDescending { it.quantity }.forEach { usage ->
                if (yPos > pageHeight - 80) {
                    // 当前页底部备注
                    currentCanvas.drawText("续下页…", pageWidth - 100f, pageHeight - 40f, subtitlePaint)
                    document.finishPage(currentPage)

                    // 新建一页
                    pageIndex++
                    val newPageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageIndex).create()
                    currentPage = document.startPage(newPageInfo)
                    currentCanvas = currentPage.canvas
                    currentCanvas.drawText("材料清单（续）", 50f, 50f, titlePaint)
                    drawTableHeader(currentCanvas)
                    yPos = 140f
                }

                val colorInfo = getColorInfo(usage.colorCode)

                colorPaint.color = colorInfo?.toArgb() ?: Color.GRAY
                currentCanvas.drawRect(50f, yPos - 10f, 70f, yPos + 10f, colorPaint)

                currentCanvas.drawText(colorInfo?.mardCode ?: usage.colorCode, 120f, yPos, cellPaint)
                currentCanvas.drawText(colorInfo?.colorName ?: "-", 200f, yPos, cellPaint)
                currentCanvas.drawText("${usage.quantity}颗", 350f, yPos, cellPaint)

                val status = when {
                    usage.quantity > 500 -> "充足"
                    usage.quantity > 100 -> "正常"
                    else -> "少量"
                }
                currentCanvas.drawText(status, 430f, yPos, cellPaint)

                yPos += 25f
            }

            // 最后一页底部备注
            val footerY = pageHeight - 80f
            currentCanvas.drawText("生成时间: ${dateFormat.format(Date())}", 50f, footerY, subtitlePaint)
            currentCanvas.drawText("由秀拼豆生成", 50f, footerY + 20f, subtitlePaint)

            document.finishPage(currentPage)
            
            // 保存 PDF
            val fileName = "xiuperler_${project.name.replace(" ", "_")}_${dateFormat.format(Date())}.pdf"
            val file = File(context.cacheDir, fileName)
            FileOutputStream(file).use { out ->
                document.writeTo(out)
            }
            document.close()
            
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * 导出材料清单为文本
     */
    fun exportMaterialList(
        project: ProjectRecord,
        beadUsage: List<BeadUsage>,
        getColorInfo: (String) -> BeadColor?
    ): String {
        val sb = StringBuilder()
        val dateStr = dateFormat.format(Date())
        
        sb.appendLine("=" .repeat(40))
        sb.appendLine("          拼豆材料清单")
        sb.appendLine("=" .repeat(40))
        sb.appendLine()
        sb.appendLine("项目名称: ${project.name}")
        sb.appendLine("生成时间: $dateStr")
        sb.appendLine("画布尺寸: ${project.beadUsage.firstOrNull()?.let { 
            val size = kotlin.math.sqrt(beadUsage.sumOf { u -> u.quantity }.toDouble()).toInt()
            "${size}x${size}"
        } ?: "N/A"}")
        sb.appendLine()
        sb.appendLine("-".repeat(40))
        sb.appendLine("所需材料:")
        sb.appendLine("-".repeat(40))
        sb.appendLine()
        
        // 按数量排序
        beadUsage.sortedByDescending { it.quantity }.forEach { usage ->
            val colorInfo = getColorInfo(usage.colorCode)
            val colorName = colorInfo?.colorName ?: "-"
            val mardCode = colorInfo?.mardCode ?: usage.colorCode
            sb.appendLine("• $mardCode $colorName: ${usage.quantity}颗")
        }
        
        sb.appendLine()
        sb.appendLine("-".repeat(40))
        sb.appendLine("统计:")
        sb.appendLine("-".repeat(40))
        sb.appendLine("颜色种类: ${beadUsage.size}")
        sb.appendLine("总数量: ${beadUsage.sumOf { it.quantity }}颗")
        
        sb.appendLine()
        sb.appendLine("=" .repeat(40))
        sb.appendLine("由秀拼豆生成")
        
        return sb.toString()
    }
    
    /**
     * 保存文本文件并返回 Uri
     */
    fun saveTextToFile(content: String, fileName: String): Uri? {
        return try {
            val file = File(context.cacheDir, "$fileName.txt")
            file.writeText(content)
            
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * 分享内容
     */
    fun shareContent(uri: Uri, mimeType: String = "*/*", title: String): Intent {
        return Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}

/**
 * 扩展 BeadColor 添加 toArgb 方法
 */
fun BeadColor.toArgb(): Int {
    return Color.argb(255, red, green, blue)
}
