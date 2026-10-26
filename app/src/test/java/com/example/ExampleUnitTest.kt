package com.example

import com.example.game.Dictionary
import com.example.game.GameCell
import com.example.game.GameEngine
import com.example.game.GameMode
import com.example.game.PaceMode
import com.example.game.WordDefinition
import com.example.game.WordTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Comprehensive unit test suite verifying all TetriCross game engine rules and audit bug fixes.
 */
class ExampleUnitTest {

    @Test
    fun stencilSlots_hideLettersByDefault_andUseTwoGapsForFivePlusLetterWords() {
        val engine = GameEngine(cols = 10, rows = 15, isRussian = true)

        // 1. Test 5-letter word ("ПИРОГ") -> must have 2 open stencil slots and 3 pre-placed anchors
        val fiveLetterDef = Dictionary.findFirstMatchingDefinition("ПИРОГ", isRussian = true)!!
        engine.startSpecificWordOnBoard(fiveLetterDef)

        assertEquals(5, engine.stencilSlots.size)
        assertTrue(engine.stencilSlots.all { !it.isHintRevealed })
        val openFiveSlots = engine.stencilSlots.filter { slot ->
            engine.board[slot.y][slot.x] == null
        }
        assertEquals("5+ letter words must leave 2 missing slots", 2, openFiveSlots.size)

        // Reveal 1 slot hint and verify isHintRevealed becomes true and charge decreases
        val initialCharges = engine.freeHintCharges
        assertTrue(engine.revealStencilSlotLetterHint())
        assertEquals(initialCharges - 1, engine.freeHintCharges)
        assertEquals(1, engine.stencilSlots.count { it.isHintRevealed })

        // 2. Test 4-letter word ("ХЛЕБ") -> must have 1 open stencil slot and 3 pre-placed anchors
        val fourLetterDef = Dictionary.findFirstMatchingDefinition("ХЛЕБ", isRussian = true)!!
        engine.startSpecificWordOnBoard(fourLetterDef)
        assertEquals(4, engine.stencilSlots.size)
        val openFourSlots = engine.stencilSlots.filter { slot ->
            engine.board[slot.y][slot.x] == null
        }
        assertEquals("3-4 letter words must leave 1 missing slot", 1, openFourSlots.size)
    }

    @Test
    fun magnetEconomy_consumesCharges_andPreventsInfiniteScoreOrComboFarming() {
        val engine = GameEngine(cols = 10, rows = 15, isRussian = true)
        val fourLetterDef = Dictionary.findFirstMatchingDefinition("СТОЛ", isRussian = true)!!
        engine.startSpecificWordOnBoard(fourLetterDef)

        assertEquals(3, engine.freeHintCharges)
        assertEquals(0, engine.score)
        assertEquals(0, engine.comboCount)

        // Use Magnet 3 times on 4-letter words (each completes the 1 missing slot)
        repeat(3) { step ->
            engine.startSpecificWordOnBoard(fourLetterDef)
            val used = engine.useMagnetToFillStencilSlot()
            assertTrue("Magnet should succeed while freeHintCharges > 0 (step $step)", used)
            assertEquals("Combo must NOT grow when completed via Magnet", 0, engine.comboCount)
        }

        // All 3 free hint charges must now be exhausted (0 left, no refund!)
        assertEquals(0, engine.freeHintCharges)
        // Score from 3 Magnet completions is 3 * 20 = 60 points
        assertEquals(60, engine.score)

        // 4th Magnet costs 50 points and awards only 20 points (net -30 -> score drops to 30)
        engine.startSpecificWordOnBoard(fourLetterDef)
        assertTrue(engine.useMagnetToFillStencilSlot())
        assertEquals(30, engine.score)
        assertEquals(0, engine.freeHintCharges)

        // 5th Magnet must be rejected because freeHintCharges == 0 and score (30) < 50
        engine.startSpecificWordOnBoard(fourLetterDef)
        assertFalse("Magnet must fail when 0 charges and score < 50", engine.useMagnetToFillStencilSlot())
    }

    @Test
    fun noOrphanedAnchorTiles_afterTwentyStencilCyclesAndSoftClears() {
        val engine = GameEngine(cols = 10, rows = 15, isRussian = true)

        repeat(20) { cycle ->
            // Drop a piece or solve active stencil word
            val activeWord = engine.questWordDefinition?.word ?: "ДОМ"
            engine.solveActiveStencilByWord(activeWord)

            if (cycle % 5 == 0) {
                engine.softClearTopRowsForFamily()
            }

            // Every isAnchor cell on the entire board MUST belong to current stencilSlots!
            val currentStencilCoords = engine.stencilSlots.map { it.x to it.y }.toSet()
            for (r in 0 until engine.rows) {
                for (c in 0 until engine.cols) {
                    val cell = engine.board[r][c]
                    if (cell?.isAnchor == true) {
                        assertTrue(
                            "Orphaned isAnchor found at ($c, $r) on cycle $cycle",
                            currentStencilCoords.contains(c to r)
                        )
                    }
                }
            }
        }
    }

