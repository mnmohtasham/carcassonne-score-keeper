package com.mani.carcassonnescorekeeper.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.mani.carcassonnescorekeeper.BuildConfig
import com.mani.carcassonnescorekeeper.R
import com.mani.carcassonnescorekeeper.ui.AppScaffold
import com.mani.carcassonnescorekeeper.ui.AppViewModel

private const val DISABLED_ALPHA = 0.38f

@Composable
fun SettingsScreen(vm: AppViewModel) {
    var confirmClear by remember { mutableStateOf(false) }
    val listColors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background)
    val hasHistory = vm.history.isNotEmpty()
    val uriHandler = LocalUriHandler.current

    AppScaffold(title = stringResource(R.string.settings), onBack = vm::back) { padding ->
        Column(Modifier.padding(padding).verticalScroll(rememberScrollState())) {
            ListItem(
                headlineContent = { Text(stringResource(R.string.keep_awake)) },
                supportingContent = { Text(stringResource(R.string.keep_awake_summary)) },
                // The whole row toggles; the switch itself is decorative for accessibility.
                trailingContent = { Switch(checked = vm.keepScreenAwake, onCheckedChange = null) },
                colors = listColors,
                modifier = Modifier.toggleable(
                    value = vm.keepScreenAwake,
                    role = Role.Switch,
                    onValueChange = vm::updateKeepScreenAwake,
                ),
            )
            HorizontalDivider()
            val clearAlpha = if (hasHistory) 1f else DISABLED_ALPHA
            ListItem(
                headlineContent = {
                    Text(
                        stringResource(R.string.clear_history),
                        color = MaterialTheme.colorScheme.error.copy(alpha = clearAlpha),
                    )
                },
                supportingContent = {
                    Text(
                        stringResource(R.string.clear_history_summary),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = clearAlpha),
                    )
                },
                colors = listColors,
                modifier = Modifier.clickable(enabled = hasHistory, role = Role.Button) { confirmClear = true },
            )
            HorizontalDivider()
            Text(
                stringResource(R.string.about),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp),
            )
            if (BuildConfig.PRIVACY_POLICY_URL.isNotEmpty()) {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.privacy_policy)) },
                    supportingContent = { Text(stringResource(R.string.privacy_policy_summary)) },
                    colors = listColors,
                    modifier = Modifier.clickable(role = Role.Button) {
                        // No browser installed is the only failure; nothing useful to show then.
                        runCatching { uriHandler.openUri(BuildConfig.PRIVACY_POLICY_URL) }
                    },
                )
            }
            ListItem(
                headlineContent = { Text(stringResource(R.string.version)) },
                supportingContent = { Text(BuildConfig.VERSION_NAME) },
                colors = listColors,
            )
            Text(
                stringResource(R.string.unofficial_disclaimer),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp),
            )
        }
    }

    if (confirmClear) {
        val count = vm.history.size
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text(stringResource(R.string.clear_history_title)) },
            text = { Text(pluralStringResource(R.plurals.clear_history_body, count, count)) },
            confirmButton = {
                TextButton(onClick = { confirmClear = false; vm.clearHistory() }) {
                    Text(stringResource(R.string.delete_all), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { confirmClear = false }) { Text(stringResource(R.string.cancel)) } },
        )
    }
}
