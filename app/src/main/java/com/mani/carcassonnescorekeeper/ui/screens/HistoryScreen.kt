package com.mani.carcassonnescorekeeper.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mani.carcassonnescorekeeper.R
import com.mani.carcassonnescorekeeper.data.Game
import com.mani.carcassonnescorekeeper.ui.AppScaffold
import com.mani.carcassonnescorekeeper.ui.AppViewModel
import com.mani.carcassonnescorekeeper.ui.EmptyState
import com.mani.carcassonnescorekeeper.ui.Screen
import com.mani.carcassonnescorekeeper.ui.formatDateTime
import com.mani.carcassonnescorekeeper.ui.durationText
import com.mani.carcassonnescorekeeper.ui.joinNames
import com.mani.carcassonnescorekeeper.ui.theme.GoldColor

@Composable
fun HistoryScreen(vm: AppViewModel) {
    AppScaffold(title = stringResource(R.string.history_title), onBack = vm::back) { padding ->
        if (vm.history.isEmpty()) {
            EmptyState(
                iconRes = R.drawable.ic_history,
                title = stringResource(R.string.history_empty_title),
                message = stringResource(R.string.history_empty_body),
                modifier = Modifier.padding(padding),
            )
            return@AppScaffold
        }
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(padding),
        ) {
            items(vm.history, key = { it.id }) { game ->
                HistoryCard(game, onClick = { vm.navigate(Screen.GameDetail(game.id)) })
            }
        }
    }
}

@Composable
private fun HistoryCard(game: Game, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    formatDateTime(game.startedAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                game.finishedAt?.let {
                    Text(
                        durationText(it - game.startedAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Row(Modifier.padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painterResource(R.drawable.ic_trophy),
                    contentDescription = null,
                    tint = GoldColor,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    joinNames(game.winners.map { game.players[it].name }),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).padding(start = 8.dp),
                )
                Text("${game.scores.maxOrNull() ?: 0}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Text(
                game.ranking.joinToString(" · ") { "${game.players[it].name} ${game.scores[it]}" },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}
