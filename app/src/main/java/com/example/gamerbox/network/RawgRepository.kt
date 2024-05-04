package com.example.gamerbox.network

import com.example.gamerbox.models.Game
import com.example.gamerbox.models.GameList

class RawgRepository(private val rawgAPI: RawgService) {

    suspend fun getPopularGames(apiKey: String): List<Game>? {
        val response = rawgAPI.getPopularGames(apiKey)

        if (response.isSuccessful) {
            return response.body()?.results
        } else {
            // Manejar el error
            return null
        }
    }

    suspend fun getRecentPopularGames(apiKey: String): List<Game>? {
        val response = rawgAPI.getPopularRecentGames(apiKey)

        if (response.isSuccessful) {
            return response.body()?.results
        } else {
            // Manejar el error
            return null
        }
    }
}
