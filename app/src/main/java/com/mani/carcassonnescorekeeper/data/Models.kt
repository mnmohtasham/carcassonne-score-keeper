package com.mani.carcassonnescorekeeper.data

/** A player in a game. [argb] is the pawn colour as an opaque ARGB value. */
data class Player(val name: String, val argb: Int)

/** One score change for the player at index [player]. Negative [delta] corrects a mistake. */
data class ScoreEvent(val player: Int, val delta: Int, val at: Long)

data class Game(
    val id: Long,
    val startedAt: Long,
    val players: List<Player>,
    val events: List<ScoreEvent> = emptyList(),
    val finishedAt: Long? = null,
) {
    val scores: List<Int> by lazy {
        val totals = IntArray(players.size)
        events.forEach { if (it.player in totals.indices) totals[it.player] += it.delta }
        totals.toList()
    }

    /** Indices of the players with the highest score; several on a tie. */
    val winners: Set<Int> by lazy {
        val top = scores.maxOrNull() ?: return@lazy emptySet()
        players.indices.filter { scores[it] == top }.toSet()
    }

    /** Player indices from first place to last. */
    val ranking: List<Int> by lazy { players.indices.sortedByDescending { scores[it] } }

    /** Standard competition rank (1, 1, 3, ...) of the player at [index]. */
    fun rankOf(index: Int): Int = 1 + scores.count { it > scores[index] }
}
