package com.mani.carcassonnescorekeeper.ui

import android.app.Application
import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.mani.carcassonnescorekeeper.data.Game
import com.mani.carcassonnescorekeeper.data.Player
import com.mani.carcassonnescorekeeper.data.ScoreEvent
import com.mani.carcassonnescorekeeper.data.Storage
import com.mani.carcassonnescorekeeper.data.knownPlayers as knownPlayersIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

sealed interface Screen {
    data object Home : Screen
    data object Setup : Screen
    data object Scoring : Screen
    data class GameDetail(val gameId: Long, val justFinished: Boolean = false) : Screen
    data object History : Screen
    data object Leaderboard : Screen
    data object Settings : Screen
}

class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val storage = Storage(app)

    // Single-threaded so saves land in order; not tied to viewModelScope so a final save
    // is not cancelled when the screen closes.
    private val io = CoroutineScope(SupervisorJob() + Dispatchers.IO.limitedParallelism(1))

    var currentGame: Game? by mutableStateOf(storage.loadCurrentGame())
        private set

    /** Finished games, newest first. */
    var history: List<Game> by mutableStateOf(storage.loadHistory())
        private set

    var keepScreenAwake: Boolean by mutableStateOf(storage.keepScreenAwake)
        private set

    // ---- Navigation ----

    private val backStack = mutableStateListOf<Screen>(Screen.Home)
    val screen: Screen get() = backStack.last()
    val canGoBack: Boolean get() = backStack.size > 1

    // Screens cross-fade, so the outgoing screen can still catch a second tap; ignore repeats.
    fun navigate(to: Screen) {
        if (screen != to) backStack.add(to)
    }

    fun back() {
        if (canGoBack) backStack.removeAt(backStack.lastIndex)
    }

    private fun resetTo(vararg screens: Screen) {
        backStack.add(screens.last()) // keep the stack non-empty while rebuilding it
        repeat(backStack.size - 1) { backStack.removeAt(0) }
        backStack.addAll(0, screens.dropLast(1))
    }

    // ---- Game in progress ----

    /** Players to pre-fill a new game with: the line-up of the last finished game. */
    fun suggestedLineup(): List<Player> = history.firstOrNull()?.players.orEmpty()

    /** Everyone who has played before, with the pawn colour they used last. */
    fun knownPlayers(): List<Player> = knownPlayersIn(history)

    fun startGame(players: List<Player>) {
        if (screen != Screen.Setup || players.isEmpty()) return // e.g. a double tap on "Start game"
        val now = System.currentTimeMillis()
        lastTap = null
        updateGame(Game(id = now, startedAt = now, players = players))
        resetTo(Screen.Home, Screen.Scoring)
    }

    private var lastTap: Pair<ScoreEvent, Long>? = null

    /**
     * Adds one point. Quick repeated taps on the same player merge into a single log entry,
     * so tapping three times records "+3" rather than three "+1"s.
     */
    fun tapPoint(player: Int) {
        val game = currentGame ?: return
        val now = SystemClock.uptimeMillis()
        val last = game.events.lastOrNull()
        val tap = lastTap
        val event = if (tap != null && last === tap.first && last.player == player && now - tap.second < TAP_MERGE_MS) {
            last.copy(delta = last.delta + 1, at = System.currentTimeMillis())
                .also { updateGame(game.copy(events = game.events.dropLast(1) + it)) }
        } else {
            ScoreEvent(player, 1, System.currentTimeMillis())
                .also { updateGame(game.copy(events = game.events + it)) }
        }
        lastTap = event to now
    }

    /** Adds a typed amount; negative values deduct points. */
    fun addPoints(player: Int, delta: Int) {
        val game = currentGame ?: return
        if (delta == 0) return
        lastTap = null
        updateGame(game.copy(events = game.events + ScoreEvent(player, delta, System.currentTimeMillis())))
    }

    fun removeEvent(index: Int): ScoreEvent? {
        val game = currentGame ?: return null
        val event = game.events.getOrNull(index) ?: return null
        lastTap = null
        updateGame(game.copy(events = game.events.toMutableList().apply { removeAt(index) }))
        return event
    }

    fun restoreEvent(index: Int, event: ScoreEvent) {
        val game = currentGame ?: return
        lastTap = null
        val events = game.events.toMutableList().apply { add(index.coerceIn(0, size), event) }
        updateGame(game.copy(events = events))
    }

    fun finishGame() {
        val game = currentGame ?: return
        val finished = game.copy(finishedAt = System.currentTimeMillis())
        updateHistory(listOf(finished) + history)
        updateGame(null)
        resetTo(Screen.Home, Screen.GameDetail(finished.id, justFinished = true))
    }

    fun discardGame() {
        updateGame(null)
        resetTo(Screen.Home)
    }

    // ---- History & settings ----

    fun deleteGame(id: Long) {
        updateHistory(history.filterNot { it.id == id })
        if ((screen as? Screen.GameDetail)?.gameId == id) back()
    }

    fun clearHistory() = updateHistory(emptyList())

    fun updateKeepScreenAwake(enabled: Boolean) {
        keepScreenAwake = enabled
        storage.keepScreenAwake = enabled
    }

    private fun updateGame(game: Game?) {
        currentGame = game
        io.launch { storage.saveCurrentGame(game) }
    }

    private fun updateHistory(games: List<Game>) {
        history = games
        io.launch { storage.saveHistory(games) }
    }

    private companion object {
        const val TAP_MERGE_MS = 2_000L
    }
}
