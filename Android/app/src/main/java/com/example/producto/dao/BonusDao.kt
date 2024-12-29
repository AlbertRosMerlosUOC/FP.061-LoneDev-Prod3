package com.example.producto.dao

import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import android.util.Log
import com.example.producto.model.Bonus
import com.example.producto.model.Player

class BonusDao {

    private val db = Firebase.database.reference.child("Bonus")

    fun getBonus(callback: (Bonus?) -> Unit) {
        db.get()
            .addOnSuccessListener { snapshot ->
                val bonus = snapshot.children.firstOrNull()?.getValue(Bonus::class.java)
                callback(bonus)
            }
            .addOnFailureListener { e ->
                callback(null)
            }
    }

    fun incrementBonus() {
        val bonusRef = Firebase.database.reference.child("Bonus").child("bonus")
        bonusRef.get().addOnSuccessListener { snapshot ->
            val currentAmount = snapshot.getValue(Int::class.java) ?: 0
            bonusRef.setValue(currentAmount + 10)
        }
    }

    fun resetBonus() {
        val bonusRef = Firebase.database.reference.child("Bonus").child("bonus")
        bonusRef.get().addOnSuccessListener {
            bonusRef.setValue(0)
        }
    }
}
