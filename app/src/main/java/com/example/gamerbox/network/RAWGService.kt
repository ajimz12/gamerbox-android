package com.example.gamerbox.network

import com.example.gamerbox.models.Game
import com.example.gamerbox.utils.Constants
import retrofit2.Response
import retrofit2.http.GET

interface RAWGService {
    @GET("games${Constants.API_KEY}")
    suspend fun getGames(): Response<Game>
}