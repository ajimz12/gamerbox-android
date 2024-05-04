package com.example.gamerbox.network

import com.example.gamerbox.models.Game
import com.example.gamerbox.models.GameList
import com.example.gamerbox.utils.Constants
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface RawgService {

    @GET("games")
    suspend fun getPopularGames(
        @Query("key") apiKey: String
    ): Response<GameList>

    @GET("https://api.rawg.io/api/games?dates=2024-05-01,2024-05-04&ordering=-added&key=" + Constants.API_KEY)
    suspend fun getPopularRecentGames(
        @Query("key") apiKey: String
    ): Response<GameList>

}
