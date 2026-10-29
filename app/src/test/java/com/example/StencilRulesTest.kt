package com.example

import com.example.game.ActivePiece
import com.example.game.Dictionary
import com.example.game.GameCell
import com.example.game.GameEngine
import com.example.game.GameMode
import com.example.game.GameStateSerializer
import com.example.game.PaceMode
import com.example.game.StencilSlot
import com.example.game.TetrominoShapes
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * Unit test suite verifying:
 * - T1–T10: Remnant relocation, NoProgressHelp, landingRow/shadow parity, wrong letter auto-undo, score regression, and Crossed Stencil mode.
 * - T11–T21: High tower immediate help, high tower warning banner, critical tower 1-row protection, anchor/slot preservation across 300 random boards,
 *   active piece preservation (pocket / queue), backward-compatible old save loading, undo after protection, and Arcade mode behavior.
 */
class StencilRulesTest {

    /**
     * T1. На 300 случайных полях с остатками вызов placeCrosswordStencilOnBoard оставляет 0 закрытых открытых лунок.
     */
    @Test
    fun t1_placeCrosswordStencilOnBoard_leavesZeroClosedOpenSlots_across300RandomBoardsWithRemnants() {
        val rng = Random(42)
        val engine = GameEngine(cols = 10, rows = 15, isRussian = true)
        val russianLetters = "АБВГДЕЖЗИЙКЛМНОПРСТУФХЦЧШЩЫЭЮЯ"

        repeat(300) { iteration ->
            for (r in 0 until engine.rows) {
                for (c in 0 until engine.cols) {
                    engine.board[r][c] = null
                }
            }
            val fillDensity = rng.nextInt(8, 45)
            repeat(fillDensity) {
                val r = rng.nextInt(5, engine.rows)
                val c = rng.nextInt(0, engine.cols)
                val ch = russianLetters[rng.nextInt(russianLetters.length)]
                engine.board[r][c] = GameCell(char = ch, colorHex = "#4DD0E1", isAnchor = false)
            }

            engine.isCrossedStencilMode = (iteration % 4 == 0)
            engine.generateNewTargetWords()
            engine.placeCrosswordStencilOnBoard()

            val openSlots = engine.getOpenStencilSlotsInReadingOrder()
            assertTrue("Iteration $iteration must have at least 1 open slot", openSlots.isNotEmpty())
            val closedOpenSlots = engine.getClosedOpenStencilSlotsInReadingOrder()
            assertEquals(
                "Iteration $iteration must leave 0 closed open slots after placeCrosswordStencilOnBoard",
                0,
                closedOpenSlots.size
            )
            for (slot in openSlots) {
                assertNull("Open slot (${slot.x}, ${slot.y}) must be empty", engine.board[slot.y][slot.x])
                assertFalse("Open slot (${slot.x}, ${slot.y}) must not be closed", engine.isStencilSlotClosed(slot))
            }
        }
    }

    /**
     * T2. После решения слова на поле с остатками следующий трафарет имеет 0 закрытых открытых лунок.
     */
    @Test
    fun t2_solvingWordOnBoardWithRemnants_leavesZeroClosedOpenSlotsOnNextStencil() {
        val rng = Random(108)
        val engine = GameEngine(cols = 10, rows = 15, isRussian = true)
        val russianLetters = "АБВГДЕЖЗИКЛМНОПРСТУФХЦЧШЩЯ"

        repeat(50) { cycle ->
            for (r in 6..13) {
                for (c in 0 until engine.cols) {
                    if (rng.nextFloat() < 0.45f) {
                        val ch = russianLetters[rng.nextInt(russianLetters.length)]
                        engine.board[r][c] = GameCell(char = ch, colorHex = "#4DD0E1", isAnchor = false)
                    }
                }
            }

            if (cycle % 2 == 0) {
                val targetWord = engine.questWordDefinition?.word ?: "ГОРА"
                assertTrue(engine.solveActiveStencilByWord(targetWord))
            } else {
                for (slot in engine.stencilSlots) {
                    if (engine.board[slot.y][slot.x] == null) {
                        engine.board[slot.y][slot.x] = GameCell(
                            char = slot.expectedChar,
                            colorHex = "#4DD0E1",
                            isAnchor = false
                        )
                    }
                }
                assertTrue(engine.checkStencilCompletion())
            }

            val closedAfterSolve = engine.getClosedOpenStencilSlotsInReadingOrder()
            assertEquals(
                "Cycle $cycle: next stencil after word clear must have 0 closed open slots",
                0,
                closedAfterSolve.size
            )
        }
    }

    /**
     * T3. При искусственно закрытой лунке после 3 ходов без прогресса следующая фигура содержит
     * нужную букву на нижнем крае, после её опускания слово очищается, счётчик равен 0.
     */
    @Test
    fun t3_closedSlot_after3MovesWithoutProgress_spawnsHelpPieceWithNeededLetterOnBottom_andClearsWord() {
        val engine = GameEngine(cols = 10, rows = 15, isRussian = true)
        val def = Dictionary.findFirstMatchingDefinition("ГОРА", isRussian = true)!!
        engine.startSpecificWordOnBoard(def)

        val openSlot = engine.getOpenStencilSlotsInReadingOrder().first()
        val expectedChar = openSlot.expectedChar

        // Artificially close the open slot with a player block above it at y = 11
        engine.board[11][openSlot.x] = GameCell(char = 'Щ', colorHex = "#4DD0E1", isAnchor = false)
        assertTrue("Slot must be detected as closed", engine.isStencilSlotClosed(openSlot))
        assertEquals(0, engine.movesWithoutStencilProgress)

        val safeCol = if (openSlot.x >= 3) 0 else 8

        repeat(GameEngine.NO_PROGRESS_LIMIT) {
            engine.activePiece = ActivePiece(
                shapeType = TetrominoShapes.SINGLE_TILE_SHAPE,
                x = safeCol,
                y = 1,
                rotation = 0,
                letters = listOf('Ъ')
            )
            engine.hardDrop()
        }

        // After 3 moves without progress with a closed slot, NoProgressHelp must have triggered on the newly spawned piece!
        assertTrue("NoProgressHelp must trigger after 3 moves without progress on a closed slot", engine.isHelpPieceActive)
        assertEquals("Counter equals NO_PROGRESS_LIMIT (3) while help piece is falling", GameEngine.NO_PROGRESS_LIMIT, engine.movesWithoutStencilProgress)
        assertFalse("Path to target slot must now be open", engine.isStencilSlotClosed(openSlot))

        val helpPiece = engine.activePiece!!
        val points = helpPiece.getPoints()
        val bottomIndex = points.indices.maxByOrNull { points[it].y }!!
        assertEquals(
            "Bottom edge of help piece must carry the needed letter",
            Dictionary.normalizeYo(expectedChar.toString()),
            Dictionary.normalizeYo(helpPiece.letters[bottomIndex].toString())
        )

        // Align the help piece with the target open slot and drop it
        val wordsClearedBefore = engine.wordsClearedCount
        engine.activePiece = helpPiece.copy(x = openSlot.x)
        engine.hardDrop()

        // Word "ГОРА" must clear automatically and spawn the next stencil, with counter == 0
        assertEquals(0, engine.movesWithoutStencilProgress)
        assertTrue("wordsClearedCount must increment after dropping help piece", engine.wordsClearedCount > wordsClearedBefore)
        assertTrue("lastUnlockedWords must contain ГОРА", engine.lastUnlockedWords.contains("ГОРА"))
        assertNotNull(engine.questWordDefinition)
    }

