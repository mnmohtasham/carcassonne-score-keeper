package com.mani.carcassonnescorekeeper.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StatsTest {
    private fun game(id: Long, vararg scores: Pair<String, Int>) = Game(
        id = id,
        startedAt = id,
        finishedAt = id + 1,
        players = scores.mapIndexed { i, (name, _) -> Player(name, PawnPalette.presets[i]) },
        events = scores.mapIndexed { i, (_, points) -> ScoreEvent(i, points, id) },
    )

    @Test
    fun scoresSumEventsIncludingCorrections() {
        val g = Game(
            id = 1, startedAt = 0,
            players = listOf(Player("Anna", PawnPalette.RED), Player("Ben", PawnPalette.BLUE)),
            events = listOf(ScoreEvent(0, 9, 0), ScoreEvent(1, 4, 0), ScoreEvent(0, -9, 0), ScoreEvent(0, 3, 0)),
        )
        assertEquals(listOf(3, 4), g.scores)
        assertEquals(setOf(1), g.winners)
    }

    @Test
    fun tiedPlayersAllWinAndShareRank() {
        val g = game(1, "Anna" to 80, "Ben" to 80, "Cara" to 50)
        assertEquals(setOf(0, 1), g.winners)
        assertEquals(listOf(1, 1, 3), g.players.indices.map(g::rankOf))
    }

    @Test
    fun gameWithoutPlayersHasNoWinners() {
        assertTrue(Game(id = 1, startedAt = 0, players = emptyList()).winners.isEmpty())
    }

    @Test
    fun leaderboardCountsWinsAndMatchesNamesIgnoringCase() {
        val history = listOf(
            game(3, "anna " to 70, "Ben" to 90),
            game(2, "Anna" to 100, "Ben" to 100),
            game(1, "Anna" to 120, "Ben" to 60),
        )
        val board = leaderboard(history)
        assertEquals(listOf("anna", "Ben"), board.map { it.name })
        val anna = board[0]
        assertEquals(2, anna.wins)
        assertEquals(3, anna.played)
        assertEquals(120, anna.bestScore)
        assertEquals(97, anna.averageScore)
        assertEquals(2, board[1].wins)
    }

    @Test
    fun leaderboardBreaksWinTiesByWinRate() {
        val history = listOf(
            game(1, "Anna" to 10, "Ben" to 5),
            game(2, "Cara" to 10, "Dan" to 5),
            game(3, "Cara" to 1, "Dan" to 5),
        )
        assertEquals(listOf("Anna", "Cara", "Dan", "Ben"), leaderboard(history).map { it.name })
    }

    @Test
    fun knownPlayersAreDistinctNewestFirstWithLatestColour() {
        val history = listOf(game(2, "Cara" to 1, "anna" to 2), game(1, "Anna" to 1, "Ben" to 2))
        val known = knownPlayers(history)
        assertEquals(listOf("Cara", "anna", "Ben"), known.map { it.name })
        // Anna was player 2 (second preset colour) in the newest game.
        assertEquals(PawnPalette.presets[1], known[1].argb)
    }
}
