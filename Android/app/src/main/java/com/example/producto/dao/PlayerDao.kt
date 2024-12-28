package com.example.producto.dao

import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import android.util.Log
import com.example.producto.model.Player

class PlayerDao {

    private val db = Firebase.database.reference.child("Player")

    fun getAllPlayers(callback: (List<Player>) -> Unit) {
        db.get().addOnSuccessListener { snapshot ->
            val players = snapshot.children.mapNotNull { it.getValue(Player::class.java) }
            callback(players)
        }.addOnFailureListener { e ->
            Log.e("Firebase", "Error al obtener jugadores: $e")
        }
    }

    fun findPlayerById(id: String, callback: (Player?) -> Unit) {
        db.orderByChild("id").equalTo(id).get()
            .addOnSuccessListener { snapshot ->
                val player = snapshot.children.firstOrNull()?.getValue(Player::class.java)
                callback(player)
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Error al buscar jugador: $e")
                callback(null)
            }
    }

    fun findPlayerByName(name: String, callback: (Player?) -> Unit) {
        db.orderByChild("name").equalTo(name).get()
            .addOnSuccessListener { snapshot ->
                val player = snapshot.children.firstOrNull()?.getValue(Player::class.java)
                callback(player)
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Error al buscar jugador: $e")
                callback(null)
            }
    }

    fun insertOrUpdatePlayer(player: Player) {
        val playerId = player.id.ifEmpty { db.push().key!! }
        db.child(playerId).setValue(player.copy(id = playerId))
            .addOnSuccessListener {
                Log.d("Firebase", "Jugador insertado/actualizado correctamente.")
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Error al insertar/actualizar jugador: $e")
            }
    }

    fun updatePlayer(player: Player) {
        val encodedId = player.id.replace(".", ",")

        db.child(encodedId).setValue(player)
            .addOnSuccessListener {
                Log.d("Firebase", "Jugador actualizado correctamente.")
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Error al actualizar jugador: $e")
            }
    }

    fun deletePlayer(playerId: String) {
        db.child(playerId).removeValue()
            .addOnSuccessListener {
                Log.d("Firebase", "Jugador eliminado correctamente.")
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Error al eliminar jugador: $e")
            }
    }
}
