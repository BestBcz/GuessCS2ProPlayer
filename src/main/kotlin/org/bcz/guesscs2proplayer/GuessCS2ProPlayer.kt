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
import net.mamoe.mirai.message.data.MessageChain




object GuessCS2ProPlayer : KotlinPlugin(
    JvmPluginDescription(
        id = "org.bcz.guesscs2proplayer",
        name = "CS2猜职业哥小游戏",
        version = "0.0.4"
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

        // 规范化输入：移除空格、特殊字符，转换为小写
        val normalizedInput = name.lowercase().replace(Regex("[^a-z0-9]"), "")
        // 将 "i" 替换为 "1" 和 "1" 替换为 "i"，生成可能的变体
        val inputWithI = normalizedInput.replace("1", "i")
        val inputWith1 = normalizedInput.replace("i", "1")
        // 将 "o" 替换为 "0" 和 "0" 替换为 "o"，生成可能的变体
        val inputWithO = inputWithI.replace("0", "o")
        val inputWith0 = inputWith1.replace("o", "0")

        return players.find { player ->
            // 规范化选手名字：移除空格、特殊字符，转换为小写
            val normalizedPlayerName = player.name.lowercase().replace(Regex("[^a-z0-9]"), "")
            // 将选手名字中的 "i" 替换为 "1" 和 "1" 替换为 "i"，生成可能的变体
            val playerNameWithI = normalizedPlayerName.replace("1", "i")
            val playerNameWith1 = normalizedPlayerName.replace("i", "1")
            // 将选手名字中的 "o" 替换为 "0" 和 "0" 替换为 "o"，生成可能的变体
            val playerNameWithO = playerNameWithI.replace("0", "o")
            val playerNameWith0 = playerNameWith1.replace("o", "0")

            // 匹配条件：
            // 1. 完全匹配（忽略大小写、空格和特殊字符）
            // 2. "i" 和 "1" 替换后的匹配
            // 3. "o" 和 "0" 替换后的匹配
            normalizedPlayerName == normalizedInput ||
                    playerNameWithI == inputWithI ||
                    playerNameWith1 == inputWith1 ||
                    playerNameWithO == inputWithO ||
                    playerNameWith0 == inputWith0
        }
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
            val group = event.group

            if (hasGameState(groupId)) {
                // 存储消息链、临时文件和是否需要发送消息
                var messageChain: MessageChain? = null
                var tempFile: File? = null
                var shouldSendMessage = true

                // 使用 groupId 作为锁对象，确保同一群的猜测按顺序处理
                synchronized(groupId) {
                    // 再次检查游戏状态，确保游戏未结束
                    if (!hasGameState(groupId)) {
                        logger.info("Game has already ended for group $groupId, ignoring guess: $message")
                        shouldSendMessage = false
                        return@subscribeAlways
                    }

                    val gameState = getGameState(groupId)!!
                    val guessedPlayer = findPlayer(message)

                    if (guessedPlayer == null) {
                        logger.info("Player not found: $message")
                        messageChain = buildMessageChain {
                            +PlainText("未找到选手：$message")
                        }
                        return@subscribeAlways
                    }

                    // 检查是否重复猜测
                    val isDuplicateGuess = gameState.guesses.any { it.second.name.equals(guessedPlayer.name, ignoreCase = true) }
                    if (isDuplicateGuess) {
                        logger.info("Duplicate guess detected: ${guessedPlayer.name}")
                        messageChain = buildMessageChain {
                            +PlainText("该选手已被猜测过，请尝试其他选手！")
                        }
                        return@subscribeAlways
                    }

                    logger.info("Step 13: Player found: ${guessedPlayer.name}, processing guess")
                    gameState.guesses.add(Pair(senderName, guessedPlayer))
                    gameState.guessesLeft--

                    // 生成临时文件，但不上传
                    tempFile = try {
                        logger.info("Step 14: Drawing guess table")
                        drawGuessTable(gameState)
                    } catch (e: Exception) {
                        logger.error("Failed to draw table: ${e.message}", e)
                        messageChain = buildMessageChain {
                            +PlainText("生成表格失败，请稍后重试。")
                        }
                        return@subscribeAlways
                    }

                    // 构建消息链，但不发送
                    messageChain = buildMessageChain {
                        if (guessedPlayer.name == gameState.targetPlayer.name) {
                            +PlainText("恭喜！${senderName} 猜对了选手：${gameState.targetPlayer.name}")
                            removeGameState(groupId)
                        } else if (gameState.guessesLeft == 0) {
                            +PlainText("游戏结束！正确选手为 ${gameState.targetPlayer.name}")
                            removeGameState(groupId)
                        } else {
                            +PlainText("${senderName} 猜测了 ${guessedPlayer.name}，剩余 ${gameState.guessesLeft} 次猜测机会。")
                        }
                    }
                }

                // 在 synchronized 块外执行 uploadImage 和 sendMessage
                if (shouldSendMessage) {
                    if (tempFile != null) {
                        val imageMessage = try {
                            if (!tempFile!!.exists() || tempFile!!.length() == 0L) {
                                throw IllegalStateException("Temporary file is empty or does not exist: ${tempFile!!.absolutePath}")
                            }
                            logger.info("Temporary image saved to ${tempFile!!.absolutePath}, size: ${tempFile!!.length()} bytes")

                            val uploadedImage = tempFile!!.toExternalResource().use { resource ->
                                group.uploadImage(resource)
                            }

                            tempFile!!.delete()
                            uploadedImage
                        } catch (e: Exception) {
                            logger.error("Step 18: Failed to upload image: ${e.message}", e)
                            group.sendMessage("图片上传失败，请稍后重试。")
                            return@subscribeAlways
                        }

                        // 将图片添加到消息链
                        messageChain = buildMessageChain {
                            +imageMessage
                            +PlainText("\n")
                            +messageChain!!
                        }
                    }

                    // 发送消息
                    if (messageChain != null) {
                        group.sendMessage(messageChain!!)
                    }
                }
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
    // 国家的洲际映射
    val countryContinents = mapOf(
        // 欧洲
        "ba" to "Europe", // Bosnia and Herzegovina
        "dk" to "Europe", // Denmark
        "sk" to "Europe", // Slovakia
        "pl" to "Europe", // Poland
        "be" to "Europe", // Belgium
        "nl" to "Europe", // Netherlands
        "de" to "Europe", // Germany
        "fr" to "Europe", // France
        "se" to "Europe", // Sweden
        "no" to "Europe", // Norway
        "fi" to "Europe", // Finland
        "gb" to "Europe", // United Kingdom
        "es" to "Europe", // Spain
        "it" to "Europe", // Italy
        "pt" to "Europe", // Portugal
        "at" to "Europe", // Austria
        "ch" to "Europe", // Switzerland
        "cz" to "Europe", // Czech Republic
        "hu" to "Europe", // Hungary
        "ro" to "Europe", // Romania
        "bg" to "Europe", // Bulgaria
        "rs" to "Europe", // Serbia
        "hr" to "Europe", // Croatia
        "si" to "Europe", // Slovenia
        "mk" to "Europe", // North Macedonia
        "al" to "Europe", // Albania
        "me" to "Europe", // Montenegro
        "ee" to "Europe", // Estonia
        "lv" to "Europe", // Latvia
        "lt" to "Europe", // Lithuania
        "ie" to "Europe", // Ireland
        "is" to "Europe", // Iceland
        "gr" to "Europe", // Greece
        "cy" to "Europe", // Cyprus

        // 独联体 (CIS)
        "ru" to "CIS", // Russia
        "ua" to "CIS", // Ukraine
        "by" to "CIS", // Belarus
        "kz" to "CIS", // Kazakhstan
        "uz" to "CIS", // Uzbekistan
        "tm" to "CIS", // Turkmenistan
        "kg" to "CIS", // Kyrgyzstan
        "tj" to "CIS", // Tajikistan
        "am" to "CIS", // Armenia
        "az" to "CIS", // Azerbaijan
        "ge" to "CIS", // Georgia
        "md" to "CIS", // Moldova

        // 北美洲
        "us" to "North America", // United States
        "ca" to "North America", // Canada
        "mx" to "North America", // Mexico
        "gt" to "North America", // Guatemala

        // 南美洲
        "br" to "South America", // Brazil
        "ar" to "South America", // Argentina
        "cl" to "South America", // Chile
        "pe" to "South America", // Peru
        "co" to "South America", // Colombia
        "ve" to "South America", // Venezuela
        "bo" to "South America", // Bolivia
        "py" to "South America", // Paraguay
        "uy" to "South America", // Uruguay
        "ec" to "South America", // Ecuador
        "gy" to "South America", // Guyana
        "sr" to "South America", // Suriname

        // 亚洲（包含中东和大洋洲）
        "tr" to "Asia", // Turkey
        "il" to "Asia", // Israel
        "cn" to "Asia", // China
        "jp" to "Asia", // Japan
        "kr" to "Asia", // South Korea
        "au" to "Asia", // Australia (归为大洋洲，但按需求归入亚洲)
        "nz" to "Asia", // New Zealand (归为大洋洲，但按需求归入亚洲)
        "in" to "Asia", // India
        "id" to "Asia", // Indonesia
        "ph" to "Asia", // Philippines
        "th" to "Asia", // Thailand
        "vn" to "Asia", // Vietnam
        "my" to "Asia", // Malaysia
        "sg" to "Asia", // Singapore
        "sa" to "Asia", // Saudi Arabia (中东)
        "ae" to "Asia", // United Arab Emirates (中东)
        "qa" to "Asia", // Qatar (中东)
        "kw" to "Asia", // Kuwait (中东)
        "bh" to "Asia", // Bahrain (中东)
        "om" to "Asia", // Oman (中东)
        "ye" to "Asia", // Yemen (中东)
        "jo" to "Asia", // Jordan (中东)
        "lb" to "Asia", // Lebanon (中东)
        "sy" to "Asia", // Syria (中东)
        "iq" to "Asia", // Iraq (中东)
        "ir" to "Asia", // Iran (中东)
        "pk" to "Asia", // Pakistan
        "af" to "Asia", // Afghanistan
        "bd" to "Asia", // Bangladesh
        "lk" to "Asia", // Sri Lanka
        "np" to "Asia", // Nepal
        "bt" to "Asia", // Bhutan
        "mm" to "Asia", // Myanmar
        "kh" to "Asia", // Cambodia
        "la" to "Asia", // Laos
        "mn" to "Asia", // Mongolia
        "pg" to "Asia", // Papua New Guinea (大洋洲，但按需求归入亚洲)
        "fj" to "Asia"  // Fiji (大洋洲，但按需求归入亚洲)
    )


    // 国家名称到缩写的映射
    val countryToCode = mapOf(
        // 欧洲
        "Bosnia and Herzegovina" to "ba",
        "Denmark" to "dk",
        "Slovakia" to "sk",
        "Poland" to "pl",
        "Belgium" to "be",
        "Netherlands" to "nl",
        "Germany" to "de",
        "France" to "fr",
        "Sweden" to "se",
        "Norway" to "no",
        "Finland" to "fi",
        "United Kingdom" to "gb",
        "Spain" to "es",
        "Italy" to "it",
        "Portugal" to "pt",
        "Austria" to "at",
        "Switzerland" to "ch",
        "Czech Republic" to "cz",
        "Hungary" to "hu",
        "Romania" to "ro",
        "Bulgaria" to "bg",
        "Serbia" to "rs",
        "Croatia" to "hr",
        "Slovenia" to "si",
        "North Macedonia" to "mk",
        "Albania" to "al",
        "Montenegro" to "me",
        "Estonia" to "ee",
        "Latvia" to "lv",
        "Lithuania" to "lt",
        "Ireland" to "ie",
        "Iceland" to "is",
        "Greece" to "gr",
        "Cyprus" to "cy",

        // 独联体 (CIS)
        "Russia" to "ru",
        "Ukraine" to "ua",
        "Belarus" to "by",
        "Kazakhstan" to "kz",
        "Uzbekistan" to "uz",
        "Turkmenistan" to "tm",
        "Kyrgyzstan" to "kg",
        "Tajikistan" to "tj",
        "Armenia" to "am",
        "Azerbaijan" to "az",
        "Georgia" to "ge",
        "Moldova" to "md",

        // 北美洲
        "United States" to "us",
        "Canada" to "ca",
        "Mexico" to "mx",
        "Guatemala" to "gt", // 添加危地马拉

        // 南美洲
        "Brazil" to "br",
        "Argentina" to "ar",
        "Chile" to "cl",
        "Peru" to "pe",
        "Colombia" to "co",
        "Venezuela" to "ve",
        "Bolivia" to "bo",
        "Paraguay" to "py",
        "Uruguay" to "uy",
        "Ecuador" to "ec",
        "Guyana" to "gy",
        "Suriname" to "sr",

        // 亚洲（包含中东和大洋洲）
        "Turkey" to "tr",
        "Israel" to "il",
        "China" to "cn",
        "Japan" to "jp",
        "South Korea" to "kr",
        "Australia" to "au",
        "New Zealand" to "nz",
        "India" to "in",
        "Indonesia" to "id",
        "Philippines" to "ph",
        "Thailand" to "th",
        "Vietnam" to "vn",
        "Malaysia" to "my",
        "Singapore" to "sg",
        "Saudi Arabia" to "sa",
        "United Arab Emirates" to "ae",
        "Qatar" to "qa",
        "Kuwait" to "kw",
        "Bahrain" to "bh",
        "Oman" to "om",
        "Yemen" to "ye",
        "Jordan" to "jo",
        "Lebanon" to "lb",
        "Syria" to "sy",
        "Iraq" to "iq",
        "Iran" to "ir",
        "Pakistan" to "pk",
        "Afghanistan" to "af",
        "Bangladesh" to "bd",
        "Sri Lanka" to "lk",
        "Nepal" to "np",
        "Bhutan" to "bt",
        "Myanmar" to "mm",
        "Cambodia" to "kh",
        "Laos" to "la",
        "Mongolia" to "mn",
        "Papua New Guinea" to "pg",
        "Fiji" to "fj",

        // 非洲（如果有需要可以添加）
        "South Africa" to "za",
        "Nigeria" to "ng",
        "Egypt" to "eg",
        "Kenya" to "ke",
        "Ghana" to "gh",
        "Algeria" to "dz",
        "Morocco" to "ma",
        "Tunisia" to "tn",
        "Uganda" to "ug",
        "Ethiopia" to "et"
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



