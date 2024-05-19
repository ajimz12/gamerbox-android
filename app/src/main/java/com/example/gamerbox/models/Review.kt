package com.example.gamerbox.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "reviews")
data class Review(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val reviewText: String,
    val rating: Float,
    val date: Date,
    val isFavorite: Boolean,
    var likes: Int,
    val userId: String,
    val gameId: Int
    )
{
    constructor() : this(0,"", 0.0f, Date(), false, 0, "", 0)
}



