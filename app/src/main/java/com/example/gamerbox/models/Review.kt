package com.example.gamerbox.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "reviews")
data class Review(
    @PrimaryKey(autoGenerate = true)
    var id: String = "",
    val reviewText: String,
    val rating: Float,
    val date: Date,
    val isFavorite: Boolean,
    var likes: MutableList<String>,
    val userId: String,
    val gameId: Int
    )
{
    constructor() : this("","", 0.0f, Date(), false, mutableListOf(), "", 0)
}



