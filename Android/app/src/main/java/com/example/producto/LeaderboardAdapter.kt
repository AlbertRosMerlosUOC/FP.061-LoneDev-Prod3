package com.example.producto

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.producto3.databinding.ItemLeaderboardBinding
import com.example.producto.model.Player

class LeaderboardAdapter(private val jugadores: List<Player>) :
    RecyclerView.Adapter<LeaderboardAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemLeaderboardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLeaderboardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val jugador = jugadores[position]
        holder.binding.nombreTextView.text = jugador.name
        holder.binding.monedasTextView.text = "${jugador.coins}"
    }

    override fun getItemCount(): Int = jugadores.size
}
