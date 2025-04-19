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
    val targetPlayer: Player,
    var guessesLeft: Int = 10,
    val guesses: MutableList<Pair<String, Player>> = mutableListOf()

)

