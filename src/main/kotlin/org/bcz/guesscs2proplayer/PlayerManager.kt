package org.bcz.guesscs2proplayer

import org.bcz.guesscs2proplayer.GuessCS2ProPlayer
import org.bcz.guesscs2proplayer.Player
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import kotlin.random.Random

object PlayerManager {
    private var players: List<Player> = emptyList()

    fun isInitialized(): Boolean = players.isNotEmpty()

    fun initialize(dataFolder: File) {
        val csvFile = File(dataFolder, "players.csv")
        if (!csvFile.exists()) {
            GuessCS2ProPlayer.logger.error("players.csv not found in ${dataFolder.path}")
            return
        }

        try {
            players = loadPlayersFromCsv(csvFile)
            GuessCS2ProPlayer.logger.info("Loaded ${players.size} players from CSV")
        } catch (e: Exception) {
            GuessCS2ProPlayer.logger.error("Failed to load players from CSV: ${e.message}", e)
        }
    }

    fun getRandomPlayer(): Player {
        if (players.isEmpty()) {
            throw IllegalStateException("Players list is not initialized or empty")
        }
        return players[Random.nextInt(players.size)]
    }

    fun findPlayer(name: String): Player? {
        if (players.isEmpty()) {
            throw IllegalStateException("Players list is not initialized")
        }

        val normalizedInput = name.lowercase().replace(Regex("[^a-z0-9]"), "")
        val inputWithI = normalizedInput.replace("1", "i")
        val inputWith1 = normalizedInput.replace("i", "1")
        val inputWithO = inputWithI.replace("0", "o")
        val inputWith0 = inputWith1.replace("o", "0")

        return players.find { player ->
            val normalizedPlayerName = player.name.lowercase().replace(Regex("[^a-z0-9]"), "")
            val playerNameWithI = normalizedPlayerName.replace("1", "i")
            val playerNameWith1 = normalizedPlayerName.replace("i", "1")
            val playerNameWithO = playerNameWithI.replace("0", "o")
            val playerNameWith0 = playerNameWith1.replace("o", "0")

            normalizedPlayerName == normalizedInput ||
                    playerNameWithI == inputWithI ||
                    playerNameWith1 == inputWith1 ||
                    playerNameWithO == inputWithO ||
                    playerNameWith0 == inputWith0
        }
    }

    private fun loadPlayersFromCsv(file: File): List<Player> {
        val players = mutableListOf<Player>()
        BufferedReader(FileReader(file, Charsets.UTF_8)).use { reader ->
            reader.readLine() // 跳过表头
            reader.forEachLine { line ->
                val columns = line.split(",").map { it.trim() }
                if (columns.size == 5) {
                    try {
                        val player = Player(
                            name = columns[0],
                            team = columns[1],
                            nationality = columns[2],
                            age = columns[3].toInt(),
                            position = columns[4]
                        )
                        players.add(player)
                    } catch (e: Exception) {
                        GuessCS2ProPlayer.logger.error("Failed to parse line: $line, error: ${e.message}", e)
                    }
                }
            }
        }
        return players
    }
}