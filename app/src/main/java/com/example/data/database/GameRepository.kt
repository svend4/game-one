package com.example.data.database

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class GameRepository(private val gameDao: GameDao) {
    val topScores: Flow<List<HighScoreEntity>> = gameDao.getTopScores()

    suspend fun insertHighScore(playerName: String, score: Int) = withContext(Dispatchers.IO) {
        gameDao.insertHighScore(HighScoreEntity(playerName = playerName, score = score))
    }

    suspend fun clearLeaderboard() = withContext(Dispatchers.IO) {
        gameDao.clearScores()
    }

    suspend fun loadSavedGame(): String? = withContext(Dispatchers.IO) {
        gameDao.getSavedGame()?.gameJson
    }

    suspend fun saveGame(json: String) = withContext(Dispatchers.IO) {
        gameDao.saveGame(SavedGameEntity(gameJson = json))
    }

    suspend fun deleteSavedGame() = withContext(Dispatchers.IO) {
        gameDao.deleteSavedGame()
    }
}
