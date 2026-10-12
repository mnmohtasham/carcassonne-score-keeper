package com.mani.carcassonnescorekeeper.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.mani.carcassonnescorekeeper.R
import com.mani.carcassonnescorekeeper.data.PawnPalette
import com.mani.carcassonnescorekeeper.data.Player
import com.mani.carcassonnescorekeeper.data.nameKey
import com.mani.carcassonnescorekeeper.ui.AppScaffold
import com.mani.carcassonnescorekeeper.ui.AppViewModel
import com.mani.carcassonnescorekeeper.ui.MeepleToken
import com.mani.carcassonnescorekeeper.ui.PawnColorSheet

private const val MIN_PLAYERS = 2
private const val MAX_NAME_LENGTH = 24
private const val MAX_PLAYERS = 8

private data class Draft(val id: Int, val name: String, val argb: Int)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SetupScreen(vm: AppViewModel) {
    val drafts = remember {
        val lineup = vm.suggestedLineup().ifEmpty {
            listOf(Player("", PawnPalette.presets[0]), Player("", PawnPalette.presets[1]))
        }
        mutableStateListOf<Draft>().apply { lineup.forEachIndexed { i, p -> add(Draft(i, p.name, p.argb)) } }
    }
    val knownPlayers = remember { vm.knownPlayers() }
    var pickingColorFor by remember { mutableStateOf<Int?>(null) }

    // Rows are addressed by id, not by list position, so a click handler that was
    // composed before a row was removed can never edit the wrong player.
    fun update(id: Int, change: (Draft) -> Draft) {
        val index = drafts.indexOfFirst { it.id == id }
        if (index >= 0) drafts[index] = change(drafts[index])
    }
    fun colorsUsedExcept(id: Int?) = drafts.filter { it.id != id }.map { it.argb }.toSet()

    /** Adds a blank row, or a returning [player] with their last pawn colour when it is still free. */
    fun addPlayer(player: Player? = null) {
        val empty = drafts.firstOrNull { it.name.isBlank() }
        if (player != null && empty != null) {
            val used = colorsUsedExcept(empty.id)
            update(empty.id) {
                it.copy(name = player.name, argb = if (player.argb in used) it.argb else player.argb)
            }
        } else if (drafts.size < MAX_PLAYERS) {
            val used = colorsUsedExcept(null)
            val argb = player?.argb?.takeIf { it !in used } ?: PawnPalette.firstFree(used)
            drafts.add(Draft((drafts.maxOfOrNull { it.id } ?: -1) + 1, player?.name.orEmpty(), argb))
        }
    }

    val names = drafts.map { it.name.trim() }
    val keys = names.map(::nameKey)
    val duplicateKeys = keys.filter { it.isNotEmpty() }.groupingBy { it }.eachCount().filterValues { it > 1 }.keys
    val problem = when {
        drafts.size < MIN_PLAYERS -> pluralStringResource(R.plurals.setup_need_players, MIN_PLAYERS, MIN_PLAYERS)
        names.any { it.isEmpty() } -> stringResource(R.string.setup_need_names)
        duplicateKeys.isNotEmpty() -> stringResource(R.string.setup_duplicate_names)
        drafts.map { it.argb }.toSet().size < drafts.size -> stringResource(R.string.setup_duplicate_colours)
        else -> null
    }
    val suggestions = knownPlayers.filter { nameKey(it.name) !in keys }
    val canAddSuggestion = drafts.size < MAX_PLAYERS || drafts.any { it.name.isBlank() }

    AppScaffold(
        title = stringResource(R.string.new_game),
        onBack = vm::back,
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Column(
                    Modifier
                        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal))
                        .padding(16.dp),
                ) {
                    if (problem != null) {
                        Text(
                            problem,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp),
                        )
                    }
                    Button(
                        onClick = { vm.startGame(drafts.map { Player(it.name.trim(), it.argb) }) },
                        enabled = problem == null,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                    ) { Text(stringResource(R.string.start_game), style = MaterialTheme.typography.titleMedium) }
                }
            }
        },
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(padding),
        ) {
            item {
                Text(
                    stringResource(R.string.setup_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            itemsIndexed(drafts, key = { _, d -> d.id }) { index, draft ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MeepleToken(
                        draft.argb,
                        size = 44.dp,
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable(
                                onClickLabel = stringResource(R.string.change_colour),
                                role = Role.Button,
                            ) { pickingColorFor = draft.id },
                    )
                    OutlinedTextField(
                        value = draft.name,
                        onValueChange = { value -> update(draft.id) { it.copy(name = value.take(MAX_NAME_LENGTH)) } },
                        placeholder = { Text(stringResource(R.string.player_placeholder, index + 1)) },
                        singleLine = true,
                        isError = nameKey(draft.name) in duplicateKeys,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next,
                        ),
                        modifier = Modifier.weight(1f).padding(start = 12.dp),
                    )
                    IconButton(onClick = { drafts.removeAll { it.id == draft.id } }) {
                        Icon(painterResource(R.drawable.ic_close), contentDescription = stringResource(R.string.remove_player))
                    }
                }
            }
            item {
                OutlinedButton(
                    onClick = { addPlayer() },
                    enabled = drafts.size < MAX_PLAYERS,
                    modifier = Modifier.padding(top = 4.dp),
                ) {
                    Icon(painterResource(R.drawable.ic_add), contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(stringResource(R.string.add_player), modifier = Modifier.padding(start = 8.dp))
                }
            }
            if (suggestions.isNotEmpty()) {
                item {
                    Text(
                        stringResource(R.string.recent_players),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(top = 16.dp),
                    )
                }
                item {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        suggestions.forEach { player ->
                            AssistChip(
                                onClick = { addPlayer(player) },
                                enabled = canAddSuggestion,
                                label = { Text(player.name) },
                                leadingIcon = { MeepleToken(player.argb, size = 20.dp) },
                            )
                        }
                    }
                }
            }
        }
    }

    pickingColorFor?.let { id ->
        val draft = drafts.firstOrNull { it.id == id } ?: return@let
        PawnColorSheet(
            playerName = draft.name.trim(),
            current = draft.argb,
            taken = colorsUsedExcept(id),
            onPick = { argb -> update(id) { it.copy(argb = argb) } },
            onDismiss = { pickingColorFor = null },
        )
    }
}
