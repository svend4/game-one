package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.game.GameViewModel
import com.example.game.GameEngine
import com.example.game.GameStateSerializer
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun testViewModelInitialization() {
    val context = ApplicationProvider.getApplicationContext<android.app.Application>()
    val viewModel = GameViewModel(context)
    assertNotNull(viewModel)
    assertNotNull(viewModel.engine)
  }

  @Test
  fun testMainActivityLaunch() {
    androidx.test.core.app.ActivityScenario.launch(MainActivity::class.java).use { scenario ->
      assertNotNull(scenario)
    }
  }

  @Test
  fun testGameStateSerializationAndDeserialization() {
    val engine = GameEngine(cols = 10, rows = 15, isRussian = true)
    engine.score = 500
    engine.isRussian = false
    
    val json = GameStateSerializer.serialize(engine)
    assertNotNull(json)
    assertTrue(json.contains("score"))
    assertTrue(json.contains("500"))

    val targetEngine = GameEngine(cols = 10, rows = 15, isRussian = true)
    val success = GameStateSerializer.deserialize(json, targetEngine)
    assertTrue(success)
    assertFalse(targetEngine.isRussian)
    org.junit.Assert.assertEquals(500, targetEngine.score)
  }

  @Test
  fun testMalformedJsonDeserializationFailure() {
    val engine = GameEngine(cols = 10, rows = 15, isRussian = true)
    val malformedJson = "{ \"score\": \"invalid\" }"
    val success = GameStateSerializer.deserialize(malformedJson, engine)
    assertFalse(success)
  }
}
