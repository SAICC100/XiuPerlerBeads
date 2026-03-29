package com.example.xiuperlerbeads.domain.model

import android.content.Context
import com.example.xiuperlerbeads.domain.model.ColorSystem
import org.json.JSONArray
import java.io.InputStreamReader

/**
 * 拼豆颜色管理器
 * 从 assets/allcolors.json 加载完整的多品牌色号对照表
 */
object BeadColorManager {

    private var allColors: List<BeadColor> = emptyList()
    private var isLoaded = false

    // 缓存查找结果
    private val mardCodeMap: MutableMap<String, BeadColor> = mutableMapOf()
    private val cocoCodeMap: MutableMap<String, BeadColor> = mutableMapOf()
    private val manmanCodeMap: MutableMap<String, BeadColor> = mutableMapOf()
    private val kakaCodeMap: MutableMap<String, BeadColor> = mutableMapOf()
    private val panpanCodeMap: MutableMap<String, BeadColor> = mutableMapOf()
    private val mixiaowoCodeMap: MutableMap<String, BeadColor> = mutableMapOf()

    /**
     * 从 assets 加载颜色数据
     */
    fun loadFromAssets(context: Context) {
        if (isLoaded) return

        try {
            val inputStream = context.assets.open("allcolors.json")
            val reader = InputStreamReader(inputStream)
            val jsonString = reader.readText()
            reader.close()

            parseColors(jsonString)
            isLoaded = true
        } catch (e: Exception) {
            e.printStackTrace()
            // 如果加载失败，使用内置的默认颜色
            loadDefaultColors()
        }
    }

    /**
     * 解析 JSON 颜色数据
     */
    private fun parseColors(jsonString: String) {
        val colors = mutableListOf<BeadColor>()

        try {
            val jsonArray = JSONArray(jsonString)

            for (i in 0 until jsonArray.length()) {
                val colorObj = jsonArray.getJSONObject(i)

                val colorHex = colorObj.optString("colorHex", "808080")
                val rgb = parseHexColor(colorHex)

                val color = BeadColor(
                    mardCode = colorObj.optString("mardCode", ""),
                    cocoCode = colorObj.optString("cocoCode", ""),
                    manmanCode = colorObj.optString("manmanCode", ""),
                    panpanCode = colorObj.optString("panpanCode", ""),
                    mixiaowoCode = colorObj.optString("mixiaowoCode", ""),
                    kakaCode = colorObj.optString("kakaCode", ""),
                    red = rgb.first,
                    green = rgb.second,
                    blue = rgb.third
                )

                colors.add(color)

                // 建立索引
                if (color.mardCode.isNotEmpty()) {
                    mardCodeMap[color.mardCode.uppercase()] = color
                }
                if (color.cocoCode.isNotEmpty()) {
                    cocoCodeMap[color.cocoCode.uppercase()] = color
                }
                if (color.manmanCode.isNotEmpty()) {
                    manmanCodeMap[color.manmanCode.uppercase()] = color
                }
                if (color.kakaCode.isNotEmpty()) {
                    kakaCodeMap[color.kakaCode.uppercase()] = color
                }
                if (color.panpanCode.isNotEmpty()) {
                    panpanCodeMap[color.panpanCode.uppercase()] = color
                }
                if (color.mixiaowoCode.isNotEmpty()) {
                    mixiaowoCodeMap[color.mixiaowoCode.uppercase()] = color
                }
            }

            allColors = colors
        } catch (e: Exception) {
            e.printStackTrace()
            loadDefaultColors()
        }
    }

    /**
     * 解析十六进制颜色
     */
    private fun parseHexColor(hex: String): Triple<Int, Int, Int> {
        return try {
            val cleanHex = hex.removePrefix("#")
            Triple(
                cleanHex.substring(0, 2).toInt(16),
                cleanHex.substring(2, 4).toInt(16),
                cleanHex.substring(4, 6).toInt(16)
            )
        } catch (e: Exception) {
            Triple(128, 128, 128)
        }
    }

    /**
     * 加载默认颜色（当 JSON 加载失败时使用）
     */
    private fun loadDefaultColors() {
        // 使用 MARD 色系的 100 种基础颜色
        allColors = createDefaultMardColors()
        rebuildIndexes()
        isLoaded = true
    }

    /**
     * 创建默认 MARD 颜色列表
     */
    private fun createDefaultMardColors(): List<BeadColor> {
        // 简化的默认颜色列表
        val defaultColors = listOf(
            Triple("A1", "FAF4C8", "浅黄"),
            Triple("A5", "F4D738", "明黄"),
            Triple("A8", "FFDA45", "橙黄"),
            Triple("A14", "FD543D", "红橙"),
            Triple("B1", "E6EE31", "荧光绿"),
            Triple("B5", "35E352", "翠绿"),
            Triple("C5", "01ACEB", "天蓝"),
            Triple("C8", "0F54C0", "深蓝"),
            Triple("D3", "2F54AF", "钴蓝"),
            Triple("D5", "B843C5", "紫罗兰"),
            Triple("E4", "E8649E", "玫红"),
            Triple("E13", "B5006D", "深红"),
            Triple("F5", "E7002F", "正红"),
            Triple("F8", "BC0028", "暗红"),
            Triple("G6", "E99C17", "金黄"),
            Triple("H1", "FDFBFF", "纯白"),
            Triple("H7", "000000", "纯黑"),
            Triple("H9", "EDEDED", "浅灰")
        )

        return defaultColors.mapIndexed { index, (code, hex, name) ->
            val rgb = parseHexColor(hex)
            BeadColor(
                mardCode = code,
                colorName = name,
                red = rgb.first,
                green = rgb.second,
blue = rgb.third
            )
        }
    }

