package com.mani.carcassonnescorekeeper.data

import org.json.JSONArray
import org.json.JSONObject

/** JSON format for saved games. Decoding rejects data the UI could not display safely. */
internal object GameJson {
    /** v1 stored pawn colours as a palette index; v2 stores them as "#RRGGBB". */
    const val FORMAT_VERSION = 2

    fun encode(game: Game): JSONObject = JSONObject().apply {
        put("id", game.id)
        put("startedAt", game.startedAt)
        game.finishedAt?.let { put("finishedAt", it) }
        put("players", JSONArray().apply {
            game.players.forEachIndexed { i, p ->
                // "score" is redundant with the events but keeps the file readable on its own.
                put(JSONObject().put("name", p.name).put("color", PawnPalette.toHex(p.argb)).put("score", game.scores[i]))
            }
        })
        put("events", JSONArray().apply {
            game.events.forEach { put(JSONArray().put(it.player).put(it.delta).put(it.at)) }
        })
    }

    /** @throws org.json.JSONException or [IllegalArgumentException] if [json] is not a valid game. */
    fun decode(json: JSONObject): Game {
        val players = json.getJSONArray("players")
        val events = json.getJSONArray("events")
        val game = Game(
            id = json.getLong("id"),
            startedAt = json.getLong("startedAt"),
            finishedAt = if (json.has("finishedAt")) json.getLong("finishedAt") else null,
            players = List(players.length()) {
                val p = players.getJSONObject(it)
                Player(p.getString("name"), decodeColor(p.opt("color"), it))
            },
            events = List(events.length()) {
                val e = events.getJSONArray(it)
                ScoreEvent(player = e.getInt(0), delta = e.getInt(1), at = e.getLong(2))
            },
        )
        require(game.players.isNotEmpty()) { "Game ${game.id} has no players" }
        require(game.events.all { it.player in game.players.indices }) { "Game ${game.id} has an event for an unknown player" }
        return game
    }

    private fun decodeColor(value: Any?, playerIndex: Int): Int = when (value) {
        is String -> requireNotNull(PawnPalette.parseHex(value)) { "Bad colour $value" }
        is Number -> PawnPalette.legacyByIndex[Math.floorMod(value.toInt(), PawnPalette.legacyByIndex.size)]
        else -> PawnPalette.legacyByIndex[playerIndex % PawnPalette.legacyByIndex.size]
    }

    fun encodeHistory(games: List<Game>): JSONObject = JSONObject()
        .put("version", FORMAT_VERSION)
        .put("games", JSONArray().apply { games.forEach { put(encode(it)) } })

    /** Decodes every readable game; [HistoryResult.skipped] counts the ones that were not. */
    fun decodeHistory(json: JSONObject): HistoryResult {
        val array = json.getJSONArray("games")
        val games = ArrayList<Game>(array.length())
        var skipped = 0
        for (i in 0 until array.length()) {
            runCatching { decode(array.getJSONObject(i)) }
                .onSuccess(games::add)
                .onFailure { skipped++ }
        }
        return HistoryResult(games, skipped)
    }

    data class HistoryResult(val games: List<Game>, val skipped: Int)
}
