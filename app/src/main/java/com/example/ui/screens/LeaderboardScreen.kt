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
    val isPaper = viewModel.isPaperTheme
    val screenBg = if (isPaper) Color(0xFFFBF7EE) else Color(0xFF1C1B1F)
    val topBarBg = if (isPaper) Color(0xFFF3EBD8) else Color(0xFF1C1B1F)
    val cardBg = if (isPaper) Color(0xFFFFFDF9) else Color(0xFF2B2930)
    val borderCol = if (isPaper) Color(0xFFD7CCC8) else Color(0xFF49454F)
    val textPrimary = if (isPaper) Color(0xFF2C1E14) else Color(0xFFE6E1E5)
    val textSecondary = if (isPaper) Color(0xFF5D4037) else Color(0xFFCAC4D0)
    val accentPrimary = if (isPaper) Color(0xFF1B365D) else Color(0xFFD0BCFF)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isRussian) "Таблица рекордов" else "High Scores",
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
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
                            tint = textPrimary
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
                                tint = if (isPaper) Color(0xFFC62828) else Color(0xFFF2B8B5)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = topBarBg,
                    titleContentColor = textPrimary
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(screenBg)
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
                                tint = textSecondary.copy(alpha = 0.25f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (isRussian) "Рекордов пока нет.\nСыграйте игру чтобы установить рекорд!" else "No scores yet.\nPlay a game to make history!",
                                textAlign = TextAlign.Center,
                                color = textSecondary,
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
                                1 -> if (isPaper) Color(0xFFB45309) else Color(0xFFFFF176) // Gold
                                2 -> if (isPaper) Color(0xFF455A64) else Color(0xFFCFD8DC) // Silver
                                3 -> if (isPaper) Color(0xFFD84315) else Color(0xFFFFB74D) // Bronze
                                else -> textSecondary.copy(alpha = 0.8f)
                            }

                            Card(
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        width = if (rank <= 3) 1.5.dp else 1.dp,
                                        color = if (rank <= 3) rankColor.copy(alpha = 0.5f) else borderCol,
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                colors = CardDefaults.cardColors(
                                    containerColor = cardBg
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
                                                color = textPrimary,
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
                                                color = textSecondary,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }

                                    Text(
                                        text = "${item.score}",
                                        color = accentPrimary,
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

