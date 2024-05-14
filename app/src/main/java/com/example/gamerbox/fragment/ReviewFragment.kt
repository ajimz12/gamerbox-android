package com.example.gamerbox.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.gamerbox.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*

class ReviewFragment : Fragment() {

    private lateinit var editTextReview: EditText
    private lateinit var ratingBar: RatingBar
    private lateinit var imageViewFavorite: ImageView
    private lateinit var datePicker: DatePicker
    private lateinit var btnSendReview: Button
    private var isFavorite: Boolean = false
    private var gameId: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_review, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        editTextReview = view.findViewById(R.id.editTextReview)
        ratingBar = view.findViewById(R.id.ratingBar)
        imageViewFavorite = view.findViewById(R.id.imageViewFavorite)
        datePicker = view.findViewById(R.id.datePicker1)
        btnSendReview = view.findViewById(R.id.btnSendReview)

        gameId = arguments?.getInt("gameId") ?: -1
        if (gameId == -1) {
            Toast.makeText(requireContext(), "GameId no encontrado", Toast.LENGTH_SHORT).show()
            requireActivity().onBackPressed()
        }

        imageViewFavorite.setOnClickListener {
            isFavorite = !isFavorite
            imageViewFavorite.setImageResource(if (isFavorite) R.drawable.ic_heart_selected else R.drawable.ic_heart)
        }

        btnSendReview.setOnClickListener {
            saveReview()
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
            Toast.makeText(requireContext(), "Introduce una reseña", Toast.LENGTH_SHORT).show()
            return
        }

        val currentUser = FirebaseAuth.getInstance().currentUser

        val review = hashMapOf(
            "gameId" to gameId,
            "reviewText" to reviewText,
            "rating" to rating,
            "date" to date,
            "isFavorite" to isFavorite,
            "userId" to currentUser?.uid,
            "userEmail" to currentUser?.email
        )

        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    FirebaseFirestore.getInstance().collection("reviews").add(review).await()
                }
                Toast.makeText(requireContext(), "Review guardada", Toast.LENGTH_SHORT).show()
                requireActivity().onBackPressed()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error al guardar review: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
