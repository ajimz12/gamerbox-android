package com.example.gamerbox.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gamerbox.R
import com.example.gamerbox.models.Game
import com.example.gamerbox.models.GameAdapter
import com.example.gamerbox.network.RawgRepository
import com.example.gamerbox.network.RetrofitService
import com.example.gamerbox.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchFragment : Fragment() {

    private lateinit var rawgRepository: RawgRepository
    private lateinit var gameAdapter: GameAdapter
    private lateinit var searchEditText: EditText
    private lateinit var gamesRecyclerView: RecyclerView

    private var searchJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchEditText = view.findViewById(R.id.searchEditText)
        gamesRecyclerView = view.findViewById(R.id.searchResultsRecyclerView)

        // Inicializar el RecyclerView
        gamesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        gameAdapter = GameAdapter { game -> onGameClick(game) }
        gamesRecyclerView.adapter = gameAdapter

        // Inicializar el repositorio
        val rawgService = RetrofitService.create()
        rawgRepository = RawgRepository(rawgService)

        // Escuchar cambios en el texto de búsqueda
        searchEditText.setOnKeyListener { _, _, _ ->
            // Cancelar la búsqueda anterior si existe
            searchJob?.cancel()
            // Iniciar una nueva búsqueda con un retraso
            searchJob = lifecycleScope.launch {
                //delay(Constants.SEARCH_DELAY)
                searchGames()
            }
            false
        }
    }

    private fun searchGames() {
        val query = searchEditText.text.toString().trim()
        if (query.isNotEmpty()) {
            lifecycleScope.launch(Dispatchers.IO) {
                val games = rawgRepository.searchGamesByTitle(query, Constants.API_KEY)
                games?.let {
                    lifecycleScope.launch(Dispatchers.Main) {
                        gameAdapter.updateData(it)
                    }
                }
            }
        } else {
            gameAdapter.updateData(emptyList())
        }
    }

    private fun onGameClick(game: Game) {
        // Acción al hacer clic en un juego (por ejemplo, abrir detalles)
    }
}
