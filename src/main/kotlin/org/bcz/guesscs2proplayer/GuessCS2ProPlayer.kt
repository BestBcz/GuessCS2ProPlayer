package org.bcz.guesscs2proplayer

import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.buildMessageChain
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import org.jetbrains.skia.Data
import org.jetbrains.skia.Image
import org.jetbrains.skia.Surface
import org.jetbrains.skia.svg.SVGDOM
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import kotlin.random.Random




object GuessCS2ProPlayer : KotlinPlugin(
    JvmPluginDescription(
        id = "org.bcz.guesscs2proplayer",
        name = "CS2猜职业哥小游戏",
        version = "0.0.1"
    ) {
        author("Bcz")
        dependsOn("xyz.cssxsh.mirai.plugin.mirai-skia-plugin", ">= 1.1.0", false)
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

        // 加载 CSV 文件
        logger.info("Checking for players.csv in ${dataFolder.path}")
        val csvFile = File(dataFolder, "players.csv")
        if (!csvFile.exists()) {
            logger.error("players.csv not found in ${dataFolder.path}")
            return
        }

        logger.info("Loading players from CSV")
        try {
            players = loadPlayersFromCsv(csvFile)
            logger.info("Step 5: Loaded ${players.size} players from CSV")
        } catch (e: Exception) {
            logger.error("Step 6: Failed to load players from CSV: ${e.message}", e)
            return
        }

        // 注册命令
        CommandManager.INSTANCE.registerCommand(StartGameCommand, true)
        CommandManager.INSTANCE.registerCommand(StopGameCommand, true)

        // 监听群消息
        globalEventChannel().subscribeAlways<GroupMessageEvent> { event ->
            logger.info("Step 10: Received group message: ${event.message.contentToString()}")
            val groupId = group.id
            val message = event.message.contentToString().trim()
            val senderName = sender.nick

            if (hasGameState(groupId)) {

                val gameState = getGameState(groupId)!!
                val guessedPlayer = findPlayer(message)

                if (guessedPlayer == null) {
                    logger.info("Player not found: $message")
                    return@subscribeAlways
                }

                logger.info("Step 13: Player found: ${guessedPlayer.name}, processing guess")
                gameState.guesses.add(Pair(senderName, guessedPlayer))
                gameState.guessesLeft--

                val tempFile = try {
                    logger.info("Step 14: Drawing guess table")
                    drawGuessTable(gameState)
                } catch (e: Exception) {
                    logger.error("Failed to draw table: ${e.message}", e)
                    group.sendMessage("生成表格失败，请稍后重试。")
                    return@subscribeAlways
                }

                val imageMessage = try {
                    if (!tempFile.exists() || tempFile.length() == 0L) {
                        throw IllegalStateException("Temporary file is empty or does not exist: ${tempFile.absolutePath}")
                    }
                    logger.info("Temporary image saved to ${tempFile.absolutePath}, size: ${tempFile.length()} bytes")

                    val uploadedImage = tempFile.toExternalResource().use { resource ->
                        group.uploadImage(resource)
                    }

                    tempFile.delete()
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
                    } else if (gameState.guessesLeft == 0) {
                        +PlainText("游戏结束！正确选手为 ${gameState.targetPlayer.name}")
                        removeGameState(groupId)
                    } else {
                        +PlainText("${senderName} 猜测了 ${guessedPlayer.name}，剩余 ${gameState.guessesLeft} 次猜测机会。")
                    }
                })
            }
        }
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
    // 队伍的地区映射
    val teamRegions = mapOf(
        "Team Falcons" to "Middle East",
        "Eternal Fire" to "Middle East",
        "Vitality" to "Europe",
        "Free Agent" to "N/A",
        "Spirit" to "Europe",
        "Natus Vincere" to "Europe",
        "Astralis" to "Europe",
        "FaZe Clan" to "Europe"
    )

    // 国家的洲际映射
    val countryContinents = mapOf(
        "ba" to "Europe", // Bosnia (for NiKo)
        "tr" to "Asia",   // Turkey (for XANTARES)
        "il" to "Asia",   // Israel (for flameZ)
        "ru" to "Europe", // Russia (for flamie, chopper)
        "ua" to "Europe", // Ukraine (for s1mple)
        "dk" to "Europe", // Denmark (for device)
        "sk" to "Europe", // Slovakia (for GuardiaN)
        "pl" to "Europe", // Poland (for pasha)
        "be" to "Europe", // Belgium (for Ex6TenZ)
        "nl" to "Europe", // Netherlands (for chrisJ)
        "br" to "South America", // Brazil (for fnx, bit)
        "de" to "Europe"  // Denmark/Germany (for karrigan)
    )

    // 国家名称到缩写的映射
    val countryToCode = mapOf(
        "Bosnia and Herzegovina" to "ba",
        "Turkey" to "tr",
        "Israel" to "il",
        "Russia" to "ru",
        "Ukraine" to "ua",
        "Denmark" to "dk",
        "Slovakia" to "sk",
        "Poland" to "pl",
        "Belgium" to "be",
        "Netherlands" to "nl",
        "Brazil" to "br",
        "Germany" to "de",
        // 根据你的 CSV 文件添加更多映射
        "United States" to "us",
        "France" to "fr",
        "Sweden" to "se",
        "Norway" to "no",
        "Finland" to "fi",
        "Australia" to "au",
        "Canada" to "ca",
        "China" to "cn",
        "Japan" to "jp",
        "South Korea" to "kr"
    )

    // 角色的类别映射
    val roleCategories = mapOf(
        "Rifler" to "Rifler",
        "AWPer" to "AWPer",
        "Coach" to "Support",
        "IGL" to "Support"
    )


    // 从插件的 data/flags 目录加载国旗 SVG 文件
    fun loadSVGFromFile(nationality: String): SVGDOM {
        val flagsDir = File(dataFolder, "flags")
        logger.info("Attempting to load flag for nationality: $nationality")
        logger.info("Flags directory: ${flagsDir.absolutePath}")

        // 将完整国家名称转换为缩写
        val countryCode = countryToCode[nationality] ?: run {
            logger.warning("No country code mapping found for nationality: $nationality, using lowercase as fallback")
            nationality.lowercase().replace(" ", "_")
        }
        logger.info("Mapped country code: $countryCode")

        if (!flagsDir.exists()) {
            logger.warning("Flags directory does not exist, creating: ${flagsDir.absolutePath}")
            flagsDir.mkdirs() // 如果目录不存在，创建目录
        }

        val svgFile = File(flagsDir, "$countryCode.svg")
        logger.info("Looking for SVG file: ${svgFile.absolutePath}")

        if (!svgFile.exists()) {
            logger.error("SVG file not found: ${svgFile.absolutePath}")
            throw IllegalStateException("SVG file not found: ${svgFile.absolutePath}")
        }

        if (!svgFile.canRead()) {
            logger.error("Cannot read SVG file: ${svgFile.absolutePath}")
            throw IllegalStateException("Cannot read SVG file: ${svgFile.absolutePath}")
        }

        try {
            val bytes = svgFile.readBytes()
            logger.info("Successfully read SVG file: ${svgFile.absolutePath}, size: ${bytes.size} bytes")
            return SVGDOM(Data.makeFromBytes(bytes))
        } catch (e: Exception) {
            logger.error("Failed to parse SVG file: ${svgFile.absolutePath}, error: ${e.message}", e)
            throw e
        }
    }

    fun SVGDOM.makeImage(width: Float, height: Float): Image {
        setContainerSize(width, height)
        return Surface.makeRasterN32Premul(width.toInt(), height.toInt()).apply { render(canvas) }.makeImageSnapshot()
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
}



