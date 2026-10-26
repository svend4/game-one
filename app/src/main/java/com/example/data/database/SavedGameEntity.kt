package com.example.data.database

data class SavedGameEntity(
    val id: Int = 1, // Singleton saved game
    val gameJson: String,
    val lastSaved: Long = System.currentTimeMillis()
)
