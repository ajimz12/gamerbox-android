package com.example.gamerbox.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.gamerbox.models.Review

@Dao
interface ReviewDao {

    @Insert
    suspend fun insertReview(review: Review)

    @Query("SELECT * FROM reviews WHERE gameId = :gameId")
    suspend fun getReviewsForGame(gameId: Int): List<Review>
}
