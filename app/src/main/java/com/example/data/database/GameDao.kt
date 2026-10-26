package com.example.data.database

import kotlinx.coroutines.flow.Flow

interface GameDao {
    fun getTopScores(): Flow<List<HighScoreEntity>>
    suspend fun insertHighScore(score: HighScoreEntity)
    suspend fun clearScores()
    suspend fun getSavedGame(): SavedGameEntity?
    suspend fun saveGame(game: SavedGameEntity)
    suspend fun deleteSavedGame()
}
