    package com.example.producto

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.producto.model.GameResult
import com.example.producto3.databinding.ItemToptenBinding

class ToptenAdapter(private val results: List<GameResult>) :
        RecyclerView.Adapter<ToptenAdapter.ViewHolder>() {

        class ViewHolder(val binding: ItemToptenBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemToptenBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val result = results[position]
            holder.binding.playerIdTextView.text = result.playerId
            holder.binding.lootTextView.text = result.loot.toString()
            holder.binding.fechaTextView.text = result.date
        }

        override fun getItemCount() = results.size
}

