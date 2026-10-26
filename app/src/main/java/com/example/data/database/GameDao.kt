package com.example.data.database

import kotlinx.coroutines.flow.Flow

data class CustomFamilyWordEntity(
    val word: String,
    val clue: String,
    val funFact: String,
    val isRussian: Boolean
)

interface GameDao {
    fun getTopScores(): Flow<List<HighScoreEntity>>
    suspend fun insertHighScore(score: HighScoreEntity)
    suspend fun clearScores()
    suspend fun getSavedGame(): SavedGameEntity?
    suspend fun saveGame(game: SavedGameEntity)
    suspend fun deleteSavedGame()
    suspend fun getCustomFamilyWords(): List<CustomFamilyWordEntity>
    suspend fun insertCustomFamilyWord(entity: CustomFamilyWordEntity)
    suspend fun deleteCustomFamilyWord(word: String)
    suspend fun getUnlockedWords(): Set<String>
    suspend fun insertUnlockedWord(word: String)
}
