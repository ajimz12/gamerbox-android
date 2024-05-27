package com.example.gamerbox.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.example.gamerbox.R
import com.example.gamerbox.models.GameDetails
import com.example.gamerbox.network.RawgRepository
import com.example.gamerbox.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReviewFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var userNameTextView: TextView
    private lateinit var userProfileImageView: ImageView
    private lateinit var reviewDateTextView: TextView
    private lateinit var reviewTextView: TextView
    private lateinit var ratingBar: RatingBar
    private lateinit var gameImageView: ImageView
    private lateinit var gameNameTextView: TextView
    private lateinit var backArrowImage: ImageView
    private lateinit var reviewLikeButton: ImageView
    private lateinit var reviewLikeCountTextView: TextView

    private var gameId: Int = -1
    private var reviewId: String = ""
    private lateinit var rawgRepository: RawgRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        auth = FirebaseAuth.getInstance()
        db = Firebase.firestore

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
        backArrowImage = view.findViewById(R.id.reviewBackArrowImage)
        reviewLikeButton = view.findViewById(R.id.reviewLikeButton)
        reviewLikeCountTextView = view.findViewById(R.id.reviewLikeCountTextView)

        val reviewText = arguments?.getString("reviewText")
        val rating = arguments?.getFloat("rating")
        val date = arguments?.getLong("date")
        val userId = arguments?.getString("userId")
        gameId = arguments?.getInt("gameId") ?: -1
        reviewId = arguments?.getString("reviewId") ?: ""

        if (reviewId.isEmpty()) {
            findNavController().popBackStack()
            return
        }

        reviewTextView.text = reviewText
        ratingBar.rating = rating ?: 0f

        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        reviewDateTextView.text = date?.let { dateFormat.format(Date(it)) } ?: ""

        val rawgService = RetrofitService.create()
        rawgRepository = RawgRepository(rawgService)

        backArrowImage.setOnClickListener {
            findNavController().popBackStack()
        }

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
                            .apply(RequestOptions.bitmapTransform(CircleCrop()))
                            .into(userProfileImageView)
                    } else {
                        userProfileImageView.setImageResource(R.drawable.ic_profile)
                    }
                }
            }
        }

        if (gameId != -1) {
            lifecycleScope.launch {
                try {
                    val gameDetails = withContext(Dispatchers.IO) {
                        rawgRepository.getGameDetails(gameId, Constants.API_KEY)
                    }
                    if (gameDetails != null) {
                        updateUI(gameDetails)
                    }
                } catch (e: Exception) {
                    println(e.message)
                }
            }
        }

        // Cargar datos de me gusta desde Firestore
        db.collection("reviews").document(reviewId)
            .addSnapshotListener { documentSnapshot, e ->
                if (e != null) {
                    return@addSnapshotListener
                }

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    val likes = documentSnapshot.get("likes") as? List<*>
                    val likeCount = likes?.size ?: 0
                    reviewLikeCountTextView.text = likeCount.toString()
                    val currentUserId = auth.currentUser?.uid
                    if (currentUserId != null && likes?.contains(currentUserId) == true) {
                        reviewLikeButton.setImageResource(R.drawable.ic_heart_selected)
                    } else {
                        reviewLikeButton.setImageResource(R.drawable.ic_heart)
                    }
                }
            }

        // Agregar listener de me gusta
        reviewLikeButton.setOnClickListener {
            updateLikeStatus()
        }
    }

    private fun updateLikeStatus() {
        val reviewRef = db.collection("reviews").document(reviewId)
        val currentUserId = auth.currentUser?.uid

        if (currentUserId != null) {
            db.runTransaction { transaction ->
                val snapshot = transaction.get(reviewRef)
                val likes = snapshot.get("likes") as? List<*>
                val newLikes = likes?.toMutableList() ?: mutableListOf()

                val userHasLiked = newLikes.contains(currentUserId)
                if (userHasLiked) {
                    newLikes.remove(currentUserId)
                } else {
                    newLikes.add(currentUserId)
                }

                transaction.update(reviewRef, "likes", newLikes)
                !userHasLiked
            }.addOnSuccessListener { hasLiked ->
                reviewLikeCountTextView.text = (reviewLikeCountTextView.text.toString().toInt() + if (hasLiked) 1 else -1).toString()
                if (hasLiked) {
                    reviewLikeButton.setImageResource(R.drawable.ic_heart_selected)
                } else {
                    reviewLikeButton.setImageResource(R.drawable.ic_heart)
                }
            }.addOnFailureListener { e ->
                println(e.message)
            }
        }
    }

    private fun updateUI(gameDetails: GameDetails) {
        // Actualizar la interfaz con los detalles del juego
        gameNameTextView.text = gameDetails.name

        // Cargar la imagen del juego
        Glide.with(requireContext())
            .load(gameDetails.backgroundImageUrl)
            .into(gameImageView)
    }
}
