package com.example.producto.dao

import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import android.util.Log
import com.example.producto.model.Bonus
import com.example.producto.model.Player

class BonusDao {

    private val db = Firebase.database.reference.child("Bonus")

    fun getBonus(callback: (Bonus?) -> Unit) {
        db.child("bonus").get()
            .addOnSuccessListener { snapshot ->
                val bonusValue = snapshot.getValue(Int::class.java)
                if (bonusValue != null) {
                    val bonus = Bonus(bonusValue)
                    callback(bonus)
                } else {
                    callback(null)
                }
            }
            .addOnFailureListener { e ->
                callback(null)
            }
    }

    fun getBonusValue(callback: (Int?) -> Unit) {
        db.child("bonus").get()
            .addOnSuccessListener { snapshot ->
                val bonusValue = snapshot.getValue(Int::class.java)
                if (bonusValue != null) {
                    callback(bonusValue)
                } else {
                    callback(null)
                }
            }
            .addOnFailureListener { e ->
                callback(null)
            }
    }

    fun incrementBonus(callback: (Int) -> Unit) {
        val bonusRef = Firebase.database.reference.child("Bonus").child("bonus")
        bonusRef.get().addOnSuccessListener { snapshot ->
            val currentAmount = snapshot.getValue(Int::class.java) ?: 0
            bonusRef.setValue(currentAmount + 10)
            callback(currentAmount + 10)
        }
    }

    fun resetBonus() {
        val bonusRef = Firebase.database.reference.child("Bonus").child("bonus")
        bonusRef.get().addOnSuccessListener {
            bonusRef.setValue(0)
        }
    }
}
