package org.bcz.guesscs2proplayer

import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.buildMessageChain
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import org.bcz.guesscs2proplayer.commands.GameCommands
import org.bcz.guesscs2proplayer.commands.StartGameSimpleCommand
import org.bcz.guesscs2proplayer.GameStateManager
import org.bcz.guesscs2proplayer.PlayerManager
import java.time.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.fixedRateTimer

object GuessCS2ProPlayer : KotlinPlugin(
    JvmPluginDescription(
        id = "org.bcz.guesscs2proplayer",
        name = "CS2猜职业哥小游戏",
        version = "0.2.1"
    ) {
        author("Bcz")
        dependsOn("xyz.cssxsh.mirai.plugin.mirai-skia-plugin", ">= 1.1.0", false)
    }
) {
    override fun onEnable() {
        logger.info("Starting to load CS2 Guess Pro Player plugin...")

        // 初始化玩家管理器
        PlayerManager.initialize(dataFolder)
        if (!PlayerManager.isInitialized()) {
            logger.error("Failed to initialize PlayerManager, plugin will not function.")
            return
        }
        logger.info("PlayerManager initialized successfully.")

        // 初始化排行榜
        LeaderboardManager.initialize(dataFolder)
        logger.info("LeaderboardManager initialized successfully.")

        // 注册命令
        val registeredGameCommands = CommandManager.INSTANCE.registerCommand(GameCommands, true)
        if (registeredGameCommands) {
            logger.info("GameCommands registered successfully.")
        } else {
            logger.error("Failed to register GameCommands.")
        }

        val registeredStartGameCommand = CommandManager.INSTANCE.registerCommand(StartGameSimpleCommand, true)
        if (registeredStartGameCommand) {
            logger.info("StartGameSimpleCommand registered successfully.")
        } else {
            logger.error("Failed to register StartGameSimpleCommand.")
        }

        // 每周一凌晨重置排行榜
        val zoneId = ZoneId.systemDefault()
        val nextMondayMidnight = LocalDateTime.now(zoneId)
            .with(LocalTime.MIDNIGHT)
            .plusDays((7 - (LocalDate.now(zoneId).dayOfWeek.value % 7)).toLong())
        val initialDelay = Duration.between(LocalDateTime.now(zoneId), nextMondayMidnight)
            .toMillis()
        fixedRateTimer("ResetLeaderboard", false, initialDelay, TimeUnit.DAYS.toMillis(7)) {
            logger.info("Resetting weekly leaderboard...")
            LeaderboardManager.resetWeeklyStats()
            logger.info("Weekly leaderboard reset successfully.")
        }

        // 监听群消息
        globalEventChannel().subscribeAlways<net.mamoe.mirai.event.events.GroupMessageEvent> { event ->
            val groupId = group.id
            val message = event.message.contentToString().trim()
            val senderName = sender.nick
            val senderId = sender.id
            val group = event.group
            var duplicateGuessMessage: String? = null

            if (GameStateManager.hasGameState(groupId)) {
                var messageChain: net.mamoe.mirai.message.data.MessageChain? = null
                var tempFile: java.io.File? = null
                var shouldSendMessage = true

                synchronized(groupId) {
                    if (!GameStateManager.hasGameState(groupId)) {
                        shouldSendMessage = false
                        return@subscribeAlways
                    }

                    val gameState = GameStateManager.getGameState(groupId)!!
                    val guessedPlayer = PlayerManager.findPlayer(message)

                    if (guessedPlayer == null) {
                        logger.info("Player not found for guess: $message in group $groupId")
                        return@synchronized
                    }

                    val guessedNameNormalized = guessedPlayer.name.lowercase().replace(Regex("[^a-z0-9]"), "")
                    val isDuplicateGuess = gameState.guesses.any { (_, guessed) ->
                        guessedNameNormalized == guessed.name.lowercase().replace(Regex("[^a-z0-9]"), "")
                    }
                    if (isDuplicateGuess) {
                        duplicateGuessMessage = "该选手已被猜测过，请尝试其他选手！"
                        return@synchronized
                    }

                    gameState.guesses.add(Pair(senderName, guessedPlayer))
                    gameState.guessesLeft--

                    tempFile = try {
                        drawGuessTable(gameState)
                    } catch (e: Exception) {
                        messageChain = buildMessageChain {
                            +PlainText("生成表格失败，请稍后重试。")
                        }
                        return@synchronized
                    }

                    messageChain = buildMessageChain {
                        if (guessedPlayer.name == gameState.targetPlayer.name) {
                            +PlainText("恭喜！${senderName} 猜对了选手：${gameState.targetPlayer.name}")
                            // 记录分数
                            gameState.scores[senderId] = gameState.scores.getOrDefault(senderId, 0) + 1
                            // 检查是否胜利
                            val roundsToWin = gameState.gameMode.roundsToWin
                            val maxRounds = when (gameState.gameMode) {
                                GameMode.Default -> 1
                                GameMode.BO3 -> 3
                                GameMode.BO5 -> 5
                            }
                            if (gameState.scores[senderId]!! >= roundsToWin || gameState.currentRound >= maxRounds) {
                                +PlainText("\n游戏结束！${senderName} 获胜！")
                                LeaderboardManager.recordWin(senderId.toString())
                                gameState.guesses.forEach { (name, _) ->
                                    if (name != senderName) LeaderboardManager.recordLoss(senderId.toString())
                                }
                                GameStateManager.removeGameState(groupId)
                            } else {
                                // 下一局
                                val newTargetPlayer = PlayerManager.getRandomPlayer()
                                GameStateManager.nextRound(groupId, newTargetPlayer)
                                +PlainText("\n下一局开始！剩余 ${maxRounds - gameState.currentRound} 局。")
                            }
                        } else if (gameState.guessesLeft == 0) {
                            +PlainText("本局结束！正确选手为 ${gameState.targetPlayer.name}")
                            // 记录失败
                            gameState.guesses.forEach { (_, _) ->
                                LeaderboardManager.recordLoss(senderId.toString())
                            }
                            // 检查是否继续
                            val maxRounds = when (gameState.gameMode) {
                                GameMode.Default -> 1
                                GameMode.BO3 -> 3
                                GameMode.BO5 -> 5
                            }
                            if (gameState.currentRound >= maxRounds) {
                                +PlainText("\n游戏结束！无人获胜。")
                                GameStateManager.removeGameState(groupId)
                            } else {
                                // 下一局
                                val newTargetPlayer = PlayerManager.getRandomPlayer()
                                GameStateManager.nextRound(groupId, newTargetPlayer)
                                +PlainText("\n下一局开始！剩余 ${maxRounds - gameState.currentRound} 局。")
                            }
                        } else {
                            +PlainText("${senderName} 猜测了 ${guessedPlayer.name}，剩余 ${gameState.guessesLeft} 次猜测机会。")
                        }
                    }
                }

                // 处理重复猜测消息
                if (duplicateGuessMessage != null) {
                    try {
                        group.sendMessage(PlainText(duplicateGuessMessage!!))
                    } catch (e: Exception) {
                        logger.error("Failed to send duplicate guess message for group $groupId: ${e.message}", e)
                    }
                    return@subscribeAlways
                }

                // 处理其他消息
                if (shouldSendMessage && messageChain != null) {
                    try {
                        if (tempFile != null) {
                            if (!tempFile!!.exists() || tempFile!!.length() == 0L) {
                                logger.error("Temporary file is empty or does not exist: ${tempFile!!.absolutePath}")
                                messageChain = buildMessageChain {
                                    +PlainText("图片生成失败，请稍后重试。")
                                }
                            } else {
                                val uploadedImage = tempFile!!.toExternalResource().use { resource ->
                                    group.uploadImage(resource)
                                }
                                tempFile!!.delete()

                                messageChain = buildMessageChain {
                                    +uploadedImage
                                    +PlainText("\n")
                                    +messageChain!!
                                }
                            }
                        }

                        group.sendMessage(messageChain!!)
                    } catch (e: Exception) {
                        logger.error("Failed to send message for group $groupId: ${e.message}", e)
                        try {
                            group.sendMessage("消息发送失败，请稍后重试。")
                        } catch (e2: Exception) {
                            logger.error("Failed to send fallback message for group $groupId: ${e2.message}", e2)
                        }
                    }
                }
            }
        }
    }
}