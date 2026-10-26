package com.example.data.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GameDatabase private constructor(context: Context) :
    SQLiteOpenHelper(context.applicationContext, "tetricross_database", null, 4) {

    private val scoresFlow = MutableStateFlow<List<HighScoreEntity>>(emptyList())
    private val dbScope = CoroutineScope(Dispatchers.IO)

    private val daoImpl = object : GameDao {
        override fun getTopScores(): Flow<List<HighScoreEntity>> = scoresFlow.asStateFlow()

        override suspend fun insertHighScore(score: HighScoreEntity) {
            val db = writableDatabase
            val values = ContentValues().apply {
                put("playerName", score.playerName)
                put("score", score.score)
                put("date", score.date)
            }
            db.insertWithOnConflict("high_scores", null, values, SQLiteDatabase.CONFLICT_REPLACE)
            refreshScores()
        }

        override suspend fun clearScores() {
            val db = writableDatabase
            db.delete("high_scores", null, null)
            refreshScores()
        }

        override suspend fun getSavedGame(): SavedGameEntity? {
            val db = readableDatabase
            val cursor = db.query(
                "saved_game",
                arrayOf("id", "gameJson", "lastSaved"),
                "id = 1",
                null,
                null,
                null,
                null
            )
            cursor.use {
                if (it.moveToFirst()) {
                    val id = it.getInt(0)
                    val json = it.getString(1) ?: return null
                    val lastSaved = it.getLong(2)
                    return SavedGameEntity(id = id, gameJson = json, lastSaved = lastSaved)
                }
            }
            return null
        }

        override suspend fun saveGame(game: SavedGameEntity) {
            val db = writableDatabase
            val values = ContentValues().apply {
                put("id", 1)
                put("gameJson", game.gameJson)
                put("lastSaved", game.lastSaved)
            }
            db.insertWithOnConflict("saved_game", null, values, SQLiteDatabase.CONFLICT_REPLACE)
        }

        override suspend fun deleteSavedGame() {
            val db = writableDatabase
            db.delete("saved_game", "id = 1", null)
        }

        override suspend fun getCustomFamilyWords(): List<CustomFamilyWordEntity> {
            val list = mutableListOf<CustomFamilyWordEntity>()
            try {
                val db = readableDatabase
                val cursor = db.query(
                    "custom_family_words",
                    arrayOf("word", "clue", "funFact", "isRussian"),
                    null,
                    null,
                    null,
                    null,
                    null
                )
                cursor.use {
                    while (it.moveToNext()) {
                        list.add(
                            CustomFamilyWordEntity(
                                word = it.getString(0) ?: "",
                                clue = it.getString(1) ?: "",
                                funFact = it.getString(2) ?: "",
                                isRussian = it.getInt(3) != 0
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return list
        }

        override suspend fun insertCustomFamilyWord(entity: CustomFamilyWordEntity) {
            try {
                val db = writableDatabase
                val values = ContentValues().apply {
                    put("word", entity.word)
                    put("clue", entity.clue)
                    put("funFact", entity.funFact)
                    put("isRussian", if (entity.isRussian) 1 else 0)
                }
                db.insertWithOnConflict("custom_family_words", null, values, SQLiteDatabase.CONFLICT_REPLACE)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override suspend fun getUnlockedWords(): Set<String> {
            val set = mutableSetOf<String>()
            try {
                val db = readableDatabase
                val cursor = db.query(
                    "unlocked_words",
                    arrayOf("word"),
                    null,
                    null,
                    null,
                    null,
                    "unlockedAt DESC"
                )
                cursor.use {
                    while (it.moveToNext()) {
                        val w = it.getString(0)
                        if (!w.isNullOrBlank()) set.add(w)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return set
        }

        override suspend fun insertUnlockedWord(word: String) {
            try {
                val db = writableDatabase
                val values = ContentValues().apply {
                    put("word", word.uppercase().trim())
                    put("unlockedAt", System.currentTimeMillis())
                }
                db.insertWithOnConflict("unlocked_words", null, values, SQLiteDatabase.CONFLICT_REPLACE)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    init {
        dbScope.launch {
            refreshScores()
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS high_scores (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                playerName TEXT NOT NULL,
                score INTEGER NOT NULL,
                date INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS saved_game (
                id INTEGER PRIMARY KEY NOT NULL,
                gameJson TEXT NOT NULL,
                lastSaved INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS custom_family_words (
                word TEXT PRIMARY KEY NOT NULL,
                clue TEXT NOT NULL,
                funFact TEXT NOT NULL,
                isRussian INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS unlocked_words (
                word TEXT PRIMARY KEY NOT NULL,
                unlockedAt INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onCreate(db)
    }

    private fun refreshScores() {
        try {
            val db = readableDatabase
            val list = mutableListOf<HighScoreEntity>()
            val cursor = db.query(
                "high_scores",
                arrayOf("id", "playerName", "score", "date"),
                null,
                null,
                null,
                null,
                "score DESC",
                "20"
            )
            cursor.use {
                while (it.moveToNext()) {
                    list.add(
                        HighScoreEntity(
                            id = it.getInt(0),
                            playerName = it.getString(1) ?: "Player",
                            score = it.getInt(2),
                            date = it.getLong(3)
                        )
                    )
                }
            }
            scoresFlow.value = list
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun gameDao(): GameDao = daoImpl

    companion object {
        @Volatile
        private var INSTANCE: GameDatabase? = null

        fun getDatabase(context: Context): GameDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = GameDatabase(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
