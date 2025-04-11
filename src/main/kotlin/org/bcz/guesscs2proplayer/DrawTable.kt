package org.bcz.guesscs2proplayer

import org.bcz.guesscs2proplayer.utils.CountryUtils
import org.jetbrains.skia.*
import java.io.File
import kotlin.random.Random
import org.jetbrains.skia.svg.SVGDOM

fun drawGuessTable(gameState: GameState): File {
    val width = 700
    val maxRows = 11 // 固定 11 行表格（1 行表头 + 10 行猜测）
    val rowHeight = 35f
    val rowSpacing = 10f
    val tableHeight = rowHeight + rowSpacing + (maxRows - 1) * (rowHeight + rowSpacing)
    val height = (tableHeight + 40f).toInt()

    val surface = Surface.makeRasterN32Premul(width, height)
    val canvas = surface.canvas

    val backgroundPaint = Paint().apply {
        color = Color.makeRGB(10, 20, 30)
    }
    canvas.drawRect(Rect.makeXYWH(0f, 0f, width.toFloat(), height.toFloat()), backgroundPaint)

    val typeface = Typeface.makeDefault()
    val font = Font(typeface, 16f)
    val textPaint = Paint().apply { color = Color.WHITE }

    val headers = listOf("NAME", "TEAM", "NAT", "AGE", "ROLE", "MAJ APP")
    val columnWidths = listOf(120f, 140f, 80f, 60f, 120f, 80f)
    val tableWidth = columnWidths.sum() - 5f
    val cornerRadius = 4f
    val outlineOffset = 5f

    val tableX = (width - tableWidth) / 2
    val tableY = (height - tableHeight) / 2

    val tableRect = Rect.makeXYWH(tableX, tableY, tableWidth, tableHeight)
    val outlineRect = tableRect.inflate(outlineOffset)
    val outlineRRect = outlineRect.toRRect(cornerRadius + outlineOffset)
    val outlinePaint = Paint().apply {
        color = Color.WHITE
        mode = PaintMode.STROKE
        strokeWidth = 1f
    }
    canvas.drawRRect(outlineRRect, outlinePaint)

    val headerRect = Rect.makeXYWH(tableX, tableY, tableWidth, rowHeight)
    val headerRRect = headerRect.toRRect(cornerRadius)
    canvas.drawRectShadowAntiAlias(headerRRect, 1f, 1f, 2f, 1f, Color.makeRGB(0, 0, 0))
    val headerPaint = Paint().apply {
        shader = Shader.makeLinearGradient(
            tableX, tableY, tableX, tableY + rowHeight,
            intArrayOf(Color.makeARGB(100, 90, 90, 90), Color.makeARGB(100, 70, 70, 70)),
            null,
            GradientStyle(tileMode = FilterTileMode.CLAMP, isPremul = true, localMatrix = null)
        )
    }
    canvas.drawRRect(headerRRect, headerPaint)

    var x = tableX
    headers.forEachIndexed { index, header ->
        val textLine = TextLine.make(header, font)
        val textWidth = textLine.width
        val textX = x + (columnWidths[index] - 5f - textWidth) / 2
        val textY = tableY + rowHeight / 2 - (font.metrics.ascent + font.metrics.descent) / 2
        canvas.drawTextLine(textLine, textX, textY, textPaint)
        x += columnWidths[index]
    }

    gameState.guesses.forEachIndexed { index, (_, player) ->
        val y = tableY + rowHeight + rowSpacing * 2 + index * (rowHeight + rowSpacing)
        x = tableX

        val fields = listOf(
            player.name,
            player.team,
            player.nationality,
            player.age.toString(),
            player.position,
            "unknown"
        )

        fields.forEachIndexed { fieldIndex, field ->
            val cellRect = Rect.makeXYWH(x, y - rowHeight / 2, columnWidths[fieldIndex] - 5f, rowHeight)
            val cellRRect = cellRect.toRRect(cornerRadius)
            canvas.drawRectShadowAntiAlias(cellRRect, 1f, 1f, 2f, 1f, Color.makeRGB(0, 0, 0))

            val cellPaint = Paint().apply {
                shader = when (fieldIndex) {
                    0 -> {
                        if (player.name == gameState.targetPlayer.name) {
                            Shader.makeLinearGradient(
                                x, y - rowHeight / 2, x, y + rowHeight / 2,
                                intArrayOf(Color.makeARGB(100, 0, 180, 0), Color.makeARGB(100, 0, 140, 0)),
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
                    1 -> {
                        if (player.team == gameState.targetPlayer.team) {
                            Shader.makeLinearGradient(
                                x, y - rowHeight / 2, x, y + rowHeight / 2,
                                intArrayOf(Color.makeARGB(100, 0, 180, 0), Color.makeARGB(100, 0, 140, 0)),
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
                    2 -> {
                        val guessedContinent = CountryUtils.countryContinents[CountryUtils.countryToCode[player.nationality]?.lowercase() ?: player.nationality.lowercase()] ?: "Unknown"
                        val targetContinent = CountryUtils.countryContinents[CountryUtils.countryToCode[gameState.targetPlayer.nationality]?.lowercase() ?: gameState.targetPlayer.nationality.lowercase()] ?: "Unknown"
                        when {
                            player.nationality == gameState.targetPlayer.nationality -> Shader.makeLinearGradient(
                                x, y - rowHeight / 2, x, y + rowHeight / 2,
                                intArrayOf(Color.makeARGB(100, 0, 180, 0), Color.makeARGB(100, 0, 140, 0)),
                                null,
                                GradientStyle(tileMode = FilterTileMode.CLAMP, isPremul = true, localMatrix = null)
                            )
                            guessedContinent == targetContinent && guessedContinent != "Unknown" -> Shader.makeLinearGradient(
                                x, y - rowHeight / 2, x, y + rowHeight / 2,
                                intArrayOf(Color.makeARGB(100, 255, 255, 0), Color.makeARGB(100, 200, 200, 0)),
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
                    3 -> {
                        val guessedAge = player.age
                        val targetAge = gameState.targetPlayer.age
                        when {
                            guessedAge == targetAge -> Shader.makeLinearGradient(
                                x, y - rowHeight / 2, x, y + rowHeight / 2,
                                intArrayOf(Color.makeARGB(100, 0, 180, 0), Color.makeARGB(100, 0, 140, 0)),
                                null,
                                GradientStyle(tileMode = FilterTileMode.CLAMP, isPremul = true, localMatrix = null)
                            )
                            Math.abs(guessedAge - targetAge) <= 2 -> Shader.makeLinearGradient(
                                x, y - rowHeight / 2, x, y + rowHeight / 2,
                                intArrayOf(Color.makeARGB(100, 255, 255, 0), Color.makeARGB(100, 200, 200, 0)),
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
                    4 -> {
                        if (player.position == gameState.targetPlayer.position) {
                            Shader.makeLinearGradient(
                                x, y - rowHeight / 2, x, y + rowHeight / 2,
                                intArrayOf(Color.makeARGB(100, 0, 180, 0), Color.makeARGB(100, 0, 140, 0)),
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
                    5 -> {
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

            if (fieldIndex == 2) {
                try {
                    val svg = CountryUtils.loadSVGFromFile(GuessCS2ProPlayer.dataFolder, player.nationality)
                    val flagImage = svg.makeImage(24f, 24f)
                    val flagRect = Rect.makeXYWH(x + (columnWidths[fieldIndex] - 5f - 24f) / 2, y - 12f, 24f, 24f).toRRect(4f)
                    canvas.drawImageRRect(flagImage, flagRect)
                    GuessCS2ProPlayer.logger.info("Successfully rendered flag for nationality: ${player.nationality}")
                } catch (e: Exception) {
                    GuessCS2ProPlayer.logger.error("Failed to render flag for nationality: ${player.nationality}, error: ${e.message}", e)
                    val displayText = CountryUtils.countryToCode[player.nationality] ?: player.nationality
                    val textLine = TextLine.make(displayText, font)
                    val textWidth = textLine.width
                    val textX = x + (columnWidths[fieldIndex] - 5f - textWidth) / 2
                    val textY = y - (font.metrics.ascent + font.metrics.descent) / 2
                    canvas.drawTextLine(textLine, textX, textY, textPaint)
                }
            } else if (fieldIndex == 3) {
                val ageText = when {
                    player.age == gameState.targetPlayer.age -> player.age.toString()
                    player.age < gameState.targetPlayer.age -> "${player.age} ↑"
                    else -> "${player.age} ↓"
                }
                val textLine = TextLine.make(ageText, font)
                val textWidth = textLine.width
                val textX = x + (columnWidths[fieldIndex] - 5f - textWidth) / 2
                val textY = y - (font.metrics.ascent + font.metrics.descent) / 2
                canvas.drawTextLine(textLine, textX, textY, textPaint)
            } else {
                val textLine = TextLine.make(field, font)
                val textWidth = textLine.width
                val textX = x + (columnWidths[fieldIndex] - 5f - textWidth) / 2
                val textY = y - (font.metrics.ascent + font.metrics.descent) / 2
                canvas.drawTextLine(textLine, textX, textY, textPaint)
            }

            x += columnWidths[fieldIndex]
        }
    }

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