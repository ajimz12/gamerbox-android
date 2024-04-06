package com.example.gamerbox.model

import com.google.gson.annotations.SerializedName

data class GameList(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val nombre: String,
    @SerializedName("background_image")
    val imagen: String
)