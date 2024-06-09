package com.example.gamerbox.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.gamerbox.R
import com.example.gamerbox.models.Review
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*

class CreateReviewFragment : Fragment() {

    private lateinit var editTextReview: EditText
    private lateinit var ratingBar: RatingBar
    private lateinit var datePicker: DatePicker
    private lateinit var btnSendReview: Button
    private lateinit var backArrowImage: ImageButton
    private var likes: MutableList<String> = mutableListOf()
    private var gameId: Int = -1
    private var reviewId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_create_review, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        editTextReview = view.findViewById(R.id.editTextReview)
        ratingBar = view.findViewById(R.id.ratingBar)
        datePicker = view.findViewById(R.id.datePicker1)
        btnSendReview = view.findViewById(R.id.btnSendReview)
        backArrowImage = view.findViewById(R.id.createReviewBackArrow)

        backArrowImage.setOnClickListener {
            findNavController().popBackStack()
        }

        gameId = arguments?.getInt("gameId") ?: -1

        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.uid?.let { userId ->
            lifecycleScope.launch {
                val review = getReviewByUserAndGame(userId, gameId)
                if (review != null) {
                    reviewId = review.id
                    editTextReview.setText(review.reviewText)
                    ratingBar.rating = review.rating

                    val calendar = Calendar.getInstance()
                    calendar.time = review.date
                    datePicker.updateDate(
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    )
                }
            }
        }

        btnSendReview.setOnClickListener {
            val reviewText = editTextReview.text.toString()
            val rating = ratingBar.rating
            val calendar = Calendar.getInstance().apply {
                set(datePicker.year, datePicker.month, datePicker.dayOfMonth)
            }
            val date = calendar.time

            if (reviewText.isBlank()) {
                Toast.makeText(requireContext(), R.string.empty_review_text, Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            if (rating < 1) {
                Toast.makeText(requireContext(), R.string.no_stars_review, Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            if (reviewId != null) {
                lifecycleScope.launch {
                    try {
                        updateReview(reviewId!!, reviewText, rating, date)
                        Toast.makeText(
                            requireContext(),
                            R.string.review_updated,
                            Toast.LENGTH_SHORT
                        ).show()
                        setFragmentResult("reviewUpdated", bundleOf("reviewId" to reviewId))
                        requireActivity().onBackPressed()
                    } catch (e: Exception) {
                        println(e.message)
                    }
                }
            } else {
                saveReview()
            }
        }

    }

    private suspend fun updateReview(
        reviewId: String,
        reviewText: String,
        rating: Float,
        date: Date
    ) {
        val reviewRef = FirebaseFirestore.getInstance().collection("reviews").document(reviewId)
        val data = hashMapOf(
            "reviewText" to reviewText,
            "rating" to rating,
            "date" to date,
        )
        withContext(Dispatchers.IO) {
            reviewRef.update(data as Map<String, Any>).await()
        }
    }

    private fun saveReview() {
        val reviewText = editTextReview.text.toString()
        val rating = ratingBar.rating
        val calendar = Calendar.getInstance().apply {
            set(datePicker.year, datePicker.month, datePicker.dayOfMonth)
        }
        val date = calendar.time

        if (reviewText.isBlank()) {
            Toast.makeText(requireContext(), R.string.empty_review_text, Toast.LENGTH_SHORT).show()
            return
        }

        if (rating < 1) {
            Toast.makeText(requireContext(), R.string.no_stars_review, Toast.LENGTH_SHORT).show()
            return
        }

        val currentUser = FirebaseAuth.getInstance().currentUser

        val review = hashMapOf(
            "gameId" to gameId,
            "reviewText" to reviewText,
            "rating" to rating,
            "date" to date,
            "likes" to likes,
            "userId" to currentUser?.uid,
            "userEmail" to currentUser?.email
        )

        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    FirebaseFirestore.getInstance().collection("reviews").add(review).await()
                }
                Toast.makeText(requireContext(), R.string.review_sent, Toast.LENGTH_SHORT).show()
                requireActivity().onBackPressed()
            } catch (e: Exception) {
                println(e.message)
            }
        }
    }

    private suspend fun getReviewByUserAndGame(userId: String, gameId: Int): Review? {
        return withContext(Dispatchers.IO) {
            val querySnapshot = FirebaseFirestore.getInstance().collection("reviews")
                .whereEqualTo("userId", userId)
                .whereEqualTo("gameId", gameId)
                .limit(1)
                .get()
                .await()

            if (!querySnapshot.isEmpty) {
                val documentSnapshot = querySnapshot.documents[0]
                val review = documentSnapshot.toObject(Review::class.java)
                review?.id = documentSnapshot.id
                review
            } else {
                null
            }
        }
    }
}
