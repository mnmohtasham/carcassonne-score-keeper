package com.mani.carcassonnescorekeeper.ui.screens

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mani.carcassonnescorekeeper.R
import com.mani.carcassonnescorekeeper.data.Player
import com.mani.carcassonnescorekeeper.data.ScoreEvent
import com.mani.carcassonnescorekeeper.ui.AppScaffold
import com.mani.carcassonnescorekeeper.ui.AppViewModel
import com.mani.carcassonnescorekeeper.ui.MeepleToken
import com.mani.carcassonnescorekeeper.ui.deltaColor
import com.mani.carcassonnescorekeeper.ui.formatDelta
import com.mani.carcassonnescorekeeper.ui.formatTime
import com.mani.carcassonnescorekeeper.ui.joinNames
import com.mani.carcassonnescorekeeper.ui.theme.GoldColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** How long the "+3" badge stays next to a score after it changes. */
private const val FLASH_MS = 2_500L

private const val MAX_DIGITS = 4
private const val KEY_SIGN = "sign"
private const val KEY_BACKSPACE = "backspace"

@Composable
fun ScoringScreen(vm: AppViewModel) {
    val game = vm.currentGame
    // Null only while this screen fades out after the game was finished or discarded.
    if (game == null) return
    val view = LocalView.current
    val resources = LocalResources.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    var keypadFor by remember { mutableStateOf<Int?>(null) }
    var confirmFinish by remember { mutableStateOf(false) }

    val scores = game.scores
    val topScore = scores.maxOrNull() ?: 0

    // Briefly show the latest change next to that player's score.
    val lastEvent = game.events.lastOrNull()
    var flash by remember { mutableStateOf<ScoreEvent?>(null) }
    LaunchedEffect(lastEvent) {
        if (lastEvent != null && System.currentTimeMillis() - lastEvent.at < FLASH_MS) {
            flash = lastEvent
            delay(FLASH_MS)
        }
        flash = null
    }

    fun removeWithUndo(index: Int) {
        val removed = vm.removeEvent(index) ?: return
        val message = resources.getString(
            R.string.removed_entry,
            game.players[removed.player].name,
            formatDelta(removed.delta),
        )
        scope.launch {
            snackbar.currentSnackbarData?.dismiss()
            val result = snackbar.showSnackbar(
                message = message,
                actionLabel = resources.getString(R.string.undo),
                duration = SnackbarDuration.Short,
            )
            if (result == SnackbarResult.ActionPerformed) vm.restoreEvent(index, removed)
        }
    }

    AppScaffold(
        title = stringResource(R.string.scoring_title),
        onBack = vm::back,
        snackbarHostState = snackbar,
        actions = {
            IconButton(onClick = { removeWithUndo(game.events.lastIndex) }, enabled = game.events.isNotEmpty()) {
                Icon(painterResource(R.drawable.ic_undo), contentDescription = stringResource(R.string.undo_last))
            }
            TextButton(onClick = { confirmFinish = true }) {
                Icon(painterResource(R.drawable.ic_flag), contentDescription = null, modifier = Modifier.size(18.dp))
                Text(stringResource(R.string.finish), modifier = Modifier.padding(start = 6.dp))
            }
        },
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(padding),
        ) {
            itemsIndexed(game.players, key = { i, _ -> "player$i" }) { i, player ->
                PlayerScoreCard(
                    player = player,
                    score = scores[i],
                    leading = topScore > 0 && scores[i] == topScore,
                    flashDelta = flash?.takeIf { it.player == i }?.delta,
                    onPlusOne = {
                        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                        vm.tapPoint(i)
                    },
                    onKeypad = { keypadFor = i },
                )
            }

            item(key = "logHeader") {
                Text(
                    stringResource(R.string.score_log),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }
            if (game.events.isEmpty()) {
                item(key = "logEmpty") {
                    Text(
                        stringResource(R.string.scoring_empty_log),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            val newestFirst = game.events.withIndex().reversed()
            items(newestFirst, key = { "event${it.index}-${it.value.at}" }) { (index, event) ->
                LogRow(
                    player = game.players[event.player],
                    event = event,
                    onRemove = { removeWithUndo(index) },
                )
            }
        }
    }

    keypadFor?.let { i ->
        ScoreKeypadSheet(
            player = game.players[i],
            currentScore = scores[i],
            onDismiss = { keypadFor = null },
            onSubmit = { vm.addPoints(i, it) },
        )
    }

    if (confirmFinish) {
        if (game.events.isEmpty()) {
            AlertDialog(
                onDismissRequest = { confirmFinish = false },
                title = { Text(stringResource(R.string.no_points_title)) },
                text = { Text(stringResource(R.string.no_points_body)) },
                confirmButton = {
                    TextButton(onClick = { confirmFinish = false; vm.discardGame() }) {
                        Text(stringResource(R.string.discard), color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { confirmFinish = false }) { Text(stringResource(R.string.keep_playing)) }
                },
            )
        } else {
            val winners = game.winners.map { game.players[it].name }
            AlertDialog(
                onDismissRequest = { confirmFinish = false },
                icon = { Icon(painterResource(R.drawable.ic_trophy), contentDescription = null, tint = GoldColor) },
                title = { Text(stringResource(R.string.finish_title)) },
                text = {
                    Text(
                        if (winners.size == 1) pluralStringResource(R.plurals.finish_winner, topScore, winners[0], topScore)
                        else pluralStringResource(R.plurals.finish_tie, topScore, joinNames(winners), topScore),
                    )
                },
                confirmButton = {
                    TextButton(onClick = { confirmFinish = false; vm.finishGame() }) {
                        Text(stringResource(R.string.finish_save))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { confirmFinish = false }) { Text(stringResource(R.string.keep_playing)) }
                },
            )
        }
    }
}

@Composable
private fun PlayerScoreCard(
    player: Player,
    score: Int,
    leading: Boolean,
    flashDelta: Int?,
    onPlusOne: () -> Unit,
    onKeypad: () -> Unit,
) {
    // Keeps the badge text while it fades out.
    val lastDelta = remember { IntArray(1) }
    if (flashDelta != null) lastDelta[0] = flashDelta
    val shownDelta = flashDelta ?: lastDelta[0]
    val plusOneDescription = stringResource(R.string.add_one_to, player.name)

    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.height(IntrinsicSize.Min)) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .width(6.dp)
                    .background(Color(player.argb)),
            )
            Row(
                Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MeepleToken(player.argb, size = 40.dp)
                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            player.name,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        if (leading) {
                            Icon(
                                painterResource(R.drawable.ic_trophy),
                                contentDescription = stringResource(R.string.leading),
                                tint = GoldColor,
                                modifier = Modifier.padding(start = 6.dp).size(18.dp),
                            )
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "$score",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                        )
                        AnimatedVisibility(
                            visible = flashDelta != null,
                            enter = fadeIn() + slideInVertically { it / 2 },
                            exit = fadeOut(),
                        ) {
                            Text(
                                formatDelta(shownDelta),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = deltaColor(shownDelta),
                                maxLines = 1,
                                modifier = Modifier.padding(start = 10.dp),
                            )
                        }
                    }
                }
                FilledTonalIconButton(onClick = onKeypad, modifier = Modifier.size(52.dp)) {
                    Icon(
                        painterResource(R.drawable.ic_dialpad),
                        contentDescription = stringResource(R.string.type_points_for, player.name),
                    )
                }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = onPlusOne,
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier
                        .size(width = 76.dp, height = 64.dp)
                        .semantics { contentDescription = plusOneDescription },
                ) {
                    Text(
                        stringResource(R.string.plus_one),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun LogRow(player: Player, event: ScoreEvent, onRemove: () -> Unit) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            MeepleToken(player.argb, size = 26.dp)
            Text(
                player.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f).padding(start = 12.dp),
            )
            Text(
                formatDelta(event.delta),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = deltaColor(event.delta),
            )
            Text(
                formatTime(event.at),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 12.dp),
            )
            IconButton(onClick = onRemove) {
                Icon(
                    painterResource(R.drawable.ic_close),
                    contentDescription = stringResource(R.string.remove_entry),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScoreKeypadSheet(
    player: Player,
    currentScore: Int,
    onDismiss: () -> Unit,
    onSubmit: (Int) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var digits by remember { mutableStateOf("") }
    var negative by remember { mutableStateOf(false) }
    val magnitude = digits.toIntOrNull() ?: 0
    val amount = if (negative) -magnitude else magnitude
    // The sheet stays tappable while it animates closed; never submit twice.
    var submitted by remember { mutableStateOf(false) }

    fun submit() {
        if (submitted || amount == 0) return
        submitted = true
        onSubmit(amount)
        scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
    }

    fun press(key: String) {
        when (key) {
            KEY_SIGN -> negative = !negative
            KEY_BACKSPACE -> digits = digits.dropLast(1)
            else -> if (digits.length < MAX_DIGITS && !(digits.isEmpty() && key == "0")) digits += key
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        // Scrollable so the Add button stays reachable on short or landscape screens.
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                MeepleToken(player.argb, size = 40.dp)
                Column(Modifier.padding(start = 12.dp)) {
                    Text(player.name, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        stringResource(R.string.current_score, currentScore),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            ) {
                Row(Modifier.padding(horizontal = 20.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        (if (negative) "−" else "+") + digits.ifEmpty { "0" },
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            digits.isEmpty() -> MaterialTheme.colorScheme.outline
                            negative -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurface
                        },
                        maxLines = 1,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        "→ ${currentScore + amount}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }

            val rows = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf(KEY_SIGN, "0", KEY_BACKSPACE),
            )
            rows.forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 8.dp)) {
                    row.forEach { key ->
                        FilledTonalButton(
                            onClick = { press(key) },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f).height(60.dp),
                        ) {
                            when (key) {
                                KEY_BACKSPACE -> Icon(
                                    painterResource(R.drawable.ic_backspace),
                                    contentDescription = stringResource(R.string.delete_digit),
                                )
                                KEY_SIGN -> {
                                    val toggleDescription = stringResource(R.string.toggle_sign)
                                    Text(
                                        if (negative) "+ / −" else "− / +",
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.semantics { contentDescription = toggleDescription },
                                    )
                                }
                                else -> Text(key, style = MaterialTheme.typography.headlineSmall)
                            }
                        }
                    }
                }
            }

            Button(
                onClick = ::submit,
                enabled = amount != 0 && !submitted,
                colors = if (negative) {
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                    )
                } else {
                    ButtonDefaults.buttonColors()
                },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(56.dp),
            ) {
                Text(
                    when {
                        amount > 0 -> pluralStringResource(R.plurals.add_points, amount, amount)
                        amount < 0 -> pluralStringResource(R.plurals.deduct_points, -amount, -amount)
                        else -> stringResource(R.string.enter_points)
                    },
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }
}
