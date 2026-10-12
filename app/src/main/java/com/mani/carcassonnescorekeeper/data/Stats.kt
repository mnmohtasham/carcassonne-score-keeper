package com.mani.carcassonnescorekeeper.data

data class PlayerStats(
    val name: String,
    val wins: Int,
    val played: Int,
    val bestScore: Int,
    val totalScore: Int,
) {
    val winRate: Float get() = if (played == 0) 0f else wins.toFloat() / played
    val averageScore: Int get() = if (played == 0) 0 else Math.round(totalScore.toFloat() / played)
}

/** Players are matched across games by name, ignoring case and surrounding spaces. */
fun nameKey(name: String): String = name.trim().lowercase()

/**
 * All-time stats per player across finished games, best first.
 * Every player tied for the top score is credited with a win.
 */
fun leaderboard(history: List<Game>): List<PlayerStats> {
    val byKey = LinkedHashMap<String, PlayerStats>()
    history.sortedBy { it.finishedAt ?: it.startedAt }.forEach { game ->
        game.players.forEachIndexed { i, player ->
            val key = nameKey(player.name)
            val score = game.scores[i]
            val won = i in game.winners
            val prev = byKey[key]
            byKey[key] = PlayerStats(
                name = player.name.trim(), // latest spelling wins
                wins = (prev?.wins ?: 0) + if (won) 1 else 0,
                played = (prev?.played ?: 0) + 1,
                bestScore = maxOf(prev?.bestScore ?: Int.MIN_VALUE, score),
                totalScore = (prev?.totalScore ?: 0) + score,
            )
        }
    }
    return byKey.values.sortedWith(
        compareByDescending<PlayerStats> { it.wins }
            .thenByDescending { it.winRate }
            .thenByDescending { it.played }
            .thenBy { nameKey(it.name) },
    )
}

/**
 * Distinct players from [history] (expected newest first), most recently played first,
 * each with the name spelling and pawn colour from their latest game.
 */
fun knownPlayers(history: List<Game>): List<Player> =
    history.flatMap { game -> game.players.map { it.copy(name = it.name.trim()) } }
        .distinctBy { nameKey(it.name) }
