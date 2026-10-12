package com.mani.carcassonnescorekeeper.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mani.carcassonnescorekeeper.R
import com.mani.carcassonnescorekeeper.data.PlayerStats
import com.mani.carcassonnescorekeeper.data.leaderboard
import com.mani.carcassonnescorekeeper.ui.AppScaffold
import com.mani.carcassonnescorekeeper.ui.AppViewModel
import com.mani.carcassonnescorekeeper.ui.EmptyState
import com.mani.carcassonnescorekeeper.ui.theme.BronzeColor
import com.mani.carcassonnescorekeeper.ui.theme.GoldColor
import com.mani.carcassonnescorekeeper.ui.theme.SilverColor

@Composable
fun LeaderboardScreen(vm: AppViewModel) {
    val stats = remember(vm.history) { leaderboard(vm.history) }

    AppScaffold(title = stringResource(R.string.leaderboard_title), onBack = vm::back) { padding ->
        if (stats.isEmpty()) {
            EmptyState(
                iconRes = R.drawable.ic_trophy,
                title = stringResource(R.string.leaderboard_empty_title),
                message = stringResource(R.string.leaderboard_empty_body),
                modifier = Modifier.padding(padding),
            )
            return@AppScaffold
        }
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(padding),
        ) {
            item {
                Text(
                    pluralStringResource(R.plurals.leaderboard_summary, vm.history.size, vm.history.size),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            items(stats, key = { it.name.lowercase() }) { s ->
                // Players with the same number of wins share a rank.
                LeaderRow(rank = 1 + stats.count { it.wins > s.wins }, stats = s)
            }
        }
    }
}

@Composable
private fun LeaderRow(rank: Int, stats: PlayerStats) {
    val medal = when {
        stats.wins == 0 -> null
        rank == 1 -> GoldColor
        rank == 2 -> SilverColor
        rank == 3 -> BronzeColor
        else -> null
    }
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            val rankDescription = stringResource(R.string.rank, rank)
            Box(
                Modifier
                    .semantics { contentDescription = rankDescription }
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(medal ?: MaterialTheme.colorScheme.surfaceContainerHighest),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "$rank",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (medal != null) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(Modifier.weight(1f).padding(horizontal = 16.dp)) {
                Text(
                    stats.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    stringResource(
                        R.string.leader_stats,
                        stats.played,
                        Math.round(stats.winRate * 100),
                        stats.bestScore,
                        stats.averageScore,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${stats.wins}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(
                    pluralStringResource(R.plurals.wins_label, stats.wins),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
