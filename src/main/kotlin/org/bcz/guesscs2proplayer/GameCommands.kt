package org.bcz.guesscs2proplayer.commands

import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.contact.Group
import org.bcz.guesscs2proplayer.GameState
import org.bcz.guesscs2proplayer.GuessCS2ProPlayer
import org.bcz.guesscs2proplayer.managers.GameStateManager
import org.bcz.guesscs2proplayer.managers.PlayerManager

object GameCommands : CompositeCommand(
    GuessCS2ProPlayer,
    primaryName = "猜选手",
    description = "CS2 猜职业哥游戏命令"
) {
    @SubCommand("start")
    @Description("开始 CS2 猜职业哥游戏")
    suspend fun CommandSender.start() {
        val group = this.subject as? Group ?: run {
            sendMessage("此命令只能在群聊中使用")
            return
        }
        val groupId = group.id

        if (GameStateManager.hasGameState(groupId)) {
            sendMessage("群内已有进行中的游戏，请先完成。")
            return
        }


        val targetPlayer = PlayerManager.getRandomPlayer()
        GameStateManager.startGame(groupId, GameState(groupId, targetPlayer))
        sendMessage("游戏开始！群内成员可以直接发送选手名字进行猜测（例如：s1mple），共有 10 次机会。")
    }

    @SubCommand("stop")
    @Description("结束 CS2 猜职业哥游戏")
    suspend fun CommandSender.stop() {
        val group = this.subject as? Group ?: run {
            sendMessage("此命令只能在群聊中使用")
            return
        }
        val groupId = group.id

        if (!GameStateManager.hasGameState(groupId)) {
            sendMessage("当前没有进行中的游戏。")
            return
        }

        val gameState = GameStateManager.getGameState(groupId)!!
        sendMessage("游戏已结束！正确选手为 ${gameState.targetPlayer.name}")
        GameStateManager.removeGameState(groupId)
    }
}