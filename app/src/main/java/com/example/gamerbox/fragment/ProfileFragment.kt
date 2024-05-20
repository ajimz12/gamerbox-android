package com.example.gamerbox.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gamerbox.R
import com.example.gamerbox.adapter.ReviewAdapter
import com.example.gamerbox.models.Review
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private lateinit var reviewRecyclerView: RecyclerView
    private lateinit var reviewAdapter: ReviewAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        reviewRecyclerView = view.findViewById(R.id.profileReviewRecyclerView)
        reviewRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        loadUserReviews()
    }

    private fun loadUserReviews() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance().collection("reviews")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                val reviewList = mutableListOf<Review>()
                for (document in documents) {
                    val review = document.toObject(Review::class.java)
                    reviewList.add(review)
                }
                if (reviewList.isNotEmpty()) {
                    reviewAdapter = ReviewAdapter(reviewList, "ProfileFragment")
                    reviewRecyclerView.adapter = reviewAdapter
                }
            }
            .addOnFailureListener { exception ->
                println("Error al recibir documentos de BD: $exception")
            }
    }
}
