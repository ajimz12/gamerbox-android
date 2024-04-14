package com.example.gamerbox.models

import com.google.gson.annotations.SerializedName

data class GameList(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("background_iage")
    val image: String
)