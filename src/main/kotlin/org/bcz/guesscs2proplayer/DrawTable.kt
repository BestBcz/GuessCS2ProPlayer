package org.bcz.guesscs2proplayer

import org.bcz.guesscs2proplayer.GuessCS2ProPlayer.loadSVGFromFile
import org.bcz.guesscs2proplayer.GuessCS2ProPlayer.makeImage
import org.jetbrains.skia.*
import java.io.File


// 绘制表格并保存为文件
fun drawGuessTable(gameState: GameState): File {
    val width = 700
    val maxRows = 11 // 固定 11 行表格（1 行表头 + 10 行猜测）
    val rowHeight = 40f // 格子高度
    val rowSpacing = 10f
    val tableHeight = rowHeight + rowSpacing + (maxRows - 1) * (rowHeight + rowSpacing) // 表头 + 10 行内容
    val height = (tableHeight + 40f).toInt() // 画布高度，留一些边距

    // 创建 Skia 画布
    val surface = Surface.makeRasterN32Premul(width, height)
    val canvas = surface.canvas

    // 绘制背景（深蓝色，偏黑）
    val backgroundPaint = Paint().apply {
        color = Color.makeRGB(10, 20, 30) // 深蓝色偏黑
    }
    canvas.drawRect(Rect.makeXYWH(0f, 0f, width.toFloat(), height.toFloat()), backgroundPaint)

    // 加载字体
    val typeface = Typeface.makeDefault()
    val font = Font(typeface, 16f)

    // 定义表格参数
    val headers = listOf("NAME", "TEAM", "NAT", "AGE", "ROLE", "MAJ APP")
    val columnWidths = listOf(120f, 140f, 80f, 60f, 120f, 80f)
    val tableWidth = columnWidths.sum() - 5f
    val cornerRadius = 4f // 圆角半径
    val outlineOffset = 5f // 白线与表格的距离

    // 计算表格居中的位置
    val tableX = (width - tableWidth) / 2 // 水平居中
    val tableY = (height - tableHeight) / 2 // 垂直居中

    // 绘制表格整体细白线轮廓（固定 11 行高度）
    val tableRect = Rect.makeXYWH(tableX, tableY, tableWidth, tableHeight)
    val outlineRect = tableRect.inflate(outlineOffset) // 向外扩展白线轮廓
    val outlineRRect = outlineRect.toRRect(cornerRadius + outlineOffset)
    val outlinePaint = Paint().apply {
        color = Color.WHITE
        mode = PaintMode.STROKE
        strokeWidth = 1f
    }
    canvas.drawRRect(outlineRRect, outlinePaint)

    // 绘制表头（连起来的格子，灰色，透明度 100）
    val headerRect = Rect.makeXYWH(tableX, tableY, tableWidth, rowHeight)
    val headerRRect = headerRect.toRRect(cornerRadius)
    canvas.drawRectShadowAntiAlias(headerRRect, 2f, 2f, 4f, 2f, Color.makeRGB(0, 0, 0))
    val headerPaint = Paint().apply {
        shader = Shader.makeLinearGradient(
            tableX, tableY, tableX, tableY + rowHeight,
            intArrayOf(Color.makeARGB(100, 90, 90, 90), Color.makeARGB(100, 70, 70, 70)), // 透明度 100
            null,
            GradientStyle(
                tileMode = FilterTileMode.CLAMP,
                isPremul = true,
                localMatrix = null
            )
        )
    }
    canvas.drawRRect(headerRRect, headerPaint)

    // 绘制表头文字（垂直居中）
    var x = tableX
    headers.forEachIndexed { index, header ->
        val textY = tableY + (rowHeight / 2) + (font.metrics.ascent + font.metrics.descent) / 2 // 垂直居中
        canvas.drawString(header, x + 5f, textY, font, Paint().apply { color = Color.WHITE })
        x += columnWidths[index]
    }

    // 绘制表格内容（每行格子分开，颜色比灰色深，透明度 100）
    gameState.guesses.forEachIndexed { index, (_, player) ->
        val y = tableY + rowHeight + rowSpacing * 2 + index * (rowHeight + rowSpacing) // 表头与第一行的间距
        x = tableX

        // 每个字段单独绘制格子
        val fields = listOf(
            player.name,
            player.team,
            player.nationality,
            player.age.toString(),
            player.position,
            "unknown" // MAJ APP 列固定为 "unknown"
        )

        fields.forEachIndexed { fieldIndex, field ->
            val cellRect = Rect.makeXYWH(x, y - rowHeight / 2, columnWidths[fieldIndex] - 5f, rowHeight)
            val cellRRect = cellRect.toRRect(cornerRadius)
            canvas.drawRectShadowAntiAlias(cellRRect, 2f, 2f, 4f, 2f, Color.makeRGB(0, 0, 0))

            // 确定格子颜色（比表头灰色深，透明度 100）
            val cellPaint = Paint().apply {
                shader = when (fieldIndex) {
                    0 -> { // NAME
                        if (player.name == gameState.targetPlayer.name) {
                            Shader.makeLinearGradient(
                                x, y - rowHeight / 2, x, y + rowHeight / 2,
                                intArrayOf(Color.makeARGB(100, 0, 180, 0), Color.makeARGB(100, 0, 140, 0)), // 更亮的绿色
                                null,
                                GradientStyle(tileMode = FilterTileMode.CLAMP, isPremul = true, localMatrix = null)
                            )
                        } else {
                            Shader.makeLinearGradient(
                                x, y - rowHeight / 2, x, y + rowHeight / 2,
                                intArrayOf(Color.makeARGB(100, 60, 60, 60), Color.makeARGB(100, 40, 40, 40)),
                                null,
                                GradientStyle(tileMode = FilterTileMode.CLAMP, isPremul = true, localMatrix = null)
                            )
                        }
                    }
                    1 -> { // TEAM
                        if (player.team == gameState.targetPlayer.team) {
                            Shader.makeLinearGradient(
                                x, y - rowHeight / 2, x, y + rowHeight / 2,
                                intArrayOf(Color.makeARGB(100, 0, 180, 0), Color.makeARGB(100, 0, 140, 0)), // 更亮的绿色
                                null,
                                GradientStyle(tileMode = FilterTileMode.CLAMP, isPremul = true, localMatrix = null)
                            )
                        } else {
                            Shader.makeLinearGradient(
                                x, y - rowHeight / 2, x, y + rowHeight / 2,
                                intArrayOf(Color.makeARGB(100, 60, 60, 60), Color.makeARGB(100, 40, 40, 40)),
                                null,
                                GradientStyle(tileMode = FilterTileMode.CLAMP, isPremul = true, localMatrix = null)
                            )
                        }
                    }
                    2 -> { // NAT
                        val guessedContinent = GuessCS2ProPlayer.countryContinents[GuessCS2ProPlayer.countryToCode[player.nationality]?.lowercase() ?: player.nationality.lowercase()] ?: "Unknown"
                        val targetContinent = GuessCS2ProPlayer.countryContinents[GuessCS2ProPlayer.countryToCode[gameState.targetPlayer.nationality]?.lowercase() ?: gameState.targetPlayer.nationality.lowercase()] ?: "Unknown"
                        when {
                            player.nationality == gameState.targetPlayer.nationality -> Shader.makeLinearGradient(
                                x, y - rowHeight / 2, x, y + rowHeight / 2,
                                intArrayOf(Color.makeARGB(100, 0, 180, 0), Color.makeARGB(100, 0, 140, 0)), // 更亮的绿色
                                null,
                                GradientStyle(tileMode = FilterTileMode.CLAMP, isPremul = true, localMatrix = null)
                            )
                            guessedContinent == targetContinent && guessedContinent != "Unknown" -> Shader.makeLinearGradient(
                                x, y - rowHeight / 2, x, y + rowHeight / 2,
                                intArrayOf(Color.makeARGB(100, 255, 255, 0), Color.makeARGB(100, 200, 200, 0)), // 更亮的黄色
                                null,
                                GradientStyle(tileMode = FilterTileMode.CLAMP, isPremul = true, localMatrix = null)
                            )
                            else -> Shader.makeLinearGradient(
                                x, y - rowHeight / 2, x, y + rowHeight / 2,
                                intArrayOf(Color.makeARGB(100, 60, 60, 60), Color.makeARGB(100, 40, 40, 40)),
                                null,
                                GradientStyle(tileMode = FilterTileMode.CLAMP, isPremul = true, localMatrix = null)
                            )
                        }
                    }
                    3 -> { // AGE
                        val guessedAge = player.age
                        val targetAge = gameState.targetPlayer.age
                        if (guessedAge == targetAge) {
                            Shader.makeLinearGradient(
                                x, y - rowHeight / 2, x, y + rowHeight / 2,
                                intArrayOf(Color.makeARGB(100, 0, 180, 0), Color.makeARGB(100, 0, 140, 0)), // 更亮的绿色
                                null,
                                GradientStyle(tileMode = FilterTileMode.CLAMP, isPremul = true, localMatrix = null)
                            )
                        } else {
                            Shader.makeLinearGradient(
                                x, y - rowHeight / 2, x, y + rowHeight / 2,
                                intArrayOf(Color.makeARGB(100, 60, 60, 60), Color.makeARGB(100, 40, 40, 40)),
                                null,
                                GradientStyle(tileMode = FilterTileMode.CLAMP, isPremul = true, localMatrix = null)
                            )
                        }
                    }
                    4 -> { // ROLE
                        if (player.position == gameState.targetPlayer.position) {
                            Shader.makeLinearGradient(
                                x, y - rowHeight / 2, x, y + rowHeight / 2,
                                intArrayOf(Color.makeARGB(100, 0, 180, 0), Color.makeARGB(100, 0, 140, 0)), // 更亮的绿色
                                null,
                                GradientStyle(tileMode = FilterTileMode.CLAMP, isPremul = true, localMatrix = null)
                            )
                        } else {
                            Shader.makeLinearGradient(
                                x, y - rowHeight / 2, x, y + rowHeight / 2,
                                intArrayOf(Color.makeARGB(100, 60, 60, 60), Color.makeARGB(100, 40, 40, 40)),
                                null,
                                GradientStyle(tileMode = FilterTileMode.CLAMP, isPremul = true, localMatrix = null)
                            )
                        }
                    }
                    5 -> { // MAJ APP
                        Shader.makeLinearGradient(
                            x, y - rowHeight / 2, x, y + rowHeight / 2,
                            intArrayOf(Color.makeARGB(100, 60, 60, 60), Color.makeARGB(100, 40, 40, 40)),
                            null,
                            GradientStyle(tileMode = FilterTileMode.CLAMP, isPremul = true, localMatrix = null)
                        )
                    }
                    else -> Shader.makeLinearGradient(
                        x, y - rowHeight / 2, x, y + rowHeight / 2,
                        intArrayOf(Color.makeARGB(100, 60, 60, 60), Color.makeARGB(100, 40, 40, 40)),
                        null,
                        GradientStyle(tileMode = FilterTileMode.CLAMP, isPremul = true, localMatrix = null)
                    )
                }
            }
            canvas.drawRRect(cellRRect, cellPaint)

            // 绘制内容
            if (fieldIndex == 2) { // NAT 列显示国旗
                try {
                    val svg = loadSVGFromFile(player.nationality)
                    val flagImage = svg.makeImage(24f, 24f)
                    val flagRect = Rect.makeXYWH(x + 5f, y - 12f, 24f, 24f).toRRect(4f)
                    canvas.drawImageRRect(flagImage, flagRect)
                    GuessCS2ProPlayer.logger.info("Successfully rendered flag for nationality: ${player.nationality}")
                } catch (e: Exception) {
                    GuessCS2ProPlayer.logger.error("Failed to render flag for nationality: ${player.nationality}, error: ${e.message}", e)
                    // 显示国家缩写（如果有映射）或原始国家名称
                    val displayText = GuessCS2ProPlayer.countryToCode[player.nationality] ?: player.nationality
                    val textY = y + (font.metrics.ascent + font.metrics.descent) / 2 // 垂直居中
                    canvas.drawString(displayText, x + 5f, textY, font, Paint().apply { color = Color.WHITE })
                }
            } else if (fieldIndex == 3) { // AGE
                val ageText = when {
                    player.age == gameState.targetPlayer.age -> player.age.toString()
                    player.age < gameState.targetPlayer.age -> "${player.age} ↑"
                    else -> "${player.age} ↓"
                }
                val textY = y + (font.metrics.ascent + font.metrics.descent) / 2 // 垂直居中
                canvas.drawString(ageText, x + 5f, textY, font, Paint().apply { color = Color.WHITE })
            } else {
                val textY = y + (font.metrics.ascent + font.metrics.descent) / 2 // 垂直居中
                canvas.drawString(field, x + 5f, textY, font, Paint().apply { color = Color.WHITE })
            }

            x += columnWidths[fieldIndex]
        }
    }

    // 将画布内容保存为图片
    val image = surface.makeImageSnapshot()
    val data = image.encodeToData(EncodedImageFormat.PNG)
    val tempFile = File.createTempFile("guesscs2player", ".png")
    tempFile.writeBytes(data!!.bytes)

    return tempFile
}