package com.mani.carcassonnescorekeeper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mani.carcassonnescorekeeper.ui.AppViewModel
import com.mani.carcassonnescorekeeper.ui.Screen
import com.mani.carcassonnescorekeeper.ui.screens.GameDetailScreen
import com.mani.carcassonnescorekeeper.ui.screens.HistoryScreen
import com.mani.carcassonnescorekeeper.ui.screens.HomeScreen
import com.mani.carcassonnescorekeeper.ui.screens.LeaderboardScreen
import com.mani.carcassonnescorekeeper.ui.screens.ScoringScreen
import com.mani.carcassonnescorekeeper.ui.screens.SettingsScreen
import com.mani.carcassonnescorekeeper.ui.screens.SetupScreen
import com.mani.carcassonnescorekeeper.ui.theme.CarcassonneTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CarcassonneTheme {
                App()
            }
        }
    }
}

@Composable
private fun App(vm: AppViewModel = viewModel()) {
    val view = LocalView.current
    DisposableEffect(vm.keepScreenAwake) {
        view.keepScreenOn = vm.keepScreenAwake
        onDispose { view.keepScreenOn = false }
    }
    BackHandler(enabled = vm.canGoBack) { vm.back() }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Crossfade(targetState = vm.screen, label = "screen") { screen ->
            when (screen) {
                Screen.Home -> HomeScreen(vm)
                Screen.Setup -> SetupScreen(vm)
                Screen.Scoring -> ScoringScreen(vm)
                is Screen.GameDetail -> GameDetailScreen(vm, screen.gameId, screen.justFinished)
                Screen.History -> HistoryScreen(vm)
                Screen.Leaderboard -> LeaderboardScreen(vm)
                Screen.Settings -> SettingsScreen(vm)
            }
        }
    }
}
