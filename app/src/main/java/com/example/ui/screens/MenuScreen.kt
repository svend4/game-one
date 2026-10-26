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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.Dictionary
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

    val isPaper = viewModel.isPaperTheme
    val screenBg = if (isPaper) Color(0xFFFBF7EE) else Color(0xFF1C1B1F)
    val cardBg = if (isPaper) Color(0xFFFFFDF9) else Color(0xFF2B2930)
    val borderCol = if (isPaper) Color(0xFFD7CCC8) else Color(0xFF49454F)
    val textPrimary = if (isPaper) Color(0xFF2C1E14) else Color(0xFFE6E1E5)
    val textSecondary = if (isPaper) Color(0xFF5D4037) else Color(0xFFCAC4D0)
    val accentPrimary = if (isPaper) Color(0xFF1B365D) else Color(0xFFD0BCFF)
    val accentGold = if (isPaper) Color(0xFFB45309) else Color(0xFFFFD54F)

    val dailyWord = remember(viewModel.isRussian) { Dictionary.getDailyWord(viewModel.isRussian) }

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
            .background(screenBg)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 18.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .widthIn(max = 520.dp)
                .verticalScroll(scrollState)
                .padding(vertical = 6.dp)
        ) {
            // Game Logo Block
            Box(
                modifier = Modifier
                    .scale(scaleFactor)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .border(1.5.dp, accentGold.copy(alpha = 0.65f), RoundedCornerShape(22.dp))
                    .background(cardBg)
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "TETRI • CROSS",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 3.sp,
                            color = accentPrimary
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
                            color = accentGold,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
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
                    .height(54.dp)
                    .testTag("play_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentPrimary,
                    contentColor = if (isPaper) Color.White else Color(0xFF381E72)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = if (viewModel.isRussian) "Начать игру" else "Play",
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (viewModel.isRussian) "НАЧАТЬ ИГРУ" else "START GAME",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                )
            }

            // --- ☀️ DAILY WORD OF THE DAY & FAMILY ALBUM / CREATOR ROW ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, accentGold.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                    .clickable { viewModel.showDailyWordModal = true }
                    .testTag("daily_word_card"),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (viewModel.isRussian) {
                                "☀️ Слово Дня (${Dictionary.russianLettersPlural(dailyWord.word.length)})"
                            } else {
                                "☀️ Word of the Day (${dailyWord.word.length} letters)"
                            },
                            color = accentGold,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp
                        )

                        Text(
                            text = if (viewModel.dailyWordCompletedToday) {
                                if (viewModel.isRussian) "✅ Разгадано!" else "✅ Solved!"
                            } else {
                                if (viewModel.isRussian) "🎁 +500 очков" else "🎁 +500 pts"
                            },
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }

                    Text(
                        text = dailyWord.definition,
                        color = textPrimary,
                        fontSize = 12.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Family Encyclopedia Album & Custom Riddle Creator Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilledTonalButton(
                            onClick = { viewModel.showEncyclopediaModal = true },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = accentGold.copy(alpha = 0.16f),
                                contentColor = accentGold
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("menu_encyclopedia_button")
                        ) {
                            Text(
                                text = if (viewModel.isRussian) {
                                    "📖 Альбом слов (${viewModel.unlockedWordsSet.size})"
                                } else {
                                    "📖 Word Album (${viewModel.unlockedWordsSet.size})"
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }

                        FilledTonalButton(
                            onClick = { viewModel.showCustomWordCreatorModal = true },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = Color(0xFF00C853).copy(alpha = 0.16f),
                                contentColor = if (isPaper) Color(0xFF1B5E20) else Color(0xFF00E676)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("menu_custom_word_button")
                        ) {
                            Text(
                                text = if (viewModel.isRussian) "✍️ Своя загадка" else "✍️ Custom Riddle",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // --- FAMILY PACE SELECTOR (3 Modes) ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, borderCol, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = cardBg),
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
                        color = textPrimary,
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
                            activeColor = if (isPaper) Color(0xFF2E7D32) else Color(0xFF81C784),
                            isPaper = isPaper,
                            onClick = { viewModel.updatePaceMode(PaceMode.PUZZLE_STEP) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("menu_pace_puzzle")
                        )
                        PaceOptionChip(
                            title = if (viewModel.isRussian) "🐢 Медленно" else "🐢 Slow",
                            subtitle = if (viewModel.isRussian) "Плавный бриз" else "Calm breeze",
                            selected = viewModel.paceMode == PaceMode.RELAXED,
                            activeColor = if (isPaper) Color(0xFF1565C0) else Color(0xFF64B5F6),
                            isPaper = isPaper,
                            onClick = { viewModel.updatePaceMode(PaceMode.RELAXED) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("menu_pace_relaxed")
                        )
                        PaceOptionChip(
                            title = if (viewModel.isRussian) "⚡ Аркада" else "⚡ Arcade",
                            subtitle = if (viewModel.isRussian) "На скорость" else "Dynamic",
                            selected = viewModel.paceMode == PaceMode.ARCADE,
                            activeColor = accentPrimary,
                            isPaper = isPaper,
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
                    .border(1.dp, borderCol, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = cardBg),
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
                        color = textPrimary,
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
                            isPaper = isPaper,
                            onClick = { viewModel.selectTheme(WordTheme.ALL) },
                            modifier = Modifier.weight(1f)
                        )
                        ThemeOptionChip(
                            label = if (viewModel.isRussian) "🏡 Дом и Кухня" else "🏡 Home & Food",
                            selected = viewModel.selectedTheme == WordTheme.HOME_COOKING,
                            isPaper = isPaper,
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
                            isPaper = isPaper,
                            onClick = { viewModel.selectTheme(WordTheme.NATURE_ANIMALS) },
                            modifier = Modifier.weight(1f)
                        )
                        ThemeOptionChip(
                            label = if (viewModel.isRussian) "🧚 Сказки и Дети" else "🧚 Fairy Tales",
                            selected = viewModel.selectedTheme == WordTheme.FAIRY_TALES_KIDS,
                            isPaper = isPaper,
                            onClick = { viewModel.selectTheme(WordTheme.FAIRY_TALES_KIDS) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Row 1 of Toggles: 1-Tile vs 4-Block & Crossed 2-Word Stencil ("✚ 2 Слова")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilledTonalButton(
                            onClick = { viewModel.toggleSingleTileMode() },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (viewModel.isSingleTileMode) Color(0xFF00C853).copy(alpha = 0.2f) else screenBg,
                                contentColor = if (viewModel.isSingleTileMode) (if (isPaper) Color(0xFF1B5E20) else Color(0xFF00E676)) else textSecondary
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (viewModel.isSingleTileMode) Color(0xFF00C853) else borderCol
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
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
                            onClick = { viewModel.toggleCrossedStencilMode() },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (viewModel.isCrossedStencilMode) accentPrimary.copy(alpha = 0.2f) else screenBg,
                                contentColor = if (viewModel.isCrossedStencilMode) accentPrimary else textSecondary
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (viewModel.isCrossedStencilMode) accentPrimary else borderCol
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("menu_crossed_stencil_toggle")
                        ) {
                            Text(
                                text = if (viewModel.isCrossedStencilMode) {
                                    if (viewModel.isRussian) "✚ 2 Слова (Крест)" else "✚ 2-Word Cross"
                                } else {
                                    if (viewModel.isRussian) "➖ 1 Слово в ряд" else "➖ 1-Word Line"
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Row 2 of Toggles: Paper Magazine Theme ("📰 Бумага") & Large Font ("👓 Крупно")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilledTonalButton(
                            onClick = { viewModel.togglePaperTheme() },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (viewModel.isPaperTheme) accentGold.copy(alpha = 0.2f) else screenBg,
                                contentColor = if (viewModel.isPaperTheme) accentGold else textSecondary
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (viewModel.isPaperTheme) accentGold else borderCol
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("menu_paper_theme_toggle")
                        ) {
                            Text(
                                text = if (viewModel.isPaperTheme) {
                                    if (viewModel.isRussian) "📰 Бумажная тема" else "📰 Paper Theme"
                                } else {
                                    if (viewModel.isRussian) "🌙 Вечерняя тема" else "🌙 Night Theme"
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }

                        FilledTonalButton(
                            onClick = { viewModel.toggleLargeFontMode() },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (viewModel.isLargeFontMode) accentGold.copy(alpha = 0.2f) else screenBg,
                                contentColor = if (viewModel.isLargeFontMode) accentGold else textSecondary
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (viewModel.isLargeFontMode) accentGold else borderCol
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
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
                    .background(cardBg)
                    .border(1.dp, borderCol, RoundedCornerShape(14.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (viewModel.isRussian) accentPrimary else Color.Transparent)
                        .clickable { if (!viewModel.isRussian) viewModel.toggleLanguage() }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "РУССКИЙ",
                        color = if (viewModel.isRussian) (if (isPaper) Color.White else Color(0xFF381E72)) else textSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (!viewModel.isRussian) accentPrimary else Color.Transparent)
                        .clickable { if (viewModel.isRussian) viewModel.toggleLanguage() }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "ENGLISH",
                        color = if (!viewModel.isRussian) (if (isPaper) Color.White else Color(0xFF381E72)) else textSecondary,
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
                        contentColor = accentPrimary
                    ),
                    border = BorderStroke(1.dp, borderCol),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.List,
                        contentDescription = if (viewModel.isRussian) "Таблица рекордов" else "Leaderboard",
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
                        containerColor = cardBg,
                        contentColor = textPrimary
                    ),
                    border = BorderStroke(1.dp, borderCol),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = if (viewModel.isRussian) "Правила игры" else "Rules",
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
                        .border(1.dp, borderCol, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (viewModel.isRussian) "Семейные правила и новые возможности:" else "Family Rules & New Features:",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = accentGold
                            )
                        )
                        Text(
                            text = if (viewModel.isRussian) {
                                "1. 🧩 Медленный Кроссворд + 🔊 Озвучка: нажмите «🔊», чтобы прослушать загадку голосом. На поле выложен трафарет слова (или «✚ 2 Слова» крест-накрест) с золотой ячейкой для недостающей буквы.\n" +
                                "2. 🌿 Пазл и ↩️ Отмена хода: спокойно направьте букву в золотую ячейку, нажмите «🧲 Магнит» для автоподстановки или «↩️ Шаг назад», если случайно ошиблись.\n" +
                                "3. 🎉 Праздничный салют и 📖 Альбом: каждое собранное слово запускает салют частиц, очищает линию и открывает познавательный факт в Семейном Альбоме!\n" +
                                "4. ✍️ Своя загадка: загадайте близким собственное семейное слово в конструкторе!"
                            } else {
                                "1. 🧩 Slow Crossword + 🔊 Voice: Tap '🔊' to hear the riddle spoken aloud. The board holds a word stencil (or '✚ 2-Word Cross') with a glowing golden slot.\n" +
                                "2. 🌿 Puzzle & ↩️ Undo: Calmly drop the matching letter into the golden slot, tap '🧲 Magnet', or tap '↩️ Undo' to step back.\n" +
                                "3. 🎉 Confetti & 📖 Word Album: Completing a word triggers confetti fireworks, clears the line, and unlocks a fun fact in the Family Word Album!\n" +
                                "4. ✍️ Custom Riddle: Create your own family words for loved ones!"
                            },
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = textPrimary,
                                lineHeight = 20.sp
                            )
                        )
                    }
                }
            }
        }

        // Modals accessible right from MenuScreen
        if (viewModel.showEncyclopediaModal) {
            FamilyWordEncyclopediaDialog(
                viewModel = viewModel,
                onDismiss = { viewModel.showEncyclopediaModal = false }
            )
        }

        if (viewModel.showCustomWordCreatorModal) {
            CustomFamilyRiddleCreatorDialog(
                viewModel = viewModel,
                onNavigateToGame = onNavigateToGame,
                onDismiss = { viewModel.showCustomWordCreatorModal = false }
            )
        }

        if (viewModel.showDailyWordModal) {
            DailyWordOfTheDayDialog(
                viewModel = viewModel,
                onStartDailyWord = {
                    viewModel.startDailyWordChallenge()
                    onNavigateToGame()
                },
                onDismiss = { viewModel.showDailyWordModal = false }
            )
        }
    }
}

@Composable
private fun PaceOptionChip(
    title: String,
    subtitle: String,
    selected: Boolean,
    activeColor: Color,
    isPaper: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (selected) activeColor.copy(alpha = 0.18f) else (if (isPaper) Color(0xFFF3EBD8) else Color(0xFF1C1B1F))
    val borderCol = if (selected) activeColor else (if (isPaper) Color(0xFFD7CCC8) else Color(0xFF49454F))
    val titleCol = if (selected) activeColor else (if (isPaper) Color(0xFF2C1E14) else Color.White)
    val subCol = if (isPaper) Color(0xFF5D4037) else Color(0xFFCAC4D0)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = borderCol,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            color = titleCol,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
        Text(
            text = subtitle,
            color = subCol,
            fontSize = 10.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ThemeOptionChip(
    label: String,
    selected: Boolean,
    isPaper: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeCol = if (isPaper) Color(0xFF1B365D) else Color(0xFFD0BCFF)
    val bg = if (selected) activeCol.copy(alpha = 0.16f) else (if (isPaper) Color(0xFFF3EBD8) else Color(0xFF1C1B1F))
    val borderCol = if (selected) activeCol else (if (isPaper) Color(0xFFD7CCC8) else Color(0xFF49454F))
    val textCol = if (selected) activeCol else (if (isPaper) Color(0xFF2C1E14) else Color(0xFFE6E1E5))

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = borderCol,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textCol,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 11.sp,
            textAlign = TextAlign.Center
        )
    }
}
