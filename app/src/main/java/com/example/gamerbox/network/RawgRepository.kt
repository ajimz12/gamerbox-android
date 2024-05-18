package com.example.gamerbox.network

import com.example.gamerbox.models.Game
import com.example.gamerbox.models.GameDetails

class RawgRepository(private val rawgAPI: RawgService) {

    suspend fun getPopularGames(apiKey: String): List<Game>? {
        val response = rawgAPI.getPopularGames(apiKey)

        return if (response.isSuccessful) {
            response.body()?.results
        } else {
            null
        }
    }

    suspend fun getRecentPopularGames(apiKey: String): List<Game>? {
        val dates = RawgService.getDatesForLasYear()
        val ordering = "-added"
        val response = rawgAPI.getPopularRecentGames(dates, ordering, apiKey)

        return if (response.isSuccessful) {
            response.body()?.results
        } else {
            null
        }
    }

    suspend fun getGameDetails(gameId: Int, apiKey: String): GameDetails? {
        val response = rawgAPI.getGameDetails(gameId, apiKey)
        return if (response.isSuccessful) {
            response.body()
        } else {
            null
        }
    }

    suspend fun searchGamesByTitle(title: String, apiKey: String): List<Game>? {
        val response = rawgAPI.searchGamesByTitle(title, apiKey)
        return if (response.isSuccessful) {
            response.body()?.results
        } else {
            null
        }
    }

}