    /**
     * T4. При всех доступных лунках и низкой башне (y > TOWER_WARNING_ROW) 10 ходов без прогресса
     * не вызывают ни помощь (NoProgressHelp), ни предупреждение (isTowerWarningActive == false),
     * ни защиту башни (towerProtectionTriggerCount == 0) после каждого хода.
     */
    @Test
    fun t4_tenMovesWithoutProgress_whenAllOpenSlotsAreReachableAndTowerLow_neverTriggersHelpWarningOrProtection() {
        val engine = GameEngine(cols = 10, rows = 15, isRussian = true)
        val def = Dictionary.findFirstMatchingDefinition("ГОРА", isRussian = true)!!
        engine.startSpecificWordOnBoard(def)

        val openSlot = engine.getOpenStencilSlotsInReadingOrder().first()
        assertFalse("Open slot is reachable", engine.isStencilSlotClosed(openSlot))

        // Distribute 10 moves across the 9 non-slot columns so blocks accumulate naturally on rows 12..14 (y > TOWER_WARNING_ROW)
        val safeCols = (0 until engine.cols).filter { it != openSlot.x }

        repeat(10) { i ->
            engine.activePiece = ActivePiece(
                shapeType = TetrominoShapes.SINGLE_TILE_SHAPE,
                x = safeCols[i % safeCols.size],
                y = 1,
                rotation = 0,
                letters = listOf('Ъ')
            )
            engine.hardDrop()

            assertFalse("Tower must remain low (y > TOWER_WARNING_ROW) at move ${i + 1}", engine.isTowerHigh())
            assertFalse("Open slot must remain open at move ${i + 1}", engine.isStencilSlotClosed(openSlot))
            assertFalse(
                "Help must NEVER trigger at move ${i + 1} when all open slots are reachable",
                engine.isHelpPieceActive
            )
            assertFalse(
                "lastHelpTriggered must be false at move ${i + 1}",
                engine.lastHelpTriggered
            )
            assertFalse(engine.isTowerWarningActive)
            assertEquals(0, engine.towerProtectionTriggerCount)
        }
        assertEquals(
            "movesWithoutStencilProgress must equal 10 after 10 natural moves without progress",
            10,
            engine.movesWithoutStencilProgress
        )
    }

    /**
     * T5. Попадание подходящей буквы в открытую лунку сбрасывает счётчик в 0.
     */
    @Test
    fun t5_matchingLetterInOpenSlot_resetsMovesWithoutStencilProgressToZero() {
        val engine = GameEngine(cols = 10, rows = 15, isRussian = true)
        val def = Dictionary.findFirstMatchingDefinition("ПИРОГ", isRussian = true)!!
        engine.startSpecificWordOnBoard(def)

        val openSlots = engine.getOpenStencilSlotsInReadingOrder()
        assertEquals(2, openSlots.size)
        val firstSlot = openSlots[0]
        val secondSlot = openSlots[1]

        val safeCol = (0 until engine.cols).first { it != firstSlot.x && it != secondSlot.x }
        repeat(2) {
            engine.activePiece = ActivePiece(
                shapeType = TetrominoShapes.SINGLE_TILE_SHAPE,
                x = safeCol,
                y = 1,
                rotation = 0,
                letters = listOf('Ъ')
            )
            engine.hardDrop()
        }
        assertEquals(2, engine.movesWithoutStencilProgress)

        // Now drop the matching letter into the first open slot
        engine.activePiece = ActivePiece(
            shapeType = TetrominoShapes.SINGLE_TILE_SHAPE,
            x = firstSlot.x,
            y = 1,
            rotation = 0,
            letters = listOf(firstSlot.expectedChar)
        )
        engine.hardDrop()

        assertEquals(
            "Matching letter in open slot must reset movesWithoutStencilProgress to 0",
            0,
            engine.movesWithoutStencilProgress
        )
    }

    /**
     * T6. undoLastMove после помощи возвращает полное содержимое board (все клетки по координатам),
     * карман (heldPieceShapeType, heldPieceLetters), трафарет, счётчик ходов без прогресса,
     * isTowerWarningActive и towerProtectionTriggerCount к состоянию до хода.
     */
    @Test
    fun t6_undoLastMove_afterHelp_restoresBoardPocketStencilCountersAndWarningFlagsToPreMoveState() {
        val engine = GameEngine(cols = 10, rows = 15, isRussian = true)
        val def = Dictionary.findFirstMatchingDefinition("ГОРА", isRussian = true)!!
        engine.startSpecificWordOnBoard(def)

        val openSlot = engine.getOpenStencilSlotsInReadingOrder().first()
        val safeCol = (0 until engine.cols).first { it != openSlot.x }

        // Close the open slot with a block at row 11 and populate Hold pocket before the move
        engine.board[11][openSlot.x] = GameCell(char = 'Щ', colorHex = "#4DD0E1", isAnchor = false)
        engine.heldPieceShapeType = 4
        engine.heldPieceLetters = listOf('Д', 'Е', 'Ж', 'З')

        // Make 2 moves without progress (counter becomes 2)
        repeat(2) {
            engine.activePiece = ActivePiece(
                shapeType = TetrominoShapes.SINGLE_TILE_SHAPE,
                x = safeCol,
                y = 1,
                rotation = 0,
                letters = listOf('Ъ')
            )
            engine.hardDrop()
        }
        assertEquals(2, engine.movesWithoutStencilProgress)
        assertTrue(engine.isStencilSlotClosed(openSlot))

        // Capture pre-move state: warning flags, protection counter, pocket, stencil, and full board by coordinates
        val warningBefore3rdMove = engine.isTowerWarningActive
        val protectionCountBefore3rdMove = engine.towerProtectionTriggerCount
        val heldShapeBefore3rdMove = engine.heldPieceShapeType
        val heldLettersBefore3rdMove = engine.heldPieceLetters?.toList()
        val counterBefore3rdMove = engine.movesWithoutStencilProgress
        val boardBefore3rdMove = Array(engine.rows) { r ->
            Array(engine.cols) { c -> engine.board[r][c]?.copy() }
        }
        val stencilBefore3rdMove = engine.stencilSlots.toList()

        // 3rd move without progress triggers NoProgressHelp (relocating the blocking cell & spawning help piece)
        engine.activePiece = ActivePiece(
            shapeType = TetrominoShapes.SINGLE_TILE_SHAPE,
            x = safeCol,
            y = 1,
            rotation = 0,
            letters = listOf('Ъ')
        )
        engine.hardDrop()

        assertTrue("Help must have triggered on 3rd move", engine.isHelpPieceActive)
        assertEquals(GameEngine.NO_PROGRESS_LIMIT, engine.movesWithoutStencilProgress)
        assertFalse(engine.isStencilSlotClosed(openSlot))

        // Call undoLastMove() -> must restore board cell-for-cell, pocket, stencilSlots, counter, and tower flags
        assertTrue(engine.undoLastMove())
        assertEquals("Counter must be restored after undo", counterBefore3rdMove, engine.movesWithoutStencilProgress)
        assertEquals("isTowerWarningActive must match pre-move value after undo", warningBefore3rdMove, engine.isTowerWarningActive)
        assertEquals("towerProtectionTriggerCount must match pre-move value after undo", protectionCountBefore3rdMove, engine.towerProtectionTriggerCount)
        assertEquals("Hold pocket shape must match pre-move value after undo", heldShapeBefore3rdMove, engine.heldPieceShapeType)
        assertEquals("Hold pocket letters must match pre-move value after undo", heldLettersBefore3rdMove, engine.heldPieceLetters)
        assertFalse("isHelpPieceActive must be false after undo", engine.isHelpPieceActive)
        assertTrue("Slot must be closed again after undo", engine.isStencilSlotClosed(openSlot))
        assertEquals("Stencil slots must match pre-move stencil", stencilBefore3rdMove, engine.stencilSlots.toList())
        assertEquals("Quest word must remain ГОРА", "ГОРА", engine.questWordDefinition?.word)

        for (r in 0 until engine.rows) {
            for (c in 0 until engine.cols) {
                assertEquals(
                    "Cell ($c, $r) must match pre-3rd-move board after undo",
                    boardBefore3rdMove[r][c],
                    engine.board[r][c]
                )
            }
        }
    }

