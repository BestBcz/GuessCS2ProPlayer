package org.bcz.guesscs2proplayer

import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.message.data.Image as MiraiImage
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.buildMessageChain
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import java.awt.Color
import java.awt.Font
import java.awt.GradientPaint
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import javax.imageio.ImageIO
import kotlin.random.Random

data class Player(
    val name: String,
    val team: String,
    val nationality: String,
    val age: Int,
    val position: String,
    val majorAppearances: Int // 新增 Major 出场次数
)

data class GameState(
    val groupId: Long,
    val targetPlayer: Player,
    var guessesLeft: Int = 10,
    val guesses: MutableList<Pair<String, Player>> = mutableListOf()
)

object GuessCS2ProPlayer : KotlinPlugin(
    JvmPluginDescription(
        id = "org.bcz.guesscs2proplayer",
        name = "CS2猜职业哥小游戏",
        version = "0.0.1"
    ) {
        author("Bcz")
    }
) {
    // 玩家数据
    private lateinit var players: List<Player>

    // 游戏状态
    private val gameStates = mutableMapOf<Long, GameState>()

    // 提供公共方法来访问和操作 gameStates
    fun hasGameState(groupId: Long): Boolean = gameStates.containsKey(groupId)

    fun getGameState(groupId: Long): GameState? = gameStates[groupId]

    fun startGame(groupId: Long, gameState: GameState) {
        gameStates[groupId] = gameState
    }

    fun removeGameState(groupId: Long) {
        gameStates.remove(groupId)
    }

    // 提供公共方法来访问 players
    fun getRandomPlayer(): Player {
        if (!::players.isInitialized || players.isEmpty()) {
            throw IllegalStateException("Players list is not initialized or empty")
        }
        return players[Random.nextInt(players.size)]
    }

    fun findPlayer(name: String): Player? {
        if (!::players.isInitialized) {
            throw IllegalStateException("Players list is not initialized")
        }
        return players.find { it.name.equals(name, ignoreCase = true) }
    }

    override fun onEnable() {
        logger.info("Guess CS2 Pro Player Plugin Enabled - Step 1: Starting onEnable")

        // 加载 CSV 文件
        logger.info("Step 2: Checking for players.csv in ${dataFolder.path}")
        val csvFile = File(dataFolder, "players.csv")
        if (!csvFile.exists()) {
            logger.error("Step 3: players.csv not found in ${dataFolder.path}")
            return
        }

        logger.info("Step 4: Loading players from CSV")
        try {
            players = loadPlayersFromCsv(csvFile)
            logger.info("Step 5: Loaded ${players.size} players from CSV")
        } catch (e: Exception) {
            logger.error("Step 6: Failed to load players from CSV: ${e.message}", e)
            return
        }

        // 注册命令
        logger.info("Step 7: Registering commands")
        CommandManager.INSTANCE.registerCommand(StartGameCommand, true)
        CommandManager.INSTANCE.registerCommand(StopGameCommand, true)
        logger.info("Step 8: Commands registered successfully")

        // 监听群消息
        logger.info("Step 9: Setting up group message listener")
        globalEventChannel().subscribeAlways<GroupMessageEvent> { event ->
            logger.info("Step 10: Received group message: ${event.message.contentToString()}")
            val groupId = group.id
            val message = event.message.contentToString().trim()
            val senderName = sender.nick

            if (hasGameState(groupId)) {
                logger.info("Step 11: Game in progress for group $groupId, processing guess: $message")
                val gameState = getGameState(groupId)!!
                val guessedPlayer = findPlayer(message)

                if (guessedPlayer == null) {
                    logger.info("Step 12: Player not found for guess: $message")
                    return@subscribeAlways
                }

                logger.info("Step 13: Player found: ${guessedPlayer.name}, processing guess")
                gameState.guesses.add(Pair(senderName, guessedPlayer))
                gameState.guessesLeft--

                val tempFile = try {
                    logger.info("Step 14: Drawing guess table")
                    drawGuessTable(gameState)
                } catch (e: Exception) {
                    logger.error("Step 15: Failed to draw table: ${e.message}", e)
                    group.sendMessage("生成表格失败，请稍后重试。")
                    return@subscribeAlways
                }

                val imageMessage = try {
                    if (!tempFile.exists() || tempFile.length() == 0L) {
                        throw IllegalStateException("Temporary file is empty or does not exist: ${tempFile.absolutePath}")
                    }
                    logger.info("Step 16: Temporary image saved to ${tempFile.absolutePath}, size: ${tempFile.length()} bytes")

                    val uploadedImage = tempFile.toExternalResource().use { resource ->
                        group.uploadImage(resource)
                    }

                    tempFile.delete()
                    logger.info("Step 17: Image uploaded successfully")
                    uploadedImage
                } catch (e: Exception) {
                    logger.error("Step 18: Failed to upload image: ${e.message}", e)
                    group.sendMessage("图片上传失败，请稍后重试。")
                    return@subscribeAlways
                }

                group.sendMessage(buildMessageChain {
                    +imageMessage
                    +PlainText("\n")
                    if (guessedPlayer.name == gameState.targetPlayer.name) {
                        +PlainText("恭喜！${senderName} 猜对了选手：${gameState.targetPlayer.name}")
                        removeGameState(groupId)
                        logger.info("Step 19: Game ended - Correct guess")
                    } else if (gameState.guessesLeft == 0) {
                        +PlainText("游戏结束！正确选手为 ${gameState.targetPlayer.name}")
                        removeGameState(groupId)
                        logger.info("Step 20: Game ended - No guesses left")
                    } else {
                        +PlainText("${senderName} 猜测了 ${guessedPlayer.name}，剩余 ${gameState.guessesLeft} 次猜测机会。")
                        logger.info("Step 21: Guess processed, ${gameState.guessesLeft} guesses left")
                    }
                })
            }
        }
        logger.info("Step 22: Group message listener setup complete")
    }

    // 从 CSV 文件加载玩家数据
    private fun loadPlayersFromCsv(file: File): List<Player> {
        val players = mutableListOf<Player>()
        // 使用 UTF-8 编码读取文件
        BufferedReader(FileReader(file, Charsets.UTF_8)).use { reader ->
            // 跳过表头
            reader.readLine()
            // 逐行读取
            reader.forEachLine { line ->
                val columns = line.split(",").map { it.trim() }
                if (columns.size == 5) {
                    try {
                        val player = Player(
                            name = columns[0],
                            team = columns[1],
                            nationality = columns[2],
                            age = columns[3].toInt(),
                            position = columns[4],
                            majorAppearances = Random.nextInt(1, 20) // 随机生成 Major 出场次数
                        )
                        players.add(player)
                    } catch (e: Exception) {
                        logger.error("Failed to parse line: $line, error: ${e.message}", e)
                    }
                }
            }
        }
        return players
    }
    // 绘制表格并保存为文件（使用 BufferedImage）
    fun drawGuessTable(gameState: GameState): File {
        val width = 700
        val height = 60 + gameState.guesses.size * 40 // 动态高度

        // 创建 BufferedImage
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val g2d = image.createGraphics()

        // 背景（深紫色渐变）
        val gradient = GradientPaint(
            0f, 0f, Color(30, 20, 40),
            0f, height.toFloat(), Color(50, 40, 60)
        )
        g2d.paint = gradient
        g2d.fillRect(0, 0, width, height)

        // 字体
        val font = Font("Default", Font.PLAIN, 14)
        val boldFont = Font("Default", Font.BOLD, 14)
        g2d.font = font

        // 绘制表头
        val headers = listOf("NAME", "TEAM", "NAT", "AGE", "ROLE", "MAJ APP")
        val columnWidths = listOf(100, 120, 100, 60, 100, 80)
        var x = 10f
        g2d.font = boldFont
        headers.forEachIndexed { index, header ->
            g2d.paint = GradientPaint(
                x, 0f, Color.WHITE,
                x, 30f, Color(200, 200, 200)
            )
            g2d.drawString(header, x + 5, 30f)
            x += columnWidths[index]
        }

        // 绘制表格内容
        g2d.font = font
        gameState.guesses.forEachIndexed { index, (guesser, player) ->
            val y = 50f + index * 40f
            x = 10f

            // 行背景
            val rowColor = if (player.name == gameState.targetPlayer.name) {
                GradientPaint(
                    0f, y - 15, Color(0, 150, 0),
                    0f, y + 15, Color(0, 100, 0)
                )
            } else {
                GradientPaint(
                    0f, y - 15, Color(40, 30, 50),
                    0f, y + 15, Color(60, 50, 70)
                )
            }
            g2d.paint = rowColor
            g2d.fillRect(10, (y - 15).toInt(), width - 20, 30)

            // NAME
            g2d.color = Color.WHITE
            g2d.drawString(player.name, x + 5, y)
            x += columnWidths[0]

            // TEAM
            val teamColor = if (player.team == gameState.targetPlayer.team) Color.GREEN else Color.WHITE
            g2d.color = teamColor
            g2d.drawString(player.team, x + 5, y)
            x += columnWidths[1]

            // NAT
            val natColor = if (player.nationality == gameState.targetPlayer.nationality) Color.GREEN else Color.WHITE
            g2d.color = natColor
            g2d.drawString(player.nationality, x + 5, y)
            x += columnWidths[2]

            // AGE
            val ageColor = if (player.age == gameState.targetPlayer.age) Color.GREEN else Color.WHITE
            val ageText = when {
                player.age == gameState.targetPlayer.age -> player.age.toString()
                player.age < gameState.targetPlayer.age -> "${player.age} ↑"
                else -> "${player.age} ↓"
            }
            g2d.color = ageColor
            g2d.drawString(ageText, x + 5, y)
            x += columnWidths[3]

            // ROLE
            val roleColor = if (player.position == gameState.targetPlayer.position) Color.GREEN else Color.WHITE
            g2d.color = roleColor
            g2d.drawString(player.position, x + 5, y)
            x += columnWidths[4]

            // MAJ APP
            val majColor = if (player.majorAppearances == gameState.targetPlayer.majorAppearances) Color.GREEN else Color.WHITE
            val majText = when {
                player.majorAppearances == gameState.targetPlayer.majorAppearances -> player.majorAppearances.toString()
                player.majorAppearances < gameState.targetPlayer.majorAppearances -> "${player.majorAppearances} ↑"
                else -> "${player.majorAppearances} ↓"
            }
            g2d.color = majColor
            g2d.drawString(majText, x + 5, y)
        }

        // 释放 Graphics2D 资源
        g2d.dispose()

        // 保存 BufferedImage 到临时文件
        val tempFile = File.createTempFile("guesscs2player", ".png")
        ImageIO.write(image, "png", tempFile)

        return tempFile
    }
}

