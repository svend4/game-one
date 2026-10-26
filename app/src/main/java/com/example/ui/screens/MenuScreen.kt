package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.game.PaceMode
import com.example.game.WordTheme

@Composable
fun MenuScreen(
    viewModel: GameViewModel,
    onNavigateToGame: () -> Unit,
    onNavigateToLeaderboard: () -> Unit
) {
    var showHowToPlay by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    // Pulsing animation for the title
    val infiniteTransition = rememberInfiniteTransition(label = "title_pulse")
    val scaleFactor by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1C1B1F))
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .widthIn(max = 520.dp)
                .verticalScroll(scrollState)
                .padding(vertical = 8.dp)
        ) {
            // Game Logo Block
            Box(
                modifier = Modifier
                    .scale(scaleFactor)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.5.dp, Color(0xFFFFD54F).copy(alpha = 0.6f), RoundedCornerShape(24.dp))
                    .background(Color(0xFF2B2930))
                    .padding(horizontal = 24.dp, vertical = 18.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "TETRI • CROSS",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 3.sp,
                            color = Color(0xFFD0BCFF)
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (viewModel.isRussian) {
                            "1. КРОССВОРД  →  2. ПАЗЛ  →  3. ТЕТРИС-ЛИНИЯ"
                        } else {
                            "1. CROSSWORD  →  2. PUZZLE  →  3. TETRIS LINE"
                        },
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Color(0xFFFFD54F),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Play Game Button
            Button(
                onClick = onNavigateToGame,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
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
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (viewModel.isRussian) "НАЧАТЬ ИГРУ" else "START GAME",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                )
            }

            // --- FAMILY PACE SELECTOR (3 Modes) ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF49454F), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (viewModel.isRussian) {
                            "⏱ Выберите комфортный темп игры:"
                        } else {
                            "⏱ Choose your comfortable pace:"
                        },
                        color = Color(0xFFE6E1E5),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PaceOptionChip(
                            title = if (viewModel.isRussian) "🌿 Пазл" else "🌿 Puzzle",
                            subtitle = if (viewModel.isRussian) "Без таймера" else "No timer",
                            selected = viewModel.paceMode == PaceMode.PUZZLE_STEP,
                            activeColor = Color(0xFF81C784),
                            onClick = { viewModel.updatePaceMode(PaceMode.PUZZLE_STEP) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("menu_pace_puzzle")
                        )
                        PaceOptionChip(
                            title = if (viewModel.isRussian) "🐢 Медленно" else "🐢 Slow",
                            subtitle = if (viewModel.isRussian) "Плавный бриз" else "Calm breeze",
                            selected = viewModel.paceMode == PaceMode.RELAXED,
                            activeColor = Color(0xFF64B5F6),
                            onClick = { viewModel.updatePaceMode(PaceMode.RELAXED) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("menu_pace_relaxed")
                        )
                        PaceOptionChip(
                            title = if (viewModel.isRussian) "⚡ Аркада" else "⚡ Arcade",
                            subtitle = if (viewModel.isRussian) "На скорость" else "Dynamic",
                            selected = viewModel.paceMode == PaceMode.ARCADE,
                            activeColor = Color(0xFFD0BCFF),
                            onClick = { viewModel.updatePaceMode(PaceMode.ARCADE) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("menu_pace_arcade")
                        )
                    }
                }
            }

            // --- THEMATIC COLLECTIONS & FAMILY COMFORT ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF49454F), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (viewModel.isRussian) {
                            "📚 Тема кроссворда и удобства для семьи:"
                        } else {
                            "📚 Crossword Theme & Family Comfort:"
                        },
                        color = Color(0xFFE6E1E5),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )

                    // 2x2 Grid of Word Themes
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ThemeOptionChip(
                            label = if (viewModel.isRussian) "📚 Все темы" else "📚 All Themes",
                            selected = viewModel.selectedTheme == WordTheme.ALL,
                            onClick = { viewModel.selectTheme(WordTheme.ALL) },
                            modifier = Modifier.weight(1f)
                        )
                        ThemeOptionChip(
                            label = if (viewModel.isRussian) "🏡 Дом и Кухня" else "🏡 Home & Food",
                            selected = viewModel.selectedTheme == WordTheme.HOME_COOKING,
                            onClick = { viewModel.selectTheme(WordTheme.HOME_COOKING) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ThemeOptionChip(
                            label = if (viewModel.isRussian) "🌿 Природа" else "🌿 Nature",
                            selected = viewModel.selectedTheme == WordTheme.NATURE_ANIMALS,
                            onClick = { viewModel.selectTheme(WordTheme.NATURE_ANIMALS) },
                            modifier = Modifier.weight(1f)
                        )
                        ThemeOptionChip(
                            label = if (viewModel.isRussian) "🧚 Сказки и Дети" else "🧚 Fairy Tales",
                            selected = viewModel.selectedTheme == WordTheme.FAIRY_TALES_KIDS,
                            onClick = { viewModel.selectTheme(WordTheme.FAIRY_TALES_KIDS) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Kids 1-Tile Mode & Senior Large Font Mode Toggles
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalButton(
                            onClick = { viewModel.toggleSingleTileMode() },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (viewModel.isSingleTileMode) Color(0xFF00E676).copy(alpha = 0.2f) else Color(0xFF1C1B1F),
                                contentColor = if (viewModel.isSingleTileMode) Color(0xFF00E676) else Color(0xFFCAC4D0)
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (viewModel.isSingleTileMode) Color(0xFF00E676) else Color(0xFF49454F)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .testTag("menu_single_tile_toggle")
                        ) {
                            Text(
                                text = if (viewModel.isSingleTileMode) {
                                    if (viewModel.isRussian) "🧒 1-Плитка (Вкл)" else "🧒 1-Tile (ON)"
                                } else {
                                    if (viewModel.isRussian) "🧱 4-Фигуры" else "🧱 4-Blocks"
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }

                        FilledTonalButton(
                            onClick = { viewModel.toggleLargeFontMode() },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (viewModel.isLargeFontMode) Color(0xFFFFD54F).copy(alpha = 0.2f) else Color(0xFF1C1B1F),
                                contentColor = if (viewModel.isLargeFontMode) Color(0xFFFFD54F) else Color(0xFFCAC4D0)
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (viewModel.isLargeFontMode) Color(0xFFFFD54F) else Color(0xFF49454F)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .testTag("menu_large_font_toggle")
                        ) {
                            Text(
                                text = if (viewModel.isLargeFontMode) {
                                    if (viewModel.isRussian) "👓 Крупно (Вкл)" else "👓 Large (ON)"
                                } else {
                                    if (viewModel.isRussian) "👓 Обычный шрифт" else "👓 Normal Font"
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Language Switcher Selector row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF2B2930))
                    .border(1.dp, Color(0xFF49454F), RoundedCornerShape(14.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (viewModel.isRussian) Color(0xFFD0BCFF) else Color.Transparent)
                        .clickable { if (!viewModel.isRussian) viewModel.toggleLanguage() }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "РУССКИЙ",
                        color = if (viewModel.isRussian) Color(0xFF381E72) else Color(0xFFCAC4D0),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (!viewModel.isRussian) Color(0xFFD0BCFF) else Color.Transparent)
                        .clickable { if (viewModel.isRussian) viewModel.toggleLanguage() }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "ENGLISH",
                        color = if (!viewModel.isRussian) Color(0xFF381E72) else Color(0xFFCAC4D0),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            // Leaderboard & Rules Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateToLeaderboard,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("leaderboard_button"),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFD0BCFF)
                    ),
                    border = BorderStroke(1.dp, Color(0xFF49454F)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.List,
                        contentDescription = "Leaderboard",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (viewModel.isRussian) "РЕКОРДЫ" else "SCORES",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                }

                Button(
                    onClick = { showHowToPlay = !showHowToPlay },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("instructions_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2B2930),
                        contentColor = Color(0xFFE6E1E5)
                    ),
                    border = BorderStroke(1.dp, Color(0xFF49454F)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Rules",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (viewModel.isRussian) "КАК ИГРАТЬ" else "HOW TO PLAY",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                }
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
                            text = if (viewModel.isRussian) "Семейные правила (Кроссворд → Пазл → Тетрис):" else "Family Rules (Crossword → Puzzle → Tetris):",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD54F)
                            )
                        )
                        Text(
                            text = if (viewModel.isRussian) {
                                "1. 🧩 Медленный Кроссворд: прочитайте загадку сверху. В центре поля уже выложены опорные буквы слова, а пустая клетка светится золотым!\n" +
                                "2. 🌿 Складывание Пазла: в режиме «Пазл без таймера» фигуры не падают сами. Спокойно наведите нужную букву на золотую ячейку (или включите «🧒 1-Плитку» для одной простой буквы).\n" +
                                "3. 🧲 Кнопка «Магнит»: в любой момент нажмите «Магнит», чтобы буква сама встала в кроссворд!\n" +
                                "4. ✨ Исчезновение как в Тетрисе: когда слово собрано, вся горизонтальная линия эффектно исчезает, принося +250 очков и новую загадку!"
                            } else {
                                "1. 🧩 Slow Crossword: Read the riddle at the top. Anchor letters are already placed on the board with a glowing golden slot for the missing letter!\n" +
                                "2. 🌿 Puzzle Folding: In 'Puzzle' mode pieces never fall on their own. Align the needed letter over the golden slot (or toggle '🧒 1-Tile' for single-letter tiles).\n" +
                                "3. 🧲 Magnet Helper: Tap 'Magnet' anytime to automatically place the missing letter into the crossword slot!\n" +
                                "4. ✨ Tetris Line Vanish: Completing the word clears the entire horizontal row like Tetris (+250 pts)!"
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

@Composable
private fun PaceOptionChip(
    title: String,
    subtitle: String,
    selected: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) activeColor.copy(alpha = 0.22f) else Color(0xFF1C1B1F))
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = if (selected) activeColor else Color(0xFF49454F),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            color = if (selected) activeColor else Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
        Text(
            text = subtitle,
            color = if (selected) Color.White else Color(0xFFCAC4D0),
            fontSize = 10.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ThemeOptionChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) Color(0xFFD0BCFF).copy(alpha = 0.22f) else Color(0xFF1C1B1F))
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = if (selected) Color(0xFFD0BCFF) else Color(0xFF49454F),
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (selected) Color(0xFFD0BCFF) else Color(0xFFE6E1E5),
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 11.sp,
            textAlign = TextAlign.Center
        )
    }
}
