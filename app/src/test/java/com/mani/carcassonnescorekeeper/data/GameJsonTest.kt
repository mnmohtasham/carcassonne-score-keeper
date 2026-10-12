package com.mani.carcassonnescorekeeper.data

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GameJsonTest {
    private val game = Game(
        id = 42,
        startedAt = 1_000,
        finishedAt = 9_000,
        players = listOf(Player("Anna", PawnPalette.YELLOW), Player("Ben", 0xFF123456.toInt())),
        events = listOf(ScoreEvent(0, 9, 2_000), ScoreEvent(1, 4, 3_000), ScoreEvent(0, -2, 4_000)),
    )

    @Test
    fun gameSurvivesRoundTrip() {
        val decoded = GameJson.decode(JSONObject(GameJson.encode(game).toString()))
        assertEquals(game, decoded)
        assertEquals(listOf(7, 4), decoded.scores)
    }

    @Test
    fun gameInProgressHasNoFinishTime() {
        val decoded = GameJson.decode(GameJson.encode(game.copy(finishedAt = null)))
        assertNull(decoded.finishedAt)
    }

    @Test
    fun encodedPlayersIncludeFinalScore() {
        val players = GameJson.encode(game).getJSONArray("players")
        assertEquals(7, players.getJSONObject(0).getInt("score"))
    }

    @Test
    fun coloursAreSavedAsHex() {
        val players = GameJson.encode(game).getJSONArray("players")
        assertEquals("#F4C430", players.getJSONObject(0).getString("color"))
        assertEquals("#123456", players.getJSONObject(1).getString("color"))
    }

    @Test
    fun legacyPaletteIndexIsMigratedToColour() {
        val json = GameJson.encode(game)
        val players = json.getJSONArray("players")
        players.getJSONObject(0).put("color", 0) // v1: index 0 was red
        players.getJSONObject(1).put("color", 3) // v1: index 3 was yellow
        val decoded = GameJson.decode(json)
        assertEquals(listOf(PawnPalette.RED, PawnPalette.YELLOW), decoded.players.map { it.argb })
    }

    @Test(expected = IllegalArgumentException::class)
    fun malformedColourIsRejected() {
        val json = GameJson.encode(game)
        json.getJSONArray("players").getJSONObject(0).put("color", "#12")
        GameJson.decode(json)
    }

    @Test(expected = IllegalArgumentException::class)
    fun eventForUnknownPlayerIsRejected() {
        val json = GameJson.encode(game)
        json.getJSONArray("events").put(JSONArray().put(5).put(1).put(5_000))
        GameJson.decode(json)
    }

    @Test(expected = IllegalArgumentException::class)
    fun gameWithoutPlayersIsRejected() {
        GameJson.decode(GameJson.encode(game.copy(players = emptyList(), events = emptyList())))
    }

    @Test
    fun historyKeepsReadableGamesWhenOneIsCorrupt() {
        val json = GameJson.encodeHistory(listOf(game, game.copy(id = 43)))
        json.getJSONArray("games").put(JSONObject().put("id", "not a game"))
        val result = GameJson.decodeHistory(JSONObject(json.toString()))
        assertEquals(listOf(42L, 43L), result.games.map { it.id })
        assertEquals(1, result.skipped)
    }
}
