package com.example.gamerbox.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.gamerbox.R
import com.example.gamerbox.adapter.ReviewAdapter
import com.example.gamerbox.models.Game
import com.example.gamerbox.models.Review
import com.example.gamerbox.network.RawgRepository
import com.example.gamerbox.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserProfileFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var userProfileImageView: ImageView
    private lateinit var userNameTextView: TextView
    private lateinit var favoriteGameSlot1: ImageView
    private lateinit var favoriteGameSlot2: ImageView
    private lateinit var favoriteGameSlot3: ImageView
    private lateinit var favoriteGameSlot4: ImageView

    private lateinit var userProfileReviewRecyclerView: RecyclerView
    private lateinit var reviewAdapter: ReviewAdapter
    private lateinit var moreUserReviewsTextView: TextView

    private lateinit var userProfileBackArrow: ImageButton


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
        favoriteGameSlot1 = view.findViewById(R.id.favoriteUserGameSlot1)
        favoriteGameSlot2 = view.findViewById(R.id.favoriteUserGameSlot2)
        favoriteGameSlot3 = view.findViewById(R.id.favoriteUserGameSlot3)
        favoriteGameSlot4 = view.findViewById(R.id.favoriteUserGameSlot4)
        userProfileBackArrow = view.findViewById(R.id.userProfileBackArrowImage)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()


        val userId = arguments?.getString("userId")
        val userProfileImageUrl = arguments?.getString("imageUrl")
        val userProfileUsername = arguments?.getString("username")

        userProfileUsername?.let {
            userNameTextView.text = it
        }

        userProfileImageUrl?.let { imageUrl ->
            Glide.with(userProfileImageView.context)
                .load(imageUrl)
                .apply(RequestOptions.circleCropTransform())
                .into(userProfileImageView)
        } ?: run {
            userProfileImageView.setImageResource(R.drawable.ic_profile)
        }

        userId?.let {
            loadFavoriteGames(it)
        }

        userProfileBackArrow.setOnClickListener {
            findNavController().popBackStack()
        }

        if (userId == auth.currentUser?.uid) {
            navigateToProfileFragment()
        }

        setupReviewsRecyclerView(view)
        loadUserReviews()
    }

    private fun loadFavoriteGames(userId: String) {
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val favoriteGamesIds = document.get("favoriteGames") as? List<Int> ?: emptyList()
                    if (favoriteGamesIds.isNotEmpty()) {
                        lifecycleScope.launch {
                            val favoriteGames = favoriteGamesIds.mapNotNull { findGameById(it) }
                            withContext(Dispatchers.Main) {
                                setFavoriteGameSlots(favoriteGames)
                            }
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                println(e.message)
            }
    }

    private fun setFavoriteGameSlots(favoriteGames: List<Game>) {
        val slots = listOf(favoriteGameSlot1, favoriteGameSlot2, favoriteGameSlot3, favoriteGameSlot4)

        favoriteGames.forEachIndexed { index, game ->
            if (index < slots.size) {
                Glide.with(this)
                    .load(game.backgroundImageUrl)
                    .into(slots[index])
            }
        }
    }

    private fun setupReviewsRecyclerView(view: View) {
        userProfileReviewRecyclerView = view.findViewById(R.id.userProfileReviewRecyclerView)
        reviewAdapter = ReviewAdapter(emptyList(), "UserProfileFragment") { review ->
            onLikeClicked(review)
        }
        userProfileReviewRecyclerView.adapter = reviewAdapter
        userProfileReviewRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        moreUserReviewsTextView = view.findViewById(R.id.moreUserReviewsTextView)
        moreUserReviewsTextView.setOnClickListener {
            findNavController().navigate(R.id.action_userProfile_to_userReviews)
        }
    }

    private fun loadUserReviews() {
        val userId = arguments?.getString("userId") ?: return

        FirebaseFirestore.getInstance().collection("reviews")
            .whereEqualTo("userId", userId)
            .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val reviewList = mutableListOf<Review>()
                for (document in documents) {
                    val review = document.toObject(Review::class.java).copy(id = document.id)
                    reviewList.add(review)
                }
                if (reviewList.size > 3) {
                    reviewAdapter.updateData(reviewList.take(3))
                    moreUserReviewsTextView.visibility = View.VISIBLE
                } else {
                    reviewAdapter.updateData(reviewList)
                    moreUserReviewsTextView.visibility = View.GONE
                }
            }
    }

    private fun onLikeClicked(review: Review) {
        val currentUserId = auth.currentUser?.uid ?: return

        val reviewRef = db.collection("reviews").document(review.id)

        if (currentUserId in review.likes) {
            review.likes.remove(currentUserId)
        } else {
            review.likes.add(currentUserId)
        }

        reviewRef.update("likes", review.likes)
            .addOnSuccessListener {
                reviewAdapter.updateReview(review)
            }
            .addOnFailureListener { e ->
                println(e.message)
            }
    }

    private suspend fun findGameById(gameId: Int): Game? {
        val rawgService = RetrofitService.create()
        val rawgRepository = RawgRepository(rawgService)
        val gameDetails = rawgRepository.getGameDetails(gameId, Constants.API_KEY)
        return gameDetails?.let {
            Game(
                id = it.id,
                name = it.name,
                backgroundImageUrl = it.backgroundImageUrl,
                releaseDate = it.releaseDate
            )
        }
    }

    private fun navigateToProfileFragment() {
        findNavController().navigate(R.id.action_userProfile_to_profileFragment)
    }
}