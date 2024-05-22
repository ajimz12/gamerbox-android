package com.example.gamerbox.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gamerbox.R
import com.example.gamerbox.models.Game
import com.example.gamerbox.adapter.GameAdapter
import com.example.gamerbox.network.RawgRepository
import com.example.gamerbox.network.RetrofitService
import com.example.gamerbox.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchFragment : Fragment() {

    private lateinit var rawgRepository: RawgRepository
    private lateinit var gameAdapter: GameAdapter
    private lateinit var searchEditText: EditText
    private lateinit var gamesRecyclerView: RecyclerView

    private var isSelectingFavorite: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchEditText = view.findViewById(R.id.searchEditText)
        gamesRecyclerView = view.findViewById(R.id.searchResultsRecyclerView)

        gamesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        gameAdapter = GameAdapter { game -> onGameClick(game) }
        gamesRecyclerView.adapter = gameAdapter

        // Inicializar el repositorio
        val rawgService = RetrofitService.create()
        rawgRepository = RawgRepository(rawgService)

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                searchGames(query)
            }
        })

        // Recibir el argumento
        isSelectingFavorite = arguments?.getBoolean("isSelectingFavorite") ?: false
    }

    private fun searchGames(query: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val games = rawgRepository.searchGamesByTitle(query, Constants.API_KEY)
            games?.let {
                withContext(Dispatchers.Main) {
                    gameAdapter.updateData(it)
                }
            }
        }
    }

    private fun onGameClick(game: Game) {
        if (isSelectingFavorite) {
            // Volver al ProfileFragment con el gameId del juego seleccionado
            val navController = findNavController()
            navController.previousBackStackEntry?.savedStateHandle?.set("selectedGameId", game.id)
            navController.popBackStack()
        } else {
            // Navegar al GameFragment
            val bundle = Bundle()
            bundle.putInt("gameId", game.id)
            findNavController().navigate(R.id.gameFragment, bundle)
        }
    }
}

