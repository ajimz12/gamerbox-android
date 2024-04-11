package com.example.gamerbox.models

import com.google.gson.annotations.SerializedName

data class Game(
    @SerializedName("counts")
    val total: Int,
    @SerializedName("results")
    val gameList: List<GameList>
)


