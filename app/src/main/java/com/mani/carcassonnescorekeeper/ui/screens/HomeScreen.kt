package com.mani.carcassonnescorekeeper.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.mani.carcassonnescorekeeper.data.leaderboard
import com.mani.carcassonnescorekeeper.ui.AppScaffold
import com.mani.carcassonnescorekeeper.ui.AppViewModel
import com.mani.carcassonnescorekeeper.ui.MeepleToken
import com.mani.carcassonnescorekeeper.ui.Screen
import com.mani.carcassonnescorekeeper.ui.formatDateTime
import com.mani.carcassonnescorekeeper.ui.theme.GoldColor

@Composable
fun HomeScreen(vm: AppViewModel) {
    val game = vm.currentGame
    var confirmDiscard by remember { mutableStateOf<(() -> Unit)?>(null) }

    AppScaffold(
        title = stringResource(R.string.app_name),
        onBack = null,
        actions = {
            IconButton(onClick = { vm.navigate(Screen.Settings) }) {
                Icon(painterResource(R.drawable.ic_settings), contentDescription = stringResource(R.string.settings))
            }
        },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (game != null) {
                GameInProgressCard(
                    game = game,
                    onResume = { vm.navigate(Screen.Scoring) },
                    onDiscard = { confirmDiscard = { vm.discardGame() } },
                )
                OutlinedButton(
                    onClick = { confirmDiscard = { vm.discardGame(); vm.navigate(Screen.Setup) } },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(stringResource(R.string.start_new_instead)) }
            } else {
                NewGameCard(onNewGame = { vm.navigate(Screen.Setup) })
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NavCard(
                    iconRes = R.drawable.ic_trophy,
                    title = stringResource(R.string.leaderboard),
                    subtitle = stringResource(R.string.leaderboard_subtitle),
                    onClick = { vm.navigate(Screen.Leaderboard) },
                    modifier = Modifier.weight(1f),
                )
                NavCard(
                    iconRes = R.drawable.ic_history,
                    title = stringResource(R.string.history),
                    subtitle = vm.history.size.let { n ->
                        if (n == 0) stringResource(R.string.history_none) else pluralStringResource(R.plurals.games, n, n)
                    },
                    onClick = { vm.navigate(Screen.History) },
                    modifier = Modifier.weight(1f),
                )
            }

            TopPlayers(vm)
        }
    }

    confirmDiscard?.let { onConfirm ->
        AlertDialog(
            onDismissRequest = { confirmDiscard = null },
            title = { Text(stringResource(R.string.discard_title)) },
            text = { Text(stringResource(R.string.discard_body)) },
            confirmButton = {
                TextButton(onClick = { confirmDiscard = null; onConfirm() }) {
                    Text(stringResource(R.string.discard), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { confirmDiscard = null }) { Text(stringResource(R.string.cancel)) } },
        )
    }
}

@Composable
private fun NewGameCard(onNewGame: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(20.dp)) {
            Icon(
                painterResource(R.drawable.ic_meeple),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp),
            )
            Text(
                stringResource(R.string.home_ready_title),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(top = 12.dp),
            )
            Text(
                stringResource(R.string.home_ready_body),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp),
            )
            Button(
                onClick = onNewGame,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp).height(52.dp),
            ) { Text(stringResource(R.string.new_game), style = MaterialTheme.typography.titleMedium) }
        }
    }
}

@Composable
private fun GameInProgressCard(game: Game, onResume: () -> Unit, onDiscard: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(stringResource(R.string.game_in_progress), style = MaterialTheme.typography.headlineSmall)
            Text(
                stringResource(R.string.started_at, formatDateTime(game.startedAt)),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 2.dp, bottom = 12.dp),
            )
            game.players.forEachIndexed { i, player ->
                Row(Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    MeepleToken(player.argb, size = 28.dp)
                    Text(
                        player.name,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f).padding(start = 12.dp),
                    )
                    Text("${game.scores[i]}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
            Row(Modifier.padding(top = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onDiscard) { Text(stringResource(R.string.discard)) }
                Spacer(Modifier.weight(1f))
                Button(onClick = onResume, modifier = Modifier.height(52.dp)) {
                    Text(stringResource(R.string.resume_game), style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
private fun NavCard(iconRes: Int, title: String, subtitle: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(onClick = onClick, modifier = modifier) {
        Column(Modifier.padding(16.dp)) {
            Icon(painterResource(iconRes), contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
            Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun TopPlayers(vm: AppViewModel) {
    val top = remember(vm.history) { leaderboard(vm.history).filter { it.wins > 0 }.take(3) }
    if (top.isEmpty()) return
    Card(onClick = { vm.navigate(Screen.Leaderboard) }, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(stringResource(R.string.top_players), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.secondary)
            top.forEach { stats ->
                Row(Modifier.padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painterResource(R.drawable.ic_trophy),
                        contentDescription = null,
                        tint = GoldColor,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(stats.name, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    Text(
                        pluralStringResource(R.plurals.wins, stats.wins, stats.wins),
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
            }
        }
    }
}
