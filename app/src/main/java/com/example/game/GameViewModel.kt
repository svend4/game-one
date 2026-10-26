package com.example.game

import android.app.Application
import android.speech.tts.TextToSpeech
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.CustomFamilyWordEntity
import com.example.data.database.GameDatabase
import com.example.data.database.GameRepository
import com.example.data.database.HighScoreEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: GameRepository

    // Central Game Engine
    val engine = GameEngine(cols = 10, rows = 15, isRussian = true)

    // Android Text-To-Speech for reading riddles, words, and fun facts aloud to seniors & children
    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    // Observable States to trigger recompositions in Jetpack Compose
    var currentScore by mutableIntStateOf(0)
    var comboCount by mutableIntStateOf(0)
    var wordsClearedCount by mutableIntStateOf(0)
    var freeHintCharges by mutableIntStateOf(3)
    var currentMode by mutableStateOf(GameMode.TETRIS)
    var paceMode by mutableStateOf(PaceMode.PUZZLE_STEP)
    var selectedTheme by mutableStateOf(WordTheme.ALL)
    var isSingleTileMode by mutableStateOf(false)
    var isLargeFontMode by mutableStateOf(true)
    var isPaperTheme by mutableStateOf(true)
    var isCrossedStencilMode by mutableStateOf(false)
    var canUndo by mutableStateOf(false)

    // Modals & Celebration triggers
    var showRiddleSpotlight by mutableStateOf(false)
    var showEncyclopediaModal by mutableStateOf(false)
    var showCustomWordCreatorModal by mutableStateOf(false)
    var showDailyWordModal by mutableStateOf(false)
    var celebrationTriggerTick by mutableIntStateOf(0)
    var dailyWordCompletedToday by mutableStateOf(false)

    var isRussian by mutableStateOf(true)
    var activePiece by mutableStateOf<ActivePiece?>(null)
    var nextPieceShapeType by mutableIntStateOf(0)
    var heldPieceShapeType by mutableStateOf<Int?>(null)
    var canHoldThisTurn by mutableStateOf(true)
    var questWord by mutableStateOf<WordDefinition?>(null)
    var secondaryQuestWord by mutableStateOf<WordDefinition?>(null)
    var stencilSlots by mutableStateOf<List<StencilSlot>>(emptyList())
    var unlockedWordsSet by mutableStateOf<Set<String>>(emptySet())
    var targetWords = mutableListOf<String>()
    var giftLetters = mutableListOf<Char>()
    var crosswordClues = mutableListOf<CrosswordClue>()
    var boardStateTick by mutableIntStateOf(0) // incremented to force recompose on array updates

    // Explicit User Pause State & Active Screen tracking (prevents background loop in Menu!)
    var isUserPaused by mutableStateOf(false)
    var isGameScreenActive by mutableStateOf(false)

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

        initTextToSpeech(application)
        syncStatesFromEngine()
        loadPersistentFamilyDataAndSavedGame()
    }

    private fun initTextToSpeech(application: Application) {
        runCatching {
            tts = TextToSpeech(application.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    isTtsReady = true
                    tts?.setSpeechRate(0.92f)
                }
            }
        }
    }

    /**
     * Speaks any riddle, word, or educational fact aloud using Android TTS (plus visual confirmation)
     */
    fun speakText(text: String) {
        if (text.isBlank()) return
        var spokenByEngine = false
        runCatching {
            val engineTts = tts
            if (isTtsReady && engineTts != null) {
                val locale = if (isRussian) Locale.forLanguageTag("ru-RU") else Locale.US
                val langStatus = engineTts.setLanguage(locale)
                if (langStatus != TextToSpeech.LANG_MISSING_DATA && langStatus != TextToSpeech.LANG_NOT_SUPPORTED) {
                    engineTts.setSpeechRate(0.92f)
                    val speakRes = engineTts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tetricross_tts")
                    spokenByEngine = (speakRes == TextToSpeech.SUCCESS)
                }
            }
        }
        if (spokenByEngine) {
            showToast(if (isRussian) "🔊 Озвучивание: $text" else "🔊 Speaking: $text")
        } else {
            showToast(if (isRussian) "💬 Загадка: $text" else "💬 Riddle: $text")
        }
    }

    /**
     * Speaks the active Crossword Stencil riddle(s) aloud for seniors and kids
     */
    fun speakActiveRiddle() {
        val primary = questWord ?: return
        val secondary = if (isCrossedStencilMode) secondaryQuestWord else null
        val speech = if (isRussian) {
            buildString {
                append("Загадка по горизонтали, ${Dictionary.russianLettersPlural(primary.word.length)}: ${primary.definition}.")
                if (secondary != null) {
                    append(" Загадка по вертикали, ${Dictionary.russianLettersPlural(secondary.word.length)}: ${secondary.definition}.")
                }
            }
        } else {
            buildString {
                append("Across riddle, ${primary.word.length} letters: ${primary.definition}.")
                if (secondary != null) {
                    append(" Down riddle, ${secondary.word.length} letters: ${secondary.definition}.")
                }
            }
        }
        speakText(speech)
    }

    fun consumeCelebrationTrigger() {
        celebrationTriggerTick = 0
    }

    fun isAnyModalOpen(): Boolean {
        return showRiddleSpotlight || showEncyclopediaModal || showCustomWordCreatorModal || showDailyWordModal
    }

    fun onAppBackgrounded() {
        pauseGameLoop()
        saveGameProgress()
    }

    /**
     * Synchronizes observable Compose states with current engine status
     */
    fun syncStatesFromEngine() {
        currentScore = engine.score
        comboCount = engine.comboCount
        wordsClearedCount = engine.wordsClearedCount
        freeHintCharges = engine.freeHintCharges
        currentMode = engine.mode
        paceMode = engine.paceMode
        selectedTheme = engine.selectedTheme
        isSingleTileMode = engine.isSingleTileMode
        isLargeFontMode = engine.isLargeFontMode
        isPaperTheme = engine.isPaperTheme
        isCrossedStencilMode = engine.isCrossedStencilMode
        dailyWordCompletedToday = engine.isDailyWordCompletedForToday()
        canUndo = engine.undoStack.isNotEmpty()
        isRussian = engine.isRussian
        activePiece = engine.activePiece
        nextPieceShapeType = engine.nextPieceShapeType
        heldPieceShapeType = engine.heldPieceShapeType
        canHoldThisTurn = engine.canHoldThisTurn
        questWord = engine.questWordDefinition
        secondaryQuestWord = engine.secondaryQuestWordDefinition
        stencilSlots = engine.stencilSlots.toList()
        targetWords = engine.levelTargetWords.toMutableList()
        giftLetters = engine.giftLettersQueue.toMutableList()
        crosswordClues = engine.crosswordClues.toMutableList()

        // Persist any newly unlocked words into the Family Encyclopedia Album
        if (engine.lastUnlockedWords.isNotEmpty()) {
            val newlyUnlocked = engine.lastUnlockedWords.toList()
            engine.lastUnlockedWords.clear()
            val updated = unlockedWordsSet.toMutableSet()
            newlyUnlocked.forEach { w ->
                updated.add(w.uppercase().trim())
            }
            unlockedWordsSet = updated
            viewModelScope.launch {
                newlyUnlocked.forEach { w ->
                    repository.insertUnlockedWord(w)
                }
            }
        }

        engine.lastResonanceWord?.let { resonated ->
            val earned = engine.lastEarnedBonus
            celebrationTriggerTick++
            if (engine.lastDailyWordJustSolved) {
                engine.lastDailyWordJustSolved = false
                showDailyWordModal = true
                showToast(
                    if (isRussian) "☀️ СЛОВО ДНЯ «$resonated» РАЗГАДАНО! +$earned очков и доброе пожелание семье!"
                    else "☀️ WORD OF THE DAY \"$resonated\" SOLVED! +$earned pts & a warm family wish!"
                )
            } else {
                showToast(
                    if (isRussian) "🎉 СЛОВО «$resonated» СОБРАНО! +$earned очков! Открыто в Альбоме слов!"
                    else "🎉 WORD \"$resonated\" COMPLETED! +$earned pts & unlocked in Word Album!"
                )
            }
            engine.lastResonanceWord = null
        }

        if (engine.lastSafetyClearTriggered) {
            showToast(
                if (isRussian) "🧹 Мягкая защита: верхние ряды бережно расчищены, игра продолжается!"
                else "🧹 Gentle Safety Net: top rows cleared so you can keep playing calmly!"
            )
            engine.lastSafetyClearTriggered = false
        }

        boardStateTick++
    }

    private fun loadPersistentFamilyDataAndSavedGame() {
        viewModelScope.launch {
            try {
                // 1. Load custom family riddles
                val customEntities = repository.getCustomFamilyWords()
                customEntities.forEach { entity ->
                    Dictionary.addCustomFamilyWord(
                        word = entity.word,
                        clue = entity.clue,
                        funFact = entity.funFact,
                        isRussian = entity.isRussian
                    )
                }

                // 2. Load unlocked encyclopedia words
                val savedUnlocked = repository.getUnlockedWords()
                if (savedUnlocked.isNotEmpty()) {
                    unlockedWordsSet = savedUnlocked
                } else {
                    val starters = setOf("ДОМ", "МАМА", "HOME", "MAMA")
                    unlockedWordsSet = starters
                    starters.forEach { repository.insertUnlockedWord(it) }
                }

                // 3. Restore saved game state
                val savedJson = repository.loadSavedGame()
                if (savedJson != null) {
                    GameStateSerializer.deserialize(savedJson, engine)
                    syncStatesFromEngine()
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
     * Finishes the current game session at any time and opens the High Score save modal.
     */
    fun finishAndSaveScore() {
        isUserPaused = false
        pauseGameLoop()
        engine.finishGameAndTriggerGameOver()
        syncStatesFromEngine()
    }

    /**
     * Undo last move ("↩️ Отменить ход") for seniors & kids
     */
    fun undoMove() {
        if (engine.undoLastMove()) {
            syncStatesFromEngine()
            saveGameProgress()
            showToast(
                if (isRussian) "↩️ Ход отменён! Поле возвращено на шаг назад."
                else "↩️ Move undone! Board restored one step back."
            )
        } else {
            showToast(
                if (isRussian) "Нет предыдущего хода для отмены"
                else "No previous move to undo"
            )
        }
    }

    /**
     * Reveals the hidden '?' letter inside the next open golden stencil slot on the board.
     */
    fun revealStencilHint() {
        if (engine.mode == GameMode.TETRIS && !isUserPaused) {
            val usedFree = engine.freeHintCharges > 0
            if (engine.revealStencilSlotLetterHint()) {
                syncStatesFromEngine()
                saveGameProgress()
                showToast(
                    if (isRussian) {
                        if (usedFree) "💡 Буква в золотой ячейке открыта (бесплатный заряд)!"
                        else "💡 Буква в золотой ячейке открыта (-15 очков)"
                    } else {
                        if (usedFree) "💡 Stencil slot letter revealed (Free Hint)!"
                        else "💡 Stencil slot letter revealed (-15 pts)"
                    }
                )
            } else {
                showToast(
                    if (isRussian) "Все буквы трафарета уже видны или не хватает зарядов!"
                    else "All stencil letters are already visible or not enough charges!"
                )
            }
        }
    }

    /**
     * Toggles between Warm Paper/Magazine Crossword Theme ("📰 Бумага") and Cozy Night Theme ("🌙 Ночь")
     */
    fun togglePaperTheme() {
        engine.isPaperTheme = !engine.isPaperTheme
        syncStatesFromEngine()
        saveGameProgress()
        showToast(
            if (engine.isPaperTheme) {
                if (isRussian) "📰 Включена светлая «Бумажная тема» в стиле журнала кроссвордов!"
                else "📰 Warm Paper Crossword Magazine theme enabled!"
            } else {
                if (isRussian) "🌙 Включена уютная вечерняя тёмная тема!"
                else "🌙 Cozy Night Dark theme enabled!"
            }
        )
    }

    /**
     * Toggles between 1-Word Stencil and Crossed 2-Word Stencil ("✚ Перекрёсток из 2 слов")
     */
    fun toggleCrossedStencilMode() {
        engine.isCrossedStencilMode = !engine.isCrossedStencilMode
        engine.skipToNextStencilPuzzle(respawnActivePiece = false)
        syncStatesFromEngine()
        saveGameProgress()
        showToast(
            if (engine.isCrossedStencilMode) {
                if (isRussian) "✚ Включён «Перекрёстный трафарет»: 2 слова с общей буквой в центре!"
                else "✚ Crossed 2-Word Stencil ON: Horizontal + Vertical words!"
            } else {
                if (isRussian) "➖ Включён классический трафарет из 1 слова"
                else "➖ Classic 1-Word Stencil ON"
            }
        )
    }

    /**
     * Creates a Custom Family Word & Riddle and places it right onto the board!
     */
    fun createAndPlayCustomFamilyWord(word: String, clue: String, funFact: String): Boolean {
        val customDef = Dictionary.addCustomFamilyWord(word, clue, funFact, isRussian) ?: return false
        viewModelScope.launch {
            repository.insertCustomFamilyWord(
                CustomFamilyWordEntity(
                    word = customDef.word,
                    clue = customDef.definition,
                    funFact = customDef.funFact,
                    isRussian = isRussian
                )
            )
        }
        engine.startSpecificWordOnBoard(customDef, isDaily = false)
        syncStatesFromEngine()
        saveGameProgress()
        showCustomWordCreatorModal = false
        showToast(
            if (isRussian) "❤️ Семейное слово «${customDef.word}» загадано и выложено на поле!"
            else "❤️ Family word \"${customDef.word}\" saved and placed on the board!"
        )
        return true
    }

    /**
     * Deletes a custom family word from Dictionary and persistent SQLite storage.
     */
    fun deleteCustomFamilyWord(word: String) {
        if (Dictionary.removeCustomFamilyWord(word)) {
            viewModelScope.launch {
                repository.deleteCustomFamilyWord(word)
            }
            boardStateTick++
            showToast(
                if (isRussian) "🗑️ Семейное слово «$word» удалено"
                else "🗑️ Custom word \"$word\" removed"
            )
        }
    }

    /**
     * Starts the special Daily Word of the Day ("☀️ Слово Дня") on the board!
     * Strictly limited to once per calendar day (fixes 2.3).
     */
    fun startDailyWordChallenge() {
        if (engine.isDailyWordCompletedForToday()) {
            showDailyWordModal = false
            showToast(
                if (isRussian) "✅ Слово Дня на сегодня уже разгадано! Возвращайтесь завтра за новым словом!"
                else "✅ Today's Word of the Day is already solved! Come back tomorrow!"
            )
            return
        }
        val dailyDef = Dictionary.getDailyWord(isRussian)
        if (engine.startSpecificWordOnBoard(dailyDef, isDaily = true)) {
            syncStatesFromEngine()
            saveGameProgress()
            showDailyWordModal = false
            showToast(
                if (isRussian) "☀️ Слово Дня размещено на трафарете! Разгадайте его для особого бонуса +500 очков!"
                else "☀️ Word of the Day placed on the board! Solve it for +500 bonus points!"
            )
        }
    }

    /**
     * Toggles bilingual vocabulary settings
     */
    fun toggleLanguage() {
        engine.isRussian = !engine.isRussian
        engine.skipToNextStencilPuzzle(respawnActivePiece = true)
        if (engine.mode == GameMode.CROSSWORD) {
            engine.scanBoardForCrosswordWords()
        }
        syncStatesFromEngine()
        showToast(if (engine.isRussian) "Язык изменён на Русский" else "Language changed to English")
        saveGameProgress()
    }

    /**
     * Selects a family thematic word collection without resetting the active falling piece position
     */
    fun selectTheme(theme: WordTheme) {
        engine.selectedTheme = theme
        engine.skipToNextStencilPuzzle(respawnActivePiece = false)
        if (engine.mode == GameMode.CROSSWORD) {
            engine.scanBoardForCrosswordWords()
        }
        syncStatesFromEngine()
        saveGameProgress()
        val label = when (theme) {
            WordTheme.ALL -> if (isRussian) "📚 Все темы" else "📚 All Themes"
            WordTheme.HOME_COOKING -> if (isRussian) "🏡 Дом и Кулинария" else "🏡 Home & Cooking"
            WordTheme.NATURE_ANIMALS -> if (isRussian) "🌿 Природа и Животные" else "🌿 Nature & Animals"
            WordTheme.FAIRY_TALES_KIDS -> if (isRussian) "🧚 Сказки и Детство" else "🧚 Fairy Tales & Kids"
        }
        showToast(if (isRussian) "Выбрана тема: $label" else "Selected theme: $label")
    }

    /**
     * Toggles between 1x1 Single-Tile Puzzle mode (for kids/seniors) and 4-block shapes
     */
    fun toggleSingleTileMode() {
        engine.isSingleTileMode = !engine.isSingleTileMode
        engine.spawnPiece()
        syncStatesFromEngine()
        saveGameProgress()
        showToast(
            if (engine.isSingleTileMode) {
                if (isRussian) "🧒 Включён режим «1-Плитка»: падает по одной удобной букве!"
                else "🧒 Single-Tile Mode ON: one easy letter tile at a time!"
            } else {
                if (isRussian) "🧱 Включён режим «Фигуры из 4 букв»"
                else "🧱 4-Letter Tetromino Mode ON"
            }
        )
    }

    /**
     * Toggles Senior High-Contrast Large Font Mode
     */
    fun toggleLargeFontMode() {
        engine.isLargeFontMode = !engine.isLargeFontMode
        syncStatesFromEngine()
        saveGameProgress()
        showToast(
            if (engine.isLargeFontMode) {
                if (isRussian) "👓 Крупный шрифт и повышенный контраст включены!"
                else "👓 Large Font & High Contrast enabled!"
            } else {
                if (isRussian) "Стандартный размер шрифта"
                else "Standard font size"
            }
        )
    }

    fun openRiddleSpotlight() {
        showRiddleSpotlight = true
    }

    fun dismissRiddleSpotlight() {
        showRiddleSpotlight = false
    }

    /**
     * Sets or cycles the Pace Mode (Puzzle Step / Relaxed / Arcade).
     * Never starts the background drop loop while the player is on the Menu screen!
     */
    fun updatePaceMode(newPace: PaceMode) {
        engine.paceMode = newPace
        syncStatesFromEngine()
        pauseGameLoop()
        if (isGameScreenActive && engine.mode == GameMode.TETRIS && !isUserPaused) {
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
                    if (isRussian) "Магнит требует 1 заряд 💡 (или 50 очков) и свободную ячейку трафарета!"
                    else "Magnet requires 1 hint charge 💡 (or 50 pts) and an open stencil slot!"
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
            if (isGameScreenActive && engine.mode == GameMode.TETRIS) {
                startGameLoop()
            }
        }
    }

    fun resumeFromPause() {
        if (isUserPaused) {
            isUserPaused = false
            if (isGameScreenActive && engine.mode == GameMode.TETRIS) {
                startGameLoop()
            }
        }
    }

    /**
     * Tetris Game Loop controller — respects isGameScreenActive, PUZZLE_STEP (no auto-drop), RELAXED (2600ms), and ARCADE
     */
    fun startGameLoop() {
        if (!isGameScreenActive || isPlaying || isUserPaused || isAnyModalOpen()) return
        if (engine.paceMode == PaceMode.PUZZLE_STEP) {
            isPlaying = false
            gameLoopJob?.cancel()
            gameLoopJob = null
            return
        }

        isPlaying = true
        gameLoopJob = viewModelScope.launch {
            while (isGameScreenActive && isPlaying && !isUserPaused && !isAnyModalOpen() && engine.mode == GameMode.TETRIS && engine.paceMode != PaceMode.PUZZLE_STEP) {
                val delayTime = when (engine.paceMode) {
                    PaceMode.PUZZLE_STEP -> 3000L
                    PaceMode.RELAXED -> 2600L
                    PaceMode.ARCADE -> maxOf(300L, (800 - (engine.score / 250) * 40).toLong())
                }
                delay(delayTime)

                if (isGameScreenActive && engine.mode == GameMode.TETRIS && isPlaying && !isUserPaused && !isAnyModalOpen() && engine.paceMode != PaceMode.PUZZLE_STEP) {
                    val moved = engine.movePiece(0, 1)
                    if (!moved) {
                        engine.lockPiece()
                        saveGameProgress()
                    }
                    syncStatesFromEngine()
                }
            }
            if (engine.mode != GameMode.TETRIS || isAnyModalOpen()) {
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

    fun shiftPieceLetters() {
        if (engine.mode == GameMode.TETRIS && !isUserPaused) {
            if (engine.shiftPieceLetters()) {
                syncStatesFromEngine()
            }
        }
    }

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

    fun guessClue(clueId: String, guess: String) {
        if (engine.mode == GameMode.CROSSWORD) {
            val correct = engine.attemptGuessWord(clueId, guess)
            if (correct) {
                val earned = engine.lastEarnedBonus
                celebrationTriggerTick++
                syncStatesFromEngine()
                saveGameProgress()
                showToast(
                    if (isRussian) "✅ Правильно! Линия исчезла! +$earned очков"
                    else "✅ Correct! Line cleared! +$earned pts"
                )
            } else {
                syncStatesFromEngine()
                showToast(if (isRussian) "❌ Неправильно, попробуйте ещё раз" else "❌ Incorrect, try again")
            }
        }
    }

    fun submitGiftWord(word: CharSequence) {
        val text = word.toString().uppercase().trim()
        if (text.length < 3) {
            showToast(if (isRussian) "Слово должно содержать не менее 3 букв!" else "Word must be at least 3 letters!")
            return
        }

        if (engine.solveActiveStencilByWord(text)) {
            syncStatesFromEngine()
            saveGameProgress()
            return
        }

        val success = engine.addGiftWord(text)
        if (success) {
            val pts = engine.lastGiftWordPointsAwarded
            syncStatesFromEngine()
            saveGameProgress()
            if (pts > 0) {
                showToast(
                    if (isRussian) "🎁 Слово в подарок! +$pts очков. Буквы добавлены в очередь."
                    else "🎁 Gift word queued! +$pts pts. Letters loaded."
                )
            } else {
                showToast(
                    if (isRussian) "🎁 Буквы слова «$text» добавлены в очередь (очки уже получены в этой партии)."
                    else "🎁 Letters of \"$text\" queued (points already claimed this game)."
                )
            }
        } else {
            showToast(
                if (isRussian) "Слово «$text» не найдено в словаре!"
                else "Word \"$text\" was not found in dictionary!"
            )
        }
    }

    fun resumeTetrisAfterCrossword() {
        if (engine.canResumeTetris()) {
            engine.mode = GameMode.TETRIS
            engine.spawnPiece()
            syncStatesFromEngine()
            startGameLoop()
            saveGameProgress()
            showToast(if (isRussian) "Складывание пазлов возобновлено!" else "Puzzle folding resumed!")
        } else if (engine.paceMode == PaceMode.ARCADE) {
            // In Arcade mode, if top rows were not cleared during the Crossword rescue round, Game Over is reached!
            engine.finishGameAndTriggerGameOver()
            syncStatesFromEngine()
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
        runCatching {
            tts?.stop()
            tts?.shutdown()
        }
    }
}
