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

    suspend fun getCustomFamilyWords(): List<CustomFamilyWordEntity> = withContext(Dispatchers.IO) {
        gameDao.getCustomFamilyWords()
    }

    suspend fun insertCustomFamilyWord(entity: CustomFamilyWordEntity) = withContext(Dispatchers.IO) {
        gameDao.insertCustomFamilyWord(entity)
    }

    suspend fun deleteCustomFamilyWord(word: String) = withContext(Dispatchers.IO) {
        gameDao.deleteCustomFamilyWord(word)
    }

    suspend fun getUnlockedWords(): Set<String> = withContext(Dispatchers.IO) {
        gameDao.getUnlockedWords()
    }

    suspend fun insertUnlockedWord(word: String) = withContext(Dispatchers.IO) {
        gameDao.insertUnlockedWord(word)
    }
}