    /**
     * T7. На 100 случайных полях и фигурах строка тени совпадает со строкой фигуры после hardDrop.
     */
    @Test
    fun t7_ghostShadowRow_matchesHardDropLandingRow_across100RandomBoardsAndPieces() {
        val rng = Random(777)
        val engine = GameEngine(cols = 10, rows = 15, isRussian = true)
        val russianLetters = "АБВГДЕЖЗИКЛМНОПРСТУФХЦЧШЩЯ"

        repeat(100) { i ->
            for (r in 0 until engine.rows) {
                for (c in 0 until engine.cols) {
                    engine.board[r][c] = null
                }
            }
            engine.placeCrosswordStencilOnBoard()

            repeat(rng.nextInt(0, 18)) {
                val r = rng.nextInt(7, 14)
                val c = rng.nextInt(0, engine.cols)
                if (engine.board[r][c] == null) {
                    engine.board[r][c] = GameCell(
                        char = russianLetters[rng.nextInt(russianLetters.length)],
                        colorHex = "#4DD0E1",
                        isAnchor = false
                    )
                }
            }
            engine.ensureOpenSlotsUnobstructedByRemnants()

            engine.spawnPiece()
            repeat(rng.nextInt(0, 3)) { engine.rotatePiece() }
            repeat(rng.nextInt(0, 4)) {
                if (rng.nextBoolean()) engine.movePiece(-1, 0) else engine.movePiece(1, 0)
            }

            val piece = engine.activePiece!!
            val expectedLandingY = engine.landingRow(piece)
            val expectedGhostPoints = engine.getGhostPiecePoints()
            val expectedBottomY = expectedGhostPoints.maxOf { it.y }

            val simulatedPointsAtLanding = piece.copy(y = expectedLandingY).getPoints()
            assertEquals(
                "Iteration $i: ghost points must match piece points at landingRow",
                simulatedPointsAtLanding,
                expectedGhostPoints
            )
            assertEquals(
                "Iteration $i: bottom Y of ghost points must match simulated landing points",
                simulatedPointsAtLanding.maxOf { it.y },
                expectedBottomY
            )
        }
    }

    /**
     * T8. Неправильная буква в открытую лунку отменяется автоматически, лунка остаётся открытой,
     * появляется предупреждение «Эта буква сюда не подходит».
     */
    @Test
    fun t8_wrongLetterInOpenSlot_autoUndoesMove_keepsSlotOpen_andShowsWarning() {
        val engine = GameEngine(cols = 10, rows = 15, isRussian = true)
        val def = Dictionary.findFirstMatchingDefinition("ГОРА", isRussian = true)!!
        engine.startSpecificWordOnBoard(def)

        val openSlot = engine.getOpenStencilSlotsInReadingOrder().first()
        val wrongChar = if (openSlot.expectedChar == 'Ж') 'Ш' else 'Ж'

        engine.activePiece = ActivePiece(
            shapeType = TetrominoShapes.SINGLE_TILE_SHAPE,
            x = openSlot.x,
            y = 1,
            rotation = 0,
            letters = listOf(wrongChar)
        )
        engine.hardDrop()

        assertNull("Open slot must remain empty after wrong letter auto-undo", engine.board[openSlot.y][openSlot.x])
        assertEquals(
            "Warning message must match specification",
            "Эта буква сюда не подходит",
            engine.lastWrongSlotLetterMessage
        )
    }

    /**
     * T9. Регрессия: сборка слова фигурами, вводом и магнитом по-прежнему очищает слово;
     * очки за слова из 4 и 5 букв не изменились.
     */
    @Test
    fun t9_regression_pieceAssembly_wordInput_andMagnetClearWord_andScoresUnchanged() {
        // 1. Piece assembly on 4-letter word ("ГОРА") -> word bonus 250 pts on combo 1
        val engine1 = GameEngine(cols = 10, rows = 15, isRussian = true)
        val goraDef = Dictionary.findFirstMatchingDefinition("ГОРА", isRussian = true)!!
        engine1.startSpecificWordOnBoard(goraDef)
        val slot1 = engine1.getOpenStencilSlotsInReadingOrder().first()
        engine1.activePiece = ActivePiece(
            shapeType = TetrominoShapes.SINGLE_TILE_SHAPE,
            x = slot1.x,
            y = 1,
            rotation = 0,
            letters = listOf(slot1.expectedChar)
        )
        engine1.hardDrop()
        assertEquals("4-letter word bonus on combo 1 must be 250", 250, engine1.lastEarnedBonus)
        assertTrue("4-letter word must be unlocked by piece assembly", engine1.lastUnlockedWords.contains("ГОРА"))

        // 2. Word input on 5-letter word ("ПИРОГ") -> combo 2 word bonus = 300, combo 3 word bonus = 350
        val pirogDef = Dictionary.findFirstMatchingDefinition("ПИРОГ", isRussian = true)!!
        engine1.startSpecificWordOnBoard(pirogDef)
        val scoreBeforePirog = engine1.score
        assertTrue(engine1.solveActiveStencilByWord("ПИРОГ"))
        assertEquals("Word bonus on combo 2 must be 300", 300, engine1.lastEarnedBonus)
        assertEquals("Score must increase by 300 on combo 2 word input", scoreBeforePirog + 300, engine1.score)

        engine1.startSpecificWordOnBoard(pirogDef)
        assertTrue(engine1.solveActiveStencilByWord("ПИРОГ"))
        assertEquals("Word bonus on combo 3 must be 350", 350, engine1.lastEarnedBonus)

        // 3. Magnet completion clears word and awards 20 pts per magnet completion without combo boost
        val engine2 = GameEngine(cols = 10, rows = 15, isRussian = true)
        engine2.startSpecificWordOnBoard(goraDef)
        assertTrue(engine2.useMagnetToFillStencilSlot())
        assertTrue(engine2.lastUnlockedWords.contains("ГОРА"))
        assertEquals(20, engine2.score)
    }

    /**
     * T10. Режим «✚ Крест»: после постановки трафарета из двух слов на 100 случайных словах/полях
     * ни одна открытая лунка обоих слов (горизонтального и вертикального) не закрыта.
     */
    @Test
    fun t10_crossedStencilMode_afterPlacingTwoWords_noOpenSlotInEitherWordIsClosed() {
        val rng = Random(2026)
        val engine = GameEngine(cols = 10, rows = 15, isRussian = true)
        engine.isCrossedStencilMode = true
        val russianLetters = "АБВГДЕЖЗИКЛМНОПРСТУФХЦЧШЩЯ"

        repeat(100) { iter ->
            for (r in 0 until engine.rows) {
                for (c in 0 until engine.cols) {
                    engine.board[r][c] = null
                }
            }
            repeat(rng.nextInt(5, 25)) {
                val r = rng.nextInt(6, engine.rows)
                val c = rng.nextInt(0, engine.cols)
                engine.board[r][c] = GameCell(
                    char = russianLetters[rng.nextInt(russianLetters.length)],
                    colorHex = "#4DD0E1",
                    isAnchor = false
                )
            }

            engine.generateNewTargetWords()
            engine.placeCrosswordStencilOnBoard()

            assertTrue("Crossed stencil iteration $iter must have horizontal slots", engine.stencilSlots.any { it.isHorizontal })
            assertTrue("Crossed stencil iteration $iter must have vertical slots", engine.stencilSlots.any { !it.isHorizontal })

            val openSlots = engine.getOpenStencilSlotsInReadingOrder()
            assertTrue("Crossed stencil iteration $iter must have open slots in both words", openSlots.size >= 2)
            for (slot in openSlots) {
                assertFalse(
                    "Crossed stencil iteration $iter: open slot (${slot.x}, ${slot.y}, isHorizontal=${slot.isHorizontal}) must NOT be closed",
                    engine.isStencilSlotClosed(slot)
                )
            }
        }
    }

