package com.example.game

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import kotlin.random.Random

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
 * Empty stencil cells glow on the grid and show which expectedChar completes the word!
 */
data class StencilSlot(
    val x: Int,
    val y: Int,
    val expectedChar: Char
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
 * - ARCADE: Classic dynamic Tetris speed.
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
    var isRussian: Boolean = true
) {
    var board = Array(rows) { arrayOfNulls<GameCell>(cols) }

    var mode = GameMode.TETRIS
    var paceMode = PaceMode.PUZZLE_STEP // Default to calm, stress-free Puzzle Step mode!
    var selectedTheme = WordTheme.ALL
    var isSingleTileMode = false // When true, spawns simple 1x1 letter tiles instead of 4-block shapes
    var isLargeFontMode = true   // Default to comfortable large font & high contrast for family/seniors

    var score = 0
    var comboCount = 0
    var wordsClearedCount = 0
    var freeHintCharges = 3 // Start with 3 generous helper charges for family-friendly play

    var activePiece: ActivePiece? = null
    var nextPieceShapeType: Int = Random.nextInt(7)

    // Hold pocket state
    var heldPieceShapeType: Int? = null
    var heldPieceLetters: List<Char>? = null
    var canHoldThisTurn: Boolean = true

    // Current words selected for letters during Tetris game
    var levelTargetWords = mutableListOf<String>()

    // Active Crossword Stencil Puzzle ("Живой Кроссворд-Трафарет в середине поля")
    var questWordDefinition: WordDefinition? = null
    var stencilSlots = mutableListOf<StencilSlot>()
    var stencilRow: Int = rows - 2
    var stencilStartCol: Int = 2
    var lastResonanceWord: String? = null
    var lastSafetyClearTriggered: Boolean = false

    // Gift letters entered by user
    val giftLettersQueue = mutableListOf<Char>()

    // Current clues found during Crossword Mode
    var crosswordClues = mutableListOf<CrosswordClue>()

    init {
        generateNewTargetWords()
        placeCrosswordStencilOnBoard()
        spawnPiece()
    }

    /**
     * Chooses target words from the dictionary (filtered by selectedTheme) and sets the active Crossword Stencil word.
     */
    fun generateNewTargetWords() {
        levelTargetWords.clear()
        val defs = Dictionary.getDefinitionsForTheme(isRussian, selectedTheme)
        if (defs.isNotEmpty()) {
            val shuffled = defs.shuffled()
            val selected = shuffled.take(4)
            levelTargetWords.addAll(selected.map { it.word })
            questWordDefinition = selected.firstOrNull()
        }
    }

    /**
     * Places the "Slow Crossword Stencil" right on the board:
     * Pre-fills most letters of the target word as anchor blocks and leaves 1 glowing empty gap slot
     * so seniors/kids can visually see the crossword word on the board and drop the missing letter into it,
     * which then triggers a classic Tetris line-clear explosion!
     */
    fun placeCrosswordStencilOnBoard() {
        stencilSlots.clear()
        val quest = questWordDefinition ?: return
        val word = quest.word

        // Pick a comfortable row near the bottom/middle of the stack
        var targetRow = rows - 2
        for (r in (rows - 2) downTo 6) {
            val occupiedCount = (0 until cols).count { board[r][it] != null && board[r][it]?.isAnchor != true }
            if (occupiedCount <= cols - word.length - 1) {
                targetRow = r
                break
            }
        }
        stencilRow = targetRow
        stencilStartCol = ((cols - word.length) / 2).coerceAtLeast(0)

        // Leave 1 central letter slot open as the puzzle gap
        val missingIndex = if (word.length > 2) Random.nextInt(1, word.length - 1) else 0

        for (i in word.indices) {
            val c = stencilStartCol + i
            if (!isValidPoint(c, stencilRow)) continue
            val ch = word[i]
            stencilSlots.add(StencilSlot(x = c, y = stencilRow, expectedChar = ch))

            if (i != missingIndex) {
                // Place pre-filled Crossword Anchor Tile
                board[stencilRow][c] = GameCell(
                    char = ch,
                    colorHex = "#5E35B1",
                    isWildcard = false,
                    isAnchor = true
                )
            } else {
                // Leave this slot empty so the player can drop the matching letter into it (or use Magnet)
                board[stencilRow][c] = null
            }
        }
    }

    /**
     * Checks if the active Crossword Stencil on the board has been completed (either by matching letter,
     * wildcard '★', or filling all stencil slots). When completed, clears the entire Tetris row!
     */
    fun checkStencilCompletion(): Boolean {
        if (stencilSlots.isEmpty()) return false
        val quest = questWordDefinition ?: return false

        val allMatched = stencilSlots.all { slot ->
            val cell = if (isValidPoint(slot.x, slot.y)) board[slot.y][slot.x] else null
            cell != null && (cell.char == slot.expectedChar || cell.char == Dictionary.WILDCARD_CHAR)
        }

        if (allMatched) {
            triggerStencilTetrisLineClear(quest.word)
            return true
        }
        return false
    }

    /**
     * Clears the entire row of the solved Crossword Stencil (plus any row above if stacked) like Tetris,
     * shifts upper blocks down, awards points & free hints, and spawns the next Crossword Stencil!
     */
    fun triggerStencilTetrisLineClear(solvedWord: String) {
        comboCount++
        wordsClearedCount++
        val bonus = 250 + (comboCount - 1) * 50
        score += bonus
        freeHintCharges++
        lastResonanceWord = solvedWord

        val rowToClear = stencilRow.coerceIn(0, rows - 1)
        // Clear the entire horizontal line at rowToClear (Tetris line vanish!)
        for (c in 0 until cols) {
            board[rowToClear][c] = null
        }

        // Apply smooth gravity so blocks above drop down
        applyGravityToStackedBlocks()

        // Generate the next Crossword Puzzle word and place its stencil
        generateNewTargetWords()
        placeCrosswordStencilOnBoard()
    }

    /**
     * Smart "Magnet" Helper for kids/seniors:
     * Automatically places the next missing letter directly into the Crossword Stencil slot on the board!
     */
    fun useMagnetToFillStencilSlot(): Boolean {
        if (stencilSlots.isEmpty()) {
            placeCrosswordStencilOnBoard()
        }
        val missingSlot = stencilSlots.firstOrNull { slot ->
            val cell = if (isValidPoint(slot.x, slot.y)) board[slot.y][slot.x] else null
            cell == null || (cell.char != slot.expectedChar && cell.char != Dictionary.WILDCARD_CHAR)
        } ?: return false

        if (freeHintCharges > 0) {
            freeHintCharges--
        } else {
            score = maxOf(0, score - 20)
        }

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
     * Allows the player to type/solve the active Stencil Crossword word directly at any time
     * to immediately trigger the Tetris Line Clear!
     */
    fun solveActiveStencilByWord(guess: String): Boolean {
        val quest = questWordDefinition ?: return false
        if (quest.word.equals(guess.trim(), ignoreCase = true)) {
            triggerStencilTetrisLineClear(quest.word)
            return true
        }
        return false
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

        // Find missing letters in the current stencil so the player ALWAYS gets the letter they need!
        val missingStencilChars = stencilSlots.mapNotNull { slot ->
            val cell = if (isValidPoint(slot.x, slot.y)) board[slot.y][slot.x] else null
            if (cell == null || cell.char != slot.expectedChar) slot.expectedChar else null
        }

        if (count == 1) {
            // In 1-tile mode for kids/seniors, 85% of the time give the exact needed stencil letter, 15% Golden Star ★
            val singleChar = when {
                giftLettersQueue.isNotEmpty() -> giftLettersQueue.removeAt(0)
                missingStencilChars.isNotEmpty() && Random.nextFloat() < 0.88f -> missingStencilChars.random()
                else -> Dictionary.WILDCARD_CHAR
            }
            return listOf(singleChar)
        }

        for (i in 0 until count) {
            if (giftLettersQueue.isNotEmpty()) {
                pieceLetters.add(giftLettersQueue.removeAt(0))
            } else if (i == 0 && missingStencilChars.isNotEmpty()) {
                // Guarantee the needed crossword letter is inside every piece!
                pieceLetters.add(missingStencilChars.random())
            } else if (i == 1 && Random.nextFloat() < 0.16f) {
                // Friendly Golden Wildcard ('★')
                pieceLetters.add(Dictionary.WILDCARD_CHAR)
            } else if (Random.nextFloat() < 0.75f && levelTargetWords.isNotEmpty()) {
                val word = levelTargetWords.random()
                pieceLetters.add(word.random())
            } else {
                val pool = if (i % 2 == 0) vowels else consonants
                pieceLetters.add(pool.random())
            }
        }

        if (pieceLetters.none { it in vowels || it == Dictionary.WILDCARD_CHAR }) {
            pieceLetters[Random.nextInt(pieceLetters.size)] = vowels.random()
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
            nextPieceShapeType = if (isSingleTileMode) TetrominoShapes.SINGLE_TILE_SHAPE else Random.nextInt(7)
        }

        // In Single-Tile mode, align X with the missing stencil slot or center for convenience
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
                // Soft Family No-Lose Protection: gently clear top rows instead of harsh punishment!
                softClearTopRowsForFamily()
                activePiece = piece
            } else {
                mode = GameMode.CROSSWORD
                scanBoardForCrosswordWords()
                activePiece = null
            }
        } else {
            activePiece = piece
        }
    }

    /**
     * Gentle "No-Lose" safety net for seniors, moms, and children:
     * Clears the top 6 rows of non-anchor clutter so the game never abruptly ends in frustration.
     */
    fun softClearTopRowsForFamily() {
        for (r in 0 until (rows - 4)) {
            for (c in 0 until cols) {
                if (board[r][c]?.isAnchor != true) {
                    board[r][c] = null
                }
            }
        }
        applyGravityToStackedBlocks()
        placeCrosswordStencilOnBoard()
        lastSafetyClearTriggered = true
    }

    fun shiftPieceLetters(): Boolean {
        val piece = activePiece ?: return false
        if (piece.letters.size < 2) {
            // In 1-tile mode, shifting cycles between the needed stencil letter and the Golden Wildcard ★!
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
            // In 1-tile mode, tapping rotate shifts the letter to ★ or needed char
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
        var dropSteps = 0
        while (movePiece(0, 1)) {
            dropSteps++
        }
        score += dropSteps * 2
        lockPiece()
    }

    fun lockPiece() {
        val piece = activePiece ?: return
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
        val stencilSolved = checkStencilCompletion()

        // 2. Also check if any other dictionary word resonated on the board
        if (!stencilSolved) {
            checkLiveQuestWordResonance()
        }

        // 3. Check standard Tetris completed horizontal lines
        checkCompletedLines()

        var topReached = false
        for (col in 0 until cols) {
            if (board[0][col] != null || board[1][col] != null || board[2][col] != null) {
                topReached = true
                break
            }
        }

        if (topReached) {
            if (paceMode != PaceMode.ARCADE) {
                // Gentle family-friendly auto-clear so there's never a stressful Game Over
                softClearTopRowsForFamily()
                spawnPiece()
            } else {
                mode = GameMode.CROSSWORD
                scanBoardForCrosswordWords()
            }
        } else {
            spawnPiece()
        }
    }

    private fun checkLiveQuestWordResonance() {
        val target = questWordDefinition?.word ?: return
        val foundClues = findAllBoardMatches(onlyTargetWord = target)
        if (foundClues.isNotEmpty()) {
            val match = foundClues.first()
            triggerStencilTetrisLineClear(match.word)
        }
    }

    private fun checkCompletedLines() {
        var linesClearedThisTurn = 0
        for (r in rows - 1 downTo 0) {
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
                linesClearedThisTurn++
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
            placeCrosswordStencilOnBoard()
        }
    }

    private fun findAllBoardMatches(onlyTargetWord: String? = null): List<CrosswordClue> {
        val matches = mutableListOf<CrosswordClue>()

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
                                if (matchedDef != null && (onlyTargetWord == null || matchedDef.word == onlyTargetWord)) {
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
                                if (matchedDef != null && (onlyTargetWord == null || matchedDef.word == onlyTargetWord)) {
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

        if (clue.word.uppercase().trim() == guess.uppercase().trim()) {
            clue.isSolved = true
            clue.isRevealed = true
            clue.revealedLettersCount = clue.length
            comboCount++
            wordsClearedCount++
            val reward = (150 * (1.0 + (comboCount - 1) * 0.2)).toInt()
            score += reward

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

            clearWordFromBoardAndLine(clue)
            return true
        }
        return false
    }

    /**
     * When a word is solved in Crossword Mode, clears the word AND vanishes its entire horizontal row
     * like a classic Tetris line-clear so the stack drops dramatically!
     */
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

    fun applyGravityToStackedBlocks() {
        for (col in 0 until cols) {
            val filledCellsInCol = mutableListOf<GameCell>()
            for (row in 0 until rows) {
                val cell = board[row][col]
                if (cell != null) {
                    filledCellsInCol.add(cell)
                }
            }
            for (row in 0 until rows) {
                val bottomOffset = rows - 1 - row
                if (bottomOffset >= 0) {
                    val listIndex = filledCellsInCol.size - 1 - row
                    if (listIndex >= 0) {
                        board[bottomOffset][col] = filledCellsInCol[listIndex]
                    } else {
                        board[bottomOffset][col] = null
                    }
                }
            }
        }
    }

    fun addGiftWord(word: String): Boolean {
        val cleanWord = word.uppercase().trim()
        if (cleanWord.length >= 2 && Dictionary.isValidWord(cleanWord, isRussian)) {
            score += cleanWord.length * 25
            for (char in cleanWord) {
                giftLettersQueue.add(char)
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

    fun skipToNextStencilPuzzle() {
        for (slot in stencilSlots) {
            if (isValidPoint(slot.x, slot.y) && board[slot.y][slot.x]?.isAnchor == true) {
                board[slot.y][slot.x] = null
            }
        }
        generateNewTargetWords()
        placeCrosswordStencilOnBoard()
        spawnPiece()
    }

    fun resetGame() {
        board = Array(rows) { arrayOfNulls<GameCell>(cols) }
        score = 0
        comboCount = 0
        wordsClearedCount = 0
        freeHintCharges = 3
        heldPieceShapeType = null
        heldPieceLetters = null
        canHoldThisTurn = true
        mode = GameMode.TETRIS
        giftLettersQueue.clear()
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
        root.put("isRussian", engine.isRussian)
        root.put("mode", engine.mode.name)
        root.put("paceMode", engine.paceMode.name)
        root.put("selectedTheme", engine.selectedTheme.name)
        root.put("isSingleTileMode", engine.isSingleTileMode)
        root.put("isLargeFontMode", engine.isLargeFontMode)
        root.put("nextPieceShapeType", engine.nextPieceShapeType)
        root.put("heldPieceShapeType", engine.heldPieceShapeType ?: -1)

        val heldLettersArr = JSONArray()
        engine.heldPieceLetters?.forEach { heldLettersArr.put(it.toString()) }
        root.put("heldPieceLetters", heldLettersArr)

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
            engine.resetGame()
            engine.score = root.optInt("score", 0)
            engine.comboCount = root.optInt("comboCount", 0)
            engine.wordsClearedCount = root.optInt("wordsClearedCount", 0)
            engine.freeHintCharges = root.optInt("freeHintCharges", 3)
            engine.isRussian = root.optBoolean("isRussian", true)
            val modeName = root.optString("mode", GameMode.TETRIS.name)
            engine.mode = runCatching { GameMode.valueOf(modeName) }.getOrDefault(GameMode.TETRIS)
            val paceName = root.optString("paceMode", PaceMode.PUZZLE_STEP.name)
            engine.paceMode = runCatching { PaceMode.valueOf(paceName) }.getOrDefault(PaceMode.PUZZLE_STEP)
            val themeName = root.optString("selectedTheme", WordTheme.ALL.name)
            engine.selectedTheme = runCatching { WordTheme.valueOf(themeName) }.getOrDefault(WordTheme.ALL)
            engine.isSingleTileMode = root.optBoolean("isSingleTileMode", false)
            engine.isLargeFontMode = root.optBoolean("isLargeFontMode", true)
            engine.nextPieceShapeType = root.optInt("nextPieceShapeType", 0)

            val heldShape = root.optInt("heldPieceShapeType", -1)
            val heldLettersJson = root.optJSONArray("heldPieceLetters")
            if (heldShape >= 0 && heldLettersJson != null && heldLettersJson.length() > 0) {
                engine.heldPieceShapeType = heldShape
                engine.heldPieceLetters = (0 until heldLettersJson.length()).map {
                    heldLettersJson.optString(it, "A").firstOrNull() ?: 'A'
                }
            }

            engine.levelTargetWords.clear()
            val targetArr = root.optJSONArray("targetWords")
            if (targetArr != null) {
                for (i in 0 until targetArr.length()) {
                    val w = targetArr.optString(i)
                    if (w.isNotBlank()) engine.levelTargetWords.add(w)
                }
            }
            if (engine.levelTargetWords.isEmpty()) {
                engine.generateNewTargetWords()
            } else {
                engine.questWordDefinition = Dictionary.findFirstMatchingDefinition(
                    engine.levelTargetWords.first(),
                    engine.isRussian
                )
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

            engine.placeCrosswordStencilOnBoard()

            if (engine.mode == GameMode.TETRIS) {
                engine.spawnPiece()
            } else if (engine.mode == GameMode.CROSSWORD) {
                engine.scanBoardForCrosswordWords()
            }
            true
        } catch (e: Throwable) {
            e.printStackTrace()
            false
        }
    }
}
