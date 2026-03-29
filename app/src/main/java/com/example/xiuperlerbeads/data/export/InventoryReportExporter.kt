package com.example.xiuperlerbeads.data.export

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.xiuperlerbeads.domain.model.Brand
import com.example.xiuperlerbeads.domain.model.BrandStock
import com.example.xiuperlerbeads.domain.model.HistoryRecord
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * 库存报表导出器
 */
class InventoryReportExporter(private val context: Context) {
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    private val fileNameFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
    
    /**
     * 导出库存报表为 PDF
     */
    fun exportInventoryReportToPdf(
        brands: List<Brand>,
        stocks: List<BrandStock>,
        historyRecords: List<HistoryRecord>
    ): Uri? {
        return try {
            val document = PdfDocument()
            val pageWidth = 595 // A4
            val pageHeight = 842
            
            // 计算统计数据
            val totalColors = stocks.count { !it.isHidden }
            val totalQuantity = stocks.sumOf { it.available }
            val lowStockCount = stocks.count { it.available in 1..50 && !it.isHidden }
            val outOfStockCount = stocks.count { it.available <= 0 && !it.isHidden }
            
            val titlePaint = Paint().apply {
                color = Color.BLACK
                textSize = 24f
                isFakeBoldText = true
            }
            val headerPaint = Paint().apply {
                color = Color.DKGRAY
                textSize = 14f
                isFakeBoldText = true
            }
            val textPaint = Paint().apply {
                color = Color.BLACK
                textSize = 11f
            }
            val subtitlePaint = Paint().apply {
                color = Color.GRAY
                textSize = 12f
            }
            val tablePaint = Paint().apply {
                color = Color.LTGRAY
                strokeWidth = 1f
                style = Paint.Style.STROKE
            }
            
            // 第一页：概览
            val pageInfo1 = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page1 = document.startPage(pageInfo1)
            val canvas1 = page1.canvas
            
            var yPos = 50f
            
            // 标题
            canvas1.drawText("库存报表", 50f, yPos, titlePaint)
            yPos += 30f
            canvas1.drawText("生成时间: ${dateFormat.format(Date())}", 50f, yPos, subtitlePaint)
            yPos += 40f
            
            // 统计概览
            canvas1.drawText("库存概览", 50f, yPos, headerPaint)
            yPos += 25f
            
            val stats = listOf(
                "颜色总数: $totalColors 种",
                "库存总量: $totalQuantity 颗",
                "低库存: $lowStockCount 种",
                "缺货: $outOfStockCount 种"
            )
            stats.forEach { stat ->
                canvas1.drawText(stat, 70f, yPos, textPaint)
                yPos += 20f
            }
            
            yPos += 20f
            
            // 库存分布
            canvas1.drawText("库存状态分布", 50f, yPos, headerPaint)
            yPos += 25f
            
            val statusStats = listOf(
                "充足 (>50颗): ${stocks.count { it.available > 50 }} 种",
                "不足 (10-50颗): ${stocks.count { it.available in 10..50 }} 种",
                "低库存 (1-10颗): ${stocks.count { it.available in 1..9 }} 种",
                "缺货 (0颗): ${stocks.count { it.available <= 0 }} 种"
            )
            statusStats.forEach { stat ->
                canvas1.drawText(stat, 70f, yPos, textPaint)
                yPos += 20f
            }
            
            yPos += 30f
            
            // 最近活动
            canvas1.drawText("最近活动 (${minOf(10, historyRecords.size)} 条)", 50f, yPos, headerPaint)
            yPos += 25f
            
            historyRecords.take(10).forEach { record ->
                if (yPos > pageHeight - 50) return@forEach
                canvas1.drawText(
                    "${dateFormat.format(Date(record.timestamp))} - ${record.description}",
                    70f, yPos, textPaint
                )
                yPos += 18f
            }
            
            document.finishPage(page1)
            
            // 第二页：库存详情
            val pageInfo2 = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 2).create()
            val page2 = document.startPage(pageInfo2)
            val canvas2 = page2.canvas
            
            yPos = 50f
            
            canvas2.drawText("库存详情", 50f, yPos, titlePaint)
            yPos += 35f
            
            // 表格头部
            val columns = listOf("色号", "品牌", "库存", "已用", "可用", "状态")
            val colWidths = listOf(80f, 100f, 60f, 60f, 60f, 80f)
            var xPos = 50f
            
            columns.forEachIndexed { index, col ->
                canvas2.drawText(col, xPos, yPos, headerPaint)
                xPos += colWidths[index]
            }
            
            yPos += 5f
            canvas2.drawLine(50f, yPos, pageWidth - 50f, yPos, tablePaint)
            yPos += 20f
            
            // 按品牌分组显示
            val sortedStocks = stocks
                .filter { !it.isHidden }
                .sortedWith(compareBy({ it.available }, { it.mardCode }))
                .take(40)
            
            sortedStocks.forEach { stock ->
                if (yPos > pageHeight - 50) return@forEach
                
                xPos = 50f
                val brand = brands.find { it.id == stock.brandId }
                
                canvas2.drawText(stock.mardCode, xPos, yPos, textPaint)
                xPos += colWidths[0]
                
                canvas2.drawText(brand?.name ?: "未知", xPos, yPos, textPaint)
                xPos += colWidths[1]
                
                canvas2.drawText("${stock.stock}", xPos, yPos, textPaint)
                xPos += colWidths[2]
                
                canvas2.drawText("${stock.used}", xPos, yPos, textPaint)
                xPos += colWidths[3]
                
                canvas2.drawText("${stock.available}", xPos, yPos, textPaint)
                xPos += colWidths[4]
                
                val status = when {
                    stock.available <= 0 -> "缺货"
                    stock.available < 10 -> "低库存"
                    stock.available < 50 -> "不足"
                    else -> "充足"
                }
                canvas2.drawText(status, xPos, yPos, textPaint)
                
                yPos += 18f
            }
            
            document.finishPage(page2)
            
            // 保存 PDF
            val fileName = "inventory_report_${fileNameFormat.format(Date())}.pdf"
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
     * 导出库存报表为 CSV
     */
    fun exportInventoryReportToCsv(
        brands: List<Brand>,
        stocks: List<BrandStock>
    ): Uri? {
        return try {
            val sb = StringBuilder()
            
            // 表头
            sb.appendLine("色号,品牌,库存,已用,可用,状态")
            
            // 数据行
            stocks.filter { !it.isHidden }.forEach { stock ->
                val brand = brands.find { it.id == stock.brandId }
                val status = when {
                    stock.available <= 0 -> "缺货"
                    stock.available < 10 -> "低库存"
                    stock.available < 50 -> "不足"
                    else -> "充足"
                }
                sb.appendLine("${stock.mardCode},${brand?.name ?: "未知"},${stock.stock},${stock.used},${stock.available},$status")
            }
            
            val fileName = "inventory_${fileNameFormat.format(Date())}.csv"
            val file = File(context.cacheDir, fileName)
            file.writeText(sb.toString())
            
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
     * 分享报表
     */
    fun shareReport(uri: Uri, title: String): Intent {
        return Intent(Intent.ACTION_SEND).apply {
            type = when {
                uri.toString().endsWith(".pdf") -> "application/pdf"
                uri.toString().endsWith(".csv") -> "text/csv"
                else -> "text/plain"
            }
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