    /**
     * T11. Высокая башня (y <= 4) и закрытая лунка: помощь срабатывает сразу, без трёх ходов;
     * фигура с нужной буквой внизу; блоки башни перенесены, а не удалены.
     */
    @Test
    fun t11_highTowerWithClosedSlot_triggersHelpImmediatelyWithoutThreeMoves_andRelocatesWithoutDeleting() {
        val engine = GameEngine(cols = 10, rows = 15, isRussian = true)
        val yablokoDef = Dictionary.findFirstMatchingDefinition("ЯБЛОКО", isRussian = true)!!
        engine.startSpecificWordOnBoard(yablokoDef)

        // Configure ЯБЛОКО at cols 2..7 so slot 'Б' at col 3 is an open slot
        val slotB = engine.stencilSlots.first { it.expectedChar == 'Б' }
        engine.board[slotB.y][slotB.x] = null
        assertEquals("Slot Б is at column 3", 3, slotB.x)

        // Block column 3 above slot Б (simulating S-piece blocking col 3 at y = 12)
        engine.board[12][slotB.x] = GameCell(char = 'С', colorHex = "#81C784", isAnchor = false)
        assertTrue("Slot Б must be closed", engine.isStencilSlotClosed(slotB))

        // Build tower in column 5 up to y = 5, then drop 1 piece at y = 4 so tower becomes high (y <= TOWER_WARNING_ROW = 4)
        for (r in 5..13) {
            engine.board[r][5] = GameCell(char = 'Н', colorHex = "#4DD0E1", isAnchor = false)
        }
        val playerBlocksBeforeMove = (0 until engine.rows).sumOf { r ->
            (0 until engine.cols).count { c -> engine.board[r][c] != null && engine.board[r][c]?.isAnchor == false }
        }

        // Make just 1 move (not 3!) landing at (5, 4) -> high tower (y <= 4)
        engine.activePiece = ActivePiece(
            shapeType = TetrominoShapes.SINGLE_TILE_SHAPE,
            x = 5,
            y = 1,
            rotation = 0,
            letters = listOf('Ъ')
        )
        engine.hardDrop()

        val targetOpenSlot = engine.getOpenStencilSlotsInReadingOrder().first()
        assertEquals("Only 1 move without progress was made", 1, engine.movesWithoutStencilProgress)
        assertTrue("NoProgressHelp must trigger immediately on high tower + closed slot", engine.isHelpPieceActive)
        assertTrue("lastHelpTriggered must be true", engine.lastHelpTriggered)
        assertFalse("Target open slot must now be unobstructed", engine.isStencilSlotClosed(targetOpenSlot))

        // Verify help piece has the needed letter on its bottom edge
        val helpPiece = engine.activePiece!!
        val pts = helpPiece.getPoints()
        val bottomIdx = pts.indices.maxByOrNull { pts[it].y }!!
        assertEquals(
            "Bottom cell of help piece must carry the expected letter of the target slot",
            Dictionary.normalizeYo(targetOpenSlot.expectedChar.toString()),
            Dictionary.normalizeYo(helpPiece.letters[bottomIdx].toString())
        )

        // Verify blocks were relocated, NOT deleted (playerBlocksBeforeMove + 1 newly locked tile)
        val playerBlocksAfterMove = (0 until engine.rows).sumOf { r ->
            (0 until engine.cols).count { c -> engine.board[r][c] != null && engine.board[r][c]?.isAnchor == false }
        }
        assertEquals("Zero blocks may be deleted during high-tower help", 0, engine.lastRemovedBlocksCount)
        assertEquals("Zero blocks may be deleted by tower protection", 0, engine.lastProtectionRemovedBlocksCount)
        assertEquals("All player blocks must be preserved via relocation", playerBlocksBeforeMove + 1, playerBlocksAfterMove)
    }

    /**
     * T12. Высокая башня без закрытых лунок: появляется предупреждение, помощь не срабатывает, ход не блокируется.
     */
    @Test
    fun t12_highTowerWithoutClosedSlots_showsWarning_doesNotTriggerHelp_andDoesNotBlockMoves() {
        val engine = GameEngine(cols = 10, rows = 15, isRussian = true)
        val def = Dictionary.findFirstMatchingDefinition("ГОРА", isRussian = true)!!
        engine.startSpecificWordOnBoard(def)

        val openSlot = engine.getOpenStencilSlotsInReadingOrder().first()
        val towerCol = (0 until engine.cols).first { it != openSlot.x }

        // Stack blocks in towerCol from row 5..13, then drop 1 tile onto row 4 (TOWER_WARNING_ROW = 4)
        for (r in 5..13) {
            engine.board[r][towerCol] = GameCell(char = 'Л', colorHex = "#4DD0E1", isAnchor = false)
        }
        engine.activePiece = ActivePiece(
            shapeType = TetrominoShapes.SINGLE_TILE_SHAPE,
            x = towerCol,
            y = 1,
            rotation = 0,
            letters = listOf('Ъ')
        )
        engine.hardDrop()

        assertTrue("Tower must be detected as high (y <= 4)", engine.isTowerHigh())
        assertTrue("No open slots are closed", engine.getClosedOpenStencilSlotsInReadingOrder().isEmpty())
        assertTrue("Warning flag isTowerWarningActive must be true", engine.isTowerWarningActive)
        assertEquals(
            "Башня близко к верху. Соберите слово или отмените ход",
            engine.lastTowerWarningMessage
        )
        assertFalse("NoProgressHelp must NOT trigger when no slot is closed", engine.isHelpPieceActive)
        assertFalse("lastHelpTriggered must be false", engine.lastHelpTriggered)

        // Verify move is NOT blocked: player can move and drop the next piece freely
        assertEquals(GameMode.TETRIS, engine.mode)
        assertNotNull(engine.activePiece)
        val otherCol = (0 until engine.cols).first { it != openSlot.x && it != towerCol }
        engine.activePiece = ActivePiece(
            shapeType = TetrominoShapes.SINGLE_TILE_SHAPE,
            x = otherCol,
            y = 1,
            rotation = 0,
            letters = listOf('Ъ')
        )
        engine.hardDrop()
        assertNotNull("Moves must continue normally without blocking", engine.board[14][otherCol])
    }

