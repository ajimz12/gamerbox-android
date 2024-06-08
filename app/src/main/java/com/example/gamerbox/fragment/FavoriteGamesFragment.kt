package com.example.gamerbox.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.gamerbox.R
import com.example.gamerbox.models.Game
import com.example.gamerbox.network.RawgRepository
import com.example.gamerbox.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FavoriteGamesFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var favoriteGameSlot1: ImageView
    private lateinit var favoriteGameSlot2: ImageView
    private lateinit var favoriteGameSlot3: ImageView
    private lateinit var favoriteGameSlot4: ImageView
    private lateinit var backArrowImage: ImageView
    private val favoriteGames: MutableList<Game?> = MutableList(4) { null }

    private lateinit var rawgRepository: RawgRepository

    private var selectedSlot: ImageView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_favorite_games, container, false)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        favoriteGameSlot1 = view.findViewById(R.id.favoriteGameSlot1)
        favoriteGameSlot2 = view.findViewById(R.id.favoriteGameSlot2)
        favoriteGameSlot3 = view.findViewById(R.id.favoriteGameSlot3)
        favoriteGameSlot4 = view.findViewById(R.id.favoriteGameSlot4)

        backArrowImage = view.findViewById(R.id.favoriteGamesBackArrow)

        val rawgService = RetrofitService.create()
        rawgRepository = RawgRepository(rawgService)

        favoriteGameSlot1.setOnClickListener { onFavoriteSlotClick(favoriteGameSlot1) }
        favoriteGameSlot2.setOnClickListener { onFavoriteSlotClick(favoriteGameSlot2) }
        favoriteGameSlot3.setOnClickListener { onFavoriteSlotClick(favoriteGameSlot3) }
        favoriteGameSlot4.setOnClickListener { onFavoriteSlotClick(favoriteGameSlot4) }

        loadFavoriteGames()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Int>("selectedGameId")
            ?.observe(viewLifecycleOwner) { gameId ->
                lifecycleScope.launch {
                    val game = findGameById(gameId)
                    game?.let {
                        setFavoriteGame(it)
                    }
                }
            }

        backArrowImage.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun onFavoriteSlotClick(slot: ImageView) {
        selectedSlot = slot
        val bundle = Bundle().apply {
            putBoolean("isSelectingFavorite", true)
        }
        findNavController().navigate(R.id.action_favorite_to_search, bundle)
    }

    private fun setFavoriteGameSlots(favoriteGames: List<Game?>) {
        val slots =
            listOf(favoriteGameSlot1, favoriteGameSlot2, favoriteGameSlot3, favoriteGameSlot4)

        slots.forEachIndexed { index, slot ->
            val game = favoriteGames[index]
            if (game != null) {
                Glide.with(this)
                    .load(game.backgroundImageUrl)
                    .into(slot)
                slot.setOnClickListener { showOptionsDialog(game) }
            } else {
                slot.setImageResource(R.drawable.add_favorite_image)
                slot.setOnClickListener { onFavoriteSlotClick(slot) }
            }
        }
    }

    private fun showOptionsDialog(game: Game) {
        AlertDialog.Builder(requireContext())
            .setTitle("Opciones del juego favorito")
            .setItems(arrayOf("Eliminar")) { _, which ->
                when (which) {
                    0 -> removeFavoriteGame(game)
                }
            }
            .show()
    }

    private fun removeFavoriteGame(game: Game) {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId)
            .update("favoriteGames", FieldValue.arrayRemove(game.id))
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Juego eliminado de favoritos", Toast.LENGTH_SHORT)
                    .show()
                val index = favoriteGames.indexOf(game)
                if (index != -1) {
                    favoriteGames[index] = null
                    setFavoriteGameSlots(favoriteGames)
                }
            }
            .addOnFailureListener { e ->
                println("Error al eliminar el juego favorito: $e")
            }
    }


    private fun setFavoriteGame(game: Game) {
        selectedSlot?.let { slot ->
            Glide.with(this)
                .load(game.backgroundImageUrl)
                .into(slot)
            saveFavoriteGameToFirestore(game)
            slot.setOnClickListener { showOptionsDialog(game) }
            selectedSlot = null
        }
    }


    private fun saveFavoriteGameToFirestore(game: Game) {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId)
            .update("favoriteGames", FieldValue.arrayUnion(game.id))
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Juego añadido a favoritos", Toast.LENGTH_SHORT)
                    .show()

                loadFavoriteGames()
            }
            .addOnFailureListener { e ->
                println("Error al añadir el juego favorito: $e")
            }
    }


    private suspend fun findGameById(gameId: Int): Game? {
        val gameDetails = rawgRepository.getGameDetails(gameId, Constants.API_KEY)
        return gameDetails?.let {
            Game(
                id = it.id,
                name = it.name,
                backgroundImageUrl = it.backgroundImageUrl,
                releaseDate = it.releaseDate
            )
        }
    }

    private fun loadFavoriteGames() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val favoriteGamesIds =
                        document.get("favoriteGames") as? List<Int> ?: emptyList()
                    if (favoriteGamesIds.isNotEmpty()) {
                        lifecycleScope.launch {
                            val loadedGames = favoriteGamesIds.mapNotNull { findGameById(it) }
                            for (i in loadedGames.indices) {
                                favoriteGames[i] = loadedGames[i]
                            }
                            withContext(Dispatchers.Main) {
                                setFavoriteGameSlots(favoriteGames)
                            }
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                println(e.message)
            }
    }
}