// 命令：开始猜选手
object StartGameCommand : SimpleCommand(
    GuessCS2ProPlayer,
    primaryName = "开始猜选手",
    description = "开始 CS2 猜职业哥游戏"
) {
    @Handler
    suspend fun CommandSender.handle() {
        val group = this.subject as? Group ?: run {
            sendMessage("此命令只能在群聊中使用")
            return
        }
        val groupId = group.id

        if (GuessCS2ProPlayer.hasGameState(groupId)) {
            sendMessage("群内已有进行中的游戏，请先完成。")
            return
        }

        val targetPlayer = GuessCS2ProPlayer.getRandomPlayer()
        GuessCS2ProPlayer.startGame(groupId, GameState(groupId, targetPlayer))
        sendMessage("游戏开始！群内成员可以直接发送选手名字进行猜测（例如：s1mple），共有 10 次机会。")
    }
}

// 命令：结束猜选手
object StopGameCommand : SimpleCommand(
    GuessCS2ProPlayer,
    primaryName = "结束猜选手",
    description = "结束 CS2 猜职业哥游戏"
) {
    @Handler
    suspend fun CommandSender.handle() {
        val group = this.subject as? Group ?: run {
            sendMessage("此命令只能在群聊中使用")
            return
        }
        val groupId = group.id

        if (!GuessCS2ProPlayer.hasGameState(groupId)) {
            sendMessage("当前没有进行中的游戏。")
            return
        }

        val gameState = GuessCS2ProPlayer.getGameState(groupId)!!
        sendMessage("游戏已结束！正确选手为 ${gameState.targetPlayer.name}")
        GuessCS2ProPlayer.removeGameState(groupId)
    }
}