    /**
     * T13. Критическая башня (y <= 2), перенос невозможен: защита удаляет не больше одного ряда за срабатывание,
     * только блоки игрока; число в сообщении равно фактически удалённым блокам.
     */
    @Test
    fun t13_criticalTowerWhenRelocationImpossible_removesAtMostOneTopRow_onlyPlayerBlocks_andReportsExactCount() {
        val engine = GameEngine(cols = 10, rows = 15, isRussian = true)
        val def = Dictionary.findFirstMatchingDefinition("ГОРА", isRussian = true)!!
        engine.startSpecificWordOnBoard(def)

        val openSlots = engine.getOpenStencilSlotsInReadingOrder()
        val openCols = openSlots.map { it.x }.toSet()

        // Fill all safe rows 3..14 in all non-open-slot columns so relocation into safe rows is impossible
        for (r in 3 until engine.rows) {
            for (c in 0 until engine.cols) {
                if (c in openCols) continue
                if (engine.board[r][c] == null) {
                    engine.board[r][c] = GameCell(char = 'П', colorHex = "#4DD0E1", isAnchor = false)
                }
            }
        }

        // Populate critical rows: row 1 has 6 player blocks, row 2 has 7 player blocks
        val row1Cols = (0 until engine.cols).filter { it !in openCols }.take(6)
        val row2Cols = (0 until engine.cols).filter { it !in openCols }.take(7)
        for (c in row1Cols) {
            engine.board[1][c] = GameCell(char = 'Б', colorHex = "#FFB74D", isAnchor = false)
        }
        for (c in row2Cols) {
            engine.board[2][c] = GameCell(char = 'В', colorHex = "#81C784", isAnchor = false)
        }

        val totalBlocksBefore = (0 until engine.rows).sumOf { r ->
            (0 until engine.cols).count { c -> engine.board[r][c] != null }
        }
        val scoreBefore = engine.score
        val comboBefore = engine.comboCount

        // Trigger protection
        engine.softClearTopRowsForFamily()

        val totalBlocksAfter = (0 until engine.rows).sumOf { r ->
            (0 until engine.cols).count { c -> engine.board[r][c] != null }
        }
        val actualRemoved = totalBlocksBefore - totalBlocksAfter

        // Only row 1 (6 blocks) must be removed; row 2 (7 blocks) must remain intact!
        assertEquals("Exactly 6 blocks from topmost occupied row 1 must be removed", 6, actualRemoved)
        assertEquals("Reported removed count must equal actual removed blocks", actualRemoved, engine.lastProtectionRemovedBlocksCount)
        assertEquals(
            "Protection message (RU) must report exact removed block count",
            "Освобождён верх поля: убрано блоков — 6",
            engine.lastProtectionMessage
        )
        engine.isRussian = false
        assertEquals(
            "Protection message (EN) must report exact removed block count",
            "Top of the board cleared: blocks removed — 6",
            engine.formatProtectionBlocksRemovedMessage(engine.lastProtectionRemovedBlocksCount)
        )
        engine.isRussian = true
        for (c in row1Cols) {
            assertNull("Topmost occupied row 1 must be cleared at col $c", engine.board[1][c])
        }
        for (c in row2Cols) {
            assertNotNull("Row 2 must NOT be deleted (at most 1 row removed per trigger)", engine.board[2][c])
        }
        // Score and comboCount must not change
        assertEquals(scoreBefore, engine.score)
        assertEquals(comboBefore, engine.comboCount)
    }

    /**
     * T14. На 300 случайных полях с трафаретом, остатками и высокой башней защита никогда не удаляет
     * блоки трафарета, якоря и буквы в лунках; после срабатывания открытые лунки остаются открытыми.
     */
    @Test
    fun t14_across300RandomBoardsWithHighTower_protectionNeverDeletesStencilAnchorsOrSlotLetters_andKeepsOpenSlotsOpen() {
        val rng = Random(1414)
        val engine = GameEngine(cols = 10, rows = 15, isRussian = true)
        val russianLetters = "АБВГДЕЖЗИКЛМНОПРСТУФХЦЧШЩЯ"

        repeat(300) { iter ->
            for (r in 0 until engine.rows) {
                for (c in 0 until engine.cols) {
                    engine.board[r][c] = null
                }
            }
            engine.isCrossedStencilMode = (iter % 3 == 0)
            engine.generateNewTargetWords()
            engine.placeCrosswordStencilOnBoard()

            // For words with >= 2 open slots, optionally fill 1 open slot with a matching player letter (non-anchor)
            val initialOpenSlots = engine.getOpenStencilSlotsInReadingOrder()
            if (initialOpenSlots.size >= 2 && iter % 2 == 0) {
                val filledSlot = initialOpenSlots.first()
                engine.board[filledSlot.y][filledSlot.x] = GameCell(
                    char = filledSlot.expectedChar,
                    colorHex = "#81C784",
                    isAnchor = false
                )
            }

            // Scatter random remnants and critical/high tower blocks across rows 0..13
            repeat(rng.nextInt(15, 65)) {
                val r = rng.nextInt(0, engine.rows)
                val c = rng.nextInt(0, engine.cols)
                if (engine.board[r][c] == null && engine.stencilSlots.none { it.x == c && it.y == r }) {
                    engine.board[r][c] = GameCell(
                        char = russianLetters[rng.nextInt(russianLetters.length)],
                        colorHex = "#4DD0E1",
                        isAnchor = false
                    )
                }
            }

            // Record all protected cells (anchors + stencil cells with matching letters) before protection
            val protectedBefore = mutableMapOf<Pair<Int, Int>, Char>()
            for (r in 0 until engine.rows) {
                for (c in 0 until engine.cols) {
                    if (engine.isProtectedStencilOrSlotCell(c, r)) {
                        protectedBefore[c to r] = engine.board[r][c]!!.char
                    }
                }
            }
            assertTrue("Iteration $iter must have protected stencil/anchor cells", protectedBefore.isNotEmpty())

            engine.softClearTopRowsForFamily()

            // Verify every protected cell (stencil block, anchor, or letter in slot) is untouched
            for ((coord, expectedChar) in protectedBefore) {
                val (c, r) = coord
                val cellAfter = engine.board[r][c]
                assertNotNull("Iteration $iter: protected cell at ($c, $r) must NEVER be deleted", cellAfter)
                assertEquals("Iteration $iter: protected cell char at ($c, $r) must not change", expectedChar, cellAfter!!.char)
            }

            // Verify all remaining open slots are open and unobstructed
            val openSlotsAfter = engine.getOpenStencilSlotsInReadingOrder()
            assertTrue("Iteration $iter must still have open slots", openSlotsAfter.isNotEmpty())
            for (slot in openSlotsAfter) {
                assertFalse(
                    "Iteration $iter: open slot (${slot.x}, ${slot.y}) must remain unobstructed after protection",
                    engine.isStencilSlotClosed(slot)
                )
            }
        }
    }