    @Test
    fun gravitySynchronizesStencilSlots_whenRowsBelowAreCleared() {
        val engine = GameEngine(cols = 10, rows = 15, isRussian = true)
        engine.clearExistingAnchorsFromBoard()
        for (r in 0 until engine.rows) {
            for (c in 0 until engine.cols) {
                engine.board[r][c] = null
            }
        }

        // Place a custom stencil at row 10 (above empty rows 11..14)
        val def = WordDefinition("ДОМ", "Уютное место", "Дом", WordTheme.HOME_COOKING)
        engine.questWordDefinition = def
        engine.stencilRow = 10
        engine.stencilStartCol = 3
        engine.stencilSlots.clear()
        engine.stencilSlots.add(com.example.game.StencilSlot(x = 3, y = 10, expectedChar = 'Д', isHorizontal = true))
        engine.stencilSlots.add(com.example.game.StencilSlot(x = 4, y = 10, expectedChar = 'О', isHorizontal = true))
        engine.stencilSlots.add(com.example.game.StencilSlot(x = 5, y = 10, expectedChar = 'М', isHorizontal = true))
        engine.board[10][3] = GameCell('Д', "#5E35B1", isAnchor = true)
        engine.board[10][4] = null // open slot
        engine.board[10][5] = GameCell('М', "#5E35B1", isAnchor = true)

        // Apply gravity: anchors at row 10 should fall to bottom row (rows - 1 = 14)
        engine.applyGravityToStackedBlocks()

        assertEquals(14, engine.stencilRow)
        assertTrue(engine.stencilSlots.all { it.y == 14 })
        assertEquals('Д', engine.board[14][3]?.char)
        assertEquals('М', engine.board[14][5]?.char)

        // Now filling the open slot at (4, 14) must complete the stencil!
        engine.board[14][4] = GameCell('О', "#4DD0E1")
        assertTrue(engine.checkStencilCompletion())
    }

    @Test
    fun liveQuestWordResonance_clearsActualMatchedRow() {
        val engine = GameEngine(cols = 10, rows = 15, isRussian = true)
        val domDef = Dictionary.findFirstMatchingDefinition("ДОМ", isRussian = true)!!
        engine.startSpecificWordOnBoard(domDef)

        // Put a non-stencil marker tile on row 8, and form the word "ДОМ" on row 8 (cols 0, 1, 2)
        // while stencilRow is at the bottom (row 13)
        engine.board[8][0] = GameCell('Д', "#4DD0E1")
        engine.board[8][1] = GameCell('О', "#4DD0E1")
        engine.board[8][7] = GameCell('Ъ', "#4DD0E1") // marker tile on row 8 that should be cleared with row 8

        // Place activePiece as a 1x1 'М' at (2, 8) and lock it
        engine.activePiece = com.example.game.ActivePiece(
            shapeType = com.example.game.TetrominoShapes.SINGLE_TILE_SHAPE,
            x = 2,
            y = 8,
            rotation = 0,
            letters = listOf('М')
        )
        engine.lockPiece()

        // Marker 'Ъ' on row 8 must have been cleared by the resonance line clear!
        var foundMarker = false
        for (r in 0 until engine.rows) {
            for (c in 0 until engine.cols) {
                if (engine.board[r][c]?.char == 'Ъ') {
                    foundMarker = true
                }
            }
        }
        assertFalse("Matched row 8 (including marker tile 'Ъ') must be cleared on resonance", foundMarker)
    }

    @Test
    fun arcadeMode_reachesCrosswordRescueFirst_andGameOverOnSecondTopOut() {
        val engine = GameEngine(cols = 10, rows = 15, isRussian = true)
        engine.paceMode = PaceMode.ARCADE

        // Fill top rows to trigger first top-out in Arcade Mode
        engine.board[1][5] = GameCell('А', "#4DD0E1")
        engine.activePiece = com.example.game.ActivePiece(
            shapeType = com.example.game.TetrominoShapes.SINGLE_TILE_SHAPE,
            x = 4,
            y = 1,
            rotation = 0,
            letters = listOf('Б')
        )
        engine.lockPiece()

        // 1st top-out in Arcade enters CROSSWORD rescue mode
        assertEquals(GameMode.CROSSWORD, engine.mode)
        assertTrue(engine.arcadeRescueUsed)

        // 2nd top-out in Arcade transitions to GAME_OVER
        engine.mode = GameMode.TETRIS
        engine.board[1][5] = GameCell('А', "#4DD0E1")
        engine.activePiece = com.example.game.ActivePiece(
            shapeType = com.example.game.TetrominoShapes.SINGLE_TILE_SHAPE,
            x = 3,
            y = 1,
            rotation = 0,
            letters = listOf('В')
        )
        engine.lockPiece()

        assertEquals(GameMode.GAME_OVER, engine.mode)
    }

    @Test
    fun dictionary_normalizesYoAndHasNoDuplicateWords() {
        val ruWords = Dictionary.RU_WORDS.map { it.word }
        assertEquals("RU_WORDS must not contain duplicate words", ruWords.size, ruWords.distinct().size)

        // Both "МЁД" and "МЕД" must match the canonical definition
        assertNotNull(Dictionary.findFirstMatchingDefinition("МЁД", isRussian = true))
        assertNotNull(Dictionary.findFirstMatchingDefinition("МЕД", isRussian = true))
        assertNotNull(Dictionary.findFirstMatchingDefinition("ПЁС", isRussian = true))
        assertNotNull(Dictionary.findFirstMatchingDefinition("ПЕС", isRussian = true))
    }
}
