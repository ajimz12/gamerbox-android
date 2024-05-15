package com.example.gamerbox.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "reviews")
data class Review(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val gameId: Int,
    val reviewText: String,
    val rating: Float,
    val date: Date,
    val isFavorite: Boolean,
    val userId: String
)
{
    constructor() : this(0, 0, "", 0.0f, Date(), false, "")
}


