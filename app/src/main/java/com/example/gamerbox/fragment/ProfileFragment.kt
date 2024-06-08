package com.example.gamerbox.fragment

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.gamerbox.R
import com.example.gamerbox.activity.AuthActivity
import com.example.gamerbox.adapter.ReviewAdapter
import com.example.gamerbox.models.Game
import com.example.gamerbox.models.Review
import com.example.gamerbox.network.RawgRepository
import com.example.gamerbox.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var profileImage: ImageView
    private lateinit var usernameText: TextView
    private lateinit var reviewRecyclerView: RecyclerView
    private lateinit var reviewAdapter: ReviewAdapter
    private lateinit var moreReviewsTextView: TextView

    private lateinit var favoriteGameSlot1: ImageView
    private lateinit var favoriteGameSlot2: ImageView
    private lateinit var favoriteGameSlot3: ImageView
    private lateinit var favoriteGameSlot4: ImageView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        auth = FirebaseAuth.getInstance()
        db = Firebase.firestore

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_edit_profile -> {
                    findNavController().navigate(R.id.action_profile_to_edit_profile)
                    true
                }

                R.id.action_logout -> {
                    showLogoutConfirmationDialog()
                    true
                }

                else -> false
            }
        }

        profileImage = view.findViewById(R.id.profileImage)
        usernameText = view.findViewById(R.id.usernameText)

        favoriteGameSlot1 = view.findViewById(R.id.favoriteGameSlot1)
        favoriteGameSlot2 = view.findViewById(R.id.favoriteGameSlot2)
        favoriteGameSlot3 = view.findViewById(R.id.favoriteGameSlot3)
        favoriteGameSlot4 = view.findViewById(R.id.favoriteGameSlot4)

        showUserData()
        setupFavoriteGamesButton(view)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        moreReviewsTextView = view.findViewById(R.id.moreReviewsTextView)
        reviewRecyclerView = view.findViewById(R.id.profileReviewRecyclerView)

        reviewAdapter = ReviewAdapter(emptyList(), "ProfileFragment") { review ->
            onLikeClicked(review)
        }
        reviewRecyclerView.adapter = reviewAdapter
        reviewRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        loadUserReviews()
        loadFavoriteGames()

        moreReviewsTextView.setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_userReviews)
        }

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>("favoritesUpdated")
            ?.observe(viewLifecycleOwner) { updated ->
                if (updated) {
                    loadFavoriteGames()
                }
            }
    }

    private fun showUserData() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val username = document.getString("username")
                        val imageUrl = document.getString("imageUrl")

                        usernameText.text = username

                        if (!imageUrl.isNullOrEmpty()) {
                            Glide.with(this)
                                .load(imageUrl)
                                .apply(RequestOptions.circleCropTransform())
                                .into(profileImage)
                        } else {
                            Glide.with(this)
                                .load(R.drawable.ic_profile)
                                .apply(RequestOptions.circleCropTransform())
                                .into(profileImage)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    println(e.message)
                }
        }
    }

    private fun showLogoutConfirmationDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Cerrar sesión")
        builder.setMessage("¿Estás seguro de que quieres cerrar sesión?")
        builder.setPositiveButton("Aceptar") { _, _ ->
            auth.signOut()
            showLogin()
        }
        builder.setNegativeButton("Cancelar", null)
        val dialog = builder.create()
        dialog.show()
    }

    private fun showLogin() {
        val loginIntent = Intent(requireContext(), AuthActivity::class.java)
        startActivity(loginIntent)
    }

    private fun loadUserReviews() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("reviews")
            .whereEqualTo("userId", userId)
            .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val reviewList = mutableListOf<Review>()
                for (document in documents) {
                    val review = document.toObject(Review::class.java).copy(id = document.id)
                    reviewList.add(review)
                }
                if (reviewList.isNotEmpty()) {
                    val recentReviews = reviewList.take(3)
                    reviewAdapter = ReviewAdapter(recentReviews, "ProfileFragment") { review ->
                        onLikeClicked(review)
                    }
                    reviewRecyclerView.adapter = reviewAdapter

                    if (reviewList.size > 3) {
                        moreReviewsTextView.visibility = View.VISIBLE
                    }
                }
            }
            .addOnFailureListener { e ->
                println(e.message)
            }
    }

    private fun loadFavoriteGames() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val favoriteGamesIds =
                        document.get("favoriteGames") as? List<Int> ?: emptyList()
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

                slots[index].setOnClickListener {
                    val bundle = Bundle().apply {
                        putInt("gameId", game.id)
                    }
                    findNavController().navigate(R.id.action_profileFragment_to_gameFragment, bundle)
                }
            }
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

    private fun setupFavoriteGamesButton(view: View) {
        val editFavoriteGamesButton = view.findViewById<TextView>(R.id.favoriteGamesButton)
        editFavoriteGamesButton.setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_favorite)
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
}
