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
import androidx.compose.material.icons.automirrored.filled.RotateRight
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
    val isPaper = viewModel.isPaperTheme
    val screenBg = if (isPaper) Color(0xFFFBF7EE) else Color(0xFF1C1B1F)
    val topBarBg = if (isPaper) Color(0xFFF3EBD8) else Color(0xFF1C1B1F)
    val textPrimary = if (isPaper) Color(0xFF2C1E14) else Color.White

    BackHandler {
        when {
            viewModel.showRiddleSpotlight -> viewModel.dismissRiddleSpotlight()
            viewModel.showEncyclopediaModal -> viewModel.showEncyclopediaModal = false
            viewModel.showCustomWordCreatorModal -> viewModel.showCustomWordCreatorModal = false
            viewModel.showDailyWordModal -> viewModel.showDailyWordModal = false
            viewModel.isUserPaused -> viewModel.resumeFromPause()
            else -> {
                viewModel.pauseGameLoop()
                viewModel.saveGameProgress()
                onNavigateBack()
            }
        }
    }

    // Track active screen and trigger game loop only when GameScreen is visible
    DisposableEffect(Unit) {
        viewModel.isGameScreenActive = true
        if (viewModel.currentMode == GameMode.TETRIS && !viewModel.isUserPaused && viewModel.paceMode != PaceMode.PUZZLE_STEP) {
            viewModel.startGameLoop()
        }
        onDispose {
            viewModel.isGameScreenActive = false
            viewModel.pauseGameLoop()
        }
    }

    LaunchedEffect(viewModel.currentMode, viewModel.isUserPaused, viewModel.paceMode) {
        if (viewModel.isGameScreenActive && viewModel.currentMode == GameMode.TETRIS && !viewModel.isUserPaused && viewModel.paceMode != PaceMode.PUZZLE_STEP) {
            viewModel.startGameLoop()
        } else {
            viewModel.pauseGameLoop()
        }
    }

    // State for local input values
    var giftTextInput by remember { mutableStateOf("") }
    var activeGuessingClue by remember { mutableStateOf<CrosswordClue?>(null) }
    var clueGuessInput by remember { mutableStateOf("") }
    var spotlightGuessInput by remember { mutableStateOf("") }
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
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (viewModel.isRussian) "ТетриКросс" else "TetriCross",
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = textPrimary
                        )
                        if (viewModel.comboCount > 1) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFFFB74D).copy(alpha = 0.25f))
                                    .border(1.dp, Color(0xFFD97706), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "🔥x${viewModel.comboCount}",
                                    color = if (isPaper) Color(0xFFB45309) else Color(0xFFFFB74D),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 10.sp
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
                            contentDescription = if (viewModel.isRussian) "В главное меню" else "Back to Menu",
                            tint = textPrimary
                        )
                    }
                },
                actions = {
                    // 1-Tap Kids/Senior Single-Tile (1x1) vs 4-Block Toggle
                    FilledTonalButton(
                        onClick = { viewModel.toggleSingleTileMode() },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (viewModel.isSingleTileMode) Color(0xFF004D40) else Color(0xFF2B2930),
                            contentColor = if (viewModel.isSingleTileMode) Color(0xFF00E676) else Color(0xFFCAC4D0)
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (viewModel.isSingleTileMode) Color(0xFF00E676) else Color(0xFF49454F)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("single_tile_mode_button")
                    ) {
                        Text(
                            text = if (viewModel.isSingleTileMode) {
                                if (viewModel.isRussian) "🧒 1-Букв" else "🧒 1-Tile"
                            } else {
                                if (viewModel.isRussian) "🧱 4-Фиг" else "🧱 4-Blk"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // 1-Tap Crossed 2-Word Stencil Toggle ("✚ 2Сл" vs "1Сл")
                    FilledTonalButton(
                        onClick = { viewModel.toggleCrossedStencilMode() },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (viewModel.isCrossedStencilMode) Color(0xFF311B92) else Color(0xFF2B2930),
                            contentColor = if (viewModel.isCrossedStencilMode) Color(0xFFFFD54F) else Color(0xFFCAC4D0)
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (viewModel.isCrossedStencilMode) Color(0xFFFFD54F) else Color(0xFF49454F)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("crossed_stencil_mode_button")
                    ) {
                        Text(
                            text = if (viewModel.isCrossedStencilMode) {
                                if (viewModel.isRussian) "✚ 2Сл" else "✚ 2W"
                            } else {
                                if (viewModel.isRussian) "➖ 1Сл" else "➖ 1W"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

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
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("pace_mode_button")
                    ) {
                        Text(
                            text = when (viewModel.paceMode) {
                                PaceMode.PUZZLE_STEP -> if (viewModel.isRussian) "🌿 Пазл" else "🌿 Puzzle"
                                PaceMode.RELAXED -> if (viewModel.isRussian) "🐢 Медл" else "🐢 Slow"
                                PaceMode.ARCADE -> if (viewModel.isRussian) "⚡ Арк" else "⚡ Arc"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Prominent Pause / Stop-Frame Button
                    FilledTonalButton(
                        onClick = { viewModel.toggleUserPause() },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (viewModel.isUserPaused) Color(0xFF81C784) else Color(0xFF2B2930),
                            contentColor = if (viewModel.isUserPaused) Color(0xFF10281C) else Color(0xFFD0BCFF)
                        ),
                        border = BorderStroke(1.dp, if (viewModel.isUserPaused) Color(0xFF81C784) else Color(0xFF49454F)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("pause_button")
                    ) {
                        Icon(
                            imageVector = if (viewModel.isUserPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = if (viewModel.isUserPaused) {
                                if (viewModel.isRussian) "Продолжить" else "Resume"
                            } else {
                                if (viewModel.isRussian) "Пауза" else "Pause"
                            },
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = if (viewModel.isUserPaused) {
                                if (viewModel.isRussian) "Пуск" else "Play"
                            } else {
                                if (viewModel.isRussian) "Стоп" else "Pause"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }

                    // Trophy Button to Finish & Save Score to Leaderboard anytime!
                    IconButton(
                        onClick = { viewModel.finishAndSaveScore() },
                        modifier = Modifier.testTag("top_finish_save_score_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = if (viewModel.isRussian) "Завершить и сохранить рекорд" else "Finish and save score",
                            tint = if (isPaper) Color(0xFFB45309) else Color(0xFFFFD54F)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.restartGame() },
                        modifier = Modifier.testTag("restart_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = if (viewModel.isRussian) "Начать заново" else "Restart",
                            tint = textPrimary
                        )
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

            // Festive Particle Confetti Fireworks + Haptic Feedback when a Crossword Stencil line vanishes!
            ConfettiCelebrationOverlay(
                triggerTick = viewModel.celebrationTriggerTick,
                modifier = Modifier.fillMaxSize()
            )

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

            // --- LARGE CROSSWORD RIDDLE SPOTLIGHT MODAL (For Seniors, Moms, & Kids) ---
            if (viewModel.showRiddleSpotlight) {
                viewModel.questWord?.let { quest ->
                    val secQuest = if (viewModel.isCrossedStencilMode) viewModel.secondaryQuestWord else null
                    Dialog(onDismissRequest = { viewModel.dismissRiddleSpotlight() }) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                                .border(2.dp, Color(0xFFD97706), RoundedCornerShape(20.dp)),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isPaper) Color(0xFFFBF7EE) else Color(0xFF2B2930)
                            ),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (viewModel.isRussian) "🧩 ЗАГАДКА КРОССВОРДА" else "🧩 CROSSWORD RIDDLE",
                                        color = if (isPaper) Color(0xFFB45309) else Color(0xFFFFD54F),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 16.sp
                                    )

                                    FilledTonalButton(
                                        onClick = { viewModel.speakActiveRiddle() },
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = Color(0xFFD97706).copy(alpha = 0.2f),
                                            contentColor = if (isPaper) Color(0xFFB45309) else Color(0xFFFFD54F)
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier
                                            .height(32.dp)
                                            .testTag("spotlight_speak_button")
                                    ) {
                                        Text(
                                            text = if (viewModel.isRussian) "🔊 Озвучить" else "🔊 Speak",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF1B365D).copy(alpha = 0.12f))
                                        .padding(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "↔ ${quest.category.ifBlank { if (viewModel.isRussian) "Семья" else "Family" }} • ${quest.word.length} " +
                                            (if (viewModel.isRussian) "букв(ы)" else "letters"),
                                        color = if (isPaper) Color(0xFF1B365D) else Color(0xFFD0BCFF),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }

                                Text(
                                    text = quest.definition,
                                    color = textPrimary,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 22.sp
                                )

                                if (secQuest != null) {
                                    HorizontalDivider(color = Color(0xFFD97706).copy(alpha = 0.3f))
                                    Text(
                                        text = if (viewModel.isRussian) {
                                            "↕ По вертикали (${secQuest.word.length} букв): ${secQuest.definition}"
                                        } else {
                                            "↕ Down (${secQuest.word.length} letters): ${secQuest.definition}"
                                        },
                                        color = textPrimary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 20.sp
                                    )
                                }

                                OutlinedTextField(
                                    value = spotlightGuessInput,
                                    onValueChange = { spotlightGuessInput = it.uppercase().trim() },
                                    label = {
                                        Text(
                                            if (viewModel.isRussian) "Знаете ответ? Введите слово целиком:"
                                            else "Know the answer? Type the full word:"
                                        )
                                    },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFFD97706),
                                        unfocusedBorderColor = textPrimary.copy(alpha = 0.3f),
                                        focusedTextColor = textPrimary,
                                        unfocusedTextColor = textPrimary
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            viewModel.useMagnetHelper()
                                            viewModel.dismissRiddleSpotlight()
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF00C853),
                                            contentColor = Color.White
                                        ),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = if (viewModel.isRussian) "🧲 Магнит" else "🧲 Magnet",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            if (spotlightGuessInput.isNotBlank()) {
                                                viewModel.submitGiftWord(spotlightGuessInput)
                                                spotlightGuessInput = ""
                                            }
                                            viewModel.dismissRiddleSpotlight()
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isPaper) Color(0xFF1B365D) else Color(0xFFD0BCFF),
                                            contentColor = if (isPaper) Color.White else Color(0xFF381E72)
                                        ),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = if (viewModel.isRussian) "Продолжить" else "Continue",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }

                                // Quick buttons to open Family Riddle Creator or Word Encyclopedia
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            viewModel.dismissRiddleSpotlight()
                                            viewModel.showEncyclopediaModal = true
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = if (viewModel.isRussian) "📖 Альбом слов" else "📖 Word Album",
                                            fontSize = 11.sp
                                        )
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            viewModel.dismissRiddleSpotlight()
                                            viewModel.showCustomWordCreatorModal = true
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = if (viewModel.isRussian) "✍️ Своё слово" else "✍️ Custom Word",
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Family Encyclopedia Album Dialog
            if (viewModel.showEncyclopediaModal) {
                FamilyWordEncyclopediaDialog(
                    viewModel = viewModel,
                    onDismiss = { viewModel.showEncyclopediaModal = false }
                )
            }

            // Custom Family Riddle Creator Dialog
            if (viewModel.showCustomWordCreatorModal) {
                CustomFamilyRiddleCreatorDialog(
                    viewModel = viewModel,
                    onNavigateToGame = null,
                    onDismiss = { viewModel.showCustomWordCreatorModal = false }
                )
            }

            // Daily Word of the Day Dialog
            if (viewModel.showDailyWordModal) {
                DailyWordOfTheDayDialog(
                    viewModel = viewModel,
                    onStartDailyWord = { viewModel.startDailyWordChallenge() },
                    onDismiss = { viewModel.showDailyWordModal = false }
                )
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
    val isPaper = viewModel.isPaperTheme
    val cardBg = if (isPaper) Color(0xFFFFFDF9) else Color(0xFF2B2930)
    val innerBg = if (isPaper) Color(0xFFF3EBD8) else Color(0xFF1C1B1F)
    val borderCol = if (isPaper) Color(0xFFD7CCC8) else Color(0xFF49454F)
    val textPrimary = if (isPaper) Color(0xFF2C1E14) else Color.White
    val textSecondary = if (isPaper) Color(0xFF5D4037) else Color(0xFFCAC4D0)
    val accentPrimary = if (isPaper) Color(0xFF1B365D) else Color(0xFFD0BCFF)
    val accentGold = if (isPaper) Color(0xFFB45309) else Color(0xFFFFD54F)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderCol, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = cardBg),
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
                            color = textSecondary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 9.sp
                        )
                    )
                    Text(
                        text = String.format("%,d", viewModel.currentScore),
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = accentPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        modifier = Modifier.testTag("score_text")
                    )
                }

                // Center: Hold + Mode + Next Piece
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MiniPieceBox(
                        label = if (viewModel.isRussian) "ЗАПАС" else "HOLD",
                        shapeType = viewModel.heldPieceShapeType,
                        tint = Color(0xFFD97706),
                        isPaper = isPaper
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(accentPrimary.copy(alpha = 0.14f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
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
                                color = accentPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                        }

                        Text(
                            text = if (viewModel.isRussian) "💡 Заряды: ${viewModel.freeHintCharges}" else "💡 Hints: ${viewModel.freeHintCharges}",
                            color = if (isPaper) Color(0xFF2E7D32) else Color(0xFF81C784),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    MiniPieceBox(
                        label = if (viewModel.isRussian) "СЛЕД" else "NEXT",
                        shapeType = viewModel.nextPieceShapeType,
                        tint = accentPrimary,
                        isPaper = isPaper
                    )
                }

                // Paper Theme Toggle + Encyclopedia Album + Large Font + Words Counter
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Paper / Night Theme quick toggle ("📰" / "🌙")
                    IconButton(
                        onClick = { viewModel.togglePaperTheme() },
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(innerBg)
                            .testTag("paper_theme_button")
                    ) {
                        Text(
                            text = if (isPaper) "📰" else "🌙",
                            fontSize = 13.sp
                        )
                    }

                    // Family Word Album quick button ("📖")
                    IconButton(
                        onClick = { viewModel.showEncyclopediaModal = true },
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(innerBg)
                            .testTag("open_album_button")
                    ) {
                        Text(
                            text = "📖",
                            fontSize = 13.sp
                        )
                    }

                    // Large Font toggle ("👓")
                    IconButton(
                        onClick = { viewModel.toggleLargeFontMode() },
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (viewModel.isLargeFontMode) accentGold.copy(alpha = 0.22f)
                                else innerBg
                            )
                            .testTag("large_font_button")
                    ) {
                        Text(
                            text = "👓",
                            fontSize = 13.sp
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (viewModel.isRussian) "СЛОВ" else "WORDS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = textSecondary,
                                fontWeight = FontWeight.Medium,
                                fontSize = 9.sp
                            )
                        )
                        Text(
                            text = "$wordsCount",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = if (isPaper) Color(0xFF2E7D32) else Color(0xFF81C784),
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )
                        )
                    }
                }
            }

            // Stage 1 -> Stage 2 -> Stage 3: Live Crossword Stencil Banner ("Кроссворд-Трафарет на поле")
            viewModel.questWord?.let { quest ->
                if (viewModel.currentMode == GameMode.TETRIS) {
                    val board = viewModel.engine.board
                    val slots = viewModel.stencilSlots.filter { it.isHorizontal }
                    val stencilPreview = if (slots.isNotEmpty()) {
                        slots.joinToString(" ") { slot ->
                            val c = if (viewModel.engine.isValidPoint(slot.x, slot.y)) board[slot.y][slot.x] else null
                            if (c != null && (c.char == slot.expectedChar || c.char == Dictionary.WILDCARD_CHAR)) {
                                c.char.toString()
                            } else if (viewModel.isSingleTileMode || slot.isHintRevealed) {
                                "[${slot.expectedChar}]"
                            } else {
                                "[?]"
                            }
                        }
                    } else {
                        quest.word.map { "?" }.joinToString(" ")
                    }

                    val secQuest = if (viewModel.isCrossedStencilMode) viewModel.secondaryQuestWord else null
                    val vSlots = viewModel.stencilSlots.filter { !it.isHorizontal }
                    val secPreview = if (secQuest != null) {
                        if (vSlots.isNotEmpty()) {
                            "${secQuest.word.length} б."
                        } else {
                            "${secQuest.word.length} б."
                        }
                    } else ""

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(innerBg)
                            .border(1.5.dp, accentGold.copy(alpha = 0.7f), RoundedCornerShape(10.dp))
                            .clickable { viewModel.openRiddleSpotlight() }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (secQuest != null) {
                                    if (viewModel.isRussian) "✚ Крест: ↔ $stencilPreview | ↕ $secPreview"
                                    else "✚ Cross: ↔ $stencilPreview | ↕ ${secQuest.word.length}L"
                                } else {
                                    if (viewModel.isRussian) "🧩 Загадка (${quest.category.ifBlank { "Слово" }}): $stencilPreview"
                                    else "🧩 Puzzle (${quest.category.ifBlank { "Word" }}): $stencilPreview"
                                },
                                color = accentGold,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = if (viewModel.isLargeFontMode) 14.sp else 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // 🔊 Voice Readout Button (TextToSpeech)
                                FilledTonalButton(
                                    onClick = { viewModel.speakActiveRiddle() },
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = accentGold.copy(alpha = 0.2f),
                                        contentColor = accentGold
                                    ),
                                    border = BorderStroke(1.dp, accentGold),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                    modifier = Modifier
                                        .height(28.dp)
                                        .testTag("speak_riddle_button")
                                ) {
                                    Text(
                                        text = "🔊",
                                        fontSize = 11.sp
                                    )
                                }

                                // 💡 Reveal '?' Letter Hint in Stencil Slot (without auto-placing)
                                FilledTonalButton(
                                    onClick = { viewModel.revealStencilHint() },
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = accentGold.copy(alpha = 0.16f),
                                        contentColor = accentGold
                                    ),
                                    border = BorderStroke(1.dp, accentGold),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                    modifier = Modifier
                                        .height(28.dp)
                                        .testTag("stencil_hint_button")
                                ) {
                                    Text(
                                        text = "💡?",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                }

                                // 🧲 Magnet Helper Button
                                FilledTonalButton(
                                    onClick = { viewModel.useMagnetHelper() },
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = Color(0xFF00C853).copy(alpha = 0.22f),
                                        contentColor = if (isPaper) Color(0xFF1B5E20) else Color(0xFF00E676)
                                    ),
                                    border = BorderStroke(1.dp, Color(0xFF00C853)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 7.dp, vertical = 2.dp),
                                    modifier = Modifier
                                        .height(28.dp)
                                        .testTag("magnet_helper_button")
                                ) {
                                    Text(
                                        text = if (viewModel.isRussian) "🧲 Магнит" else "🧲 Magnet",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                }

                                // ↩️ Undo Move Button
                                OutlinedButton(
                                    onClick = { viewModel.undoMove() },
                                    enabled = viewModel.canUndo,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, borderCol),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                    modifier = Modifier
                                        .height(28.dp)
                                        .testTag("undo_move_button")
                                ) {
                                    Text(
                                        text = "↩️",
                                        fontSize = 10.sp
                                    )
                                }

                                // 🔄 Skip Stencil Button
                                OutlinedButton(
                                    onClick = { viewModel.skipStencilPuzzle() },
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, borderCol),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                    modifier = Modifier
                                        .height(28.dp)
                                        .testTag("skip_stencil_button")
                                ) {
                                    Text(
                                        text = "🔄",
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }

                        // Large readable clue text for seniors, moms, and children (16sp in Large Font mode!)
                        Text(
                            text = if (secQuest != null) {
                                "↔ ${quest.definition}\n↕ ${secQuest.definition}"
                            } else {
                                quest.definition
                            },
                            color = textPrimary,
                            fontSize = if (viewModel.isLargeFontMode) 16.sp else 12.sp,
                            lineHeight = if (viewModel.isLargeFontMode) 20.sp else 16.sp,
                            fontWeight = FontWeight.SemiBold,
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
private fun MiniPieceBox(label: String, shapeType: Int?, tint: Color, isPaper: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = label,
            color = if (isPaper) Color(0xFF5D4037) else Color(0xFFCAC4D0),
            fontSize = 8.sp,
            fontWeight = FontWeight.SemiBold
        )
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(if (isPaper) Color(0xFFF3EBD8) else Color(0xFF1C1B1F))
                .border(0.5.dp, if (isPaper) Color(0xFFD7CCC8) else Color(0xFF49454F), RoundedCornerShape(6.dp)),
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
                    color = if (isPaper) Color(0xFF8D6E63) else Color(0xFF49454F),
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
    val isPaper = viewModel.isPaperTheme
    val boardSurfaceBg = if (isPaper) Color(0xFFFFFDF9) else Color(0xFF2B2930)
    val gridLineColor = if (isPaper) Color(0xFF8D6E63).copy(alpha = 0.22f) else Color(0xFF938F99).copy(alpha = 0.10f)
    val boardBorderColor = if (isPaper) Color(0xFFBCAAA4) else Color(0xFF49454F)

    key(viewModel.boardStateTick, viewModel.isUserPaused, viewModel.isLargeFontMode, viewModel.isPaperTheme) {
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

        // Crossword Stencil Slots map (x,y -> StencilSlot)
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
                    .background(boardSurfaceBg)
                    .drawBehind {
                        val strokeWidth = 1.dp.toPx()
                        val colWidth = size.width / cols
                        val rowHeight = size.height / rows

                        for (c in 1 until cols) {
                            val x = c * colWidth
                            drawLine(
                                color = gridLineColor,
                                start = androidx.compose.ui.geometry.Offset(x, 0f),
                                end = androidx.compose.ui.geometry.Offset(x, size.height),
                                strokeWidth = strokeWidth
                            )
                        }

                        for (r in 1 until rows) {
                            val y = r * rowHeight
                            drawLine(
                                color = gridLineColor,
                                start = androidx.compose.ui.geometry.Offset(0f, y),
                                end = androidx.compose.ui.geometry.Offset(size.width, y),
                                strokeWidth = strokeWidth
                            )
                        }
                    }
                    .border(1.5.dp, boardBorderColor, RoundedCornerShape(20.dp))
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
                                            activeChar != null -> if (isPaper) Color(0xFF1B365D) else Color(0xFFD0BCFF)
                                            isAnchorTile -> if (isPaper) Color(0xFFF3E5AB) else Color(0xFF4527A0)
                                            cell != null -> if (isPaper) Color(0xFFEDE0D4) else Color(0xFF49454F)
                                            isOpenStencilTarget -> Color(0xFFFFD54F).copy(alpha = if (isPaper) 0.45f else 0.25f)
                                            isGhost -> if (isPaper) Color(0xFF1B365D).copy(alpha = 0.14f) else Color(0xFFD0BCFF).copy(alpha = 0.14f)
                                            else -> Color.Transparent
                                        }
                                    )
                                    .then(
                                        when {
                                            isRadar -> Modifier.border(2.dp, Color(0xFF00E676), RoundedCornerShape(4.dp))
                                            isOpenStencilTarget -> Modifier.border(
                                                2.dp,
                                                Color(0xFFD97706),
                                                RoundedCornerShape(4.dp)
                                            )
                                            isWildcard && (activeChar != null || cell != null) -> Modifier.border(
                                                1.5.dp,
                                                Color(0xFFD97706),
                                                RoundedCornerShape(4.dp)
                                            )
                                            activeChar != null -> Modifier.border(
                                                1.5.dp,
                                                if (isPaper) Color(0xFFD97706) else Color.White,
                                                RoundedCornerShape(4.dp)
                                            )
                                            isAnchorTile -> Modifier.border(
                                                1.5.dp,
                                                if (isPaper) Color(0xFF8D6E63) else Color(0xFFFFD54F).copy(alpha = 0.88f),
                                                RoundedCornerShape(4.dp)
                                            )
                                            cell != null -> Modifier.border(
                                                1.dp,
                                                if (isPaper) Color(0xFFBCAAA4) else Color(0xFFD0BCFF).copy(alpha = 0.4f),
                                                RoundedCornerShape(4.dp)
                                            )
                                            isGhost -> Modifier.border(
                                                1.dp,
                                                if (isPaper) Color(0xFF1B365D).copy(alpha = 0.4f) else Color(0xFFD0BCFF).copy(alpha = 0.45f),
                                                RoundedCornerShape(4.dp)
                                            )
                                            else -> Modifier
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                val displayText = when {
                                    viewModel.isUserPaused -> ""
                                    activeChar != null -> activeChar.toString()
                                    cell != null -> cell.char.toString()
                                    isOpenStencilTarget -> {
                                        if (viewModel.isSingleTileMode || stencilSlot?.isHintRevealed == true) {
                                            stencilSlot?.expectedChar?.toString() ?: "?"
                                        } else {
                                            "?"
                                        }
                                    }
                                    else -> ""
                                }
                                if (displayText.isNotEmpty()) {
                                    Text(
                                        text = displayText,
                                        fontSize = if (viewModel.isLargeFontMode) 17.sp else 13.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontFamily = FontFamily.Monospace,
                                        color = when {
                                            isWildcard -> Color(0xFF1C1B1F)
                                            activeChar != null -> if (isPaper) Color.White else Color(0xFF381E72)
                                            isOpenStencilTarget -> if (isPaper) Color(0xFFB45309) else Color(0xFFFFD54F).copy(alpha = 0.92f)
                                            isAnchorTile -> if (isPaper) Color(0xFF2C1E14) else Color(0xFFFFF59D)
                                            else -> if (isPaper) Color(0xFF2C1E14) else Color(0xFFE6E1E5)
                                        },
                                        textAlign = TextAlign.Center
                                    )
                                } else if (!isGhost) {
                                    Box(
                                        modifier = Modifier
                                            .size(2.dp)
                                            .background(gridLineColor)
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
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PauseCircle,
                            contentDescription = null,
                            tint = Color(0xFFD0BCFF),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = if (viewModel.isRussian) "СТОП-КАДР / ПАУЗА" else "GAME PAUSED",
                            color = Color(0xFFD0BCFF),
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            text = if (viewModel.isRussian) {
                                "Совет: нажмите «💡?» для открытия буквы в золотой ячейке, «🔊» для озвучки загадки голосом или «🏆», чтобы сохранить свой рекорд!"
                            } else {
                                "Tip: Tap '💡?' to reveal the slot letter, '🔊' to hear the riddle aloud, or '🏆' to save your high score!"
                            },
                            color = Color(0xFFCAC4D0),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Button(
                            onClick = { viewModel.resumeFromPause() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
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
                        FilledTonalButton(
                            onClick = { viewModel.finishAndSaveScore() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("finish_and_save_score_button"),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = Color(0xFFFFD54F).copy(alpha = 0.2f),
                                contentColor = Color(0xFFFFD54F)
                            ),
                            border = BorderStroke(1.dp, Color(0xFFFFD54F)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (viewModel.isRussian) "🏆 ЗАВЕРШИТЬ И СОХРАНИТЬ СЧЁТ" else "🏆 FINISH & SAVE SCORE",
                                fontWeight = FontWeight.Bold
                            )
                        }
                        OutlinedButton(
                            onClick = { viewModel.restartGame() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
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
                            },
                            modifier = Modifier.heightIn(min = 48.dp)
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
    val isPaper = viewModel.isPaperTheme
    val panelBg = if (isPaper) Color(0xFFFFFDF9) else Color(0xFF2B2930)
    val innerBg = if (isPaper) Color(0xFFF3EBD8) else Color(0xFF1C1B1F)
    val borderCol = if (isPaper) Color(0xFFD7CCC8) else Color(0xFF49454F)
    val textPrimary = if (isPaper) Color(0xFF2C1E14) else Color.White
    val accentPrimary = if (isPaper) Color(0xFF1B365D) else Color(0xFFD0BCFF)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(20.dp))
            .background(panelBg)
            .border(1.dp, borderCol, RoundedCornerShape(20.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Direct Word Solver / Gift custom word entry field + Custom Family Word Creator Button
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = innerBg),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedTextField(
                    value = giftInput,
                    onValueChange = { onGiftInputChange(it.uppercase()) },
                    label = {
                        Text(
                            text = if (viewModel.isRussian) "✍️ Ответ загадки или слово" else "✍️ Riddle answer or word",
                            fontSize = 10.sp
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("gift_word_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentPrimary,
                        unfocusedBorderColor = textPrimary.copy(alpha = 0.25f),
                        focusedLabelColor = accentPrimary,
                        unfocusedLabelColor = textPrimary.copy(alpha = 0.55f),
                        focusedTextColor = textPrimary,
                        unfocusedTextColor = textPrimary
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
                        .height(38.dp)
                        .testTag("gift_word_submit"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accentPrimary,
                        contentColor = if (isPaper) Color.White else Color(0xFF381E72)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp)
                ) {
                    Text(
                        text = if (viewModel.isRussian) "Внести" else "Submit",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }

                // ✍️ Family Custom Riddle Creator Shortcut
                FilledTonalButton(
                    onClick = { viewModel.showCustomWordCreatorModal = true },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color(0xFF00C853).copy(alpha = 0.18f),
                        contentColor = if (isPaper) Color(0xFF1B5E20) else Color(0xFF00E676)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    modifier = Modifier
                        .height(38.dp)
                        .testTag("open_custom_riddle_creator_button")
                ) {
                    Text(
                        text = "✍️+",
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Row 1: Tactical Manipulation (Undo Step, Hold Pocket, Rotate Piece, Shift Piece Letters)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ↩️ UNDO STEP BUTTON
            FilledTonalButton(
                onClick = { viewModel.undoMove() },
                enabled = viewModel.canUndo,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = innerBg,
                    contentColor = if (isPaper) Color(0xFFB45309) else Color(0xFFFFD54F)
                ),
                border = BorderStroke(1.dp, borderCol),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                modifier = Modifier
                    .height(48.dp)
                    .testTag("ctrl_undo")
            ) {
                Text(
                    text = if (viewModel.isRussian) "↩️ Назад" else "↩️ Undo",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // HOLD POCKET BUTTON
            FilledTonalButton(
                onClick = { viewModel.holdPiece() },
                enabled = viewModel.canHoldThisTurn,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = innerBg,
                    contentColor = if (isPaper) Color(0xFFD97706) else Color(0xFFFFB74D)
                ),
                border = BorderStroke(1.dp, borderCol),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                modifier = Modifier
                    .height(48.dp)
                    .testTag("ctrl_hold")
            ) {
                Icon(
                    imageVector = Icons.Default.Inventory2,
                    contentDescription = if (viewModel.isRussian) "Отложить фигуру в карман" else "Hold Piece",
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
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
                    .background(accentPrimary)
                    .testTag("ctrl_rotate")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.RotateRight,
                    contentDescription = if (viewModel.isRussian) "Повернуть фигуру" else "Rotate Shape",
                    tint = if (isPaper) Color.White else Color(0xFF381E72),
                    modifier = Modifier.size(24.dp)
                )
            }

            // SHIFT LETTERS INSIDE PIECE BUTTON
            FilledTonalButton(
                onClick = { viewModel.shiftPieceLetters() },
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = innerBg,
                    contentColor = if (isPaper) Color(0xFF2E7D32) else Color(0xFF81C784)
                ),
                border = BorderStroke(1.dp, Color(0xFF81C784).copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                modifier = Modifier
                    .height(48.dp)
                    .testTag("ctrl_shift_letters")
            ) {
                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = if (viewModel.isRussian) "Сдвинуть буквы внутри фигуры" else "Cycle Letters",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = if (viewModel.isRussian) "Сдвиг" else "Shift",
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
                    .background(if (isPaper) Color(0xFFEDE0D4) else Color(0xFF49454F))
                    .testTag("ctrl_left")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = if (viewModel.isRussian) "Влево" else "Move Left",
                    tint = textPrimary,
                    modifier = Modifier.size(30.dp)
                )
            }

            IconButton(
                onClick = { viewModel.moveDown() },
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(if (isPaper) Color(0xFFEDE0D4) else Color(0xFF49454F))
                    .testTag("ctrl_down")
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = if (viewModel.isRussian) "Шаг вниз" else "Step Down",
                    tint = textPrimary,
                    modifier = Modifier.size(30.dp)
                )
            }

            Button(
                onClick = { viewModel.dropPiece() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentPrimary,
                    contentColor = if (isPaper) Color.White else Color(0xFF381E72)
                ),
                modifier = Modifier
                    .height(48.dp)
                    .testTag("ctrl_drop"),
                shape = RoundedCornerShape(24.dp),
                contentPadding = PaddingValues(horizontal = 14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardDoubleArrowDown,
                    contentDescription = if (viewModel.isRussian) "Поставить фигуру" else "Place Piece",
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
                    .background(if (isPaper) Color(0xFFEDE0D4) else Color(0xFF49454F))
                    .testTag("ctrl_right")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = if (viewModel.isRussian) "Вправо" else "Move Right",
                    tint = textPrimary,
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
                            fontSize = if (viewModel.isLargeFontMode) 14.sp else 12.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (!clue.isSolved) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Voice Readout
                                OutlinedButton(
                                    onClick = { viewModel.speakText(clue.clue) },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp),
                                    border = BorderStroke(1.dp, Color(0xFFFFD54F).copy(alpha = 0.5f))
                                ) {
                                    Text(
                                        text = "🔊",
                                        fontSize = 9.sp
                                    )
                                }

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
