package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.game.*
import kotlinx.coroutines.delay
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onNavigateBack: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    BackHandler {
        if (viewModel.isUserPaused) {
            viewModel.resumeFromPause()
        } else {
            viewModel.pauseGameLoop()
            viewModel.saveGameProgress()
            onNavigateBack()
        }
    }

    // Trigger game loop when the screen is active in Tetris Mode (respects PaceMode.PUZZLE_STEP!)
    LaunchedEffect(viewModel.currentMode, viewModel.isUserPaused, viewModel.paceMode) {
        if (viewModel.currentMode == GameMode.TETRIS && !viewModel.isUserPaused && viewModel.paceMode != PaceMode.PUZZLE_STEP) {
            viewModel.startGameLoop()
        } else {
            viewModel.pauseGameLoop()
        }
    }

    // Stop game loop when leaving
    DisposableEffect(Unit) {
        onDispose {
            viewModel.pauseGameLoop()
        }
    }

    // State for local input values
    var giftTextInput by remember { mutableStateOf("") }
    var activeGuessingClue by remember { mutableStateOf<CrosswordClue?>(null) }
    var clueGuessInput by remember { mutableStateOf("") }
    var showGameOverDialog by remember { mutableStateOf(false) }
    var playerNameInput by remember { mutableStateOf("") }

    LaunchedEffect(viewModel.currentMode) {
        if (viewModel.currentMode == GameMode.GAME_OVER) {
            showGameOverDialog = true
        }
    }

    // Transient toasts banner
    var transientToast by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(viewModel.toastMessage) {
        viewModel.toastMessage?.let {
            transientToast = it
            delay(2800)
            transientToast = null
            viewModel.clearToast()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (viewModel.isRussian) "ТетриКросс" else "TetriCross",
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp,
                            color = Color.White
                        )
                        if (viewModel.comboCount > 1) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFFFB74D).copy(alpha = 0.2f))
                                    .border(1.dp, Color(0xFFFFB74D), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "🔥 x${viewModel.comboCount}",
                                    color = Color(0xFFFFB74D),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            viewModel.pauseGameLoop()
                            viewModel.saveGameProgress()
                            onNavigateBack()
                        },
                        modifier = Modifier.testTag("game_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Menu",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    // 1-Tap Pace Mode Switcher (Puzzle Step / Very Slow / Arcade)
                    FilledTonalButton(
                        onClick = { viewModel.cyclePaceMode() },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = when (viewModel.paceMode) {
                                PaceMode.PUZZLE_STEP -> Color(0xFF1B5E20)
                                PaceMode.RELAXED -> Color(0xFF0D47A1)
                                PaceMode.ARCADE -> Color(0xFF4A148C)
                            },
                            contentColor = Color.White
                        ),
                        border = BorderStroke(
                            1.dp,
                            when (viewModel.paceMode) {
                                PaceMode.PUZZLE_STEP -> Color(0xFF81C784)
                                PaceMode.RELAXED -> Color(0xFF64B5F6)
                                PaceMode.ARCADE -> Color(0xFFD0BCFF)
                            }
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("pace_mode_button")
                    ) {
                        Text(
                            text = when (viewModel.paceMode) {
                                PaceMode.PUZZLE_STEP -> if (viewModel.isRussian) "🌿 Пазл (Без таймера)" else "🌿 Puzzle"
                                PaceMode.RELAXED -> if (viewModel.isRussian) "🐢 Медленно" else "🐢 Slow"
                                PaceMode.ARCADE -> if (viewModel.isRussian) "⚡ Аркада" else "⚡ Arcade"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Prominent Pause / Resume Button
                    FilledTonalButton(
                        onClick = { viewModel.toggleUserPause() },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (viewModel.isUserPaused) Color(0xFF81C784) else Color(0xFF2B2930),
                            contentColor = if (viewModel.isUserPaused) Color(0xFF10281C) else Color(0xFFD0BCFF)
                        ),
                        border = BorderStroke(1.dp, if (viewModel.isUserPaused) Color(0xFF81C784) else Color(0xFF49454F)),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("pause_button")
                    ) {
                        Icon(
                            imageVector = if (viewModel.isUserPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = if (viewModel.isUserPaused) "Resume" else "Pause",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (viewModel.isUserPaused) {
                                if (viewModel.isRussian) "Пуск" else "Play"
                            } else {
                                if (viewModel.isRussian) "Пауза" else "Pause"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }

                    IconButton(
                        onClick = { viewModel.restartGame() },
                        modifier = Modifier.testTag("restart_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Restart",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1C1B1F),
                    titleContentColor = Color.White
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
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val isWide = maxWidth > 600.dp

                if (isWide) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1.1f)
                                .fillMaxHeight(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            BoardView(viewModel = viewModel, onNavigateBack = onNavigateBack)
                        }

                        Column(
                            modifier = Modifier
                                .weight(0.9f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ScoreAndPreviewPanel(viewModel = viewModel)

                            if (viewModel.currentMode == GameMode.TETRIS) {
                                LiveTetrisControlPanel(
                                    viewModel = viewModel,
                                    giftInput = giftTextInput,
                                    onGiftInputChange = { giftTextInput = it },
                                    onSubmitGiftWord = {
                                        viewModel.submitGiftWord(giftTextInput)
                                        giftTextInput = ""
                                        focusManager.clearFocus()
                                    }
                                )
                            } else {
                                LiveCrosswordPanel(
                                    viewModel = viewModel,
                                    onSelectClueToGuess = {
                                        activeGuessingClue = it
                                        clueGuessInput = ""
                                    }
                                )
                            }
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ScoreAndPreviewPanel(viewModel = viewModel)

                        Box(
                            modifier = Modifier
                                .weight(1.12f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            BoardView(viewModel = viewModel, onNavigateBack = onNavigateBack)
                        }

                        Box(
                            modifier = Modifier
                                .weight(0.88f)
                                .fillMaxWidth()
                        ) {
                            if (viewModel.currentMode == GameMode.TETRIS) {
                                LiveTetrisControlPanel(
                                    viewModel = viewModel,
                                    giftInput = giftTextInput,
                                    onGiftInputChange = { giftTextInput = it },
                                    onSubmitGiftWord = {
                                        viewModel.submitGiftWord(giftTextInput)
                                        giftTextInput = ""
                                        focusManager.clearFocus()
                                    }
                                )
                            } else {
                                LiveCrosswordPanel(
                                    viewModel = viewModel,
                                    onSelectClueToGuess = {
                                        activeGuessingClue = it
                                        clueGuessInput = ""
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Toast feedback banners
            AnimatedVisibility(
                visible = transientToast != null,
                enter = fadeIn() + slideInVertically(initialOffsetY = { -40 }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { -40 }),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 12.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF00E676)),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Text(
                        text = transientToast ?: "",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 9.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // --- CROSSWORD GUESS MODAL DIALOG ---
            activeGuessingClue?.let { clue ->
                Dialog(onDismissRequest = { activeGuessingClue = null }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .border(1.dp, Color(0xFFD0BCFF), RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930))
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (viewModel.isRussian) "Разгадать Слово" else "Guess Word",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    color = Color(0xFFD0BCFF),
                                    fontWeight = FontWeight.Bold
                                )
                            )

                            if (clue.category.isNotBlank()) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFD0BCFF).copy(alpha = 0.15f))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${clue.category} • ${clue.length} " + (if (viewModel.isRussian) "букв(ы)" else "letters"),
                                        color = Color(0xFFD0BCFF),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Text(
                                text = clue.clue,
                                color = Color.White.copy(alpha = 0.9f),
                                textAlign = TextAlign.Center,
                                fontSize = 15.sp,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Text(
                                text = clue.getMaskedPreview(),
                                color = Color(0xFFFFB74D),
                                fontSize = 18.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.ExtraBold
                            )

                            OutlinedTextField(
                                value = clueGuessInput,
                                onValueChange = { clueGuessInput = it.uppercase().trim() },
                                label = { Text(if (viewModel.isRussian) "Ваш ответ" else "Your answer") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFD0BCFF),
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                    focusedLabelColor = Color(0xFFD0BCFF),
                                    unfocusedLabelColor = Color.White.copy(alpha = 0.5f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("clue_guess_input"),
                                keyboardOptions = KeyboardOptions(
                                    imeAction = ImeAction.Done,
                                    keyboardType = KeyboardType.Text
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        viewModel.guessClue(clue.id, clueGuessInput)
                                        activeGuessingClue = null
                                    }
                                )
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { activeGuessingClue = null },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                                ) {
                                    Text(if (viewModel.isRussian) "Отмена" else "Cancel")
                                }

                                Button(
                                    onClick = {
                                        viewModel.guessClue(clue.id, clueGuessInput)
                                        activeGuessingClue = null
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("clue_guess_submit_button"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFD0BCFF),
                                        contentColor = Color(0xFF381E72)
                                    )
                                ) {
                                    Text(if (viewModel.isRussian) "Проверить" else "Submit")
                                }
                            }
                        }
                    }
                }
            }

            // --- GAME OVER DIALOG ---
            if (showGameOverDialog) {
                Dialog(onDismissRequest = { }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .border(2.dp, Color(0xFFD0BCFF), RoundedCornerShape(20.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1B1F))
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (viewModel.isRussian) "ИГРА ОКОНЧЕНА" else "GAME OVER",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    color = Color(0xFFD0BCFF),
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 2.sp
                                )
                            )

                            Text(
                                text = if (viewModel.isRussian) {
                                    "Ваш итоговый результат:"
                                } else {
                                    "Final Score:"
                                },
                                color = Color.White.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp
                            )

                            Text(
                                text = "${viewModel.currentScore}",
                                style = MaterialTheme.typography.displayMedium.copy(
                                    color = Color(0xFFD0BCFF),
                                    fontWeight = FontWeight.Black
                                )
                            )

                            OutlinedTextField(
                                value = playerNameInput,
                                onValueChange = { playerNameInput = it },
                                label = { Text(if (viewModel.isRussian) "Имя игрока" else "Player Name") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFD0BCFF),
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("game_over_name_input")
                            )

                            Button(
                                onClick = {
                                    val name = if (playerNameInput.isBlank()) "Player" else playerNameInput
                                    viewModel.submitHighScore(name)
                                    showGameOverDialog = false
                                    viewModel.restartGame()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("save_score_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFD0BCFF),
                                    contentColor = Color(0xFF381E72)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = if (viewModel.isRussian) "СОХРАНИТЬ РЕЗУЛЬТАТ" else "SAVE HIGH SCORE",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScoreAndPreviewPanel(viewModel: GameViewModel) {
    val wordsCount = viewModel.wordsClearedCount

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF49454F), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Score Column
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = if (viewModel.isRussian) "СЧЁТ" else "SCORE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFFCAC4D0),
                            fontWeight = FontWeight.Medium,
                            fontSize = 9.sp
                        )
                    )
                    Text(
                        text = String.format("%,d", viewModel.currentScore),
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = Color(0xFFD0BCFF),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        modifier = Modifier.testTag("score_text")
                    )
                }

                // Center: Hold + Mode + Next Piece
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MiniPieceBox(
                        label = if (viewModel.isRussian) "ЗАПАС" else "HOLD",
                        shapeType = viewModel.heldPieceShapeType,
                        tint = Color(0xFFFFB74D)
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (viewModel.currentMode == GameMode.TETRIS) Color(0xFFD0BCFF).copy(alpha = 0.15f)
                                    else Color(0xFF81C784).copy(alpha = 0.15f)
                                )
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (viewModel.currentMode == GameMode.TETRIS) {
                                    when (viewModel.paceMode) {
                                        PaceMode.PUZZLE_STEP -> if (viewModel.isRussian) "ШАГ ПАЗЛА" else "PUZZLE STEP"
                                        PaceMode.RELAXED -> if (viewModel.isRussian) "МЕДЛЕННО" else "RELAXED"
                                        PaceMode.ARCADE -> if (viewModel.isRussian) "ТЕТРИС" else "TETRIS"
                                    }
                                } else {
                                    if (viewModel.isRussian) "КРОССВОРД" else "CROSSWORD"
                                },
                                color = if (viewModel.currentMode == GameMode.TETRIS) Color(0xFFD0BCFF) else Color(0xFF81C784),
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                        }

                        Text(
                            text = if (viewModel.isRussian) "💡 Заряды: ${viewModel.freeHintCharges}" else "💡 Hints: ${viewModel.freeHintCharges}",
                            color = Color(0xFF81C784),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    MiniPieceBox(
                        label = if (viewModel.isRussian) "СЛЕД" else "NEXT",
                        shapeType = viewModel.nextPieceShapeType,
                        tint = Color(0xFFD0BCFF)
                    )
                }

                // Cleared Words Column
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (viewModel.isRussian) "СЛОВ" else "WORDS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFFCAC4D0),
                            fontWeight = FontWeight.Medium,
                            fontSize = 9.sp
                        )
                    )
                    Text(
                        text = "$wordsCount",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = Color(0xFF81C784),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    )
                }
            }

            // Stage 1 -> Stage 2 -> Stage 3: Live Crossword Stencil Banner ("Кроссворд-Трафарет на поле")
            viewModel.questWord?.let { quest ->
                if (viewModel.currentMode == GameMode.TETRIS) {
                    val board = viewModel.engine.board
                    val slots = viewModel.stencilSlots
                    val stencilPreview = if (slots.isNotEmpty()) {
                        slots.joinToString(" ") { slot ->
                            val c = if (viewModel.engine.isValidPoint(slot.x, slot.y)) board[slot.y][slot.x] else null
                            if (c != null && (c.char == slot.expectedChar || c.char == Dictionary.WILDCARD_CHAR)) {
                                c.char.toString()
                            } else {
                                "[${slot.expectedChar}]"
                            }
                        }
                    } else {
                        quest.word
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF1C1B1F))
                            .border(1.dp, Color(0xFFFFD54F).copy(alpha = 0.55f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (viewModel.isRussian) {
                                    "🧩 Загадка (${quest.category.ifBlank { "Слово" }}): $stencilPreview"
                                } else {
                                    "🧩 Puzzle (${quest.category.ifBlank { "Word" }}): $stencilPreview"
                                },
                                color = Color(0xFFFFD54F),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Magnet Helper Button (1-tap auto-fills the missing letter into the stencil!)
                                FilledTonalButton(
                                    onClick = { viewModel.useMagnetHelper() },
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = Color(0xFF00E676).copy(alpha = 0.2f),
                                        contentColor = Color(0xFF00E676)
                                    ),
                                    border = BorderStroke(1.dp, Color(0xFF00E676)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier
                                        .height(26.dp)
                                        .testTag("magnet_helper_button")
                                ) {
                                    Text(
                                        text = if (viewModel.isRussian) "🧲 Магнит" else "🧲 Magnet",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                }

                                // Skip Stencil Button
                                OutlinedButton(
                                    onClick = { viewModel.skipStencilPuzzle() },
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, Color(0xFF49454F)),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                    modifier = Modifier
                                        .height(26.dp)
                                        .testTag("skip_stencil_button")
                                ) {
                                    Text(
                                        text = "🔄",
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }

                        // Large readable clue text for seniors, moms, and children
                        Text(
                            text = quest.definition,
                            color = Color.White.copy(alpha = 0.92f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniPieceBox(label: String, shapeType: Int?, tint: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = label,
            color = Color(0xFFCAC4D0),
            fontSize = 8.sp,
            fontWeight = FontWeight.SemiBold
        )
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF1C1B1F))
                .border(0.5.dp, Color(0xFF49454F), RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (shapeType != null) {
                val offsets = TetrominoShapes.getOffsets(shapeType, 0)
                Box(modifier = Modifier.size(16.dp)) {
                    offsets.forEach { offset ->
                        val drawX = 5.dp + (offset.first * 4).dp
                        val drawY = 5.dp + (offset.second * 4).dp
                        Box(
                            modifier = Modifier
                                .size(3.5.dp)
                                .offset(x = drawX, y = drawY)
                                .clip(RoundedCornerShape(0.5.dp))
                                .background(tint)
                        )
                    }
                }
            } else {
                Text(
                    text = "—",
                    color = Color(0xFF49454F),
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun BoardView(
    viewModel: GameViewModel,
    onNavigateBack: () -> Unit
) {
    key(viewModel.boardStateTick, viewModel.isUserPaused) {
        val board = viewModel.engine.board
        val rows = viewModel.engine.rows
        val cols = viewModel.engine.cols
        val activePiece = viewModel.activePiece

        val activePointsMap = remember(activePiece) {
            activePiece?.let { piece ->
                val points = piece.getPoints()
                points.mapIndexed { idx, point ->
                    point to (piece.letters.getOrNull(idx) ?: ' ')
                }.toMap()
            } ?: emptyMap()
        }

        // Ghost piece landing shadow coordinates
        val ghostPointsSet = remember(activePiece, viewModel.boardStateTick) {
            if (viewModel.currentMode == GameMode.TETRIS && activePiece != null) {
                viewModel.engine.getGhostPiecePoints().toSet()
            } else {
                emptySet()
            }
        }

        // Crossword Stencil Slots map (x,y -> expectedChar)
        val stencilSlotsMap = remember(viewModel.stencilSlots, viewModel.boardStateTick) {
            viewModel.stencilSlots.associateBy { GridPoint(it.x, it.y) }
        }

        // Word Radar highlighted coordinates in Crossword Mode
        val radarPointsSet = remember(viewModel.crosswordClues, viewModel.boardStateTick) {
            viewModel.crosswordClues
                .filter { it.radarActive && !it.isSolved }
                .flatMap { it.getCoordinates() }
                .toSet()
        }

        var accumulatedDragX by remember { mutableFloatStateOf(0f) }
        var accumulatedDragY by remember { mutableFloatStateOf(0f) }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(10f / 15f)
                .pointerInput(viewModel.currentMode, viewModel.isUserPaused) {
                    if (viewModel.currentMode == GameMode.TETRIS && !viewModel.isUserPaused) {
                        detectTapGestures(
                            onTap = { viewModel.rotatePiece() },
                            onDoubleTap = { viewModel.shiftPieceLetters() }
                        )
                    }
                }
                .pointerInput(viewModel.currentMode, viewModel.isUserPaused) {
                    if (viewModel.currentMode == GameMode.TETRIS && !viewModel.isUserPaused) {
                        detectDragGestures(
                            onDragStart = {
                                accumulatedDragX = 0f
                                accumulatedDragY = 0f
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                accumulatedDragX += dragAmount.x
                                accumulatedDragY += dragAmount.y
                                val stepThreshold = 42f
                                if (abs(accumulatedDragX) > stepThreshold) {
                                    if (accumulatedDragX > 0) viewModel.moveRight() else viewModel.moveLeft()
                                    accumulatedDragX = 0f
                                }
                                if (accumulatedDragY > stepThreshold * 1.4f) {
                                    viewModel.moveDown()
                                    accumulatedDragY = 0f
                                }
                            }
                        )
                    }
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF2B2930))
                    .drawBehind {
                        val strokeWidth = 1.dp.toPx()
                        val color = Color(0xFF938F99).copy(alpha = 0.10f)
                        val colWidth = size.width / cols
                        val rowHeight = size.height / rows

                        for (c in 1 until cols) {
                            val x = c * colWidth
                            drawLine(
                                color = color,
                                start = androidx.compose.ui.geometry.Offset(x, 0f),
                                end = androidx.compose.ui.geometry.Offset(x, size.height),
                                strokeWidth = strokeWidth
                            )
                        }

                        for (r in 1 until rows) {
                            val y = r * rowHeight
                            drawLine(
                                color = color,
                                start = androidx.compose.ui.geometry.Offset(0f, y),
                                end = androidx.compose.ui.geometry.Offset(size.width, y),
                                strokeWidth = strokeWidth
                            )
                        }
                    }
                    .border(1.5.dp, Color(0xFF49454F), RoundedCornerShape(20.dp))
                    .padding(4.dp),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                for (r in 0 until rows) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (c in 0 until cols) {
                            val pt = GridPoint(c, r)
                            val activeChar = activePointsMap[pt]
                            val isGhost = activeChar == null && ghostPointsSet.contains(pt)
                            val isRadar = radarPointsSet.contains(pt)
                            val stencilSlot = stencilSlotsMap[pt]
                            val cell = board[r][c]
                            val isWildcard = activeChar == Dictionary.WILDCARD_CHAR || cell?.isWildcard == true
                            val isAnchorTile = cell?.isAnchor == true
                            val isOpenStencilTarget = stencilSlot != null && cell == null && activeChar == null

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .padding(1.2.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        when {
                                            isRadar -> Color(0xFF00E676).copy(alpha = 0.35f)
                                            isWildcard && (activeChar != null || cell != null) -> Color(0xFFFFB74D)
                                            activeChar != null -> Color(0xFFD0BCFF)
                                            isAnchorTile -> Color(0xFF4527A0) // Deep Crossword Anchor Tile
                                            cell != null -> Color(0xFF49454F)
                                            isOpenStencilTarget -> Color(0xFFFFD54F).copy(alpha = 0.22f) // Glowing Crossword Target Slot!
                                            isGhost -> Color(0xFFD0BCFF).copy(alpha = 0.12f)
                                            else -> Color.Transparent
                                        }
                                    )
                                    .then(
                                        when {
                                            isRadar -> Modifier.border(2.dp, Color(0xFF00E676), RoundedCornerShape(4.dp))
                                            isOpenStencilTarget -> Modifier.border(
                                                2.dp,
                                                Color(0xFFFFD54F),
                                                RoundedCornerShape(4.dp)
                                            )
                                            isWildcard && (activeChar != null || cell != null) -> Modifier.border(
                                                1.5.dp,
                                                Color(0xFFFFF176),
                                                RoundedCornerShape(4.dp)
                                            )
                                            activeChar != null -> Modifier.border(
                                                1.5.dp,
                                                Color.White,
                                                RoundedCornerShape(4.dp)
                                            )
                                            isAnchorTile -> Modifier.border(
                                                1.5.dp,
                                                Color(0xFFFFD54F).copy(alpha = 0.85f),
                                                RoundedCornerShape(4.dp)
                                            )
                                            cell != null -> Modifier.border(
                                                1.dp,
                                                Color(0xFFD0BCFF).copy(alpha = 0.4f),
                                                RoundedCornerShape(4.dp)
                                            )
                                            isGhost -> Modifier.border(
                                                1.dp,
                                                Color(0xFFD0BCFF).copy(alpha = 0.45f),
                                                RoundedCornerShape(4.dp)
                                            )
                                            else -> Modifier
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                val displayText = when {
                                    viewModel.isUserPaused -> "" // Hide letters during pause
                                    activeChar != null -> activeChar.toString()
                                    cell != null -> cell.char.toString()
                                    isOpenStencilTarget -> stencilSlot?.expectedChar?.toString() ?: ""
                                    else -> ""
                                }
                                if (displayText.isNotEmpty()) {
                                    Text(
                                        text = displayText,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontFamily = FontFamily.Monospace,
                                        color = when {
                                            isWildcard -> Color(0xFF1C1B1F)
                                            activeChar != null -> Color(0xFF381E72)
                                            isOpenStencilTarget -> Color(0xFFFFD54F).copy(alpha = 0.75f)
                                            isAnchorTile -> Color(0xFFFFF59D)
                                            else -> Color(0xFFE6E1E5)
                                        },
                                        textAlign = TextAlign.Center
                                    )
                                } else if (!isGhost) {
                                    Box(
                                        modifier = Modifier
                                            .size(2.dp)
                                            .background(Color(0xFF938F99).copy(alpha = 0.15f))
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Pause Modal Overlay Curtain
            if (viewModel.isUserPaused) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF1C1B1F).copy(alpha = 0.94f))
                        .border(2.dp, Color(0xFFD0BCFF), RoundedCornerShape(20.dp))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PauseCircle,
                            contentDescription = null,
                            tint = Color(0xFFD0BCFF),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = if (viewModel.isRussian) "ИГРА НА ПАУЗЕ" else "GAME PAUSED",
                            color = Color(0xFFD0BCFF),
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            text = if (viewModel.isRussian) {
                                "Совет: в режиме «🌿 Пазл» фигуры не падают сами — вы можете спокойно собирать слово кроссворда в центре поля или нажать «🧲 Магнит»!"
                            } else {
                                "Tip: In '🌿 Puzzle' mode pieces never fall on their own — take your time completing the crossword word on the grid or tap '🧲 Magnet'!"
                            },
                            color = Color(0xFFCAC4D0),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(
                            onClick = { viewModel.resumeFromPause() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("resume_pause_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFD0BCFF),
                                contentColor = Color(0xFF381E72)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (viewModel.isRussian) "ПРОДОЛЖИТЬ" else "RESUME GAME",
                                fontWeight = FontWeight.Bold
                            )
                        }
                        OutlinedButton(
                            onClick = { viewModel.restartGame() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE6E1E5)),
                            border = BorderStroke(1.dp, Color(0xFF49454F)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(if (viewModel.isRussian) "НАЧАТЬ ЗАНОВО" else "RESTART")
                        }
                        TextButton(
                            onClick = {
                                viewModel.saveGameProgress()
                                onNavigateBack()
                            }
                        ) {
                            Text(
                                text = if (viewModel.isRussian) "Выйти в главное меню" else "Exit to Main Menu",
                                color = Color(0xFFCAC4D0)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LiveTetrisControlPanel(
    viewModel: GameViewModel,
    giftInput: String,
    onGiftInputChange: (String) -> Unit,
    onSubmitGiftWord: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF2B2930))
            .border(1.dp, Color(0xFF49454F), RoundedCornerShape(20.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Direct Word Solver / Gift custom word entry field
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1B1F)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = giftInput,
                    onValueChange = { onGiftInputChange(it.uppercase()) },
                    label = {
                        Text(
                            text = if (viewModel.isRussian) "✍️ Введите ответ загадки или своё слово" else "✍️ Type puzzle answer or gift word",
                            fontSize = 10.sp
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("gift_word_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFD0BCFF),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedLabelColor = Color(0xFFD0BCFF),
                        unfocusedLabelColor = Color.White.copy(alpha = 0.5f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Send,
                        keyboardType = KeyboardType.Text
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = { onSubmitGiftWord() }
                    )
                )

                Button(
                    onClick = onSubmitGiftWord,
                    modifier = Modifier
                        .height(40.dp)
                        .testTag("gift_word_submit"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD0BCFF),
                        contentColor = Color(0xFF381E72)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Text(
                        text = if (viewModel.isRussian) "Внести" else "Submit",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Row 1: Tactical Manipulation (Hold Pocket, Rotate Piece, Shift Piece Letters)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // HOLD POCKET BUTTON
            FilledTonalButton(
                onClick = { viewModel.holdPiece() },
                enabled = viewModel.canHoldThisTurn,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = Color(0xFF1C1B1F),
                    contentColor = Color(0xFFFFB74D)
                ),
                border = BorderStroke(1.dp, Color(0xFF49454F)),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier
                    .height(42.dp)
                    .testTag("ctrl_hold")
            ) {
                Icon(
                    imageVector = Icons.Default.Inventory2,
                    contentDescription = "Hold Piece",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (viewModel.isRussian) "Карман" else "Hold",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // ROTATE SHAPE BUTTON
            IconButton(
                onClick = { viewModel.rotatePiece() },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFD0BCFF))
                    .testTag("ctrl_rotate")
            ) {
                Icon(
                    imageVector = Icons.Default.RotateRight,
                    contentDescription = "Rotate Shape",
                    tint = Color(0xFF381E72),
                    modifier = Modifier.size(26.dp)
                )
            }

            // SHIFT LETTERS INSIDE PIECE BUTTON
            FilledTonalButton(
                onClick = { viewModel.shiftPieceLetters() },
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = Color(0xFF1C1B1F),
                    contentColor = Color(0xFF81C784)
                ),
                border = BorderStroke(1.dp, Color(0xFF81C784).copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier
                    .height(42.dp)
                    .testTag("ctrl_shift_letters")
            ) {
                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = "Cycle Letters",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (viewModel.isRussian) "Сдвиг букв" else "Shift ABC",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Row 2: Directional Navigation + Hard Drop / Place Piece
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.moveLeft() },
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF49454F))
                    .testTag("ctrl_left")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Move Left",
                    tint = Color(0xFFE6E1E5),
                    modifier = Modifier.size(30.dp)
                )
            }

            IconButton(
                onClick = { viewModel.moveDown() },
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF49454F))
                    .testTag("ctrl_down")
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Step Down",
                    tint = Color(0xFFE6E1E5),
                    modifier = Modifier.size(30.dp)
                )
            }

            Button(
                onClick = { viewModel.dropPiece() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1C1B1F),
                    contentColor = Color(0xFFD0BCFF)
                ),
                border = BorderStroke(1.dp, Color(0xFFD0BCFF).copy(alpha = 0.5f)),
                modifier = Modifier
                    .height(48.dp)
                    .testTag("ctrl_drop"),
                shape = RoundedCornerShape(24.dp),
                contentPadding = PaddingValues(horizontal = 14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardDoubleArrowDown,
                    contentDescription = "Place Piece",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (viewModel.isRussian) "ПОСТАВИТЬ" else "PLACE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            IconButton(
                onClick = { viewModel.moveRight() },
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF49454F))
                    .testTag("ctrl_right")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Move Right",
                    tint = Color(0xFFE6E1E5),
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}

@Composable
fun LiveCrosswordPanel(
    viewModel: GameViewModel,
    onSelectClueToGuess: (CrosswordClue) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF2B2930))
            .border(1.dp, Color(0xFF49454F), RoundedCornerShape(20.dp))
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (viewModel.isRussian) "🧩 КРОССВОРД НА ПОЛЕ:" else "🧩 BOARD CROSSWORD:",
                color = Color(0xFFD0BCFF),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
            Text(
                text = if (viewModel.isRussian) {
                    "Бесплатных подсказок: ${viewModel.freeHintCharges}"
                } else {
                    "Free Hints: ${viewModel.freeHintCharges}"
                },
                color = Color(0xFF81C784),
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp
            )
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(viewModel.crosswordClues) { clue ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = when {
                                clue.isSolved -> Color(0xFF81C784)
                                clue.radarActive -> Color(0xFF00E676)
                                else -> Color(0xFF49454F)
                            },
                            shape = RoundedCornerShape(12.dp)
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (clue.isSolved) Color(0xFF10281C) else Color(0xFF1C1B1F)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFD0BCFF).copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${clue.category.ifBlank { if (viewModel.isRussian) "Слово" else "Word" }} • ${clue.length} б.",
                                    color = Color(0xFFD0BCFF),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text(
                                text = if (clue.isSolved) "${clue.word} ✅" else clue.getMaskedPreview(),
                                color = if (clue.isSolved) Color(0xFF81C784) else Color(0xFFFFB74D),
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp
                            )
                        }

                        Text(
                            text = clue.clue,
                            color = Color.White.copy(alpha = if (clue.isSolved) 0.6f else 0.92f),
                            fontSize = 12.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (!clue.isSolved) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Hint 1: Open 1 letter
                                OutlinedButton(
                                    onClick = { viewModel.revealOneLetterHint(clue.id) },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp),
                                    border = BorderStroke(1.dp, Color(0xFF81C784).copy(alpha = 0.5f))
                                ) {
                                    Text(
                                        text = if (viewModel.freeHintCharges > 0) "💡+1Б (0)" else "💡+1Б (-15)",
                                        color = Color(0xFF81C784),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                // Hint 2: Word Radar on board
                                OutlinedButton(
                                    onClick = { viewModel.toggleWordRadar(clue.id) },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp),
                                    border = BorderStroke(
                                        1.dp,
                                        if (clue.radarActive) Color(0xFF00E676) else Color(0xFFD0BCFF).copy(alpha = 0.5f)
                                    )
                                ) {
                                    Text(
                                        text = if (viewModel.isRussian) "📡 Радар" else "📡 Radar",
                                        color = if (clue.radarActive) Color(0xFF00E676) else Color(0xFFD0BCFF),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                // Hint 3: Full Reveal (-50)
                                OutlinedButton(
                                    onClick = { viewModel.payToRevealClue(clue.id) },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp),
                                    border = BorderStroke(1.dp, Color(0xFFEFB8C8).copy(alpha = 0.4f))
                                ) {
                                    Text(
                                        text = "🔓(-50)",
                                        color = Color(0xFFEFB8C8),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.weight(1f))

                                // Guess Answer Button
                                Button(
                                    onClick = { onSelectClueToGuess(clue) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFD0BCFF),
                                        contentColor = Color(0xFF381E72)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text(
                                        text = if (viewModel.isRussian) "Ввести" else "Guess",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Button(
            onClick = { viewModel.resumeTetrisAfterCrossword() },
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .testTag("resume_tetris_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFD0BCFF),
                contentColor = Color(0xFF381E72)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (viewModel.isRussian) "ПРОДОЛЖИТЬ СКЛАДЫВАНИЕ ПАЗЛОВ" else "RESUME PUZZLE FOLDING",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}
