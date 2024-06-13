package com.example.gamerbox.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.gamerbox.R
import com.example.gamerbox.models.Game

// Manejo en RecyclerViews
class GameAdapter(
    private val onItemClick: (Game) -> Unit
) : RecyclerView.Adapter<GameAdapter.GameViewHolder>() {

    private var gamesList: List<Game> = emptyList()

    // ViewHolder que mantiene las referencias a los elementos de la vista de cada elemento del juego
    class GameViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageViewCover: ImageView = itemView.findViewById(R.id.gameImageView)
        val textViewName: TextView = itemView.findViewById(R.id.gameTitleTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_game, parent, false)
        return GameViewHolder(itemView)
    }

    //  Vincula los datos de un objeto Game a los elementos de la interfaz de usuario
    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        val game = gamesList[position]
        holder.textViewName.text = game.name
        Glide.with(holder.itemView.context)
            .load(game.backgroundImageUrl)
            .into(holder.imageViewCover)

        holder.itemView.setOnClickListener {
            onItemClick(game)
        }
    }

    override fun getItemCount(): Int {
        return gamesList.size
    }

    fun updateData(newGamesList: List<Game>) {
        gamesList = newGamesList
        notifyDataSetChanged()
    }
}

