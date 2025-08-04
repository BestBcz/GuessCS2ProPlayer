package org.bcz.guesscs2proplayer.commands

import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.contact.Group
import org.bcz.guesscs2proplayer.GameMode
import org.bcz.guesscs2proplayer.GameState
import org.bcz.guesscs2proplayer.GuessCS2ProPlayer
import org.bcz.guesscs2proplayer.managers.GameStateManager
import org.bcz.guesscs2proplayer.managers.PlayerManager
import org.bcz.guesscs2proplayer.LeaderboardManager
import org.bcz.guesscs2proplayer.Config

object GameCommands : CompositeCommand(
    GuessCS2ProPlayer,
    primaryName = "猜选手",
    description = "CS2 猜职业哥游戏命令"
) {
    @SubCommand("开始")
    @Description("开始 CS2 猜职业哥游戏，可指定模式：Default, bo3, bo5")
    suspend fun CommandSender.start(mode: String? = "Default") {
        doStart(this, mode)
    }

    @SubCommand("结束")
    @Description("结束 CS2 猜职业哥游戏")
    suspend fun CommandSender.stop() {
        doStop(this)
    }

    @SubCommand("排行榜")
    @Description("查看本周胜场排行榜")
    suspend fun CommandSender.leaderboard() {
        val group = this.subject as? Group ?: run {
            sendMessage("此命令只能在群聊中使用")
            return
        }
        val leaderboard = LeaderboardManager.getWeeklyLeaderboard()
        sendMessage("🏆 本周胜场排行榜：\n$leaderboard")
    }

    @SubCommand("resetleaderboard")
    @Description("重置排行榜（管理员专用）")
    suspend fun CommandSender.resetLeaderboard() {
        val group = this.subject as? Group ?: run {
            sendMessage("此命令只能在群聊中使用")
            return
        }
        // 可选：添加管理员权限检查
        LeaderboardManager.resetWeeklyStats()
        sendMessage("排行榜已重置！")
    }

    @SubCommand("网络模式")
    @Description("切换网络模式：on/off - 开启/关闭实时获取选手信息")
    suspend fun CommandSender.networkMode(enabled: String) {
        val group = this.subject as? Group ?: run {
            sendMessage("此命令只能在群聊中使用")
            return
        }
        
        val isEnabled = when (enabled.lowercase()) {
            "on", "true", "1", "开启" -> true
            "off", "false", "0", "关闭" -> false
            else -> {
                sendMessage("参数错误！请使用：on/off 或 开启/关闭")
                return
            }
        }
        
        PlayerManager.setNetworkMode(isEnabled)
        val status = if (isEnabled) "开启" else "关闭"
        sendMessage("网络模式已$status！${if (isEnabled) "将实时从HLTV/液体百科获取选手信息" else "将使用本地CSV数据"}")
    }

    @SubCommand("状态")
    @Description("查看当前游戏状态和网络模式")
    suspend fun CommandSender.status() {
        val group = this.subject as? Group ?: run {
            sendMessage("此命令只能在群聊中使用")
            return
        }
        
        val groupId = group.id
        val hasGame = GameStateManager.hasGameState(groupId)
        val networkMode = PlayerManager.isNetworkModeEnabled()
        
        val statusMessage = buildString {
            appendLine("📊 游戏状态：")
            appendLine("• 网络模式：${if (networkMode) "✅ 开启" else "❌ 关闭"}")
            appendLine("• 当前游戏：${if (hasGame) "✅ 进行中" else "❌ 无"}")
            
            if (hasGame) {
                val gameState = GameStateManager.getGameState(groupId)!!
                appendLine("• 剩余次数：${gameState.guessesLeft}")
                appendLine("• 游戏模式：${gameState.gameMode.name}")
            }
        }
        
        sendMessage(statusMessage)
    }

    @SubCommand("配置")
    @Description("查看当前配置信息")
    suspend fun CommandSender.config() {
        val group = this.subject as? Group ?: run {
            sendMessage("此命令只能在群聊中使用")
            return
        }
        
        sendMessage(Config.getConfigSummary())
    }
}

object StartGameSimpleCommand : SimpleCommand(
    GuessCS2ProPlayer,
    primaryName = "开始猜选手",
    description = "直接开始 CS2 猜职业哥游戏（默认单局模式）"
) {
    @Handler
    suspend fun CommandSender.handle() {
        doStart(this, "Default")
    }
}

suspend fun doStart(sender: CommandSender, mode: String? = "Default") {
    val group = sender.subject as? Group ?: run {
        sender.sendMessage("此命令只能在群聊中使用")
        return
    }
    val groupId = group.id

    if (GameStateManager.hasGameState(groupId)) {
        sender.sendMessage("群内已有进行中的游戏，请先完成。")
        return
    }

    val gameMode = when (mode?.lowercase()) {
        "bo3" -> GameMode.BO3
        "bo5" -> GameMode.BO5
        else -> GameMode.Default
    }

    try {
        sender.sendMessage("正在获取选手信息...")
        val targetPlayer = PlayerManager.getRandomPlayer()
        GameStateManager.startGame(groupId, GameState(groupId, targetPlayer, gameMode = gameMode))
        
        val networkMode = PlayerManager.isNetworkModeEnabled()
        val modeInfo = if (networkMode) "（实时数据模式）" else "（本地数据模式）"
        
        sender.sendMessage("游戏开始${modeInfo}（模式：${gameMode.name}）！群内成员可以直接发送选手名字进行猜测（例如：s1mple），每局共有 10 次机会。")
    } catch (e: Exception) {
        GuessCS2ProPlayer.logger.error("Failed to start game", e)
        sender.sendMessage("游戏启动失败：${e.message}")
    }
}

suspend fun doStop(sender: CommandSender) {
    val group = sender.subject as? Group ?: run {
        sender.sendMessage("此命令只能在群聊中使用")
        return
    }
    val groupId = group.id

    if (!GameStateManager.hasGameState(groupId)) {
        sender.sendMessage("当前没有进行中的游戏。")
        return
    }

    val gameState = GameStateManager.getGameState(groupId)!!
    sender.sendMessage("游戏已结束！正确选手为 ${gameState.targetPlayer.name}")
    GameStateManager.removeGameState(groupId)
}