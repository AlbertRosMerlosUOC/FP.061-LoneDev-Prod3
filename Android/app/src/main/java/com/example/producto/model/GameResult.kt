package com.example.producto.model

data class GameResult(
    val id: String = "",
    val playerId: String = "",
    val loot: Int = 0,
    val result1: String = "",
    val result2: String = "",
    val result3: String = "",
    val date: String = "",
    val location: String? = null
)

