package com.example.producto

import com.example.producto.model.GameResult
import retrofit2.http.GET

interface FirebaseApi {
    @GET("GameResult.json")
    suspend fun getGameResults(): Map<String, GameResult>
}