    /**
     * T15. Защита не удаляет падающую фигуру: она попадает в карман или возвращается в очередь.
     */
    @Test
    fun t15_protectionNeverDeletesFallingPiece_movesToHoldPocketOrReturnsToNextQueue() {
        val engine = GameEngine(cols = 10, rows = 15, isRussian = true)

        // Case 1: Hold pocket is empty -> falling piece goes into Hold Pocket
        engine.heldPieceShapeType = null
        engine.heldPieceLetters = null
        engine.board[1][4] = GameCell(char = 'Б', colorHex = "#4DD0E1", isAnchor = false)
        engine.activePiece = ActivePiece(
            shapeType = 2,
            x = 4,
            y = 1,
            rotation = 0,
            letters = listOf('К', 'Л', 'М', 'Н')
        )

        engine.softClearTopRowsForFamily()

        assertTrue("lastProtectionPieceToHold must be true when pocket was empty", engine.lastProtectionPieceToHold)
        assertFalse(engine.lastProtectionPieceToQueue)
        assertEquals("Фигура отложена в карман", engine.lastProtectionPieceMessage)
        assertEquals("Falling piece shape must be saved in pocket", 2, engine.heldPieceShapeType)
        assertEquals("Falling piece letters must be saved in pocket", listOf('К', 'Л', 'М', 'Н'), engine.heldPieceLetters)
        assertNotNull("A replacement activePiece from the queue must be present", engine.activePiece)

        // Case 2: Hold pocket is already occupied -> falling piece returns to Next Queue
        engine.board[1][4] = GameCell(char = 'В', colorHex = "#4DD0E1", isAnchor = false)
        engine.activePiece = ActivePiece(
            shapeType = 5,
            x = 4,
            y = 1,
            rotation = 0,
            letters = listOf('Р', 'С', 'Т', 'У')
        )

        engine.softClearTopRowsForFamily()

        assertFalse(engine.lastProtectionPieceToHold)
        assertTrue("lastProtectionPieceToQueue must be true when pocket was occupied", engine.lastProtectionPieceToQueue)
        assertEquals("Фигура возвращена в очередь", engine.lastProtectionPieceMessage)
        // Pocket must retain the first piece (shape 2, К-Л-М-Н)
        assertEquals(2, engine.heldPieceShapeType)
        assertEquals(listOf('К', 'Л', 'М', 'Н'), engine.heldPieceLetters)
        // Next queue must hold the returned piece (shape 5, Р-С-Т-У)
        assertEquals(5, engine.nextPieceShapeType)
        assertEquals(listOf('Р', 'С', 'Т', 'У'), engine.nextPieceLetters)
    }

    /**
     * T16. Старое сохранение без новых полей загружается без ошибок и даёт значения по умолчанию.
     */
    @Test
    fun t16_oldSaveWithoutNewFields_deserializesWithoutError_andUsesDefaultValues() {
        val engine = GameEngine(cols = 10, rows = 15, isRussian = true)
        engine.isTowerWarningActive = true
        engine.towerProtectionTriggerCount = 4
        engine.movesWithoutStencilProgress = 5

        // Build legacy JSON save without movesWithoutStencilProgress, isTowerWarningActive, towerProtectionTriggerCount
        val legacyJson = """
            {
              "cols": 10,
              "rows": 15,
              "score": 500,
              "comboCount": 2,
              "wordsClearedCount": 2,
              "freeHintCharges": 3,
              "isRussian": true,
              "mode": "TETRIS",
              "paceMode": "PUZZLE_STEP",
              "selectedTheme": "ALL",
              "stencilRow": 14,
              "stencilStartCol": 3,
              "questWord": "ГОРА",
              "boardCells": [],
              "stencilSlots": []
            }
        """.trimIndent()

        val loaded = GameStateSerializer.deserialize(legacyJson, engine)
        assertTrue("Legacy save JSON must deserialize cleanly", loaded)
        assertEquals(500, engine.score)
        assertEquals(2, engine.comboCount)
        assertEquals("Default movesWithoutStencilProgress must be 0", 0, engine.movesWithoutStencilProgress)
        assertFalse("Default isTowerWarningActive must be false", engine.isTowerWarningActive)
        assertEquals("Default towerProtectionTriggerCount must be 0", 0, engine.towerProtectionTriggerCount)
    }

    /**
     * T17. Откат undoLastMove после срабатывания защиты восстанавливает поле, карман и счётчики.
     */
    @Test
    fun t17_undoLastMove_afterProtection_restoresBoardPocketAndCounters() {
        val engine = GameEngine(cols = 10, rows = 15, isRussian = true)
        val def = Dictionary.findFirstMatchingDefinition("ГОРА", isRussian = true)!!
        engine.startSpecificWordOnBoard(def)

        engine.heldPieceShapeType = null
        engine.heldPieceLetters = null
        engine.movesWithoutStencilProgress = 2
        engine.towerProtectionTriggerCount = 0
        engine.isTowerWarningActive = true

        // Place critical tower block at (4, 1)
        engine.board[1][4] = GameCell(char = 'Д', colorHex = "#FFB74D", isAnchor = false)
        engine.activePiece = ActivePiece(
            shapeType = 3,
            x = 4,
            y = 1,
            rotation = 0,
            letters = listOf('Ф', 'Х', 'Ц', 'Ч')
        )

        val boardBeforeProtection = Array(engine.rows) { r ->
            Array(engine.cols) { c -> engine.board[r][c]?.copy() }
        }
        val stencilBeforeProtection: List<StencilSlot> = engine.stencilSlots.toList()

        // Trigger protection
        engine.softClearTopRowsForFamily()

        assertEquals(1, engine.towerProtectionTriggerCount)
        assertEquals(3, engine.heldPieceShapeType)
        assertEquals(listOf('Ф', 'Х', 'Ц', 'Ч'), engine.heldPieceLetters)
        assertNull("Critical block at (4, 1) must have been relocated out of row 1", engine.board[1][4])

        // Undo protection
        assertTrue(engine.undoLastMove())

        assertEquals("towerProtectionTriggerCount must be restored to 0", 0, engine.towerProtectionTriggerCount)
        assertEquals("movesWithoutStencilProgress must be restored to 2", 2, engine.movesWithoutStencilProgress)
        assertTrue("isTowerWarningActive must be restored to true", engine.isTowerWarningActive)
        assertNull("Hold pocket shape must be restored to null", engine.heldPieceShapeType)
        assertNull("Hold pocket letters must be restored to null", engine.heldPieceLetters)
        assertEquals("Stencil must be restored", stencilBeforeProtection, engine.stencilSlots.toList())
        assertEquals("Active piece letters must be restored", listOf('Ф', 'Х', 'Ц', 'Ч'), engine.activePiece?.letters)

        for (r in 0 until engine.rows) {
            for (c in 0 until engine.cols) {
                assertEquals(
                    "Board cell ($c, $r) must match pre-protection state after undo",
                    boardBeforeProtection[r][c]?.char,
                    engine.board[r][c]?.char
                )
            }
        }
    }

