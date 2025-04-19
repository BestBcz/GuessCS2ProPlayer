package org.bcz.guesscs2proplayer


data class Player(
    val name: String,
    val team: String,
    val nationality: String,
    val age: Int,
    val position: String,
    val majorAppearances: Int // 新增 Major 出场次数
)

data class GameState(
    val groupId: Long,
    var targetPlayer: Player,
    var guessesLeft: Int = 10,
    val guesses: MutableList<Pair<String, Player>> = mutableListOf(),
    val gameMode: GameMode = GameMode.Default, // 新增游戏模式
    var currentRound: Int = 1, // 当前局数
    val scores: MutableMap<Long, Int> = mutableMapOf() // 玩家ID -> 分数

)

