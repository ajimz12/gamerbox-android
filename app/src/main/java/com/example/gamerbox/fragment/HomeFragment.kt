package com.example.gamerbox.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gamerbox.R
import com.example.gamerbox.adapter.GameAdapter
import com.example.gamerbox.network.RawgRepository
import com.example.gamerbox.utils.Constants
import com.example.gamerbox.viewmodel.HomeViewModel
import com.example.gamerbox.viewmodel.HomeViewModelFactory

class HomeFragment : Fragment() {

    private lateinit var popularGamesRecyclerView: RecyclerView
    private lateinit var recentGamesRecyclerView: RecyclerView
    private lateinit var gamesAdapter: GameAdapter
    private lateinit var recentGamesAdapter: GameAdapter
    private lateinit var progressBar: ProgressBar

    private val homeViewModel: HomeViewModel by viewModels {
        HomeViewModelFactory(rawgRepository)
    }

    private val rawgService = RetrofitService.create()
    private val rawgRepository = RawgRepository(rawgService)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        popularGamesRecyclerView = view.findViewById(R.id.popularGamesRecyclerView)
        recentGamesRecyclerView = view.findViewById(R.id.recentGamesRecyclerView)
        progressBar = view.findViewById(R.id.progressBar)

        gamesAdapter = GameAdapter { game ->
            navigateToGameFragment(game.id)
        }

        recentGamesAdapter = GameAdapter { game ->
            navigateToGameFragment(game.id)
        }

        popularGamesRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recentGamesRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        popularGamesRecyclerView.adapter = gamesAdapter
        recentGamesRecyclerView.adapter = recentGamesAdapter

        return view
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        homeViewModel.fetchGames(Constants.API_KEY)

        homeViewModel.popularGames.observe(viewLifecycleOwner) { popularGames ->
            gamesAdapter.submitList(popularGames)
            gamesAdapter.notifyDataSetChanged()
        }

        homeViewModel.recentGames.observe(viewLifecycleOwner) { recentGames ->
            recentGamesAdapter.submitList(recentGames)
            recentGamesAdapter.notifyDataSetChanged()
        }

        homeViewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun navigateToGameFragment(gameId: Int) {
        val bundle = Bundle().apply {
            putInt("gameId", gameId)
        }
        findNavController().navigate(R.id.action_home_to_game, bundle)
    }
}
