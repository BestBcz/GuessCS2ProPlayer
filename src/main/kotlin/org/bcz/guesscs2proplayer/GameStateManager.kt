package org.bcz.guesscs2proplayer.managers

import org.bcz.guesscs2proplayer.GameState
import org.bcz.guesscs2proplayer.Player

object GameStateManager {
    private val gameStates = mutableMapOf<Long, GameState>()

    fun hasGameState(groupId: Long): Boolean = gameStates.containsKey(groupId)

    fun getGameState(groupId: Long): GameState? = gameStates[groupId]

    fun startGame(groupId: Long, gameState: GameState) {
        gameStates[groupId] = gameState
    }

    fun removeGameState(groupId: Long) {
        gameStates.remove(groupId)
    }

    fun nextRound(groupId: Long, newTargetPlayer: Player) {
        val gameState = gameStates[groupId] ?: return
        gameState.targetPlayer = newTargetPlayer
        gameState.guessesLeft = 10
        gameState.guesses.clear()
        gameState.currentRound++
    }
}