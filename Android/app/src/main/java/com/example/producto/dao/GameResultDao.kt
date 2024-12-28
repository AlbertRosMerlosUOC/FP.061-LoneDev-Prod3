package com.example.producto.dao

import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import android.util.Log
import com.example.producto.model.GameResult
import kotlinx.coroutines.suspendCancellableCoroutine
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class GameResultDao {

    private val db = Firebase.database.reference.child("GameResult")

    fun getHistoryByPlayer(playerId: String, callback: (List<GameResult>) -> Unit) {
        db.orderByChild("playerId").equalTo(playerId).get()
            .addOnSuccessListener { snapshot ->
                val gameResults = snapshot.children.mapNotNull { it.getValue(GameResult::class.java) }
                callback(gameResults)
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Error al obtener historial de partidas: $e")
            }
    }

    suspend fun getHistoryByPlayerSuspend(playerId: String): List<GameResult> = suspendCancellableCoroutine { continuation ->
        getHistoryByPlayer(playerId) { gameResults ->
            val sortedResults = gameResults.sortedByDescending { gameResult ->
                try {
                    SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).parse(gameResult.date)
                } catch (e: Exception) {
                    Log.e("Parsing Error", "Error parsing date for gameResult: $gameResult", e)
                    null
                }
            }
            continuation.resumeWith(Result.success(sortedResults))
        }
    }

    fun insertGame(gameResult: GameResult) {
        val gameId = db.push().key!!
        db.child(gameId).setValue(gameResult.copy(id = gameId))
            .addOnSuccessListener {
                Log.d("Firebase", "Resultado de juego insertado correctamente.")
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Error al insertar resultado de juego: $e")
            }
    }
}

