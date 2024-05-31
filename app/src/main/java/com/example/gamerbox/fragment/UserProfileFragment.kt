package com.example.gamerbox.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.example.gamerbox.R
import com.example.gamerbox.adapter.FavoriteGamesAdapter
import com.example.gamerbox.models.Game
import com.google.firebase.firestore.FirebaseFirestore

class UserProfileFragment : Fragment() {

    private lateinit var userProfileImageView: ImageView
    private lateinit var userNameTextView: TextView
    private lateinit var favoriteGamesRecyclerView: RecyclerView
    private lateinit var favoriteGamesAdapter: FavoriteGamesAdapter
    private lateinit var favoriteGames: MutableList<Game>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_user_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userProfileImageView = view.findViewById(R.id.userProfileImage)
        userNameTextView = view.findViewById(R.id.usernameProfileTextView)
        favoriteGamesRecyclerView = view.findViewById(R.id.favoriteGamesRecyclerView)

        favoriteGames = mutableListOf()
        favoriteGamesAdapter = FavoriteGamesAdapter(favoriteGames)
        favoriteGamesRecyclerView.layoutManager = LinearLayoutManager(context)
        favoriteGamesRecyclerView.adapter = favoriteGamesAdapter

        val userId = arguments?.getString("userId")
        val userProfileImageUrl = arguments?.getString("imageUrl")
        val userProfileUsername = arguments?.getString("username")

        userProfileUsername?.let {
            userNameTextView.text = it
        }

        userProfileImageUrl?.let { imageUrl ->
            Glide.with(userProfileImageView.context)
                .load(imageUrl)
                .apply(RequestOptions.bitmapTransform(CircleCrop()))
                .into(userProfileImageView)
        } ?: run {
            userProfileImageView.setImageResource(R.drawable.ic_profile)
        }

        userId?.let {
            FirebaseFirestore.getInstance().collection("users").document(it)
                .collection("favoriteGames")
                .get()
                .addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot) {
                        val game = document.toObject(Game::class.java)
                        favoriteGames.add(game)
                        println(game.id)
                    }
                    favoriteGamesAdapter.notifyDataSetChanged()
                }
        }
    }
}
