package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.GameViewModel

@Composable
fun MenuScreen(
    viewModel: GameViewModel,
    onNavigateToGame: () -> Unit,
    onNavigateToLeaderboard: () -> Unit
) {
    var showHowToPlay by remember { mutableStateOf(false) }

    // Pulsing animation for the title
    val infiniteTransition = rememberInfiniteTransition(label = "title_pulse")
    val scaleFactor by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1C1B1F))
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.widthIn(max = 500.dp)
        ) {
            // Cool visual game logo block
            Box(
                modifier = Modifier
                    .scale(scaleFactor)
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.5.dp, Color(0xFF49454F), RoundedCornerShape(24.dp))
                    .background(Color(0xFF2B2930))
                    .padding(horizontal = 32.dp, vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "TETRI",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 4.sp,
                            color = Color(0xFFD0BCFF)
                        )
                    )
                    Text(
                        text = "CROSS",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 8.sp,
                            color = Color(0xFFE6E1E5)
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (viewModel.isRussian) "ТЕТРИС × КРОССВОРД" else "TETRIS × CROSSWORD",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = Color(0xFFCAC4D0),
                            letterSpacing = 2.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Play Game Button
            Button(
                onClick = onNavigateToGame,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .testTag("play_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD0BCFF),
                    contentColor = Color(0xFF381E72)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (viewModel.isRussian) "НАЧАТЬ ИГРУ" else "START GAME",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
            }

            // Language Switcher Selector row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF2B2930))
                    .border(1.dp, Color(0xFF49454F), RoundedCornerShape(16.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (viewModel.isRussian) Color(0xFFD0BCFF) else Color.Transparent)
                        .clickable { if (!viewModel.isRussian) viewModel.toggleLanguage() }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "РУССКИЙ",
                        color = if (viewModel.isRussian) Color(0xFF381E72) else Color(0xFFCAC4D0),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (!viewModel.isRussian) Color(0xFFD0BCFF) else Color.Transparent)
                        .clickable { if (viewModel.isRussian) viewModel.toggleLanguage() }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "ENGLISH",
                        color = if (!viewModel.isRussian) Color(0xFF381E72) else Color(0xFFCAC4D0),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            // Leaderboard Button
            OutlinedButton(
                onClick = onNavigateToLeaderboard,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("leaderboard_button"),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFFD0BCFF)
                ),
                border = BorderStroke(1.dp, Color(0xFF49454F)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.List,
                    contentDescription = "Leaderboard",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (viewModel.isRussian) "РЕКОРДЫ" else "LEADERBOARD",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            // Instructions Button
            Button(
                onClick = { showHowToPlay = !showHowToPlay },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("instructions_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2B2930),
                    contentColor = Color(0xFFE6E1E5)
                ),
                border = BorderStroke(1.dp, Color(0xFF49454F)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Rules",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (viewModel.isRussian) "ПРАВИЛА ИГРЫ" else "RULES / TUTORIAL",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Normal
                    )
                )
            }

            // Instruction expanded details
            AnimatedVisibility(visible = showHowToPlay) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF49454F), RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2B2930)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (viewModel.isRussian) "Как играть:" else "How to Play:",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD0BCFF)
                            )
                        )
                        Text(
                            text = if (viewModel.isRussian) {
                                "1. 🧱 Тактический Тетрис: управляйте фигурами кнопками или жестами по полю. Используйте «🔀 Сдвиг букв» для перестановки букв внутри фигуры и «📥 Карман» для запаса!\n" +
                                "2. 🎯 Слово Охоты и ★ Джокер: соберите целевое слово прямо во время падения фигур для мгновенного взрыва (+250 очков и бесплатная подсказка). Золотая звезда ★ заменяет любую букву!\n" +
                                "3. 🎁 Слово в подарок: впишите своё слово из словаря, чтобы получить очки и загрузить его буквы в следующие фигуры.\n" +
                                "4. 🧩 Режим Кроссворда: открывайте по 1 букве (💡), включайте «📡 Радар» для подсветки координат слова на поле или отгадывайте слово целиком!"
                            } else {
                                "1. 🧱 Tactical Tetris: Move pieces via buttons or board gestures. Use '🔀 Shift ABC' to cycle letters inside the falling piece and '📥 Hold' to save a piece!\n" +
                                "2. 🎯 Quest Word & ★ Joker: Assemble the target Quest Word live during Tetris for an instant resonance clear (+250 pts & free hint). Golden ★ matches any letter!\n" +
                                "3. 🎁 Gift Word: Type any valid dictionary word to earn bonus points and preload its letters into upcoming pieces.\n" +
                                "4. 🧩 Crossword Mode: Reveal single letters (💡), activate '📡 Radar' to highlight word cells on the board, or solve clues to lower the stack!"
                            },
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color(0xFFE6E1E5),
                                lineHeight = 20.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

