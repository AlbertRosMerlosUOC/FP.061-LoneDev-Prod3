package com.example.producto

import com.example.producto.model.GameResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GameResultRepository(private val api: FirebaseApi) {

    suspend fun getTop10(): List<GameResult> = withContext(Dispatchers.IO) {
        try {
            val gameResults = api.getGameResults()
            gameResults.values
                .sortedByDescending { it.loot }
                .take(10)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

}