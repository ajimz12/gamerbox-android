package com.example.gamerbox.network

import com.example.gamerbox.models.Game
import com.example.gamerbox.models.GameDetails

class RawgRepository(private val rawgService: RawgService) {

    suspend fun getPopularGames(apiKey: String): List<Game>? {
        val response = rawgService.getPopularGames(apiKey)

        return if (response.isSuccessful) {
            response.body()?.results
        } else {
            null
        }
    }

    suspend fun getPopularGamesForLastYear(apiKey: String): List<Game>? {
        val dates = RawgService.getDatesForLastYear()
        val ordering = "-added"
        val response = rawgService.getPopularRecentGames(dates, ordering, apiKey)

        return if (response.isSuccessful) {
            response.body()?.results
        } else {
            null
        }
    }

    suspend fun getGameDetails(gameId: Int, apiKey: String): GameDetails? {
        val response = rawgService.getGameDetails(gameId, apiKey)
        return if (response.isSuccessful) {
            response.body()
        } else {
            null
        }
    }

    suspend fun searchGamesByTitle(title: String, apiKey: String): List<Game>? {
        val response = rawgService.searchGamesByTitle(title, apiKey)
        return if (response.isSuccessful) {
            response.body()?.results
        } else {
            null
        }
    }

}
