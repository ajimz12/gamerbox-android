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
import com.example.gamerbox.models.Game
import com.example.gamerbox.models.GameAdapter
import com.example.gamerbox.network.RawgRepository
import com.example.gamerbox.network.RetrofitService
import com.example.gamerbox.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {

    private lateinit var popularGamesRecyclerView: RecyclerView
    private lateinit var recentGamesRecyclerView: RecyclerView
    private lateinit var gamesAdapter: GameAdapter
    private lateinit var recentGamesAdapter: GameAdapter
    private val popularGamesList = mutableListOf<Game>()
    private val recentGamesList = mutableListOf<Game>()
    private lateinit var rawgRepository: RawgRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_home, container, false)

        popularGamesRecyclerView = view.findViewById(R.id.popularGamesRecyclerView)
        recentGamesRecyclerView = view.findViewById(R.id.recentGamesRecyclerView)

        gamesAdapter = GameAdapter(popularGamesList) { game ->
            navigateToGameFragment(game.id)
        }
        recentGamesAdapter = GameAdapter(recentGamesList) { game ->
            navigateToGameFragment(game.id)
        }

        popularGamesRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recentGamesRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        popularGamesRecyclerView.adapter = gamesAdapter
        recentGamesRecyclerView.adapter = recentGamesAdapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rawgService = RetrofitService.create()
        rawgRepository = RawgRepository(rawgService)

        lifecycleScope.launch {
            try {
                // Obtener juegos populares
                val popularGames = withContext(Dispatchers.IO) {
                    rawgRepository.getPopularGames(Constants.API_KEY)
                }
                popularGamesList.clear()
                popularGames?.let { popularGamesList.addAll(it) }
                gamesAdapter.notifyDataSetChanged()

                // Obtener juegos recientes
                val recentGames = withContext(Dispatchers.IO) {
                    rawgRepository.getRecentPopularGames(Constants.API_KEY)
                }
                recentGamesList.clear()
                recentGames?.let { recentGamesList.addAll(it) }
                recentGamesAdapter.notifyDataSetChanged()
            } catch (e: Exception) {
                // Manejar el error al obtener los juegos
                e.printStackTrace()
            }
        }
    }

    private fun navigateToGameFragment(gameId: Int) {
        val bundle = Bundle().apply {
            putInt("gameId", gameId)
        }

        // Navegar al GameFragment pasando el Bundle con el ID del juego
        findNavController().navigate(R.id.action_home_to_game, bundle)
    }

}
