package com.example.gamerbox.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gamerbox.R
import com.example.gamerbox.adapter.ReviewAdapter
import com.example.gamerbox.models.Review
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AllReviewsFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var allReviewsRecyclerView: RecyclerView
    private lateinit var reviewAdapter: ReviewAdapter
    private lateinit var allReviewBackArrow: ImageButton
    private var gameId: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        return inflater.inflate(R.layout.fragment_all_reviews, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        allReviewsRecyclerView = view.findViewById(R.id.allReviewsRecyclerView)
        allReviewBackArrow = view.findViewById(R.id.allReviewBackArrowImage)
        reviewAdapter = ReviewAdapter(emptyList(), "AllReviewsFragment") { review ->
            onLikeClicked(review)
        }
        allReviewsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        allReviewsRecyclerView.adapter = reviewAdapter

        gameId = arguments?.getInt("gameId") ?: -1
        if (gameId != -1) {
            loadAllReviews()
        }

        allReviewBackArrow.setOnClickListener {
            findNavController().popBackStack()
        }

    }

    private fun loadAllReviews() {
        FirebaseFirestore.getInstance().collection("reviews")
            .whereEqualTo("gameId", gameId)
            .get()
            .addOnSuccessListener { documents ->
                val reviewList = mutableListOf<Review>()
                for (document in documents) {
                    val review = document.toObject(Review::class.java).copy(id = document.id)
                    reviewList.add(review)
                }
                reviewList.sortByDescending { it.likes.size }

                reviewAdapter.updateData(reviewList)
            }
            .addOnFailureListener { e ->
                println(e.message)
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
