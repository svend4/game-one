package com.example.game

import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import kotlin.random.Random

data class StencilDebugSlot(
    val index: Int,
    val x: Int,
    val y: Int,
    val expectedChar: Char,
    val actualChar: Char?,
    val isMatched: Boolean
)

data class GridPoint(val x: Int, val y: Int)

data class GameCell(
    val char: Char,
    val colorHex: String,
    val isWildcard: Boolean = char == Dictionary.WILDCARD_CHAR,
    val isAnchor: Boolean = false, // Pre-placed crossword puzzle anchor letter
    val id: String = UUID.randomUUID().toString()
)

data class ActivePiece(
    val shapeType: Int, // 0 to 6 (I, O, T, S, Z, J, L), or 7 for Single-Tile 1x1 Kids/Senior Puzzle mode!
    val x: Int,
    val y: Int,
    val rotation: Int = 0,
    val letters: List<Char> // 4 letters for tetrominoes, or 1 letter for Single-Tile mode
) {
    fun getPoints(): List<GridPoint> {
        val offsets = TetrominoShapes.getOffsets(shapeType, rotation)
        return offsets.map { GridPoint(x + it.first, y + it.second) }
    }
}

/**
 * Represents a pre-placed Crossword Stencil cell on the board ("Трафарет пазла").
 * By default, empty stencil slots hide the expected letter behind '?' (isHintRevealed = false)
 * so the player genuinely thinks about the crossword riddle!
 * In Kids 1-Tile Mode or after using a Hint, the expectedChar is shown inside the slot.
 */
data class StencilSlot(
    val x: Int,
    val y: Int,
    val expectedChar: Char,
    val isHorizontal: Boolean = true,
    val isHintRevealed: Boolean = false
)

/**
 * Snapshot of the game state before a move, enabling 1-tap "↩️ Отменить ход" (Undo) for seniors & kids.
 */
data class BoardSnapshot(
    val boardCopy: Array<Array<GameCell?>>,
    val score: Int,
    val comboCount: Int,
    val wordsClearedCount: Int,
    val freeHintCharges: Int,
    val activePiece: ActivePiece?,
    val nextPieceShapeType: Int,
    val heldPieceShapeType: Int?,
    val heldPieceLetters: List<Char>?,
    val canHoldThisTurn: Boolean,
    val isRussian: Boolean,
    val isDailyWordActive: Boolean,
    val dailyWordCompletedToday: Boolean,
    val lastDailyWordCompletedDayKey: Int,
    val giftLettersQueue: List<Char>,
    val usedGiftWordsThisGame: Set<String>,
    val questWord: WordDefinition?,
    val secondaryQuestWord: WordDefinition?,
    val stencilSlots: List<StencilSlot>,
    val stencilRow: Int,
    val stencilStartCol: Int,
    val usedMagnetOnCurrentStencil: Boolean = false,
    val isHorizontalCrossSolved: Boolean = false,
    val isVerticalCrossSolved: Boolean = false
)

data class CrosswordClue(
    val id: String = UUID.randomUUID().toString(),
    val word: String,
    val clue: String,
    val category: String = "",
    val startX: Int,
    val startY: Int,
    val isHorizontal: Boolean,
    val length: Int,
    var isSolved: Boolean = false,
    var isRevealed: Boolean = false,
    var revealedLettersCount: Int = 1,
    var radarActive: Boolean = false
) {
    fun getCoordinates(): List<GridPoint> {
        return (0 until length).map { idx ->
            if (isHorizontal) GridPoint(startX + idx, startY)
            else GridPoint(startX, startY + idx)
        }
    }

    fun getMaskedPreview(): String {
        if (isSolved || isRevealed) return word
        return word.mapIndexed { index, c ->
            if (index < revealedLettersCount) c.toString() else "_"
        }.joinToString(" ")
    }
}

enum class GameMode {
    TETRIS,
    CROSSWORD,
    GAME_OVER
}

/**
 * Pace modes designed for children, seniors, moms, and arcade fans:
 * - PUZZLE_STEP: Turn-based puzzle! Pieces NEVER fall automatically; player moves at their own pace.
 * - RELAXED: Very slow, gentle breeze (2600ms per cell, no speed scaling).
 * - ARCADE: Classic dynamic Tetris speed with Crossword Rescue Bonus & reachable Game Over.
 */
enum class PaceMode(val delayMs: Long) {
    PUZZLE_STEP(0L),
    RELAXED(2600L),
    ARCADE(800L)
}

object TetrominoShapes {
    const val SINGLE_TILE_SHAPE = 7

    val SHAPES = listOf(
        // 0: I shape
        listOf(
            listOf(0 to -1, 0 to 0, 0 to 1, 0 to 2),
            listOf(-1 to 0, 0 to 0, 1 to 0, 2 to 0),
            listOf(0 to -1, 0 to 0, 0 to 1, 0 to 2),
            listOf(-1 to 0, 0 to 0, 1 to 0, 2 to 0)
        ),
        // 1: O shape
        listOf(
            listOf(0 to 0, 0 to 1, 1 to 0, 1 to 1),
            listOf(0 to 0, 0 to 1, 1 to 0, 1 to 1),
            listOf(0 to 0, 0 to 1, 1 to 0, 1 to 1),
            listOf(0 to 0, 0 to 1, 1 to 0, 1 to 1)
        ),
        // 2: T shape
        listOf(
            listOf(0 to 0, -1 to 0, 1 to 0, 0 to -1),
            listOf(0 to 0, 0 to -1, 0 to 1, 1 to 0),
            listOf(0 to 0, -1 to 0, 1 to 0, 0 to 1),
            listOf(0 to 0, 0 to -1, 0 to 1, -1 to 0)
        ),
        // 3: S shape
        listOf(
            listOf(0 to 0, 1 to 0, 0 to 1, -1 to 1),
            listOf(0 to 0, 0 to -1, 1 to 0, 1 to 1),
            listOf(0 to 0, 1 to 0, 0 to 1, -1 to 1),
            listOf(0 to 0, 0 to -1, 1 to 0, 1 to 1)
        ),
        // 4: Z shape
        listOf(
            listOf(0 to 0, -1 to 0, 0 to 1, 1 to 1),
            listOf(0 to 0, 0 to 1, 1 to 0, 1 to -1),
            listOf(0 to 0, -1 to 0, 0 to 1, 1 to 1),
            listOf(0 to 0, 0 to 1, 1 to 0, 1 to -1)
        ),
        // 5: J shape
        listOf(
            listOf(0 to 0, 0 to -1, 0 to 1, -1 to 1),
            listOf(0 to 0, -1 to 0, 1 to 0, 1 to 1),
            listOf(0 to 0, 0 to -1, 0 to 1, 1 to -1),
            listOf(0 to 0, -1 to 0, -1 to -1, 1 to 0)
        ),
        // 6: L shape
        listOf(
            listOf(0 to 0, 0 to -1, 0 to 1, 1 to 1),
            listOf(0 to 0, -1 to 0, 1 to 0, -1 to 1),
            listOf(0 to 0, 0 to -1, 0 to 1, -1 to -1),
            listOf(0 to 0, -1 to 0, 1 to -1, 1 to 0)
        ),
        // 7: Single 1x1 Puzzle Tile (for Kids & Cozy Puzzle Mode!)
        listOf(
            listOf(0 to 0),
            listOf(0 to 0),
            listOf(0 to 0),
            listOf(0 to 0)
        )
    )

    val COLORS = listOf(
        "#4DD0E1", // Cyan
        "#FFF176", // Yellow
        "#BA68C8", // Purple
        "#81C784", // Green
        "#E57373", // Red
        "#64B5F6", // Blue
        "#FFB74D", // Orange
        "#00E676"  // Emerald for 1x1 Puzzle Tile
    )

    fun getOffsets(shapeId: Int, rotation: Int): List<Pair<Int, Int>> {
        val rotations = SHAPES[shapeId.coerceIn(0, SHAPES.lastIndex)]
        return rotations[rotation.coerceAtLeast(0) % rotations.size]
    }
}

