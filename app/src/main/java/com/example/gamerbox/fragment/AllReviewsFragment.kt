package com.example.gamerbox.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gamerbox.R
import com.example.gamerbox.adapter.ReviewAdapter
import com.example.gamerbox.models.Review
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AllReviewsFragment : Fragment() {

    private lateinit var allReviewsRecyclerView: RecyclerView
    private lateinit var reviewAdapter: ReviewAdapter
    private var gameId: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_all_reviews, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        allReviewsRecyclerView = view.findViewById(R.id.allReviewsRecyclerView)
        reviewAdapter = ReviewAdapter(emptyList(), "AllReviewsFragment")
        allReviewsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        allReviewsRecyclerView.adapter = reviewAdapter

        gameId = arguments?.getInt("gameId") ?: -1
        if (gameId != -1) {
            loadAllReviews()
        }
    }

    private fun loadAllReviews() {
        FirebaseFirestore.getInstance().collection("reviews")
            .whereEqualTo("gameId", gameId)
            .get()
            .addOnSuccessListener { documents ->
                val reviewList = mutableListOf<Review>()
                for (document in documents) {
                    val review = document.toObject(Review::class.java)
                    reviewList.add(review)
                }
                reviewAdapter = ReviewAdapter(reviewList, "GameFragment")
            }
            .addOnFailureListener { exception ->
                println("Error al recibir documentos de BD: $exception")
            }
    }
}
