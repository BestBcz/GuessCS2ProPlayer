
package org.bcz.guesscs2proplayer

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.time.LocalDate
import java.util.concurrent.ConcurrentHashMap

object LeaderboardManager {
    private val winCounts = ConcurrentHashMap<String, Int>()
    private val lossCounts = ConcurrentHashMap<String, Int>()
    private lateinit var dataFolder: File

    @Serializable
    data class LeaderboardData(
        val wins: Map<String, Int>,
        val losses: Map<String, Int>
    )

    suspend fun initialize(folder: File) {
        dataFolder = folder
        val leaderboardFile = File(dataFolder, "leaderboard.json")
        if (leaderboardFile.exists()) {
            try {
                val data = Json.decodeFromString<LeaderboardData>(leaderboardFile.readText())
                winCounts.putAll(data.wins)
                lossCounts.putAll(data.losses)
                GuessCS2ProPlayer.logger.info("Loaded leaderboard data: ${winCounts.size} wins, ${lossCounts.size} losses")
            } catch (e: Exception) {
                GuessCS2ProPlayer.logger.error("Failed to load leaderboard data: ${e.message}", e)
            }
        }
    }

    fun recordWin(playerId: String) {
        winCounts[playerId] = winCounts.getOrDefault(playerId, 0) + 1
        saveLeaderboard()
    }

    fun recordLoss(playerId: String) {
        lossCounts[playerId] = lossCounts.getOrDefault(playerId, 0) + 1
        saveLeaderboard()
    }

    fun getWeeklyLeaderboard(): String {
        if (winCounts.isEmpty()) return "🏆 当前无数据"
        return winCounts.entries.sortedByDescending { it.value }
            .joinToString("\n") { "ID ${it.key}: ${it.value}胜" }
    }

    fun resetWeeklyStats() {
        // 备份上周数据
        val backupFile = File(dataFolder, "leaderboard_backup_${LocalDate.now()}.json")
        if (winCounts.isNotEmpty() || lossCounts.isNotEmpty()) {
            val data = LeaderboardData(winCounts.toMap(), lossCounts.toMap())
            backupFile.writeText(Json.encodeToString(data))
            GuessCS2ProPlayer.logger.info("Backed up leaderboard to ${backupFile.absolutePath}")
        }
        // 清空数据
        winCounts.clear()
        lossCounts.clear()
        saveLeaderboard()
    }

    private fun saveLeaderboard() {
        val leaderboardFile = File(dataFolder, "leaderboard.json")
        try {
            val data = LeaderboardData(winCounts.toMap(), lossCounts.toMap())
            leaderboardFile.writeText(Json.encodeToString(data))
        } catch (e: Exception) {
            GuessCS2ProPlayer.logger.error("Failed to save leaderboard: ${e.message}", e)
        }
    }
}