class GameEngine(
    val cols: Int = 10,
    val rows: Int = 15,
    var isRussian: Boolean = true,
    var random: Random = Random.Default
) {
    var board = Array(rows) { arrayOfNulls<GameCell>(cols) }

    var mode = GameMode.TETRIS
    var paceMode = PaceMode.PUZZLE_STEP // Default to calm, stress-free Puzzle Step mode!
    var selectedTheme = WordTheme.ALL
    var isSingleTileMode = false        // When true, spawns simple 1x1 letter tiles instead of 4-block shapes
    var isLargeFontMode = true          // Default to comfortable large font & high contrast for family/seniors
    var isPaperTheme = true             // Default to warm Paper/Magazine Crossword aesthetic for comfort!
    var isCrossedStencilMode = false    // When true, places 2 intersecting words (Horizontal + Vertical) on the board!
    var isDailyWordActive = false       // True when the current stencil is the Daily Word of the Day
    var dailyWordCompletedToday = false
    var lastDailyWordCompletedDayKey: Int = -1

    var score = 0
    var comboCount = 0
    var wordsClearedCount = 0
    var freeHintCharges = 3

    // Tracks whether Magnet was used on the current stencil (prevents infinite hint/combo/score farming)
    var usedMagnetOnCurrentStencil = false
    var isHorizontalCrossSolved = false
    var isVerticalCrossSolved = false
    // Tracks whether the Arcade Crossword Rescue round was already used in the current Arcade session
    var arcadeRescueUsed = false
    var lastEarnedBonus = 0
    var lastGiftWordPointsAwarded = 0

    var activePiece: ActivePiece? = null
    var nextPieceShapeType: Int = random.nextInt(7)

    // Hold pocket state
    var heldPieceShapeType: Int? = null
    var heldPieceLetters: List<Char>? = null
    var canHoldThisTurn: Boolean = true

    // Undo history stack (up to 8 moves)
    val undoStack = ArrayDeque<BoardSnapshot>()

    // Current words selected for letters during Tetris game
    var levelTargetWords = mutableListOf<String>()

    // Active Crossword Stencil Puzzle ("Живой Кроссворд-Трафарет на поле")
    var questWordDefinition: WordDefinition? = null
    var secondaryQuestWordDefinition: WordDefinition? = null // Used in Crossed 2-Word Stencil mode (Vertical word)
    var stencilSlots = mutableListOf<StencilSlot>()
    var stencilRow: Int = rows - 1
    var stencilStartCol: Int = 2
    var lastResonanceWord: String? = null
    var lastUnlockedWords = mutableListOf<String>()
    var lastDailyWordJustSolved: Boolean = false
    var lastSafetyClearTriggered: Boolean = false

    // Gift letters entered by user & set of already-rewarded gift words in this game session
    val giftLettersQueue = mutableListOf<Char>()
    val usedGiftWordsThisGame = mutableSetOf<String>()

    // Current clues found during Crossword Mode
    var crosswordClues = mutableListOf<CrosswordClue>()

    init {
        generateNewTargetWords()
        placeCrosswordStencilOnBoard()
        spawnPiece()
    }

    /**
     * Removes all pre-placed anchor tiles across the entire board so no orphaned isAnchor cells
     * ever remain after gravity, line clears, or stencil switches.
     */
    fun clearExistingAnchorsFromBoard() {
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                if (board[r][c]?.isAnchor == true) {
                    board[r][c] = null
                }
            }
        }
    }

    fun isDailyWordCompletedForToday(): Boolean {
        val todayKey = Dictionary.getCurrentCalendarDayKey()
        if (dailyWordCompletedToday && lastDailyWordCompletedDayKey != todayKey) {
            dailyWordCompletedToday = false
        }
        return dailyWordCompletedToday && lastDailyWordCompletedDayKey == todayKey
    }

    /**
     * Lifts non-anchor player cells in column [col] at and above [fromRow] up by 1 cell
     * so placing a stencil on [fromRow] never overwrites player-placed blocks (fixes 2.4).
     */
    private fun preservePlayerCellBeforeStencil(col: Int, fromRow: Int) {
        if (!isValidPoint(col, fromRow)) return
        val existing = board[fromRow][col] ?: return
        if (existing.isAnchor) {
            board[fromRow][col] = null
            return
        }
        for (r in 0 until fromRow) {
            board[r][col] = board[r + 1][col]
        }
        board[fromRow][col] = null
    }

    /**
     * Saves a snapshot before modifying the board so the player can tap "↩️ Шаг назад" (Undo) anytime.
     */
    fun saveUndoSnapshot() {
        val copy = Array(rows) { r ->
            Array(cols) { c ->
                board[r][c]?.copy()
            }
        }
        val snapshot = BoardSnapshot(
            boardCopy = copy,
            score = score,
            comboCount = comboCount,
            wordsClearedCount = wordsClearedCount,
            freeHintCharges = freeHintCharges,
            activePiece = activePiece?.copy(letters = activePiece?.letters?.toList() ?: emptyList()),
            nextPieceShapeType = nextPieceShapeType,
            heldPieceShapeType = heldPieceShapeType,
            heldPieceLetters = heldPieceLetters?.toList(),
            canHoldThisTurn = canHoldThisTurn,
            isRussian = isRussian,
            isDailyWordActive = isDailyWordActive,
            dailyWordCompletedToday = dailyWordCompletedToday,
            lastDailyWordCompletedDayKey = lastDailyWordCompletedDayKey,
            giftLettersQueue = giftLettersQueue.toList(),
            usedGiftWordsThisGame = usedGiftWordsThisGame.toSet(),
            questWord = questWordDefinition,
            secondaryQuestWord = secondaryQuestWordDefinition,
            stencilSlots = stencilSlots.toList(),
            stencilRow = stencilRow,
            stencilStartCol = stencilStartCol,
            usedMagnetOnCurrentStencil = usedMagnetOnCurrentStencil,
            isHorizontalCrossSolved = isHorizontalCrossSolved,
            isVerticalCrossSolved = isVerticalCrossSolved
        )
        if (undoStack.size >= 8) {
            undoStack.removeFirst()
        }
        undoStack.addLast(snapshot)
    }

    /**
     * Restores the previous board state ("↩️ Отменить ход").
     */
    fun undoLastMove(): Boolean {
        if (undoStack.isEmpty()) return false
        val snap = undoStack.removeLast()
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                board[r][c] = snap.boardCopy[r][c]
            }
        }
        score = snap.score
        comboCount = snap.comboCount
        wordsClearedCount = snap.wordsClearedCount
        freeHintCharges = snap.freeHintCharges
        nextPieceShapeType = snap.nextPieceShapeType
        heldPieceShapeType = snap.heldPieceShapeType
        heldPieceLetters = snap.heldPieceLetters?.toList()
        canHoldThisTurn = snap.canHoldThisTurn
        isRussian = snap.isRussian
        isDailyWordActive = snap.isDailyWordActive
        dailyWordCompletedToday = snap.dailyWordCompletedToday
        lastDailyWordCompletedDayKey = snap.lastDailyWordCompletedDayKey
        giftLettersQueue.clear()
        giftLettersQueue.addAll(snap.giftLettersQueue)
        usedGiftWordsThisGame.clear()
        usedGiftWordsThisGame.addAll(snap.usedGiftWordsThisGame)
        questWordDefinition = snap.questWord
        secondaryQuestWordDefinition = snap.secondaryQuestWord
        stencilSlots.clear()
        stencilSlots.addAll(snap.stencilSlots)
        stencilRow = snap.stencilRow
        stencilStartCol = snap.stencilStartCol
        usedMagnetOnCurrentStencil = snap.usedMagnetOnCurrentStencil
        isHorizontalCrossSolved = snap.isHorizontalCrossSolved
        isVerticalCrossSolved = snap.isVerticalCrossSolved
        mode = GameMode.TETRIS
        if (snap.activePiece != null) {
            activePiece = snap.activePiece
        } else {
            spawnPiece()
        }
        return true
    }

    /**
     * Chooses target words from the dictionary (filtered by selectedTheme) and sets the active Crossword Stencil word(s).
     * Supports words of 3..8 letters (including 7-8 letter custom family words like "БАБУШКА").
     */
    fun generateNewTargetWords() {
        levelTargetWords.clear()
        isDailyWordActive = false
        val defs = Dictionary.getDefinitionsForTheme(isRussian, selectedTheme)
            .filter { it.word.length in 3..8 }
            .ifEmpty { Dictionary.getDefinitionsForLanguage(isRussian) }

        if (defs.isNotEmpty()) {
            val shuffled = defs.shuffled(random)
            val primary = shuffled.first()
            questWordDefinition = primary
            levelTargetWords.add(primary.word)

            if (isCrossedStencilMode) {
                // Prefer a vertical partner word whose LAST letter (or any non-first letter) intersects primary.word
                // so the vertical word extends upward from the bottom row with its open slot at the very top!
                val crossCandidate = shuffled.drop(1).firstOrNull { cand ->
                    cand.word.length in 3..5 && cand.word != primary.word && primary.word.contains(cand.word.last())
                } ?: shuffled.drop(1).firstOrNull { cand ->
                    cand.word.length in 3..5 && cand.word != primary.word &&
                        cand.word.substring(1).any { ch -> primary.word.contains(ch) }
                } ?: shuffled.drop(1).firstOrNull { cand ->
                    cand.word.length in 3..5 && cand.word != primary.word &&
                        cand.word.any { ch -> primary.word.contains(ch) }
                }
                secondaryQuestWordDefinition = crossCandidate
                if (crossCandidate != null) {
                    levelTargetWords.add(crossCandidate.word)
                }
            } else {
                secondaryQuestWordDefinition = null
            }

            shuffled.drop(1).take(3).forEach {
                if (!levelTargetWords.contains(it.word)) {
                    levelTargetWords.add(it.word)
                }
            }
        }
    }

    /**
     * Places a specific word (such as a Custom Family Riddle or the Daily Word of the Day) directly on the board!
     * Returns false if the Daily Word was already completed today (prevents infinite Daily Word farming).
     */
    fun startSpecificWordOnBoard(def: WordDefinition, isDaily: Boolean = false): Boolean {
        if (isDaily && isDailyWordCompletedForToday()) {
            return false
        }
        saveUndoSnapshot()
        clearExistingAnchorsFromBoard()
        isDailyWordActive = isDaily
        questWordDefinition = def
        secondaryQuestWordDefinition = null
        if (!levelTargetWords.contains(def.word)) {
            levelTargetWords.add(0, def.word)
        }
        placeCrosswordStencilOnBoard()
        spawnPiece()
        return true
    }

    /**
     * Places the "Slow Crossword Stencil" right on the bottom floor of the board (row = rows - 1):
     * - Every open horizontal slot sits at y = rows - 1 (14) directly on the solid board floor, so a dropped tile
     *   NEVER falls through the slot!
     * - Any existing player blocks on the target cells are gently lifted up by 1 cell (never overwritten).
     * - In Crossed Stencil mode ("✚ Крест"), the horizontal word sits on the bottom row (rows - 1) and the vertical word
     *   extends upward from the intersection, with its open slot at the TOP-MOST cell (startRowV) so all cells below it
     *   are solid anchors and the path from above is 100% unobstructed!
     */
    fun placeCrosswordStencilOnBoard() {
        clearExistingAnchorsFromBoard()
        stencilSlots.clear()
        usedMagnetOnCurrentStencil = false
        isHorizontalCrossSolved = false
        isVerticalCrossSolved = false
        val quest = questWordDefinition ?: return
        val hWord = quest.word.take(cols - 2)

        val vDef = if (isCrossedStencilMode && !isDailyWordActive) secondaryQuestWordDefinition else null
        val vWord = vDef?.word?.take(5)

        if (vWord != null) {
            var intersectH = -1
            var intersectV = -1
            // 1st priority: match the last character of vWord so the horizontal word sits at rows - 1
            for (hi in hWord.indices) {
                if (hWord[hi] == vWord.last()) {
                    intersectH = hi
                    intersectV = vWord.lastIndex
                    break
                }
            }
            // 2nd priority: match any index vi >= 1 so vMissingIdx = 0 (top-most cell) is above the intersection
            if (intersectH == -1) {
                outer@ for (vi in vWord.lastIndex downTo 1) {
                    for (hi in hWord.indices) {
                        if (hWord[hi] == vWord[vi]) {
                            intersectH = hi
                            intersectV = vi
                            break@outer
                        }
                    }
                }
            }
            // 3rd fallback: match vi == 0
            if (intersectH == -1) {
                for (hi in hWord.indices) {
                    if (hWord[hi] == vWord[0]) {
                        intersectH = hi
                        intersectV = 0
                        break
                    }
                }
            }

            if (intersectH != -1 && intersectV != -1) {
                val targetRow = (rows - 1 - (vWord.length - 1 - intersectV)).coerceIn(7, rows - 1)
                stencilRow = targetRow
                stencilStartCol = ((cols - hWord.length) / 2).coerceIn(0, (cols - hWord.length).coerceAtLeast(0))
                val crossCol = stencilStartCol + intersectH
                val startRowV = stencilRow - intersectV

                // Ensure the open horizontal slot is NOT on the intersection column
                val hMissingIdx = hWord.indices.firstOrNull { it != intersectH } ?: intersectH
                // Place the open vertical slot at the TOP-MOST cell of the vertical word (index 0) whenever intersectV > 0
                // so nothing is above it and all cells below it are solid anchor tiles!
                val vMissingIdx = if (intersectV > 0) 0 else 1.coerceAtMost(vWord.lastIndex)

                // If targetRow < rows - 1 (when intersectV < vWord.lastIndex), ensure the column under hMissingIdx
                // has support up to targetRow + 1 so the horizontal open slot cannot be fallen through.
                val hMissingCol = stencilStartCol + hMissingIdx
                for (supportRow in (rows - 1) downTo (stencilRow + 1)) {
                    if (isValidPoint(hMissingCol, supportRow) && board[supportRow][hMissingCol] == null) {
                        board[supportRow][hMissingCol] = GameCell(
                            char = Dictionary.WILDCARD_CHAR,
                            colorHex = "#3949AB",
                            isWildcard = true,
                            isAnchor = true
                        )
                    }
                }

                // 1. Place Horizontal Word
                for (i in hWord.indices) {
                    val c = stencilStartCol + i
                    if (!isValidPoint(c, stencilRow)) continue
                    preservePlayerCellBeforeStencil(c, stencilRow)
                    val ch = hWord[i]
                    stencilSlots.add(
                        StencilSlot(
                            x = c,
                            y = stencilRow,
                            expectedChar = ch,
                            isHorizontal = true,
                            isHintRevealed = false
                        )
                    )
                    if (i != hMissingIdx) {
                        board[stencilRow][c] = GameCell(
                            char = ch,
                            colorHex = "#5E35B1",
                            isWildcard = false,
                            isAnchor = true
                        )
                    } else {
                        board[stencilRow][c] = null
                    }
                }

                // 2. Place Vertical Word intersecting at (crossCol, stencilRow)
                for (j in vWord.indices) {
                    val r = startRowV + j
                    if (!isValidPoint(crossCol, r)) continue
                    if (r != stencilRow) {
                        preservePlayerCellBeforeStencil(crossCol, r)
                    }
                    val ch = vWord[j]
                    if (stencilSlots.none { it.x == crossCol && it.y == r }) {
                        stencilSlots.add(
                            StencilSlot(
                                x = crossCol,
                                y = r,
                                expectedChar = ch,
                                isHorizontal = false,
                                isHintRevealed = false
                            )
                        )
                    }
                    if (j == intersectV) {
                        if (hMissingIdx != intersectH) {
                            board[r][crossCol] = GameCell(
                                char = ch,
                                colorHex = "#5E35B1",
                                isWildcard = false,
                                isAnchor = true
                            )
                        }
                    } else if (j != vMissingIdx) {
                        board[r][crossCol] = GameCell(
                            char = ch,
                            colorHex = "#3949AB",
                            isWildcard = false,
                            isAnchor = true
                        )
                    } else {
                        board[r][crossCol] = null
                    }
                }
                return
            }
        }

        // Standard 1-Word Horizontal Stencil: ALWAYS place on the bottom row (rows - 1 = 14)
        // so the solid floor of the board supports every open golden slot from underneath!
        stencilRow = rows - 1
        stencilStartCol = ((cols - hWord.length) / 2).coerceAtLeast(0)

        // Words of 5 or more letters have 2 missing slots; shorter words (3..4 letters) have 1 missing slot
        val missingIndices: Set<Int> = if (hWord.length >= 5) {
            setOf(1, hWord.length - 2)
        } else if (hWord.length > 2) {
            setOf(random.nextInt(1, hWord.length - 1))
        } else {
            setOf(0)
        }

        for (i in hWord.indices) {
            val c = stencilStartCol + i
            if (!isValidPoint(c, stencilRow)) continue
            preservePlayerCellBeforeStencil(c, stencilRow)
            val ch = hWord[i]
            stencilSlots.add(
                StencilSlot(
                    x = c,
                    y = stencilRow,
                    expectedChar = ch,
                    isHorizontal = true,
                    isHintRevealed = false
                )
            )

            if (i !in missingIndices) {
                board[stencilRow][c] = GameCell(
                    char = ch,
                    colorHex = "#5E35B1",
                    isWildcard = false,
                    isAnchor = true
                )
            } else {
                board[stencilRow][c] = null
            }
        }
    }

    /**
     * Reveals the hidden '?' letter inside the next unrevealed open stencil slot without auto-placing the tile.
     * Costs 1 freeHintCharge (or 15 points if 0 charges).
     */
    fun revealStencilSlotLetterHint(): Boolean {
        val idx = stencilSlots.indexOfFirst { slot ->
            val cell = if (isValidPoint(slot.x, slot.y)) board[slot.y][slot.x] else null
            val isOpen = cell == null || (cell.char != slot.expectedChar && cell.char != Dictionary.WILDCARD_CHAR)
            isOpen && !slot.isHintRevealed
        }
        if (idx == -1) return false

        if (freeHintCharges > 0) {
            freeHintCharges--
        } else if (score >= 15) {
            score -= 15
        } else {
            return false
        }

        stencilSlots[idx] = stencilSlots[idx].copy(isHintRevealed = true)
        return true
    }

    private var isCheckingStencilCompletion = false

    /**
     * Attempts to restore questWordDefinition if it was null, using current stencil slots or riddle clues.
     * Logs a warning when restoration occurs.
     */
    fun ensureQuestWordDefinitionRestored(): WordDefinition? {
        val current = questWordDefinition
        if (current != null) return current

        val hWord = stencilSlots
            .filter { it.isHorizontal }
            .sortedBy { it.x }
            .map { it.expectedChar }
            .joinToString("")

        val candidate = when {
            hWord.isNotBlank() -> hWord
            levelTargetWords.isNotEmpty() -> levelTargetWords.first()
            crosswordClues.isNotEmpty() -> crosswordClues.first().word
            else -> ""
        }

        if (candidate.isNotBlank()) {
            val def = Dictionary.findFirstMatchingDefinition(candidate, isRussian)
                ?: WordDefinition(
                    word = candidate.uppercase(),
                    definition = crosswordClues.firstOrNull { it.word == candidate }?.clue ?: "Загаданное слово",
                    category = Dictionary.getCategory(candidate, isRussian),
                    funFact = Dictionary.getFunFact(candidate, isRussian)
                )
            questWordDefinition = def
            Log.w("TetriCross", "questWordDefinition was null! Restored word '${def.word}' from active stencil slots/riddle")
            return def
        }

        Log.w("TetriCross", "checkStencilCompletion: questWordDefinition was null and could not be restored")
        return null
    }

    /**
     * Returns real-time debug information for each stencil slot on the board.
     */
    fun getStencilDebugSlots(): List<StencilDebugSlot> {
        return stencilSlots.mapIndexed { idx, slot ->
            val cell = if (isValidPoint(slot.x, slot.y)) board[slot.y][slot.x] else null
            val actual = cell?.char
            val matched = if (cell == null) {
                false
            } else if (cell.char == Dictionary.WILDCARD_CHAR) {
                true
            } else {
                Dictionary.normalizeYo(cell.char.toString()) == Dictionary.normalizeYo(slot.expectedChar.toString())
            }
            StencilDebugSlot(
                index = idx,
                x = slot.x,
                y = slot.y,
                expectedChar = slot.expectedChar,
                actualChar = actual,
                isMatched = matched
            )
        }
    }

    /**
     * Checks if the active Crossword Stencil on the board has been completed (either by matching letter,
     * wildcard '★', or filling all stencil slots).
     * If questWordDefinition is null, restores it from the current riddle/stencil and logs a warning.
     * In Crossed Stencil mode ("✚ Крест"), also locks completed horizontal or vertical arms as anchors
     * so partial progress is protected until both intersecting words are finished!
     */
    fun checkStencilCompletion(): Boolean {
        if (isCheckingStencilCompletion) return false
        if (stencilSlots.isEmpty()) return false
        val quest = questWordDefinition ?: ensureQuestWordDefinitionRestored() ?: return false

        val hSlots = stencilSlots.filter { it.isHorizontal }
        val vSlots = stencilSlots.filter { !it.isHorizontal }

        fun isSlotMatched(slot: StencilSlot): Boolean {
            if (!isValidPoint(slot.x, slot.y)) return false
            val cell = board[slot.y][slot.x] ?: return false
            if (cell.char == Dictionary.WILDCARD_CHAR) return true
            return Dictionary.normalizeYo(cell.char.toString()) == Dictionary.normalizeYo(slot.expectedChar.toString())
        }

        val hMatched = hSlots.isNotEmpty() && hSlots.all { isSlotMatched(it) }
        val vMatched = vSlots.isEmpty() || vSlots.all { isSlotMatched(it) }

        if (hMatched && !isHorizontalCrossSolved) {
            isHorizontalCrossSolved = true
            hSlots.forEach { slot ->
                val c = board[slot.y][slot.x]
                if (c != null && !c.isAnchor) {
                    board[slot.y][slot.x] = c.copy(isAnchor = true)
                }
            }
        }
        if (vSlots.isNotEmpty() && vMatched && !isVerticalCrossSolved) {
            isVerticalCrossSolved = true
            vSlots.forEach { slot ->
                val c = board[slot.y][slot.x]
                if (c != null && !c.isAnchor) {
                    board[slot.y][slot.x] = c.copy(isAnchor = true)
                }
            }
        }

        if (hMatched && vMatched) {
            try {
                isCheckingStencilCompletion = true
                val vWord = secondaryQuestWordDefinition?.word
                val displaySolved = if (isCrossedStencilMode && vWord != null) {
                    "${quest.word} ✚ $vWord"
                } else {
                    quest.word
                }
                triggerStencilTetrisLineClear(
                    solvedDisplay = displaySolved,
                    primaryWord = quest.word,
                    secondaryWord = vWord,
                    completedWithMagnet = usedMagnetOnCurrentStencil
                )
                return true
            } finally {
                isCheckingStencilCompletion = false
            }
        }
        return false
    }

    /**
     * Clears the entire row of the solved Crossword Stencil (plus vertical cross tiles if in 2D mode) like Tetris,
     * shifts upper blocks down, awards points & free hints, records unlocked words, and spawns the next Crossword Stencil!
     * Honest economy: if Magnet was used on this stencil, does NOT refund freeHintCharges and does NOT increment comboCount!
     */
    fun triggerStencilTetrisLineClear(
        solvedDisplay: String,
        primaryWord: String = solvedDisplay,
        secondaryWord: String? = null,
        completedWithMagnet: Boolean = false,
        rowsToClearOverride: Set<Int>? = null,
        specificCellsToClear: List<GridPoint>? = null
    ) {
        wordsClearedCount += if (secondaryWord != null) 2 else 1

        val bonus: Int
        if (completedWithMagnet) {
            // Honest Magnet economy: no combo growth and no free hint refund when Magnet was used!
            comboCount = 0
            bonus = if (isDailyWordActive) 100 else 20
        } else {
            comboCount++
            val baseBonus = when {
                isDailyWordActive -> 500
                secondaryWord != null -> if (paceMode == PaceMode.ARCADE) 500 else 400
                else -> 250
            }
            bonus = baseBonus + (comboCount - 1) * 50
            freeHintCharges++
        }

        score += bonus
        lastEarnedBonus = bonus
        lastResonanceWord = solvedDisplay
        usedMagnetOnCurrentStencil = false
        isHorizontalCrossSolved = false
        isVerticalCrossSolved = false

        lastUnlockedWords.add(primaryWord)
        if (secondaryWord != null) {
            lastUnlockedWords.add(secondaryWord)
        }
        if (isDailyWordActive) {
            dailyWordCompletedToday = true
            lastDailyWordCompletedDayKey = Dictionary.getCurrentCalendarDayKey()
            lastDailyWordJustSolved = true
            isDailyWordActive = false
        }

        // Clear all stencil slots and any anchor tiles on the board first
        for (slot in stencilSlots) {
            if (isValidPoint(slot.x, slot.y)) {
                board[slot.y][slot.x] = null
            }
        }
        clearExistingAnchorsFromBoard()

        // Clear specific matched cells (e.g. vertical resonance word cells) if provided
        specificCellsToClear?.forEach { pt ->
            if (isValidPoint(pt.x, pt.y)) {
                board[pt.y][pt.x] = null
            }
        }

        val rowsToClear = rowsToClearOverride?.filter { it in 0 until rows }?.toSet()
            ?: setOf(stencilRow.coerceIn(0, rows - 1))

        for (r in rowsToClear) {
            for (c in 0 until cols) {
                board[r][c] = null
            }
        }

        // Apply smooth gravity so blocks above drop down
        stencilSlots.clear()
        applyGravityToStackedBlocks()

        // Generate the next Crossword Puzzle word and place its stencil
        generateNewTargetWords()
        placeCrosswordStencilOnBoard()
    }

    /**
     * Smart "Magnet" Helper for kids/seniors:
     * Automatically places the next missing letter directly into the Crossword Stencil slot on the board!
     * Honest economy: requires at least 1 freeHintCharge or 50 score points, and marks usedMagnetOnCurrentStencil = true
     * so completing a stencil via Magnet cannot be used to farm infinite charges, combo, or score.
     */
    fun useMagnetToFillStencilSlot(): Boolean {
        if (stencilSlots.isEmpty()) {
            placeCrosswordStencilOnBoard()
        }
        val missingSlotIndex = stencilSlots.indexOfFirst { slot ->
            val cell = if (isValidPoint(slot.x, slot.y)) board[slot.y][slot.x] else null
            cell == null || (cell.char != slot.expectedChar && cell.char != Dictionary.WILDCARD_CHAR)
        }
        if (missingSlotIndex == -1) return false

        if (freeHintCharges <= 0 && score < 50) {
            return false
        }

        saveUndoSnapshot()

        if (freeHintCharges > 0) {
            freeHintCharges--
        } else {
            score = maxOf(0, score - 50)
        }

        usedMagnetOnCurrentStencil = true
        val missingSlot = stencilSlots[missingSlotIndex]
        stencilSlots[missingSlotIndex] = missingSlot.copy(isHintRevealed = true)

        board[missingSlot.y][missingSlot.x] = GameCell(
            char = missingSlot.expectedChar,
            colorHex = "#00E676",
            isWildcard = false,
            isAnchor = true
        )

        checkStencilCompletion()
        return true
    }

    /**
     * Allows the player to type/solve the active Stencil Crossword word directly at any time:
     * - Respects usedMagnetOnCurrentStencil so combining Magnet + manual word input never refunds charges or farms points (fixes 2.3).
     * - In Crossed Stencil mode ("✚ Крест"), adapts by mode:
     *   - In Arcade mode, typing either word immediately clears the cross for fast action.
     *   - In Puzzle/Relaxed family modes, typing the horizontal word fills the horizontal arm (+150 pts if vertical remains)
     *     and typing the vertical word fills the vertical arm, completing the cross when both arms are solved!
     */
    fun solveActiveStencilByWord(guess: String): Boolean {
        val clean = Dictionary.normalizeYo(guess)
        ensureQuestWordDefinitionRestored()
        val quest = questWordDefinition ?: return false
        val sec = if (isCrossedStencilMode) secondaryQuestWordDefinition else null

        val matchesPrimary = Dictionary.normalizeYo(quest.word) == clean
        val matchesSecondary = sec != null && Dictionary.normalizeYo(sec.word) == clean
        if (!matchesPrimary && !matchesSecondary) return false

        saveUndoSnapshot()

        if (sec != null && isCrossedStencilMode && paceMode != PaceMode.ARCADE) {
            val hSlots = stencilSlots.filter { it.isHorizontal }
            val vSlots = stencilSlots.filter { !it.isHorizontal }
            if (matchesPrimary && vSlots.isNotEmpty()) {
                for (slot in hSlots) {
                    if (isValidPoint(slot.x, slot.y)) {
                        board[slot.y][slot.x] = GameCell(
                            char = slot.expectedChar,
                            colorHex = "#00E676",
                            isWildcard = false,
                            isAnchor = true
                        )
                    }
                }
                isHorizontalCrossSolved = true
                if (!checkStencilCompletion()) {
                    // Horizontal arm solved, vertical arm still open: award honest partial reward
                    val partialBonus = if (usedMagnetOnCurrentStencil) 10 else 150
                    score += partialBonus
                    lastEarnedBonus = partialBonus
                    lastUnlockedWords.add(quest.word)
                }
                return true
            } else if (matchesSecondary && hSlots.isNotEmpty()) {
                // Fill all vertical slots plus the intersection slot
                for (slot in stencilSlots) {
                    val isVerticalCol = vSlots.any { it.x == slot.x }
                    if (!slot.isHorizontal || isVerticalCol) {
                        if (isValidPoint(slot.x, slot.y)) {
                            board[slot.y][slot.x] = GameCell(
                                char = slot.expectedChar,
                                colorHex = "#00E676",
                                isWildcard = false,
                                isAnchor = true
                            )
                        }
                    }
                }
                isVerticalCrossSolved = true
                if (!checkStencilCompletion()) {
                    val partialBonus = if (usedMagnetOnCurrentStencil) 10 else 150
                    score += partialBonus
                    lastEarnedBonus = partialBonus
                    lastUnlockedWords.add(sec.word)
                }
                return true
            }
        }

        // Fill all stencil cells with their expected characters as anchors and check completion
        for (slot in stencilSlots) {
            if (isValidPoint(slot.x, slot.y)) {
                board[slot.y][slot.x] = GameCell(
                    char = slot.expectedChar,
                    colorHex = "#00E676",
                    isWildcard = false,
                    isAnchor = true
                )
            }
        }

        val completed = checkStencilCompletion()
        if (!completed) {
            triggerStencilTetrisLineClear(
                solvedDisplay = if (sec != null && isCrossedStencilMode) "${quest.word} ✚ ${sec.word}" else quest.word,
                primaryWord = quest.word,
                secondaryWord = if (isCrossedStencilMode) sec?.word else null,
                completedWithMagnet = usedMagnetOnCurrentStencil
            )
        }
        return true
    }

    fun isValidPoint(x: Int, y: Int): Boolean {
        return x in 0 until cols && y in 0 until rows
    }

    /**
     * Generates friendly letters for a falling piece:
     * Always includes the missing letter(s) needed for the active Crossword Stencil!
     */
    private fun generateBalancedPieceLetters(count: Int): List<Char> {
        val pieceLetters = mutableListOf<Char>()
        val vowels = if (isRussian) "АЕИОУЯ" else "AEIOU"
        val consonants = if (isRussian) "БВДГКЛМНПРСТЧШ" else "TNSRLDMCGPKB"

        val missingStencilChars = stencilSlots.mapNotNull { slot ->
            val cell = if (isValidPoint(slot.x, slot.y)) board[slot.y][slot.x] else null
            if (cell == null || cell.char != slot.expectedChar) slot.expectedChar else null
        }

        if (count == 1) {
            val singleChar = when {
                giftLettersQueue.isNotEmpty() -> giftLettersQueue.removeAt(0)
                missingStencilChars.isNotEmpty() && random.nextFloat() < 0.88f -> missingStencilChars.first()
                else -> Dictionary.WILDCARD_CHAR
            }
            return listOf(singleChar)
        }

        for (i in 0 until count) {
            if (giftLettersQueue.isNotEmpty()) {
                pieceLetters.add(giftLettersQueue.removeAt(0))
            } else if (i == 0 && missingStencilChars.isNotEmpty()) {
                pieceLetters.add(missingStencilChars.random(random))
            } else if (i == 1 && random.nextFloat() < 0.16f) {
                pieceLetters.add(Dictionary.WILDCARD_CHAR)
            } else if (random.nextFloat() < 0.75f && levelTargetWords.isNotEmpty()) {
                val word = levelTargetWords.random(random)
                pieceLetters.add(word.random(random))
            } else {
                val pool = if (i % 2 == 0) vowels else consonants
                pieceLetters.add(pool.random(random))
            }
        }

        if (pieceLetters.none { it in vowels || it == Dictionary.WILDCARD_CHAR }) {
            pieceLetters[random.nextInt(pieceLetters.size)] = vowels.random(random)
        }
        return pieceLetters
    }

    fun spawnPiece(customShape: Int? = null, customLetters: List<Char>? = null) {
        val shapeType = when {
            customShape != null -> customShape
            isSingleTileMode -> TetrominoShapes.SINGLE_TILE_SHAPE
            else -> nextPieceShapeType
        }
        if (customShape == null) {
            nextPieceShapeType = if (isSingleTileMode) TetrominoShapes.SINGLE_TILE_SHAPE else random.nextInt(7)
        }

        val missingSlotX = stencilSlots.firstOrNull { slot ->
            isValidPoint(slot.x, slot.y) && board[slot.y][slot.x] == null
        }?.x
        val startX = missingSlotX ?: (cols / 2)
        val startY = 1
        val letterCount = if (shapeType == TetrominoShapes.SINGLE_TILE_SHAPE) 1 else 4
        val pieceLetters = customLetters ?: generateBalancedPieceLetters(letterCount)

        val piece = ActivePiece(
            shapeType = shapeType,
            x = startX,
            y = startY,
            rotation = 0,
            letters = pieceLetters
        )

        canHoldThisTurn = true

        if (checkCollision(piece)) {
            if (paceMode != PaceMode.ARCADE) {
                softClearTopRowsForFamily()
                activePiece = piece
            } else {
                handleArcadeTopReached()
            }
        } else {
            activePiece = piece
        }
    }

    private fun handleArcadeTopReached() {
        activePiece = null
        if (!arcadeRescueUsed) {
            arcadeRescueUsed = true
            mode = GameMode.CROSSWORD
            scanBoardForCrosswordWords()
        } else {
            mode = GameMode.GAME_OVER
        }
    }

    /**
     * Explicitly finishes the current game session and transitions to GAME_OVER so the player
     * can record their score to the High Score Leaderboard at any time.
     */
    fun finishGameAndTriggerGameOver() {
        activePiece = null
        mode = GameMode.GAME_OVER
    }

    /**
     * Gentle "No-Lose" safety net for seniors, moms, and children:
     * Clears old anchors and top rows so the game never abruptly ends in frustration.
     */
    fun softClearTopRowsForFamily() {
        clearExistingAnchorsFromBoard()
        for (r in 0 until (rows - 4)) {
            for (c in 0 until cols) {
                board[r][c] = null
            }
        }
        applyGravityToStackedBlocks()
        placeCrosswordStencilOnBoard()
        lastSafetyClearTriggered = true
    }

    fun shiftPieceLetters(): Boolean {
        val piece = activePiece ?: return false
        if (piece.letters.size < 2) {
            val missingChar = stencilSlots.firstOrNull { slot ->
                isValidPoint(slot.x, slot.y) && board[slot.y][slot.x] == null
            }?.expectedChar ?: Dictionary.WILDCARD_CHAR
            val current = piece.letters.firstOrNull() ?: missingChar
            val nextChar = if (current == Dictionary.WILDCARD_CHAR) missingChar else Dictionary.WILDCARD_CHAR
            activePiece = piece.copy(letters = listOf(nextChar))
            return true
        }
        val shifted = listOf(piece.letters.last()) + piece.letters.dropLast(1)
        activePiece = piece.copy(letters = shifted)
        return true
    }

    fun holdCurrentPiece(): Boolean {
        if (!canHoldThisTurn || mode != GameMode.TETRIS) return false
        val current = activePiece ?: return false

        val prevHeldShape = heldPieceShapeType
        val prevHeldLetters = heldPieceLetters

        heldPieceShapeType = current.shapeType
        heldPieceLetters = current.letters

        if (prevHeldShape != null && prevHeldLetters != null) {
            spawnPiece(customShape = prevHeldShape, customLetters = prevHeldLetters)
        } else {
            spawnPiece()
        }
        canHoldThisTurn = false
        return true
    }

    fun getGhostPiecePoints(): List<GridPoint> {
        var ghost = activePiece ?: return emptyList()
        while (true) {
            val next = ghost.copy(y = ghost.y + 1)
            if (checkCollision(next)) break
            ghost = next
        }
        return ghost.getPoints()
    }

    fun checkCollision(piece: ActivePiece): Boolean {
        for (point in piece.getPoints()) {
            if (!isValidPoint(point.x, point.y)) return true
            if (point.y >= 0 && board[point.y][point.x] != null) return true
        }
        return false
    }

    fun movePiece(dx: Int, dy: Int): Boolean {
        val piece = activePiece ?: return false
        val moved = piece.copy(x = piece.x + dx, y = piece.y + dy)
        if (!checkCollision(moved)) {
            activePiece = moved
            return true
        }
        return false
    }

    fun rotatePiece(): Boolean {
        val piece = activePiece ?: return false
        if (piece.shapeType == TetrominoShapes.SINGLE_TILE_SHAPE) {
            return shiftPieceLetters()
        }
        val nextRotation = (piece.rotation + 1) % 4
        val candidates = listOf(
            piece.copy(rotation = nextRotation),
            piece.copy(x = piece.x - 1, rotation = nextRotation),
            piece.copy(x = piece.x + 1, rotation = nextRotation),
            piece.copy(y = piece.y - 1, rotation = nextRotation)
        )
        for (candidate in candidates) {
            if (!checkCollision(candidate)) {
                activePiece = candidate
                return true
            }
        }
        return false
    }

    fun hardDrop() {
        saveUndoSnapshot()
        var dropSteps = 0
        while (movePiece(0, 1)) {
            dropSteps++
        }
        score += dropSteps * 2
        lockPiece(alreadySavedUndo = true)
    }

    fun lockPiece(alreadySavedUndo: Boolean = false) {
        val piece = activePiece ?: return
        if (!alreadySavedUndo) {
            saveUndoSnapshot()
        }
        val points = piece.getPoints()
        val baseColor = TetrominoShapes.COLORS[piece.shapeType % TetrominoShapes.COLORS.size]

        for (i in points.indices) {
            val p = points[i]
            val char = piece.letters.getOrElse(i) { 'A' }
            val cellColor = if (char == Dictionary.WILDCARD_CHAR) "#FFD54F" else baseColor
            if (isValidPoint(p.x, p.y)) {
                board[p.y][p.x] = GameCell(
                    char = char,
                    colorHex = cellColor,
                    isWildcard = char == Dictionary.WILDCARD_CHAR
                )
            }
        }

        activePiece = null
        score += 10

        // 1. Check if the Crossword Stencil on the board was completed!
        var wordOrStencilCleared = checkStencilCompletion()

        // 2. Also check if the quest word resonated anywhere else on the board
        if (!wordOrStencilCleared) {
            wordOrStencilCleared = checkLiveQuestWordResonance()
        }

        // 3. Check standard Tetris completed horizontal lines (only if stencil didn't just reset the board)
        if (!wordOrStencilCleared) {
            checkCompletedLines()
        }

        var topReached = false
        for (col in 0 until cols) {
            if (board[0][col] != null || board[1][col] != null || board[2][col] != null) {
                topReached = true
                break
            }
        }

        if (topReached) {
            if (paceMode != PaceMode.ARCADE) {
                softClearTopRowsForFamily()
                spawnPiece()
            } else {
                handleArcadeTopReached()
            }
        } else {
            spawnPiece()
        }
    }

    /**
     * Checks if the active quest word (or secondary quest word) was formed by resonance on the board:
     * - Horizontal matches clear their single row.
     * - Short vertical matches (3–4 letters) clear the word's own cells + 1 bottom-most row of the match
     *   (preventing accidental 3-letter vertical matches from wiping 3 full rows = 30 cells).
     * - Long vertical matches (5+ letters) trigger a Mega-Resonance that clears all rows spanned by the word!
     */
    private fun checkLiveQuestWordResonance(): Boolean {
        val target = questWordDefinition?.word ?: return false
        val foundClues = findAllBoardMatches(onlyTargetWord = target)
        if (foundClues.isNotEmpty()) {
            val match = foundClues.first()
            val coords = match.getCoordinates()
            val rowsToClear: Set<Int>
            val extraCellsToClear: List<GridPoint>?
            if (match.isHorizontal || match.length >= 5) {
                rowsToClear = coords.map { it.y }.toSet()
                extraCellsToClear = null
            } else {
                // Short vertical word (3–4 letters): clear the word's cells + 1 bottom-most row
                val bottomRow = coords.maxOfOrNull { it.y } ?: stencilRow
                rowsToClear = setOf(bottomRow)
                extraCellsToClear = coords
            }
            triggerStencilTetrisLineClear(
                solvedDisplay = match.word,
                primaryWord = match.word,
                secondaryWord = null,
                completedWithMagnet = false,
                rowsToClearOverride = rowsToClear,
                specificCellsToClear = extraCellsToClear
            )
            return true
        }
        return false
    }

    /**
     * Clears standard completed horizontal Tetris lines WITHOUT wiping out an active, partially filled
     * Crossword Stencil (fixes 2.5). Shifts stencil coordinates down for any cleared rows below them,
     * and only respawns a stencil if the stencil itself had no remaining open slots.
     */
    private fun checkCompletedLines() {
        var linesClearedThisTurn = 0
        var r = rows - 1
        while (r >= 0) {
            var full = true
            for (c in 0 until cols) {
                if (board[r][c] == null) {
                    full = false
                    break
                }
            }
            if (full) {
                for (y in r downTo 1) {
                    for (x in 0 until cols) {
                        board[y][x] = board[y - 1][x]
                    }
                }
                for (x in 0 until cols) {
                    board[0][x] = null
                }
                // Shift any stencil slots located above the cleared row r down by 1
                if (stencilSlots.isNotEmpty()) {
                    val shifted = stencilSlots.mapNotNull { slot ->
                        when {
                            slot.y == r -> null
                            slot.y < r -> slot.copy(y = (slot.y + 1).coerceAtMost(rows - 1))
                            else -> slot
                        }
                    }
                    stencilSlots.clear()
                    stencilSlots.addAll(shifted)
                    if (stencilRow < r) {
                        stencilRow = (stencilRow + 1).coerceAtMost(rows - 1)
                    }
                }
                linesClearedThisTurn++
                // Do not decrement r because row r now contains what was row r-1
            } else {
                r--
            }
        }
        if (linesClearedThisTurn > 0) {
            comboCount++
            score += when (linesClearedThisTurn) {
                1 -> 150 * comboCount
                2 -> 350 * comboCount
                3 -> 700 * comboCount
                else -> 1200 * comboCount
            }
            val hasOpenSlot = stencilSlots.any { slot ->
                isValidPoint(slot.x, slot.y) && board[slot.y][slot.x] == null
            }
            if (stencilSlots.isEmpty() || !hasOpenSlot) {
                placeCrosswordStencilOnBoard()
            }
        }
    }

    private fun findAllBoardMatches(onlyTargetWord: String? = null): List<CrosswordClue> {
        val matches = mutableListOf<CrosswordClue>()
        val normalizedTarget = onlyTargetWord?.let { Dictionary.normalizeYo(it) }

        // Horizontal scan
        for (r in 0 until rows) {
            var c = 0
            while (c < cols) {
                if (board[r][c] != null) {
                    val startC = c
                    val sb = StringBuilder()
                    while (c < cols && board[r][c] != null) {
                        sb.append(board[r][c]!!.char)
                        c++
                    }
                    val runStr = sb.toString()
                    val runLength = runStr.length
                    if (runLength >= 3) {
                        for (len in runLength downTo 3) {
                            for (offset in 0..runLength - len) {
                                val sub = runStr.substring(offset, offset + len)
                                val matchedDef = Dictionary.findFirstMatchingDefinition(sub, isRussian)
                                if (matchedDef != null &&
                                    (normalizedTarget == null || Dictionary.normalizeYo(matchedDef.word) == normalizedTarget)
                                ) {
                                    if (matches.none { it.word == matchedDef.word }) {
                                        matches.add(
                                            CrosswordClue(
                                                word = matchedDef.word,
                                                clue = matchedDef.definition,
                                                category = matchedDef.category.ifBlank { Dictionary.getCategory(matchedDef.word, isRussian) },
                                                startX = startC + offset,
                                                startY = r,
                                                isHorizontal = true,
                                                length = len
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    c++
                }
            }
        }

        // Vertical scan
        for (c in 0 until cols) {
            var r = 0
            while (r < rows) {
                if (board[r][c] != null) {
                    val startR = r
                    val sb = StringBuilder()
                    while (r < rows && board[r][c] != null) {
                        sb.append(board[r][c]!!.char)
                        r++
                    }
                    val runStr = sb.toString()
                    val runLength = runStr.length
                    if (runLength >= 3) {
                        for (len in runLength downTo 3) {
                            for (offset in 0..runLength - len) {
                                val sub = runStr.substring(offset, offset + len)
                                val matchedDef = Dictionary.findFirstMatchingDefinition(sub, isRussian)
                                if (matchedDef != null &&
                                    (normalizedTarget == null || Dictionary.normalizeYo(matchedDef.word) == normalizedTarget)
                                ) {
                                    if (matches.none { it.word == matchedDef.word }) {
                                        matches.add(
                                            CrosswordClue(
                                                word = matchedDef.word,
                                                clue = matchedDef.definition,
                                                category = matchedDef.category.ifBlank { Dictionary.getCategory(matchedDef.word, isRussian) },
                                                startX = c,
                                                startY = startR + offset,
                                                isHorizontal = false,
                                                length = len
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    r++
                }
            }
        }

        return matches
    }

    fun scanBoardForCrosswordWords() {
        crosswordClues.clear()
        crosswordClues.addAll(findAllBoardMatches())

        if (crosswordClues.isEmpty()) {
            val fallbackDefs = Dictionary.getDefinitionsForTheme(isRussian, selectedTheme)
                .filter { it.word.length in 3..5 }
                .shuffled()
                .take(2)

            fallbackDefs.forEachIndexed { idx, def ->
                val targetRow = (rows - 1 - idx * 2).coerceIn(2, rows - 1)
                val startCol = ((cols - def.word.length) / 2).coerceAtLeast(0)
                for (i in def.word.indices) {
                    val c = startCol + i
                    if (isValidPoint(c, targetRow)) {
                        board[targetRow][c] = GameCell(
                            char = def.word[i],
                            colorHex = TetrominoShapes.COLORS[(idx + i) % TetrominoShapes.COLORS.size]
                        )
                    }
                }
                crosswordClues.add(
                    CrosswordClue(
                        word = def.word,
                        clue = def.definition,
                        category = def.category.ifBlank { Dictionary.getCategory(def.word, isRussian) },
                        startX = startCol,
                        startY = targetRow,
                        isHorizontal = true,
                        length = def.word.length
                    )
                )
            }
        }
    }

    fun attemptGuessWord(clueId: String, guess: String): Boolean {
        val clueIndex = crosswordClues.indexOfFirst { it.id == clueId }
        if (clueIndex == -1) return false
        val clue = crosswordClues[clueIndex]

        if (Dictionary.normalizeYo(clue.word) == Dictionary.normalizeYo(guess)) {
            clue.isSolved = true
            clue.isRevealed = true
            clue.revealedLettersCount = clue.length
            comboCount++
            wordsClearedCount++
            val reward = (150 * (1.0 + (comboCount - 1) * 0.2)).toInt()
            score += reward
            lastEarnedBonus = reward
            lastUnlockedWords.add(clue.word)

            clearWordFromBoardAndLine(clue)
            return true
        } else {
            comboCount = 0
        }
        return false
    }

    fun revealNextLetterHint(clueId: String): Boolean {
        val clue = crosswordClues.firstOrNull { it.id == clueId } ?: return false
        if (clue.isSolved || clue.isRevealed) return false

        if (freeHintCharges > 0) {
            freeHintCharges--
        } else {
            score = maxOf(0, score - 15)
        }

        clue.revealedLettersCount = (clue.revealedLettersCount + 1).coerceAtMost(clue.length)
        if (clue.revealedLettersCount >= clue.length) {
            clue.isRevealed = true
            clue.isSolved = true
            lastUnlockedWords.add(clue.word)
            clearWordFromBoardAndLine(clue)
        }
        return true
    }

    fun activateWordRadar(clueId: String): Boolean {
        val clue = crosswordClues.firstOrNull { it.id == clueId } ?: return false
        if (clue.isSolved) return false

        if (!clue.radarActive) {
            if (freeHintCharges > 0) {
                freeHintCharges--
            } else {
                score = maxOf(0, score - 25)
            }
            crosswordClues.forEach { it.radarActive = (it.id == clueId) }
        } else {
            clue.radarActive = false
        }
        return true
    }

    fun attemptRevealWord(clueId: String): Boolean {
        val clueIndex = crosswordClues.indexOfFirst { it.id == clueId }
        if (clueIndex == -1) return false
        val clue = crosswordClues[clueIndex]

        if (!clue.isRevealed && !clue.isSolved) {
            score = maxOf(0, score - 50)
            clue.isRevealed = true
            clue.isSolved = true
            clue.revealedLettersCount = clue.length
            lastUnlockedWords.add(clue.word)

            clearWordFromBoardAndLine(clue)
            return true
        }
        return false
    }

    private fun clearWordFromBoardAndLine(clue: CrosswordClue) {
        val rowsAffected = clue.getCoordinates().map { it.y }.toSet()
        for (r in rowsAffected) {
            if (r in 0 until rows) {
                for (c in 0 until cols) {
                    board[r][c] = null
                }
            }
        }

        applyGravityToStackedBlocks()

        val solvedBackupState = crosswordClues.filter { it.isSolved }.map { it.word }.toSet()
        scanBoardForCrosswordWords()
        for (c in crosswordClues) {
            if (solvedBackupState.contains(c.word)) {
                c.isSolved = true
                c.isRevealed = true
                c.revealedLettersCount = c.length
            }
        }
    }

    /**
     * Compacts remaining blocks downward and keeps stencilSlots & stencilRow synchronized
     * with any moved anchor tiles so stencil coordinates never desync from board cells.
     */
    fun applyGravityToStackedBlocks() {
        val movedPositions = mutableMapOf<GridPoint, Int>() // old (x,y) -> newY

        for (col in 0 until cols) {
            val filledCellsInCol = mutableListOf<Pair<Int, GameCell>>()
            for (row in 0 until rows) {
                val cell = board[row][col]
                if (cell != null) {
                    filledCellsInCol.add(row to cell)
                }
            }
            for (row in 0 until rows) {
                val bottomOffset = rows - 1 - row
                if (bottomOffset >= 0) {
                    val listIndex = filledCellsInCol.size - 1 - row
                    if (listIndex >= 0) {
                        val (oldRow, cell) = filledCellsInCol[listIndex]
                        board[bottomOffset][col] = cell
                        movedPositions[GridPoint(col, oldRow)] = bottomOffset
                    } else {
                        board[bottomOffset][col] = null
                    }
                }
            }
        }

        if (stencilSlots.isNotEmpty()) {
            // Find how much horizontal stencil anchors shifted down
            val horizontalAnchorNewRows = stencilSlots
                .filter { it.isHorizontal && movedPositions.containsKey(GridPoint(it.x, it.y)) }
                .mapNotNull { movedPositions[GridPoint(it.x, it.y)] }

            val newHorizontalRow = horizontalAnchorNewRows.minOrNull() ?: (rows - 1)
            val rowDelta = newHorizontalRow - stencilRow
            stencilRow = newHorizontalRow

            // Keep all horizontal stencil anchors aligned on newHorizontalRow (never tear a word across rows!)
            for (slot in stencilSlots.filter { it.isHorizontal }) {
                val landedY = movedPositions[GridPoint(slot.x, slot.y)]
                if (landedY != null && landedY != newHorizontalRow) {
                    val cell = board[landedY][slot.x]
                    board[landedY][slot.x] = null
                    if (isValidPoint(slot.x, newHorizontalRow)) {
                        board[newHorizontalRow][slot.x] = cell
                    }
                }
            }

            val updatedSlots = stencilSlots.map { slot ->
                val explicitNewY = movedPositions[GridPoint(slot.x, slot.y)]
                val targetY = when {
                    slot.isHorizontal -> newHorizontalRow
                    explicitNewY != null -> explicitNewY
                    else -> (slot.y + rowDelta).coerceIn(0, rows - 1)
                }
                slot.copy(y = targetY)
            }
            stencilSlots.clear()
            stencilSlots.addAll(updatedSlots)
        }

        if (stencilSlots.isNotEmpty()) {
            checkStencilCompletion()
        }
    }

    /**
     * Adds a valid word's letters to giftLettersQueue for family assistance.
     * Anti-farming rule (fixes 2.3):
     * - Awards points ONLY the first time a unique word is gifted during the current game session.
     * - Does NOT unlock the word in the Family Word Album (words must be solved on the board to unlock).
     */
    fun addGiftWord(word: String): Boolean {
        val cleanWord = word.uppercase().trim()
        if (cleanWord.length >= 3 && Dictionary.isValidWord(cleanWord, isRussian)) {
            val canonical = Dictionary.normalizeYo(cleanWord)
            if (usedGiftWordsThisGame.add(canonical)) {
                val pts = cleanWord.length * 25
                score += pts
                lastGiftWordPointsAwarded = pts
            } else {
                lastGiftWordPointsAwarded = 0
            }
            for (char in cleanWord) {
                if (giftLettersQueue.size < 24) {
                    giftLettersQueue.add(char)
                }
            }
            return true
        }
        return false
    }

    fun canResumeTetris(): Boolean {
        for (col in 0 until cols) {
            if (board[0][col] != null || board[1][col] != null || board[2][col] != null) {
                return false
            }
        }
        return true
    }

    fun forceLowerStackAndResume() {
        clearExistingAnchorsFromBoard()
        for (r in 0 until 6) {
            for (c in 0 until cols) {
                board[r][c] = null
            }
        }
        applyGravityToStackedBlocks()
        mode = GameMode.TETRIS
        generateNewTargetWords()
        placeCrosswordStencilOnBoard()
        spawnPiece()
    }

    fun skipToNextStencilPuzzle(respawnActivePiece: Boolean = true) {
        clearExistingAnchorsFromBoard()
        generateNewTargetWords()
        placeCrosswordStencilOnBoard()
        if (respawnActivePiece || activePiece == null) {
            spawnPiece()
        }
    }

    fun resetGame() {
        board = Array(rows) { arrayOfNulls<GameCell>(cols) }
        score = 0
        comboCount = 0
        wordsClearedCount = 0
        freeHintCharges = 3
        usedMagnetOnCurrentStencil = false
        isHorizontalCrossSolved = false
        isVerticalCrossSolved = false
        arcadeRescueUsed = false
        lastEarnedBonus = 0
        lastGiftWordPointsAwarded = 0
        heldPieceShapeType = null
        heldPieceLetters = null
        canHoldThisTurn = true
        undoStack.clear()
        mode = GameMode.TETRIS
        giftLettersQueue.clear()
        usedGiftWordsThisGame.clear()
        crosswordClues.clear()
        generateNewTargetWords()
        placeCrosswordStencilOnBoard()
        spawnPiece()
    }
}

object GameStateSerializer {

    fun serialize(engine: GameEngine): String {
        val root = JSONObject()
        root.put("cols", engine.cols)
        root.put("rows", engine.rows)
        root.put("score", engine.score)
        root.put("comboCount", engine.comboCount)
        root.put("wordsClearedCount", engine.wordsClearedCount)
        root.put("freeHintCharges", engine.freeHintCharges)
        root.put("usedMagnetOnCurrentStencil", engine.usedMagnetOnCurrentStencil)
        root.put("isHorizontalCrossSolved", engine.isHorizontalCrossSolved)
        root.put("isVerticalCrossSolved", engine.isVerticalCrossSolved)
        root.put("arcadeRescueUsed", engine.arcadeRescueUsed)
        root.put("isRussian", engine.isRussian)
        root.put("mode", engine.mode.name)
        root.put("paceMode", engine.paceMode.name)
        root.put("selectedTheme", engine.selectedTheme.name)
        root.put("isSingleTileMode", engine.isSingleTileMode)
        root.put("isLargeFontMode", engine.isLargeFontMode)
        root.put("isPaperTheme", engine.isPaperTheme)
        root.put("isCrossedStencilMode", engine.isCrossedStencilMode)
        root.put("isDailyWordActive", engine.isDailyWordActive)
        root.put("dailyWordCompletedToday", engine.isDailyWordCompletedForToday())
        root.put("lastDailyWordCompletedDayKey", engine.lastDailyWordCompletedDayKey)
        root.put("nextPieceShapeType", engine.nextPieceShapeType)
        root.put("heldPieceShapeType", engine.heldPieceShapeType ?: -1)
        root.put("canHoldThisTurn", engine.canHoldThisTurn)
        root.put("stencilRow", engine.stencilRow)
        root.put("stencilStartCol", engine.stencilStartCol)
        root.put("questWord", engine.questWordDefinition?.word ?: "")
        root.put("secondaryQuestWord", engine.secondaryQuestWordDefinition?.word ?: "")

        val heldLettersArr = JSONArray()
        engine.heldPieceLetters?.forEach { heldLettersArr.put(it.toString()) }
        root.put("heldPieceLetters", heldLettersArr)

        val usedGiftsArr = JSONArray()
        engine.usedGiftWordsThisGame.forEach { usedGiftsArr.put(it) }
        root.put("usedGiftWordsThisGame", usedGiftsArr)

        engine.activePiece?.let { piece ->
            val pieceObj = JSONObject()
            pieceObj.put("shapeType", piece.shapeType)
            pieceObj.put("x", piece.x)
            pieceObj.put("y", piece.y)
            pieceObj.put("rotation", piece.rotation)
            val lettersArr = JSONArray()
            piece.letters.forEach { lettersArr.put(it.toString()) }
            pieceObj.put("letters", lettersArr)
            root.put("activePiece", pieceObj)
        }

        val slotsArray = JSONArray()
        engine.stencilSlots.forEach { slot ->
            val slotObj = JSONObject()
            slotObj.put("x", slot.x)
            slotObj.put("y", slot.y)
            slotObj.put("expectedChar", slot.expectedChar.toString())
            slotObj.put("isHorizontal", slot.isHorizontal)
            slotObj.put("isHintRevealed", slot.isHintRevealed)
            slotsArray.put(slotObj)
        }
        root.put("stencilSlots", slotsArray)

        val cellsArray = JSONArray()
        for (r in 0 until engine.rows) {
            for (c in 0 until engine.cols) {
                val cell = engine.board[r][c]
                if (cell != null) {
                    val cellObj = JSONObject()
                    cellObj.put("r", r)
                    cellObj.put("c", c)
                    cellObj.put("char", cell.char.toString())
                    cellObj.put("colorHex", cell.colorHex)
                    cellObj.put("isAnchor", cell.isAnchor)
                    cellsArray.put(cellObj)
                }
            }
        }
        root.put("boardCells", cellsArray)

        val targetArr = JSONArray()
        engine.levelTargetWords.forEach { targetArr.put(it) }
        root.put("targetWords", targetArr)

        val giftArr = JSONArray()
        engine.giftLettersQueue.forEach { giftArr.put(it.toString()) }
        root.put("giftQueue", giftArr)

        return root.toString()
    }

    fun deserialize(json: String, engine: GameEngine): Boolean {
        return try {
            val root = JSONObject(json)
            engine.undoStack.clear()
            engine.score = root.optInt("score", 0)
            engine.comboCount = root.optInt("comboCount", 0)
            engine.wordsClearedCount = root.optInt("wordsClearedCount", 0)
            engine.freeHintCharges = root.optInt("freeHintCharges", 3)
            engine.usedMagnetOnCurrentStencil = root.optBoolean("usedMagnetOnCurrentStencil", false)
            engine.isHorizontalCrossSolved = root.optBoolean("isHorizontalCrossSolved", false)
            engine.isVerticalCrossSolved = root.optBoolean("isVerticalCrossSolved", false)
            engine.arcadeRescueUsed = root.optBoolean("arcadeRescueUsed", false)
            engine.isRussian = root.optBoolean("isRussian", true)
            val modeName = root.optString("mode", GameMode.TETRIS.name)
            engine.mode = runCatching { GameMode.valueOf(modeName) }.getOrDefault(GameMode.TETRIS)
            val paceName = root.optString("paceMode", PaceMode.PUZZLE_STEP.name)
            engine.paceMode = runCatching { PaceMode.valueOf(paceName) }.getOrDefault(PaceMode.PUZZLE_STEP)
            val themeName = root.optString("selectedTheme", WordTheme.ALL.name)
            engine.selectedTheme = runCatching { WordTheme.valueOf(themeName) }.getOrDefault(WordTheme.ALL)
            engine.isSingleTileMode = root.optBoolean("isSingleTileMode", false)
            engine.isLargeFontMode = root.optBoolean("isLargeFontMode", true)
            engine.isPaperTheme = root.optBoolean("isPaperTheme", true)
            engine.isCrossedStencilMode = root.optBoolean("isCrossedStencilMode", false)
            engine.isDailyWordActive = root.optBoolean("isDailyWordActive", false)
            engine.lastDailyWordCompletedDayKey = root.optInt("lastDailyWordCompletedDayKey", -1)
            val rawDailyDone = root.optBoolean("dailyWordCompletedToday", false)
            val todayKey = Dictionary.getCurrentCalendarDayKey()
            engine.dailyWordCompletedToday = rawDailyDone && (
                engine.lastDailyWordCompletedDayKey == todayKey || engine.lastDailyWordCompletedDayKey == -1
            )
            if (engine.dailyWordCompletedToday && engine.lastDailyWordCompletedDayKey == -1) {
                engine.lastDailyWordCompletedDayKey = todayKey
            }
            engine.nextPieceShapeType = root.optInt("nextPieceShapeType", 0)
            engine.canHoldThisTurn = root.optBoolean("canHoldThisTurn", true)
            engine.stencilRow = root.optInt("stencilRow", engine.rows - 1)
            engine.stencilStartCol = root.optInt("stencilStartCol", 2)

            engine.usedGiftWordsThisGame.clear()
            val usedGiftsArr = root.optJSONArray("usedGiftWordsThisGame")
            if (usedGiftsArr != null) {
                for (i in 0 until usedGiftsArr.length()) {
                    val w = usedGiftsArr.optString(i)
                    if (w.isNotBlank()) engine.usedGiftWordsThisGame.add(w)
                }
            }

            val heldShape = root.optInt("heldPieceShapeType", -1)
            val heldLettersJson = root.optJSONArray("heldPieceLetters")
            if (heldShape >= 0 && heldLettersJson != null && heldLettersJson.length() > 0) {
                engine.heldPieceShapeType = heldShape
                engine.heldPieceLetters = (0 until heldLettersJson.length()).map {
                    heldLettersJson.optString(it, "A").firstOrNull() ?: 'A'
                }
            } else {
                engine.heldPieceShapeType = null
                engine.heldPieceLetters = null
            }

            engine.levelTargetWords.clear()
            val targetArr = root.optJSONArray("targetWords")
            if (targetArr != null) {
                for (i in 0 until targetArr.length()) {
                    val w = targetArr.optString(i)
                    if (w.isNotBlank()) engine.levelTargetWords.add(w)
                }
            }

            val savedQuestWord = root.optString("questWord", "")
            val primaryWordToLookup = savedQuestWord.ifBlank { engine.levelTargetWords.firstOrNull() ?: "" }
            if (primaryWordToLookup.isNotBlank()) {
                engine.questWordDefinition = Dictionary.findFirstMatchingDefinition(primaryWordToLookup, engine.isRussian)
            }
            val savedSecWord = root.optString("secondaryQuestWord", "")
            if (savedSecWord.isNotBlank()) {
                engine.secondaryQuestWordDefinition = Dictionary.findFirstMatchingDefinition(savedSecWord, engine.isRussian)
            } else if (engine.isCrossedStencilMode && engine.levelTargetWords.size >= 2) {
                engine.secondaryQuestWordDefinition = Dictionary.findFirstMatchingDefinition(engine.levelTargetWords[1], engine.isRussian)
            } else {
                engine.secondaryQuestWordDefinition = null
            }

            if (engine.questWordDefinition == null) {
                engine.generateNewTargetWords()
            }

            engine.giftLettersQueue.clear()
            val giftArr = root.optJSONArray("giftQueue")
            if (giftArr != null) {
                for (i in 0 until giftArr.length()) {
                    val ch = giftArr.optString(i).firstOrNull()
                    if (ch != null) engine.giftLettersQueue.add(ch)
                }
            }

            for (r in 0 until engine.rows) {
                for (c in 0 until engine.cols) {
                    engine.board[r][c] = null
                }
            }

            val cellsArray = root.optJSONArray("boardCells")
            if (cellsArray != null) {
                for (i in 0 until cellsArray.length()) {
                    val obj = cellsArray.optJSONObject(i) ?: continue
                    val r = obj.optInt("r", -1)
                    val c = obj.optInt("c", -1)
                    val ch = obj.optString("char", "A").firstOrNull() ?: 'A'
                    val colorHex = obj.optString("colorHex", "#4DD0E1")
                    val isAnchor = obj.optBoolean("isAnchor", false)
                    if (r in 0 until engine.rows && c in 0 until engine.cols) {
                        engine.board[r][c] = GameCell(
                            char = ch,
                            colorHex = colorHex,
                            isWildcard = ch == Dictionary.WILDCARD_CHAR,
                            isAnchor = isAnchor
                        )
                    }
                }
            }

            engine.stencilSlots.clear()
            val slotsArray = root.optJSONArray("stencilSlots")
            if (slotsArray != null && slotsArray.length() > 0) {
                for (i in 0 until slotsArray.length()) {
                    val slotObj = slotsArray.optJSONObject(i) ?: continue
                    val x = slotObj.optInt("x", 0)
                    val y = slotObj.optInt("y", engine.stencilRow)
                    val expectedChar = slotObj.optString("expectedChar", "A").firstOrNull() ?: 'A'
                    val isHorizontal = slotObj.optBoolean("isHorizontal", true)
                    val isHintRevealed = slotObj.optBoolean("isHintRevealed", false)
                    engine.stencilSlots.add(
                        StencilSlot(
                            x = x,
                            y = y,
                            expectedChar = expectedChar,
                            isHorizontal = isHorizontal,
                            isHintRevealed = isHintRevealed
                        )
                    )
                }
            } else {
                engine.placeCrosswordStencilOnBoard()
            }

            val activePieceObj = root.optJSONObject("activePiece")
            if (activePieceObj != null && engine.mode == GameMode.TETRIS) {
                val shapeType = activePieceObj.optInt("shapeType", 0)
                val x = activePieceObj.optInt("x", engine.cols / 2)
                val y = activePieceObj.optInt("y", 1)
                val rotation = activePieceObj.optInt("rotation", 0)
                val lettersArr = activePieceObj.optJSONArray("letters")
                val letters = mutableListOf<Char>()
                if (lettersArr != null) {
                    for (i in 0 until lettersArr.length()) {
                        letters.add(lettersArr.optString(i, "A").firstOrNull() ?: 'A')
                    }
                }
                if (letters.isNotEmpty()) {
                    engine.activePiece = ActivePiece(
                        shapeType = shapeType,
                        x = x,
                        y = y,
                        rotation = rotation,
                        letters = letters
                    )
                } else {
                    engine.spawnPiece()
                }
            } else if (engine.mode == GameMode.TETRIS) {
                engine.spawnPiece()
            } else if (engine.mode == GameMode.CROSSWORD) {
                engine.activePiece = null
                engine.scanBoardForCrosswordWords()
            } else {
                engine.activePiece = null
            }
            true
        } catch (e: Throwable) {
            e.printStackTrace()
            false
        }
    }
}
