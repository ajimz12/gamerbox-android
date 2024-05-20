package com.example.gamerbox.fragment

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.example.gamerbox.R
import com.example.gamerbox.models.GameDetails
import com.example.gamerbox.network.RawgRepository
import com.example.gamerbox.network.RetrofitService
import com.example.gamerbox.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReviewFragment : Fragment() {

    private lateinit var userNameTextView: TextView
    private lateinit var userProfileImageView: ImageView
    private lateinit var reviewDateTextView: TextView
    private lateinit var reviewTextView: TextView
    private lateinit var ratingBar: RatingBar
    private lateinit var gameImageView: ImageView
    private lateinit var gameNameTextView: TextView
    private var gameId: Int = -1

    private lateinit var rawgRepository: RawgRepository

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
        gameId = arguments?.getInt("gameId") ?: -1

        reviewTextView.text = reviewText
        ratingBar.rating = rating ?: 0f

        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        reviewDateTextView.text = date?.let { dateFormat.format(Date(it)) } ?: ""

        val rawgService = RetrofitService.create()
        rawgRepository = RawgRepository(rawgService)

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
