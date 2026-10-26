package com.example.data.database

data class HighScoreEntity(
    val id: Int = 0,
    val playerName: String,
    val score: Int,
    val date: Long = System.currentTimeMillis()
)