    /**
     * 重建所有索引
     */
    private fun rebuildIndexes() {
        mardCodeMap.clear()
        cocoCodeMap.clear()
        manmanCodeMap.clear()
        kakaCodeMap.clear()
        panpanCodeMap.clear()
        mixiaowoCodeMap.clear()

        for (color in allColors) {
            if (color.mardCode.isNotEmpty()) {
                mardCodeMap[color.mardCode.uppercase()] = color
            }
            if (color.cocoCode.isNotEmpty()) {
                cocoCodeMap[color.cocoCode.uppercase()] = color
            }
            if (color.manmanCode.isNotEmpty()) {
                manmanCodeMap[color.manmanCode.uppercase()] = color
            }
            if (color.kakaCode.isNotEmpty()) {
                kakaCodeMap[color.kakaCode.uppercase()] = color
            }
            if (color.panpanCode.isNotEmpty()) {
                panpanCodeMap[color.panpanCode.uppercase()] = color
            }
            if (color.mixiaowoCode.isNotEmpty()) {
                mixiaowoCodeMap[color.mixiaowoCode.uppercase()] = color
            }
        }
    }

    /**
     * 获取所有颜色
     */
    fun getAllColors(): List<BeadColor> = allColors

    /**
     * 根据 MARD 色号查找颜色
     */
    fun findByMardCode(code: String): BeadColor? {
        return mardCodeMap[code.uppercase()]
    }

    /**
     * 根据色号和色号体系查找颜色
     */
    fun findByCode(code: String, system: ColorSystem): BeadColor? {
        val normalizedCode = code.uppercase().trim()

        return when (system) {
            ColorSystem.MARD -> mardCodeMap[normalizedCode]
            ColorSystem.COCO -> cocoCodeMap[normalizedCode]
            ColorSystem.MANMAN -> manmanCodeMap[normalizedCode]
            ColorSystem.KAKA -> kakaCodeMap[normalizedCode]
            ColorSystem.PANPAN -> panpanCodeMap[normalizedCode]
            ColorSystem.MIXIAOWO -> mixiaowoCodeMap[normalizedCode]
        }
    }

    /**
     * 根据任意色号查找颜色（优先匹配 MARD）
     */
    fun findByAnyCode(code: String): BeadColor? {
        val normalizedCode = code.uppercase().trim()

        // 优先精确匹配 MARD
        mardCodeMap[normalizedCode]?.let { return it }

        // 匹配其他品牌
        cocoCodeMap[normalizedCode]?.let { return it }
        manmanCodeMap[normalizedCode]?.let { return it }
        kakaCodeMap[normalizedCode]?.let { return it }
        panpanCodeMap[normalizedCode]?.let { return it }
        mixiaowoCodeMap[normalizedCode]?.let { return it }

        return null
    }

    /**
     * 搜索颜色
     */
    fun search(query: String, system: ColorSystem): List<BeadColor> {
        val normalizedQuery = query.uppercase().trim()
        if (normalizedQuery.isEmpty()) return allColors

        return allColors.filter { color ->
            val displayCode = color.displayCode(system).uppercase()
            displayCode.contains(normalizedQuery) ||
            color.mardCode.uppercase().contains(normalizedQuery) ||
            color.colorName.contains(normalizedQuery)
        }
    }

    /**
     * 获取指定色号体系的所有颜色
     */
    fun getColorsForSystem(system: ColorSystem): List<BeadColor> {
        return allColors.filter { it.hasCode(system) }
    }

    /**
     * 根据 RGB 查找最接近的颜色
     */
    fun findClosestColor(red: Int, green: Int, blue: Int): BeadColor? {
        if (allColors.isEmpty()) return null

        var minDistance = Double.MAX_VALUE
        var closestColor: BeadColor? = null

        for (color in allColors) {
            val distance = colorDistance(red, green, blue, color.red, color.green, color.blue)
            if (distance < minDistance) {
                minDistance = distance
                closestColor = color
            }
        }

        return closestColor
    }

    /**
     * 计算颜色距离（加权欧几里得距离）
     */
    private fun colorDistance(r1: Int, g1: Int, b1: Int, r2: Int, g2: Int, b2: Int): Double {
        val rMean = (r1 + r2) / 2.0
        val dr = (r1 - r2).toDouble()
        val dg = (g1 - g2).toDouble()
        val db = (b1 - b2).toDouble()
        return kotlin.math.sqrt(
            (2 + rMean / 256) * dr * dr +
            4 * dg * dg +
            (2 + (255 - rMean) / 256) * db * db
        )
    }
    
    /**
     * 根据十六进制颜色字符串查找最接近的颜色
     */
    fun findClosestColor(hex: String): BeadColor? {
        return try {
            val cleanHex = hex.removePrefix("#")
            val r = cleanHex.substring(0, 2).toInt(16)
            val g = cleanHex.substring(2, 4).toInt(16)
            val b = cleanHex.substring(4, 6).toInt(16)
            findClosestColor(r, g, b)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 转换色号到 MARD 格式
     */
    fun convertToMardCode(code: String, fromSystem: ColorSystem): String? {
        val color = findByCode(code, fromSystem)
        return color?.mardCode
    }

    /**
     * 在不同色号体系间转换
     */
    fun convertCode(code: String, fromSystem: ColorSystem, toSystem: ColorSystem): String? {
        val mardCode = convertToMardCode(code, fromSystem) ?: return null
        val color = findByMardCode(mardCode) ?: return null
        return color.displayCode(toSystem)
    }
}
