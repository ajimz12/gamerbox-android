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

class UserReviewsFragment : Fragment() {

    private lateinit var userReviewsRecyclerView: RecyclerView
    private lateinit var userReviewBackArrow: ImageButton
    private lateinit var reviewAdapter: ReviewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_user_reviews, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userReviewsRecyclerView = view.findViewById(R.id.userReviewsRecyclerView)
        userReviewBackArrow = view.findViewById(R.id.userReviewBackArrowImage)
        userReviewsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        loadAllUserReviews()

        userReviewBackArrow.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun loadAllUserReviews() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance().collection("reviews")
            .whereEqualTo("userId", userId)
            .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val reviewList = mutableListOf<Review>()
                for (document in documents) {
                    val review = document.toObject(Review::class.java)
                    reviewList.add(review)
                }
                if (reviewList.isNotEmpty()) {
                    reviewAdapter = ReviewAdapter(reviewList, "UserReviewsFragment")
                    userReviewsRecyclerView.adapter = reviewAdapter
                }
            }
            .addOnFailureListener { exception ->
                println("Error al recibir documentos de BD: $exception")
            }
    }
}
