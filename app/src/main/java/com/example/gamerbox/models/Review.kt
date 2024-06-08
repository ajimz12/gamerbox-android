package com.example.gamerbox.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.util.*

@Entity(tableName = "reviews")
data class Review(
    @PrimaryKey(autoGenerate = true)
    var id: String,
    val reviewText: String,
    val rating: Float,
    val date: Date,
    var likes: MutableList<String>,
    val userId: String,
    val gameId: Int
    )  {
    constructor() : this("","", 0.0f, Date(), mutableListOf(), "", 0)
}



