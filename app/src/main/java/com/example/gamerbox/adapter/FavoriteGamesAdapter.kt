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

class FavoriteGamesAdapter(private val favoriteGames: List<Game>) :
    RecyclerView.Adapter<FavoriteGamesAdapter.FavoriteGamesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteGamesViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_favorite_game, parent, false)
        return FavoriteGamesViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoriteGamesViewHolder, position: Int) {
        val game = favoriteGames[position]
        holder.bind(game)
    }

    override fun getItemCount(): Int = favoriteGames.size

    class FavoriteGamesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val gameImageView: ImageView = itemView.findViewById(R.id.gameImageView)
        private val gameNameTextView: TextView = itemView.findViewById(R.id.gameNameTextView)

        fun bind(game: Game) {
            gameNameTextView.text = game.name
            Glide.with(gameImageView.context)
                .load(game.backgroundImageUrl)
                .into(gameImageView)
        }
    }
}
