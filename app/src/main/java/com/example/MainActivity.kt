package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.game.GameViewModel
import com.example.ui.screens.GameScreen
import com.example.ui.screens.LeaderboardScreen
import com.example.ui.screens.MenuScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                // Shared view model to retain progress or trigger operations
                val gameViewModel: GameViewModel = viewModel()
                var currentScreen by rememberSaveable { mutableStateOf("MENU") }

                when (currentScreen) {
                    "MENU" -> {
                        MenuScreen(
                            viewModel = gameViewModel,
                            onNavigateToGame = { currentScreen = "GAME" },
                            onNavigateToLeaderboard = { currentScreen = "LEADERBOARD" }
                        )
                    }
                    "GAME" -> {
                        GameScreen(
                            viewModel = gameViewModel,
                            onNavigateBack = { currentScreen = "MENU" }
                        )
                    }
                    "LEADERBOARD" -> {
                        LeaderboardScreen(
                            viewModel = gameViewModel,
                            onNavigateBack = { currentScreen = "MENU" }
                        )
                    }
                }
            }
        }
    }
}
