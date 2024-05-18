package com.example.gamerbox.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.gamerbox.R
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

class ReviewFragment : Fragment() {

    private lateinit var userNameTextView: TextView
    private lateinit var userProfileImageView: ImageView
    private lateinit var reviewDateTextView: TextView
    private lateinit var reviewTextView: TextView
    private lateinit var ratingBar: RatingBar
    private lateinit var gameImageView: ImageView
    private lateinit var gameNameTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_review, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userNameTextView = view.findViewById(R.id.userNameTextView)
        userProfileImageView = view.findViewById(R.id.userImageView)
        reviewDateTextView = view.findViewById(R.id.reviewDateTextView)
        reviewTextView = view.findViewById(R.id.reviewTextTextView)
        ratingBar = view.findViewById(R.id.reviewRatingBar)
        gameImageView = view.findViewById(R.id.gameImageView)
        gameNameTextView = view.findViewById(R.id.gameTitleTextView)

        val reviewText = arguments?.getString("reviewText")
        val rating = arguments?.getFloat("rating")
        val date = arguments?.getLong("date")
        val userId = arguments?.getString("userId")
        val gameId = arguments?.getInt("gameId")

        reviewTextView.text = reviewText
        ratingBar.rating = rating ?: 0f
        reviewDateTextView.text = Date(date ?: 0).toString()

        // Cargar datos del usuario desde Firestore
        userId?.let {
            val userProfileRef = FirebaseFirestore.getInstance().collection("users").document(it)
            userProfileRef.get().addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    val userName = documentSnapshot.getString("username")
                    userNameTextView.text = userName

                    val userProfileImageUrl = documentSnapshot.getString("imageUrl")
                    if (!userProfileImageUrl.isNullOrEmpty()) {
                        Glide.with(userProfileImageView.context)
                            .load(userProfileImageUrl)
                            .into(userProfileImageView)
                    } else {
                        userProfileImageView.setImageResource(R.drawable.ic_profile)
                    }
                }
            }
        }

        // Cargar datos del juego desde Firestore
        gameId?.let {
            val gameRef = FirebaseFirestore.getInstance().collection("games").document(it.toString())
            gameRef.get().addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    val gameName = documentSnapshot.getString("name")
                    gameNameTextView.text = gameName

                    val gameImageUrl = documentSnapshot.getString("imageUrl")
                    if (!gameImageUrl.isNullOrEmpty()) {
                        Glide.with(gameImageView.context)
                            .load(gameImageUrl)
                            .into(gameImageView)
                    } else {
                        gameImageView.setImageResource(R.drawable.game_image)
                    }
                }
            }
        }
    }
}

