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

    val targetPlayer = PlayerManager.getRandomPlayer()
    GameStateManager.startGame(groupId, GameState(groupId, targetPlayer, gameMode = gameMode))
    sender.sendMessage("游戏开始（模式：${gameMode.name}）！群内成员可以直接发送选手名字进行猜测（例如：s1mple），每局共有 10 次机会。")
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