package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.game.Dictionary
import com.example.game.GameCell
import com.example.game.GameEngine
import com.example.game.GameStateSerializer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun gameStateSerializer_roundTripPreservesBoardStencilAndActivePieceWithoutDuplication() {
        val original = GameEngine(cols = 10, rows = 15, isRussian = true)
        val wordDef = Dictionary.findFirstMatchingDefinition("ПИРОГ", isRussian = true)!!
        original.startSpecificWordOnBoard(wordDef)
        original.score = 420
        original.comboCount = 3
        original.wordsClearedCount = 2
        original.freeHintCharges = 4
        original.revealStencilSlotLetterHint()

        // Place a custom non-anchor block at (0, 14)
        original.board[14][0] = GameCell('Я', "#4DD0E1", isAnchor = false)

        val expectedAnchorCount = (0 until original.rows).sumOf { r ->
            (0 until original.cols).count { c -> original.board[r][c]?.isAnchor == true }
        }
        val expectedSlots = original.stencilSlots.toList()
        val expectedPiece = original.activePiece

        val json = GameStateSerializer.serialize(original)

        val restored = GameEngine(cols = 10, rows = 15, isRussian = true)
        assertTrue(GameStateSerializer.deserialize(json, restored))

        assertEquals(420, restored.score)
        assertEquals(3, restored.comboCount)
        assertEquals(2, restored.wordsClearedCount)
        assertEquals(3, restored.freeHintCharges)
        assertEquals("ПИРОГ", restored.questWordDefinition?.word)
        assertEquals(expectedSlots, restored.stencilSlots)
        assertEquals(expectedPiece, restored.activePiece)
        assertEquals('Я', restored.board[14][0]?.char)

        val restoredAnchorCount = (0 until restored.rows).sumOf { r ->
            (0 until restored.cols).count { c -> restored.board[r][c]?.isAnchor == true }
        }
        assertEquals("Deserialization must not duplicate or orphan anchor tiles", expectedAnchorCount, restoredAnchorCount)
    }

    @Test
    fun menuToGameAndFinishScoreFlow_worksCorrectly() {
        composeTestRule.onNodeWithTag("play_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("play_button").performClick()

        // Verify game controls and new Finish & Save Score button are displayed
        composeTestRule.onNodeWithTag("score_text").assertIsDisplayed()
        composeTestRule.onNodeWithTag("stencil_hint_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("top_finish_save_score_button").assertIsDisplayed()

        // Clicking Finish & Save Score opens the Game Over High Score dialog
        composeTestRule.onNodeWithTag("top_finish_save_score_button").performClick()
        composeTestRule.onNodeWithTag("save_score_button").assertIsDisplayed()
    }
}
