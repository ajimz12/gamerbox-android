package com.example.gamerbox.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.gamerbox.R
import com.example.gamerbox.adapter.ReviewAdapter
import com.example.gamerbox.models.GameDetails
import com.example.gamerbox.models.Review
import com.example.gamerbox.network.RawgRepository
import com.example.gamerbox.network.RetrofitService
import com.example.gamerbox.utils.Constants
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat

class GameFragment : Fragment() {

    private lateinit var rawgRepository: RawgRepository

    private lateinit var gameTitleTextView: TextView
    private lateinit var gameDescriptionTextView: TextView
    private lateinit var gameDateTextView: TextView
    private lateinit var gameImageView: ImageView
    private lateinit var gameAdditionalImageView: ImageView
    private lateinit var gamemetacriticTextView: TextView
    private lateinit var fab: FloatingActionButton
    private lateinit var reviewRecyclerView: RecyclerView
    private lateinit var noReviewsTextView: TextView
    private lateinit var reviewAdapter: ReviewAdapter
    private var gameId: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_game, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        gameTitleTextView = view.findViewById(R.id.gameDetailsTitleTextView)
        gameDescriptionTextView = view.findViewById(R.id.gameDetailsDescriptionTextView)
        gameDateTextView = view.findViewById(R.id.gameDetailsDatetextView)
        gameImageView = view.findViewById(R.id.gameDetailsImageView)
        gameAdditionalImageView = view.findViewById(R.id.gameDetailsAdditionalImageView)
        gamemetacriticTextView = view.findViewById(R.id.gameDetailsMetacriticTextView)
        fab = view.findViewById(R.id.actionGameFab)
        reviewRecyclerView = view.findViewById(R.id.reviewsRecyclerView)
        noReviewsTextView = view.findViewById(R.id.noReviewsTextView)

        gameId = arguments?.getInt("gameId") ?: -1
        if (gameId != -1) {
            // Inicializar el servicio y repositorio
            val rawgService = RetrofitService.create()
            rawgRepository = RawgRepository(rawgService)

            // Configurar el RecyclerView para mostrar las reseñas
            reviewAdapter = ReviewAdapter()
            reviewRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            reviewRecyclerView.adapter = reviewAdapter

            // Obtener los detalles del juego desde la API
            lifecycleScope.launch {
                try {
                    val gameDetails = withContext(Dispatchers.IO) {
                        rawgRepository.getGameDetails(gameId, Constants.API_KEY)
                    }
                    if (gameDetails != null) {
                        updateUI(gameDetails)
                        loadReviews()
                    }
                } catch (e: Exception) {
                    println(e.message)
                }
            }
        }

        fab.setOnClickListener {
            showBottomSheetMenu()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Limpiar el RecyclerView
        reviewRecyclerView.adapter = null
    }

    @SuppressLint("SimpleDateFormat")
    private fun updateUI(gameDetails: GameDetails) {
        // Actualizar la interfaz con los detalles del juego
        gameTitleTextView.text = gameDetails.name

        val description = Html.fromHtml(gameDetails.description, 0).toString()
        gameDescriptionTextView.text = description

        // Pasar fecha de API a solo el año
        val releaseDate = gameDetails.releaseDate
        val inputFormat = SimpleDateFormat("yyyy-MM-dd")
        val outputFormat = SimpleDateFormat("yyyy")
        val date = inputFormat.parse(releaseDate)
        val year = outputFormat.format(date)

        gameDateTextView.text = year

        val metacritic = gameDetails.metacritic
        gamemetacriticTextView.text = metacritic.toString()

        when {
            metacritic <= 30 -> gamemetacriticTextView.setBackgroundResource(R.color.red)
            metacritic <= 40 -> gamemetacriticTextView.setBackgroundResource(R.color.lightRed)
            metacritic <= 60 -> gamemetacriticTextView.setBackgroundResource(R.color.red)
            metacritic <= 80 -> gamemetacriticTextView.setBackgroundResource(R.color.yellow)
            else -> gamemetacriticTextView.setBackgroundResource(R.color.green)
        }

        // Cargar la imagen del juego
        Glide.with(requireContext())
            .load(gameDetails.backgroundImageUrl)
            .into(gameAdditionalImageView)

        Glide.with(requireContext())
            .load(gameDetails.backgroundAdditionalImageUrl)
            .into(gameImageView)
    }

    private fun loadReviews() {
        FirebaseFirestore.getInstance().collection("reviews")
            .whereEqualTo("gameId", gameId)
            .get()
            .addOnSuccessListener { documents ->
                val reviewList = mutableListOf<Review>()
                for (document in documents) {
                    val review = document.toObject(Review::class.java)
                    reviewList.add(review)
                }
                if (reviewList.isEmpty()) {
                    noReviewsTextView.visibility = View.VISIBLE
                    reviewRecyclerView.visibility = View.GONE
                } else {
                    noReviewsTextView.visibility = View.GONE
                    reviewRecyclerView.visibility = View.VISIBLE
                    reviewAdapter.submitList(reviewList)
                }
            }
            .addOnFailureListener { exception ->
                println("Error al recibir documentos de BD: $exception")
            }
    }

    private fun showBottomSheetMenu() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_menu, null)
        bottomSheetDialog.setContentView(bottomSheetView)

        val reviewButton = bottomSheetView.findViewById<View>(R.id.menuReview)

        reviewButton.setOnClickListener {
            if (gameId != -1) {
                findNavController().navigate(R.id.action_game_to_review, Bundle().apply {
                    putInt("gameId", gameId)
                })

            } else {
                Toast.makeText(requireContext(), "GameId no encontrado", Toast.LENGTH_SHORT).show()
            }
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }
}