    /**
     * T18. В режиме «Аркада» (PaceMode.ARCADE) при высокой башне (y <= 4) показывается предупреждение
     * о высокой башне, мягкая защита не удаляет блоки, а при достижении критической зоны (y <= 2)
     * срабатывает Crossword Rescue (и Game Over при повторном касании).
     */
    @Test
    fun t18_arcadeMode_showsHighTowerWarning_doesNotDeleteBlocksWithProtection_andTriggersCrosswordRescueAtCriticalZone() {
        val engine = GameEngine(cols = 10, rows = 15, isRussian = true)
        engine.paceMode = PaceMode.ARCADE
        val def = Dictionary.findFirstMatchingDefinition("ГОРА", isRussian = true)!!
        engine.startSpecificWordOnBoard(def)

        val openSlot = engine.getOpenStencilSlotsInReadingOrder().first()
        val towerCol = (0 until engine.cols).first { it != openSlot.x }

        // 1. High tower at y = 4 (TOWER_WARNING_ROW) in Arcade mode -> shows warning banner, does not trigger protection
        for (r in 5..13) {
            engine.board[r][towerCol] = GameCell(char = 'Л', colorHex = "#4DD0E1", isAnchor = false)
        }
        engine.activePiece = ActivePiece(
            shapeType = TetrominoShapes.SINGLE_TILE_SHAPE,
            x = towerCol,
            y = 1,
            rotation = 0,
            letters = listOf('Ъ')
        )
        engine.hardDrop()

        assertTrue("Tower is high (y <= 4) in Arcade mode", engine.isTowerHigh())
        assertFalse("Tower is not yet critical (y > 2)", engine.isTowerCritical())
        assertTrue("High tower warning banner must be active in Arcade mode", engine.isTowerWarningActive)
        assertEquals(
            "Башня близко к верху. Соберите слово или отмените ход",
            engine.lastTowerWarningMessage
        )
        assertEquals("Protection must not trigger in Arcade mode", 0, engine.towerProtectionTriggerCount)
        assertEquals(GameMode.TETRIS, engine.mode)

        // 2. Reach critical zone (y <= 2) in Arcade mode -> protection does NOT delete blocks; Crossword Rescue triggers
        engine.board[2][towerCol] = GameCell(char = 'М', colorHex = "#4DD0E1", isAnchor = false)
        val totalBlocksBeforeCriticalLock = (0 until engine.rows).sumOf { r ->
            (0 until engine.cols).count { c -> engine.board[r][c] != null }
        }
        val otherCol = (0 until engine.cols).first { it != openSlot.x && it != towerCol }
        engine.activePiece = ActivePiece(
            shapeType = TetrominoShapes.SINGLE_TILE_SHAPE,
            x = otherCol,
            y = 1,
            rotation = 0,
            letters = listOf('Н')
        )
        engine.lockPiece()

        val totalBlocksAfterCriticalLock = (0 until engine.rows).sumOf { r ->
            (0 until engine.cols).count { c -> engine.board[r][c] != null }
        }
        assertEquals("Protection must NEVER trigger in Arcade mode", 0, engine.towerProtectionTriggerCount)
        assertEquals("Zero blocks may be removed by protection in Arcade mode", 0, engine.lastProtectionRemovedBlocksCount)
        assertFalse("lastSafetyClearTriggered must be false in Arcade mode", engine.lastSafetyClearTriggered)
        assertTrue(
            "No blocks may be removed in Arcade mode (board has at least all pre-existing blocks + locked tile)",
            totalBlocksAfterCriticalLock >= totalBlocksBeforeCriticalLock + 1
        )
        assertNotNull("Critical block at (towerCol, 2) must NOT be deleted in Arcade mode", engine.board[2][towerCol])
        assertNotNull("Newly locked critical block at (otherCol, 1) must NOT be deleted in Arcade mode", engine.board[1][otherCol])
        for (r in 4..13) {
            assertNotNull("Tower block at (towerCol, $r) must remain on board in Arcade mode", engine.board[r][towerCol])
        }
        assertEquals("First critical top-out in Arcade mode must trigger CROSSWORD rescue", GameMode.CROSSWORD, engine.mode)
        assertTrue("arcadeRescueUsed must be true after first critical top-out", engine.arcadeRescueUsed)

        // 3. Second critical top-out in Arcade mode -> if openSlot became closed by Crossword Rescue board words,
        //    help triggers first without deleting blocks, and when the help piece does not fill the slot at y <= 2,
        //    it transitions to GAME_OVER without deleting blocks
        engine.mode = GameMode.TETRIS
        engine.isHelpPieceActive = false
        val blocksBeforeSecondTopOut = (0 until engine.rows).sumOf { r ->
            (0 until engine.cols).count { c -> engine.board[r][c] != null }
        }
        engine.activePiece = ActivePiece(
            shapeType = TetrominoShapes.SINGLE_TILE_SHAPE,
            x = otherCol,
            y = 2,
            rotation = 0,
            letters = listOf('П')
        )
        engine.lockPiece()

        if (engine.mode == GameMode.TETRIS && engine.isHelpPieceActive) {
            val blocksAfterHelpOnSecondTopOut = (0 until engine.rows).sumOf { r ->
                (0 until engine.cols).count { c -> engine.board[r][c] != null }
            }
            assertEquals(0, engine.towerProtectionTriggerCount)
            assertEquals(0, engine.lastProtectionRemovedBlocksCount)
            assertEquals(blocksBeforeSecondTopOut + 1, blocksAfterHelpOnSecondTopOut)

            engine.activePiece = ActivePiece(
                shapeType = TetrominoShapes.SINGLE_TILE_SHAPE,
                x = otherCol,
                y = 2,
                rotation = 0,
                letters = listOf('П')
            )
            engine.lockPiece()
        }

        val blocksAfterSecondTopOut = (0 until engine.rows).sumOf { r ->
            (0 until engine.cols).count { c -> engine.board[r][c] != null }
        }
        assertEquals(GameMode.GAME_OVER, engine.mode)
        assertEquals(0, engine.towerProtectionTriggerCount)
        assertTrue(blocksAfterSecondTopOut >= blocksBeforeSecondTopOut + 1)
    }

    /**
     * T19. В режиме «Аркада» (PaceMode.ARCADE) при высокой башне (y <= 4, выше критической зоны)
     * и закрытой открытой лунке после фиксации фигуры сразу срабатывает помощь (NoProgressHelp),
     * параллельно показывается предупреждение о высокой башне (isTowerWarningActive == true),
     * ни один блок не удаляется и режим остаётся GameMode.TETRIS.
     */
    @Test
    fun t19_arcadeMode_highTowerWithClosedSlot_triggersHelpImmediately_showsWarningInParallel_deletesZeroBlocks_andStaysInTetrisMode() {
        val engine = GameEngine(cols = 10, rows = 15, isRussian = true)
        engine.paceMode = PaceMode.ARCADE
        val def = Dictionary.findFirstMatchingDefinition("ЯБЛОКО", isRussian = true)!!
        engine.startSpecificWordOnBoard(def)

        val openSlot = engine.getOpenStencilSlotsInReadingOrder().first()
        // Close the open slot from above at y = 12
        engine.board[12][openSlot.x] = GameCell(char = 'С', colorHex = "#81C784", isAnchor = false)
        assertTrue("Open slot must be closed before move", engine.isStencilSlotClosed(openSlot))

        // Build tower in another column up to y = 5, then lock a piece at y = 4 (high tower y <= 4, not critical y > 2)
        val towerCol = (0 until engine.cols).first { it != openSlot.x }
        for (r in 5..13) {
            engine.board[r][towerCol] = GameCell(char = 'Н', colorHex = "#4DD0E1", isAnchor = false)
        }
        val playerBlocksBefore = (0 until engine.rows).sumOf { r ->
            (0 until engine.cols).count { c -> engine.board[r][c] != null && engine.board[r][c]?.isAnchor == false }
        }

        engine.activePiece = ActivePiece(
            shapeType = TetrominoShapes.SINGLE_TILE_SHAPE,
            x = towerCol,
            y = 4,
            rotation = 0,
            letters = listOf('Ъ')
        )
        engine.lockPiece()

        // Verify help triggered immediately and warning is active in parallel
        assertTrue("isHelpPieceActive must be true immediately in Arcade mode on high tower + closed slot", engine.isHelpPieceActive)
        assertTrue("lastHelpTriggered must be true immediately in Arcade mode on high tower + closed slot", engine.lastHelpTriggered)
        assertTrue("isTowerWarningActive must be true in parallel with help in Arcade mode", engine.isTowerWarningActive)
        assertEquals(
            "Башня близко к верху. Соберите слово или отмените ход",
            engine.lastTowerWarningMessage
        )

        // Verify zero blocks were deleted and mode stays GameMode.TETRIS
        val playerBlocksAfter = (0 until engine.rows).sumOf { r ->
            (0 until engine.cols).count { c -> engine.board[r][c] != null && engine.board[r][c]?.isAnchor == false }
        }
        assertEquals("lastProtectionRemovedBlocksCount must be 0", 0, engine.lastProtectionRemovedBlocksCount)
        assertEquals("towerProtectionTriggerCount must be 0", 0, engine.towerProtectionTriggerCount)
        assertEquals("lastRemovedBlocksCount must be 0", 0, engine.lastRemovedBlocksCount)
        assertEquals("All player blocks plus the locked tile must remain on the board", playerBlocksBefore + 1, playerBlocksAfter)
        assertEquals("Mode must remain GameMode.TETRIS", GameMode.TETRIS, engine.mode)
    }

