package com.example.game

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.GameDatabase
import com.example.data.database.GameRepository
import com.example.data.database.HighScoreEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: GameRepository

    // Central Game Engine
    val engine = GameEngine(cols = 10, rows = 15, isRussian = true)

    // Observable States to trigger recompositions in Jetpack Compose
    var currentScore by mutableStateOf(0)
    var comboCount by mutableStateOf(0)
    var wordsClearedCount by mutableStateOf(0)
    var freeHintCharges by mutableStateOf(3)
    var currentMode by mutableStateOf(GameMode.TETRIS)
    var paceMode by mutableStateOf(PaceMode.PUZZLE_STEP)
    var isRussian by mutableStateOf(true)
    var activePiece by mutableStateOf<ActivePiece?>(null)
    var nextPieceShapeType by mutableStateOf(0)
    var heldPieceShapeType by mutableStateOf<Int?>(null)
    var canHoldThisTurn by mutableStateOf(true)
    var questWord by mutableStateOf<WordDefinition?>(null)
    var stencilSlots by mutableStateOf<List<StencilSlot>>(emptyList())
    var targetWords = mutableListOf<String>()
    var giftLetters = mutableListOf<Char>()
    var crosswordClues = mutableListOf<CrosswordClue>()
    var boardStateTick by mutableStateOf(0) // incremented to force recompose on array updates

    // Explicit User Pause State
    var isUserPaused by mutableStateOf(false)

    // Input errors or tips
    var toastMessage by mutableStateOf<String?>(null)

    // High Scores list
    val topScores: StateFlow<List<HighScoreEntity>>

    // Game timer / Loop Job
    private var gameLoopJob: Job? = null
    var isPlaying by mutableStateOf(false)

    init {
        val database = GameDatabase.getDatabase(application)
        repository = GameRepository(database.gameDao())
        topScores = repository.topScores.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        syncStatesFromEngine()
        attemptAutoLoadSavedGame()
    }

    /**
     * Synergizes local observable compose states with current engine status
     */
    fun syncStatesFromEngine() {
        currentScore = engine.score
        comboCount = engine.comboCount
        wordsClearedCount = engine.wordsClearedCount
        freeHintCharges = engine.freeHintCharges
        currentMode = engine.mode
        paceMode = engine.paceMode
        isRussian = engine.isRussian
        activePiece = engine.activePiece
        nextPieceShapeType = engine.nextPieceShapeType
        heldPieceShapeType = engine.heldPieceShapeType
        canHoldThisTurn = engine.canHoldThisTurn
        questWord = engine.questWordDefinition
        stencilSlots = engine.stencilSlots.toList()
        targetWords = engine.levelTargetWords.toMutableList()
        giftLetters = engine.giftLettersQueue.toMutableList()
        crosswordClues = engine.crosswordClues.toMutableList()

        engine.lastResonanceWord?.let { resonated ->
            showToast(
                if (isRussian) "✨ СЛОВО «$resonated» СОБРАНО! Линия исчезла! +250 очков и +1 подсказка!"
                else "✨ WORD \"$resonated\" COMPLETED! Line vanished! +250 pts & +1 Free Hint!"
            )
            engine.lastResonanceWord = null
        }

        boardStateTick++
    }

    private fun attemptAutoLoadSavedGame() {
        viewModelScope.launch {
            try {
                val savedJson = repository.loadSavedGame()
                if (savedJson != null) {
                    GameStateSerializer.deserialize(savedJson, engine)
                    syncStatesFromEngine()
                    showToast(if (isRussian) "Предыдущая игра восстановлена!" else "Previous game restored!")
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    fun saveGameProgress() {
        viewModelScope.launch {
            try {
                val json = GameStateSerializer.serialize(engine)
                repository.saveGame(json)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    fun submitHighScore(playerName: String) {
        viewModelScope.launch {
            if (playerName.isNotBlank()) {
                repository.insertHighScore(playerName, currentScore)
                repository.deleteSavedGame()
            }
        }
    }

    fun clearScores() {
        viewModelScope.launch {
            repository.clearLeaderboard()
        }
    }

    /**
     * Toggles bilingual vocabulary settings
     */
    fun toggleLanguage() {
        engine.isRussian = !engine.isRussian
        engine.skipToNextStencilPuzzle()
        if (engine.mode == GameMode.CROSSWORD) {
            engine.scanBoardForCrosswordWords()
        }
        syncStatesFromEngine()
        showToast(if (engine.isRussian) "Язык изменён на Русский" else "Language changed to English")
        saveGameProgress()
    }

    /**
     * Sets or cycles the Pace Mode (Puzzle Step / Relaxed / Arcade)
     */
    fun updatePaceMode(newPace: PaceMode) {
        engine.paceMode = newPace
        syncStatesFromEngine()
        pauseGameLoop()
        if (engine.mode == GameMode.TETRIS && !isUserPaused) {
            startGameLoop()
        }
        saveGameProgress()
        val msg = when (newPace) {
            PaceMode.PUZZLE_STEP -> if (isRussian) "🌿 Режим «Пазл без таймера»: фигуры ждут вашего хода!" else "🌿 Puzzle Mode: No timer, move at your own pace!"
            PaceMode.RELAXED -> if (isRussian) "🐢 Режим «Очень медленно»: плавное спокойное падение" else "🐢 Relaxed Mode: Very slow gentle falling"
            PaceMode.ARCADE -> if (isRussian) "⚡ Режим «Аркада»: классическая скорость Тетриса" else "⚡ Arcade Mode: Classic Tetris speed"
        }
        showToast(msg)
    }

    fun cyclePaceMode() {
        val next = when (engine.paceMode) {
            PaceMode.PUZZLE_STEP -> PaceMode.RELAXED
            PaceMode.RELAXED -> PaceMode.ARCADE
            PaceMode.ARCADE -> PaceMode.PUZZLE_STEP
        }
        updatePaceMode(next)
    }

    /**
     * Smart Magnet Helper for kids, moms, and seniors:
     * Places the missing letter right into the Crossword Stencil on the board!
     */
    fun useMagnetHelper() {
        if (engine.mode == GameMode.TETRIS && !isUserPaused) {
            if (engine.useMagnetToFillStencilSlot()) {
                syncStatesFromEngine()
                saveGameProgress()
            } else {
                showToast(
                    if (isRussian) "Все ячейки трафарета уже заполнены!"
                    else "All stencil slots are already filled!"
                )
            }
        }
    }

    /**
     * Skips to a fresh Crossword Stencil riddle on the board
     */
    fun skipStencilPuzzle() {
        if (engine.mode == GameMode.TETRIS && !isUserPaused) {
            engine.skipToNextStencilPuzzle()
            syncStatesFromEngine()
            saveGameProgress()
            showToast(
                if (isRussian) "🔄 Новая загадка-трафарет размещена на поле!"
                else "🔄 New crossword stencil placed on the board!"
            )
        }
    }

    /**
     * Toggle user pause modal and loop
     */
    fun toggleUserPause() {
        isUserPaused = !isUserPaused
        if (isUserPaused) {
            pauseGameLoop()
            saveGameProgress()
        } else {
            if (engine.mode == GameMode.TETRIS) {
                startGameLoop()
            }
        }
    }

    fun resumeFromPause() {
        if (isUserPaused) {
            isUserPaused = false
            if (engine.mode == GameMode.TETRIS) {
                startGameLoop()
            }
        }
    }

    /**
     * Tetris Game Loop controller — respects PUZZLE_STEP (no auto-drop), RELAXED (2600ms), and ARCADE
     */
    fun startGameLoop() {
        if (isPlaying || isUserPaused) return
        // In PUZZLE_STEP mode, pieces never fall automatically!
        if (engine.paceMode == PaceMode.PUZZLE_STEP) {
            isPlaying = false
            gameLoopJob?.cancel()
            gameLoopJob = null
            return
        }

        isPlaying = true
        gameLoopJob = viewModelScope.launch {
            while (isPlaying && !isUserPaused && engine.mode == GameMode.TETRIS && engine.paceMode != PaceMode.PUZZLE_STEP) {
                val delayTime = when (engine.paceMode) {
                    PaceMode.PUZZLE_STEP -> 3000L
                    PaceMode.RELAXED -> 2600L // Very slow, constant, calm speed without acceleration
                    PaceMode.ARCADE -> maxOf(300L, (800 - (engine.score / 250) * 40).toLong())
                }
                delay(delayTime)

                if (engine.mode == GameMode.TETRIS && isPlaying && !isUserPaused && engine.paceMode != PaceMode.PUZZLE_STEP) {
                    val moved = engine.movePiece(0, 1)
                    if (!moved) {
                        engine.lockPiece()
                        saveGameProgress()
                    }
                    syncStatesFromEngine()
                }
            }
            if (engine.mode == GameMode.CROSSWORD) {
                isPlaying = false
            }
        }
    }

    fun pauseGameLoop() {
        isPlaying = false
        gameLoopJob?.cancel()
        gameLoopJob = null
    }

    // --- GAME ACTIONS ---

    fun moveLeft() {
        if (engine.mode == GameMode.TETRIS && !isUserPaused) {
            engine.movePiece(-1, 0)
            syncStatesFromEngine()
        }
    }

    fun moveRight() {
        if (engine.mode == GameMode.TETRIS && !isUserPaused) {
            engine.movePiece(1, 0)
            syncStatesFromEngine()
        }
    }

    fun moveDown() {
        if (engine.mode == GameMode.TETRIS && !isUserPaused) {
            val moved = engine.movePiece(0, 1)
            if (!moved) {
                engine.lockPiece()
                saveGameProgress()
            }
            syncStatesFromEngine()
        }
    }

    fun dropPiece() {
        if (engine.mode == GameMode.TETRIS && !isUserPaused) {
            engine.hardDrop()
            saveGameProgress()
            syncStatesFromEngine()
        }
    }

    fun rotatePiece() {
        if (engine.mode == GameMode.TETRIS && !isUserPaused) {
            engine.rotatePiece()
            syncStatesFromEngine()
        }
    }

    /**
     * Cyclically shifts the 4 letters inside the falling piece
     */
    fun shiftPieceLetters() {
        if (engine.mode == GameMode.TETRIS && !isUserPaused) {
            if (engine.shiftPieceLetters()) {
                syncStatesFromEngine()
            }
        }
    }

    /**
     * Swaps the active falling piece with the Hold Pocket slot
     */
    fun holdPiece() {
        if (engine.mode == GameMode.TETRIS && !isUserPaused) {
            if (engine.holdCurrentPiece()) {
                syncStatesFromEngine()
                showToast(
                    if (isRussian) "📥 Фигура отложена в карман!"
                    else "📥 Piece swapped to Hold pocket!"
                )
            } else {
                showToast(
                    if (isRussian) "Карман уже использован на этом ходу!"
                    else "Hold already used this turn!"
                )
            }
        }
    }

    /**
     * Progressive Hint 1: Opens 1 letter of the crossword word (-15 pts or 1 free hint charge)
     */
    fun revealOneLetterHint(clueId: String) {
        if (engine.mode == GameMode.CROSSWORD) {
            val usedFree = engine.freeHintCharges > 0
            if (engine.revealNextLetterHint(clueId)) {
                syncStatesFromEngine()
                saveGameProgress()
                showToast(
                    if (isRussian) {
                        if (usedFree) "💡 Открыта буква (бесплатный заряд)!" else "💡 Открыта буква (-15 очков)"
                    } else {
                        if (usedFree) "💡 Letter revealed (Free Hint)!" else "💡 Letter revealed (-15 pts)"
                    }
                )
            }
        }
    }

    /**
     * Progressive Hint 2: Highlights the word's exact cells on the 2D board ("Word Radar")
     */
    fun toggleWordRadar(clueId: String) {
        if (engine.mode == GameMode.CROSSWORD) {
            val usedFree = engine.freeHintCharges > 0
            if (engine.activateWordRadar(clueId)) {
                syncStatesFromEngine()
                showToast(
                    if (isRussian) {
                        if (usedFree) "📡 Радар слова включён на поле (бесплатно)!" else "📡 Радар подсветил слово на поле (-25 очков)"
                    } else {
                        if (usedFree) "📡 Word Radar highlighted on grid (Free)!" else "📡 Word Radar active on grid (-25 pts)"
                    }
                )
            }
        }
    }

    /**
     * Spends 50 points to reveal a crossword word completely and clear it
     */
    fun payToRevealClue(clueId: String) {
        if (engine.mode == GameMode.CROSSWORD) {
            val success = engine.attemptRevealWord(clueId)
            if (success) {
                syncStatesFromEngine()
                saveGameProgress()
                showToast(if (isRussian) "🔓 Слово полностью открыто и линия очищена (-50 очков)!" else "🔓 Word revealed & line cleared (-50 pts)!")
            }
        }
    }

    /**
     * Submits a text guess for a specific crossword clue list item
     */
    fun guessClue(clueId: String, guess: String) {
        if (engine.mode == GameMode.CROSSWORD) {
            val correct = engine.attemptGuessWord(clueId, guess)
            if (correct) {
                syncStatesFromEngine()
                saveGameProgress()
                showToast(
                    if (isRussian) "✅ Правильно! Линия исчезла! +${150 + (comboCount - 1) * 30} очков"
                    else "✅ Correct! Line cleared! +${150 + (comboCount - 1) * 30} pts"
                )
            } else {
                syncStatesFromEngine()
                showToast(if (isRussian) "❌ Неправильно, попробуйте ещё раз" else "❌ Incorrect, try again")
            }
        }
    }

    /**
     * User enters a custom word (or guesses the active Stencil word directly!).
     */
    fun submitGiftWord(word: CharSequence) {
        val text = word.toString().uppercase().trim()
        if (text.length < 2) {
            showToast(if (isRussian) "Слово слишком короткое!" else "Word is too short!")
            return
        }

        // First check if the user typed the exact answer to the active Crossword Stencil on the board!
        if (engine.solveActiveStencilByWord(text)) {
            syncStatesFromEngine()
            saveGameProgress()
            return
        }

        val success = engine.addGiftWord(text)
        if (success) {
            syncStatesFromEngine()
            saveGameProgress()
            showToast(
                if (isRussian) "🎁 Слово в подарок! +${text.length * 25} очков. Буквы добавлены в очередь."
                else "🎁 Gift word queued! +${text.length * 25} pts. Letters loaded."
            )
        } else {
            showToast(
                if (isRussian) "Слово «$text» не найдено в словаре!"
                else "Word \"$text\" was not found in dictionary!"
            )
        }
    }

    /**
     * Check if the user can skip / resume Tetris
     */
    fun resumeTetrisAfterCrossword() {
        if (engine.canResumeTetris()) {
            engine.mode = GameMode.TETRIS
            engine.spawnPiece()
            syncStatesFromEngine()
            startGameLoop()
            saveGameProgress()
            showToast(if (isRussian) "Складывание пазлов возобновлено!" else "Puzzle folding resumed!")
        } else {
            engine.forceLowerStackAndResume()
            syncStatesFromEngine()
            startGameLoop()
            saveGameProgress()
            showToast(
                if (isRussian) "Верхние ряды расчищены, игра продолжается!"
                else "Top rows cleared, game resumed!"
            )
        }
    }

    /**
     * Completely resets current game board
     */
    fun restartGame() {
        isUserPaused = false
        pauseGameLoop()
        engine.resetGame()
        syncStatesFromEngine()
        startGameLoop()
        saveGameProgress()
        showToast(if (isRussian) "Новая игра начата!" else "New game started!")
    }

    fun showToast(msg: String) {
        toastMessage = msg
    }

    fun clearToast() {
        toastMessage = null
    }

    override fun onCleared() {
        super.onCleared()
        pauseGameLoop()
    }
}
