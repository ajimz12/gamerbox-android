package com.example.gamerbox.network

import com.example.gamerbox.models.GameDetails
import com.example.gamerbox.models.GameList
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

interface RawgService {
    @GET("games")
    suspend fun getPopularGames(
        @Query("key") apiKey: String
    ): Response<GameList>

    @GET("games")
    suspend fun getPopularRecentGames(
        @Query("dates") dates: String,
        @Query("ordering") ordering: String,
        @Query("key") apiKey: String
    ): Response<GameList>

    @GET("games/{id}")
    suspend fun getGameDetails(
        @Path("id") gameId: Int,
        @Query("key") apiKey: String
    ): Response<GameDetails>

    @GET("games")
    suspend fun searchGamesByTitle(
        @Query("search") title: String,
        @Query("key") apiKey: String
    ): Response<GameList>

    companion object {
        fun getDatesForLasYear(): String {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -365)

            val endDate = Date()
            val startDate = calendar.time

            return "${dateFormat.format(startDate)},${dateFormat.format(endDate)}"
        }
    }
}
