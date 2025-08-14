package org.bcz.guesscs2proplayer

import org.bcz.guesscs2proplayer.utils.CountryUtils
import org.jetbrains.skia.*
import java.io.File
import kotlin.random.Random
import org.jetbrains.skia.svg.SVGDOM

fun drawGuessTable(gameState: GameState): File {
    val width = 600 // 减小画布宽度，优化文件大小
    val maxRows = 11 // 固定 11 行表格（1 行表头 + 10 行猜测）
    val rowHeight = 40f // 减小行高
    val rowSpacing = 6f // 减小行间距
    val tableHeight = rowHeight + rowSpacing + (maxRows - 1) * (rowHeight + rowSpacing)
    val height = (tableHeight + 50f).toInt() // 减小顶部边距

    val surface = Surface.makeRasterN32Premul(width, height)
    val canvas = surface.canvas

    // 现代渐变背景
    val backgroundPaint = Paint().apply {
        shader = Shader.makeLinearGradient(
            0f, 0f, 0f, height.toFloat(),
            intArrayOf(
                Color.makeRGB(248, 250, 252), // 超浅蓝灰色顶部
                Color.makeRGB(241, 245, 249), // 浅蓝灰色中部
                Color.makeRGB(226, 232, 240)  // 稍深的蓝灰色底部
            ),
            null,
            GradientStyle(tileMode = FilterTileMode.CLAMP, isPremul = true, localMatrix = null)
        )
    }
    canvas.drawRect(Rect.makeXYWH(0f, 0f, width.toFloat(), height.toFloat()), backgroundPaint)

    // 尝试加载自定义字体，如果失败则使用默认字体
    val typeface = try {
        val fontFile = File(GuessCS2ProPlayer.dataFolder, "TheNeue-Black.ttf")
        if (fontFile.exists()) {
            Typeface.makeFromFile(fontFile.absolutePath)
        } else {
            GuessCS2ProPlayer.logger.warning("Custom font file not found: ${fontFile.absolutePath}, using default font")
            Typeface.makeDefault()
        }
    } catch (e: Exception) {
        GuessCS2ProPlayer.logger.error("Failed to load custom font, using default font: ${e.message}", e)
        Typeface.makeDefault()
    }
    
    val headerFont = Font(typeface, 16f) // 减小表头字体
    val contentFont = Font(typeface, 14f) // 减小内容字体
    
    // 现代文字颜色
    val headerTextPaint = Paint().apply { 
        color = Color.WHITE // 白色，在渐变背景上更清晰
        isAntiAlias = true
    }
    val contentTextPaint = Paint().apply { 
        color = Color.makeRGB(51, 65, 85) // 现代深蓝灰色
        isAntiAlias = true
    }

    val headers = listOf("选手姓名", "队伍", "国籍", "年龄", "位置")
    val columnWidths = listOf(90f, 150f, 60f, 50f, 80f) // 减小列宽
    val columnSpacing = 3f // 减小列间距
    val tableWidth = columnWidths.sum() + (columnWidths.size - 1) * columnSpacing + 16f // 减小边距
    val cornerRadius = 12f // 减小圆角，适应新尺寸

    val tableX = (width - tableWidth) / 2
    val tableY = (height - tableHeight) / 2

    // 绘制简化阴影效果
    val shadowRect = Rect.makeXYWH(tableX + 4f, tableY + 4f, tableWidth, tableHeight)
    val shadowRRect = shadowRect.toRRect(cornerRadius)
    val shadowPaint = Paint().apply {
        color = Color.makeARGB(60, 0, 0, 0)
        isAntiAlias = true
    }
    canvas.drawRRect(shadowRRect, shadowPaint)

    // 主表格背景 - 毛玻璃效果
    val tableRect = Rect.makeXYWH(tableX, tableY, tableWidth, tableHeight)
    val tableRRect = tableRect.toRRect(cornerRadius)
    
    // 绘制半透明白色背景
    val tablePaint = Paint().apply {
        color = Color.makeARGB(200, 255, 255, 255) // 半透明白色
        isAntiAlias = true
    }
    canvas.drawRRect(tableRRect, tablePaint)
    
    // 添加内边框高光效果
    val borderPaint = Paint().apply {
        color = Color.makeARGB(30, 255, 255, 255) // 白色高光边框
        isAntiAlias = true
        strokeWidth = 1f
    }
    // 绘制边框（使用描边效果）
    canvas.drawRRect(tableRRect, borderPaint)

    // 表头 - 现代渐变设计
    val headerRect = Rect.makeXYWH(tableX, tableY, tableWidth, rowHeight)
    val headerRRect = headerRect.toRRect(cornerRadius)
    
    // 绘制表头背景渐变
    val headerPaint = Paint().apply {
        shader = Shader.makeLinearGradient(
            tableX, tableY, tableX, tableY + rowHeight,
            intArrayOf(
                Color.makeRGB(99, 102, 241), // 现代靛蓝色
                Color.makeRGB(139, 92, 246), // 现代紫色
                Color.makeRGB(168, 85, 247)  // 现代紫罗兰色
            ),
            null,
            GradientStyle(tileMode = FilterTileMode.CLAMP, isPremul = true, localMatrix = null)
        )
        isAntiAlias = true
    }
    canvas.drawRRect(headerRRect, headerPaint)
    
    // 添加表头高光效果
    val headerHighlightPaint = Paint().apply {
        shader = Shader.makeLinearGradient(
            tableX, tableY, tableX, tableY + rowHeight / 2,
            intArrayOf(
                Color.makeARGB(60, 255, 255, 255), // 半透明白色高光
                Color.makeARGB(0, 255, 255, 255)   // 透明
            ),
            null,
            GradientStyle(tileMode = FilterTileMode.CLAMP, isPremul = true, localMatrix = null)
        )
        isAntiAlias = true
    }
    canvas.drawRRect(headerRRect, headerHighlightPaint)

    // 绘制表头文字
    var x = tableX + 10f
    headers.forEachIndexed { index, header ->
        val textLine = TextLine.make(header, headerFont)
        val textWidth = textLine.width
        val textX = x + (columnWidths[index] - textWidth) / 2
        val textY = tableY + rowHeight / 2 - (headerFont.metrics.ascent + headerFont.metrics.descent) / 2
        canvas.drawTextLine(textLine, textX, textY, headerTextPaint)
        x += columnWidths[index] + 4f
    }

    // 绘制猜测行
    gameState.guesses.forEachIndexed { index, (_, player) ->
        val y = tableY + rowHeight + rowSpacing + index * (rowHeight + rowSpacing)
        x = tableX + 10f

        val fields = listOf(
            player.name,
            player.team,
            player.nationality,
            player.age.toString(),
            player.position
        )

        fields.forEachIndexed { fieldIndex, field ->
            val cellRect = Rect.makeXYWH(x, y, columnWidths[fieldIndex], rowHeight)
            val cellRRect = cellRect.toRRect(6f) // 减小圆角，适应新尺寸
            
            // 根据匹配情况设置单元格颜色
            val cellPaint = Paint().apply {
                shader = when (fieldIndex) {
                    0 -> { // 姓名
                        if (player.name == gameState.targetPlayer.name) {
                            Shader.makeLinearGradient(
                                x, y, x, y + rowHeight,
                                intArrayOf(
                                    Color.makeRGB(0, 176, 80), // Windows 10 绿色
                                    Color.makeRGB(0, 150, 70)
                                ),
                                null,
                                GradientStyle(tileMode = FilterTileMode.CLAMP, isPremul = true, localMatrix = null)
                            )
                        } else {
                            Shader.makeLinearGradient(
                                x, y, x, y + rowHeight,
                                intArrayOf(
                                    Color.makeRGB(245, 245, 245), // 浅灰色
                                    Color.makeRGB(235, 235, 235)
                                ),
                                null,
                                GradientStyle(tileMode = FilterTileMode.CLAMP, isPremul = true, localMatrix = null)
                            )
                        }
                    }
                    1 -> { // 队伍
                        if (player.team == gameState.targetPlayer.team) {
                            Shader.makeLinearGradient(
                                x, y, x, y + rowHeight,
                                intArrayOf(
                                    Color.makeRGB(0, 176, 80),
                                    Color.makeRGB(0, 150, 70)
                                ),
                                null,
                                GradientStyle(tileMode = FilterTileMode.CLAMP, isPremul = true, localMatrix = null)
                            )
                        } else {
                            Shader.makeLinearGradient(
                                x, y, x, y + rowHeight,
                                intArrayOf(
                                    Color.makeRGB(245, 245, 245),
                                    Color.makeRGB(235, 235, 235)
                                ),
                                null,
                                GradientStyle(tileMode = FilterTileMode.CLAMP, isPremul = true, localMatrix = null)
                            )
                        }
                    }
                    2 -> { // 国籍
                        val guessedContinent = CountryUtils.countryContinents[CountryUtils.countryToCode[player.nationality]?.lowercase() ?: player.nationality.lowercase()] ?: "Unknown"
                        val targetContinent = CountryUtils.countryContinents[CountryUtils.countryToCode[gameState.targetPlayer.nationality]?.lowercase() ?: gameState.targetPlayer.nationality.lowercase()] ?: "Unknown"
                        when {
                            player.nationality == gameState.targetPlayer.nationality -> Shader.makeLinearGradient(
                                x, y, x, y + rowHeight,
                                intArrayOf(
                                    Color.makeRGB(0, 176, 80),
                                    Color.makeRGB(0, 150, 70)
                                ),
                                null,
                                GradientStyle(tileMode = FilterTileMode.CLAMP, isPremul = true, localMatrix = null)
                            )
                            guessedContinent == targetContinent && guessedContinent != "Unknown" -> Shader.makeLinearGradient(
                                x, y, x, y + rowHeight,
                                intArrayOf(
                                    Color.makeRGB(255, 185, 0), // Windows 10 黄色
                                    Color.makeRGB(230, 165, 0)
                                ),
                                null,
                                GradientStyle(tileMode = FilterTileMode.CLAMP, isPremul = true, localMatrix = null)
                            )
                            else -> Shader.makeLinearGradient(
                                x, y, x, y + rowHeight,
                                intArrayOf(
                                    Color.makeRGB(245, 245, 245),
                                    Color.makeRGB(235, 235, 235)
                                ),
                                null,
                                GradientStyle(tileMode = FilterTileMode.CLAMP, isPremul = true, localMatrix = null)
                            )
                        }
                    }
                    3 -> { // 年龄
                        val guessedAge = player.age
                        val targetAge = gameState.targetPlayer.age
                        when {
                            guessedAge == targetAge -> Shader.makeLinearGradient(
                                x, y, x, y + rowHeight,
                                intArrayOf(
                                    Color.makeRGB(0, 176, 80),
                                    Color.makeRGB(0, 150, 70)
                                ),
                                null,
                                GradientStyle(tileMode = FilterTileMode.CLAMP, isPremul = true, localMatrix = null)
                            )
                            Math.abs(guessedAge - targetAge) <= 2 -> Shader.makeLinearGradient(
                                x, y, x, y + rowHeight,
                                intArrayOf(
                                    Color.makeRGB(255, 185, 0),
                                    Color.makeRGB(230, 165, 0)
                                ),
                                null,
                                GradientStyle(tileMode = FilterTileMode.CLAMP, isPremul = true, localMatrix = null)
                            )
                            else -> Shader.makeLinearGradient(
                                x, y, x, y + rowHeight,
                                intArrayOf(
                                    Color.makeRGB(245, 245, 245),
                                    Color.makeRGB(235, 235, 235)
                                ),
                                null,
                                GradientStyle(tileMode = FilterTileMode.CLAMP, isPremul = true, localMatrix = null)
                            )
                        }
                    }
                    4 -> { // 位置
                        if (player.position == gameState.targetPlayer.position) {
                            Shader.makeLinearGradient(
                                x, y, x, y + rowHeight,
                                intArrayOf(
                                    Color.makeRGB(0, 176, 80),
                                    Color.makeRGB(0, 150, 70)
                                ),
                                null,
                                GradientStyle(tileMode = FilterTileMode.CLAMP, isPremul = true, localMatrix = null)
                            )
                        } else {
                            Shader.makeLinearGradient(
                                x, y, x, y + rowHeight,
                                intArrayOf(
                                    Color.makeRGB(245, 245, 245),
                                    Color.makeRGB(235, 235, 235)
                                ),
                                null,
                                GradientStyle(tileMode = FilterTileMode.CLAMP, isPremul = true, localMatrix = null)
                            )
                        }
                    }
                    else -> Shader.makeLinearGradient(
                        x, y, x, y + rowHeight,
                        intArrayOf(
                            Color.makeRGB(245, 245, 245),
                            Color.makeRGB(235, 235, 235)
                        ),
                        null,
                        GradientStyle(tileMode = FilterTileMode.CLAMP, isPremul = true, localMatrix = null)
                    )
                }
                isAntiAlias = true
            }
            canvas.drawRRect(cellRRect, cellPaint)

            // 绘制单元格内容
            if (fieldIndex == 2) { // 国籍 - 显示国旗
                try {
                    val svg = CountryUtils.loadSVGFromFile(GuessCS2ProPlayer.dataFolder, player.nationality)
                    val flagImage = svg.makeImage(24f, 24f) // 减小国旗尺寸
                    val flagRect = Rect.makeXYWH(x + (columnWidths[fieldIndex] - 24f) / 2, y + (rowHeight - 24f) / 2, 24f, 24f).toRRect(3f)
                    canvas.drawImageRRect(flagImage, flagRect)
                    GuessCS2ProPlayer.logger.info("Successfully rendered flag for nationality: ${player.nationality}")
                } catch (e: Exception) {
                    GuessCS2ProPlayer.logger.error("Failed to render flag for nationality: ${player.nationality}, error: ${e.message}", e)
                    val displayText = CountryUtils.countryToCode[player.nationality] ?: player.nationality
                    val textLine = TextLine.make(displayText, contentFont)
                    val textWidth = textLine.width
                    val textX = x + (columnWidths[fieldIndex] - textWidth) / 2
                    val textY = y + rowHeight / 2 - (contentFont.metrics.ascent + contentFont.metrics.descent) / 2
                    canvas.drawTextLine(textLine, textX, textY, contentTextPaint)
                }
            } else if (fieldIndex == 3) { // 年龄 - 显示箭头指示
                val ageText = when {
                    player.age == gameState.targetPlayer.age -> player.age.toString()
                    player.age < gameState.targetPlayer.age -> "${player.age} ↑"
                    else -> "${player.age} ↓"
                }
                val textLine = TextLine.make(ageText, contentFont)
                val textWidth = textLine.width
                val textX = x + (columnWidths[fieldIndex] - textWidth) / 2
                val textY = y + rowHeight / 2 - (contentFont.metrics.ascent + contentFont.metrics.descent) / 2
                canvas.drawTextLine(textLine, textX, textY, contentTextPaint)
            } else { // 其他字段
                val textLine = TextLine.make(field, contentFont)
                val textWidth = textLine.width
                val textX = x + (columnWidths[fieldIndex] - textWidth) / 2
                val textY = y + rowHeight / 2 - (contentFont.metrics.ascent + contentFont.metrics.descent) / 2
                canvas.drawTextLine(textLine, textX, textY, contentTextPaint)
            }

            x += columnWidths[fieldIndex] + columnSpacing
        }
    }

    // 添加标题 - 现代设计
    val titleFont = Font(typeface, 22f) // 减小标题字体
    val titleText = "CS2 猜职业选手"
    val titleLine = TextLine.make(titleText, titleFont)
    val titleX = (width - titleLine.width) / 2
    val titleY = 25f
    
    // 绘制标题阴影
    val titleShadowPaint = Paint().apply {
        color = Color.makeARGB(60, 0, 0, 0)
        isAntiAlias = true
    }
    canvas.drawTextLine(titleLine, titleX + 2f, titleY + 2f, titleShadowPaint)
    
    // 绘制主标题
    val titlePaint = Paint().apply {
        shader = Shader.makeLinearGradient(
            titleX, titleY, titleX + titleLine.width, titleY,
            intArrayOf(
                Color.makeRGB(99, 102, 241), // 现代靛蓝色
                Color.makeRGB(168, 85, 247)  // 现代紫罗兰色
            ),
            null,
            GradientStyle(tileMode = FilterTileMode.CLAMP, isPremul = true, localMatrix = null)
        )
        isAntiAlias = true
    }
    canvas.drawTextLine(titleLine, titleX, titleY, titlePaint)

    val image = surface.makeImageSnapshot()
    val data = image.encodeToData(EncodedImageFormat.PNG)
    val uniqueId = "${System.currentTimeMillis()}_${Random.nextInt(10000)}"
    val tempFile = File.createTempFile("guesscs2player_$uniqueId", ".png")
    tempFile.writeBytes(data!!.bytes)

    return tempFile
}

fun SVGDOM.makeImage(width: Float, height: Float): Image {
    setContainerSize(width, height)
    return Surface.makeRasterN32Premul(width.toInt(), height.toInt()).apply { render(canvas) }.makeImageSnapshot()
}