package com.example.gamerbox.fragment

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.gamerbox.R
import com.example.gamerbox.models.GameDetails
import com.example.gamerbox.network.RawgRepository
import com.example.gamerbox.network.RetrofitService
import com.example.gamerbox.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GameFragment : Fragment() {

    private lateinit var rawgRepository: RawgRepository

    // Inicializar las vistas
    private lateinit var gameTitleTextView: TextView
    private lateinit var gameDescriptionTextView: TextView
    private lateinit var gameDateTextView: TextView
    private lateinit var gameImageView: ImageView
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_game, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        gameTitleTextView = view.findViewById(R.id.gameDetailsTitleTextView)
        gameDescriptionTextView = view.findViewById(R.id.gameDetailsDescriptionTextView)
        gameDateTextView = view.findViewById(R.id.gameDetailsDatetextView)
        gameImageView = view.findViewById(R.id.gameDetailsImageView)

        val gameId = arguments?.getInt("gameId") ?: -1
        if (gameId != -1) {
            // Inicializar el servicio y el repositorio
            val rawgService = RetrofitService.create()
            rawgRepository = RawgRepository(rawgService)

            // Obtener los detalles del juego desde la API
            lifecycleScope.launch {
                try {
                    val gameDetails = withContext(Dispatchers.IO) {
                        rawgRepository.getGameDetails(gameId, Constants.API_KEY)
                    }
                    if (gameDetails != null) {
                        updateUI(gameDetails)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    // Manejar el error al obtener los detalles del juego
                }
            }
        } else {
            // Manejar el caso donde no se proporciona un ID de juego válido
        }
    }

    private fun updateUI(gameDetails: GameDetails) {
        // Actualizar la UI con los detalles del juego
        gameTitleTextView.text = gameDetails.name

        val description = Html.fromHtml(gameDetails.description).toString()
        gameDescriptionTextView.text = description

        gameDateTextView.text = gameDetails.releaseDate


        // Cargar la imagen del juego utilizando Glide
        Glide.with(requireContext())
            .load(gameDetails.backgroundImageUrl)
            .into(gameImageView)
    }
}
