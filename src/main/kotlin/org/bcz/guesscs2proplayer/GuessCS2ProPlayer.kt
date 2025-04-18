package org.bcz.guesscs2proplayer

import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.buildMessageChain
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import org.bcz.guesscs2proplayer.commands.GameCommands
import org.bcz.guesscs2proplayer.managers.GameStateManager
import org.bcz.guesscs2proplayer.managers.PlayerManager

object GuessCS2ProPlayer : KotlinPlugin(
    JvmPluginDescription(
        id = "org.bcz.guesscs2proplayer",
        name = "CS2猜职业哥小游戏",
        version = "0.0.6"
    ) {
        author("Bcz")
        dependsOn("xyz.cssxsh.mirai.plugin.mirai-skia-plugin", ">= 1.1.0", false)
    }
) {
    override fun onEnable() {
        // 初始化玩家管理器
        runBlocking {
            PlayerManager.initialize(dataFolder)
        }
        if (!PlayerManager.isInitialized()) {
            logger.error("Failed to initialize PlayerManager, plugin will not function.")
            return
        }

        // 注册命令
        CommandManager.INSTANCE.registerCommand(GameCommands, true)

        // 监听群消息
        globalEventChannel().subscribeAlways<net.mamoe.mirai.event.events.GroupMessageEvent> { event ->
            val groupId = group.id
            val message = event.message.contentToString().trim()
            val senderName = sender.nick
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
                        messageChain = buildMessageChain {
                            //+PlainText("未找到选手：$message")
                        }
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
                        logger.error("Failed to draw table: ${e.message}", e)
                        messageChain = buildMessageChain {
                            +PlainText("生成表格失败，请稍后重试。")
                        }
                        return@synchronized
                    }

                    messageChain = buildMessageChain {
                        if (guessedPlayer.name == gameState.targetPlayer.name) {
                            +PlainText("恭喜！${senderName} 猜对了选手：${gameState.targetPlayer.name}")
                            GameStateManager.removeGameState(groupId)
                        } else if (gameState.guessesLeft == 0) {
                            +PlainText("游戏结束！正确选手为 ${gameState.targetPlayer.name}")
                            GameStateManager.removeGameState(groupId)
                        } else {
                            +PlainText("${senderName} 猜测了 ${guessedPlayer.name}，剩余 ${gameState.guessesLeft} 次猜测机会。")
                        }
                    }
                }
                // 把 suspend 函数
                if (duplicateGuessMessage != null) {
                    group.sendMessage(PlainText(duplicateGuessMessage!!))
                    return@subscribeAlways
                }


                if (shouldSendMessage) {
                    if (tempFile != null) {
                        val imageMessage = try {
                            if (!tempFile!!.exists() || tempFile!!.length() == 0L) {
                                throw IllegalStateException("Temporary file is empty or does not exist: ${tempFile!!.absolutePath}")
                            }

                            val uploadedImage = tempFile!!.toExternalResource().use { resource ->
                                group.uploadImage(resource)
                            }

                            tempFile!!.delete()
                            uploadedImage
                        } catch (e: Exception) {
                            logger.error("Failed to upload image: ${e.message}", e)
                            group.sendMessage("图片上传失败，请稍后重试。")
                            return@subscribeAlways
                        }

                        messageChain = buildMessageChain {
                            +imageMessage
                            +PlainText("\n")
                            +messageChain!!
                        }
                    }

                    if (messageChain != null) {
                        group.sendMessage(messageChain!!)
                    }
                }
            }
        }
    }
}
