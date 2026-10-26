package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.GameViewModel
import androidx.activity.compose.BackHandler
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    viewModel: GameViewModel,
    onNavigateBack: () -> Unit
) {
    BackHandler { onNavigateBack() }
    val topScores by viewModel.topScores.collectAsState()
    val isRussian = viewModel.isRussian

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isRussian) "Таблица рекордов" else "High Scores",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE6E1E5)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("leaderboard_back")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFFE6E1E5)
                        )
                    }
                },
                actions = {
                    if (topScores.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.clearScores() },
                            modifier = Modifier.testTag("leaderboard_clear")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Clear all scores",
                                tint = Color(0xFFF2B8B5)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1C1B1F),
                    titleContentColor = Color(0xFFE6E1E5)
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1C1B1F))
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (topScores.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Color.White.copy(alpha = 0.12f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (isRussian) "Рекордов пока нет.\nСыграйте игру чтобы установить рекорд!" else "No scores yet.\nPlay a game to make history!",
                                textAlign = TextAlign.Center,
                                color = Color(0xFFCAC4D0),
                                fontSize = 16.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .widthIn(max = 600.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        itemsIndexed(topScores) { index, item ->
                            val rank = index + 1
                            val rankColor = when (rank) {
                                1 -> Color(0xFFFFF176) // Gold
                                2 -> Color(0xFFCFD8DC) // Silver
                                3 -> Color(0xFFFFB74D) // Bronze
                                else -> Color(0xFFCAC4D0).copy(alpha = 0.8f)
                            }

                            Card(
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        width = if (rank <= 3) 1.5.dp else 1.dp,
                                        color = if (rank <= 3) rankColor.copy(alpha = 0.5f) else Color(0xFF49454F),
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFF2B2930)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        // Rank visual indicator
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(rankColor.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "#$rank",
                                                color = rankColor,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(16.dp))

                                        Column {
                                            Text(
                                                text = item.playerName,
                                                color = Color(0xFFE6E1E5),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp
                                            )
                                            val dateFormatted = remember(item.date) {
                                                try {
                                                    val df = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                                                    df.format(Date(item.date))
                                                } catch (e: Exception) {
                                                    ""
                                                }
                                            }
                                            Text(
                                                text = dateFormatted,
                                                color = Color(0xFFCAC4D0),
                                                fontSize = 12.sp
                                            )
                                        }
                                    }

                                    Text(
                                        text = "${item.score}",
                                        color = Color(0xFFD0BCFF),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 20.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

