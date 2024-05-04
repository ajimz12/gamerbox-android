package com.example.gamerbox.models

import com.google.gson.annotations.SerializedName

data class GameList(
    @SerializedName("results") val results: List<Game>
)