    /**
     * T20. В режиме «Аркада» (PaceMode.ARCADE) на критической зоне (y <= 2) при закрытой открытой лунке:
     * 1) фиксация обычной фигуры сначала вызывает помощь (isHelpPieceActive == true), mode == TETRIS, arcadeRescueUsed == false;
     * 2) затем фиксация фигуры помощи, которая не заполнила лунку, на критической зоне переводит игру в mode == CROSSWORD и arcadeRescueUsed == true;
     * 3) следующее касание критической зоны переводит игру в mode == GAME_OVER.
     */
    @Test
    fun t20_arcadeMode_criticalZoneWithClosedSlot_triggersHelpFirst_thenCrosswordRescueWhenHelpPieceFails_thenGameOver() {
        val engine = GameEngine(cols = 10, rows = 15, isRussian = true)
        engine.paceMode = PaceMode.ARCADE
        val def = Dictionary.findFirstMatchingDefinition("ЯБЛОКО", isRussian = true)!!
        engine.startSpecificWordOnBoard(def)

        val openSlot = engine.getOpenStencilSlotsInReadingOrder().first()
        // Close the open slot from above at y = 12
        engine.board[12][openSlot.x] = GameCell(char = 'С', colorHex = "#81C784", isAnchor = false)
        assertTrue("Open slot must be closed before critical-zone move", engine.isStencilSlotClosed(openSlot))

        // Build tower in another column at y = 3..13 and lock an ordinary piece at y = 2 (critical zone y <= 2)
        val towerCol = (0 until engine.cols).first { it != openSlot.x }
        for (r in 3..13) {
            engine.board[r][towerCol] = GameCell(char = 'Н', colorHex = "#4DD0E1", isAnchor = false)
        }
        val playerBlocksBeforeHelp = (0 until engine.rows).sumOf { r ->
            (0 until engine.cols).count { c -> engine.board[r][c] != null && engine.board[r][c]?.isAnchor == false }
        }

        engine.isHelpPieceActive = false
        engine.activePiece = ActivePiece(
            shapeType = TetrominoShapes.SINGLE_TILE_SHAPE,
            x = towerCol,
            y = 2,
            rotation = 0,
            letters = listOf('Ъ')
        )
        engine.lockPiece()

        // 1) Ordinary piece at critical zone with closed slot -> triggers NoProgressHelp first, stays in TETRIS, arcadeRescueUsed == false
        val playerBlocksAfterHelp = (0 until engine.rows).sumOf { r ->
            (0 until engine.cols).count { c -> engine.board[r][c] != null && engine.board[r][c]?.isAnchor == false }
        }
        assertTrue("isHelpPieceActive must be true on first critical lock with closed slot", engine.isHelpPieceActive)
        assertTrue("lastHelpTriggered must be true on first critical lock with closed slot", engine.lastHelpTriggered)
        assertEquals("Mode must remain GameMode.TETRIS when help triggers", GameMode.TETRIS, engine.mode)
        assertFalse("arcadeRescueUsed must remain false when help triggers", engine.arcadeRescueUsed)
        assertEquals("Zero blocks removed by protection", 0, engine.lastProtectionRemovedBlocksCount)
        assertEquals("Zero blocks removed during help relocation", 0, engine.lastRemovedBlocksCount)
        assertEquals("All player blocks plus the locked tile remain on the board", playerBlocksBeforeHelp + 1, playerBlocksAfterHelp)

        // 2) Lock the active help piece without filling the slot at critical zone (even if slot is closed again) -> CROSSWORD, arcadeRescueUsed == true
        engine.board[12][openSlot.x] = GameCell(char = 'С', colorHex = "#81C784", isAnchor = false)
        assertTrue("isHelpPieceActive is still true for the help piece turn", engine.isHelpPieceActive)
        engine.activePiece = ActivePiece(
            shapeType = TetrominoShapes.SINGLE_TILE_SHAPE,
            x = towerCol,
            y = 2,
            rotation = 0,
            letters = listOf('Ъ')
        )
        engine.lockPiece()

        assertEquals("After help piece fails to fill slot at critical zone, mode must be CROSSWORD", GameMode.CROSSWORD, engine.mode)
        assertTrue("arcadeRescueUsed must become true after Crossword Rescue", engine.arcadeRescueUsed)

        // 3) Next critical-zone touch after rescue -> GAME_OVER
        engine.mode = GameMode.TETRIS
        engine.isHelpPieceActive = false
        // Open slot is unobstructed (or help already used), lock at critical row y = 1
        for ( slot in engine.getOpenStencilSlotsInReadingOrder()) {
            for (r in 0 until slot.y) {
                if (engine.board[r][slot.x]?.isAnchor == false) {
                    engine.board[r][slot.x] = null
                }
            }
        }
        engine.activePiece = ActivePiece(
            shapeType = TetrominoShapes.SINGLE_TILE_SHAPE,
            x = towerCol,
            y = 1,
            rotation = 0,
            letters = listOf('П')
        )
        engine.lockPiece()

        assertEquals("Second critical top-out after Crossword Rescue must trigger GAME_OVER", GameMode.GAME_OVER, engine.mode)
    }

    /**
     * T21. В режиме «Аркада» (PaceMode.ARCADE) на критической зоне (y <= 2), когда закрытых открытых лунок нет,
     * сразу срабатывает Crossword Rescue (mode == GameMode.CROSSWORD, arcadeRescueUsed == true) без вызова помощи.
     */
    @Test
    fun t21_arcadeMode_criticalZoneWithoutClosedSlots_triggersCrosswordRescueImmediately() {
        val engine = GameEngine(cols = 10, rows = 15, isRussian = true)
        engine.paceMode = PaceMode.ARCADE
        val def = Dictionary.findFirstMatchingDefinition("ГОРА", isRussian = true)!!
        engine.startSpecificWordOnBoard(def)

        val openSlot = engine.getOpenStencilSlotsInReadingOrder().first()
        assertTrue("There must be no closed open slots", engine.getClosedOpenStencilSlotsInReadingOrder().isEmpty())

        val towerCol = (0 until engine.cols).first { it != openSlot.x }
        for (r in 3..13) {
            engine.board[r][towerCol] = GameCell(char = 'Л', colorHex = "#4DD0E1", isAnchor = false)
        }

        engine.activePiece = ActivePiece(
            shapeType = TetrominoShapes.SINGLE_TILE_SHAPE,
            x = towerCol,
            y = 2,
            rotation = 0,
            letters = listOf('Ъ')
        )
        engine.lockPiece()

        assertFalse("Help must not trigger when no open slot is closed", engine.isHelpPieceActive)
        assertFalse("lastHelpTriggered must be false when no open slot is closed", engine.lastHelpTriggered)
        assertEquals("Mode must immediately become GameMode.CROSSWORD", GameMode.CROSSWORD, engine.mode)
        assertTrue("arcadeRescueUsed must be true", engine.arcadeRescueUsed)
        assertEquals("Protection must not trigger in Arcade mode", 0, engine.towerProtectionTriggerCount)
        assertEquals("Zero blocks removed by protection", 0, engine.lastProtectionRemovedBlocksCount)
    }
}
