package com.mani.carcassonnescorekeeper.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mani.carcassonnescorekeeper.R
import com.mani.carcassonnescorekeeper.data.Game
import com.mani.carcassonnescorekeeper.ui.AppScaffold
import com.mani.carcassonnescorekeeper.ui.AppViewModel
import com.mani.carcassonnescorekeeper.ui.MeepleToken
import com.mani.carcassonnescorekeeper.ui.Screen
import com.mani.carcassonnescorekeeper.ui.deltaColor
import com.mani.carcassonnescorekeeper.ui.formatDateTime
import com.mani.carcassonnescorekeeper.ui.formatDelta
import com.mani.carcassonnescorekeeper.ui.durationText
import com.mani.carcassonnescorekeeper.ui.formatTime
import com.mani.carcassonnescorekeeper.ui.joinNames
import com.mani.carcassonnescorekeeper.ui.theme.BronzeColor
import com.mani.carcassonnescorekeeper.ui.theme.GoldColor
import com.mani.carcassonnescorekeeper.ui.theme.SilverColor

@Composable
fun GameDetailScreen(vm: AppViewModel, gameId: Long, justFinished: Boolean) {
    val game = vm.history.firstOrNull { it.id == gameId }
    // Null only while this screen fades out after the game was finished or deleted.
    if (game == null) return
    var confirmDelete by remember { mutableStateOf(false) }

    AppScaffold(
        title = if (justFinished) stringResource(R.string.final_scores) else formatDateTime(game.startedAt),
        onBack = vm::back,
        actions = {
            IconButton(onClick = { confirmDelete = true }) {
                Icon(painterResource(R.drawable.ic_delete), contentDescription = stringResource(R.string.delete_game))
            }
        },
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(padding),
        ) {
            item { WinnerBanner(game) }
            item { Standings(game) }
            item {
                val finished = game.finishedAt ?: game.startedAt
                Text(
                    stringResource(
                        R.string.game_meta,
                        formatDateTime(game.startedAt),
                        durationText(finished - game.startedAt),
                        pluralStringResource(R.plurals.entries, game.events.size, game.events.size),
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (justFinished) {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = vm::back, modifier = Modifier.weight(1f).height(52.dp)) { Text(stringResource(R.string.home)) }
                        Button(
                            onClick = { vm.back(); vm.navigate(Screen.Setup) },
                            modifier = Modifier.weight(1f).height(52.dp),
                        ) { Text(stringResource(R.string.play_again)) }
                    }
                }
            }
            item {
                Text(
                    stringResource(R.string.score_log),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            items(game.events) { event ->
                val player = game.players[event.player]
                Column {
                    Row(Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            formatTime(event.at),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(72.dp),
                        )
                        MeepleToken(player.argb, size = 24.dp)
                        Text(
                            player.name,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f).padding(start = 10.dp),
                        )
                        Text(
                            formatDelta(event.delta),
                            fontWeight = FontWeight.Bold,
                            color = deltaColor(event.delta),
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.delete_game_title)) },
            text = { Text(stringResource(R.string.delete_game_body)) },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; vm.deleteGame(game.id) }) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text(stringResource(R.string.cancel)) } },
        )
    }
}

@Composable
private fun WinnerBanner(game: Game) {
    val winners = game.winners.map { game.players[it].name }
    val top = game.scores.maxOrNull() ?: 0
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painterResource(R.drawable.ic_trophy),
                contentDescription = null,
                tint = GoldColor,
                modifier = Modifier.size(56.dp),
            )
            Text(
                if (winners.size == 1) stringResource(R.string.winner_single, winners[0]) else stringResource(R.string.winner_tie, joinNames(winners)),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp),
            )
            Text(pluralStringResource(R.plurals.points, top, top), style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun Standings(game: Game) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(vertical = 8.dp)) {
            game.ranking.forEach { i ->
                val rank = game.rankOf(i)
                val player = game.players[i]
                Row(
                    Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "$rank",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = when (rank) {
                            1 -> GoldColor
                            2 -> SilverColor
                            3 -> BronzeColor
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.width(28.dp),
                    )
                    MeepleToken(player.argb, size = 32.dp)
                    Text(
                        player.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f).padding(start = 12.dp),
                    )
                    Text("${game.scores[i]}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
