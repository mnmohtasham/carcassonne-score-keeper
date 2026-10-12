package com.mani.carcassonnescorekeeper.data

import android.content.Context
import android.util.AtomicFile
import android.util.Log
import androidx.core.content.edit
import org.json.JSONObject
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

/**
 * Persists the game in progress, finished games and settings in the app's private storage.
 * Files are plain JSON written atomically, so a crash mid-write never corrupts them.
 */
class Storage(context: Context) {
    private val dir = context.filesDir
    private val currentFile = AtomicFile(File(dir, "current_game.json"))
    private val historyFile = AtomicFile(File(dir, "history.json"))
    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    var keepScreenAwake: Boolean
        get() = prefs.getBoolean(KEY_KEEP_AWAKE, false)
        set(value) = prefs.edit { putBoolean(KEY_KEEP_AWAKE, value) }

    fun loadCurrentGame(): Game? {
        val text = read(currentFile) ?: return null
        return runCatching { GameJson.decode(JSONObject(text)) }
            .onFailure { Log.w(TAG, "Unreadable current game, ignoring it", it) }
            .getOrNull()
    }

    fun saveCurrentGame(game: Game?) {
        if (game == null) currentFile.delete() else write(currentFile, GameJson.encode(game).toString())
    }

    /** Finished games, newest first. */
    fun loadHistory(): List<Game> {
        val text = read(historyFile) ?: return emptyList()
        val result = runCatching { GameJson.decodeHistory(JSONObject(text)) }
            .getOrElse {
                Log.e(TAG, "Unreadable history", it)
                GameJson.HistoryResult(emptyList(), skipped = 1)
            }
        if (result.skipped > 0) {
            // Keep the original rather than letting the next save drop the unreadable games.
            Log.e(TAG, "Skipped ${result.skipped} unreadable game(s); keeping a copy of the file")
            val backup = File(dir, "history-unreadable-${Integer.toHexString(text.hashCode())}.json")
            if (!backup.exists()) runCatching { backup.writeText(text) }
        }
        return result.games.sortedByDescending { it.finishedAt ?: it.startedAt }
    }

    fun saveHistory(games: List<Game>) {
        write(historyFile, GameJson.encodeHistory(games).toString())
    }

    private fun read(file: AtomicFile): String? =
        try {
            file.readFully().toString(Charsets.UTF_8)
        } catch (_: FileNotFoundException) {
            null
        } catch (e: IOException) {
            Log.e(TAG, "Failed to read ${file.baseFile.name}", e)
            null
        }

    private fun write(file: AtomicFile, text: String) {
        val out = try {
            file.startWrite()
        } catch (e: IOException) {
            Log.e(TAG, "Failed to open ${file.baseFile.name} for writing", e)
            return
        }
        try {
            out.write(text.toByteArray(Charsets.UTF_8))
            file.finishWrite(out)
        } catch (e: IOException) {
            file.failWrite(out)
            Log.e(TAG, "Failed to save ${file.baseFile.name}", e)
        }
    }

    private companion object {
        const val TAG = "Storage"
        const val KEY_KEEP_AWAKE = "keep_screen_awake"
    }
}
