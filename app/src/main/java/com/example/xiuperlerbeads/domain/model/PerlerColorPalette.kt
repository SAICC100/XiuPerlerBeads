package com.example.xiuperlerbeads.domain.model

/**
 * Professional Perler Beads color palette
 * Based on official Perler brand colors
 */
object PerlerColorPalette {

    val allColors: List<PerlerColor> = listOf(
        // Reds
        PerlerColor(1, "白色", 255, 255, 255, "P000"),
        PerlerColor(2, "黑色", 35, 35, 35, "P001"),
        PerlerColor(3, "灰色", 128, 128, 128, "P002"),
        PerlerColor(4, "浅灰", 192, 192, 192, "P003"),
        PerlerColor(5, "暗红", 179, 57, 57, "P004"),
        PerlerColor(6, "红色", 255, 89, 94, "P005"),
        PerlerColor(7, "深粉", 254, 107, 107, "P006"),
        PerlerColor(8, "粉色", 255, 145, 145, "P007"),
        PerlerColor(9, "珊瑚", 250, 114, 104, "P008"),
        PerlerColor(10, "玫瑰红", 241, 91, 108, "P009"),

        // Oranges & Yellows
        PerlerColor(11, "橙色", 255, 145, 67, "P010"),
        PerlerColor(12, "深橙", 255, 107, 53, "P011"),
        PerlerColor(13, "杏色", 255, 182, 119, "P012"),
        PerlerColor(14, "桃色", 255, 182, 155, "P013"),
        PerlerColor(15, "柠檬黄", 255, 237, 121, "P014"),
        PerlerColor(16, "黄色", 255, 230, 109, "P015"),
        PerlerColor(17, "金黄", 255, 209, 72, "P016"),
        PerlerColor(18, "琥珀", 255, 186, 55, "P017"),
        PerlerColor(19, "橙黄", 255, 167, 42, "P018"),

        // Greens
        PerlerColor(20, "深绿", 35, 115, 69, "P019"),
        PerlerColor(21, "绿色", 76, 175, 80, "P020"),
        PerlerColor(22, "浅绿", 129, 199, 132, "P021"),
        PerlerColor(23, "薄荷绿", 78, 205, 196, "P022"),
        PerlerColor(24, "青绿", 0, 181, 167, "P023"),
        PerlerColor(25, "松石绿", 0, 206, 196, "P024"),
        PerlerColor(26, "海绿", 0, 166, 156, "P025"),
        PerlerColor(27, "荧光绿", 140, 255, 25, "P026"),
        PerlerColor(28, "酸橙绿", 178, 255, 89, "P027"),
        PerlerColor(29, "橄榄绿", 115, 140, 62, "P028"),
        PerlerColor(30, "苔藓绿", 94, 114, 52, "P029"),

        // Blues
        PerlerColor(31, "深蓝", 41, 98, 180, "P030"),
        PerlerColor(32, "蓝色", 66, 133, 244, "P031"),
        PerlerColor(33, "浅蓝", 128, 181, 255, "P032"),
        PerlerColor(34, "天蓝", 100, 181, 246, "P033"),
        PerlerColor(35, "宝蓝", 30, 107, 195, "P034"),
        PerlerColor(36, "湖蓝", 0, 168, 212, "P035"),
        PerlerColor(37, "钴蓝", 0, 71, 171, "P036"),
        PerlerColor(38, "海军蓝", 35, 55, 91, "P037"),
        PerlerColor(39, "牛仔蓝", 69, 105, 145, "P038"),
        PerlerColor(40, "雾蓝", 167, 191, 213, "P039"),

        // Purples
        PerlerColor(41, "紫色", 138, 100, 196, "P040"),
        PerlerColor(42, "深紫", 123, 65, 172, "P041"),
        PerlerColor(43, "浅紫", 170, 143, 215, "P042"),
        PerlerColor(44, "薰衣草", 200, 162, 225, "P043"),
        PerlerColor(45, "品红", 214, 78, 172, "P044"),
        PerlerColor(46, "洋红", 195, 55, 132, "P045"),
        PerlerColor(47, "紫红", 188, 79, 155, "P046"),
        PerlerColor(48, "兰花粉", 244, 134, 193, "P047"),
        PerlerColor(49, "亮粉", 255, 105, 180, "P048"),
        PerlerColor(50, "浅粉", 255, 183, 197, "P049"),

        // Browns
        PerlerColor(51, "棕色", 121, 85, 72, "P050"),
        PerlerColor(52, "深棕", 93, 62, 46, "P051"),
        PerlerColor(53, "浅棕", 161, 133, 110, "P052"),
        PerlerColor(54, "摩卡", 140, 100, 70, "P053"),
        PerlerColor(55, "可可", 135, 85, 60, "P054"),
        PerlerColor(56, "咖啡", 111, 78, 55, "P055"),
        PerlerColor(57, "焦糖", 239, 176, 89, "P056"),
        PerlerColor(58, "奶油棕", 222, 184, 135, "P057"),

        // Special Colors
        PerlerColor(59, "肤色", 255, 223, 186, "P058"),
        PerlerColor(60, "浅肤色", 255, 236, 210, "P059"),
        PerlerColor(61, "深肤色", 204, 142, 104, "P060"),
        PerlerColor(62, "米色", 245, 245, 220, "P061"),
        PerlerColor(63, "象牙白", 255, 255, 240, "P062"),
        PerlerColor(64, "银色", 192, 192, 192, "P063"),
        PerlerColor(65, "金色", 212, 175, 55, "P064"),
        PerlerColor(66, "古铜色", 205, 127, 50, "P065"),

        // Pastels
        PerlerColor(67, "薄荷", 189, 224, 215, "P066"),
        PerlerColor(68, "淡紫", 230, 230, 250, "P067"),
        PerlerColor(69, "淡粉", 255, 218, 233, "P068"),
        PerlerColor(70, "淡蓝", 173, 216, 230, "P069"),
        PerlerColor(71, "淡黄", 255, 255, 204, "P070"),
        PerlerColor(72, "淡绿", 220, 237, 207, "P071"),

        // Naturals
        PerlerColor(73, "木色", 170, 130, 100, "P072"),
        PerlerColor(74, "浅木色", 200, 165, 130, "P073"),
        PerlerColor(75, "深木色", 130, 90, 60, "P074"),
        PerlerColor(76, "雪白", 255, 250, 250, "P075"),
        PerlerColor(77, "烟白", 240, 240, 240, "P076"),
        PerlerColor(78, "石墨", 70, 70, 70, "P077"),
        PerlerColor(79, "炭灰", 90, 90, 90, "P078"),
        PerlerColor(80, "透明蓝", 217, 237, 247, "P079")
    )

    /**
     * Find the closest matching Perler color for a given RGB value
     */
    fun findClosestColor(red: Int, green: Int, blue: Int): PerlerColor {
        var minDistance = Double.MAX_VALUE
        var closestColor = allColors[0]

        for (color in allColors) {
            val distance = colorDistance(
                red, green, blue,
                color.red, color.green, color.blue
            )
            if (distance < minDistance) {
                minDistance = distance
                closestColor = color
            }
        }

        return closestColor
    }

    /**
     * Calculate color distance using Euclidean distance in RGB space
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
     * Get a subset of colors for simplified palettes
     */
    fun getBasicColors(): List<PerlerColor> = allColors.take(50)

    /**
     * Get pastel colors only
     */
    fun getPastelColors(): List<PerlerColor> = allColors.filter { it.id in 67..72 }

    /**
     * Get color by ID
     */
    fun getColorById(id: Int): PerlerColor? = allColors.find { it.id == id }
}
