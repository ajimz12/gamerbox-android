package com.example.gamerbox.api

import com.example.gamerbox.model.Game
import retrofit2.Call
import retrofit2.http.GET

interface RawgApiService {
    @GET("games")
    fun getGames(): Call<List<Game>>
}
